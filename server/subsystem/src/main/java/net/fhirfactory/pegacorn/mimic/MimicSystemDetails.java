package net.fhirfactory.pegacorn.mimic;


import net.fhirfactory.pegacorn.core.constants.systemwide.DeploymentSystemIdentificationInterface;
import net.fhirfactory.pegacorn.core.model.componentid.PegacornSystemComponentTypeTypeEnum;
import net.fhirfactory.pegacorn.core.model.generalid.FDN;
import net.fhirfactory.pegacorn.core.model.generalid.FDNToken;
import net.fhirfactory.pegacorn.core.model.generalid.RDN;

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

    public FDNToken getSystemIdentifier() {
        FDN systemFDN = new FDN();
        systemFDN.appendRDN(new RDN(PegacornSystemComponentTypeTypeEnum.SUBSYSTEM.getDisplayName(), "Mimic"));
        return(systemFDN.getToken());
    }

    @Override
    public String getSystemOwnerName() {
        return ("TBA");
    }
}
