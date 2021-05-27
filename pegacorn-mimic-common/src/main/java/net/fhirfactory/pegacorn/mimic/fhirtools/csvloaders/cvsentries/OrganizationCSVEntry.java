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

public class OrganizationCSVEntry {
    @CsvBindByPosition(position = 0)
    private String organisationParentShortName;
    @CsvBindByPosition(position = 1)
    private String organisationShortName;
    @CsvBindByPosition(position = 2)
    private String organisationLongName;
    @CsvBindByPosition(position = 3)
    private String organizationTypeShortName;
    @CsvBindByPosition(position = 4)
    private String organizationTypeLongName;
    @CsvBindByPosition(position = 5)
    private String emailAddress;;
    @CsvBindByPosition(position = 6)
    private String telephoneNumber;
    @CsvBindByPosition(position = 7)
    private String facsimile;

    public String getOrganisationParentShortName() {
        return organisationParentShortName;
    }

    public void setOrganisationParentShortName(String organisationParentShortName) {
        this.organisationParentShortName = organisationParentShortName;
    }

    public String getOrganisationShortName() {
        return organisationShortName;
    }

    public void setOrganisationShortName(String organisationShortName) {
        this.organisationShortName = organisationShortName;
    }

    public String getOrganisationLongName() {
        return organisationLongName;
    }

    public void setOrganisationLongName(String organisationLongName) {
        this.organisationLongName = organisationLongName;
    }

    public String getOrganizationTypeShortName() {
        return organizationTypeShortName;
    }

    public void setOrganizationTypeShortName(String organizationTypeShortName) {
        this.organizationTypeShortName = organizationTypeShortName;
    }

    public String getOrganizationTypeLongName() {
        return organizationTypeLongName;
    }

    public void setOrganizationTypeLongName(String organizationTypeLongName) {
        this.organizationTypeLongName = organizationTypeLongName;
    }

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getFacsimile() {
		return facsimile;
	}

	public void setFacsimile(String facsimile) {
		this.facsimile = facsimile;
	}
}
