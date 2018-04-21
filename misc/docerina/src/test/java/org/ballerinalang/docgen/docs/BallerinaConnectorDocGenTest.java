/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.docgen.docs;

import org.ballerinalang.docgen.docs.utils.BallerinaDocGenTestUtils;
import org.ballerinalang.docgen.model.PackageDoc;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tests for connector doc generation.
 */
@Test(groups = "broken")
public class BallerinaConnectorDocGenTest {

    private String sourceRoot;

    @BeforeClass
    public void setup() {
        sourceRoot = BallerinaFunctionDocGenTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                                                                                            "balFiles/connectors";
    }

    @Test(description = "Test single action in a connector file", enabled = false)
    public void testConnectorWithSingleAction() {
        try {
            Map<String, PackageDoc> docsMap = BallerinaDocGenerator
                    .generatePackageDocsFromBallerina(sourceRoot, "helloWorldConnector.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            BLangPackage balPackage = docsMap.get(".").bLangPackage;

            List<BLangConnector> connectors = balPackage.getConnectors();

            Assert.assertEquals(connectors.size(), 1);
            BLangConnector connectorDoc = (BLangConnector) connectors.iterator().next();
            Assert.assertEquals(connectorDoc.getParameters().size(), 4);
            List<BLangAction> actions = connectorDoc.getActions();
            BLangAction action = actions.get(0);
            Assert.assertEquals(action.getParameters().size(), 1);
//            Assert.assertEquals(action.getReturnParameters().size(), 1);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test single action in a connector file", enabled = false)
    public void testConnectorWithMultipleAction() {
        try {
            Map<String, PackageDoc> docsMap = BallerinaDocGenerator
                    .generatePackageDocsFromBallerina(sourceRoot, "balWith2Actions.bal");

            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);

            BLangPackage balPackage = docsMap.get(".").bLangPackage;

            List<BLangConnector> connectors = balPackage.getConnectors();

            Assert.assertEquals(connectors.size(), 1);
            BLangConnector connector = connectors.iterator().next();
            Assert.assertEquals(connector.getParameters().size(), 4);

            for (BLangAction action : connector.getActions()) {
                Assert.assertEquals(action.getParameters().size(), 1);
//                Assert.assertEquals(action.getReturnParameters().size(), 1);
            }
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
