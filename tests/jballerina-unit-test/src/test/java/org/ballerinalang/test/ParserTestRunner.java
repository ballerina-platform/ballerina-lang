/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.test;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

/**
 * Test running parser tests through compiler phases.
 *
 * @since 2.0.0
 */
public class ParserTestRunner {

    private final Path ballerinaLangDir = Paths.get("").toAbsolutePath().getParent().getParent();
    private final Path parserDir = ballerinaLangDir.resolve("compiler").resolve("ballerina-parser");

    @Test(dataProvider = "parser-test-file-provider")
    public void test(String fileName, String path) {
        try {
            BCompileUtil.compile(path);
        } catch (Exception e) {
            Assert.fail("failed to compile parser test: \"" + fileName + "\"", e);
        }
    }

    public HashSet<String> skipList() {
        HashSet<String> hashSet = new HashSet<>();
        // Compiler should be able to compile any user input.
        // Therefore, when adding an item to the skip list, please create an issue.
        hashSet.add("func_def_source_08.bal");
        hashSet.add("match_stmt_source_13.bal");
        hashSet.add("method_call_expr_source_03.bal"); // issue #34620
        hashSet.add("method_call_expr_source_05.bal"); // issue #34620
        hashSet.add("float_literal_source_08.bal"); // issue #34620
        hashSet.add("float_literal_source_07.bal"); // issue #34620
        hashSet.add("resiliency_source_04.bal");  // issue #35795
        // Temporarily skip client decl. tests.
        hashSet.add("client_decl_stmt_source_01.bal");
        hashSet.add("client_decl_stmt_source_02.bal");
        hashSet.add("client_decl_stmt_source_03.bal");
        hashSet.add("client_decl_stmt_source_04.bal");
        hashSet.add("client_decl_stmt_source_05.bal");
        hashSet.add("client_decl_stmt_source_06.bal");
        hashSet.add("client_decl_stmt_source_07.bal");
        hashSet.add("client_decl_stmt_source_08.bal");
        hashSet.add("client_decl_stmt_source_09.bal");
        hashSet.add("client_decl_stmt_source_10.bal");
        hashSet.add("client_decl_stmt_source_11.bal");
        hashSet.add("client_decl_stmt_source_12.bal");
        hashSet.add("client_decl_stmt_source_13.bal");
        hashSet.add("client_decl_source_01.bal");
        hashSet.add("client_decl_source_02.bal");
        hashSet.add("client_decl_source_03.bal");
        hashSet.add("client_decl_source_04.bal");
        hashSet.add("client_decl_source_05.bal");
        hashSet.add("client_decl_source_06.bal");
        hashSet.add("client_decl_source_07.bal");
        hashSet.add("client_decl_source_08.bal");
        hashSet.add("client_decl_source_09.bal");
        hashSet.add("client_decl_source_10.bal");
        hashSet.add("client_decl_source_11.bal");
        hashSet.add("client_decl_source_12.bal");
        hashSet.add("client_decl_source_13.bal");
        return hashSet;
    }

    @DataProvider(name = "parser-test-file-provider")
    public Object[][] dataProvider() {
        HashSet<String> skippedTests = skipList();
        try {
            return Files.walk(parserDir.resolve("src").resolve("test").resolve("resources"))
                    .filter(path -> {
                        File file = path.toFile();
                        return file.isFile() && file.getName().endsWith(".bal")
                                && !skippedTests.contains(file.getName());
                    })
                    .map(path -> new Object[]{path.toFile().getName(), path.toString()})
                    .toArray(size -> new Object[size][2]);
        } catch (IOException e) {
            Assert.fail("Can't resolve parser tests", e);
            return new Object[0][];
        }
    }
}
