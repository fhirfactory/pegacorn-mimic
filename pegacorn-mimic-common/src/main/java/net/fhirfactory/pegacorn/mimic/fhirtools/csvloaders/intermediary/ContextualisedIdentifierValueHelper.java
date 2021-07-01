/*
 * Copyright (c) 2021 Mark A. Hunter
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fhirfactory.buildingblocks.esr.resources.datatypes.IdentifierESDT;

public class ContextualisedIdentifierValueHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ContextualisedIdentifierValueHelper.class);

    public String buildComprehensiveIdentifierValue(String parentIdentifierValue, String contextualisedValue){
        LOG.debug(".buildComprehensiveIdentifierValue(): Entry");
        boolean noContext = true;
        if(parentIdentifierValue != null){
            if(!parentIdentifierValue.isEmpty()) {
                noContext = false;
            }
        }
        String comprehensiveValue = null;
        if(!noContext){
            comprehensiveValue = parentIdentifierValue + IdentifierESDT.getContextualisedValueSeparator() + contextualisedValue;
        } else {
            comprehensiveValue = contextualisedValue;
        }
        LOG.debug(".buildComprehensiveIdentifierValue(): Exit");
        return(comprehensiveValue);
    }

    public String extractContextualValue(String comprehensiveValue){
        if(comprehensiveValue == null){
            return(null);
        }
        if(comprehensiveValue.isEmpty()){
            return(null);
        }
        String contextualValue = null;
        if(comprehensiveValue.contains(IdentifierESDT.getContextualisedValueSeparator())){
            String[] splitString = comprehensiveValue.split(IdentifierESDT.getContextualisedValueSeparator());
            contextualValue = splitString[splitString.length-1];
        } else {
            contextualValue = comprehensiveValue;
        }
        return(contextualValue);
    }
}
