package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.api;

import ca.uhn.fhir.rest.api.MethodOutcome;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.tasks.OrganizationCVSLoaderTask;
import net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary.TrivialOrganization;
import org.apache.camel.CamelContext;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class OrganizationCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private OrganizationCVSLoaderTask cvsLoaderTask;

    public OrganizationCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel organizationCLIServer = new JChannel().name("OrganizationRPC");
//                JgroupsComponentBuilderFactory.jgroups().channel(organizationCLIServer).register(camelContext, "OrganizationRPC");
                rpcDispatcher = new RpcDispatcher(organizationCLIServer, this);
                organizationCLIServer.connect("ResourceCLI");
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
        LOG.info(".processRequest(): Initialise the trivOrg (TrivialOrganization) attribute");
        TrivialOrganization trivOrg = null;
        try{
            LOG.info(".processRequest(): Convert the incoming message string to a TrivialOrganization object");
            trivOrg = jsonMapper.readValue(inputString, TrivialOrganization.class);
        } catch(JsonMappingException mappingException){
            LOG.error(".processRequest(): JSON Mapper Error --> {}", mappingException.getMessage());
            trivOrg = null;
        }
        LOG.info(".processRequest(): Check to see if the TrivialOrganization has been converted");
        if(trivOrg != null){
            LOG.info(".processRequest(): Invoke Organization Conversion & Server Creation process");
            MethodOutcome outcome = cvsLoaderTask.doCommandCreateFromCSVEntry(trivOrg);
            LOG.info(".processRequest(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}
