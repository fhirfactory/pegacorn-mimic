package net.fhirfactory.pegacorn.mimic.synapsesink;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

public class SynapseMessageSink extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("netty-http:http://0.0.0.0:21000/transactions/{id}")
                .routeId("Metrix Event Receiver -->")
                .transform(simple("${bodyAs(String)}"))
                .log(LoggingLevel.INFO, "{$body}");

    }
}
