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
import org.ballerinalang.model.StructDef;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BallerinaStructDocGenTest {

    private String resources = "src/test/resources/balFiles/structs/";

    @Test(description = "Test a Bal file with structs")
    public void testStruct() {
        try {
            Map<String, BLangPackage> docsMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(resources + "balWithStruct.bal");
            Assert.assertNotNull(docsMap);
            Assert.assertEquals(docsMap.size(), 1);

            BLangPackage balPackage = docsMap.get(".");
            List<StructDef> structs = new ArrayList<>();
            structs.addAll(Arrays.asList(balPackage.getStructDefs()));

            Assert.assertEquals(structs.size(), 1);
            StructDef struct = (StructDef) structs.iterator().next();
            Assert.assertEquals(struct.getFieldDefStmts().length, 3);
            Assert.assertEquals(struct.getAnnotations().length, 4);
        } catch (IOException e) {
            Assert.fail();
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
