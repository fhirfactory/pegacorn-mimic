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

public class HealthcareServiceCSVEntry {
    @CsvBindByPosition(position = 0)
    private String healthCareServiceShortName;

    @CsvBindByPosition(position = 1)
    private String healthCareServiceLongName;
    
    @CsvBindByPosition(position = 2)
    private String organisationalUnit;
    
    @CsvBindByPosition(position = 3)
    private String telephoneNumber;

    public String getHealthCareServiceLongName() {
        return healthCareServiceLongName;
    }

    public void setHealthCareServiceLongName(String healthCareServiceLongName) {
        this.healthCareServiceLongName = healthCareServiceLongName;
    }

    public String getHealthCareServiceShortName() {
        return healthCareServiceShortName;
    }

    public void setHealthCareServiceShortName(String healthCareServiceShortName) {
        this.healthCareServiceShortName = healthCareServiceShortName;
    }

    public String getOrganisationalUnit() {
        return organisationalUnit;
    }

    public void setOrganisationalUnit(String organisationalUnit) {
        this.organisationalUnit = organisationalUnit;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
}
