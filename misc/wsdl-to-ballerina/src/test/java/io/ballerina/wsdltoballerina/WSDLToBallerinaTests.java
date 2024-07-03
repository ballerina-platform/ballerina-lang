/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.wsdltoballerina;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WSDLToBallerinaTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String XML_DIR = "xml";
    private static final String BAL_DIR = "ballerina";

    private final Path sample0XML = RES_DIR.resolve(XML_DIR)
            .resolve("sample_4.xml");
    private final Path sample0Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_0.bal");

    @Test(description = "testBasicXML")
    public void testBasicXML() throws IOException {
        String xmlFileContent = Files.readString(sample0XML);
        SOAPClientResponse generatedCodeBlock = (new WSDLToBallerina()).generateFromWSDL(xmlFileContent);
        System.out.println(generatedCodeBlock.getTypesCodeBlock());
        String expectedCodeBlock = "Text";
        Assert.assertEquals("Text", expectedCodeBlock);
    }
}
