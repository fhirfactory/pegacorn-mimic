/*
 * Copyright (c) 2022 Mark Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.mimic.hl7v2server.cli.subcommands;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.HL7Service;
import ca.uhn.hl7v2.app.Initiator;
import net.fhirfactory.pegacorn.mimic.hl7v2server.cli.services.HL7V24A19QueryHandler;
import net.fhirfactory.pegacorn.mimic.hl7v2server.cli.services.QueryRoutingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
        name="QRYA19QueryServer",
        description="Server (Receiver) for HL7v2 Messages"
)
public class HL7v2QueryServer implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(HL7v2QueryServer.class);
    private HapiContext context;
    private HL7Service mllpServer;
    private Initiator initiator;

    private static final boolean USE_SECURE_SOCKET = false;

    @CommandLine.Option(names = {"-p", "--port"})
    private Integer port;

    @CommandLine.Option(names = {"-v", "--version"})
    private String version;

    @CommandLine.Option(names = {"-t", "--trigger"})
    private String triggerType;

    @CommandLine.Option(names = {"-m", "--message"})
    private String messageType;

    @CommandLine.Option(names = {"-i", "--processingid"})
    private String processingId;

    @Override
    public void run() {
        runQueryServer();
        System.exit(0);
    }

    public void runQueryServer(){
        LOG.info(".runQueryServer(): Entry");
        getLogger().info(".runQueryServer(): Entry (Constructor)");
        context = new DefaultHapiContext();
        getLogger().info(".runQueryServer(): Created Context");
        try {
            getLogger().info(".runQueryServer(): [Creating MLLP/HL7 Server] Start");
            mllpServer = context.newServer(port, USE_SECURE_SOCKET);
            getLogger().info(".runQueryServer(): [Creating MLLP/HL7 Server] Finish");
            getLogger().info(".runQueryServer(): [Creating Routing Service] Start");
            boolean hasParameters = StringUtils.isNotEmpty(version)
                    && StringUtils.isNotEmpty(triggerType)
                    && StringUtils.isNotEmpty(messageType)
                    && StringUtils.isNotEmpty(processingId);
            QueryRoutingService routingService;
            if(hasParameters){
                routingService = new QueryRoutingService(version, triggerType, processingId, messageType);
            } else {
                routingService = new QueryRoutingService();
            }
            getLogger().info(".runQueryServer(): [Creating Routing Service] Finish");
            getLogger().info(".runQueryServer(): [Creating ADT^A19 Handler] Start");
            HL7V24A19QueryHandler a19QueryHandler = new HL7V24A19QueryHandler();
            getLogger().info(".runQueryServer(): [Creating ADT^A19 Handler] Finish");
            getLogger().info(".runQueryServer(): [Registering ADT^A19 Handler] Start");
            mllpServer.registerApplication(routingService, a19QueryHandler);
            getLogger().info(".runQueryServer(): [Registering ADT^A19 Handler] Finish");
            //
            // Head to Loopy Land
            mllpServer.start();
            while(mllpServer.isRunning()){
                Thread.sleep(10000);
                getLogger().info("runQueryServer(): Still Running :)");
            }

        } catch (Exception ex){
            getLogger().info(".ForwardMLLPMessageService(): Failed to Connect, reason->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
    }

    //
    // Getters (and Setters)
    //

    protected Logger getLogger(){
        return(LOG);
    }
}
