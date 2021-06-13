/*
 * Copyright (c) 2021 Mark Hunter
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.fhirfactory.pegacorn.mimic.fhirtools.fhirserverclient;

import java.util.List;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.pegacorn.mimic.fhirtools.careteam.CareTeamCLI;
import net.fhirfactory.pegacorn.mimic.fhirtools.healthcareservice.HealthcareServiceCLI;
import net.fhirfactory.pegacorn.mimic.fhirtools.location.LocationCLI;
import net.fhirfactory.pegacorn.mimic.fhirtools.organization.OrganizationCLI;
import net.fhirfactory.pegacorn.mimic.fhirtools.practitioner.PractitionerCLI;
import net.fhirfactory.pegacorn.mimic.fhirtools.practitionerrole.PractitionerRoleCLI;
import picocli.CommandLine;

@CommandLine.Command(
        name="FHIRClientCLI",
        description="FHIR Server Client CLI",
        subcommands = {
            OrganizationCLI.class, PractitionerRoleCLI.class, PractitionerCLI.class, LocationCLI.class, CareTeamCLI.class, HealthcareServiceCLI.class
        }
)
public class FHIRServerClientCLI implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(FHIRServerClientCLI.class);

    @CommandLine.Option(names = {"-s", "--server"})
    private String targetURL;

    private Address actualAddress;
    private JChannel mimicFHIRServerClient;
    private RpcDispatcher rpcDispatcher;

    public static void main(String[] args) {
        CommandLine.run(new FHIRServerClientCLI(), args);
    }

    @Override
    public void run() {
        System.out.println("The FHIR-Server Client Configuration command");
        doCommandSetFHIRServerURL();
        System.exit(0);
    }

    public void doCommandSetFHIRServerURL(){
        LOG.info(".doCommandSetFHIRServerURL(): Entry");
        Object objectSet[] = new Object[1];
        Class classSet[] = new Class[1];
        objectSet[0] = targetURL;
        classSet[0] = String.class;
        initialiseJGroupsChannel();
        RequestOptions requestOptions = new RequestOptions(ResponseMode.GET_FIRST, 5000);
        try {
            LOG.info(".doCommandSetFHIRServerURL(): Sending request --> {}", this.targetURL);
            String response = rpcDispatcher.callRemoteMethod(this.actualAddress, "setFHIRServerURL", objectSet, classSet, requestOptions);
            LOG.info(".doCommandSetFHIRServerURL(): Response --> {}", response);
        } catch(Exception ex){
            ex.printStackTrace();
            LOG.error(".doCommandSetFHIRServerURL(): Error --> {}", ex.toString());
        }
        LOG.info(".doCommandSetFHIRServerURL(): Exit");
    }

    void initialiseJGroupsChannel(){
        try {
            LOG.info(".initialiseJGroupsChannel(): Entry");
            this.mimicFHIRServerClient = new JChannel("udp.xml").name("FHIRServerClientRCPClient");
            this.mimicFHIRServerClient.connect("ResourceCLI");
            View view = this.mimicFHIRServerClient.view();
            List<Address> members = view.getMembers();
            for(Address member: members) {
                if ("FHIRClientConfigRPC".contentEquals(member.toString())) {
                    LOG.info(".initialiseJGroupsChannel(): Found Server Endpoint");
                    this.actualAddress = member;
                    break;
                }
            }
            this.rpcDispatcher = new RpcDispatcher(this.mimicFHIRServerClient,null);
        } catch(Exception ex){
            LOG.error(".initialiseJGroupsChannel(): Error --> " + ex.toString());
        }
    }
}
