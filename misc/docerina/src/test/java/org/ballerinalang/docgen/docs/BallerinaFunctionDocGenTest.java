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
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Tests for function doc generation.
 */
@Test(groups = "broken")
public class BallerinaFunctionDocGenTest {

    private String sourceRoot;

    @BeforeClass()
    public void setup() {
        sourceRoot = BallerinaFunctionDocGenTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                "balFiles/functions";
    }

    @Test(description = "Test a Bal file with one Function")
    public void testABalWithOneFunction() {
        try {
            Map<String, PackageDoc> docsMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(sourceRoot, "helloWorld.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);

            BLangPackage doc = docsMap.get(".").bLangPackage;
            Collection<BLangFunction> functions = doc.getFunctions();
            Assert.assertEquals(functions.size(), 1);

            BLangFunction function = functions.iterator().next();
            Assert.assertEquals(function.getParameters().size(), 1);
//            Assert.assertEquals(function.getReturnParameters().size(), 1);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a Bal file with multiple Functions")
    public void testABalWithMultipleFunctions() {
        try {
            Map<String, PackageDoc> docsMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(sourceRoot,
                                                                           "balWith2Functions.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);

            BLangPackage doc = docsMap.get(".").bLangPackage;
            Collection<BLangFunction> functions = doc.getFunctions();
            Assert.assertEquals(functions.size(), 2);

            Iterator<BLangFunction> iterator = functions.iterator();
            BLangFunction function = iterator.next();
            Assert.assertEquals(function.getParameters().size(), 1);
//            Assert.assertEquals(function.getReturnParameters().size(), 1);

            BLangFunction function1 = iterator.next();
            Assert.assertEquals(function1.getParameters().size(), 2);
//            Assert.assertEquals(function1.getReturnParameters().size(), 0);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    @Test(description = "Test a Bal file with a native function")
    public void testABalWithNativeFunction() {
        try {
            Map<String, PackageDoc> docsMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(sourceRoot, "natives.bal", "", true);
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);
            BallerinaDocGenTestUtils.printDocMap(docsMap);

            BLangPackage doc = docsMap.get(".").bLangPackage;
            Collection<BLangFunction> functions = doc.getFunctions();
            Assert.assertEquals(functions.size(), 1);

            BLangFunction function = functions.iterator().next();
            Assert.assertEquals(function.getParameters().size(), 1);
//            Assert.assertEquals(function.getReturnParameters().size(), 1);
            Assert.assertEquals(function.getAnnotationAttachments().size(), 2);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
