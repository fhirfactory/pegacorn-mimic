package net.fhirfactory.pegacorn.mimic;

import net.fhirfactory.pegacorn.datasets.fhir.r4.internal.systems.DeploymentInstanceDetail;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MimicSiteDetails extends DeploymentInstanceDetail {

    public MimicSiteDetails(){
        super();
    }

    @Override
    protected String specifyOrganizationName() {
        return("Canberra Health Services");
    }

    @Override
    protected String specifySystemOwnerContactName() {
        return ("TBA");
    }

    @Override
    protected String sepcifySystemOwnerContactEmail() {
        return ("TBA");
    }

    @Override
    protected String specifySystemOwnerContactPhone() {
        return ("TBA");
    }

    @Override
    protected String specifySystemAdministratorContactName() {
        return ("TBA");
    }

    @Override
    protected String specifySystemAdministratorContactEmail() {
        return ("TBA");
    }

    @Override
    protected String specifySystemAdministratorContactPhone() {
        return ("TBA");
    }

    @Override
    protected String specifyEndpointName() {
        return ("AETHER");
    }

    @Override
    protected CodeableConcept specifyOrganizationType() {
        CodeableConcept orgType = new CodeableConcept();
        Coding orgTypeCoding = new Coding();
        orgTypeCoding.setSystem("https://www.hl7.org/fhir/codesystem-organization-type.html");
        orgTypeCoding.setCode("prov");
        orgTypeCoding.setDisplay("Healthcare Provider");
        orgType.setText("Healthcare Provider");
        orgType.getCoding().add(orgTypeCoding);
        return(orgType);
    }

    @Override
    protected String createSystemReference() {
        return ("http://health.act.gov.au/aether");
    }


}
