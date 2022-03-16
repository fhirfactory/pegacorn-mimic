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
package net.fhirfactory.pegacorn.mimic.fhirtools.practitioner.subcommands.create;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.fhirfactory.pegacorn.core.model.dates.EffectivePeriod;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.PractitionerESR;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.datatypes.ContactPointESDT;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.datatypes.HumanNameESDT;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.valuesets.ContactPointESDTTypeEnum;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.valuesets.ContactPointESDTUseEnum;
import net.fhirfactory.pegacorn.core.model.ui.resources.simple.valuesets.HumanNameESDTUseEnum;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@CommandLine.Command(
        name="create",
        description="Creates a Practitioner (and associated) Resources"
)
public class createPractitioner implements Runnable{
    private static final Logger LOG = LoggerFactory.getLogger(createPractitioner.class);
    private Address actualAddress;
    private JChannel rpcClient;
    private RpcDispatcher rpcDispatcher;

    @CommandLine.Option(names = {"-n", "--name"})
    private String practitionerName;

    @CommandLine.Option(names = {"-e", "--email"})
    private String practitionerEmail;

    @CommandLine.Option(names = {"-x", "--extension"})
    private String extensionNumber;

    @CommandLine.Option(names = {"-m", "--mobile"})
    private String mobileNumber;

    @Override
    public void run() {
        LOG.info(".run(): Entry, Practitioner Name: {}, Practitioner Email: {}, Practitioner Extension: {}, Practitioner Mobile: {}",
                practitionerName, practitionerEmail, extensionNumber, mobileNumber);
        doCreate();
        System.exit(0);
    }

    public void doCreate(){
        LOG.info(".doCreate(): Entry");
        PractitionerESR practitioner = new PractitionerESR();
        practitioner.setEmailAddress(getPractitionerEmail());
        practitioner.setDisplayName(getPractitionerName());
        // Name
        HumanNameESDT practitionerName = new HumanNameESDT();
        practitionerName.setDisplayName(getPractitionerName());
        String[] nameSplit = getPractitionerName().split(" ");
        if(nameSplit.length == 2){
            practitionerName.setPreferredGivenName(nameSplit[0]);
            practitionerName.setFamilyName(nameSplit[1]);
            practitionerName.setNameUse(HumanNameESDTUseEnum.OFFICIAL);
        }
        EffectivePeriod period = new EffectivePeriod();
        period.setEffectiveStartDate(Date.from(Instant.now()));
        practitionerName.setPeriod(period);
        practitioner.setOfficialName(practitionerName);
        // Extension
        ContactPointESDT primaryPhone = new ContactPointESDT();
        primaryPhone.setName("Extension");
        primaryPhone.setValue(extensionNumber);
        primaryPhone.setType(ContactPointESDTTypeEnum.PABX_EXTENSION);
        primaryPhone.setUse(ContactPointESDTUseEnum.WORK);
        primaryPhone.setRank(1);
        practitioner.getContactPoints().add(primaryPhone);
        // Mobile
        ContactPointESDT mobilePhone = new ContactPointESDT();
        mobilePhone.setName("Mobile");
        mobilePhone.setValue(mobileNumber);
        mobilePhone.setType(ContactPointESDTTypeEnum.MOBILE);
        primaryPhone.setUse(ContactPointESDTUseEnum.WORK);
        primaryPhone.setRank(2);
        practitioner.getContactPoints().add(mobilePhone);
        String practitionerAsString = entryAsJSONObject(practitioner);
        String result;
        if(practitionerAsString != null){
            result = performQuery(practitionerAsString);
        } else {
            result = null;
        }
        LOG.info(".doCreate(): Result --> {}", result);
        LOG.debug(".doCreate(): Exit");
    }

    public String performQuery(String practitioner){
        LOG.debug(".performQuery(): Entry");
        initialiseJGroupsChannel();
        RequestOptions requestOptions = new RequestOptions(ResponseMode.GET_FIRST, 5000);
        Class classes[] = new Class[1];
        classes[0] = String.class;
        String objectSet[] = new String[1];
        objectSet[0] = practitioner;
        try {
            LOG.info(".performQuery(): Sending request --> {}", practitioner);
            String response = rpcDispatcher.callRemoteMethod(this.actualAddress, "createPractitioner", objectSet, classes, requestOptions);
            LOG.info(".performQuery(): Response --> {}", response);
            return(response);
        } catch(Exception ex){
            ex.printStackTrace();
            LOG.error(".performQuery(): Error --> {}", ex.toString());
        }
        LOG.debug(".performQuery(): Exit");
        return("Error... ");
    }


    void initialiseJGroupsChannel(){
        try {
            LOG.info(".initialiseJGroupsChannel(): Entry");
            this.rpcClient = new JChannel("udp.xml").name("PractitionerRCPClient");
            this.rpcClient.connect("ResourceCLI");
            View view = this.rpcClient.view();
            List<Address> members = view.getMembers();
            for(Address member: members) {
                if ("PractitionerRPC".contentEquals(member.toString())) {
                    LOG.info(".initialiseJGroupsChannel(): Found Server Endpoint");
                    this.actualAddress = member;
                    break;
                }
            }
            this.rpcDispatcher = new RpcDispatcher(this.rpcClient,null);
        } catch(Exception ex){
            LOG.error(".initialiseJGroupsChannel(): Error --> " + ex.toString());
        }
    }

    private String entryAsJSONObject(PractitionerESR practitioner){
        try{
            ObjectMapper jsonMapper = new ObjectMapper();
            String orgAsString = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(practitioner);
            return(orgAsString);
        } catch(Exception ex){
            ex.printStackTrace();
        }
        return(null);
    }

    public static Logger getLOG() {
        return LOG;
    }

    public Address getActualAddress() {
        return actualAddress;
    }

    public void setActualAddress(Address actualAddress) {
        this.actualAddress = actualAddress;
    }

    public JChannel getRpcClient() {
        return rpcClient;
    }

    public void setRpcClient(JChannel rpcClient) {
        this.rpcClient = rpcClient;
    }

    public RpcDispatcher getRpcDispatcher() {
        return rpcDispatcher;
    }

    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) {
        this.rpcDispatcher = rpcDispatcher;
    }

    public String getPractitionerName() {
        return practitionerName;
    }

    public void setPractitionerName(String practitionerName) {
        this.practitionerName = practitionerName;
    }

    public String getPractitionerEmail() {
        return practitionerEmail;
    }

    public void setPractitionerEmail(String practitionerEmail) {
        this.practitionerEmail = practitionerEmail;
    }

    public String getExtensionNumber() {
        return extensionNumber;
    }

    public void setExtensionNumber(String extensionNumber) {
        this.extensionNumber = extensionNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
}