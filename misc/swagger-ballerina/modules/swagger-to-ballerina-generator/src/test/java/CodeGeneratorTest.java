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

import org.ballerinalang.swagger.CodeGenerator;
import org.ballerinalang.swagger.utils.GeneratorConstants.GenType;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Unit tests for {@link org.ballerinalang.swagger.CodeGenerator}
 */
public class CodeGeneratorTest {
    private String testResourceRoot;

    @BeforeClass()
    public void setup() {
        testResourceRoot = CodeGeneratorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    }

    @Test(description = "Test Ballerina skeleton generation when destination path is provided")
    public void generateSkeletonWithDestination() {
        String outPath = testResourceRoot + File.separator + "service.bal";
        String definitionPath = testResourceRoot + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();

        try {
            generator.generate(GenType.SKELETON, definitionPath, outPath);
            File genFile = new File(outPath);

            if (genFile.exists()) {
                String result = new String(Files.readAllBytes(Paths.get(genFile.getPath())));
                Assert.assertTrue(result != null && result.contains("SwaggerPetstore"));
            } else {
                Assert.fail("Service was not generated");
            }
        } catch (IOException e) {
            Assert.fail("Error while generating the service");
        }
    }

    @Test(description = "Test Ballerina skeleton generation when destination path is not provided")
    public void generateSkeletonWithoutDestination() {
        String definitionPath = testResourceRoot + File.separator + "petstore.yaml";
        CodeGenerator generator = new CodeGenerator();

        try {
            generator.generate(GenType.SKELETON, definitionPath, null);
        } catch (IOException e) {
            Assert.fail("Error while generating the service");
        }
    }
}
