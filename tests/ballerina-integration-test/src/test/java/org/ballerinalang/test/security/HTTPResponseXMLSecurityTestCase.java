/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.test.security;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.BaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing actual runtime behaviour of the XML parser from security point of view.
 */
public class HTTPResponseXMLSecurityTestCase extends BaseTest {

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        String balFilePath = new File("src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "xmlSecurity" + File.separator +
                "xml-parsing-service.bal").getAbsolutePath();
        serverInstance.startBallerinaServer(balFilePath, 9090);
    }

    @Test(description = "Test the service for XML External Entity Injection attack")
    public void testXMLExternalEntityInjection() throws Exception {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<!DOCTYPE foo [" +
                "<!ELEMENT foo ANY >" +
                "<!ENTITY xxe SYSTEM \"https://www.w3schools.com/xml/note.xml\" >]>" +
                "<foo>&xxe;</foo>";
        HttpResponse response = sendXmlPayload(xmlString);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        String respMsg = "";
        Assert.assertEquals(response.getData(), respMsg, "Message content mismatched");
    }

    @Test(description = "Test the service for XML Entity Expansion attack", timeOut = 10000)
    public void testXMLEntityExpansion() throws Exception {
        String xmlString = "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE lolz [\n" +
                "<!ENTITY lol \"lol\">\n" +
                "<!ENTITY lol2 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n" +
                "<!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n" +
                "<!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n" +
                "<!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n" +
                "<!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n" +
                "<!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n" +
                "<!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n" +
                "<!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n" +
                "]>\n" +
                "<lolz>&lol9;</lolz>";
        HttpResponse response = sendXmlPayload(xmlString);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 500, "Response code mismatched");
        //TODO: Introduce validation of error message once GitHub issue #4533 is fixed.
    }

    private HttpResponse sendXmlPayload(String requestMsg) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaderNames.CONTENT_TYPE.toString(), TestConstant.CONTENT_TYPE_XML);
        String serviceUrl = "http://localhost:9090/xmlparser";
        HttpResponse response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
        if (response == null) {
            //Retrying to avoid intermittent test failure
            response = HttpClientRequest.doPost(serviceUrl, requestMsg, headers);
        }
        return response;
    }

    @AfterClass(alwaysRun = true)
    private void cleanup() throws Exception {
        serverInstance.stopServer();
    }
}
