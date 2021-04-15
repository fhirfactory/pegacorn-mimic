package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.location.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.internals.esr.resources.LocationESR;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.location.tasks.LocationCreator;
import org.apache.camel.CamelContext;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fhirfactory.pegacorn.internals.esr.transactions.ESRMethodOutcome;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LocationCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(LocationCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private LocationCreator locationCreatorTask;

    public LocationCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel locationCLIServer = new JChannel().name("LocationRPC");
                rpcDispatcher = new RpcDispatcher(locationCLIServer, this);
                locationCLIServer.connect("ResourceCLI");
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
        LOG.info(".processRequest(): Initialise the location (LocationESR) attribute");
        LocationESR location = null;
        try{
            LOG.info(".processRequest(): Convert the incoming message string to a LocationESR object");
            location = jsonMapper.readValue(inputString, LocationESR.class);
        } catch(JsonMappingException mappingException){
            LOG.error(".processRequest(): JSON Mapper Error --> {}", mappingException.getMessage());
            location = null;
        }
        LOG.info(".processRequest(): Check to see if the LocationESR has been converted");
        if(location != null){
            LOG.info(".processRequest(): Invoke Location Conversion & Server Creation process");
            ESRMethodOutcome outcome = locationCreatorTask.createLocation(location);
            LOG.info(".processRequest(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}
