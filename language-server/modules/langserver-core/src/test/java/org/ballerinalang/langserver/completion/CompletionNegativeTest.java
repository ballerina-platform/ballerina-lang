/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.util.CompletionTestUtil;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 * Abstract negative test class for completions.
 */
public abstract class CompletionNegativeTest extends CompletionTest {
    @Override
    @Test(dataProvider = "completion-negative-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException {
        String configJsonPath = "completion" + File.separator + configPath + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);
        List<CompletionItem> responseItemList = getResponseItemList(configJsonObject);
        List<CompletionItem> negativeList = getExpectedList(configJsonObject);
        Assert.assertEquals(false, CompletionTestUtil.containsAtLeastOne(negativeList, responseItemList));
    }

    @Override
    @DataProvider(name = "completion-negative-data-provider")
    public abstract Object[][] dataProvider();
}
