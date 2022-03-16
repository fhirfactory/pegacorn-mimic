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
package net.fhirfactory.pegacorn.mimic.hl7v2.cli.services;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.Initiator;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;
import net.fhirfactory.pegacorn.mimic.hl7v2.cli.subcommands.SendSingleMessageCommand;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardMLLPMessageService {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardMLLPMessageService.class);

    private HapiContext context;
    private Connection connection;
    Initiator initiator;

    //
    // Constructor(s)
    //

    public ForwardMLLPMessageService(String hostname, Integer portNumber){
        getLogger().info(".ForwardMLLPMessageService(): Entry (Constructor)");
        context = new DefaultHapiContext();
        getLogger().info(".ForwardMLLPMessageService(): Created Context");
        try {
            // create a new MLLP client over the specified port
            connection = context.newClient(hostname, portNumber, false);
            getLogger().info(".ForwardMLLPMessageService(): Created Connection Client");
            // The initiator which will be used to transmit our message
            initiator = connection.getInitiator();
            getLogger().info(".ForwardMLLPMessageService(): Established Connection");
        } catch (Exception ex){
            getLogger().info(".ForwardMLLPMessageService(): Failed to Connect, reason->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }
    }

    //
    // Business Methods
    //

    public void sendMessage( Message message) {
        getLogger().info(".sendMessage(): Entry");
        try {
            // send HL7 message over the connection established
            Parser parser = context.getPipeParser();
            getLogger().info("Sending message:" + "\n" + parser.encode(message));
            Message response = initiator.sendAndReceive(message);

            // display the message response received from the remote party
            String responseString = parser.encode(response);
            getLogger().info("Received response:\n" + responseString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String messageString){
        getLogger().info(".sendMessage(): Entry");
        Parser parser = context.getPipeParser();
        getLogger().info(".sendMessage(): Parser Created");
        parser.getParserConfiguration().setValidating(false);
        getLogger().info(".sendMessage(): Set Parser Validation to -False-");
        try {
            Message message = parser.parse(messageString);
            getLogger().info(".sendMessage(): Parsed String into Message");
            sendMessage(message);
            getLogger().info(".sendMessage(): Sent Message");
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //
    // Getters (and Setters)
    //

    protected HapiContext getHAPIContext(){
        return(this.context);
    }

    protected Logger getLogger(){
        return(LOG);
    }

}
