package net.fhirfactory.pegacorn.mimic.fhirtools.practitionerrole;

import net.fhirfactory.pegacorn.mimic.fhirtools.practitionerrole.subcommands.practitionerrolecsv.LoadFromCSVCLI;
import picocli.CommandLine;

@CommandLine.Command(
        name="PractitionerRole",
        description="PractitionerRole CLI",
        subcommands = {
                LoadFromCSVCLI.class
        }
)
public class PractitionerRoleCLI implements Runnable{
    public static void main(String[] args) {
        CommandLine.run(new PractitionerRoleCLI(), args);
    }

    @Override
    public void run() {
        System.out.println("The PractitionerRoleCLI command");
    }
}
