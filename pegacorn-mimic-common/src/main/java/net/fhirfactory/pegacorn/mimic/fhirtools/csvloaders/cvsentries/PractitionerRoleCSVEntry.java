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
package net.fhirfactory.pegacorn.mimic.fhirtools.csvloaders.cvsentries;

import com.opencsv.bean.CsvBindByPosition;

public class PractitionerRoleCSVEntry {
    @CsvBindByPosition(position = 0)
    private String organisationUnitShortName;
    @CsvBindByPosition(position = 1)
    private String locationTag;
    @CsvBindByPosition(position = 2)
    private String roleCategory;
    @CsvBindByPosition(position = 3)
    private String roleShortName;
    @CsvBindByPosition(position = 4)
    private String activeDirectoryGroup;
    @CsvBindByPosition(position = 5)
    private String practitionerRoleShortName;
    @CsvBindByPosition(position = 6)
    private String practitionerRoleLongName;
    @CsvBindByPosition(position = 7)
    private String contactExtensions;
    @CsvBindByPosition(position = 8)
    private String contactMobile;

    public String getOrganisationUnitShortName() {
        return organisationUnitShortName;
    }

    public void setOrganisationUnitShortName(String organisationUnitShortName) {
        this.organisationUnitShortName = organisationUnitShortName;
    }

    public String getLocationTag() {
        return locationTag;
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }

    public String getRoleCategory() {
        return roleCategory;
    }

    public void setRoleCategory(String roleCategory) {
        this.roleCategory = roleCategory;
    }

    public String getRoleShortName() {
        return roleShortName;
    }

    public void setRoleShortName(String roleShortName) {
        this.roleShortName = roleShortName;
    }

    public String getActiveDirectoryGroup() {
        return activeDirectoryGroup;
    }

    public void setActiveDirectoryGroup(String activeDirectoryGroup) {
        this.activeDirectoryGroup = activeDirectoryGroup;
    }

    public String getPractitionerRoleShortName() {
        return practitionerRoleShortName;
    }

    public void setPractitionerRoleShortName(String practitionerRoleShortName) {
        this.practitionerRoleShortName = practitionerRoleShortName;
    }

    public String getPractitionerRoleLongName() {
        return practitionerRoleLongName;
    }

    public void setPractitionerRoleLongName(String practitionerRoleLongName) {
        this.practitionerRoleLongName = practitionerRoleLongName;
    }

    public String getContactExtensions() {
        return contactExtensions;
    }

    public void setContactExtensions(String contactExtensions) {
        this.contactExtensions = contactExtensions;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }
}
