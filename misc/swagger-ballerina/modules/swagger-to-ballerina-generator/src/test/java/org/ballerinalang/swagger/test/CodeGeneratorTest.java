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

package org.ballerinalang.swagger.test;

import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.exception.BallerinaOpenApiException;
import org.ballerinalang.swagger.utils.GeneratorConstants.GenType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for {@link org.ballerinalang.swagger.CodeGenerator}
 */
public class CodeGeneratorTest {
    private Path projectPath;

    @BeforeClass()
    public void setup() {
        String resourcePath = CodeGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        projectPath = Paths.get(resourcePath);
    }

    @Test(description = "Test Ballerina skeleton generation")
    public void generateSkeleton() {
        final String pkgName = "service";
        String definitionPath = projectPath + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(pkgName);
        Path outFile = projectPath.resolve(Paths.get(pkgName, "gen", "SwaggerPetstore.bal"));

        try {
            Path cachePath = projectPath.resolve(Paths.get(".ballerina"));
            if (Files.notExists(cachePath)) {
                Files.createDirectory(cachePath);
            }

            generator.generate(GenType.MOCK, definitionPath, projectPath.toString());
            if (Files.exists(outFile)) {
                String result = new String(Files.readAllBytes(outFile));
                Assert.assertTrue(result != null && result.contains("listPets (endpoint outboundEp"));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while writing the service. " + e.getMessage());
        } catch (BallerinaOpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        }
    }

    @Test(description = "Test Ballerina connector generation")
    public void generateConnector() {
        final String pkgName = "connector";
        String definitionPath = projectPath + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();
        generator.setSrcPackage(pkgName);
        Path outFile = projectPath.resolve(Paths.get(pkgName, "gen", "SwaggerPetstore.bal"));

        try {
            Path cachePath = projectPath.resolve(Paths.get(".ballerina"));
            if (Files.notExists(cachePath)) {
                Files.createDirectory(cachePath);
            }

            generator.generate(GenType.CONNECTOR, definitionPath, projectPath.toString());
            if (Files.exists(outFile)) {
                String result = new String(Files.readAllBytes(outFile));
                Assert.assertTrue(result != null && result.contains("public function listPets()"));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        } catch (BallerinaOpenApiException e) {
            Assert.fail("Error while generating the service. " + e.getMessage());
        }
    }

}
