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
package org.ballerinalang.composer.service.workspace.langserver.definitions;

import org.ballerinalang.composer.service.workspace.langserver.CompletionTest;
import org.testng.annotations.DataProvider;

import java.io.File;

/**
 * Completion item tests for action definition
 */
public class ActionDefinition extends CompletionTest {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"emptyLinePrimitiveDataTypes.json", "completions" + File.separator + "action" + File.separator},
                {"nonEmptyLinePrimitiveDataTypes.json", "completions" + File.separator + "action" + File.separator},
                {"userDefinedStructEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"userDefinedStructNonEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"userDefinedFunctionsEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"userDefinedFunctionsNonEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"importPackagesEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"importPackagesNonEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"allVisibleSymbolsNonEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"allVisibleSymbolsEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"languageConstructsEmptyLine.json", "completions" + File.separator + "action" + File.separator},
                {"languageConstructsNonEmptyLine.json", "completions" + File.separator + "action" + File.separator}
        };
    }
}
