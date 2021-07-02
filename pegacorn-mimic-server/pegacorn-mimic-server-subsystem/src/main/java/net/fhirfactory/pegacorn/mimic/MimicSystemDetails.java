package net.fhirfactory.pegacorn.mimic;

import javax.enterprise.context.ApplicationScoped;

import net.fhirfactory.pegacorn.common.model.componentid.TopologyNodeTypeEnum;
import net.fhirfactory.pegacorn.common.model.generalid.FDN;
import net.fhirfactory.pegacorn.common.model.generalid.FDNToken;
import net.fhirfactory.pegacorn.common.model.generalid.RDN;
import net.fhirfactory.pegacorn.deployment.topology.model.mode.ConcurrencyModeEnum;
import net.fhirfactory.pegacorn.deployment.topology.model.mode.ResilienceModeEnum;

@ApplicationScoped
public class MimicSystemDetails  {

    public String getSystemName() {
        return "Mimic";
    }


    public String getSystemVersion() {
        return "1.0.0";
    }


    public FDNToken getSystemIdentifier() {
        FDN systemFDN = new FDN();
        systemFDN.appendRDN(new RDN(TopologyNodeTypeEnum.SUBSYSTEM.getNodeElementType(), "Mimic"));
        return(systemFDN.getToken());
    }

    public String getSystemOwnerName() {
        return ("TBA");
    }


    public ConcurrencyModeEnum getDefaultSubsystemConcurrencyMode() {
        return (ConcurrencyModeEnum.CONCURRENCY_MODE_STANDALONE);
    }


    public ResilienceModeEnum getDefaultSubsystemResilienceMode() {
        return (ResilienceModeEnum.RESILIENCE_MODE_STANDALONE);
    }
}
