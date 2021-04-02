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
import net.fhirfactory.pegacorn.internals.directories.entries.PractitionerRoleDirectoryEntry;
import net.fhirfactory.pegacorn.internals.directories.entries.datatypes.*;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.PractitionerRoleCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.SimplisticOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
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

    public List<PractitionerRoleDirectoryEntry> convertCSVEntry2PractitionerRoleLite(List<PractitionerRoleCSVEntry> prSet){
        LOG.debug(".convertCSVEntry2PractitionerRoleLite(): Entry");
        ArrayList<PractitionerRoleDirectoryEntry> workingList = new ArrayList<>();
        LOG.trace(".convertCSVEntry2PractitionerRoleLite(): Iterate through CSV Entries and Converting to PractitionerRoleLite");
        for(PractitionerRoleCSVEntry currentEntry: prSet) {
            PractitionerRoleDirectoryEntry currentPractitionerRoleDirectoryEntry = new PractitionerRoleDirectoryEntry();
            // Add Organization
            IdentifierDE organizationIdentifier = new IdentifierDE();
            organizationIdentifier.setValue(currentEntry.getOrganisationUnitShortName());
            organizationIdentifier.setType("ShortName");
            organizationIdentifier.setUse(IdentifierDEUseEnum.USUAL);
            currentPractitionerRoleDirectoryEntry.setPrimaryOrganizationID(organizationIdentifier);
            // Add ShortName
            IdentifierDE practitionerRoleIdentifier0 = new IdentifierDE();
            practitionerRoleIdentifier0.setType("ShortName");
            practitionerRoleIdentifier0.setUse(IdentifierDEUseEnum.OFFICIAL);
            practitionerRoleIdentifier0.setValue(currentEntry.getPractitionerRoleShortName());
            currentPractitionerRoleDirectoryEntry.addIdentifier(practitionerRoleIdentifier0);
            // Add LongName
            IdentifierDE practitionerRoleIdentifier1 = new IdentifierDE();
            practitionerRoleIdentifier1.setType("LongName");
            practitionerRoleIdentifier1.setUse(IdentifierDEUseEnum.SECONDARY);
            practitionerRoleIdentifier1.setValue(currentEntry.getPractitionerRoleLongName());
            currentPractitionerRoleDirectoryEntry.addIdentifier(practitionerRoleIdentifier1);
            // Display Name
            currentPractitionerRoleDirectoryEntry.setDisplayName(currentEntry.getPractitionerRoleLongName());
            // Set Role Category & RoleID
            currentPractitionerRoleDirectoryEntry.setPrimaryRoleCategory(currentEntry.getRoleCategory());
            currentPractitionerRoleDirectoryEntry.setPrimaryRoleID(currentEntry.getRoleShortName());
            // Add Telephone ContactPoints
            List<ContactPointDE> fixedLineNumbers = getFixedLineNumbers(currentEntry.getContactExtensions());
            currentPractitionerRoleDirectoryEntry.getContactPoints().addAll(fixedLineNumbers);
            // Add Location
            IdentifierDE locationIdentifier = new IdentifierDE();
            locationIdentifier.setType("Campus");
            locationIdentifier.setUse(IdentifierDEUseEnum.SECONDARY);
            locationIdentifier.setValue(currentEntry.getLocationTag());
            currentPractitionerRoleDirectoryEntry.setPrimaryLocationID(locationIdentifier);
            // Add Active Directory Group
            currentPractitionerRoleDirectoryEntry.setPractitionerRoleADGroup(currentEntry.getActiveDirectoryGroup());
            // Add Mobile Phone ContactPoints
            List<ContactPointDE> mobilePhoneNumbers = getMobilePhoneNumbers(currentEntry.getContactMobile());
            currentPractitionerRoleDirectoryEntry.getContactPoints().addAll(mobilePhoneNumbers);
            // All done!
            workingList.add(currentPractitionerRoleDirectoryEntry);
        }
        LOG.debug(".convertCSVEntry2PractitionerRoleLite(): Exit, Organization hierarchy established");
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

    private List<ContactPointDE> getFixedLineNumbers(String entry){
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
        ArrayList<ContactPointDE> phoneList = new ArrayList<>();
        for(int counter = 0; counter < entrySet.length; counter += 1){
            String strippedString = entrySet[counter].strip();
            String completeNumber = "";
            ContactPointDETypeEnum channelType = ContactPointDETypeEnum.LANDLINE;
            if(strippedString.length() == 5){
                completeNumber = SWITCH_PREFIX_NUMBER + strippedString;
                channelType = ContactPointDETypeEnum.PABX_EXTENSION;
            } else {
                completeNumber = strippedString;
            }
            ContactPointDE commChannel = new ContactPointDE();
            commChannel.setType(channelType);
            commChannel.setUse(ContactPointDEUseEnum.WORK);
            commChannel.setValue(completeNumber);
            phoneList.add(commChannel);
        }
        return(phoneList);
    }

    private List<ContactPointDE> getMobilePhoneNumbers(String entry){
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
        ArrayList<ContactPointDE> phoneList = new ArrayList<>();
        for(int counter = 0; counter < entrySet.length; counter += 1){
            String strippedString = entrySet[counter].strip();
            ContactPointDE commChannel = new ContactPointDE();
            commChannel.setValue(strippedString);
            commChannel.setType(ContactPointDETypeEnum.MOBILE);
            phoneList.add(commChannel);
        }
        return(phoneList);
    }
}
