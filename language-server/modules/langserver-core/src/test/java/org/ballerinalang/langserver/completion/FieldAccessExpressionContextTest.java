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
package org.ballerinalang.langserver.completion;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Field Access Expression Context tests.
 *
 * @since 2.0.0
 */
public class FieldAccessExpressionContextTest extends CompletionTest {

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "field_access_expression_context";
    }

    @Override
    public List<String> skipList() {
        return Collections.emptyList();
    }
}