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
import org.javatuples.Pair;
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
    private static final String BAL_DIR = "ballerina";
    private static final String JSON_DIR = "json";
    private static final String PROJECT_DIR = "project";
    private static final String SOURCE_DIR = "source";
    private static final String ASSERT_DIR = "assert";

    private final Path sample0Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_0.json");
    private final Path sample0Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_0.bal");

    private final Path sample1Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_1.json");
    private final Path sample1Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_1.bal");

    private final Path sample2Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_2.json");
    private final Path sample2Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_2.bal");
    private final Path sample2TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_2_type_desc.bal");

    private final Path sample3Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_3.json");
    private final Path sample3Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_3.bal");
    private final Path sample3TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_3_type_desc.bal");
    private final Path sample3PersonBal = RES_DIR.resolve("ballerina")
            .resolve("sample_3_person.bal");
    private final Path sample3PersonTypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_3_person_type_desc.bal");
    private final Path sample3SpecialCharBal = RES_DIR.resolve("ballerina")
            .resolve("sample_3_special_char.bal");

    private final Path sample4Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_4.json");
    private final Path sample4Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_4.bal");
    private final Path sample4TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_4_type_desc.bal");

    private final Path sample5Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_5.json");
    private final Path sample5Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_5.bal");

    private final Path sample6Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_6.json");
    private final Path sample6Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_6.bal");
    private final Path sample6TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_6_type_desc.bal");
    private final Path sample6ClosedBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_6_closed.bal");
    private final Path sample6TypeDescClosedBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_6_type_desc_closed.bal");

    private final Path sample7Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_7.json");
    private final Path sample7Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_7.bal");

    private final Path sample8Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_8.json");
    private final Path sample8Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_8.bal");
    private final Path sample8TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_8_type_desc.bal");

    private final Path sample9Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_9.json");
    private final Path sample9Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_9.bal");
    private final Path sample9TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_9_type_desc.bal");

    private final Path sample10Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_10.json");
    private final Path sample10Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_10.bal");
    private final Path sample10TypeDescBal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_10_type_desc.bal");

    private final Path sample11Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_11.json");
    private final Path sample11Bal = RES_DIR.resolve(BAL_DIR)
            .resolve("sample_11.bal");

    private final Path sample12Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_12.json");

    private final Path sample13Json = RES_DIR.resolve(JSON_DIR)
            .resolve("sample_13.json");

    private final Path singleBalFile = RES_DIR.resolve(PROJECT_DIR).resolve(SOURCE_DIR)
            .resolve("singleFileProject").resolve("SingleBalFile.bal");

    private final Path invalidBalFile = RES_DIR.resolve(PROJECT_DIR).resolve(SOURCE_DIR)
            .resolve("InvalidBalFile.txt");

    private final Path balProjectFileDefaultModule = RES_DIR.resolve(PROJECT_DIR).resolve(SOURCE_DIR)
            .resolve("balProject").resolve("main.bal");
    private final Path balProjectFileDefaultModuleAssert = RES_DIR.resolve(PROJECT_DIR).resolve(ASSERT_DIR)
            .resolve("balProject").resolve("sample_bal_project_default.bal");

    private final Path balProjectFileUtilModule = RES_DIR.resolve(PROJECT_DIR).resolve(SOURCE_DIR)
            .resolve("balProject").resolve("modules").resolve("util").resolve("util.bal");
    private final Path balProjectFileUtilModuleAssert = RES_DIR.resolve(PROJECT_DIR).resolve(ASSERT_DIR)
            .resolve("balProject").resolve("sample_bal_project_util.bal");

    @Test(description = "Test for primitive and null types")
    public void testForPrimitiveAndNullTypes() throws IOException {
        String jsonFileContent = Files.readString(sample0Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample0Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for all number types")
    public void testForJsonNumberTypes() throws IOException {
        String jsonFileContent = Files.readString(sample1Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample1Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing fields in same JSON object")
    public void testForDifferencingFields() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing fields in same JSON object - inline")
    public void testForDifferencingFieldsInLine() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing field values in same JSON object")
    public void testForDifferencingFieldValues() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for differencing field values in same JSON object - inline")
    public void testForDifferencingFieldValuesInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for empty and non-empty JSON array")
    public void testForEmptyNonEmptyJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample4Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample4Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for empty and non-empty JSON array - inline")
    public void testForEmptyNonEmptyJsonArrayInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample4Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample4TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON array with values of same and different type")
    public void testForSameAndDifferentTypeValuesInArray() throws IOException {
        String jsonFileContent = Files.readString(sample5Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample5Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array")
    public void testForDifferentObjectsInJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array - inline")
    public void testForDifferentObjectsInJsonArrayInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array - closed")
    public void testForDifferentObjectsInJsonArrayClosed() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, true, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6ClosedBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array - inline/closed")
    public void testForDifferentObjectsInJsonArrayInLineClosed() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, true, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample6TypeDescClosedBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for different objects in JSON array - inline/closed/forceFormatRecField")
    public void testForDifferentObjectsInJsonArrayInLineClosedForceFormatRecFields() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, true, true, null, null)
                .getCodeBlock();
        String expectedCodeBlock = Files.readString(sample6TypeDescClosedBal);
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for multi dimensional JSON array")
    public void testForMultiDimensionalJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample7Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample7Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for complex JSON object")
    public void testForComplexJsonObject() throws IOException {
        String jsonFileContent = Files.readString(sample8Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample8Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for complex JSON object - inline")
    public void testForComplexJsonObjectInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample8Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample8TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for many types in JSON array")
    public void testForMultipleItemsJsonArray() throws IOException {
        String jsonFileContent = Files.readString(sample9Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample9Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for many types in JSON array - inline")
    public void testForMultipleItemsJsonArrayInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample9Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample9TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON array of objects")
    public void testForJsonArrayOfObjects() throws IOException {
        String jsonFileContent = Files.readString(sample10Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample10Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON array of objects - inline")
    public void testForJsonArrayOfObjectsInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample10Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample10TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON field names with special characters")
    public void testForJsonFieldsOfSpecialChars() throws IOException {
        String jsonFileContent = Files.readString(sample11Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample11Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for Invalid JSON")
    public void testForInvalidJson() throws IOException {
        String jsonFileContent = Files.readString(sample12Json);
        List<JsonToRecordMapperDiagnostic> diagnostics =
                JsonToRecordMapper.convert(jsonFileContent, "", false, false, false, null, null).getDiagnostics();
        String diagnosticMessage =
                "Provided JSON is invalid : Unterminated object at line 15 column 8 path $.friend.address.city";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for JSON with a fieldName similar to recordName")
    public void testForSimilarRecordNameAndFieldName() throws IOException {
        String jsonFileContent = Files.readString(sample13Json);
        List<JsonToRecordMapperDiagnostic> diagnostics =
                JsonToRecordMapper.convert(jsonFileContent, "Person", false, false, false, null, null).getDiagnostics();
        String diagnosticMessage = "Provided record name 'Person' conflicts with the other generated records. " +
                "Consider providing a different name.";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for JSON with user defined record name")
    public void testForUserDefinedRecordName() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock = JsonToRecordMapper
                .convert(jsonFileContent, "Person", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3PersonBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON with user defined record name - inline")
    public void testForUserDefinedRecordNameInLIne() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock = JsonToRecordMapper
                .convert(jsonFileContent, "Person", true, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3PersonTypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for JSON with user defined record name with special chars")
    public void testForUserDefinedRecordNameWithSpecialChars() throws IOException {
        String jsonFileContent = Files.readString(sample3Json);
        String generatedCodeBlock =
                JsonToRecordMapper.convert(jsonFileContent, "Special Person", false, false, false, null, null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample3SpecialCharBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test Choreo Transformation and Data Mapping Payloads")
    public void testChoreoTransPayloads() throws IOException {
        Map<Path, Path> samples = new HashMap<>();
        for (int i = 0; i <= 3; i++) {
            Path jsonInputPath = RES_DIR.resolve(JSON_DIR).resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_input.json", i));
            Path balInputPath = RES_DIR.resolve(BAL_DIR).resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_input.bal", i));
            Path jsonOutputPath = RES_DIR.resolve(JSON_DIR).resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_output.json", i));
            Path balOutputPath = RES_DIR.resolve(BAL_DIR).resolve("ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_output.bal", i));
            samples.put(jsonInputPath, balInputPath);
            samples.put(jsonOutputPath, balOutputPath);
        }
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            JsonToRecordResponse jsonToRecordResponse =
                    JsonToRecordMapper.convert(jsonFileContent, null, false, false, false, null, null);
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

    @Test(description = "Test for conflicting record names in invalid bal file")
    public void testForConflictingRecordNamesInInvalidBalFile() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        JsonToRecordResponse response = JsonToRecordMapper
                .convert(jsonFileContent, null, false, false, false, invalidBalFile.toUri().toString(), null);
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for conflicting record names in single bal file project")
    public void testForConflictingRecordNamesInSingleBalFileProject() throws IOException {
        Map<String, Pair<Path, Path>> existingRecordsToJsonSamples = new HashMap<>() {{
            put("sample_4.bal", new Pair<>(sample4Bal, sample4Json));
            put("sample_8.bal", new Pair<>(sample8Bal, sample8Json));
            put("sample_10.bal", new Pair<>(sample10Bal, sample10Json));
            put("sample_11.bal", new Pair<>(sample11Bal, sample11Json));
        }};
        Map<String, Pair<Path, Path>> existingRecordsToJsonTypeDescSamples = new HashMap<>() {{
            put("sample_4_type_desc.bal", new Pair<>(sample4Bal, sample4Json));
            put("sample_9_type_desc.bal", new Pair<>(sample9Bal, sample9Json));
            put("sample_10_type_desc.bal", new Pair<>(sample10Bal, sample10Json));
        }};

        for (Map.Entry<String, Pair<Path, Path>> entry : existingRecordsToJsonSamples.entrySet()) {
            Path jsonFilePath = entry.getValue().getValue1();
            Path balFilePath = RES_DIR.resolve(PROJECT_DIR).resolve(ASSERT_DIR).resolve("singleFileProject")
                    .resolve(entry.getKey());
            String balExistingFilePath = entry.getValue().getValue0().toUri().toString();
            String jsonFileContent = Files.readString(jsonFilePath);
            JsonToRecordResponse jsonToRecordResponse = JsonToRecordMapper
                    .convert(jsonFileContent, null, false, false, false, balExistingFilePath, null);
            String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(balFilePath).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
        for (Map.Entry<String, Pair<Path, Path>> entry : existingRecordsToJsonTypeDescSamples.entrySet()) {
            Path jsonFilePath = entry.getValue().getValue1();
            Path balFilePath = RES_DIR.resolve(PROJECT_DIR).resolve(ASSERT_DIR).resolve("singleFileProject")
                    .resolve(entry.getKey());
            String balExistingFilePath = entry.getValue().getValue0().toUri().toString();
            String jsonFileContent = Files.readString(jsonFilePath);
            JsonToRecordResponse jsonToRecordResponse =
                    JsonToRecordMapper.convert(jsonFileContent, null, true, false, false, balExistingFilePath, null);
            String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(balFilePath).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
    }

    @Test(description = "Test for conflicting parent record name")
    public void testForConflictingParentRecordName() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper
                .convert(jsonFileContent, "Friend", false, false, false, sample2Bal.toUri().toString(), null)
                .getDiagnostics();
        String diagnosticMessage = "Provided record name 'Friend' conflicts with already existing records. " +
                "Consider providing a different name.";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for conflicting record names in bal project default module")
    public void testForConflictingRecordNamesInBalProjectDefault() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordMapper
                .convert(jsonFileContent, null, false, false, false,
                        balProjectFileDefaultModule.toUri().toString(), null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(balProjectFileDefaultModuleAssert).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for conflicting record names in bal project non-default module")
    public void testForConflictingRecordNamesInBalProjectNonDefault() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper
                .convert(jsonFileContent, "", false, false, false, balProjectFileUtilModule.toUri().toString(), null)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(balProjectFileUtilModuleAssert).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for conflicting record name rename diagnostics")
    public void testForConflictingRecordNameRenameDiagnostics() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper
                .convert(jsonFileContent, "NewRecord", false, false, false,
                        balProjectFileUtilModule.toUri().toString(), null)
                .getDiagnostics();
        String diagnosticMessage0 = "The record name 'State' is renamed as 'State_01'. " +
                "Consider rename it back to a meaningful name.";
        String diagnosticMessage1 = "The record name 'Author' is renamed as 'Author_01'. " +
                "Consider rename it back to a meaningful name.";
        Assert.assertEquals(diagnostics.size(), 2);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage0);
        Assert.assertEquals(diagnostics.get(1).message(), diagnosticMessage1);
    }
}
