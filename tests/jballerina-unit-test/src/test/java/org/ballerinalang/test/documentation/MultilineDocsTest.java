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
import org.ballerinalang.docgen.generator.model.ModuleDoc;
import org.ballerinalang.docgen.generator.model.Project;
import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Test multi-line documentation when deprecation is present or not.
 */
public class MultilineDocsTest {
    private Function multilineDocsFunction;
    private Function multilineDocsDeprecatedFunction;

    private final String lines = "<p>Returns a formatted string using the specified format string and arguments. "
            + "Following format specifiers are allowed.</p>\n"
            + "<p>b - boolean</p>\n"
            + "<p>B - boolean (ALL_CAPS)</p>\n"
            + "<p>d - int</p>\n"
            + "<p>f - float</p>\n"
            + "<p>x - hex</p>\n"
            + "<p>X - HEX (ALL_CAPS)</p>\n"
            + "<p>s - string (This specifier is applicable for any of the supported types in Ballerina.\n"
            + "These values will be converted to their string representation.)</p>\n"
            + "<pre><code class=\"language-ballerina\">"
            + "string s8 = io:sprintf(&quot;%s scored %d for %s and has an average of %.2f.&quot;, name, marks, "
            + "subjects[0], average);\n"
            + "</code></pre>\n";

    @BeforeClass
    public void setup() throws IOException {
        String sourceRoot =
                "test-src" + File.separator + "documentation" + File.separator + "multi_line_docs_project";
        io.ballerina.projects.Project project = BCompileUtil.loadProject(sourceRoot);
        Map<String, ModuleDoc> moduleDocMap = BallerinaDocGenerator.generateModuleDocMap(project);
        Project docerinaProject = BallerinaDocGenerator.getDocsGenModel(moduleDocMap, project.currentPackage()
                .packageOrg().toString(), project.currentPackage().packageVersion().toString());
        Module testModule = docerinaProject.modules.get(0);

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
        Assert.assertEquals(multilineDocsFunction.description, lines);
    }

}
