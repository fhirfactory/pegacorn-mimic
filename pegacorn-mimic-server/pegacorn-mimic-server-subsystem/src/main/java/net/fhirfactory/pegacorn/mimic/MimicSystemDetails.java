package net.fhirfactory.pegacorn.mimic;

import net.fhirfactory.pegacorn.common.model.componentid.TopologyNodeTypeEnum;
import net.fhirfactory.pegacorn.common.model.generalid.FDN;
import net.fhirfactory.pegacorn.common.model.generalid.FDNToken;
import net.fhirfactory.pegacorn.common.model.generalid.RDN;
import net.fhirfactory.pegacorn.deployment.properties.codebased.DeploymentSystemIdentificationInterface;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MimicSystemDetails implements DeploymentSystemIdentificationInterface {
    @Override
    public String getSystemName() {
        return "Mimic";
    }

    @Override
    public String getSystemVersion() {
        return "1.0.0";
    }

    @Override
    public FDNToken getSystemIdentifier() {
        FDN systemFDN = new FDN();
        systemFDN.appendRDN(new RDN(TopologyNodeTypeEnum.SUBSYSTEM.getNodeElementType(), "Mimic"));
        return(systemFDN.getToken());
    }

    @Override
    public String getSystemOwnerName() {
        return ("TBA");
    }
}
