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
 * Completion item tests for function definition.
 */
public class FunctionDefinition extends CompletionTest {
    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][] {
                {"emptyLinePrimitiveDataTypes.json", "function"},
                {"nonEmptyLinePrimitiveDataTypes.json", "function"},
                {"userDefinedRecordEmptyLine.json", "function"},
                {"userDefinedRecordNonEmptyLine.json", "function"},
                {"userDefinedFunctionsEmptyLine.json", "function"},
                {"userDefinedFunctionsNonEmptyLine.json", "function"},
                {"importPackagesEmptyLine.json", "function"},
                {"importPackagesNonEmptyLine.json", "function"},
                {"allVisibleSymbolsEmptyLine.json", "function"},
                {"languageConstructsEmptyLine.json", "function"},
                {"languageConstructsNonEmptyLine.json", "function"},
//                {"enumSuggestAssignment1.json", "function"},
//                {"enumSuggestAssignment2.json", "function"},
//                {"enumSuggestVarDef1.json", "function"},
//                {"enumSuggestVarDef2.json", "function"},
//                {"structFields.json", "function"},
//                {"structBoundFunctionsAndFields.json", "function"},
//                {"packageContentWithSucceedingCharacter1.json", "function"}
        };
    }
}
