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
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BallerinaConnectorDocGenTest {

    private String resources = "src/test/resources/balFiles/connectors/";

    @Test(description = "Test single action in a connector file")
    public void testConnectorWithSingleAction() {
        try {
            Map<String, BLangPackage> docsMap = BallerinaDocGenerator
                    .generatePackageDocsFromBallerina(resources + "helloWorldConnector.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            BLangPackage balPackage = docsMap.get(".");

            List<BallerinaConnectorDef> connectors = new ArrayList<>();
            connectors.addAll(Arrays.asList(balPackage.getConnectors()));
            
            Assert.assertEquals(connectors.size(), 1);
            BallerinaConnectorDef connectorDoc = (BallerinaConnectorDef) connectors.iterator().next();
            Assert.assertEquals(connectorDoc.getParameterDefs().length, 4);
            BallerinaAction[] actions = connectorDoc.getActions();
            BallerinaAction action = actions[0];
            Assert.assertEquals(action.getParameterDefs().length, 1);
            Assert.assertEquals(action.getReturnParameters().length, 1);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test single action in a connector file")
    public void testConnectorWithMultipleAction() {
        try {
            Map<String, BLangPackage> docsMap = BallerinaDocGenerator
                    .generatePackageDocsFromBallerina(resources + "balWith2Actions.bal");

            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);

            BLangPackage balPackage = docsMap.get(".");

            List<BallerinaConnectorDef> connectors = new ArrayList<>();
            connectors.addAll(Arrays.asList(balPackage.getConnectors()));

            Assert.assertEquals(connectors.size(), 1);
            BallerinaConnectorDef connector = (BallerinaConnectorDef) connectors.iterator().next();
            Assert.assertEquals(connector.getParameterDefs().length, 4);

            for (BallerinaAction action : connector.getActions()) {
                Assert.assertEquals(action.getParameterDefs().length, 1);
                Assert.assertEquals(action.getReturnParameters().length, 1);
            }
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
