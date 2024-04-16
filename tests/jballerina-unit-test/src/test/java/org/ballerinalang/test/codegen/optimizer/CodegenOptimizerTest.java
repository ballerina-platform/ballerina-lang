/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.ballerinalang.test.codegen.optimizer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodegenOptimizerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodegenOptimizerTest.class);
    private static final String RES_DIR = "src/test/resources/test-src/codegen-optimizer";
    private static final String PROJECT_DIR = RES_DIR + "/projects/unusedFunctionDetection";
    private static final Path OPTIMIZATION_REPORT_JSON =
            Path.of(PROJECT_DIR).toAbsolutePath().resolve("target").resolve("codegen_optimization_report.json");
    private static final Path EXPECTED_JSON_PATH =
            Path.of(RES_DIR).toAbsolutePath().resolve("json-files").resolve("unusedFunctionDetection.json");
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compileOptimized("test-src/codegen-optimizer/projects/unusedFunctionDetection");
    }

    @Test
    public void testJsonOutput() {
        JsonObject actualJsonObject = fileContentAsObject(OPTIMIZATION_REPORT_JSON.normalize());
        JsonObject expectedJsonObject = fileContentAsObject(EXPECTED_JSON_PATH.toAbsolutePath().normalize());
        Assert.assertEquals(actualJsonObject, expectedJsonObject);
    }

    private static JsonObject fileContentAsObject(Path filePath) {
        String contentAsString = "";
        try {
            contentAsString = new String(Files.readAllBytes(filePath));
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage());
        }
        return JsonParser.parseString(contentAsString).getAsJsonObject();
    }
}
