/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.bala.bir;

import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for loading a package from its BIR when a dependency of it is compiled from sources.
 * Verifies that the types generated for the source-compiled dependency, which are referred to
 * by their generated names in the BIR, are resolved.
 *
 * @since 2201.14.3
 */
public class BIRGeneratedTypeBalaTest {

    private static final Path ORG_CACHE_PATH = Path.of("build").toAbsolutePath().normalize()
            .resolve("repo").resolve("cache").resolve("testorg");

    @Test(dataProvider = "generatedTypeKinds")
    public void testLoadBIRWithSourceCompiledDependency(String kind) throws IOException {
        String lib = "bir_" + kind + "_lib";
        String dep = "bir_" + kind + "_dep";
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_" + lib);
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_" + dep);

        // Remove the caches of the library to compile it from sources. The dependent package is loaded from its BIR.
        assertTrue(Files.exists(ORG_CACHE_PATH.resolve(dep).resolve("1.0.0").resolve("bir").resolve(dep + ".bir")),
                "BIR of " + dep + " is not cached");
        ProjectUtils.deleteDirectory(ORG_CACHE_PATH.resolve(lib));

        CompileResult result = BCompileUtil.compile("test-src/bala/test_bala/bir/bir_" + kind + "_test.bal");
        assertEquals(result.getErrorCount(), 0);
        BRunUtil.invoke(result, "testLoadBIRWithSourceCompiledDependency");
    }

    @DataProvider(name = "generatedTypeKinds")
    public Object[] generatedTypeKinds() {
        return new Object[]{
                // anonymous record types generated for the values of field annotations
                "anon_type",
                // immutable types generated for readonly intersections
                "readonly_type"
        };
    }
}
