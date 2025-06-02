/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.bir;

import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * This class contains unit tests to check the typedesc creation for constructors.
 *
 * @since 2201.12.0
 */
public class TypeDescBIRTest {
    private BCompileUtil.BIRCompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.generateBIR("test-src/bir/typedesc_bir.bal");
    }

    @Test(description = "Test record field set")
    public void testRecordFieldSet() {
        String actual = BIREmitter.emitModule(result.getExpectedBIR());
        String expected = null;
        try {
            expected = readFile("typedesc_bir");
        } catch (IOException e) {
            Assert.fail("Failed to read the expected BIR file: typedesc_bir");
        }
        Assert.assertEquals(actual, expected);
    }

    private String readFile(String name) throws IOException {
        Path filePath = Path.of("src", "test", "resources", "test-src", "bir", "bir-dump", name).toAbsolutePath();
        if (Files.exists(filePath)) {
            StringBuilder contentBuilder = new StringBuilder();
            try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            }
            return contentBuilder.toString().trim();
        }
        Assert.fail("Expected BIR file not found for test: " + name);
        return null;
    }
}
