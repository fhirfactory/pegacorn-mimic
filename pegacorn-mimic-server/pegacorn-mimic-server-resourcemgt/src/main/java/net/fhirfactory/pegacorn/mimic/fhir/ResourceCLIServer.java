package net.fhirfactory.pegacorn.mimic.fhir;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.careteam.api.CareTeamCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.healthcareservice.api.HealthCareServiceCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.location.api.LocationCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.api.OrganizationCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitioner.api.PractitionerCLIRPCServer;
import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.practitionerrole.api.PractitionerRoleCLIRPCServer;

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
    
    @Inject
    private CareTeamCLIRPCServer careTeamCLIRPCServer;
    
    @Inject
    HealthCareServiceCLIRPCServer healthCareServiceCLIRPCServer;

    @PostConstruct
    public void initialise(){
        organizationCLIServer.doInitialisation();
        fhirServerClientMimic.doInitialisation();
        practitionerRoleCLIRPCServer.doInitialisation();
        practitionerCLIRPCServer.doInitialisation();
        locationCLIRPCServer.doInitialisation();
        careTeamCLIRPCServer.doInitialisation();
        healthCareServiceCLIRPCServer.doInitialisation();
    }

    @Override
    public void configure() throws Exception {
        from("direct:status")
                .log(LoggingLevel.INFO, "FHIRServerClientMimic Command --> ${body}");
    }
}
