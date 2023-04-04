/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.xml;

import org.ballerinalang.jvm.XMLFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Negative test cases to cover xml parsing.
 *
 * @since 1.2.0
 */
public class XMLParserNegativeTest {

    @Test(dataProvider = "xmlValues")
    public void testXmlArg(String xmlValue, String expectedErrorMessage) {
        try {
            XMLFactory.parse(xmlValue);
            Assert.fail("Negative test failed for: `" + xmlValue + "'. Expected exception with message: " +
                    expectedErrorMessage);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), "failed to parse xml: " + expectedErrorMessage);
        }
    }

    @DataProvider(name = "xmlValues")
    public Object[][] xmlValues() {
        return new Object[][]{
                {"<book name=\"irshad<\"></book>" , "Unexpected '<'  in attribute value\n" +
                        " at [row,col {unknown-source}]: [1,19]"},
                {"<!-- comments cannot have -- in it -->" , "[com.ctc.wstx.exc.WstxLazyException] String '--' not " +
                        "allowed in comment (missing '>'?)\n at [row,col {unknown-source}]: [1,4]"},
                {"<-note>irshad</-note>", "Unexpected character '-' (code 45) in prolog, after '<'.\n" +
                        " at [row,col {unknown-source}]: [1,2]"},
                {"<note><.b>irshad</.b></note>", "Unexpected character '.' (code 46) in content after '<' " +
                        "(malformed start element?).\n at [row,col {unknown-source}]: [1,8]"},
                {"<?pi cannot contain ?> in it?><book></book>", "Unexpected character 'i' (code 105) in prolog; " +
                        "expected '<'\n at [row,col {unknown-source}]: [1,24]"}
        };
    }
}
