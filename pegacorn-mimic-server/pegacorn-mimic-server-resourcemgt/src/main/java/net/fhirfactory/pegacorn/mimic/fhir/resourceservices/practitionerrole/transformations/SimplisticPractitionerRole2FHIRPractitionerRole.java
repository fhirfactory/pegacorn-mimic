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
package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.transformations;

import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.group.GroupFactory;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.group.GroupResourceHelpers;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.location.LocationResourceHelper;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.organization.OrganizationResourceHelpers;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.practitionerrole.PractitionerRoleFactory;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.practitionerrole.PractitionerRoleResourceHelper;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.practitionerrole.RoleFactory;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.resource.InformationConfidentialitySecurityCodeEnum;
import net.fhirfactory.pegacorn.datasets.fhir.r4.resources.resource.SecurityLabelFactory;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.SimplisticPractitionerRole;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SimplisticPractitionerRole2FHIRPractitionerRole {
    private static final Logger LOG = LoggerFactory.getLogger(SimplisticPractitionerRole2FHIRPractitionerRole.class);

    @Inject
    private PractitionerRoleFactory prFactory;

    @Inject
    private PractitionerRoleResourceHelper prResourceHelper;

    @Inject
    private OrganizationResourceHelpers organizationResourceHelpers;

    @Inject
    private LocationResourceHelper locationResourceHelper;

    @Inject
    private RoleFactory roleFactory;

    @Inject
    private SecurityLabelFactory securityLabelFactory;

    @Inject
    private GroupFactory groupFactory;

    public SimplisticPractitionerRole2FHIRPractitionerRole() {
    }

    public PractitionerRole convertToPractitionerRole(SimplisticPractitionerRole simplisticPractitionerRole){
        String prCode = simplisticPractitionerRole.getPractitionerRoleShortName();
        String prName = simplisticPractitionerRole.getPractitionerRoleLongName();
        ArrayList<CodeableConcept> roleSet = new ArrayList<>();
        CodeableConcept role = roleFactory.buildRoleWithCategory(simplisticPractitionerRole.getRoleCategory(), simplisticPractitionerRole.getRoleShortName());
        roleSet.add(role);
        PractitionerRole practitionerRole = prFactory.buildPractitionerRole(prCode, prName, roleSet);
        List<ContactPoint> contactPoints = createContactPoints(simplisticPractitionerRole);
        practitionerRole.getTelecom().addAll(contactPoints);
        Reference organizationReference = organizationResourceHelpers.buildOrganizationReference(simplisticPractitionerRole.getOrganizationShortName(), Identifier.IdentifierUse.USUAL);
        practitionerRole.setOrganization(organizationReference);
        Reference locationReference = locationResourceHelper.buildLocationReference(simplisticPractitionerRole.getLocationShortName(), Identifier.IdentifierUse.USUAL);
        practitionerRole.addLocation(locationReference);
        Coding confidentialitySecurityLabel = securityLabelFactory.constructConfidentialitySecurityLabel(InformationConfidentialitySecurityCodeEnum.U);
        Meta prMetadata = new Meta();
        prMetadata.addSecurity(confidentialitySecurityLabel);
        practitionerRole.setMeta(prMetadata);
        return(practitionerRole);
    }

    private ContactPoint createContactPoint(ContactPoint.ContactPointSystem cpSystem, ContactPoint.ContactPointUse cpUse, String cpValue, int cpRank){
        ContactPoint newContactPoint = new ContactPoint();
        newContactPoint.setSystem(cpSystem);
        newContactPoint.setValue(cpValue);
        newContactPoint.setRank(cpRank);
        newContactPoint.setUse(cpUse);
        return(newContactPoint);
    }

    private List<ContactPoint> createContactPoints(SimplisticPractitionerRole simplisticPR){
        ArrayList<ContactPoint> contactPointList = new ArrayList<ContactPoint>();
        int rank = 1;
        for(String currentExtension: simplisticPR.getLandlineNumbers()){
            ContactPoint currentContactPoint = createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.WORK, currentExtension, rank);
            rank += 1;
            contactPointList.add(currentContactPoint);
        }
        for(String currentMobile: simplisticPR.getMobileNumbers()) {
            ContactPoint mobilePhone = createContactPoint(ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, currentMobile, rank);
            rank += 1;
            contactPointList.add(mobilePhone);
        }
        for(String currentMobile: simplisticPR.getMobileNumbers()) {
            ContactPoint smsPhone = createContactPoint(ContactPoint.ContactPointSystem.SMS, ContactPoint.ContactPointUse.WORK, currentMobile, rank);
            rank += 1;
            contactPointList.add(smsPhone);
        }
        return(contactPointList);
    }

    public Group createPractitionerGroup(SimplisticPractitionerRole simplisticPractitionerRole){
        Group practitionerGroup = groupFactory.buildPractitionerGroupForPractitionerRole(simplisticPractitionerRole.getPractitionerRoleShortName());
        return(practitionerGroup);
    }
}
