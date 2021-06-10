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

import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.converters.exception.ConverterException;
import io.ballerina.converters.util.ConverterUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for JsonToRecordConverter.
 */
public class JsonToRecordConverterTests {
    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();
    ArrayList<TypeDefinitionNode> records;

    private final Path basicSchemaJson = RES_DIR.resolve("json")
            .resolve("basic_schema.json");
    private final Path basicSchemaBal = RES_DIR.resolve("ballerina")
            .resolve("basic_schema.bal");

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

    private final Path crlfJson = RES_DIR.resolve("json")
            .resolve("crlf.json");
    private final Path crlfBal = RES_DIR.resolve("ballerina")
            .resolve("from_crlf.bal");

    private final Path emptyBal = RES_DIR.resolve("ballerina")
            .resolve("empty.bal");

    // write the codeblock to an empty .bal file
    public void writeToEmpty(String codeBlock) {
        Charset charset = StandardCharsets.US_ASCII;
        try (BufferedWriter writer = Files.newBufferedWriter(emptyBal, charset)) {
            writer.write(codeBlock, 0, codeBlock.length());
        } catch (IOException x) {
            System.err.format("IOException: %s%n", x);
        }
    }

    @Test(description = "Test with basic json schema string")
    public void testBasicSchema() throws ConverterException, IOException {
        String jsonFileContent = Files.readString(basicSchemaJson);
        records = JsonToRecordConverter.convert(jsonFileContent);
        String generatedCodeBlock = records.stream().map(Object::toString)
                .collect(Collectors.joining(""));
        String expectedCodeBlock = Files.readString(basicSchemaBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with basic json object")
    public void testBasicJson() throws ConverterException, IOException {
        String jsonFileContent = Files.readString(basicObjectJson);
        records = JsonToRecordConverter.convert(jsonFileContent);
        String generatedCodeBlock = records.stream().map(Object::toString)
                .collect(Collectors.joining(""));
        String expectedCodeBlock = Files.readString(basicObjectBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test schema with nested objects")
    public void testNestedSchema() throws ConverterException, IOException {
        String jsonFileContent = Files.readString(nestedSchemaJson);
        records = JsonToRecordConverter.convert(jsonFileContent);
        String generatedCodeBlock = records.stream().map(Object::toString)
                .collect(Collectors.joining(""));
        String expectedCodeBlock = Files.readString(nestedSchemaBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test json with nested objects")
    public void testNestedJson() throws ConverterException, IOException {
        String jsonFileContent = Files.readString(nestedObjectJson);
        records = JsonToRecordConverter.convert(jsonFileContent);
        String generatedCodeBlock = records.stream().map(Object::toString)
                .collect(Collectors.joining(""));
        String expectedCodeBlock = Files.readString(nestedObjectBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test reference type extraction")
    public void testRefTypeExtraction() throws ConverterException {
        String refString = "#/definitions/address/state";
        String extracted = ConverterUtils.extractReferenceType(refString);
        Assert.assertEquals(extracted, "state");
    }

    @Test(description = "Test with CRLF formatted json file")
    public void testCRLFJson() throws ConverterException, IOException {
        String jsonFileContent = Files.readString(crlfJson);
        records = JsonToRecordConverter.convert(jsonFileContent);
        String generatedCodeBlock = records.stream().map(Object::toString)
                .collect(Collectors.joining(""));
        String expectedCodeBlock = Files.readString(crlfBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
    }

    @Test(description = "Test with sample json objects")
    public void testSamples() throws ConverterException, IOException {
        Map<Path, Path> samples = new HashMap<>();
        samples.put(sample1Json, sample1Bal);
        samples.put(sample2Json, sample2Bal);
        samples.put(sample3Json, sample3Bal);
        samples.put(sample4Json, sample4Bal);
        samples.put(sample5Json, sample5Bal);
        samples.put(sample6Json, sample6Bal);
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            records = JsonToRecordConverter.convert(jsonFileContent);
            String generatedCodeBlock = records.stream().map(Object::toString)
                    .collect(Collectors.joining(""));
            String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
    }
}
