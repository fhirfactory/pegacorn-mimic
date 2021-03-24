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
package net.fhirfactory.pegacorn.mimic.fhir;

import net.fhirfactory.pegacorn.mimic.fhir.resourceservices.organization.api.OrganizationCLIRPCServer;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class FHIRServerClientMimic {
    private static final Logger LOG = LoggerFactory.getLogger(FHIRServerClientMimic.class);

    private boolean initialised;
    private JChannel channel;
    private RpcDispatcher rpcDispatcher;

    @Inject
    private OrganizationCLIRPCServer organisationCLIServer;

    private String targetFHIRServerURL;

    public FHIRServerClientMimic(){
        initialised = false;
    }

    @PostConstruct
    public void initialise() {
        if(!initialised) {
            try {
                JChannel fhirClientConfigCLIServer = new JChannel().name("FHIRClientConfigRPC");
                rpcDispatcher = new RpcDispatcher(fhirClientConfigCLIServer, this);
                fhirClientConfigCLIServer.connect("ResourceCLI");
            } catch (Exception ex) {
                LOG.error(".initialise(): Error --> {}", ex.getMessage());
            }
        }
    }

    public String getTargetFHIRServerURL() {
        return targetFHIRServerURL;
    }

    public void setTargetFHIRServerURL(String targetFHIRServerURL) {
        this.targetFHIRServerURL = targetFHIRServerURL;
    }

    public void doInitialisation(){
        initialise();
    }

    public String setFHIRServerURL(String inputString) throws Exception {
        LOG.info(".setFHIRServerURL(): Entry, message --> {}", inputString);
        this.targetFHIRServerURL = inputString;
        LOG.info(".setFHIRServerURL(): FHIR Server URL set to --> {}", this.targetFHIRServerURL);
        return("OK");
    }
}
