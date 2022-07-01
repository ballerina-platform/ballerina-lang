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

package io.ballerina.converters;

import io.ballerina.converters.diagnostic.JsonToRecordDirectConverterDiagnostic;
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
 * Tests for JsonToRecordDirectConverter.
 */
public class JsonToRecordDirectConverterTests {

    private static final Path RES_DIR = Paths.get("src/test/resources/").toAbsolutePath();

    @Test(description = "Test all sample JSON values")
    public void testSamples() throws IOException {
        Map<Path, Path> samples = new HashMap<>();
        for (int i = 0; i <= 13; i++) {
            Path jsonPath = RES_DIR.resolve("json").resolve(String.format("sample_%d.json", i));
            Path balPath = RES_DIR.resolve("ballerina").resolve(String.format("sample_%d.bal", i));
            samples.put(jsonPath, balPath);
        }
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            JsonToRecordResponse jsonToRecordResponse =
                    JsonToRecordDirectConverter.convert(jsonFileContent, null, false, false);
            if (jsonToRecordResponse.getCodeBlock() != null) {
                String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
                String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
                Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
            }
            List<JsonToRecordDirectConverterDiagnostic> diagnostics = jsonToRecordResponse.getDiagnostics();
            for (JsonToRecordDirectConverterDiagnostic diagnostic : diagnostics) {
                //TODO: react when there is a diagnostic message.
            }
        }
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
                    JsonToRecordDirectConverter.convert(jsonFileContent, null, false, false);
            if (jsonToRecordResponse.getCodeBlock() != null) {
                String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
                String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
                Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
            }
            List<JsonToRecordDirectConverterDiagnostic> diagnostics = jsonToRecordResponse.getDiagnostics();
            for (JsonToRecordDirectConverterDiagnostic diagnostic : diagnostics) {
                //TODO: react when there is a diagnostic message.
            }
        }
    }

    @Test(description = "Test all sample JSON values with TypeDesc")
    public void testSamplesWithTypeDesc() throws IOException {
        Map<Path, Path> samples = new HashMap<>();
        for (int i = 0; i <= 13; i++) {
            Path jsonPath = RES_DIR.resolve("json").resolve(String.format("sample_%d.json", i));
            Path balPath = RES_DIR.resolve("ballerina").resolve(String.format("sample_%d_type_desc.bal", i));
            samples.put(jsonPath, balPath);
        }
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            JsonToRecordResponse jsonToRecordResponse =
                    JsonToRecordDirectConverter.convert(jsonFileContent, null, true, false);
            if (jsonToRecordResponse.getCodeBlock() != null) {
                String generatedCodeBlock = jsonToRecordResponse.getCodeBlock().replaceAll("\\s+", "");
                String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
                Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
            }
            List<JsonToRecordDirectConverterDiagnostic> diagnostics = jsonToRecordResponse.getDiagnostics();
            for (JsonToRecordDirectConverterDiagnostic diagnostic : diagnostics) {
                //TODO: react when there is a diagnostic message.
            }
        }
    }
}
