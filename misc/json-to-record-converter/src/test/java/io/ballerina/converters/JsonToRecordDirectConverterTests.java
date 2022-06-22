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

import io.ballerina.converters.exception.JsonToRecordDirectConverterException;
import org.ballerinalang.formatter.core.FormatterException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

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

    private final Path sample7Json = RES_DIR.resolve("json")
            .resolve("sample_7.json");
    private final Path sample7Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_7.bal");

    private final Path sample8Json = RES_DIR.resolve("json")
            .resolve("sample_8.json");
    private final Path sample8Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_8.bal");

    private final Path sample9Json = RES_DIR.resolve("json")
            .resolve("sample_9.json");
    private final Path sample9Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_9.bal");

    private final Path sample10Json = RES_DIR.resolve("json")
            .resolve("sample_10.json");
    private final Path sample10Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_10.bal");

    private final Path sample11Json = RES_DIR.resolve("json")
            .resolve("sample_11.json");
    private final Path sample11Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_11.bal");

    private final Path sample12Json = RES_DIR.resolve("json")
            .resolve("sample_12.json");
    private final Path sample12Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_12.bal");

    private final Path sample13Json = RES_DIR.resolve("json")
            .resolve("sample_13.json");
    private final Path sample13Bal = RES_DIR.resolve("ballerina")
            .resolve("sample_13.bal");

    @Test(description = "Test all sample JSON values")
    public void testSamples() throws IOException, FormatterException, JsonToRecordDirectConverterException {
        Map<Path, Path> samples = new HashMap<>();
        samples.put(sample0Json, sample0Bal);
        samples.put(sample1Json, sample1Bal);
        samples.put(sample2Json, sample2Bal);
        samples.put(sample3Json, sample3Bal);
        samples.put(sample4Json, sample4Bal);
        samples.put(sample5Json, sample5Bal);
        samples.put(sample6Json, sample6Bal);
        samples.put(sample7Json, sample7Bal);
        samples.put(sample8Json, sample8Bal);
        samples.put(sample9Json, sample9Bal);
        samples.put(sample10Json, sample10Bal);
        samples.put(sample11Json, sample11Bal);
        samples.put(sample12Json, sample12Bal);
        samples.put(sample13Json, sample13Bal);
        for (Map.Entry<Path, Path> sample : samples.entrySet()) {
            String jsonFileContent = Files.readString(sample.getKey());
            String generatedCodeBlock =
                    JsonToRecordDirectConverter.convert(jsonFileContent, null, false, false).getCodeBlock().
                    replaceAll("\\s+", "");
            String expectedCodeBlock = Files.readString(sample.getValue()).replaceAll("\\s+", "");
            Assert.assertEquals(generatedCodeBlock, expectedCodeBlock);
        }
    }
}
