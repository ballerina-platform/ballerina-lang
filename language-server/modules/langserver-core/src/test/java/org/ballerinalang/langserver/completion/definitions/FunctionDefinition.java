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
                {"actionInvocationSuggestion1.json", "function"},
                {"actionInvocationSuggestion2.json", "function"},
                {"variableBoundItemSuggestions1.json", "function"},
                {"variableBoundItemSuggestions2.json", "function"},
                {"variableBoundItemSuggestions3.json", "function"},
                {"variableBoundItemSuggestions4.json", "function"},
                {"throwStatementErrorSuggestions.json", "function"},
                {"endpointAttributeSuggestion1.json", "function"},
                {"recordVarDef1.json", "function"},
                {"recordVarDef2.json", "function"},
                {"functionPointerAsParameter.json", "function"},
                {"matchStatementSuggestions1.json", "function"},
                {"matchStatementSuggestions2.json", "function"},
                {"matchStatementSuggestions3.json", "function"},
                {"matchStatementSuggestions4.json", "function"},
                {"matchExpressionSuggestions.json", "function"},
                {"matchExpressionSuggestions2.json", "function"},
                {"matchExpressionSuggestions3.json", "function"},
                {"matchExpressionSuggestions4.json", "function"},
                {"matchExpressionSuggestions5.json", "function"},
                {"errorLiftingSuggestions1.json", "function"},
                {"errorLiftingSuggestions2.json", "function"},
                {"iterableOperation1.json", "function"},
                {"iterableOperation2.json", "function"},
                {"iterableOperation3.json", "function"},
                {"iterableOperation4.json", "function"},
                {"iterableOperation5.json", "function"},
                {"iterableOperation6.json", "function"},
                {"suggestionsInWorkersWithinFunction.json", "function"},
                {"completionBeforeIfElse.json", "function"},
                {"completionWithinIf.json", "function"},
                {"completionWithinElse.json", "function"},
                {"completionWithinElseIf.json", "function"},
                {"completionBeforeWhile.json", "function"},
                {"completionWithinWhile.json", "function"},
                {"completionWithinWorkersInResource.json", "function"},
                {"completionWithinCatch.json", "function"},
                {"completionWithinTransaction.json", "function"},
                {"completionWithinTransactionOnRetry.json", "function"},
                {"objectAttachFunctionImpl1.json", "function"},
                {"objectAttachFunctionImpl2.json", "function"},
                {"objectAttachFunctionImpl3.json", "function"},
                {"completionAfterReturn.json", "function"},
                {"functionCompletionWithMissingImport.json", "function"},
        };
    }
}
