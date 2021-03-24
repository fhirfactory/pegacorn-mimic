/*
 * Copyright (c) 2021 Mark Hunter
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
package net.fhirfactory.pegacorn.mimic.fhirtools.practitionerrole.subcommands.practitionerrolecsv;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.OrganizationCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.PractitionerRoleCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.SimplisticOrganization;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.SimplisticPractitionerRole;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PractitionerRoleCSVReader {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerRoleCSVReader.class);
    private List<PractitionerRoleCSVEntry> elementList;

    private static final String SWITCH_PREFIX_NUMBER = "02512";

    public PractitionerRoleCSVReader(){
        elementList = new ArrayList<>();
        LOG.info(".PractitionerRoleCSVReader(): Constructor called");
    }

    public List<PractitionerRoleCSVEntry> readOPractitionerRoleCSV(String csvFileName){
        LOG.debug(".readOPractitionerRoleCSV(): Entry");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(csvFileName));
            ColumnPositionMappingStrategy csvMappingStrategy = new ColumnPositionMappingStrategy();
            csvMappingStrategy.setType(PractitionerRoleCSVEntry.class);
            CsvToBean contentSet = new CsvToBeanBuilder(reader)
                    .withType(PractitionerRoleCSVEntry.class)
                    .withMappingStrategy(csvMappingStrategy)
                    .build();
            this.elementList = contentSet.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LOG.debug(".readOPractitionerRoleCSV(): Exit, file read");
        return(this.elementList);
    }

    public List<SimplisticPractitionerRole> convertCSVEntry2SimplisticPractitionerRole(List<PractitionerRoleCSVEntry> prSet){
        LOG.debug(".convertCSVEntry2SimplisticPractitionerRole(): Entry");
        ArrayList<SimplisticPractitionerRole> workingList = new ArrayList<>();
        LOG.trace(".convertCSVEntry2SimplisticPractitionerRole(): Iterate through CSV Entries and Converting to Simplistic PractitionerRole");
        for(PractitionerRoleCSVEntry currentEntry: prSet) {
            SimplisticPractitionerRole currentPractitionerRole = new SimplisticPractitionerRole();
            currentPractitionerRole.setOrganizationShortName(currentEntry.getOrganisationUnitShortName());
            currentPractitionerRole.setPractitionerRoleShortName(currentEntry.getPractitionerRoleShortName());
            currentPractitionerRole.setPractitionerRoleLongName(currentEntry.getPractitionerRoleLongName());
            currentPractitionerRole.setRoleCategory(currentEntry.getRoleCategory());
            currentPractitionerRole.setRoleShortName(currentEntry.getRoleShortName());
            List<String> fixedLineNumbers = getFixedLineNumbers(currentEntry.getContactExtensions());
            currentPractitionerRole.getLandlineNumbers().addAll(fixedLineNumbers);
            currentPractitionerRole.setLocationShortName(currentEntry.getLocationTag());
            currentPractitionerRole.setRoleADGroup(currentEntry.getActiveDirectoryGroup());
            List<String> mobilePhoneNumbers = getMobilePhoneNumbers(currentEntry.getContactMobile());
            currentPractitionerRole.getMobileNumbers().addAll(mobilePhoneNumbers);
            workingList.add(currentPractitionerRole);
        }
        LOG.debug(".organiseOrganizationHierarchy(): Exit, Organization hierarchy established");
        return(workingList);
    }

    private void printHierarchy(SimplisticOrganization leaf){
        String outputString = leaf.getOrganizationShortName();
        while(leaf.getParentOrganization() != null){
            leaf = leaf.getParentOrganization();
            outputString = leaf.getOrganizationShortName() + "->" + outputString;
        }
        LOG.info("Hierarchy: {}", outputString);
    }

    private List<String> getFixedLineNumbers(String entry){
        if(entry == null || entry.length() < 5){
            return(new ArrayList<>());
        }
        String[] entrySet;
        if(entry.contains(";")){
            entrySet = entry.split(";");
        } else {
            entrySet = new String[1];
            entrySet[0] = entry;
        }
        ArrayList<String> phoneList = new ArrayList<>();
        for(int counter = 0; counter < entrySet.length; counter += 1){
            String strippedString = entrySet[counter].strip();
            String completeNumber = "";
            if(strippedString.length() == 5){
                completeNumber = SWITCH_PREFIX_NUMBER + strippedString;
            } else {
                completeNumber = strippedString;
            }
            phoneList.add(completeNumber);
        }
        return(phoneList);
    }

    private List<String> getMobilePhoneNumbers(String entry){
        if(entry == null || entry.length() < 8){
            return(new ArrayList<>());
        }
        String[] entrySet;
        if(entry.contains("/")){
            entrySet = entry.split("/");
        } else {
            entrySet = new String[1];
            entrySet[0] = entry;
        }
        ArrayList<String> phoneList = new ArrayList<>();
        for(int counter = 0; counter < entrySet.length; counter += 1){
            String strippedString = entrySet[counter].strip();
            phoneList.add(strippedString);
        }
        return(phoneList);
    }
}
