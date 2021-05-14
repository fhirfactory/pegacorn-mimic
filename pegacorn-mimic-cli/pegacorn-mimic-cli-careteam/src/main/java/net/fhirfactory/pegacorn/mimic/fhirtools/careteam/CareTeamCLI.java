package net.fhirfactory.pegacorn.mimic.fhirtools.careteam;

import net.fhirfactory.pegacorn.mimic.fhirtools.careteam.subcommands.careteamcsv.LoadFromCSVCLI;
import picocli.CommandLine;

@CommandLine.Command(
        name="CareTeam",
        description="CareTeam CLI",
        subcommands = {
                LoadFromCSVCLI.class
        }
)
public class CareTeamCLI implements Runnable{
    public static void main(String[] args) {
        CommandLine.run(new CareTeamCLI(), args);
    }

    @Override
    public void run() {
        System.out.println("The CareTeamCLI command");
    }
}
