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
package net.fhirfactory.pegacorn.mimic.fhirtools.healthcareservice.subcommands.healthcareservicecsv;

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

import net.fhirfactory.buildingblocks.esr.models.resources.CommonIdentifierESDTTypes;
import net.fhirfactory.buildingblocks.esr.models.resources.HealthcareServiceESR;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ContactPointESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ContactPointESDTTypeEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ContactPointESDTUseEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.HealthcareServiceCSVEntry;

public class HealthCareServiceCSVReader {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCareServiceCSVReader.class);
    private List<HealthcareServiceCSVEntry> elementList;
    
    public HealthCareServiceCSVReader(){
        elementList = new ArrayList<>();
        LOG.info(".HealthCareServiceCSVReader(): Constructor called");
    }

    public List<HealthcareServiceCSVEntry> readHealthCareServiceCSV(String csvFileName){
        LOG.debug(".readHealthCareServiceCSV(): Entry");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(csvFileName));
            ColumnPositionMappingStrategy csvMappingStrategy = new ColumnPositionMappingStrategy();
            csvMappingStrategy.setType(HealthcareServiceCSVEntry.class);
            CsvToBean contentSet = new CsvToBeanBuilder(reader)
                    .withType(HealthcareServiceCSVEntry.class)
                    .withMappingStrategy(csvMappingStrategy)
                    .build();
            this.elementList = contentSet.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LOG.debug(".readHealthCareServiceCSV(): Exit, file read");
        return(this.elementList);
    }

    public List<HealthcareServiceESR> convertCSVEntry2HealthCareServices(List<HealthcareServiceCSVEntry> healthCareServiceCSVEntries){
        LOG.debug(".convertCSVEntry2HealthCareServices(): Entry");
        LOG.trace(".convertCSVEntry2HealthCareServices(): Iterate through CSV Entries and Converting to HealthCareService");
     
      
        List<HealthcareServiceESR>healthCareServices = new ArrayList<>();
        
        for(HealthcareServiceCSVEntry currentEntry: healthCareServiceCSVEntries) {
            HealthcareServiceESR newHealthCareService = new HealthcareServiceESR();
            
            newHealthCareService.setOrganisationalUnit(currentEntry.getOrganisationalUnit());
           
        	
            CommonIdentifierESDTTypes identifierTypes = new CommonIdentifierESDTTypes();
            	
        	IdentifierESDT shortNameBasedIdentifier = new IdentifierESDT();
            shortNameBasedIdentifier.setType(identifierTypes.getShortName());
            shortNameBasedIdentifier.setUse(IdentifierESDTUseEnum.USUAL);
            shortNameBasedIdentifier.setValue(currentEntry.getHealthCareServiceShortName());
            newHealthCareService.getIdentifiers().add(shortNameBasedIdentifier);
            newHealthCareService.assignSimplifiedID(true, identifierTypes.getShortName(), IdentifierESDTUseEnum.USUAL);
    
            IdentifierESDT longNameBasedIdentifier = new IdentifierESDT();
        	longNameBasedIdentifier.setType(identifierTypes.getLongName());
        	longNameBasedIdentifier.setUse(IdentifierESDTUseEnum.SECONDARY);
        	longNameBasedIdentifier.setValue(currentEntry.getHealthCareServiceLongName());
        	newHealthCareService.getIdentifiers().add(longNameBasedIdentifier);
        	
        	
            String telehoneNumber = currentEntry.getTelephoneNumber();
            ContactPointESDT telephoneNumberContactChannel = new ContactPointESDT();
            telephoneNumberContactChannel.setName("Telephone");
            telephoneNumberContactChannel.setValue(telehoneNumber);
            telephoneNumberContactChannel.setType(ContactPointESDTTypeEnum.LANDLINE);
            telephoneNumberContactChannel.setUse(ContactPointESDTUseEnum.WORK);
            telephoneNumberContactChannel.setRank(1);
            newHealthCareService.getContactPoints().add(telephoneNumberContactChannel); 
        	
        	healthCareServices.add(newHealthCareService);
        }
        
        return(healthCareServices);
    }
}
