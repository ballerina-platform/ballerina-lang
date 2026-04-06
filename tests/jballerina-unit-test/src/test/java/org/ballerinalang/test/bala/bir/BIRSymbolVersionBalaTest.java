/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.bala.bir;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

/**
 * Tests for BIR symbol version resolution when loading bala files.
 * Verifies that package IDs for transitive dependencies are resolved
 * to the version in the current import symbols, not the version embedded
 * in the BIR constant pool.
 *
 * @since 2201.12.x
 */
public class BIRSymbolVersionBalaTest {

    private CompileResult result;

    private static final String LIB_PROJECT_PATH =
            "test-src/bala/test_projects/test_project_bir_symbol_version_lib";

    @BeforeClass
    public void setup() throws IOException {
        BCompileUtil.compileAndCacheBala(LIB_PROJECT_PATH);
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_bir_symbol_version_dep");

        Path libTomlPath = Path.of("src/test/resources").toAbsolutePath().normalize()
                .resolve(LIB_PROJECT_PATH).resolve("Ballerina.toml");
        String originalContent = Files.readString(libTomlPath);
        String updatedContent = originalContent.replace("version = \"1.0.0\"", "version = \"1.0.1\"");
        Files.writeString(libTomlPath, updatedContent);
        try {
            BCompileUtil.compileAndCacheBala(LIB_PROJECT_PATH);
        } finally {
            Files.writeString(libTomlPath, originalContent);
        }

        result = BCompileUtil.compile("test-src/bala/test_bala/bir/bir_symbol_version_test.bal");
    }

    @Test
    public void testBIRSymbolVersionResolution() {
        assertEquals(result.getErrorCount(), 0);
        BRunUtil.invoke(result, "testBIRSymbolVersionResolution");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
