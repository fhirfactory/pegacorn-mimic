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

public class CareTeamCSVEntry {
	@CsvBindByPosition(position = 0)
	private String roleName;
	@CsvBindByPosition(position = 1)
	private String codeYellowResponder;
	@CsvBindByPosition(position = 2)
	private String codeYellowNotifier;
	@CsvBindByPosition(position = 3)
	private String codeOrangeResponder;
	@CsvBindByPosition(position = 4)
	private String codeOrangeNotifier;
	@CsvBindByPosition(position = 5)
	private String codeBrownResponder;
	@CsvBindByPosition(position = 6)
	private String codeBrownNotifier;
	@CsvBindByPosition(position = 7)
	private String codePurpleResponder;
	@CsvBindByPosition(position = 8)
	private String codePurpleNotifier;
	@CsvBindByPosition(position = 9)
	private String codeBlackResponder;
	@CsvBindByPosition(position = 10)
	private String codeBlackNotifier;

	
	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getCodeYellowResponder() {
		return codeYellowResponder;
	}

	public void setCodeYellowResponder(String codeYellowResponder) {
		this.codeYellowResponder = codeYellowResponder;
	}

	public String getCodeYellowNotifier() {
		return codeYellowNotifier;
	}

	public void setCodeYellowNotifier(String codeYellowNotifier) {
		this.codeYellowNotifier = codeYellowNotifier;
	}

	public String getCodeOrangeResponder() {
		return codeOrangeResponder;
	}

	public void setCodeOrangeResponder(String codeOrangeResponder) {
		this.codeOrangeResponder = codeOrangeResponder;
	}

	public String getCodeOrangeNotifier() {
		return codeOrangeNotifier;
	}

	public void setCodeOrangeNotifier(String codeOrangeNotifier) {
		this.codeOrangeNotifier = codeOrangeNotifier;
	}

	public String getCodeBrownResponder() {
		return codeBrownResponder;
	}

	public void setCodeBrownResponder(String codeBrownResponder) {
		this.codeBrownResponder = codeBrownResponder;
	}

	public String getCodeBrownNotifier() {
		return codeBrownNotifier;
	}

	public void setCodeBrownNotifier(String codeBrownNotifier) {
		this.codeBrownNotifier = codeBrownNotifier;
	}

	public String getCodePurpleResponder() {
		return codePurpleResponder;
	}

	public void setCodePurpleResponder(String codePurpleResponder) {
		this.codePurpleResponder = codePurpleResponder;
	}

	public String getCodePurpleNotifier() {
		return codePurpleNotifier;
	}

	public void setCodePurpleNotifier(String codePurpleNotifier) {
		this.codePurpleNotifier = codePurpleNotifier;
	}

	public String getCodeBlackResponder() {
		return codeBlackResponder;
	}

	public void setCodeBlackResponder(String codeBlackResponder) {
		this.codeBlackResponder = codeBlackResponder;
	}

	public String getCodeBlackNotifier() {
		return codeBlackNotifier;
	}

	public void setCodeBlackNotifier(String codeBlackNotifier) {
		this.codeBlackNotifier = codeBlackNotifier;
	}
}
