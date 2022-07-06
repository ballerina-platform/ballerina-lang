/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.jsonmapper;


import io.ballerina.jsonmapper.diagnostic.JsonToRecordMapperDiagnostic;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for JsonToRecordMapperTests.
 */
public class JsonToRecordMapperTests {

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
    private final Path sample2TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_2_type_desc.bal");

    private final Path sample3Json = RES_DIR.resolve("json")
            .resolve("sample_3.json");
    private final Path sample3Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_3.bal");
    private final Path sample3TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_3_type_desc.bal");

    private final Path sample4Json = RES_DIR.resolve("json")
            .resolve("sample_4.json");
    private final Path sample4Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_4.bal");
    private final Path sample4TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_4_type_desc.bal");

    private final Path sample5Json = RES_DIR.resolve("json")
            .resolve("sample_5.json");
    private final Path sample5Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_5.bal");

    private final Path sample6Json = RES_DIR.resolve("json")
            .resolve("sample_6.json");
    private final Path sample6Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_6.bal");
    private final Path sample6TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_6_type_desc.bal");

    private final Path sample7Json = RES_DIR.resolve("json")
            .resolve("sample_7.json");
    private final Path sample7Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_7.bal");

    private final Path sample8Json = RES_DIR.resolve("json")
            .resolve("sample_8.json");
    private final Path sample8Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_8.bal");
    private final Path sample8TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_8_type_desc.bal");

    private final Path sample9Json = RES_DIR.resolve("json")
            .resolve("sample_9.json");
    private final Path sample9Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_9.bal");
    private final Path sample9TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_9_type_desc.bal");

    private final Path sample10Json = RES_DIR.resolve("json")
            .resolve("sample_10.json");
    private final Path sample10Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_10.bal");
    private final Path sample10TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_10_type_desc.bal");

    @Test(description = "Test for primitive and null types")
    public void testForPrimitiveAndNullTypes() throws IOException {
        String jsonFileContent = Files.readString(sample0Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample0Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for all number types")
    public void testForJsonNumberTypes() throws IOException {
        String jsonFileContent = Files.readString(sample1Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample1Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing fields in same JSON object")
    public void testForDifferencingFields() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing fields in same JSON object - inline")
    public void testForDifferencingFieldsInLine() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing field values in same JSON object")
    public void testForDifferencingFieldValues() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing field values in same JSON object - inline")
    public void testForDifferencingFieldValuesInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for empty and non-empty JSON array")
    public void testForEmptyNonEmptyJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample4Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample4Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for empty and non-empty JSON array - inline")
    public void testForEmptyNonEmptyJsonArrayInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample4Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample4TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON array with values of same and different type")
    public void testForSameAndDifferentTypeValuesInArray() throws IOException {
        String jsonFileContent = Files.readString(sample5Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample5Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array")
    public void testForDifferentObjectsInJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array - inline")
    public void testForDifferentObjectsInJsonArrayInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for multi dimensional JSON array")
    public void testForMultiDimensionalJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample7Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample7Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for complex JSON object")
    public void testForComplexJsonObject() throws IOException {
        String jsonFileContent = Files.readString(sample8Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample8Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for complex JSON object - inline")
    public void testForComplexJsonObjectInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample8Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample8TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for many types in JSON array")
    public void testForMultipleItemsJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample9Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample9Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for many types in JSON array - inline")
    public void testForMultipleItemsJsonArrayInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample9Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample9TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON array of objects")
    public void testForJsonArrayOfObjects() throws IOException {
        String jsonFileContent = Files.readString(sample10Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample10Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON array of objects - inline")
    public void testForJsonArrayOfObjectsInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample10Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample10TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test Choreo Transformation and Data Mapping Payloads")
    public void testChoreoTransPayloads() throws IOException {
        Map<Path, Path> samples = new HashMap<>();
        for (int i = 0; i <= 3; i++) {
            Path jsonInputPath = RES_DIR.resolve("json").resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_input.json", i));
            Path balInputPath = RES_DIR.resolve("ballerina").resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_input.bal", i));
            Path jsonOutputPath = RES_DIR.resolve("json").resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_output.json", i));
            Path balOutputPath = RES_DIR.resolve("ballerina").resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_output.bal", i));
            samples.put(jsonInputPath, balInputPath);
            samples.put(jsonOutputPath, balOutputPath);
        }
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            JsonToRecordResponse jsonToRecordResponse =
                    JsonToRecordMapper.convert(jsonFileContent, null, false, false);
            if (jsonToRecordResponse.getCodeBlock() != null) {
                String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
                String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
                Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
            }
            List<JsonToRecordMapperDiagnostic> diagnostics = jsonToRecordResponse.getDiagnostics();
            for (JsonToRecordMapperDiagnostic diagnostic : diagnostics) {
                //TODO: react when there is a diagnostic message.
            }
        }
    }
}
