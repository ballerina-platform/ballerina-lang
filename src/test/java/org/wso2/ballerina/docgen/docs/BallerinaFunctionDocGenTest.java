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
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.Package;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocGenTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class BallerinaFunctionDocGenTest {

    private String resources = "src/test/resources/balFiles/functions/";

    @BeforeClass()
    public void setup() {
    }

    @Test(description = "Test a Bal file with one Function")
    public void testABalWithOneFunction() {
        try {
            Map<String, Package> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(resources + "helloWorld.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            
            Package doc = docsMap.get("a.b");
            Collection<Function> functions = doc.getPrivateFunctions().values();
            Assert.assertEquals(functions.size(), 1);

            BallerinaFunction function = (BallerinaFunction) functions.iterator().next();
            Assert.assertEquals(function.getParameterDefs().length, 1);
            Assert.assertEquals(function.getReturnParameters().length, 1);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a Bal file with multiple Functions")
    public void testABalWithMultipleFunctions() {
        try {
            Map<String, Package> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(resources + "balWith2Functions.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);
            
            Package doc = docsMap.get("a.b");
            Collection<Function> functions = Arrays.asList(doc.getFiles().get(0).getFunctions());
            Assert.assertEquals(functions.size(), 2);

            Iterator iterator = functions.iterator();
            BallerinaFunction function = (BallerinaFunction) iterator.next();
            Assert.assertEquals(function.getParameterDefs().length, 1);
            Assert.assertEquals(function.getReturnParameters().length, 1);

            BallerinaFunction function1 = (BallerinaFunction) iterator.next();
            Assert.assertEquals(function1.getParameterDefs().length, 2);
            Assert.assertEquals(function1.getReturnParameters().length, 0);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a Bal file with functions that has same method signature and different arguments")
    public void testBalWithSameFuncSignature() {
        try {
            Map<String, Package> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(
                            resources + "balFuncWithSameSignature.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);

            Package doc = docsMap.get("a.b");
            Collection<Function> functions = Arrays.asList(doc.getFiles().get(0).getFunctions());
            Assert.assertEquals(functions.size(), 2);

            Iterator iterator = functions.iterator();
            BallerinaFunction function = (BallerinaFunction) iterator.next();
            Assert.assertEquals(function.getParameterDefs().length, 2);
            Assert.assertEquals(function.getReturnParameters().length, 1);

            BallerinaFunction function1 = (BallerinaFunction) iterator.next();
            Assert.assertEquals(function1.getParameterDefs().length, 4);
            Assert.assertEquals(function1.getReturnParameters().length, 2);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
    
    // @Test(description = "Test a bal file with native functions")
    // public void testABalWithNativeFunctions() {
    //     try {
    //         Map<String, Package> docsMap =
    //            BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(resources + "balWithNativeFunctions.bal");
    //         Assert.assertNotNull(docsMap);
    //         Assert.assertEquals(docsMap.size(), 1);
    //         BallerinaDocGenTestUtils.printDocMap(docsMap);
    //     } finally {
    //         BallerinaDocGenTestUtils.cleanUp();
    //     }
    // }
}
