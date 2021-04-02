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
package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.transformations;

import net.fhirfactory.pegacorn.internals.fhir.r4.resources.organization.OrganizationFactory;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.organization.OrganizationResourceHelpers;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.resource.InformationConfidentialitySecurityCodeEnum;
import net.fhirfactory.pegacorn.internals.fhir.r4.resources.resource.SecurityLabelFactory;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.TrivialOrganization;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.codesystems.OrganizationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TrivialOrganization2FHIROrganization {
    private static final Logger LOG = LoggerFactory.getLogger(TrivialOrganization2FHIROrganization.class);

    @Inject
    private OrganizationFactory organizationFactory;

    @Inject
    private OrganizationResourceHelpers organizationResourceHelpers;

    @Inject
    private SecurityLabelFactory securityLabelFactory;

    public Organization convertToOrganization(TrivialOrganization incomingSimpleOrganization){
        LOG.debug(".convertToOrganization(): Entry, incomingSimpleOrganization --> {}", incomingSimpleOrganization);
        String orgShortName = incomingSimpleOrganization.getOrganizationShortName();
        String orgLongName = incomingSimpleOrganization.getOrganizationLongName();
        Reference organisationReference = null;
        if(incomingSimpleOrganization.getParentOrganizationShortName() != null) {
            String parentOrgShortName = incomingSimpleOrganization.getParentOrganizationShortName();
            organisationReference = organizationResourceHelpers.buildOrganizationReference(parentOrgShortName, Identifier.IdentifierUse.USUAL);
        }
        OrganizationType orgType = OrganizationType.DEPT;
        if(incomingSimpleOrganization.getOrganizationTypeCode() != null){
            if(incomingSimpleOrganization.getOrganizationTypeCode().contentEquals("Prov")){
                orgType = OrganizationType.PROV;
            }
        }
        Organization newOrganization = organizationFactory.buildOrganization(orgShortName, orgLongName, orgType, organisationReference);
        Coding confidentialitySecurityLabel = securityLabelFactory.constructConfidentialitySecurityLabel(InformationConfidentialitySecurityCodeEnum.U);
        Meta organizationMetadata = new Meta();
        organizationMetadata.addSecurity(confidentialitySecurityLabel);
        newOrganization.setMeta(organizationMetadata);
        LOG.debug(".convertToOrganization(): Exit, created Organization --> {}", organisationReference);
        return(newOrganization);
    }
}
