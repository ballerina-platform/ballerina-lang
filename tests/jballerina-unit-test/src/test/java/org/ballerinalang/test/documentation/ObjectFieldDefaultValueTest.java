/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.documentation;

import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.generator.model.BClass;
import org.ballerinalang.docgen.generator.model.DefaultableVariable;
import org.ballerinalang.docgen.generator.model.Module;
import org.ballerinalang.docgen.generator.model.Project;
import org.ballerinalang.docgen.model.ModuleDoc;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test cases to check default value initialization for bClasses in docs.
 */
public class ObjectFieldDefaultValueTest {
    private Module testModule;
    private List<BClass> bClasses;

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "default_value_initialization";
        CompileResult result = BCompileUtil.compile(sourceRoot, "test_module");

        List<BLangPackage> modules = new LinkedList<>();
        modules.add((BLangPackage) result.getAST());
        Map<String, ModuleDoc> docsMap = BallerinaDocGenerator.generateModuleDocs(
                Paths.get("src/test/resources", sourceRoot).toAbsolutePath().toString(), modules);
        List<ModuleDoc> moduleDocList = new ArrayList<>(docsMap.values());
        moduleDocList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

        Project project = BallerinaDocGenerator.getDocsGenModel(moduleDocList);
        testModule = project.modules.get(0);
        bClasses = testModule.classes;
    }

    @Test(description = "Test default value initialization for int")
    public void testDefValInitInt() {
        for (BClass bClass : bClasses) {
            for (DefaultableVariable variable : bClass.fields) {
                if (bClass.name.equals("Foo")) {
                    if (variable.name.equals("i")) {
                        Assert.assertEquals(variable.defaultValue, "1");
                    }
                } else if (bClass.name.equals("Bar")) {
                    if (variable.name.equals("i")) {
                        Assert.assertEquals(variable.defaultValue, "1");
                    }
                }
            }
        }
    }

    @Test(description = "Test default value initialization for string")
    public void testDefValInitString() {
        for (BClass bClass : bClasses) {
            for (DefaultableVariable variable : bClass.fields) {
                if (bClass.name.equals("Bar")) {
                    if (variable.name.equals("s")) {
                        Assert.assertEquals(variable.defaultValue, "str");
                    }
                } else if (bClass.name.equals("Student")) {
                    if (variable.name.equals("name")) {
                        Assert.assertEquals(variable.defaultValue, "John");
                    }
                }
            }
        }
    }

    @Test(description = "Test default value initialization for Objects")
    public void testDefValInitObject() {
        for (BClass bClass : bClasses) {
            for (DefaultableVariable variable : bClass.fields) {
                if (bClass.name.equals("Bar")) {
                    if (variable.name.equals("foos")) {
                        Assert.assertEquals(variable.defaultValue, "Foo");
                    }
                } else if (bClass.name.equals("Student")) {
                    if (variable.name.equals("f")) {
                        Assert.assertEquals(variable.defaultValue, "Bar");
                    }
                }
            }
        }
    }

}
