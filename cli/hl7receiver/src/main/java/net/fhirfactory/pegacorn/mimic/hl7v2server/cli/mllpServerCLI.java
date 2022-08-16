/*
 * Copyright (c) 2022 Mark Hunter
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
package net.fhirfactory.pegacorn.mimic.hl7v2server.cli;


import net.fhirfactory.pegacorn.mimic.hl7v2server.cli.subcommands.HL7v2QueryServer;
import picocli.CommandLine;

@CommandLine.Command(
        name="mllpServer",
        description="MLLP Server Command Line Interface",
        subcommands = {
                HL7v2QueryServer.class
        }
)
public class mllpServerCLI implements Runnable{

        public static void main(String[] args) {
                CommandLine.run(new mllpServerCLI(), args);
        }

        @Override
        public void run() {
                System.out.println("MLLP Receive Message Service");
        }
}
