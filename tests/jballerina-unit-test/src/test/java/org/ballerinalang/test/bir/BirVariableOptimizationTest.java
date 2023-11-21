/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.bir;

import org.ballerinalang.test.BCompileUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.optimizer.BIROptimizer;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * Test to confirm the functionality of the {@link BIROptimizer}.
 */
public class BirVariableOptimizationTest {
    private BIREmitter birEmitter;
    private BCompileUtil.BIRCompileResult result;

    @BeforeClass
    public void setup() {
        birEmitter = BIREmitter.getInstance(new CompilerContext());
        result = BCompileUtil.generateBIR("test-src/bir/biroptimizer.bal");
    }

    @Test(description = "Test the liveness analysis on functions")
    public void testFunctions() {
        result.getExpectedBIR().getFunctions().forEach(this::assertFunctions);
    }

    @Test(description = "Test the liveness analysis on attached functions")
    public void testAttachedFunctions() {
         result.getExpectedBIR().typeDefs.forEach(
                typeDefinition -> typeDefinition.attachedFuncs.forEach(this::assertFunctions));
    }

    private void assertFunctions(BIRNode.BIRFunction func) {
        String expectedBir = null;
        try {
            expectedBir = readFile(func.getName().getValue());
        } catch (IOException e) {
            Assert.fail("Failed when reading file", e);
        }
        if (!"".equals(expectedBir)) {
            String funcBir = birEmitter.emitFunction(func, 0);
            Assert.assertEquals(funcBir, expectedBir);
        }
    }

    private String readFile(String name) throws IOException {
        // The files in the bir-dump folder are named with the function name and contain the expected bir dump for
        // the function
        name = name.replaceAll("<", "").replaceAll(">", "");
        Path filePath = Paths.get("src", "test", "resources", "test-src", "bir", "bir-dump", name).toAbsolutePath();
        if (Files.exists(filePath)) {
            StringBuilder contentBuilder = new StringBuilder();

            Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8);
            stream.forEach(s -> contentBuilder.append(s).append("\n"));

            return contentBuilder.toString().trim();
        }
        return "";
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

