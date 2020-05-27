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
import org.ballerinalang.docgen.generator.model.Function;
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
 * Test multi-line documentation when deprecation is present or not.
 */
public class MultilineDocsTest {
    private Function multilineDocsFunction;
    private Function multilineDocsDeprecatedFunction;

    private final String firstLine = "<p>Returns a formatted string using the specified format string and arguments. "
            + "Following format specifiers are allowed.</p>\n";
    private final String deprecatedFirstLine = "Returns a formatted string using the specified format string and "
            + "arguments. Following format specifiers are allowed.\n";

    private final String otherLines = "<p>b - boolean</p>\n"
            + "<p>B - boolean (ALL_CAPS)</p>\n"
            + "<p>d - int</p>\n"
            + "<p>f - float</p>\n"
            + "<p>x - hex</p>\n"
            + "<p>X - HEX (ALL_CAPS)</p>\n"
            + "<p>s - string (This specifier is applicable for any of the supported types in Ballerina.\n"
            + "These values will be converted to their string representation.)</p>\n"
            + "<pre><code class=\"language-ballerina\"> "
            + "string s8 = io:sprintf(&quot;%s scored %d for %s and has an average of %.2f.&quot;, name, marks, "
            + "subjects[0], average);\n"
            + "</code></pre>\n";

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "multi_line_docs_project";
        CompileResult result = BCompileUtil.compile(sourceRoot, "test_module");

        List<BLangPackage> modules = new LinkedList<>();
        modules.add((BLangPackage) result.getAST());
        Map<String, ModuleDoc> docsMap = BallerinaDocGenerator.generateModuleDocs(
                Paths.get("src/test/resources", sourceRoot).toAbsolutePath().toString(), modules);
        List<ModuleDoc> moduleDocList = new ArrayList<>(docsMap.values());
        moduleDocList.sort(Comparator.comparing(pkg -> pkg.bLangPackage.packageID.toString()));

        Project project = BallerinaDocGenerator.getDocsGenModel(moduleDocList);
        Module testModule = project.modules.get(0);

        for (Function function : testModule.functions) {
            String funcName = function.name;
            if ("multilineDocsFunction".equals(funcName)) {
                this.multilineDocsFunction = function;
            } else if ("deprecatedMultilineDocsFunction".equals(funcName)) {
                this.multilineDocsDeprecatedFunction = function;
            }
        }
    }

    @Test(description = "Test multiline documentation")
    public void testMultilineDocs() {
        Assert.assertNotNull(multilineDocsFunction);
        Assert.assertEquals(multilineDocsFunction.description, firstLine + otherLines);
    }

    @Test(description = "Test multiline documentation with @deprecated annotation")
    public void testMultilineDocsWithDeprecation() {
        Assert.assertEquals(multilineDocsDeprecatedFunction.description, deprecatedFirstLine + otherLines);
    }
}
