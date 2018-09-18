/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.ballerina.swagger.convertor.service;

import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.ballerina.swagger.convertor.SwaggerConverterException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test cases related to resources of a OAS definition.
 */
public class SwaggerResourcesTest {
    Path balFilesPath;
    Path oasDefinitionsPath;
    
    @BeforeClass()
    public void setUp() {
        String resourcesFolder = new File("src/test/resources").getAbsolutePath();
        balFilesPath = Paths.get(resourcesFolder).resolve("bal-files");
        oasDefinitionsPath = Paths.get(resourcesFolder).resolve("oas-definitions");
    }
    
    /**
     * Test case to check if path value in ResourceConfig annotation having no hyphen generates valid OAS definition.
     */
    @Test(description = "Test hyphen is added when hyphen is not in the resource path value")
    public void testPathNoPrefixedHyphen() throws SwaggerConverterException, IOException {
        String balSrc = FileUtils.readFileToString(balFilesPath.resolve("path-annotations-no-hyphen.bal").toFile());
        String oasSrc =
                FileUtils.readFileToString(oasDefinitionsPath.resolve("path-annotation-no-hyphen.yaml").toFile());
        
        String generatedOAS = SwaggerConverterUtils.generateOAS3Definitions(balSrc, "hello");
    
        SwaggerDeserializationResult resultParseResult = new SwaggerParser().readWithInfo(generatedOAS);
        SwaggerDeserializationResult oasSrcParseResult = new SwaggerParser().readWithInfo(oasSrc);
    
        Assert.assertEquals(oasSrcParseResult.getSwagger(), resultParseResult.getSwagger(), "Generated OAS resource " +
                                                                                            "path is wrong.");
    }
    
    /**
     * Test case to check if path value in ResourceConfig annotation having an hyphen generates valid OAS definition.
     */
    @Test(description = "Test hyphen is added when hyphen is not in the resource path value")
    public void testPathWithPrefixedHyphen() throws SwaggerConverterException, IOException {
        String balSrc = FileUtils.readFileToString(balFilesPath.resolve("path-annotations-with-hyphen.bal").toFile());
        String oasSrc =
                FileUtils.readFileToString(oasDefinitionsPath.resolve("path-annotation-no-hyphen.yaml").toFile());
        
        String generatedOAS = SwaggerConverterUtils.generateOAS3Definitions(balSrc, "hello");
    
        SwaggerDeserializationResult resultParseResult = new SwaggerParser().readWithInfo(generatedOAS);
        SwaggerDeserializationResult oasSrcParseResult = new SwaggerParser().readWithInfo(oasSrc);
    
        Assert.assertEquals(oasSrcParseResult.getSwagger(), resultParseResult.getSwagger(), "Generated OAS resource " +
                                                                                            "path is wrong.");
    }
}

