/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.ballerina.projects.Project;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test type generation.
 *
 * @since 2201.3.1
 */
public class TypeGeneratorTests {
    private static final Path RES_DIR = Paths.get("src", "test", "resources").toAbsolutePath();
    private static final String BALLERINA = "ballerina";
    private static final String RESULTS = "results";
    Gson gson = new GsonBuilder().serializeNulls().create();

    @Test(description = "simple type model generation")
    public void testSimpleTypeModelGeneration() throws IOException {
        Path projectPath = RES_DIR.resolve(BALLERINA).resolve(
                Path.of("type_gen_simple"));
        Path expectedJsonPath = RES_DIR.resolve(RESULTS).resolve(Path.of("type_gen_sample.json"));

        Project project = TestUtils.loadBuildProject(projectPath, false);
        ComponentModelBuilder componentModelBuilder = new ComponentModelBuilder();
        ComponentModel generatedModel = componentModelBuilder.constructComponentModel(project.currentPackage());
        ComponentModel expectedModel = TestUtils.getComponentFromGivenJsonFile(expectedJsonPath);

        String generatedEntities = gson.toJson(generatedModel.getEntities()).replaceAll("\\s+", "");
        String expectedEntities = gson.toJson(expectedModel.getEntities())
                .replaceAll("\\s+", "")
                .replaceAll("\\{srcPath\\}", RES_DIR.toAbsolutePath().toString());

        Assert.assertEquals(generatedEntities, expectedEntities);
    }
}
