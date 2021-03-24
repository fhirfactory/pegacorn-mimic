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
package net.fhirfactory.pegacorn.mimic.fhirtools.commands.model;

public enum MimicFHIRSubCommandEnum {
    SUBCOMMAND_CREATE("action_create"),
    SUBCOMMAND_DELETE("action_delete"),
    SUBCOMMAND_REVIEW("action_review"),
    SUBCOMMAND_UPDATE("action_update"),
    SUBCOMMAND_CREATE_FROM_CSV_ENTRY("action_create_from_csv_entry"),
    SUBCOMMAND_CONFIG_SET("action_config_set"),
    SUBCOMMAND_CONFIG_UNSET("action_config_unset");

    private String subCommandValue;

    private MimicFHIRSubCommandEnum(String value){
        this.subCommandValue = value;
    }

    public String getSubCommandValue(){
        return(this.subCommandValue);
    }
}
