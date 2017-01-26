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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.docgen.docs.model.BallerinaFunctionDoc;
import org.wso2.ballerina.docgen.docs.model.BallerinaPackageDoc;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocGenTestUtils;

import java.util.List;
import java.util.Map;

public class BallerinaFunctionDocGenTest {

    private String resources = "src/test/resources/balFiles/functions/";

    @BeforeClass()
    public void setup() {
    }

    @Test(description = "Test a Bal file with one Function")
    public void testABalWithOneFunction() {
        try {
            Map<String, BallerinaPackageDoc> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(resources + "helloWorld.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            
            BallerinaPackageDoc doc = docsMap.get("a.b");
            List<BallerinaFunctionDoc> funcDocs = doc.getBallerinaFunctionDocs();
            Assert.assertEquals(funcDocs.size(), 1);
            BallerinaFunctionDoc funcDoc = funcDocs.get(0);
            Assert.assertEquals(funcDoc.getParameters().size(), 1);
            Assert.assertEquals(funcDoc.getReturnParams().size(), 1);
            Assert.assertEquals(funcDoc.getThrownExceptions().size(), 0);
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a Bal file with multiple Functions")
    public void testABalWithMultipleFunctions() {
        try {
            Map<String, BallerinaPackageDoc> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(resources + "balWith2Functions.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            
            BallerinaPackageDoc doc = docsMap.get("a.b");
            List<BallerinaFunctionDoc> funcDocs = doc.getBallerinaFunctionDocs();
            Assert.assertEquals(funcDocs.size(), 2);
            BallerinaFunctionDoc funcDoc = funcDocs.get(0);
            Assert.assertEquals(funcDoc.getParameters().size(), 1);
            Assert.assertEquals(funcDoc.getReturnParams().size(), 1);
            Assert.assertEquals(funcDoc.getThrownExceptions().size(), 0);

            BallerinaFunctionDoc funcDoc1 = funcDocs.get(1);
            Assert.assertEquals(funcDoc1.getParameters().size(), 2);
            Assert.assertEquals(funcDoc1.getReturnParams().size(), 0);
            Assert.assertEquals(funcDoc1.getThrownExceptions().size(), 0);
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
    
    // @Test(description = "Test a bal file with native functions")
    // public void testABalWithNativeFunctions() {
    //     try {
    //         Map<String, BallerinaPackageDoc> docsMap =
    //            BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(resources + "balWithNativeFunctions.bal");
    //         Assert.assertNotNull(docsMap);
    //         Assert.assertEquals(docsMap.size(), 1);
    //         BallerinaDocGenTestUtils.printDocMap(docsMap);
    //     } finally {
    //         BallerinaDocGenTestUtils.cleanUp();
    //     }
    // }
}
