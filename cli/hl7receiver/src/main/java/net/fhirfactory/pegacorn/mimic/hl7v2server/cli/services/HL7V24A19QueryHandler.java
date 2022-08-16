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

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ADR_A19;
import ca.uhn.hl7v2.model.v24.message.QRY_A19;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class HL7V24A19QueryHandler implements ReceivingApplication {
    private static final Logger LOG = LoggerFactory.getLogger(HL7V24A19QueryHandler.class);



    //
    // Constructor(s)
    //

    //
    // Business Methods
    //

    @Override
    public Message processMessage(Message message, Map map) throws ReceivingApplicationException, HL7Exception {
        getLogger().info(".processMessage(): Entry, message->{}", message);
        QRY_A19 queryMessage = (QRY_A19)message;
        ADR_A19 responseMessage = new ADR_A19();

        responseMessage.getMSH().getFieldSeparator().setValue(queryMessage.getMSH().getFieldSeparator().getValue());
        responseMessage.getMSH().getEncodingCharacters().setValue(queryMessage.getMSH().getEncodingCharacters().getValue());
        responseMessage.getMSH().getMessageControlID().setValue(queryMessage.getMSH().getMessageControlID().getValue());
        responseMessage.getMSH().getSendingApplication().getHd1_NamespaceID().setValue("DRICaTS-Mimic");
        responseMessage.getMSH().getSendingFacility().getHd1_NamespaceID().setValue("Sanctuary");
        responseMessage.getMSH().getReceivingApplication().getHd1_NamespaceID().setValue("DRICaTS-Mimic");
        responseMessage.getMSH().getDateTimeOfMessage().getTimeOfAnEvent().setValue(Date.from(Instant.now()));
        responseMessage.getMSH().getProcessingID().getProcessingMode().setValue(queryMessage.getMSH().getProcessingID().getProcessingID().getValue());
        responseMessage.getMSH().getVersionID().getVersionID().setValue(queryMessage.getMSH().getVersionID().getVersionID().getValue());
        responseMessage.getMSH().getMessageType().getMessageType().setValue("ADR");
        responseMessage.getMSH().getMessageType().getTriggerEvent().setValue("A19");

        getLogger().info(".processMessage(): Exit, responseMessage->{}", responseMessage);
        return(responseMessage);
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
