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
package net.fhirfactory.pegacorn.mimic.fhirtools.organization.subcommands.organizationcsv;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import net.fhirfactory.pegacorn.internals.esr.helpers.ContextualisedIdentifierValueFactory;
import net.fhirfactory.pegacorn.internals.esr.resources.OrganizationESR;
import net.fhirfactory.pegacorn.internals.esr.resources.common.CommonIdentifierESDTTypes;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierESDT;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.TypeESDT;
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

public class OrganizationCSVReader {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCSVReader.class);

    private List<OrganizationCSVEntry> elementList;
    private OrganizationESR rootOrganization;

    public OrganizationCSVReader(){
        elementList = new ArrayList<>();
        rootOrganization = null;
        LOG.info(".OrganizationCSVReader(): Constructor called");
    }

    public List<OrganizationCSVEntry> readOrganizationCSV(String csvFileName){
        LOG.debug(".readOrganizationCSV(): Entry");
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(csvFileName));
            ColumnPositionMappingStrategy csvMappingStrategy = new ColumnPositionMappingStrategy();
            csvMappingStrategy.setType(OrganizationCSVEntry.class);
            CsvToBean contentSet = new CsvToBeanBuilder(reader)
                    .withType(OrganizationCSVEntry.class)
                    .withMappingStrategy(csvMappingStrategy)
                    .build();
            this.elementList = contentSet.parse();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LOG.debug(".readOrganizationCSV(): Exit, file read, entry count --> {}", this.elementList.size());
        return(this.elementList);
    }

    public List<OrganizationESR> organiseOrganizationHierarchy(){
        LOG.debug(".organiseOrganizationHierarchy(): Entry");
        CommonIdentifierESDTTypes identifierTypes = new CommonIdentifierESDTTypes();
        ArrayList<OrganizationCSVEntry> initialWorkingList = new ArrayList<>();
        ArrayList<OrganizationCSVEntry> nonRootWorkingList = new ArrayList<>();
        LOG.debug(".organiseOrganizationHierarchy(): Creating working list (initialWorkingList)");
        initialWorkingList.addAll(this.elementList);
        HashMap<String, OrganizationESR> processedEntries = new HashMap<>();
        LOG.info(".organiseOrganizationHierarchy(): Determining Parent (root) Organization");
        for (OrganizationCSVEntry currentEntry : initialWorkingList) {
            LOG.info(".organiseOrganizationHierarchy(): Processing node --> {}", currentEntry.getOrganisationShortName());
            if(currentEntry.getOrganisationParentShortName() == null || currentEntry.getOrganisationParentShortName().isEmpty()){
                LOG.info(".organiseOrganizationHierarchy(): Is root Node");
                OrganizationESR organisation = buildOrganization(null, currentEntry);
                processedEntries.put(organisation.getIdentifierWithType(identifierTypes.getShortName()).getLeafValue(), organisation);
            } else {
                LOG.info(".organiseOrganizationHierarchy(): Is not a root Node");
                nonRootWorkingList.add(currentEntry);
            }
        }
        LOG.info(".organiseOrganizationHierarchy(): Now sorting non-root nodes!");
        Integer depthCount = 15;
        Integer depthCounter = 0;
        while(!nonRootWorkingList.isEmpty() && (depthCounter  < depthCount)) {
            ArrayList<OrganizationCSVEntry> processedList = new ArrayList<>();
            for (OrganizationCSVEntry currentEntry : nonRootWorkingList) {
                if (processedEntries.containsKey(currentEntry.getOrganisationParentShortName())) {
                    OrganizationESR currentOrganizationParent = processedEntries.get(currentEntry.getOrganisationParentShortName());
                    OrganizationESR currentOrganization = buildOrganization(currentOrganizationParent, currentEntry);
                    processedList.add(currentEntry);
                    processedEntries.put(currentOrganization.getIdentifierWithType(identifierTypes.getShortName()).getLeafValue(), currentOrganization);
                }
            }
            nonRootWorkingList.removeAll(processedList);
            depthCounter += 1;
        }
        int remainingOrgs = nonRootWorkingList.size();
        if(remainingOrgs > 0){
            LOG.info(".organiseOrganizationHierarchy(): List depthcount reached, remaining entries --> {}", remainingOrgs);
            for(OrganizationCSVEntry remainingEntry: nonRootWorkingList){
                LOG.info(".organiseOrganizationHierarchy(): Unprocessed Entry shortName->{}, parentShortName->{}", remainingEntry.getOrganisationShortName(), remainingEntry.getOrganisationParentShortName());
            }
        }
        LOG.info(".organiseOrganizationHierarchy(): now add resources to output list");
        ArrayList<OrganizationESR> allOrgs = new ArrayList<>();
        allOrgs.addAll(processedEntries.values());
        int counter = 0;
        for(OrganizationESR organization: allOrgs){
            LOG.info(".organiseOrganizationHierarchy(): OrganizationESR[{}]->{}",counter,organization.getDisplayName());
            counter += 1;
        }
        LOG.debug(".organiseOrganizationHierarchy(): Exit, Organization hierarchy established");
        return(allOrgs);
    }

    public OrganizationESR buildOrganization(OrganizationESR parentOrganization, OrganizationCSVEntry entry){
        LOG.debug(".buildOrganization(): Entry");
        if(entry == null){
            LOG.debug(".buildOrganization(): Exit, entry (OrganizationCSVEntry) is empty, exiting");
            return(null);
        }
        LOG.debug(".buildOrganization(): Entry, shortName->{}, longName->{}, parentShortName->{}",
                entry.getOrganisationShortName(), entry.getOrganisationLongName(), entry.getOrganisationParentShortName());
        ContextualisedIdentifierValueFactory identifierValueFactory = new ContextualisedIdentifierValueFactory();
        CommonIdentifierESDTTypes identifierTypes = new CommonIdentifierESDTTypes();
        OrganizationESR organization = new OrganizationESR();
        String newShortNameIdentifierValue = null;
        String newLongNameIdentifierValue = null;
        if(parentOrganization == null){
            LOG.debug(".buildOrganization(): Is Parent Organization, shortName->{}", entry.getOrganisationShortName());
            newShortNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(null, entry.getOrganisationShortName());
            newLongNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(null, entry.getOrganisationLongName());
        } else {
            LOG.debug(".buildOrganization(): Is Business Unit, shortName->{}, parent->{}", entry.getOrganisationShortName(), entry.getOrganisationParentShortName());
            IdentifierESDT shortNameIdentifier = parentOrganization.getIdentifierWithType(identifierTypes.getShortName());
            String parentShortName = shortNameIdentifier.getValue();
            LOG.debug(".buildOrganization(): parentShortNameIdentifierValue->{}", parentShortName);
            newShortNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(parentShortName, entry.getOrganisationShortName());
            LOG.debug(".buildOrganization(): newShortNameIdentifierValue->{}", newShortNameIdentifierValue);
            organization.setParentOrganization(parentOrganization.getSimplifiedID());
            IdentifierESDT longNameIdentifier = parentOrganization.getIdentifierWithType(identifierTypes.getLongName());
            String parentLongName = longNameIdentifier.getValue();
            newLongNameIdentifierValue = identifierValueFactory.buildComprehensiveIdentifierValue(parentLongName, entry.getOrganisationLongName());
            parentOrganization.getContainedOrganizations().add(newShortNameIdentifierValue);
        }
        IdentifierESDT shortnameBasedIdentifier = new IdentifierESDT();
        shortnameBasedIdentifier.setType(identifierTypes.getShortName());
        shortnameBasedIdentifier.setUse(IdentifierESDTUseEnum.USUAL);
        shortnameBasedIdentifier.setValue(newShortNameIdentifierValue);
        shortnameBasedIdentifier.setLeafValue(entry.getOrganisationShortName());
        organization.getIdentifiers().add(shortnameBasedIdentifier);
        IdentifierESDT longNameBasedIdentifier = new IdentifierESDT();
        longNameBasedIdentifier.setType(identifierTypes.getLongName());
        longNameBasedIdentifier.setUse(IdentifierESDTUseEnum.USUAL);
        longNameBasedIdentifier.setValue(newLongNameIdentifierValue);
        longNameBasedIdentifier.setLeafValue(entry.getOrganisationLongName());
        organization.getIdentifiers().add(longNameBasedIdentifier);
        // Assign simplifiedID
        organization.assignSimplifiedID(true, identifierTypes.getShortName(), IdentifierESDTUseEnum.USUAL);
        organization.setDisplayName(entry.getOrganisationShortName());
        organization.setDescription(entry.getOrganisationLongName());
        TypeESDT orgType = new TypeESDT();
        orgType.setTypeDefinition("OrganizationType");
        orgType.setTypeValue(entry.getOrganizationTypeShortName());
        orgType.setTypeDisplayValue(entry.getOrganizationTypeLongName());
        organization.setOrganizationType(orgType);
        LOG.debug(".buildOrganization(): Exit, OrganizationESR built");
        return (organization);
    }
}
