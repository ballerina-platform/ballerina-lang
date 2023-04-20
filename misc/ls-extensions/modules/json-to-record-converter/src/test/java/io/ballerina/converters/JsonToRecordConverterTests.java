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

import io.ballerina.converters.exception.JsonToRecordConverterException;
import io.ballerina.converters.util.ConverterUtils;
import io.ballerina.jsonmapper.diagnostic.JsonToRecordMapperDiagnostic;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Tests for JsonToRecordConverter.
 */
public class JsonToRecordConverterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    private static final String JsonToRecordService = "jsonToRecord/convert";

    private final Path basicSchemaJson = RES_DIR.resolve("json")
            .resolve("basic_schema.json");
    private final Path basicSchemaBal = RES_DIR.resolve("ballerina")
            .resolve("basic_schema.bal");

    private final Path invalidSchemaJson = RES_DIR.resolve("json")
            .resolve("invalid_json_schema.json");

    private final Path invalidJson = RES_DIR.resolve("json")
            .resolve("invalid_json.json");

    private final Path nullJson = RES_DIR.resolve("json")
            .resolve("null_json.json");

    private final Path basicObjectJson = RES_DIR.resolve("json")
            .resolve("basic_object.json");
    private final Path basicObjectBal = RES_DIR.resolve("ballerina")
            .resolve("basic_object.bal");

    private final Path nestedSchemaJson = RES_DIR.resolve("json")
            .resolve("nested_schema.json");
    private final Path nestedSchemaBal = RES_DIR.resolve("ballerina")
            .resolve("nested_schema.bal");

    private final Path nestedObjectJson = RES_DIR.resolve("json")
            .resolve("nested_object.json");
    private final Path nestedObjectBal = RES_DIR.resolve("ballerina")
            .resolve("nested_object.bal");

    private final Path nullObjectJson = RES_DIR.resolve("json")
            .resolve("null_object.json");
    private final Path nullObjectBal = RES_DIR.resolve("ballerina")
            .resolve("null_object.bal");
    private final Path nullObjectDirectConversionBal = RES_DIR.resolve("ballerina")
            .resolve("null_object_direct_conversion.bal");

    private final Path sample1Json = RES_DIR.resolve("json")
            .resolve("sample_1.json");
    private final Path sample1Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_1.bal");

    private final Path sample2Json = RES_DIR.resolve("json")
            .resolve("sample_2.json");
    private final Path sample2Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_2.bal");

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
    private final Path sample7TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_7_type_desc.bal");

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
    private final Path sample10WithoutConflictBal = RES_DIR.resolve("ballerina")
            .resolve("sample_10_without_conflict.bal");

    private final Path sample11Json = RES_DIR.resolve("json")
            .resolve("sample_11.json");
    private final Path sample11TypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("sample_11_type_desc.bal");

    private final Path crlfJson = RES_DIR.resolve("json")
            .resolve("crlf.json");
    private final Path crlfBal = RES_DIR.resolve("ballerina")
            .resolve("from_crlf.bal");

    private final Path nestedObjectTypeDescBal = RES_DIR.resolve("ballerina")
            .resolve("nested_object_type_desc.bal");

    private final Path nestedObjectClosedDescBal = RES_DIR.resolve("ballerina")
            .resolve("nested_object_closed_desc.bal");

    private final Path closedRecordBal = RES_DIR.resolve("ballerina")
            .resolve("closed_record.bal");

    private final Path emptyArrayJson = RES_DIR.resolve("json")
            .resolve("empty_array.json");

    @Test(description = "Test with basic json schema string")
    public void testBasicSchema() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(basicSchemaJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "", false,
                false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(basicSchemaBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with basic json object")
    public void testBasicJson() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(basicObjectJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "", false,
                false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(basicObjectBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test get type record descriptor nested objects")
    public void testNestedSchema() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(nestedSchemaJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "", false,
                false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(nestedSchemaBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test get record type descriptor for basic objects")
    public void testBasicObjectForRecordTypeDesc() throws
            JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(basicObjectJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "",
                true, false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(basicObjectBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test type descriptor code for nested objects")
    public void testTypeDescCodeForNestedObjects() throws
            JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(nestedObjectJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "",
                true, false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(nestedObjectTypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test get closed type descriptor record descriptor nested objects")
    public void testClosedNestedSchemaForRecordTypeDesc() throws
            JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(nestedObjectJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "", true,
                true, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(nestedObjectClosedDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test json with nested objects")
    public void testNestedJson() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(nestedObjectJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "", false,
                false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(nestedObjectBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test reference type extraction")
    public void testRefTypeExtraction() throws JsonToRecordConverterException {
        String refString = "#/definitions/address/state";
        String extracted = ConverterUtils.extractReferenceType(refString);
        Assert.assertEquals(extracted, "state");
    }

    @Test(description = "Test with CRLF formatted json file")
    public void testCRLFJson() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(crlfJson);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "", false,
                false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(crlfBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with sample json objects")
    public void testSamples() throws JsonToRecordConverterException, IOException, FormatterException {
        Map<Path, Path> samples = new HashMap<>();
        samples.put(sample1Json, sample1Bal);
        samples.put(sample2Json, sample2Bal);
        samples.put(sample3Json, sample3Bal);
        samples.put(sample4Json, sample4Bal);
        samples.put(sample5Json, sample5Bal);
        samples.put(sample6Json, sample6Bal);
        samples.put(sample8Json, sample8Bal);
        samples.put(sample9Json, sample9Bal);
        samples.put(sample10Json, sample10Bal);
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "",
                    false, false, false).getCodeBlock().
                    replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
    }

    @Test(description = "Test with sample json objects for fields")
    public void testFieldForSamples() throws JsonToRecordConverterException, IOException, FormatterException {
        Map<Path, Path> samples = new HashMap<>();
        samples.put(sample3Json, sample3TypeDescBal);
        samples.put(sample6Json, sample6TypeDescBal);
        samples.put(sample8Json, sample8TypeDescBal);
        samples.put(sample9Json, sample9TypeDescBal);
        samples.put(sample10Json, sample10TypeDescBal);
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "Person",
                    true, false, false).getCodeBlock().replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
    }

    @Test(description = "Test a sample json for a closed record")
    public void testClosedRecord() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(sample7Json);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "",
                false, true, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(closedRecordBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with sample json for a closed record and objects for fields")
    public void testFieldForClosedRecord() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(sample7Json);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "",
                true, false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample7TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with null objects")
    public void testNullObject() throws IOException, FormatterException {
        String jsonFileContent = Files.readString(nullObjectJson);
        try {
            JsonToRecordConverter.convert(jsonFileContent, "",
                            true, false, false).getCodeBlock()
                    .replaceAll("\\s+", "");
            Assert.assertTrue(true);
        } catch (JsonToRecordConverterException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Unsupported, Null or Missing type in Json");
        }
    }

    @Test(description = "Test with sample json for a closed record and objects for fields")
    public void testEmptyArray() throws IOException, FormatterException {
        String jsonFileContent = Files.readString(emptyArrayJson);
        try {
            JsonToRecordConverter.convert(jsonFileContent, "",
                            true, false, false).getCodeBlock()
                    .replaceAll("\\s+", "");
            Assert.assertTrue(true);
        } catch (JsonToRecordConverterException e) {
            Assert.fail();
        }
    }

    @Test(description = "Test JSON2Record endpoint")
    public void testJSON2RecordService() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(basicObjectJson);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, null);
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(basicObjectBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test JSON2Record endpoint for null object")
    public void testJSON2RecordServiceNullObjects() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(nullObjectJson);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, null);
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(nullObjectDirectConversionBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test JSON2Record endpoint for JSON Schema")
    public void testJSON2RecordServiceJSONSchema() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(basicSchemaJson);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, null);
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(basicSchemaBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test JSON2Record endpoint for Invalid JSON Schema")
    public void testJSON2RecordServiceInvalidJSONSchema() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(invalidSchemaJson);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, null);
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = "";
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test JSON2Record endpoint for Invalid JSON Object")
    public void testJSON2RecordServiceInvalidJSONObject() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(invalidJson);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, null);
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        List<String> generatedDiagnosticMessages = response.getDiagnostics().stream()
                .map(JsonToRecordMapperDiagnostic::toString).collect(Collectors.toList());
        List<String> expectedCDiagnosticMessages = List.of("[ERROR] Provided JSON is invalid : " +
                "Unterminated object at line 5 column 4 path $.position");
        Assert.assertEquals(generatedDiagnosticMessages, expectedCDiagnosticMessages);
    }

    @Test(description = "Test JSON2Record endpoint for null JSON")
    public void testJSON2RecordServiceNullJSON() throws IOException, ExecutionException, InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(nullJson);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, null);
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        List<String> generatedDiagnosticMessages = response.getDiagnostics().stream()
                .map(JsonToRecordMapperDiagnostic::toString).collect(Collectors.toList());
        List<String> expectedCDiagnosticMessages =
                List.of("[ERROR] Provided JSON is unsupported. It may be null or have missing types.");
        Assert.assertEquals(generatedDiagnosticMessages, expectedCDiagnosticMessages);
    }

    @Test(description = "Test JSON2Record endpoint for conflicting record names")
    public void testJSON2RecordServiceConflictingRecNames() throws IOException, ExecutionException,
            InterruptedException {
        Endpoint serviceEndpoint = TestUtil.initializeLanguageSever();
        String jsonString = Files.readString(sample10Json);

        JsonToRecordRequest request = new JsonToRecordRequest(jsonString, null,
                false, false, false, sample10Bal.toUri().toString());
        CompletableFuture<?> result = serviceEndpoint.request(JsonToRecordService, request);
        io.ballerina.jsonmapper.JsonToRecordResponse response =
                (io.ballerina.jsonmapper.JsonToRecordResponse) result.get();
        String generatedCodeBlock = response.getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample10WithoutConflictBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test null and empty array field type extraction")
    public void testNullAndEmptyArray() throws JsonToRecordConverterException, IOException, FormatterException {
        String jsonFileContent = Files.readString(sample11Json);
        String generatedCodeBlock = JsonToRecordConverter.convert(jsonFileContent, "",
                true, false, false).getCodeBlock().replaceAll("\\s+", "");
        String expectedCodeBlock = Files.readString(sample11TypeDescBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }
}
