/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 2.0.0
 */
public class AddOnFailCodeActionTest extends AbstractCodeActionTest {

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config, String source) throws IOException, WorkspaceDocumentException {
        super.test(config, source);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"on_fail_codeaction_for_check_stmt_config1.json", "on_fail_codeaction_for_check_stmt_source1.bal"},
                {"on_fail_codeaction_for_check_stmt_config1a.json", "on_fail_codeaction_for_check_stmt_source1a.bal"},
                {"on_fail_codeaction_for_check_stmt_config2.json", "on_fail_codeaction_for_check_stmt_source2.bal"},
                {"on_fail_codeaction_for_check_stmt_config3.json", "on_fail_codeaction_for_check_stmt_source3.bal"},
                {"on_fail_codeaction_for_check_stmt_config4.json", "on_fail_codeaction_for_check_stmt_source4.bal"},
                {"on_fail_codeaction_for_check_stmt_config5.json", "on_fail_codeaction_for_check_stmt_source5.bal"},
                {"on_fail_codeaction_for_fail_stmt_config1.json", "on_fail_codeaction_for_fail_stmt_source1.bal"},
                {"on_fail_codeaction_for_fail_stmt_config2.json", "on_fail_codeaction_for_fail_stmt_source2.bal"},
                {"on_fail_codeaction_for_fail_stmt_config3.json", "on_fail_codeaction_for_fail_stmt_source3.bal"},
                {"on_fail_codeaction_for_fail_stmt_config4.json", "on_fail_codeaction_for_fail_stmt_source4.bal"},
                {"on_fail_for_foreach_check_stmt.json", "on_fail_for_foreach_check_stmt.bal"},
                {"on_fail_for_lock_check_stmt.json", "on_fail_for_lock_check_stmt.bal"},
                {"on_fail_for_match_check_stmt.json", "on_fail_for_match_check_stmt.bal"},
                {"on_fail_for_while_check_stmt.json", "on_fail_for_while_check_stmt.bal"},
                {"on_fail_for_while_condition_check_stmt.json", "on_fail_for_while_condition_check_stmt.bal"},
        };
    }

    @Override
    public String getResourceDir() {
        return "add-on-fail";
    }
}
