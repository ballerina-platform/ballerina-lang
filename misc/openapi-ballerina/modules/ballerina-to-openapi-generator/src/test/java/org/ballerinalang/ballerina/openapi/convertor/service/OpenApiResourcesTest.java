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

package org.ballerinalang.ballerina.openapi.convertor.service;

import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.ballerina.openapi.convertor.OpenApiConverterException;
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
public class OpenApiResourcesTest {
    Path balFilesPath;
    Path oasDefinitionsPath;
    
    @BeforeClass()
    public void setUp() {
        String resourcesFolder = new File("src/test/resources").getAbsolutePath();
        balFilesPath = Paths.get(resourcesFolder).resolve("bal-files");
        oasDefinitionsPath = Paths.get(resourcesFolder).resolve("oas-definitions");
    }
    
    /**
     * Test case to check if path value in ResourceConfig annotation having no prefixed forward slash generates valid
     * OAS definition.
     */
    @Test(description = "Test forward slash is added when forward slash is not in the resource path value",
            enabled = false)
    public void testPathNoPrefixedForwardSlash() throws OpenApiConverterException, IOException {
        String balSrc =
                FileUtils.readFileToString(balFilesPath.resolve("path-annotations-no-forward-slash.bal").toFile());
        String oasSrc =
            FileUtils.readFileToString(oasDefinitionsPath.resolve("path-annotation-no-forward-slash.yaml").toFile());
        
        String generatedOAS = OpenApiConverterUtils.generateOAS3Definitions(balSrc, "hello");
        
        SwaggerDeserializationResult resultParseResult = new SwaggerParser().readWithInfo(generatedOAS);
        SwaggerDeserializationResult oasSrcParseResult = new SwaggerParser().readWithInfo(oasSrc);
        
        Assert.assertEquals(oasSrcParseResult.getSwagger(), resultParseResult.getSwagger(),
                "Generated OAS resource " + "path is wrong.");
    }
    
    /**
     * Test case to check if path value in ResourceConfig annotation having a prefixed forward slash generates valid
     * OAS definition.
     */
    @Test(description = "Test forward slash is added when forward slash is not in the resource path value",
            enabled = false)
    public void testPathWithPrefixedForwardSlash() throws OpenApiConverterException, IOException {
        String balSrc =
                FileUtils.readFileToString(balFilesPath.resolve("path-annotations-with-forward-slash.bal").toFile());
        String oasSrc =
            FileUtils.readFileToString(oasDefinitionsPath.resolve("path-annotation-no-forward-slash.yaml").toFile());
        
        String generatedOAS = OpenApiConverterUtils.generateOAS3Definitions(balSrc, "hello");
        
        SwaggerDeserializationResult resultParseResult = new SwaggerParser().readWithInfo(generatedOAS);
        SwaggerDeserializationResult oasSrcParseResult = new SwaggerParser().readWithInfo(oasSrc);
        
        Assert.assertEquals(oasSrcParseResult.getSwagger(), resultParseResult.getSwagger(),
                "Generated OAS resource " + "path is wrong.");
    }
}

