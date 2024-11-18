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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for JsonToRecordMapperTests.
 */
public class JsonToRecordMapperTests {

    private static final Path RES_DIR = Path.of("src/test/resources/").toAbsolutePath();

    private final Path sample0Json = RES_DIR.resolve("json/sample_0.json");
    private final Path sample0Bal = RES_DIR.resolve("ballerina/sample_0.bal");

    private final Path sample1Json = RES_DIR.resolve("json/sample_1.json");
    private final Path sample1Bal = RES_DIR.resolve("ballerina/sample_1.bal");

    private final Path sample2Json = RES_DIR.resolve("json/sample_2.json");
    private final Path sample2Bal = RES_DIR.resolve("ballerina/sample_2.bal");
    private final Path sample2TypeDescBal = RES_DIR.resolve("ballerina/sample_2_type_desc.bal");

    private final Path sample3Json = RES_DIR.resolve("json/sample_3.json");
    private final Path sample3Bal = RES_DIR.resolve("ballerina/sample_3.bal");
    private final Path sample3TypeDescBal = RES_DIR.resolve("ballerina/sample_3_type_desc.bal");
    private final Path sample3PersonBal = RES_DIR.resolve("ballerina/sample_3_person.bal");
    private final Path sample3PersonTypeDescBal = RES_DIR.resolve("ballerina/sample_3_person_type_desc.bal");
    private final Path sample3SpecialCharBal = RES_DIR.resolve("ballerina/sample_3_special_char.bal");

    private final Path sample4Json = RES_DIR.resolve("json/sample_4.json");
    private final Path sample4Bal = RES_DIR.resolve("ballerina/sample_4.bal");
    private final Path sample4TypeDescBal = RES_DIR.resolve("ballerina/sample_4_type_desc.bal");

    private final Path sample5Json = RES_DIR.resolve("json/sample_5.json");
    private final Path sample5Bal = RES_DIR.resolve("ballerina/sample_5.bal");

    private final Path sample6Json = RES_DIR.resolve("json/sample_6.json");
    private final Path sample6Bal = RES_DIR.resolve("ballerina/sample_6.bal");
    private final Path sample6TypeDescBal = RES_DIR.resolve("ballerina/sample_6_type_desc.bal");
    private final Path sample6ClosedBal = RES_DIR.resolve("ballerina/sample_6_closed.bal");
    private final Path sample6TypeDescClosedBal = RES_DIR.resolve("ballerina/sample_6_type_desc_closed.bal");

    private final Path sample7Json = RES_DIR.resolve("json/sample_7.json");
    private final Path sample7Bal = RES_DIR.resolve("ballerina/sample_7.bal");

    private final Path sample8Json = RES_DIR.resolve("json/sample_8.json");
    private final Path sample8Bal = RES_DIR.resolve("ballerina/sample_8.bal");
    private final Path sample8TypeDescBal = RES_DIR.resolve("ballerina/sample_8_type_desc.bal");

    private final Path sample9Json = RES_DIR.resolve("json/sample_9.json");
    private final Path sample9Bal = RES_DIR.resolve("ballerina/sample_9.bal");
    private final Path sample9TypeDescBal = RES_DIR.resolve("ballerina/sample_9_type_desc.bal");

    private final Path sample10Json = RES_DIR.resolve("json/sample_10.json");
    private final Path sample10Bal = RES_DIR.resolve("ballerina/sample_10.bal");
    private final Path sample10TypeDescBal = RES_DIR.resolve("ballerina/sample_10_type_desc.bal");

    private final Path sample11Json = RES_DIR.resolve("json/sample_11.json");
    private final Path sample11Bal = RES_DIR.resolve("ballerina/sample_11.bal");

    private final Path sample12Json = RES_DIR.resolve("json/sample_12.json");

    private final Path sample13Json = RES_DIR.resolve("json/sample_13.json");

    private final Path sample14Json = RES_DIR.resolve("json/sample_14.json");
    private final Path sample14Bal = RES_DIR.resolve("ballerina/sample_14.bal");

    private final Path sample15Json = RES_DIR.resolve("json/sample_15.json");
    private final Path sample15TypeDescBal = RES_DIR.resolve("ballerina/sample_15_type_desc.bal");

    private final Path sample16Json = RES_DIR.resolve("json/sample_16.json");

    private final Path sample16Bal = RES_DIR.resolve("ballerina/sample_16.bal");

    private final Path singleBalFile = RES_DIR.resolve("project/source/singleFileProject/SingleBalFile.bal");

    private final Path invalidBalFile = RES_DIR.resolve("project/source/InvalidBalFile.txt");

    private final Path balProjectFileDefaultModule = RES_DIR.resolve("project/source/balProject/main.bal");
    private final Path balProjectFileDefaultModuleAssert =
            RES_DIR.resolve("project/assert/balProject/sample_bal_project_default.bal");

    private final Path balProjectFileUtilModule = RES_DIR.resolve("project/source/balProject/modules/util/util.bal");
    private final Path balProjectFileUtilModuleAssert =
            RES_DIR.resolve("project/assert/balProject/sample_bal_project_util.bal");

    @Test(description = "Test for primitive and null types")
    public void testForPrimitiveAndNullTypes() throws IOException {
        runPositiveTest(sample0Json, sample0Bal, "", false, false, false);
    }

    @Test(description = "Test for all number types")
    public void testForJsonNumberTypes() throws IOException {
        runPositiveTest(sample1Json, sample1Bal, "", false, false, false);
    }

    @Test(description = "Test for differencing fields in same JSON object")
    public void testForDifferencingFields() throws IOException {
        runPositiveTest(sample2Json, sample2Bal, "", false, false, false);
    }

    @Test(description = "Test for differencing fields in same JSON object - inline")
    public void testForDifferencingFieldsInLine() throws IOException {
        runPositiveTest(sample2Json, sample2TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for differencing field values in same JSON object")
    public void testForDifferencingFieldValues() throws IOException {
        runPositiveTest(sample3Json, sample3Bal, "", false, false, false);
    }

    @Test(description = "Test for differencing field values in same JSON object - inline")
    public void testForDifferencingFieldValuesInLIne() throws IOException {
        runPositiveTest(sample3Json, sample3TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for empty and non-empty JSON array")
    public void testForEmptyNonEmptyJsonArray() throws IOException {
        runPositiveTest(sample4Json, sample4Bal, "", false, false, false);
    }

    @Test(description = "Test for empty and non-empty JSON array - inline")
    public void testForEmptyNonEmptyJsonArrayInLIne() throws IOException {
        runPositiveTest(sample4Json, sample4TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for JSON array with values of same and different type")
    public void testForSameAndDifferentTypeValuesInArray() throws IOException {
        runPositiveTest(sample5Json, sample5Bal, "", false, false, false);
    }

    @Test(description = "Test for different objects in JSON array")
    public void testForDifferentObjectsInJsonArray() throws IOException {
        runPositiveTest(sample6Json, sample6Bal, "", false, false, false);
    }

    @Test(description = "Test for different objects in JSON array - inline")
    public void testForDifferentObjectsInJsonArrayInLIne() throws IOException {
        runPositiveTest(sample6Json, sample6TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for different objects in JSON array - closed")
    public void testForDifferentObjectsInJsonArrayClosed() throws IOException {
        runPositiveTest(sample6Json, sample6ClosedBal, "", false, true, false);
    }

    @Test(description = "Test for different objects in JSON array - inline/closed")
    public void testForDifferentObjectsInJsonArrayInLineClosed() throws IOException {
        runPositiveTest(sample6Json, sample6TypeDescClosedBal, "", true, true, false);
    }

    @Test(description = "Test for different objects in JSON array - inline/closed/forceFormatRecField")
    public void testForDifferentObjectsInJsonArrayInLineClosedForceFormatRecFields() throws IOException {
        runPositiveTest(sample6Json, sample6TypeDescClosedBal, "", true, true, true);
    }

    @Test(description = "Test for multi dimensional JSON array")
    public void testForMultiDimensionalJsonArray() throws IOException {
        runPositiveTest(sample7Json, sample7Bal, "", false, false, false);
    }

    @Test(description = "Test for complex JSON object")
    public void testForComplexJsonObject() throws IOException {
        runPositiveTest(sample8Json, sample8Bal, "", false, false, false);
    }

    @Test(description = "Test for complex JSON object - inline")
    public void testForComplexJsonObjectInLIne() throws IOException {
        runPositiveTest(sample8Json, sample8TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for many types in JSON array")
    public void testForMultipleItemsJsonArray() throws IOException {
        runPositiveTest(sample9Json, sample9Bal, "", false, false, false);
    }

    @Test(description = "Test for many types in JSON array - inline")
    public void testForMultipleItemsJsonArrayInLIne() throws IOException {
        runPositiveTest(sample9Json, sample9TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for JSON array of objects")
    public void testForJsonArrayOfObjects() throws IOException {
        runPositiveTest(sample10Json, sample10Bal, "", false, false, false);
    }

    @Test(description = "Test for JSON array of objects - inline")
    public void testForJsonArrayOfObjectsInLIne() throws IOException {
        runPositiveTest(sample10Json, sample10TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for JSON field names with special characters")
    public void testForJsonFieldsOfSpecialChars() throws IOException {
        runPositiveTest(sample11Json, sample11Bal, "", false, false, false);
    }

    @Test(description = "Test for Invalid JSON")
    public void testForInvalidJson() throws IOException {
        String jsonFileContent = Files.readString(sample12Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper.convert(
                        jsonFileContent, "", false, false, false, null, null, false).getDiagnostics();
        String diagnosticMessage =
                "Provided JSON is invalid : Unterminated object at line 15 column 8 path $.friend.address.city";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for JSON with a fieldName similar to recordName")
    public void testForSimilarRecordNameAndFieldName() throws IOException {
        String jsonFileContent = Files.readString(sample13Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper.convert(
                        jsonFileContent, "Person", false, false, false, null, null, false).getDiagnostics();
        String diagnosticMessage = "Provided record name 'Person' conflicts with the other generated records. " +
                "Consider providing a different name.";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for JSON with user defined record name")
    public void testForUserDefinedRecordName() throws IOException {
        runPositiveTest(sample3Json, sample3PersonBal, "Person", false, false, false);
    }

    @Test(description = "Test for JSON with user defined record name - inline")
    public void testForUserDefinedRecordNameInLIne() throws IOException {
        runPositiveTest(sample3Json, sample3PersonTypeDescBal, "Person", true, false, false);
    }

    @Test(description = "Test for JSON with user defined record name with special chars")
    public void testForUserDefinedRecordNameWithSpecialChars() throws IOException {
        runPositiveTest(sample3Json, sample3SpecialCharBal, "Special Person", false, false, false);
    }

    @Test(description = "Test for nested JSON with same recurring field")
    public void testForJsonWithRecurringFieldName() throws IOException {
        runPositiveTest(sample14Json, sample14Bal, "", false, false, false);
    }

    @Test(description = "Test for nested JSON with same recurring field to generate inline record")
    public void testForJsonWithRecurringFieldNameToGenerateInlineRecord() throws IOException {
        String jsonFileContent = Files.readString(sample14Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper.convert(
                jsonFileContent, "", true, false, false, null, null, false).getDiagnostics();
        String diagnosticMessage = "Proper inline record cannot be generated due to the nested structure " +
                "of the JSON. This will cause infinite record nesting. Consider renaming field 'items'.";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for nested JSON with same recurring field to generate inline record - 1")
    public void testForJsonWithRecurringFieldNameToGenerateInlineRecord1() throws IOException {
        String jsonFileContent = Files.readString(sample16Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper.convert(
                jsonFileContent, "", true, false, false, null, null, false).getDiagnostics();
        String diagnosticMessage = "Proper inline record cannot be generated due to the nested structure " +
                "of the JSON. This will cause infinite record nesting. Consider renaming field 'productRef'.";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for JSON array of nested objects - inline")
    public void testForJsonArrayOfNestedObjectsInLIne() throws IOException {
        runPositiveTest(sample15Json, sample15TypeDescBal, "", true, false, false);
    }

    @Test(description = "Test for JSON with optional fields")
    public void testForJsonWithOptionalFields() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(
                jsonFileContent, "", false, false, false, null, null, true).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample16Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test Choreo Transformation and Data Mapping Payloads")
    public void testChoreoTransPayloads() throws IOException {
        Map<Path, Path> samples = new HashMap<>();
        for (int i = 0; i <= 3; i++) {
            Path jsonInputPath = RES_DIR.resolve("json/ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_input.json", i));
            Path balInputPath = RES_DIR.resolve("ballerina/ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_input.bal", i));
            Path jsonOutputPath = RES_DIR.resolve("json/ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_output.json", i));
            Path balOutputPath = RES_DIR.resolve("ballerina/ChoreoTransPayloads")
                    .resolve(String.format("sample_%d_output.bal", i));
            samples.put(jsonInputPath, balInputPath);
            samples.put(jsonOutputPath, balOutputPath);
        }
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            JsonToRecordResponse jsonToRecordResponse = JsonToRecordMapper.convert(
                    jsonFileContent, null, false, false, false, null, null, false);
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
        JsonToRecordResponse response = JsonToRecordMapper.convert(
                jsonFileContent, null, false, false, false, invalidBalFile.toUri().toString(), null, false);
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample2Bal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for conflicting record names in single bal file project")
    public void testForConflictingRecordNamesInSingleBalFileProject() throws IOException {
        Map<String, Map.Entry<Path, Path>> existingRecordsToJsonSamples = Map.of(
            "sample_4.bal", Map.entry(sample4Bal, sample4Json),
            "sample_8.bal", Map.entry(sample8Bal, sample8Json),
            "sample_10.bal", Map.entry(sample10Bal, sample10Json),
            "sample_11.bal", Map.entry(sample11Bal, sample11Json)
        );
        Map<String, Map.Entry<Path, Path>> existingRecordsToJsonTypeDescSamples = Map.of(
            "sample_4_type_desc.bal", Map.entry(sample4Bal, sample4Json),
            "sample_9_type_desc.bal", Map.entry(sample9Bal, sample9Json),
            "sample_10_type_desc.bal", Map.entry(sample10Bal, sample10Json)
        );

        for (Map.Entry<String, Map.Entry<Path, Path>> entry : existingRecordsToJsonSamples.entrySet()) {
            Path jsonFilePath = entry.getValue().getValue();
            Path balFilePath = RES_DIR.resolve("project/assert/singleFileProject").resolve(entry.getKey());
            String balExistingFilePath = entry.getValue().getKey().toUri().toString();
            String jsonFileContent = Files.readString(jsonFilePath);
            JsonToRecordResponse jsonToRecordResponse = JsonToRecordMapper.convert(
                    jsonFileContent, null, false, false, false, balExistingFilePath, null, false);
            String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(balFilePath).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
        for (Map.Entry<String, Map.Entry<Path, Path>> entry : existingRecordsToJsonTypeDescSamples.entrySet()) {
            Path jsonFilePath = entry.getValue().getValue();
            Path balFilePath = RES_DIR.resolve("project/assert/singleFileProject").resolve(entry.getKey());
            String balExistingFilePath = entry.getValue().getKey().toUri().toString();
            String jsonFileContent = Files.readString(jsonFilePath);
            JsonToRecordResponse jsonToRecordResponse = JsonToRecordMapper.convert(
                    jsonFileContent, null, true, false, false, balExistingFilePath, null, false);
            String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(balFilePath).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
    }

    @Test(description = "Test for conflicting parent record name")
    public void testForConflictingParentRecordName() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper.convert(
                jsonFileContent, "Friend", false, false, false, sample2Bal.toUri().toString(), null, false)
                .getDiagnostics();
        String diagnosticMessage = "Provided record name 'Friend' conflicts with already existing records. " +
                "Consider providing a different name.";
        Assert.assertEquals(diagnostics.size(), 1);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage);
    }

    @Test(description = "Test for conflicting record names in bal project default module")
    public void testForConflictingRecordNamesInBalProjectDefault() throws IOException {
        String jsonFileContent = Files.readString(sample2Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(
                jsonFileContent, null, false, false, false, balProjectFileDefaultModule.toUri().toString(), null, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(balProjectFileDefaultModuleAssert).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for conflicting record names in bal project non-default module")
    public void testForConflictingRecordNamesInBalProjectNonDefault() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        String generatedCodeBlock = JsonToRecordMapper.convert(
                jsonFileContent, "", false, false, false, balProjectFileUtilModule.toUri().toString(), null, false)
                .getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(balProjectFileUtilModuleAssert).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test for conflicting record name rename diagnostics")
    public void testForConflictingRecordNameRenameDiagnostics() throws IOException {
        String jsonFileContent = Files.readString(sample6Json);
        List<JsonToRecordMapperDiagnostic> diagnostics = JsonToRecordMapper.convert(jsonFileContent, "NewRecord",
                        false, false, false, balProjectFileUtilModule.toUri().toString(), null, false)
                .getDiagnostics();
        String diagnosticMessage0 = "The record name 'State' is renamed as 'State_01'. " +
                "Consider rename it back to a meaningful name.";
        String diagnosticMessage1 = "The record name 'Author' is renamed as 'Author_01'. " +
                "Consider rename it back to a meaningful name.";
        Assert.assertEquals(diagnostics.size(), 2);
        Assert.assertEquals(diagnostics.get(0).message(), diagnosticMessage0);
        Assert.assertEquals(diagnostics.get(1).message(), diagnosticMessage1);
    }

    private void runPositiveTest(Path json, Path bal, String recordName, boolean isRecordTypeDesc, 
                                 boolean closed, boolean forceFormatRecField)
            throws IOException {
        String jsonFileContent = Files.readString(json);
        String generatedCodeBlock = JsonToRecordMapper.convert(
                jsonFileContent, recordName, isRecordTypeDesc, closed, forceFormatRecField, null, null, false)
                .getCodeBlock();
        String expectedCodeBlock = Files.readString(bal);
        if (!forceFormatRecField) {
            generatedCodeBlock = generatedCodeBlock.replaceAll("\\s+", "");
            expectedCodeBlock = expectedCodeBlock.replaceAll("\\s+", "");
        }
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }
}
