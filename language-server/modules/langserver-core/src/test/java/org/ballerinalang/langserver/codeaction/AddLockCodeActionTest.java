/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com)
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

package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test class to test the add lock code action.
 *
 * @since 2201.9.0
 */
public class AddLockCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"add_lock1.json"},
                {"add_lock2.json"},
                {"add_lock3.json"},
                {"add_lock4.json"},
                {"add_lock5.json"},
                {"add_lock6.json"},
                {"add_lock7.json"},
                {"add_lock8.json"},
                {"add_lock9.json"},
                {"add_lock10.json"},
                {"add_lock11.json"},
                {"add_lock12.json"},
                {"add_lock13.json"},
                {"add_lock14.json"},
                {"add_lock15.json"},
                {"add_lock16.json"},
                {"add_lock17.json"},
                {"add_lock18.json"},
                {"add_lock19.json"},
                {"add_lock20.json"},
                {"add_lock21.json"},
                {"add_lock22.json"},
                {"add_lock23.json"},
                {"add_lock24.json"},
                {"add_lock25.json"},
                {"add_lock26.json"}
        };
    }

    @Test(dataProvider = "negative-data-provider")
    @Override
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "negative-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"add_lock_negative1.json"},
                {"add_lock_negative2.json"},
                {"add_lock_negative3.json"},
                {"add_lock_negative4.json"},
                {"add_lock_negative5.json"},
                {"add_lock_negative6.json"},
                {"add_lock_negative7.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "add-lock";
    }
}
