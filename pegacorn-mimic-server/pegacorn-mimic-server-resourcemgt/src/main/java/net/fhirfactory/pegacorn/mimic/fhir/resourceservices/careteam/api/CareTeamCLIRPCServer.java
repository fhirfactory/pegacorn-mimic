package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.careteam.api;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.fhirfactory.buildingblocks.esr.models.resources.CareTeamESR;
import net.fhirfactory.buildingblocks.esr.models.resources.LocationESR;
import net.fhirfactory.buildingblocks.esr.models.transaction.ESRMethodOutcome;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.careteam.tasks.CareTeamCreator;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.location.tasks.LocationCreator;

@ApplicationScoped
public class CareTeamCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(CareTeamCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private CareTeamCreator careTeamCreatorTask;

    public CareTeamCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel careTeamCLIServer = new JChannel().name("CareTeamRPC");
                rpcDispatcher = new RpcDispatcher(careTeamCLIServer, this);
                careTeamCLIServer.connect("ResourceCLI");
            } catch (Exception ex) {
                LOG.error(".initialise(): Error --> {}", ex.getMessage());
            }
        }
    }

    public void doInitialisation(){
        initialise();
    }

    public String processRequest(String inputString) throws Exception {
        LOG.info(".processRequest(): Entry, message --> {}", inputString);
        LOG.info(".processRequest(): Create a JSON Mapper");
        ObjectMapper jsonMapper = new ObjectMapper();
        LOG.info(".processRequest(): Initialise the care team (CareTeamESR) attribute");
        CareTeamESR careTeam = null;
        try{
            LOG.info(".processRequest(): Convert the incoming message string to a CareTeamESR object");
            careTeam = jsonMapper.readValue(inputString, CareTeamESR.class);
        } catch(JsonMappingException mappingException){
            LOG.error(".processRequest(): JSON Mapper Error --> {}", mappingException.getMessage());
            careTeam = null;
        }
        LOG.info(".processRequest(): Check to see if the CareTeamESR has been converted");
        if(careTeam != null){
            LOG.info(".processRequest(): Invoke Location Conversion & Server Creation process");
            ESRMethodOutcome outcome = careTeamCreatorTask.createCareTeam(careTeam);
            LOG.info(".processRequest(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}
