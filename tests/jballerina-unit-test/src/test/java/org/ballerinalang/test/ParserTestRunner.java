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
        // Following should be fixed
        // TODO: categorize and create issues
        hashSet.add("xml_template_source_24.bal");
        hashSet.add("explicit-new-with-object-keyword-with-multiple-args-negative02.bal");
        hashSet.add("explicit-new-with-object-keyword-with-one-arg-negative01.bal");
        hashSet.add("explicit-new-with-object-keyword-with-no-args.bal");
        hashSet.add("explicit-new-with-object-keyword-with-multiple-args.bal");
        hashSet.add("explicit-new-with-object-keyword-with-one-arg-negative02.bal");
        hashSet.add("explicit-new-with-object-keyword-with-multiple-args-negative01.bal");
        hashSet.add("explicit-new-with-object-keyword-with-one-args.bal");
        hashSet.add("func_def_source_08.bal");
        hashSet.add("find_node_test_1.bal");
        hashSet.add("enum_decl_source_08.bal");
        hashSet.add("enum_decl_source_05.bal");
        hashSet.add("enum_decl_source_09.bal");
        hashSet.add("minutiae_test_05_with_no_newlines.bal");
        hashSet.add("import_decl_source_13.bal");
        hashSet.add("ambiguity_source_29.bal");
        hashSet.add("typed_binding_patterns_source_16.bal");
        hashSet.add("do_stmt_source_07.bal");
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
