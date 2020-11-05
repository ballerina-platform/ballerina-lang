/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.formatter.core;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test the formatting of parser test cases.
 *
 * @since 2.0.0
 */
public class ParserTestFormatter extends FormatterTest {

    @Test(dataProvider = "test-file-provider")
    public void test(String fileName, String path) throws IOException {
        super.testParserResources(path);
    }

//    // Uncomment to run a subset of test cases.
//    @Override
//    public Object[][] testSubset() {
//        Path buildDirectory = Paths.get("build").toAbsolutePath().normalize();
//
//        return new Object[][] {
//                {"annot_decl_source_01.bal", getFilePath("annot_decl_source_01.bal",
//                        buildDirectory.resolve("resources").resolve("test").toString()).orElse(null)}
//        };
//    }

    @Override
    public List<String> skipList() {
        return Arrays.asList(
                // the following tests need to be skipped since these contain intended extra minutiaes
                "minutiae_test_01.bal",
                "minutiae_test_02.bal",
                "minutiae_test_03.bal",
                "minutiae_test_04.bal",
                "minutiae_test_05.bal",
                "minutiae_test_05_with_no_newlines.bal",
                "doc_source_15.bal",
                "doc_source_06.bal",

                // the following tests need to be enabled in the future
                "ambiguity_source_11.bal", // parser issue for indexed expressions #26420
                "typed_binding_patterns_source_19.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_04.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_15.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_01.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_14.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_02.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_17.bal", // parser issue for binding patterns #26421
                "typed_binding_patterns_source_03.bal", // parser issue for binding patterns #26421
                "annotations_source_04.bal", // could be considered an invalid scenario
                "list_binding_pattern_source_01.bal", // parser issue for binding patterns #26421
                "mapping_binding_pattern_source_01.bal", // parser issue for binding patterns #26421
                "forEach_stmt_source_19.bal", // parser issue for binding patterns #26421
                "forEach_stmt_source_18.bal", // parser issue for binding patterns #26421
                "forEach_stmt_source_12.bal", // parser issue for binding patterns #26421
                "match_stmt_source_08.bal", // parser issue for binding patterns & match block #26421
                "match_stmt_source_05.bal", // parser issue for binding patterns & match block #26421
                "match_stmt_source_14.bal", // match block
                "match_stmt_source_01.bal", // match block
                "receive_action_source_01.bal", // issue #26376
                "match_stmt_source_10.bal", // issue #26376
                "match_stmt_source_12.bal", // issue #26376
                "typed_binding_patterns_source_16.bal", // parser issue for binding patterns #26421
                "error_binding_pattern_source_02.bal"); // issue #26376
    }

    @DataProvider(name = "test-file-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getParserTestConfigs();
    }

    @Override
    public String getTestResourceDir() {
        return Paths.get("parser-tests").toString();
    }

    private Optional<String> getFilePath(String fileName, String directoryPath) {
        try {
            return Optional.ofNullable(Files.walk(Paths.get(directoryPath))
                    .filter(f -> f.getFileName().toString().equals(fileName))
                    .collect(Collectors.toList()).get(0).toString());
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
