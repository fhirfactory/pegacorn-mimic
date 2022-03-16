package net.fhirfactory.pegacorn.mimic.fhir;

import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.location.api.LocationCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.api.OrganizationCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitioner.api.PractitionerCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.api.PractitionerRoleCLIRPCServer;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ResourceCLIServer extends RouteBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ResourceCLIServer.class);

    @Inject
    private OrganizationCLIRPCServer organizationCLIServer;

    @Inject
    private PractitionerRoleCLIRPCServer practitionerRoleCLIRPCServer;

    @Inject
    private FHIRServerClientMimic fhirServerClientMimic;

    @Inject
    private PractitionerCLIRPCServer practitionerCLIRPCServer;

    @Inject
    private LocationCLIRPCServer locationCLIRPCServer;

    @PostConstruct
    public void initialise(){
        organizationCLIServer.doInitialisation();
        fhirServerClientMimic.doInitialisation();
        practitionerRoleCLIRPCServer.doInitialisation();
        practitionerCLIRPCServer.doInitialisation();
        locationCLIRPCServer.doInitialisation();
    }

    @Override
    public void configure() throws Exception {
        from("direct:status")
                .log(LoggingLevel.INFO, "FHIRServerClientMimic Command --> ${body}");
    }
}
