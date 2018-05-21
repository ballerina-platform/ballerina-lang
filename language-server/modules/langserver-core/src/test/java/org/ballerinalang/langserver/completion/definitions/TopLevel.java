/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion.definitions;

import org.ballerinalang.langserver.completion.CompletionTest;
import org.testng.annotations.DataProvider;

/**
 * Completion item tests for Top Level Resolving.
 */
public class TopLevel extends CompletionTest {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"topLevelNonEmptyFirstLine.json", "toplevel"},
                {"topLevelEmptyFirstLine.json", "toplevel"},
                {"endpointTypeCompletion1.json", "toplevel"},
                {"endpointTypeCompletion2.json", "toplevel"},
                {"endpointTypeCompletion3.json", "toplevel"},
                {"endpointTypeCompletion4.json", "toplevel"},
                {"endpointAttributeSuggestion1.json", "toplevel"},
                {"endpointAttributeSuggestion2.json", "toplevel"},
        };
    }
}
