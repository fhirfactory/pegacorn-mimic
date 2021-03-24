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

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import net.fhirfactory.pegacorn.datasets.fhir.r4.operationaloutcome.OperationOutcomeGenerator;
import net.fhirfactory.pegacorn.datasets.fhir.r4.operationaloutcome.OperationOutcomeSeverityEnum;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.group.GroupFactory;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.group.GroupResourceHelpers;
import net.fhirfactory.pegacorn.mimic.fhir.FHIRServerClientMimic;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.transformations.SimplisticPractitionerRole2FHIRPractitionerRole;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.SimplisticPractitionerRole;
import net.fhirfactory.pegacorn.util.FHIRContextUtility;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PractitionerGroupCreator {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerGroupCreator.class);

    @Inject
    private FHIRServerClientMimic clientMimic;

    @Inject
    private FHIRContextUtility contextUtility;

    @Inject
    private GroupFactory groupFactory;

    @Inject
    private GroupResourceHelpers groupResourceHelpers;

    @Inject
    private OperationOutcomeGenerator outcomeGeneration;

    public MethodOutcome createPractitionerGroup(SimplisticPractitionerRole simplisticPR){
        LOG.info(".createPractitionerGroup(): Entry, simplisticPR --> {}", simplisticPR);
        if(simplisticPR == null){
            LOG.info(".createPractitionerGroup(): simplisticPR is null, return a failed MethodOutcome");
            OperationOutcome operationOutcome = outcomeGeneration.generateResourceFailedAddOutcome(null, OperationOutcomeSeverityEnum.SEVERITY_ERROR);
            MethodOutcome outcome = new MethodOutcome();
            outcome.setId(operationOutcome.getIdElement());
            outcome.setCreated(false);
            outcome.setOperationOutcome(operationOutcome);
            return(outcome);
        }
        LOG.info(".createPractitionerGroup(): Resolve the FHIR Context");
        FhirContext fhirContext = contextUtility.getFhirContext();
        LOG.info(".createPractitionerGroup(): Get the FHIR Server Client (server address:{})",clientMimic.getTargetFHIRServerURL() );
        IGenericClient fhirServerClient = fhirContext.newRestfulGenericClient(clientMimic.getTargetFHIRServerURL());
        LOG.info(".createPractitionerGroup(): Convert TrivialOrganization --> to --> FHIR::Organization");
        Group practitionerGroup = null;
        try {
            practitionerGroup = groupFactory.buildPractitionerGroupForPractitionerRole(simplisticPR.getRoleShortName());
        } catch(Exception ex){
            ex.printStackTrace();
        }
        LOG.info(".createPractitionerGroup(): Convert Resource to JSON String");
        String resourceAsString = convertToJSONString(practitionerGroup);
        LOG.info(".createPractitionerGroup(): Call the FHIR Server and Add Organization");
        MethodOutcome outcome = fhirServerClient
                .create()
                .resource(resourceAsString)
                .execute();
        LOG.info(".createPractitionerGroup(): FHIR Server called, returning MethodOutcome");
        return(outcome);
    }

    private String convertToJSONString(Group practitionerGroup){
        IParser jsonParser = contextUtility.getJsonParser();
        String orgAsString = jsonParser.encodeResourceToString(practitionerGroup);
        return(orgAsString);
    }
}
