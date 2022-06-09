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

/**
 * Tests for JsonToRecordDirectConverter.
 */
public class JsonToRecordDirectConverterTests {
    @Test(description = "Test with basic json schema string")
    public void testBasicSchema() throws FormatterException {
//        String jsonFileContent = Files.readString(basicSchemaJson);
        String generatedCodeBlock = JsonToRecordDirectConverter.convert("").getCodeBlock();
        System.out.println(generatedCodeBlock);
//        String expectedCodeBlock = Files.readString(basicSchemaBal).replaceAll("\\s+", "");
        Assert.assertEquals(generatedCodeBlock, "typeTestrecord{};");
    }
}
