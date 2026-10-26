/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.analyzer.DataflowAnalyzer;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Map;

/**
 * Tests recompiling a loaded project after edits.
 *
 * @since 2201.14.0
 */
public class RecompilationTests extends BaseTest {

    private static final Path RESOURCE_DIRECTORY = Path.of("src/test/resources/");

    @Test(description = "tests that the dataflow analyzer does not keep symbols of earlier compilations")
    public void testDataflowAnalyzerStateDoesNotGrowAcrossRecompilations() throws ReflectiveOperationException {
        Project project = TestUtils.loadBuildProject(RESOURCE_DIRECTORY.resolve("recompilation_project"));
        project.currentPackage().getCompilation();
        DataflowAnalyzer analyzer = DataflowAnalyzer.getInstance(
                project.projectEnvironmentContext().getService(CompilerContext.class));
        Map<?, ?> invocations = perPackageMap(analyzer, "invocationToDependent");
        int afterFirstCompilation = invocations.size();
        Assert.assertTrue(afterFirstCompilation > 0);

        for (int i = 1; i <= 3; i++) {
            project = recompileWithEdit(project, i);
        }

        Assert.assertEquals(invocations.size(), afterFirstCompilation);
    }

    private static Project recompileWithEdit(Project project, int edit) {
        Module module = project.currentPackage().getDefaultModule();
        Document document = module.document(module.documentIds().iterator().next());
        String content = document.textDocument() + "\nfunction edit" + edit + "() {\n}\n";
        Project edited = document.modify().withContent(content).apply().module().project();
        edited.currentPackage().getCompilation();
        return edited;
    }

    private static Map<?, ?> perPackageMap(DataflowAnalyzer analyzer, String name)
            throws ReflectiveOperationException {
        Field field = DataflowAnalyzer.class.getDeclaredField(name);
        field.setAccessible(true);
        return (Map<?, ?>) field.get(analyzer);
    }
}
