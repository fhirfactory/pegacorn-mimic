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

package net.fhirfactory.pegacorn.mimic.hl7v2server.cli.services;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class ORUEventHandler implements ReceivingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(ORUEventHandler.class);

    private static HapiContext context = new DefaultHapiContext();

    //
    // Constructor(s)
    //

    //
    // Business Methods
    //

    @Override
    public Message processMessage(Message receivedMessage, Map metaData) throws ReceivingApplicationException, HL7Exception {
        String receivedEncodedMessage = context.getPipeParser().encode(receivedMessage);
        getLogger().info(".processMessage()\n: Entry, receivedMessage->{}\n\n", receivedEncodedMessage);

        try {
            Message ackMessage = receivedMessage.generateACK();
            getLogger().info(".processMessage(): Exit ackMessage->{}", ackMessage);
            return (ackMessage);
        } catch (IOException e) {
            throw new HL7Exception(e);
        }
    }


    //
    // alternate version handlers
    //

    @Override
    public boolean canProcess(Message message) {

        return (true);
    }


    //
    // Getters and Setters
    //

    protected Logger getLogger(){
        return(LOG);
    }
}
