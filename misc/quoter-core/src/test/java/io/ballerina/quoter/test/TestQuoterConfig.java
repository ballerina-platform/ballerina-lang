/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina.quoter.test;

import io.ballerina.quoter.config.QuoterConfig;

/**
 * Configuration file used for testing purposes.
 * Overrides several parameters with test parameters.
 */
public class TestQuoterConfig extends QuoterConfig {
    private static final String CHILD_NAMES_JSON = "syntax_tree_descriptor.json";
    private static final String DEFAULT_TEST_PARSER = "module";
    private static final String DEFAULT_TEST_TIMEOUT_MS = "10000";


    private final String templateFile;
    private final String tabStart;
    private final String formatterName;

    public TestQuoterConfig(String templateFile, int tabStart, String formatterName) {
        this.templateFile = templateFile;
        this.tabStart = String.valueOf(tabStart);
        this.formatterName = formatterName;
    }

    @Override
    public String getOrThrow(String key) {
        switch (key) {
            case EXTERNAL_FORMATTER_TEMPLATE:
                return templateFile;
            case EXTERNAL_FORMATTER_TAB_START:
                return tabStart;
            case INTERNAL_SYNTAX_TREE_DESCRIPTOR:
                return CHILD_NAMES_JSON;
            case EXTERNAL_FORMATTER_NAME:
                return formatterName;
            case EXTERNAL_FORMATTER_USE_TEMPLATE:
                return String.valueOf(true);
            case EXTERNAL_PARSER_NAME:
                return DEFAULT_TEST_PARSER;
            case EXTERNAL_PARSER_TIMEOUT:
                return DEFAULT_TEST_TIMEOUT_MS;
            case EXTERNAL_IGNORE_MINUTIAE:
                return String.valueOf(false);
            default:
                throw new RuntimeException("Unknown key: " + key);
        }
    }

    @Override
    public String readTemplateFile() {
        return FileReaderUtils.readFileAsResource(getOrThrow(EXTERNAL_FORMATTER_TEMPLATE));
    }
}
