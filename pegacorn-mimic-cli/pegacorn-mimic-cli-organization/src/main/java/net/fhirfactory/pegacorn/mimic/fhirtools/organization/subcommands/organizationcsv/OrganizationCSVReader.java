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
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries.OrganizationCSVEntry;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.SimplisticOrganization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OrganizationCSVReader {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCSVReader.class);

    private List<OrganizationCSVEntry> elementList;
    private SimplisticOrganization rootOrganization;

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
        LOG.debug(".readOrganizationCSV(): Exit, file read");
        return(this.elementList);
    }

    public List<SimplisticOrganization> organiseOrganizationHierarchy(){
        LOG.debug(".organiseOrganizationHierarchy(): Entry");
        ArrayList<OrganizationCSVEntry> workingList = new ArrayList<>();
        workingList.addAll(this.elementList);
        ArrayList<SimplisticOrganization> rootEntries = new ArrayList<>();
        HashMap<String, SimplisticOrganization> allEntries = new HashMap<>();
        LOG.trace(".organiseOrganizationHierarchy(): Determining Parent (root) Organization");
        for(OrganizationCSVEntry currentEntry: workingList) {
            SimplisticOrganization currentOrg = new SimplisticOrganization();
            currentOrg.setOrganizationShortName(currentEntry.getOrganisationShortName());
            currentOrg.setOrganizationLongName(currentOrg.getOrganizationLongName());
            currentOrg.setOrganizationTypeCode(currentEntry.getOrganizationTypeShortName());
            currentOrg.setOrganizationTypeName(currentEntry.getOrganizationTypeLongName());
            currentOrg.setParentOrganizationShortName(currentEntry.getOrganisationParentShortName());
            allEntries.put(currentEntry.getOrganisationShortName(), currentOrg);
        }
        for(SimplisticOrganization currentOrg: allEntries.values()){
            if(currentOrg.getParentOrganizationShortName() == null || currentOrg.getParentOrganizationShortName().equals("")){
                rootEntries.add(currentOrg);
            } else {
                boolean found = false;
                for(String orgName: allEntries.keySet()){
                    if(currentOrg.getParentOrganizationShortName().contentEquals(orgName)){
                        currentOrg.setParentOrganization(allEntries.get(orgName));
                        found = true;
                        break;
                    }
                }
                if(!found){
                    rootEntries.add(currentOrg);
                }
            }
        }
        for(SimplisticOrganization currentOrg: allEntries.values()){
            printHierarchy(currentOrg);
        }
        ArrayList<SimplisticOrganization> allOrgs = new ArrayList<>();
        allOrgs.addAll(allEntries.values());
        LOG.debug(".organiseOrganizationHierarchy(): Exit, Organization hierarchy established");
        return(allOrgs);
    }

    private void printHierarchy(SimplisticOrganization leaf){
        String outputString = leaf.getOrganizationShortName();
        while(leaf.getParentOrganization() != null){
            leaf = leaf.getParentOrganization();
            outputString = leaf.getOrganizationShortName() + "->" + outputString;
        }
        LOG.info("Hierarchy: {}", outputString);
    }
}
