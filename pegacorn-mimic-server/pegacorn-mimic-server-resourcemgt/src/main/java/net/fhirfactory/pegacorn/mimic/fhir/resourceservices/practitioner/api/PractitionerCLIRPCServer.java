package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitioner.api;

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

import net.fhirfactory.pegacorn.internals.esr.resources.PractitionerESR;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierESDTUseEnum;
import net.fhirfactory.pegacorn.internals.esr.resources.datatypes.IdentifierType;
import net.fhirfactory.pegacorn.internals.esr.transactions.ESRMethodOutcome;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitioner.tasks.PractitionerCreator;

@ApplicationScoped
public class PractitionerCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(PractitionerCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private PractitionerCreator practitionerCreator;

    public PractitionerCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel prCLIServer = new JChannel().name("PractitionerRPC");
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

    public String createPractitioner(String inputString) throws Exception {
        LOG.info(".createPractitioner(): Entry, message --> {}", inputString);
        LOG.info(".createPractitioner(): Create a JSON Mapper");
        ObjectMapper jsonMapper = new ObjectMapper();
        LOG.info(".createPractitioner(): Initialise the practitionerDirectoryEntry (PractitionerDirectoryEntry) attribute");
        PractitionerESR practitionerESR = null;
        try{
            LOG.info(".createPractitioner(): Convert the incoming message string to a PractitionerDirectoryEntry object");
            practitionerESR = jsonMapper.readValue(inputString, PractitionerESR.class);
            LOG.info(".createPractitioner(): Conversion successful....");
        } catch(JsonMappingException mappingException){
            LOG.error(".createPractitioner(): JSON Mapper Error --> {}", mappingException.getMessage());
            mappingException.printStackTrace();
            practitionerESR = null;
        }
        if(practitionerESR != null){
            LOG.info(".createPractitioner(): Invoke PractitionerDirectoryEntry Conversion & Server Creation process");
            practitionerESR.assignSimplifiedID(true, IdentifierType.EMAIL_ADDRESS, IdentifierESDTUseEnum.USUAL);
            ESRMethodOutcome outcome = practitionerCreator.createPractitioner(practitionerESR);
            LOG.info(".createPractitioner(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}
