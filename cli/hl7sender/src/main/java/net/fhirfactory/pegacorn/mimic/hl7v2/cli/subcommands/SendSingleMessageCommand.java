package net.fhirfactory.pegacorn.mimic.hl7v2.cli.subcommands;

import net.fhirfactory.pegacorn.mimic.hl7v2.cli.services.ForwardMLLPMessageService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

@CommandLine.Command(
        name="sendSingleMessage",
        description="Sends a Single MLLP Message from a file --> AS IS, without modification"
)
public class SendSingleMessageCommand implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(SendSingleMessageCommand.class);
    private Address actualAddress;
    private JChannel locationRPCClient;
    private RpcDispatcher rpcDispatcher;

    @CommandLine.Option(names = {"-f", "--filename"})
    private String fileName;

    @CommandLine.Option(names = {"-h", "--host"})
    private String hostName;

    @CommandLine.Option(names = {"-p", "--port"})
    private Integer port;

    String messageString;

    @Override
    public void run() {
        LOG.info(".run(): Entry, fileName --> {}", fileName);
        loadMessageFromFile();
        sendMessage();
        System.exit(0);
    }

    public void loadMessageFromFile(){
        LOG.info(".loadMessageFromFile(): Entry");
        try {
            LOG.info(".loadMessageFromFile(): Attempting to Access File");
            File file = new File(fileName);
            LOG.info(".loadMessageFromFile(): Created File Handle...");
            messageString  = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            LOG.info(".loadMessageFromFile(): Message->{}", messageString);
        } catch( Exception ex){
            LOG.info(".loadMessageFromFile(): Problem loading file, error->{}", ExceptionUtils.getMessage(ex));
            ex.printStackTrace();
        }

    }

    public void sendMessage(){
        LOG.info(".sendMessage(): Entry");
        ForwardMLLPMessageService mllpForwarder = new ForwardMLLPMessageService(hostName, port);
        LOG.info(".sendMessage(): mllpForwarder created");
        mllpForwarder.sendMessage(messageString);
        LOG.info(".sendMessage(): Exit");
    }
}
