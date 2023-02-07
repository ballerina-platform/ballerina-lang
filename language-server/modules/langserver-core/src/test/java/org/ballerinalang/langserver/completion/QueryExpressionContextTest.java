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
package org.ballerinalang.langserver.completion;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Query expression context related tests. Covers query expressions and query actions.
 */
public class QueryExpressionContextTest extends CompletionTest {

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public List<String> skipList() {
        return Arrays.asList(
                // TODO ST identifies the wrong token when there's a newline. Need to look at that.
                "query_expr_ctx_join_clause_config6a.json",
                // TODO: Suggestions in join's on clause should be limited to lists being joined
                "query_expr_ctx_join_clause_config8.json",
                // On Conflict
                "query_expr_ctx_onconflict_clause_config1.json",
                "query_expr_ctx_onconflict_clause_config1a.json",
                // Order By [asc/desc]
                "query_expr_ctx_orderby_clause_config4.json",
                "query_expr_ctx_config3.json" // issue #31449
        );
    }

    @Override
    public String getTestResourceDir() {
        return "query_expression";
    }
}
