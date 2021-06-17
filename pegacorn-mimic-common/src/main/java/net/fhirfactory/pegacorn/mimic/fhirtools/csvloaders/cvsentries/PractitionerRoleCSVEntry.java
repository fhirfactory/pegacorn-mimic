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
    @CsvBindByPosition(position=14)
    private String facsimile;
    @CsvBindByPosition(position=15)
    private String pager;
    @CsvBindByPosition(position=16)
    private String division;
    @CsvBindByPosition(position=17)
    private String branch;
    @CsvBindByPosition(position=18)
    private String section;
    @CsvBindByPosition(position=19)
    private String subSection;
    @CsvBindByPosition(position=20)
    private String businessUnit;
    
    
    public String getOrganisationUnitShortName() {
        return organisationUnitShortName.trim();
    }

    public void setOrganisationUnitShortName(String organisationUnitShortName) {
        this.organisationUnitShortName = organisationUnitShortName;
    }

    public String getLocationTag() {
        return locationTag.trim();
    }

    public void setLocationTag(String locationTag) {
        this.locationTag = locationTag;
    }

    public String getRoleCategory() {
        return roleCategory.trim();
    }

    public void setRoleCategory(String roleCategory) {
        this.roleCategory = roleCategory;
    }

    public String getRoleShortName() {
        return roleShortName.trim();
    }

    public void setRoleShortName(String roleShortName) {
        this.roleShortName = roleShortName;
    }

    public String getActiveDirectoryGroup() {
        return activeDirectoryGroup.trim();
    }

    public void setActiveDirectoryGroup(String activeDirectoryGroup) {
        this.activeDirectoryGroup = activeDirectoryGroup;
    }

    public String getPractitionerRoleShortName() {
        return practitionerRoleShortName.trim();
    }

    public void setPractitionerRoleShortName(String practitionerRoleShortName) {
        this.practitionerRoleShortName = practitionerRoleShortName;
    }

    public String getPractitionerRoleLongName() {
        return practitionerRoleLongName.trim();
    }

    public void setPractitionerRoleLongName(String practitionerRoleLongName) {
        this.practitionerRoleLongName = practitionerRoleLongName;
    }

    public String getContactExtensions() {
        if (contactExtensions != null) {
            return contactExtensions.trim();
        }
        
        return contactExtensions;
    }

    public void setContactExtensions(String contactExtensions) {
        this.contactExtensions = contactExtensions;
    }

    public String getContactMobile() {
        if (contactMobile != null) {
            return contactMobile.trim();
        }
        
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

	public String getFacsimile() {
	    if (facsimile != null) {
	        return facsimile.trim();
	    }
	    
	    return facsimile;
	}

	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
	}

	public String getPager() {
	    if (pager != null) {
	        return pager.trim();
	    }
	    
	    return pager;
	}

	public void setPager(String pager) {
		this.pager = pager;
	}

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getSubSection() {
        return subSection;
    }

    public void setSubSection(String subSection) {
        this.subSection = subSection;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }
}
