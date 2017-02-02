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
package org.wso2.ballerina.docgen.docs;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.docgen.docs.model.BallerinaPackageDoc;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocGenTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class BallerinaConnectorDocGenTest {

    private String resources = "src/test/resources/balFiles/connectors/";

    @Test(description = "Test single action in a connector file")
    public void testConnectorWithSingleAction() {
        try {
            Map<String, BallerinaPackageDoc> docsMap = BallerinaDocGeneratorMain
                    .generatePackageDocsFromBallerina(resources + "helloWorldConnector.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            BallerinaPackageDoc doc = docsMap.get("a.b");
            List<BallerinaConnector> connectors = doc.getBallerinaConnectors();
            Assert.assertEquals(connectors.size(), 1);
            BallerinaConnector connectorDoc = connectors.get(0);
            Assert.assertEquals(connectorDoc.getParameters().length, 4);
            BallerinaAction[] actions = connectorDoc.getActions();
            BallerinaAction action = actions[0];
            Assert.assertEquals(action.getParameters().length, 2);
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
            Map<String, BallerinaPackageDoc> docsMap = BallerinaDocGeneratorMain
                    .generatePackageDocsFromBallerina(resources + "balWith2Actions.bal");

            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            BallerinaPackageDoc doc = docsMap.get("a.b");
            List<BallerinaConnector> connectors = doc.getBallerinaConnectors();
            Assert.assertEquals(connectors.size(), 1);
            BallerinaConnector connector = connectors.get(0);
            Assert.assertEquals(connector.getParameters().length, 4);

            for (BallerinaAction action : connector.getActions()) {
                Assert.assertEquals(action.getParameters().length, 2);
                Assert.assertEquals(action.getReturnParameters().length, 1);
            }
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
