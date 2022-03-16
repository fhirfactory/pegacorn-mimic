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
package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.location.tasks;

import net.fhirfactory.pegacorn.core.model.ui.brokers.LocationESRBroker;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.LocationESR;
import net.fhirfactory.pegacorn.core.model.ui.transactions.ESRMethodOutcome;
import net.fhirfactory.pegacorn.core.model.ui.transactions.ESRMethodOutcomeEnum;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.common.ResourceStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LocationCreator extends ResourceStorageService {
    private static final Logger LOG = LoggerFactory.getLogger(LocationCreator.class);

    @Inject
    private LocationESRBroker esrBroker;

    public ESRMethodOutcome createLocation(LocationESR location){
        LOG.info(".createLocation(): Entry, location --> {}", location);
        if(location == null){
            LOG.info(".createLocation(): location is null, return a failed MethodOutcome");
            ESRMethodOutcome outcome = new ESRMethodOutcome();
            outcome.setCreated(false);
            outcome.setStatus(ESRMethodOutcomeEnum.CREATE_ENTRY_INVALID);
            outcome.setStatusReason("createLocation resource is null");
            return(outcome);
        }
        LOG.info(".createLocation(): Invoke the OrganizationESRBroker and create local entry");
        ESRMethodOutcome outcome = esrBroker.createLocationDE(location);
        LOG.info(".createLocation(): FHIR Server called, returning MethodOutcome");
        return(outcome);
    }

    @Override
    protected Logger getLogger(){
        return(LOG);
    }
}