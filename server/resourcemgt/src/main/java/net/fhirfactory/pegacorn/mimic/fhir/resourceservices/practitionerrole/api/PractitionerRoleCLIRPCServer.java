package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.api;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.core.model.ui.transactions.ESRMethodOutcome;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.tasks.PractitionerRoleCreator;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.PractitionerRoleESRApproximate;
import org.apache.camel.CamelContext;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PractitionerRoleCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerRoleCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private PractitionerRoleCreator practitionerRoleCreator;

    public PractitionerRoleCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel prCLIServer = new JChannel().name("PractitionerRoleRPC");
                rpcDispatcher = new RpcDispatcher(prCLIServer, this);
                prCLIServer.connect("ResourceCLI");
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
        LOG.info(".processRequest(): Initialise the incomingPractitionerRole (PractitionerRoleDirectoryEntry) attribute");
        PractitionerRoleESRApproximate incomingPractitionerRole = null;
        try{
            LOG.info(".processRequest(): Convert the incoming message string to a PractitionerRoleDirectoryEntry object");
            incomingPractitionerRole = jsonMapper.readValue(inputString, PractitionerRoleESRApproximate.class);
        } catch(JsonMappingException mappingException){
            LOG.error(".processRequest(): JSON Mapper Error --> {}", mappingException.getMessage());
            incomingPractitionerRole = null;
        }
        LOG.info(".processRequest(): Check to see if the PractitionerRoleDirectoryEntry has been converted");
        if(incomingPractitionerRole != null){
            LOG.info(".processRequest(): Invoke PractitionerRole/PractitionerRoleDirectoryEntry Conversion & Server Creation process");
            ESRMethodOutcome outcome = practitionerRoleCreator.createPractitionerRole(incomingPractitionerRole);
            LOG.info(".processRequest(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}