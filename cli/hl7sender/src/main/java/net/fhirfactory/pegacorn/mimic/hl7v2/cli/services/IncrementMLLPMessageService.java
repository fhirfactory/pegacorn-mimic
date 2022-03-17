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
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v24.message.ORU_R01;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.util.Terser;
import net.fhirfactory.pegacorn.core.constants.petasos.PetasosPropertyConstants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class IncrementMLLPMessageService {
    private static final Logger LOG = LoggerFactory.getLogger(IncrementMLLPMessageService.class);
    private HapiContext context;
    private Parser parser;
    private String firstNameSeed;
    private String lastNameSeed;
    private String accessionSeed;
    private Integer initialMR;
    private DateTimeFormatter timeFormatter;

    //
    // Constructor(s)
    //

    public IncrementMLLPMessageService(String firstNameSeed, String lastNameSeed, String accessionSeed, int initialMR){
        context = new DefaultHapiContext();
        parser = context.getPipeParser();
        parser.getParserConfiguration().setValidating(false);
        parser.getParserConfiguration().setAllowUnknownVersions(true);
        this.firstNameSeed = firstNameSeed;
        this.lastNameSeed = lastNameSeed;
        this.accessionSeed = accessionSeed;
        this.initialMR = initialMR;
        timeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of(PetasosPropertyConstants.DEFAULT_TIMEZONE));

    }

    //
    // Business Methods
    //

    public Integer getInitialMR(){
        return(initialMR);
    }

    public String addSuffixToFirstName(String oriName){
        String newName = oriName + firstNameSeed;
        return(newName);
    }

    public String addSuffixToLastName(String oriName){
        String newName = oriName + lastNameSeed;
        return(newName);
    }

    public String getInitialAccessionNumber(){
        return(accessionSeed);
    }

    public String setStartValues(String templateMessage){
        LOG.info(".setStartValues(): Entry");
        String messageString = SerializationUtils.clone(templateMessage);
        String outMesssageString = null;
        try {
            ORU_R01 outMesssage = (ORU_R01)toMessage(messageString);
            LOG.info(".incrementMessage(): Structure->{}", outMesssage.printStructure());
            Terser terser = new Terser(outMesssage);
            String currentTime = timeFormatter.format(Instant.now());
            terser.set("/.MSH-7-1",currentTime);
            String initialMRString = Integer.toString(initialMR);
            LOG.info(".setStartValues(): mr->{}", initialMRString);
            terser.set("/PATIENT_RESULT/PATIENT/PID-3(0)-1",Integer.toString(initialMR));
            String familyName = addSuffixToLastName(terser.get("/PATIENT_RESULT/PATIENT/PID-5(0)-1"));
            LOG.info(".setStartValues(): familyName->{}", familyName);
            terser.set("/PATIENT_RESULT/PATIENT/PID-5(0)-1", familyName);
            String firstName = addSuffixToFirstName(terser.get("/PATIENT_RESULT/PATIENT/PID-5(0)-2"));
            LOG.info(".setStartValues(): firstName->{}", firstName);
            terser.set("/PATIENT_RESULT/PATIENT/PID-5(0)-2", firstName);
            terser.set("/PATIENT_RESULT/ORDER_OBSERVATION/ORC-3-1", accessionSeed);
            terser.set("/PATIENT_RESULT/ORDER_OBSERVATION/OBR-3-1", accessionSeed);
            outMesssageString = parser.encode(outMesssage);
        } catch (Exception ex){
            LOG.info(".setStartValues(): Error, message->{}", ExceptionUtils.getMessage(ex));
        }
        LOG.info(".setStartValues(): Exit");
        return(outMesssageString);
    }

    public String accessionIncrementor(String currentAccessionValue){
        String[] accessionNumberFields = currentAccessionValue.split("-");
        Integer accessionNumber = Integer.valueOf(accessionNumberFields[1]);
        accessionNumber += 1;
        String newAccessionNumber = accessionNumberFields[0] + "-" + Integer.toString(accessionNumber) + "-" + accessionNumberFields[2] + "-" + accessionNumberFields[3];
        return(newAccessionNumber);
    }

    public String incrementMessage(String oriMessage){
        LOG.info(".incrementMessage(): Entry");
        String messageString = SerializationUtils.clone(oriMessage);
        String outMesssageString = null;
        try {
            ORU_R01 outMesssage = (ORU_R01)toMessage(messageString);
            LOG.info(".incrementMessage(): Structure->{}", outMesssage.printStructure());
            Terser terser = new Terser(outMesssage);

            String currentTime = timeFormatter.format(Instant.now());
            terser.set("/.MSH-7-1",currentTime);

            String pidMR = terser.get("/PATIENT_RESULT/PATIENT/PID-3(0)-1");
            LOG.info(".incrementMessage(): pidMR->{}", pidMR);
            Integer mr = Integer.valueOf(pidMR);
            mr += 1;
            LOG.info(".incrementMessage(): mr->{}", mr);
            terser.set("/PATIENT_RESULT/PATIENT/PID-3(0)-1",Integer.toString(mr));

            String familyName = incrementUserName(terser.get("/PATIENT_RESULT/PATIENT/PID-5(0)-1"));
            terser.set("/PATIENT_RESULT/PATIENT/PID-5(0)-1", familyName);

            String firstName = incrementUserName(terser.get("/PATIENT_RESULT/PATIENT/PID-5(0)-2"));
            terser.set("/PATIENT_RESULT/PATIENT/PID-5(0)-2", firstName);

            String accessionNumber = accessionIncrementor(terser.get("/PATIENT_RESULT/ORDER_OBSERVATION/ORC-3-1"));
            terser.set("/PATIENT_RESULT/ORDER_OBSERVATION/ORC-3-1", accessionNumber);
            terser.set("/PATIENT_RESULT/ORDER_OBSERVATION/OBR-3-1", accessionNumber);

            outMesssageString = parser.encode(outMesssage);
        } catch (Exception ex){
            LOG.info(".incrementMessage(): Error, message->{}", ExceptionUtils.getMessage(ex));
        }
        LOG.info(".incrementMessage(): Exit");
        return(outMesssageString);
    }

    public String incrementUserName(String oriName){
        LOG.info(".incrementUserName(): Entry, oriName->{}", oriName);
        int nameLength = oriName.length();
        String newName = oriName.substring(0, nameLength-4);
        char[] nameArray = oriName.toCharArray();
        char lastM0Char = nameArray[nameLength-1];
        char lastM1Char = nameArray[nameLength-2];
        char lastM2Char = nameArray[nameLength-3];
        char lastM3Char = nameArray[nameLength-4];
        boolean increment1 = false;
        boolean increment2 = false;
        boolean increment3 = false;
        if(lastM0Char == 'Z'){
            increment1 = true;
            lastM0Char = 'A';
        } else {
            lastM0Char += 1;
        }
        if(increment1){
            if(lastM1Char == 'Z'){
                increment2 = true;
                lastM1Char = 'A';
            } else {
                lastM1Char += 1;
            }
        }
        if(increment2){
            if(lastM2Char == 'Z'){
                increment3 = true;
                lastM2Char = 'A';
            } else {
                lastM2Char += 1;
            }
        }
        if(increment3){
            if(lastM3Char == 'Z'){
                lastM0Char = 'A';
                lastM1Char = 'A';
                lastM2Char = 'A';
                lastM3Char = 'A';
            } else {
                lastM3Char += 1;
            }
        }
        String outputName = newName + lastM3Char + lastM2Char + lastM1Char + lastM0Char;
        LOG.info(".incrementUserName(): Exit, outputName->{}", outputName);
        return(outputName);
    }

    public Message toMessage(String messageString){
        LOG.info(".toMessage(): Entry");
        try {
            Message message = parser.parse(messageString);
            LOG.info(".toMessage(): Exit, message encapsulated!");
            return (message);
        } catch(Exception ex){
            ex.printStackTrace();
            LOG.info(".toMessage(): Exit, could not encapsulate message");
            return(null);
        }
    }
}
