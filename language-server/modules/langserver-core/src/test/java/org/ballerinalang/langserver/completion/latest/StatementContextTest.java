/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion.latest;

import org.testng.annotations.DataProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Statement Context tests.
 *
 * @since 2.0.0
 */
public class StatementContextTest extends CompletionTestNew {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public Object[][] testSubset() {
        return new Object[][] {
                {"assignment_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"assignment_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"assignment_stmt_ctx_config3.json", this.getTestResourceDir()},
                {"assignment_stmt_ctx_config4.json", this.getTestResourceDir()},
                {"async_send_action_ctx_config1.json", this.getTestResourceDir()},
                {"async_send_action_ctx_config2.json", this.getTestResourceDir()},
                {"checking_call_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"checking_call_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"continue_break_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"continue_break_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"do_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"do_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"else_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"else_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"elseif_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"elseif_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"elseif_stmt_ctx_config4.json", this.getTestResourceDir()},
                {"elseif_stmt_ctx_config5.json", this.getTestResourceDir()},
                {"elseif_stmt_ctx_config6.json", this.getTestResourceDir()},
                {"flush_action_ctx_config1.json", this.getTestResourceDir()},
                {"flush_action_ctx_config2.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config3.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config4.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config5.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config6.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config7.json", this.getTestResourceDir()},
                {"foreach_stmt_ctx_config8.json", this.getTestResourceDir()},
                {"fork_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"fork_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"function_call_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"function_call_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"if_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"if_stmt_ctx_config2.json", this.getTestResourceDir()},
//                {"if_stmt_ctx_config3.json", this.getTestResourceDir()}, // Blocked By #26317
                {"if_stmt_ctx_config4.json", this.getTestResourceDir()},
                {"if_stmt_ctx_config5.json", this.getTestResourceDir()},
                {"if_stmt_ctx_config6.json", this.getTestResourceDir()},
                {"lock_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"match_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"receive_action_ctx_config1.json", this.getTestResourceDir()},
                {"receive_action_ctx_config2.json", this.getTestResourceDir()},
                {"remote_action_config1.json", this.getTestResourceDir()},
                {"sync_send_action_ctx_config1.json", this.getTestResourceDir()},
                {"sync_send_action_ctx_config2.json", this.getTestResourceDir()},
                {"wait_action_ctx_config1.json", this.getTestResourceDir()},
                {"wait_action_ctx_config2.json", this.getTestResourceDir()},
                {"wait_action_ctx_config3.json", this.getTestResourceDir()},
                {"wait_action_ctx_config4.json", this.getTestResourceDir()},
                {"wait_action_ctx_config5.json", this.getTestResourceDir()},
                {"wait_action_ctx_config6.json", this.getTestResourceDir()},
                {"wait_action_ctx_config7.json", this.getTestResourceDir()},
                {"wait_action_ctx_config8.json", this.getTestResourceDir()},
                {"wait_action_ctx_config9.json", this.getTestResourceDir()},
                {"while_stmt_ctx_config1.json", this.getTestResourceDir()},
                {"while_stmt_ctx_config2.json", this.getTestResourceDir()},
                {"while_stmt_ctx_config3.json", this.getTestResourceDir()},
                {"while_stmt_ctx_config4.json", this.getTestResourceDir()},
                {"while_stmt_ctx_config5.json", this.getTestResourceDir()},
                {"while_stmt_ctx_config6.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config1.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config2.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config3.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config4.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config5.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config6.json", this.getTestResourceDir()},
                {"worker_declaration_ctx_config7.json", this.getTestResourceDir()},
                {"xmlns_ctx_config1.json", this.getTestResourceDir()},
                {"xmlns_ctx_config2.json", this.getTestResourceDir()},
        };
    }

    @Override
    public List<String> skipList() {
        return Arrays.asList("if_stmt_ctx_config3.json", "elseif_stmt_ctx_config3.json");
    }

    @Override
    public String getTestResourceDir() {
        return "statement_context";
    }
}
