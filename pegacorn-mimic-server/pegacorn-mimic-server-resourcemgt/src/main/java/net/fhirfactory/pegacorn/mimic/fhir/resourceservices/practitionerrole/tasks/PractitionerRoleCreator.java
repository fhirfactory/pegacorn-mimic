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
package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.tasks;

import net.fhirfactory.pegacorn.internals.esr.brokers.LocationESRBroker;
import net.fhirfactory.pegacorn.internals.esr.brokers.OrganizationESRBroker;
import net.fhirfactory.pegacorn.internals.esr.brokers.PractitionerRoleESRBroker;
import net.fhirfactory.pegacorn.internals.esr.resources.PractitionerRoleESR;
import net.fhirfactory.pegacorn.internals.esr.resources.common.ExtremelySimplifiedResource;
import net.fhirfactory.pegacorn.internals.esr.transactions.ESRMethodOutcome;
import net.fhirfactory.pegacorn.internals.esr.transactions.ESRMethodOutcomeEnum;
import net.fhirfactory.pegacorn.internals.esr.resources.common.CommonIdentifierESDTTypes;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierESDT;
import net.fhirfactory.pegacorn.internals.esr.resources.search.common.Pagination;
import net.fhirfactory.pegacorn.internals.esr.resources.search.common.SearchCriteria;
import net.fhirfactory.pegacorn.internals.esr.resources.search.common.Sort;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.common.ResourceStorageService;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.PractitionerRoleESRApproximate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PractitionerRoleCreator extends ResourceStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerRoleCreator.class);

    @Inject
    private PractitionerRoleESRBroker practitionerRoleDirectoryResourceBroker;

    @Inject
    private CommonIdentifierESDTTypes commonIdentifierESDTTypes;

    @Inject
    private RoleCreator roleCreator;

    @Inject
    private RoleCategoryCreator roleCategoryCreator;

    @Inject
    private LocationESRBroker locationBroker;

    @Inject
    private OrganizationESRBroker organizationBroker;

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    public ESRMethodOutcome createPractitionerRole(PractitionerRoleESRApproximate practitionerRoleApprox){
        LOG.info(".createPractitionerRole(): Entry, simplisticPR --> {}", practitionerRoleApprox);
        if(practitionerRoleApprox == null){
            LOG.info(".createPractitionerRole(): simplisticPR is null, return a failed MethodOutcome");
            ESRMethodOutcome outcome = new ESRMethodOutcome();
            outcome.setCreated(false);
            outcome.setStatus(ESRMethodOutcomeEnum.CREATE_ENTRY_INVALID);
            outcome.setStatusReason("PractitionerRoleDirectoryEntry resource is null");
            return(outcome);
        }
        LOG.info(".createPractitionerRole(): Invoke the PractitionerRoleDirectoryResourceBroker and create local entry");
        PractitionerRoleESR practitionerRole = new PractitionerRoleESR();
        practitionerRole.getIdentifiers().addAll(practitionerRoleApprox.getIdentifiers());
        practitionerRole.setDisplayName(practitionerRoleApprox.getDisplayName());
        if(practitionerRoleApprox.isPrimaryLocationIDContextual()){
            String locationID = getLocationID(practitionerRoleApprox.getPrimaryLocationID());
            practitionerRole.setPrimaryLocationID(locationID);
        } else {
            practitionerRole.setPrimaryLocationID(practitionerRoleApprox.getPrimaryLocationID());
        }
        if(practitionerRoleApprox.isPrimaryOrganizationIDContextual()){
            String organizationID = getOrganizationID(practitionerRoleApprox.getPrimaryOrganizationID());
            practitionerRole.setPrimaryLocationID(organizationID);
        } else {
            practitionerRole.setPrimaryLocationID(practitionerRoleApprox.getPrimaryOrganizationID());
        }
        ArrayList<String> roles = new ArrayList<>();
        roles.add(practitionerRoleApprox.getPrimaryRoleID());
        String roleCategoryID = createRoleCategory(practitionerRoleApprox.getPrimaryRoleCategoryID(), roles);
        practitionerRole.setPrimaryRoleCategoryID(roleCategoryID);
        String roleID = createRole(roleCategoryID, practitionerRoleApprox);
        practitionerRole.setPrimaryRoleID(roleID);
        practitionerRole.getContactPoints().addAll(practitionerRoleApprox.getContactPoints());
        practitionerRole.setPractitionerRoleADGroup(practitionerRoleApprox.getPractitionerRoleADGroup());
        ESRMethodOutcome outcome = practitionerRoleDirectoryResourceBroker.createPractitionerRole(practitionerRoleApprox);
        LOG.info(".createPractitionerRole(): PractitionerRoleESRBroker called, returning MethodOutcome");
        return(outcome);
    }

    private String createRole(String roleCategoryID, PractitionerRoleESRApproximate practitionerRoleESRApproximate){
        LOG.debug(".createRole(): Entry, roleCategory->{}, practitionerRole->{}", roleCategoryID, practitionerRoleESRApproximate);
        if(roleCategoryID == null || practitionerRoleESRApproximate == null ){
            LOG.debug(".createRole(): Exit, either roleCategoryID or practitionerRole are null");
            return(null);
        }
        if(practitionerRoleESRApproximate.getPrimaryRoleID() == null){
            LOG.debug(".createRole(): Exit, primaryRoleID is null");
            return(null);
        }
        ESRMethodOutcome outcome = roleCreator.createRole(roleCategoryID, practitionerRoleESRApproximate.getPrimaryRoleID());
        if(outcome.isCreated()){
            LOG.debug(".createRole(): Exit, Role created.");
            return(outcome.getId());
        }
        LOG.debug(".createRole(): Exit, Role failed to be created.");
        return(null);
    }

    private String createRoleCategory(String roleCategoryID, List<String> roleIDs){
        if(roleCategoryID == null ){
            return(null);
        }
        ESRMethodOutcome outcome = roleCategoryCreator.createRoleCategory(roleCategoryID, roleIDs);
        if(outcome.isCreated()){
            return(outcome.getId());
        }
        return(null);
    }

    private String getLocationID(String providedName){
        try {
            ESRMethodOutcome outcome = locationBroker.searchForESRsUsingAttribute(new SearchCriteria("leafValue",providedName), new Sort(), new Pagination());
            if(outcome.isSearchSuccessful()) {
                ExtremelySimplifiedResource esr = outcome.getSearchResult().get(0);
                String identifierValue = esr.getSimplifiedID();
                return (identifierValue);
            }
        } catch( Exception ex) {
            // nothing
        }
        return(null);
    }

    private String getOrganizationID(String providedName){
        try {
            ESRMethodOutcome outcome = organizationBroker.searchForESRsUsingAttribute(new SearchCriteria("leafValue",providedName), new Sort(), new Pagination());
            if(outcome.isSearchSuccessful()) {
                ExtremelySimplifiedResource esr = outcome.getSearchResult().get(0);
                String identifierValue = esr.getSimplifiedID();
                return (identifierValue);
            }
        } catch( Exception ex) {
            // nothing
        }
        return(null);
    }
}
