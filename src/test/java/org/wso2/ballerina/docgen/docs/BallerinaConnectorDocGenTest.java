package org.wso2.ballerina.docgen.docs;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.docgen.docs.model.BallerinaConnectorDoc;
import org.wso2.ballerina.docgen.docs.model.BallerinaPackageDoc;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocGenTestUtils;

import java.util.List;
import java.util.Map;

public class BallerinaConnectorDocGenTest {

    private String resources = "src/test/resources/balFiles/connectors/";

    @BeforeClass()
    public void setup() {
    }

    @Test(description = "Test single action in a connector file")
    public void testConnectorWithSingleAction() {
        try {
            Map<String, BallerinaPackageDoc> docsMap = BallerinaDocGeneratorMain
                    .generatePackageDocsFromBallerina(resources + "helloWorldConnector.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            BallerinaPackageDoc doc = docsMap.get("a.b");
            List<BallerinaConnectorDoc> connectorDocs = doc.getBallerinaConnectorDocs();
            Assert.assertEquals(connectorDocs.size(), 1);
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
