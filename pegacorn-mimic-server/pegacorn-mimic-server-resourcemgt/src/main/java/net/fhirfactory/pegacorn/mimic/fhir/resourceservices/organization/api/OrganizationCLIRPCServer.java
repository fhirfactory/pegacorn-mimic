package net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.api;

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

import net.fhirfactory.buildingblocks.esr.models.resources.OrganizationESR;
import net.fhirfactory.buildingblocks.esr.models.transaction.ESRMethodOutcome;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.tasks.OrganizationCreator;

@ApplicationScoped
public class OrganizationCLIRPCServer {
    private static final Logger LOG = LoggerFactory.getLogger(OrganizationCLIRPCServer.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private CamelContext camelContext;

    @Inject
    private OrganizationCreator cvsLoaderTask;

    public OrganizationCLIRPCServer(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel organizationCLIServer = new JChannel().name("OrganizationRPC");
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
        LOG.info(".processRequest(): Initialise the organization (TrivialOrganization) attribute");
        OrganizationESR organization = null;
        try{
            LOG.info(".processRequest(): Convert the incoming message string to a TrivialOrganization object");
            organization = jsonMapper.readValue(inputString, OrganizationESR.class);
        } catch(JsonMappingException mappingException){
            LOG.error(".processRequest(): JSON Mapper Error --> {}", mappingException.getMessage());
            organization = null;
        }
        LOG.info(".processRequest(): Check to see if the TrivialOrganization has been converted");
        if(organization != null){
            LOG.info(".processRequest(): Invoke Organization Conversion & Server Creation process");
            ESRMethodOutcome outcome = cvsLoaderTask.createOrganization(organization);
            LOG.info(".processRequest(): Conversion/Creation process complete. Outcome --> {}", outcome);
            return(outcome.toString());
        }
        return("No Good....");
    }
}
