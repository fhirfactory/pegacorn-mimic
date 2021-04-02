package net.fhirfactory.pegacorn.mimic.fhirtools.commands.model;

public enum MimicFHIRCommandAttributeTypeEnum {
    ATTRIBUTE_FHIR_RESOURCE("attribute_fhir_resource"),
    ATTRIBUTE_SIMPLE_ORGANIZATION("attribute_simple_organization"),
    ATTRIBUTE_URL("attribute_url");

    private String attributeType;

    private MimicFHIRCommandAttributeTypeEnum(String value){
        this.attributeType = value;
    }

    public String getAttributeType(){
        return(this.attributeType);
    }
}
