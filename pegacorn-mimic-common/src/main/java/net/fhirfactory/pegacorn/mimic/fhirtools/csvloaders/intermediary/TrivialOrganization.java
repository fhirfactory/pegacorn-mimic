package net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary;

import java.util.ArrayList;

public class TrivialOrganization {
    private ArrayList<String> containedOrganisationShortNames;
    private String organizationShortName;
    private String organizationLongName;
    private String organizationTypeCode;
    private String organizationTypeName;
    private String parentOrganizationShortName;

    public TrivialOrganization(){
        containedOrganisationShortNames = new ArrayList<>();
    }


    public ArrayList<String> getContainedOrganisationShortNames() {
        return containedOrganisationShortNames;
    }

    public void setContainedOrganisationShortNames(ArrayList<String> containedOrganisationShortNames) {
        this.containedOrganisationShortNames = containedOrganisationShortNames;
    }

    public String getOrganizationShortName() {
        return organizationShortName;
    }

    public void setOrganizationShortName(String organizationShortName) {
        this.organizationShortName = organizationShortName;
    }

    public String getOrganizationLongName() {
        return organizationLongName;
    }

    public void setOrganizationLongName(String organizationLongName) {
        this.organizationLongName = organizationLongName;
    }

    public String getOrganizationTypeCode() {
        return organizationTypeCode;
    }

    public void setOrganizationTypeCode(String organizationTypeCode) {
        this.organizationTypeCode = organizationTypeCode;
    }

    public String getOrganizationTypeName() {
        return organizationTypeName;
    }

    public void setOrganizationTypeName(String organizationTypeName) {
        this.organizationTypeName = organizationTypeName;
    }

    public String getParentOrganizationShortName() {
        return parentOrganizationShortName;
    }

    public void setParentOrganizationShortName(String parentOrganizationShortName) {
        this.parentOrganizationShortName = parentOrganizationShortName;
    }
}
