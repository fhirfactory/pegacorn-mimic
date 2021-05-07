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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.buildingblocks.esr.models.transaction.ESRMethodOutcome;
import net.fhirfactory.buildingblocks.esr.models.transaction.ESRMethodOutcomeEnum;
import net.fhirfactory.pegacorn.internals.esr.brokers.RoleESRBroker;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.common.ResourceStorageService;

@ApplicationScoped
public class RoleCreator extends ResourceStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(RoleCreator.class);

    @Inject
    private RoleESRBroker roleBroker;

    @Override
    protected Logger getLogger(){
        return(LOG);
    }

    public ESRMethodOutcome createRole(String roleCategoryID, String roleID){
        LOG.info(".createRole(): Entry, roleCategoryID --> {}, role --> {}", roleCategoryID, roleID );
        if(roleID == null){
            LOG.info(".createRole(): roleID is null, return a failed MethodOutcome");
            ESRMethodOutcome outcome = new ESRMethodOutcome();
            outcome.setCreated(false);
            outcome.setStatus(ESRMethodOutcomeEnum.CREATE_ENTRY_INVALID);
            outcome.setStatusReason("createRole resource is null");
            return(outcome);
        }
        LOG.info(".createRole(): Invoke the RoleESRBroker and create local entry");
        ESRMethodOutcome outcome = roleBroker.createRole(roleCategoryID, roleID);
        LOG.info(".createRole(): FHIR Server called, returning MethodOutcome");
        return(outcome);
    }
}
