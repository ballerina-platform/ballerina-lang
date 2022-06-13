/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.converters;

import org.ballerinalang.formatter.core.FormatterException;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for JsonToRecordDirectConverter.
 */
public class JsonToRecordDirectConverterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    private final Path sample0Json = RES_DIR.resolve("json")
            .resolve("sample_0.json");
    private final Path sample0Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_0.bal");

    private final Path sample1Json = RES_DIR.resolve("json")
            .resolve("sample_1.json");
    private final Path sample1Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_1.bal");

    private final Path sample2Json = RES_DIR.resolve("json")
            .resolve("sample_2.json");
    private final Path sample2Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_2.bal");
    @Test(description = "Test with basic json value")
    public void testBasicJson() throws FormatterException, IOException {
        String jsonFileContent = Files.readString(sample0Json);
        String generatedCodeBlock = JsonToRecordDirectConverter.convert(jsonFileContent).getCodeBlock().
                replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample0Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with basic json value contains arrays")
    public void testJsonWithArray() throws FormatterException, IOException {
        String jsonFileContent = Files.readString(sample1Json);
        String generatedCodeBlock = JsonToRecordDirectConverter.convert(jsonFileContent).getCodeBlock().
                replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample1Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with basic nested json value")
    public void testNestedJson() throws FormatterException, IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordDirectConverter.convert(jsonFileContent).getCodeBlock().
                replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }
}
