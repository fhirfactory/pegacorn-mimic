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
package net.fhirfactory.pegacorn.mimic.fhirtools.careteam.subcommands.careteamcsv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;

import net.fhirfactory.buildingblocks.esr.models.resources.CareTeamESR;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierType;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ParticipantESDT;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.CareTeamCSVEntry;

public class CareTeamCSVReader {
    private static final Logger LOG = LoggerFactory.getLogger(CareTeamCSVReader.class);
    private List<CareTeamCSVEntry> elementList;
    
    public CareTeamCSVReader(){
        elementList = new ArrayList<>();
        LOG.info(".CareTeamCSVReader(): Constructor called");
    }

    public List<CareTeamCSVEntry> readCareTeamCSV(String csvFileName){
        LOG.debug(".readCareTeamCSV(): Entry");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(csvFileName));
            ColumnPositionMappingStrategy csvMappingStrategy = new ColumnPositionMappingStrategy();
            csvMappingStrategy.setType(CareTeamCSVEntry.class);
            CsvToBean contentSet = new CsvToBeanBuilder(reader)
                    .withType(CareTeamCSVEntry.class)
                    .withMappingStrategy(csvMappingStrategy)
                    .build();
            this.elementList = contentSet.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LOG.debug(".readCareTeamCSV(): Exit, file read");
        return(this.elementList);
    }

    public List<CareTeamESR> convertCSVEntry2CareTeams(List<CareTeamCSVEntry> careTeamCSVEntries){
        LOG.debug(".convertCSVEntry2CareTeams(): Entry");
        LOG.trace(".convertCSVEntry2CareTeams(): Iterate through CSV Entries and Converting to CareTeam");
     
        Map<String, List<ParticipantESDT>>careTeamsMap = new HashMap<>();
        
        
        for(CareTeamCSVEntry currentEntry: careTeamCSVEntries) {
        	String careTeamShortName = currentEntry.getCareTeamShortName();
        	String careTeamLongName = currentEntry.getCareTeamShortName();
        	
        	
        	List<ParticipantESDT> participants = careTeamsMap.get(careTeamShortName);
        	if (participants == null) {
        		participants = new ArrayList<>();
        		careTeamsMap.putIfAbsent(careTeamLongName, participants);
        	}
        	
        	participants.add(new ParticipantESDT(currentEntry.getPractitionerRoleShortName()));
        }
        
        // Now we have all the data in the map we can convert this to care teams.
        List<CareTeamESR>careTeams = new ArrayList<>();
                
        for (Map.Entry<String, List<ParticipantESDT>> entry : careTeamsMap.entrySet()) {
        	CareTeamESR newCareTeam = new CareTeamESR();
        	
        	newCareTeam.setParticipants(entry.getValue());
        	
        	IdentifierESDT shortNameBasedIdentifier = new IdentifierESDT();
            shortNameBasedIdentifier.setType(IdentifierType.SHORT_NAME);
            shortNameBasedIdentifier.setUse(IdentifierESDTUseEnum.USUAL);
            shortNameBasedIdentifier.setValue(entry.getKey());
            shortNameBasedIdentifier.setLeafValue(entry.getKey());
            newCareTeam.getIdentifiers().add(shortNameBasedIdentifier);
            newCareTeam.assignSimplifiedID(true, IdentifierType.SHORT_NAME, IdentifierESDTUseEnum.USUAL);

            IdentifierESDT longNameBasedIdentifier = new IdentifierESDT();
        	longNameBasedIdentifier.setType(IdentifierType.LONG_NAME);
        	longNameBasedIdentifier.setUse(IdentifierESDTUseEnum.SECONDARY);
        	longNameBasedIdentifier.setValue(entry.getKey());
        	longNameBasedIdentifier.setLeafValue(entry.getKey());
            newCareTeam.getIdentifiers().add(longNameBasedIdentifier);
        	
        	careTeams.add(newCareTeam);
        }
        
        return(careTeams);
    }
}
