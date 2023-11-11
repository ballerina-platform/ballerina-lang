/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.rename;

import org.ballerinalang.langserver.util.TestUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test renaming across multiple files in a ballerina project.
 */
public class ProjectRenameTest extends AbstractRenameTest {

    @Test(description = "Test reference", dataProvider = "testDataProvider")
    public void test(String configPath, String varName) throws IOException {
        performTest(configPath, varName);
    }

    @DataProvider
    private Object[][] testDataProvider() {
        return new Object[][]{
                {"rename_class_result.json", "Student"},
                {"rename_function_result.json", "getStudents"},
                {"rename_function_named_arg_result.json", "firstName"},
                {"rename_global_var_result.json", "path"},
                {"rename_error_config1.json", "Mod2Error"},
                {"rename_error_config2.json", "Mod2Error"},
                {"rename_package_alias_result1.json", "mod1"},
                {"rename_package_alias_result2.json", "mod"},
                {"rename_package_alias_result3.json", "mod"},
                {"rename_package_alias_result4.json", "mod1"},
                {"rename_package_alias_result5.json", "mod1"},
                {"rename_to_keyword1.json", "string"},
                {"rename_to_keyword2.json", "string"},

                // Negative/invalid cases
                {"rename_keyword_result1.json", "kw"},
                {"rename_invalid_token_result1.json", "token"},
                {"rename_table_row_type_and_stream_type.json", "Student"},
        };
    }

    @AfterClass
    public void shutDownLanguageServer() throws IOException {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }

    @Override
    protected String configRoot() {
        return "project";
    }

    @Override
    protected String sourceRoot() {
        return "project";
    }
}
