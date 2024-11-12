/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.runtime.internal.XmlFactory;
import io.ballerina.runtime.internal.values.ErrorValue;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for XML security.
 */
@Test
public class XMLSecurityTest {

    @Test
    public void testExternalEntityInjection() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [" +
                "<!ELEMENT foo ANY >" +
                "<!ENTITY xxe SYSTEM \"https://www.w3schools.com/xml/note.xml\" >]>" +
                "<foo>&xxe;</foo>";
        String expectedErrorMessage = "failed to parse xml: Encountered a reference to external entity \"xxe\", " +
                "but stream reader has feature \"javax.xml.stream.isSupportingExternalEntities\" disabled" +
                System.lineSeparator() + " at [row,col {unknown-source}]: [1,146]";
        try {
            XmlFactory.parse(xmlString);
            Assert.fail("Negative test failed for: `" + xmlString + "'. Expected exception with message: " +
                    expectedErrorMessage);
        } catch (Exception e) {
            Assert.assertEquals(e.getMessage(), expectedErrorMessage);
        }
    }

    @Test (timeOut = 10000, expectedExceptions = ErrorValue.class)
    public void testEntityExpansion() {
        String xmlString = """
                <?xml version="1.0"?>
                <!DOCTYPE lolz [
                <!ENTITY lol "lol">
                <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
                <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
                <!ENTITY lol4 "&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;">
                <!ENTITY lol5 "&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;">
                <!ENTITY lol6 "&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;">
                <!ENTITY lol7 "&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;">
                <!ENTITY lol8 "&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;">
                <!ENTITY lol9 "&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;">
                ]>
                <lolz>&lol9;</lolz>""";
        XmlFactory.parse(xmlString).toString();
    }
}
