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
package net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.intermediary;

import java.util.ArrayList;

public class TrivialOrganization {
    private ArrayList<String> containedOrgs;
    private String organizationShortName;
    private String organizationLongName;
    private String organizationTypeCode;
    private String organizationTypeName;
    private String parentOrganizationShortName;

    public TrivialOrganization(){
        containedOrgs = new ArrayList<>();
    }

    public ArrayList<String> getContainedOrgs() {
        return containedOrgs;
    }

    public void setContainedOrgs(ArrayList<String> containedOrgs) {
        this.containedOrgs = containedOrgs;
    }

    public String getOrganizationShortName() {
        return organizationShortName;
    }

    public void setOrganizationShortName(String organizationShortName) {
        this.organizationShortName = organizationShortName;
    }

    public String getOrganizationLongName() {
        return organizationLongName;
    }

    public void setOrganizationLongName(String organizationLongName) {
        this.organizationLongName = organizationLongName;
    }

    public String getOrganizationTypeCode() {
        return organizationTypeCode;
    }

    public void setOrganizationTypeCode(String organizationTypeCode) {
        this.organizationTypeCode = organizationTypeCode;
    }

    public String getOrganizationTypeName() {
        return organizationTypeName;
    }

    public void setOrganizationTypeName(String organizationTypeName) {
        this.organizationTypeName = organizationTypeName;
    }

    public String getParentOrganizationShortName() {
        return parentOrganizationShortName;
    }

    public void setParentOrganizationShortName(String parentOrganizationShortName) {
        this.parentOrganizationShortName = parentOrganizationShortName;
    }
}
