package net.fhirfactory.pegacorn.mimic.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command( name = "practitionerCLI", description = "FHIR::Practitioner CRUD Operaitons CLI" )
public class PractitionerCLI implements Runnable {

    public static void main (String[] args){
        CommandLine.run(new PractitionerCLI(), args);
    }

    @Override
    public void run() {
    }
}
