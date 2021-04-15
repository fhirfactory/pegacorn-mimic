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
package net.fhirfactory.pegacorn.mimic.fhirtools.location.subcommands.locationcsv;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import net.fhirfactory.pegacorn.internals.esr.helpers.ContextualisedIdentifierValueFactory;
import net.fhirfactory.pegacorn.internals.esr.resources.LocationESR;
import net.fhirfactory.pegacorn.internals.esr.resources.OrganizationESR;
import net.fhirfactory.pegacorn.internals.esr.resources.common.CommonIdentifierESDTTypes;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierESDT;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.TypeESDT;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.LocationCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.OrganizationCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.ContextualisedIdentifierValueHelper;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.IdentifierESDTTypesUsedInCLI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocationCSVReader {
    private static final Logger LOG = LoggerFactory.getLogger(LocationCSVReader.class);

    private List<LocationCSVEntry> elementList;
    private LocationESR rootLocation;

    public LocationCSVReader(){
        elementList = new ArrayList<>();
        rootLocation = null;
        LOG.info(".LocationCSVReader(): Constructor called");
    }

    public List<LocationCSVEntry> readLocationCSV(String csvFileName){
        LOG.debug(".readLocationCSV(): Entry");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(csvFileName));
            ColumnPositionMappingStrategy csvMappingStrategy = new ColumnPositionMappingStrategy();
            csvMappingStrategy.setType(LocationCSVEntry.class);
            CsvToBean contentSet = new CsvToBeanBuilder(reader)
                    .withType(LocationCSVEntry.class)
                    .withMappingStrategy(csvMappingStrategy)
                    .build();
            this.elementList = contentSet.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LOG.debug(".readLocationCSV(): Exit, file read");
        return(this.elementList);
    }

    public List<LocationESR> organiseLocationHierarchy(){
        LOG.debug(".organiseLocationHierarchy(): Entry");
        CommonIdentifierESDTTypes identifierTypes = new CommonIdentifierESDTTypes();
        ArrayList<LocationCSVEntry> initialWorkingList = new ArrayList<>();
        ArrayList<LocationCSVEntry> nonRootWorkingList = new ArrayList<>();
        LOG.debug(".organiseLocationHierarchy(): Creating working list (initialWorkingList)");
        initialWorkingList.addAll(this.elementList);
        HashMap<String, LocationESR> processedEntries = new HashMap<>();
        LOG.trace(".organiseLocationHierarchy(): Determining Parent (root) Location");
        for (LocationCSVEntry currentEntry : initialWorkingList) {
            LOG.info(".organiseLocationHierarchy(): Processing node --> {}", currentEntry.getLocationShortName());
            if(currentEntry.getParentLocationShortName() == null || currentEntry.getParentLocationShortName().isEmpty()){
                LOG.info(".organiseLocationHierarchy(): Is root Node");
                LocationESR organisation = buildLocation(null, currentEntry);
                processedEntries.put(organisation.getIdentifierWithType(identifierTypes.getShortName()).getLeafValue(), organisation);
            } else {
                LOG.info(".organiseLocationHierarchy(): Is not a root Node");
                nonRootWorkingList.add(currentEntry);
            }
        }
        LOG.info(".organiseLocationHierarchy(): Now processing and sorting non-root nodes!");
        Integer depthCount = 15;
        Integer depthCounter = 0;
        while(!nonRootWorkingList.isEmpty() && (depthCounter < depthCount)) {
            ArrayList<LocationCSVEntry> processedList = new ArrayList<>();
            for (LocationCSVEntry currentEntry : nonRootWorkingList) {
                if (processedEntries.containsKey(currentEntry.getParentLocationShortName())) {
                    LocationESR currentLocationParent = processedEntries.get(currentEntry.getParentLocationShortName());
                    LocationESR currentLocation = buildLocation(currentLocationParent, currentEntry);
                    processedList.add(currentEntry);
                    processedEntries.put(currentLocation.getIdentifierWithType(identifierTypes.getShortName()).getLeafValue(), currentLocation);
                }
            }
            nonRootWorkingList.removeAll(processedList);
            depthCounter += 1;
        }
        int remainingLocations = nonRootWorkingList.size();
        if(remainingLocations > 0){
            LOG.info(".organiseLocationHierarchy(): List depthcount reached, remaining entries --> {}", remainingLocations);
            for(LocationCSVEntry remainingEntry: nonRootWorkingList){
                LOG.info(".organiseLocationHierarchy(): Unprocessed Entry shortName->{}, parentShortName->{}", remainingEntry.getLocationShortName(), remainingEntry.getParentLocationShortName());
            }
        }
        LOG.info(".organiseLocationHierarchy(): now add resources to output list");
        ArrayList<LocationESR> allLocations = new ArrayList<>();
        allLocations.addAll(processedEntries.values());
        int counter = 0;
        for(LocationESR location: allLocations){
            LOG.info(".organiseLocationHierarchy(): LocationESR[{}]->{}",counter,location.getDisplayName());
            counter += 1;
        }
        LOG.debug(".organiseLocationHierarchy(): Exit, Location hierarchy established");
        return(allLocations);
    }

    public LocationESR buildLocation(LocationESR parentLocation, LocationCSVEntry entry){
        if(entry == null){
            return(null);
        }
        CommonIdentifierESDTTypes identifierTypes = new CommonIdentifierESDTTypes();
        ContextualisedIdentifierValueFactory identifierValueFactory = new ContextualisedIdentifierValueFactory();
        LocationESR location = new LocationESR();
        String newShortNameIdentifierValue = null;
        String newLongNameIdentifierValue = null;
        TypeESDT orgType = new TypeESDT();
        orgType.setTypeDefinition("LocationType");
        if(parentLocation == null){
            newShortNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(null, entry.getLocationShortName());
            newLongNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(null, entry.getLocationLongName());
            orgType.setTypeValue("Campus");
            orgType.setTypeDisplayValue("Campus");
        } else {
            IdentifierESDT shortNameIdentifier = parentLocation.getIdentifierWithType(identifierTypes.getShortName());
            String parentShortName = shortNameIdentifier.getValue();
            newShortNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(parentShortName, entry.getLocationShortName());
            location.setContainingLocationID(parentLocation.getSimplifiedID());
            IdentifierESDT longNameIdentifier = parentLocation.getIdentifierWithType(identifierTypes.getLongName());
            String parentLongName = longNameIdentifier.getValue();
            newLongNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(parentLongName, entry.getLocationLongName());
            parentLocation.getContainedLocationIDs().add(newShortNameIdentifierValue);
            orgType.setTypeValue("Building");
            orgType.setTypeDisplayValue("Building");
        }
        IdentifierESDT shortnameBasedIdentifier = new IdentifierESDT();
        shortnameBasedIdentifier.setType(identifierTypes.getShortName());
        shortnameBasedIdentifier.setUse(IdentifierESDTUseEnum.USUAL);
        shortnameBasedIdentifier.setValue(newShortNameIdentifierValue);
        shortnameBasedIdentifier.setLeafValue(entry.getLocationShortName());
        location.getIdentifiers().add(shortnameBasedIdentifier);
        IdentifierESDT longNameBasedIdentifier = new IdentifierESDT();
        longNameBasedIdentifier.setType(identifierTypes.getLongName());
        longNameBasedIdentifier.setUse(IdentifierESDTUseEnum.OFFICIAL);
        longNameBasedIdentifier.setValue(newLongNameIdentifierValue);
        longNameBasedIdentifier.setLeafValue(entry.getLocationLongName());
        location.getIdentifiers().add(longNameBasedIdentifier);
        location.assignSimplifiedID(true, identifierTypes.getShortName(), IdentifierESDTUseEnum.OFFICIAL);
        location.setDisplayName(entry.getLocationShortName());
        location.setDescription(entry.getLocationLongName());
        location.setLocationType(orgType);
        return (location);
    }
}
