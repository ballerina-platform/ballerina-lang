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
                // the following tests need to be skipped since these contain intended extra/no minutiae
                "minutiae_test_01.bal",
                "minutiae_test_02.bal",
                "minutiae_test_03.bal",
                "minutiae_test_04.bal",
                "minutiae_test_05.bal",
                "minutiae_test_05_with_no_newlines.bal",
                "invalid_token_minutiae_test_01.bal",
                "invalid_token_minutiae_test_02.bal",
                "doc_source_15.bal",
                "doc_source_06.bal",
                "module_var_decl_source_16.bal",
                "doc_source_24.bal",
                "doc_source_27.bal",
                "invalid_identifier_source_02.bal",
                "float_literal_source_07.bal",
                "method_call_expr_source_03.bal",
                "method_call_expr_source_05.bal",
                "qualified_identifier_assert_08.bal",
                "conditional_expr_source_28.bal",
                "resiliency_source_04.bal",
                "record_type_def_source_14.bal",
                "record_type_def_source_29.bal",
                "object_type_def_source_12.bal",
                "anon_func_source_01.bal",
                "conditional_expr_source_27.bal",

                // the following tests need to be enabled in the future
                "annotations_source_04.bal", // could be considered an invalid scenario
                "receive_action_source_01.bal", // issue #26376
                "doc_source_21.bal", // issue #28172
                "module_var_decl_source_18.bal", // issue #31307
                "match_stmt_source_21.bal", // issue #35240
                "func_params_source_27.bal", // issue #35240

                "service_decl_source_02.bal", "service_decl_source_05.bal", "service_decl_source_17.bal",
                "service_decl_source_20.bal",

                // parser tests with syntax errors that cannot be handled by the formatter
                "worker_decl_source_03.bal", "worker_decl_source_05.bal", "invalid_identifier_source_01.bal",
                "ambiguity_source_23.bal", "ambiguity_source_09.bal", "ambiguity_source_18.bal",
                "ambiguity_source_30.bal", "ambiguity_source_24.bal", "ambiguity_source_26.bal",
                "ambiguity_source_16.bal", "ambiguity_source_03.bal", "ambiguity_source_29.bal",
                "ambiguity_source_28.bal", "ambiguity_source_04.bal", "ambiguity_source_10.bal",
                "ambiguity_source_13.bal", "typed_binding_patterns_source_18.bal",
                "typed_binding_patterns_source_08.bal", "typed_binding_patterns_source_09.bal",
                "typed_binding_patterns_source_07.bal", "annotations_source_09.bal", "annotations_source_08.bal",
                "annotations_source_06.bal", "resiliency_source_01.bal", "resiliency_source_02.bal",
                "separated_node_list_modify_all_nodes_assert.bal", "child_node_list_test_01.bal",
                "intersection_type_source_06.bal", "array_type_source_17.bal",
                "stream_type_source_05.bal", "func_type_source_05.bal", "func_type_source_07.bal",
                "func_type_source_08.bal", "table_type_source_08.bal", "table_type_source_09.bal",
                "singleton_type_source_08.bal", "singleton_type_source_06.bal", "simple_types_source_02.bal",
                "tuple_type_source_04.bal", "tuple_type_source_06.bal", "trivia_source_02.bal",
                "enum_decl_source_05.bal", "enum_decl_source_08.bal", "enum_decl_source_09.bal",
                "service_decl_source_09.bal", "service_decl_source_15.bal", "service_decl_source_03.bal",
                 "service_decl_source_12.bal", "service_decl_source_10.bal", "service_decl_source_04.bal",
                "service_decl_source_11.bal", "import_decl_source_19.bal", "import_decl_source_20.bal",
                "import_decl_source_21.bal", "import_decl_source_23.bal", "import_decl_source_22.bal",
                "import_decl_source_06.bal", "import_decl_source_04.bal", "import_decl_source_10.bal",
                "import_decl_source_05.bal", "import_decl_source_15.bal", "import_decl_source_14.bal",
                "import_decl_source_16.bal", "module_var_decl_source_09.bal", "module_var_decl_source_03.bal",
                "module_var_decl_source_07.bal", "module_var_decl_source_06.bal", "isolated_service_func_source_04.bal",
                "func_def_source_06.bal", "func_def_source_12.bal", "func_def_source_16.bal", "func_def_source_03.bal",
                "func_def_source_17.bal", "func_def_source_15.bal", "func_def_source_14.bal",
                "func_params_source_06.bal", "func_def_source_19.bal", "func_def_source_24.bal",
                "func_def_source_18.bal", "func_params_source_07.bal", "func_def_source_07.bal",
                "func_params_source_04.bal", "func_def_source_23.bal", "func_def_source_22.bal",
                "func_params_source_03.bal", "func_def_source_20.bal", "func_def_source_09.bal",
                "func_def_source_21.bal", "func_params_source_02.bal",
                "record_type_def_source_20.bal", "record_type_def_source_07.bal", "record_type_def_source_06.bal",
                "record_type_def_source_10.bal", "xmlns_decl_source_03.bal", "xmlns_decl_source_07.bal",
                "xmlns_decl_source_08.bal", "annot_decl_source_06.bal", "annot_decl_source_07.bal",
                "annot_decl_source_03.bal", "annot_decl_source_09.bal",
                "annot_decl_source_08.bal", "class_def_source_23.bal",
                "class_def_source_21.bal", "class_def_source_34.bal",
                "class_def_source_08.bal", "class_def_source_31.bal", "class_def_source_26.bal",
                "class_def_source_42.bal", "class_def_source_05.bal", "class_def_source_39.bal",
                "class_def_source_10.bal", "class_def_source_06.bal", "class_def_source_07.bal",
                "object_type_def_source_39.bal", "object_type_def_source_05.bal", "object_type_def_source_10.bal",
                "object_type_def_source_06.bal", "object_type_def_source_17.bal",
                "object_type_def_source_22.bal", "object_type_def_source_23.bal", "object_type_def_source_37.bal",
                "object_type_def_source_21.bal", "object_type_def_source_09.bal", "object_type_def_source_08.bal",
                "object_type_def_source_40.bal", "object_type_def_source_42.bal", "object_type_def_source_43.bal",
                "list_binding_pattern_source_02.bal", "local_type_defn_stmt_source_01.bal", "fork_stmt_source_08.bal",
                "fork_stmt_source_06.bal", "fork_stmt_source_04.bal",
                "do_stmt_source_07.bal", "do_stmt_source_05.bal", "do_stmt_source_04.bal", "retry_stmt_source_06.bal",
                "transaction_stmt_source_02.bal", "transaction_stmt_source_04.bal", "rollback_stmt_source_02.bal",
                "forEach_stmt_source_23.bal", "forEach_stmt_source_20.bal", "forEach_stmt_source_21.bal",
                "forEach_stmt_source_07.bal", "forEach_stmt_source_06.bal", "forEach_stmt_source_16.bal",
                "forEach_stmt_source_15.bal", "forEach_stmt_source_14.bal", "block_stmt_source_06.bal",
                "block_stmt_source_07.bal", "block_stmt_source_04.bal",
                "compound_assignment_stmt_source_9.bal", "compound_assignment_stmt_source_10.bal",
                "lock_stmt_source_09.bal", "lock_stmt_source_04.bal",
                "lock_stmt_source_06.bal", "lock_stmt_source_07.bal", "if_else_source_03.bal",
                "if_else_source_06.bal", "if_else_source_05.bal", "if_else_source_04.bal",
                "if_else_source_09.bal", "if_else_source_08.bal", "assignment_stmt_source_12.bal",
                "assignment_stmt_source_11.bal", "assignment_stmt_source_10.bal", "assignment_stmt_source_14.bal",
                "match_stmt_source_09.bal", "match_stmt_source_13.bal", "match_stmt_source_04.bal",
                "match_stmt_source_15.bal", "match_stmt_source_03.bal", "match_stmt_source_16.bal",
                "match_stmt_source_06.bal", "match_stmt_source_07.bal", "match_stmt_source_11.bal",
                "object_constructor_source_09.bal", "object-constructor-with-methods.bal",
                "object_constructor_source_05.bal", "object_constructor_source_04.bal",
                "object_constructor_source_06.bal", "object_constructor_source_07.bal",
                "object_constructor_source_03.bal", "object_constructor_source_02.bal",
                "object_constructor_source_01.bal", "anon_func_source_08.bal", "anon_func_source_06.bal",
                "anon_func_source_10.bal", "anon_func_source_05.bal", "anon_func_source_03.bal",
                "table_constructor_source_26.bal", "base16_literal_source_02.bal", "base64_literal_source_02.bal",
                "string_template_source_04.bal", "xml_template_source_13.bal", "xml_template_source_10.bal",
                "xml_template_source_05.bal", "xml_template_source_08.bal", "xml_template_source_09.bal",
                "xml_template_source_27.bal", "xml_template_source_24.bal",
                "explicit-new-with-object-keyword-with-one-arg-negative02.bal",
                "explicit-new-with-object-keyword-with-multiple-args-negative02.bal",
                "explicit-new-with-object-keyword-with-multiple-args.bal", "new_expr_source_03.bal",
                "explicit-new-with-object-keyword-with-one-arg-negative01.bal",
                "explicit-new-with-object-keyword-with-multiple-args-negative01.bal", "trap_action_source_02.bal",
                "query_action_source_03.bal", "query_action_source_06.bal", "remote_method_call_source_10.bal",
                "send_action_source_03.bal", "send_action_source_04.bal", "start_action_source_02.bal",
                "flush_action_source_02.bal", "ambiguity_source_06.bal", "typed_binding_patterns_source_22.bal",
                "resiliency_source_03.bal", "module_var_decl_source_10.bal", "error_binding_pattern_source_03.bal",
                "receive_action_source_02.bal", "receive_action_source_03.bal", "module_var_decl_source_15.bal",
                "annot_decl_source_02.bal", "do_stmt_source_08.bal", "func_params_source_11.bal",
                "predeclared-module-prefix_02.bal", "object_type_def_source_44.bal", "record_type_def_source_27.bal",
                "func_type_source_09.bal", "func_type_source_13.bal", "func_type_source_14.bal",
                "func_type_source_15.bal", "func_type_source_16.bal", "import_decl_source_24.bal",
                "member_access_expr_source_11.bal", "float_literal_source_08.bal", "object_type_def_source_47.bal", 
                "client_resource_access_action_source_05.bal", "client_resource_access_action_source_06.bal", 
                "resiliency_source_05.bal", "regexp_constructor_source_26.bal", "regexp_constructor_source_28.bal");
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
