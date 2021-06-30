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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ContactPointESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ContactPointESDTTypeEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ContactPointESDTUseEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierType;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.PractitionerRoleCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.PractitionerRoleESRApproximate;

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

    public List<PractitionerRoleESRApproximate> convertCSVEntry2PractitionerRoleLite(List<PractitionerRoleCSVEntry> prSet){
        LOG.debug(".convertCSVEntry2PractitionerRoleLite(): Entry");

        ArrayList<PractitionerRoleESRApproximate> workingList = new ArrayList<>();
        LOG.trace(".convertCSVEntry2PractitionerRoleLite(): Iterate through CSV Entries and Converting to PractitionerRoleLite");
        for(PractitionerRoleCSVEntry currentEntry: prSet) {
            PractitionerRoleESRApproximate practitionerRoleESRApproximate = new PractitionerRoleESRApproximate();
            // Add Organization
            practitionerRoleESRApproximate.setPrimaryOrganizationID(currentEntry.getOrganisationUnitShortName());
            practitionerRoleESRApproximate.setPrimaryOrganizationIDContextual(true);
            // Add ShortName
            IdentifierESDT practitionerRoleIdentifier0 = new IdentifierESDT();
            practitionerRoleIdentifier0.setType(IdentifierType.SHORT_NAME);
            practitionerRoleIdentifier0.setUse(IdentifierESDTUseEnum.USUAL);
            practitionerRoleIdentifier0.setValue(currentEntry.getPractitionerRoleShortName());
            practitionerRoleIdentifier0.setLeafValue(currentEntry.getPractitionerRoleShortName());
            practitionerRoleESRApproximate.addIdentifier(practitionerRoleIdentifier0);
            // Add LongName
            IdentifierESDT practitionerRoleIdentifier1 = new IdentifierESDT();
            practitionerRoleIdentifier1.setType(IdentifierType.LONG_NAME);
            practitionerRoleIdentifier1.setUse(IdentifierESDTUseEnum.SECONDARY);
            practitionerRoleIdentifier1.setValue(currentEntry.getPractitionerRoleLongName());
            practitionerRoleIdentifier1.setLeafValue(currentEntry.getPractitionerRoleLongName());
            practitionerRoleESRApproximate.addIdentifier(practitionerRoleIdentifier1);
            // Display Name
            practitionerRoleESRApproximate.setDisplayName(currentEntry.getPractitionerRoleShortName());
            practitionerRoleESRApproximate.setDescription(currentEntry.getPractitionerRoleLongName());
            // Set Role Category & RoleID
            practitionerRoleESRApproximate.setPrimaryRoleCategoryID(currentEntry.getRoleCategory());
            practitionerRoleESRApproximate.setPrimaryRoleCategoryIDContextual(true);
            practitionerRoleESRApproximate.setPrimaryRoleID(currentEntry.getRoleShortName());
            practitionerRoleESRApproximate.setPrimaryRoleIDContextual(true);
            
            Integer rank = 1;
            // Add Telephone ContactPoints
            List<ContactPointESDT> fixedLineNumbers = getFixedLineNumbers(currentEntry.getContactExtensions(), rank);
            practitionerRoleESRApproximate.getContactPoints().addAll(fixedLineNumbers);
           
            
            // Add fax number
            String faxNumber = currentEntry.getFacsimile();
            ContactPointESDT facsimileContactChannel = new ContactPointESDT();
            facsimileContactChannel.setName("Facsimile");
            facsimileContactChannel.setValue(faxNumber);
            facsimileContactChannel.setType(ContactPointESDTTypeEnum.FACSIMILE);
            facsimileContactChannel.setUse(ContactPointESDTUseEnum.WORK);
            facsimileContactChannel.setRank(rank++);
            practitionerRoleESRApproximate.getContactPoints().add(facsimileContactChannel);  
            
            // Add pager
            String pagerNumber = currentEntry.getPager();
            ContactPointESDT pagerContactChannel = new ContactPointESDT();
            pagerContactChannel.setName("Pager");
            pagerContactChannel.setValue(pagerNumber);
            pagerContactChannel.setType(ContactPointESDTTypeEnum.PAGER);
            pagerContactChannel.setUse(ContactPointESDTUseEnum.WORK);
            pagerContactChannel.setRank(rank++);
            practitionerRoleESRApproximate.getContactPoints().add(pagerContactChannel);             
            
            
            // Add Location (TODO Need to add better location primaryKey establishment)
            practitionerRoleESRApproximate.setPrimaryLocationID(currentEntry.getLocationTag());
            practitionerRoleESRApproximate.setPrimaryLocationIDContextual(true);
            // Add Active Directory Group
            practitionerRoleESRApproximate.setPractitionerRoleADGroup(currentEntry.getActiveDirectoryGroup());
            // Add Mobile Phone ContactPoints
            List<ContactPointESDT> mobilePhoneNumbers = getMobilePhoneNumbers(currentEntry.getContactMobile(), rank);
            practitionerRoleESRApproximate.getContactPoints().addAll(mobilePhoneNumbers);
            // Assign SimplifiedID
            practitionerRoleESRApproximate.assignSimplifiedID(true, IdentifierType.SHORT_NAME, IdentifierESDTUseEnum.USUAL);
            
            // All done!
            workingList.add(practitionerRoleESRApproximate);
        }
        LOG.debug(".convertCSVEntry2PractitionerRoleLite(): Exit, Organization hierarchy established");
        return(workingList);
    }

    private List<ContactPointESDT> getFixedLineNumbers(String entry, int rank){
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
        ArrayList<ContactPointESDT> phoneList = new ArrayList<>();
        for(int counter = 0; counter < entrySet.length; counter += 1){
            String strippedString = entrySet[counter].strip();
            String completeNumber = "";
            ContactPointESDTTypeEnum channelType = ContactPointESDTTypeEnum.LANDLINE;
            if(strippedString.length() == 5){
                completeNumber = SWITCH_PREFIX_NUMBER + strippedString;
                channelType = ContactPointESDTTypeEnum.PABX_EXTENSION;
            } else {
                completeNumber = strippedString;
            }
            ContactPointESDT commChannel = new ContactPointESDT();
            commChannel.setType(channelType);
            commChannel.setUse(ContactPointESDTUseEnum.WORK);
            commChannel.setValue(completeNumber);
            commChannel.setRank(rank++);
            phoneList.add(commChannel);
        }
        return(phoneList);
    }

    private List<ContactPointESDT> getMobilePhoneNumbers(String entry, int rank){
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
        ArrayList<ContactPointESDT> phoneList = new ArrayList<>();
        for(int counter = 0; counter < entrySet.length; counter += 1){
            String strippedString = entrySet[counter].strip();
            ContactPointESDT commChannel = new ContactPointESDT();
            commChannel.setValue(strippedString);
            commChannel.setType(ContactPointESDTTypeEnum.MOBILE);
            commChannel.setName("Mobile");
            commChannel.setRank(rank++);
            commChannel.setUse(ContactPointESDTUseEnum.WORK);
            phoneList.add(commChannel);
        }
        return(phoneList);
    }
}
