/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.openapi;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.openapi.cmd.Filter;
import org.ballerinalang.openapi.cmd.OpenAPIBallerinaProject;
import org.ballerinalang.openapi.cmd.OpenAPICommandTest;
import org.ballerinalang.openapi.exception.BallerinaOpenApiException;
import org.ballerinalang.openapi.model.GenSrcFile;
import org.ballerinalang.openapi.utils.GeneratorConstants.GenType;
import org.ballerinalang.openapi.utils.TypeExtractorUtil;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for {@link org.ballerinalang.openapi.CodeGenerator}.
 */
public class CodeGeneratorTest {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private Path projectPath;
    private Path sourceRoot;
    List<String> list1 = new ArrayList<>();
    List<String> list2 = new ArrayList<>();
    Filter filter = new Filter(list1, list2);
    @BeforeClass
    public void setUp() {
        projectPath = RES_DIR.resolve(Paths.get("expected", "petStore"));
    }

    @Test(description = "Test Ballerina skeleton generation")
    public void generateSkeleton() {
        final String pkgName = "module";
        final String serviceName = "openapi_petstore";
        String definitionPath = RES_DIR + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(pkgName);

        //Set relative path for contract path which will be printed on the generated service bal file
        Path absPath = Paths.get(definitionPath);
        Path basePath = Paths.get(projectPath.toString());
        Path pathRelative = basePath.relativize(absPath);

        try {
            OpenAPIBallerinaProject ballerinaProject = OpenAPICommandTest.createBalProject(projectPath.toString());
            OpenAPICommandTest.createBalProjectModule(ballerinaProject, pkgName);
            Path outFile = ballerinaProject.getImplPath().resolve(Paths.get("openapi_petstore.bal"));
            generator.generateService(projectPath.toString(), definitionPath, pathRelative.toString(), serviceName,
                    projectPath.toString(), filter);
            if (Files.exists(outFile)) {
                String result = new String(Files.readAllBytes(outFile));
                Assert.assertTrue(result.contains("service openapi_petstore on ep0, ep1 {\n" +
                        "\n" +
                        "    @http:ResourceConfig {\n" +
                        "        methods:[\"GET\"],\n" +
                        "        path:\"/pets\"\n" +
                        "    }\n" +
                        "    resource function listpets (http:Caller outboundEp, http:Request Req ) returns error? {\n"
                        + "\n" +
                        "    }\n" +
                        "\n" +
                        "    @http:ResourceConfig {\n" +
                        "        methods:[\"POST\"],\n" +
                        "        path:\"/pets\"\n" +
                        "    }\n" +
                        "    resource function resource__post_pets (http:Caller outboundEp, http:Request Req )" +
                        " returns error? {\n" +
                        "\n" +
                        "    }\n" +
                        "\n" +
                        "    @http:ResourceConfig {\n" +
                        "        methods:[\"GET\"],\n" +
                        "        path:\"/pets/{petId}\"\n" +
                        "    }\n" +
                        "    resource function showpetbyid (http:Caller outboundEp, http:Request Req, string petId )" +
                        " returns error? {\n" +
                        "\n" +
                        "    }\n" +
                        "\n" +
                        "}"));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException | BallerinaOpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        }
    }

    @Test(description = "Test Ballerina client generation")
    public void generateClient() {
        final String pkgName = "client";
        String definitionPath = RES_DIR + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(pkgName);
        try {
            OpenAPIBallerinaProject ballerinaProject = OpenAPICommandTest.createBalProject(projectPath.toString()
            );
            Path outFile = ballerinaProject.getImplPath().resolve("client")
                    .resolve(Paths.get("openapi_petstore.bal"));
            generator.generateClient(projectPath.toString(), definitionPath, "openapi_petstore",
                    projectPath.toString(), filter);
            if (Files.exists(outFile)) {
                String result = new String(Files.readAllBytes(outFile));
                Assert.assertTrue(result.contains("public remote function listPets()"));
            } else {
                Assert.fail("Client was not generated");
            }
        } catch (IOException | BallerinaOpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        }
    }

    @Test(description = "Test openapi definition to ballerina source code generation",
            dataProvider = "fileProvider")
    public void openApiToBallerinaCodeGenTest(String yamlFile, String expectedFile) {
        String definitionPath = RES_DIR.resolve(yamlFile).toString();
        Path expectedFilePath = RES_DIR.resolve(Paths.get("expected", expectedFile));

        CodeGenerator generator = new CodeGenerator();
        try {
            String expectedContent = new String(Files.readAllBytes(expectedFilePath));
            List<GenSrcFile> generatedFileList = generator.generateBalSource(GenType.GEN_SERVICE,
                    definitionPath, "", "", filter);
            if (generatedFileList.size() > 0) {
                GenSrcFile actualGeneratedContent = generatedFileList.get(0);
                Assert.assertEquals(actualGeneratedContent.getContent(), expectedContent,
                        "expected content and actual generated content is mismatched for: " + yamlFile);
            }
        } catch (IOException | BallerinaOpenApiException e) {
            Assert.fail("Error while generating the ballerina content for the openapi definition: "
                    + yamlFile + " " + e.getMessage());
        }
    }
    
    @Test
    public void escapeIdentifierTest() {
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("abc"), "abc");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("string"), "'string");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("int"), "'int");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("io.foo.bar"), "'io\\.foo\\.bar");
        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier("getV1CoreVersion"), "getV1CoreVersion");
//        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier
//        ("sample_service_\\ \\!\\:\\[\\;"), "'sample_service_\\ \\!\\:\\[\\;");
//        Assert.assertEquals(TypeExtractorUtil.escapeIdentifier
//        ("listPets resource_!$:[;"), "'listPets\\ resource_\\!\\$\\:\\[\\;");
    }
    
    @Test
    public void escapeTypeTest() {
        Assert.assertEquals(TypeExtractorUtil.escapeType("abc"), "abc");
        Assert.assertEquals(TypeExtractorUtil.escapeType("string"), "string");
        Assert.assertEquals(TypeExtractorUtil.escapeType("int"), "int");
        Assert.assertEquals(TypeExtractorUtil.escapeType("io.foo.bar"), "'io\\.foo\\.bar");
        Assert.assertEquals(TypeExtractorUtil.escapeType("getV1CoreVersion"), "getV1CoreVersion");
    }

    @DataProvider(name = "fileProvider")
    public Object[][] fileProvider() {
        return new Object[][]{
                {"emptyService.yaml", "emptyService.bal"},
                {"emptyPath.yaml", "emptyPath.bal"},
                {"noOperationId.yaml", "noOperationId.bal"},
                {"multiMethodResources.yaml", "multiMethodResources.bal"},
                {"nonEmptyPath.yaml", "nonEmptyPath.bal"},
                {"petstore.yaml", "petstore.bal"},
        };
    }

    @AfterTest
    public void afterTest() {
        try {
            FileUtils.deleteDirectory(projectPath.toFile());
        } catch (IOException e) {
            // Ignore.
        }
    }
}
