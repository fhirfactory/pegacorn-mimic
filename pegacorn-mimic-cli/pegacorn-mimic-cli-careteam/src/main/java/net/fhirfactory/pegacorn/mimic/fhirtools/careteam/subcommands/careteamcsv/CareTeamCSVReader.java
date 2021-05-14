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
import net.fhirfactory.buildingblocks.esr.models.resources.CommonIdentifierESDTTypes;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ParticipantESDT;
import net.fhirfactory.buildingblocks.esr.models.resources.datatypes.ParticipantTypeEnum;
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
     
        Map<String, List<ParticipantESDT>>careTeamsMap = initialiseCareTeams();
        
        
        for(CareTeamCSVEntry currentEntry: careTeamCSVEntries) {
        	String practitionerRoleName = currentEntry.getRoleName();
        	
        	
        	if (currentEntry.getCodeBrownNotifier().equals("Y")) {
        		// Add the practitioner role as a notifier
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Brown");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.NOTIFIER));
        	}
        	
        	
        	if (currentEntry.getCodeBrownResponder().equals("Y")) {
        		// Add the practitioner role as a responder
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Brown");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.RESPONDER));
        	}
        	
        	
        	if (currentEntry.getCodeOrangeNotifier().equals("Y")) {
        		// Add the practitioner role as a notifier
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Orange");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.NOTIFIER));
        	}
        	
        	
        	if (currentEntry.getCodeOrangeResponder().equals("Y")) {
        		// Add the practitioner role as a responder
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Orange");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.RESPONDER));
        	}
        	
        	
        	if (currentEntry.getCodeYellowNotifier().equals("Y")) {
        		// Add the practitioner role as a notifier
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Yellow");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.NOTIFIER));
        	}

        	
        	if (currentEntry.getCodeYellowResponder().equals("Y")) {
        		// Add the practitioner role as a responder     
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Yellow");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.RESPONDER));
        	}
        	
        	
        	if (currentEntry.getCodePurpleNotifier().equals("Y")) {
        		// Add the practitioner role as a notifier   
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Purple");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.NOTIFIER));
        	}
        	
        	
        	if (currentEntry.getCodePurpleResponder().equals("Y")) {
        		// Add the practitioner role as a responder      	
        		List<ParticipantESDT> participants = careTeamsMap.get("Code Purple");
        		participants.add(new ParticipantESDT(practitionerRoleName, ParticipantTypeEnum.RESPONDER));
        	}
        }
        
        // Now we have all the data in the map we can convert this to care teams.
        List<CareTeamESR>careTeams = new ArrayList<>();
        
        CommonIdentifierESDTTypes identifierTypes = new CommonIdentifierESDTTypes();
        
        for (Map.Entry<String, List<ParticipantESDT>> entry : careTeamsMap.entrySet()) {
        	CareTeamESR newCareTeam = new CareTeamESR();
        	
        	newCareTeam.setParticipants(entry.getValue());
        	
        	 IdentifierESDT shortnameBasedIdentifier = new IdentifierESDT();
             shortnameBasedIdentifier.setType(identifierTypes.getShortName());
             shortnameBasedIdentifier.setUse(IdentifierESDTUseEnum.USUAL);
             shortnameBasedIdentifier.setValue(entry.getKey());
             shortnameBasedIdentifier.setLeafValue(entry.getKey());
             
             newCareTeam.getIdentifiers().add(shortnameBasedIdentifier);
             
             newCareTeam.assignSimplifiedID(true, identifierTypes.getShortName(), IdentifierESDTUseEnum.OFFICIAL);
        	
        	careTeams.add(newCareTeam);
        }
        
        return(careTeams);
    }
    
    
    
    /**
     * Add an entry to the map for each care team.  The participant list is empty at this stage.
     * 
     * @return
     */
    private Map<String, List<ParticipantESDT>> initialiseCareTeams() {
    	Map<String, List<ParticipantESDT>>careTeams = new HashMap<>();
    	
    	careTeams.put("Code Yellow", new ArrayList<ParticipantESDT>());
    	careTeams.put("Code Orange", new ArrayList<ParticipantESDT>());
    	careTeams.put("Code Brown", new ArrayList<ParticipantESDT>());
    	careTeams.put("Code Purple", new ArrayList<ParticipantESDT>());

    	
    	return careTeams;
    }  
}
