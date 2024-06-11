/*
 *   Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org).
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.ballerinalang.test.bir;

import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class contains unit tests to validate various desugaring/optimizations on records produce the expected BIR.
 *
 * @since 2201.9.0
 */
public class RecordDesugarTest {

    private BIREmitter birEmitter;
    private BCompileUtil.BIRCompileResult result;

    @BeforeClass
    public void setup() {
        birEmitter = BIREmitter.getInstance(new CompilerContext());
        result = BCompileUtil.generateBIR("test-src/bir/record_desugar.bal");
    }

    @Test(description = "Test record field set")
    public void testRecordFieldSet() {
        List<String> functions = Arrays.asList("setRequiredField", "setNillableField", "setOptionalField");
        result.getExpectedBIR().functions.stream().filter(function -> functions.contains(function.name.value))
                .forEach(this::assertFunctions);
    }

    private void assertFunctions(BIRNode.BIRFunction function) {
        String actual = BIREmitter.emitFunction(function, 0);
        String expected = null;
        try {
            expected = readFile(function.name.value);
        } catch (IOException e) {
            Assert.fail("Failed to read the expected BIR file for function: " + function.name.value, e);
        }
        Assert.assertEquals(actual, expected);
    }

    private String readFile(String name) throws IOException {
        // The files in the bir-dump folder are named with the function name and contain the expected bir dump for
        // the function
        Path filePath = Paths.get("src", "test", "resources", "test-src", "bir", "bir-dump", name).toAbsolutePath();
        if (Files.exists(filePath)) {
            StringBuilder contentBuilder = new StringBuilder();
            try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
                stream.forEach(s -> contentBuilder.append(s).append("\n"));
            }

            return contentBuilder.toString().trim();
        }
        Assert.fail("Expected BIR file not found for function: " + name);
        return null;
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
