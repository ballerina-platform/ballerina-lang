/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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
package io.ballerina.shell.service.test.getresult;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * getResult End point tests for xml.
 *
 * @since 2201.1.1
 */
public class XmlTests extends AbstractGetResultTest {
    @Test(description = "Test for xml")
    public void testXml() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("xml.basic.json");
    }

    @Test(description = "Test for xml attribute getters and setters")
    public void testXmlAttributes() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("xml.attributes.json");
    }

    @Test(description = "Test for xml namespaces")
    public void testXmlNamespaces() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("xml.namespaces.json");
    }
}
