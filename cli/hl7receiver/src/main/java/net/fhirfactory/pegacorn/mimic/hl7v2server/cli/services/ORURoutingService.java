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

import ca.uhn.hl7v2.protocol.ApplicationRouter;

public class ORURoutingService implements ApplicationRouter.AppRoutingData {
    String version;
    String triggerEvent;
    String processingId;
    String messageType;

    //
    // Constructor(s)
    //
    public ORURoutingService(){
        version = "*";
        triggerEvent = "*";
        processingId = "*";
        messageType = "ORU";
    }

    public ORURoutingService(String version, String triggerEvent, String processingId, String messageType){
        this.version = version;
        this.triggerEvent = triggerEvent;
        this.processingId = processingId;
        this.messageType = messageType;
    }

    //
    // Getters
    //

    @Override
    public String getVersion() {
        return (version);
    }

    @Override
    public String getTriggerEvent() {
        return(triggerEvent);
    }

    @Override
    public String getProcessingId() {
        return(processingId);
    }

    @Override
    public String getMessageType() {
        return(messageType);
    }
}
