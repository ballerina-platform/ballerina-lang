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
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Tests for struct doc generation.
 */
@Test(groups = "broken")
public class BallerinaStructDocGenTest {

    private String sourceRoot;

    @BeforeClass()
    public void setup() {
        sourceRoot = BallerinaFunctionDocGenTest.class.getProtectionDomain().getCodeSource().getLocation().getPath() +
                "balFiles/structs";
    }

    @Test(description = "Test a Bal file with structs", enabled = false)
    public void testStruct() {
        try {
            Map<String, PackageDoc> docsMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(sourceRoot, "balWithStruct.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);

            BLangPackage balPackage = docsMap.get(".").bLangPackage;
            List<BLangStruct> structs = balPackage.getStructs();

            Assert.assertEquals(structs.size(), 1);
            BLangStruct struct = (BLangStruct) structs.iterator().next();
            Assert.assertEquals(struct.getFields().size(), 3);
            Assert.assertEquals(struct.getAnnotationAttachments().size(), 4);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
