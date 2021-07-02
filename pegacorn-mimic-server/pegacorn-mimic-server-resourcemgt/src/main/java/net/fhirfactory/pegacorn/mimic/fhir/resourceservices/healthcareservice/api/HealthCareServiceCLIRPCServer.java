package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.healthcareservice.api;

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

import net.fhirfactory.pegacorn.internals.esr.resources.HealthcareServiceESR;
import net.fhirfactory.pegacorn.internals.esr.transactions.ESRMethodOutcome;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.healthcareservice.tasks.HealthCareServiceCreator;

@ApplicationScoped
public class HealthCareServiceCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCareServiceCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private HealthCareServiceCreator healthCareServiceCreatorTask;

    public HealthCareServiceCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel healthCareServiceCLIServer = new JChannel().name("HealthCareServiceRPC");
                rpcDispatcher = new RpcDispatcher(healthCareServiceCLIServer, this);
                healthCareServiceCLIServer.connect("ResourceCLI");
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
        LOG.info(".processRequest(): Initialise the care team (HealthcareServiceESR) attribute");
        HealthcareServiceESR healthCareService = null;
        try{
            LOG.info(".processRequest(): Convert the incoming message string to a HealthcareServiceESR object");
            healthCareService = jsonMapper.readValue(inputString, HealthcareServiceESR.class);
        } catch(JsonMappingException mappingException){
            LOG.error(".processRequest(): JSON Mapper Error --> {}", mappingException.getMessage());
            healthCareService = null;
        }
        LOG.info(".processRequest(): Check to see if the HealthcareServiceESR has been converted");
        if(healthCareService != null){
            LOG.info(".processRequest(): Invoke Health Care Service Conversion & Server Creation process");
            ESRMethodOutcome outcome = healthCareServiceCreatorTask.createHealthCareService(healthCareService);
            LOG.info(".processRequest(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}
