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

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Completion item tests for function definition.
 */
public class FunctionDefinitionCompletionTest extends CompletionTest {

    private static final Logger log = LoggerFactory.getLogger(FunctionDefinitionCompletionTest.class);

    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        log.info("Test textDocument/completion for Function Definition Scope");
        return new Object[][] {
                {"emptyLineCompletion.json", "function"},
                {"nonEmptyLineCompletion.json", "function"},
                {"actionInvocationSuggestion1.json", "function"},
//                {"actionInvocationSuggestion2.json", "function"}, // parser fix
                {"aliasedModuleContentSuggestion1.json", "function"},
                {"variableBoundItemSuggestions1.json", "function"},
                {"variableBoundItemSuggestions2.json", "function"},
                {"variableBoundItemSuggestions3.json", "function"},
                {"variableBoundItemSuggestions4.json", "function"},
                {"panicStatementErrorSuggestions.json", "function"},
                {"functionPointerAsParameter.json", "function"},
//                {"matchStatementSuggestions1.json", "function"}, // Do not support
//                {"matchStatementSuggestions3.json", "function"}, // Do not support
//                {"matchStatementSuggestions4.json", "function"}, // Do not support
//                {"matchStatementSuggestions5.json", "function"}, // Do not support
//                {"matchStatementSuggestions6.json", "function"}, // Do not support
//                {"matchStatementSuggestions7.json", "function"}, // Do not support
//                {"matchStatementSuggestions8.json", "function"}, // Do not support
//                {"matchStatementSuggestions9.json", "function"}, // Do not support
//                {"matchStatementSuggestions10.json", "function"}, // Do not support
//                {"errorLiftingSuggestions1.json", "function"}, // Do not support
                {"errorLiftingSuggestions2.json", "function"}, // Do not support
                {"iterableOperation1.json", "function"},
                {"iterableOperation2.json", "function"},
                {"iterableOperation4.json", "function"},
                {"iterableOperation6.json", "function"},
//                {"completionWithinWorkersInResource.json", "function"},
                {"suggestionsInWorkersWithinFunction.json", "function"},
                {"completionBeforeIfElse.json", "function"},
                {"completionWithinIf.json", "function"},
                {"completionWithinElse.json", "function"},
                {"completionWithinElseIf.json", "function"},
                {"completionBeforeWhile.json", "function"},
                {"completionWithinWhile.json", "function"},
                {"typeGuardSuggestions1.json", "function"},
                {"typeGuardSuggestions2.json", "function"},
                {"typeGuardSuggestions3.json", "function"},
                {"typeGuardSuggestions4.json", "function"},
                {"typeGuardSuggestions5.json", "function"},
                {"typeguardDestruct1.json", "function"},
                {"completionAfterIf1.json", "function"},
                {"completionAfterIf2.json", "function"},
//                {"completionWithinInvocationArgs1.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs2.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs3.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs4.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs5.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs6.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs7.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs8.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs9.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs10.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs11.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs12.json", "function"}, // Revamp needed
//                {"completionWithinInvocationArgs13.json", "function"}, // Revamp needed
//                {"chainCompletion6.json", "function"}, // Revamp needed
//                {"chainCompletion7.json", "function"}, // Revamp needed
                {"newObjectCompletion1.json", "function"},
                {"newObjectCompletion2.json", "function"},
                {"newObjectCompletion3.json", "function"},
//                {"delimiterBasedCompletionForCompleteSource1.json", "function"}, // Revamp needed
//                {"delimiterBasedCompletionForCompleteSource2.json", "function"}, // Revamp needed
//                {"functionParamAnnotationBodyCompletion1.json", "function"},
//                {"functionParamAnnotationBodyCompletion2.json", "function"},
//                {"functionParamAnnotationBodyCompletion3.json", "function"},
//                {"functionParamAnnotationBodyCompletion4.json", "function"},
//                {"completionWithTupleVariableDef.json", "function"},
//                {"completionWithinComments.json", "function"}, // Revamp needed
//                {"completionWithinLiterals.json", "function"},
//                {"assignmentStmt1.json", "function"},
//                {"assignmentStmt2.json", "function"},
//                {"assignmentStmt3.json", "function"},
//                {"assignmentStmt4.json", "function"}, // Revamp needed
//                {"assignmentStmt5.json", "function"}, // Revamp needed
//                {"assignmentStmt6.json", "function"}, // Revamp needed
//                {"varDefStmt1.json", "function"}, // Revamp needed
//                {"varDefStmt2.json", "function"}, // Revamp needed
//                {"varDefStmt3.json", "function"}, // Revamp needed
                {"typeofKWSuggestion.json", "function"},
                {"typeofKWSuggestion2.json", "function"},
                {"forkJoinCompletion1.json", "function"},
                {"forkJoinCompletion2.json", "function"},
                {"forkJoinCompletion3.json", "function"},
                {"forkJoinCompletion4.json", "function"},
//                {"packageContentSuggestionInvalid1.json", "function"}, // Revamp needed
//                {"errorUnionSuggestion1.json", "function"},
//                {"typeDescSuggestions1.json", "function"},
//                {"typeDescSuggestions2.json", "function"}, // Revamp needed
//                {"delimiterBasedCompletionOverArrays.json", "function"}, // Revamp needed
//                {"delimiterBasedCompletionOverMaps.json", "function"}, // Revamp needed
//                {"delimiterBasedCompletionOverXML.json", "function"},
//                {"langLibPackageSuggestion1.json", "function"}, // Revamp needed
//                {"langLibPackageSuggestion2.json", "function"}, // Revamp needed
//                {"groupedExpressionSuggestion1.json", "function"}, // Revamp needed
//                {"errorVarDefSuggestion1.json", "function"},
//                {"completionWithinDoClause.json", "function"}, // Revamp needed
//                {"commitKWSuggestion1.json", "function"}, // Revamp needed
//                {"commitKWSuggestion2.json", "function"}, // Revamp needed
//                {"statementWithMissingSemiColon1.json", "function"},
//                {"statementWithMissingSemiColon2.json", "function"},
//                {"statementWithMissingSemiColon3.json", "function"},
//                {"statementWithMissingSemiColon4.json", "function"},
        };
    }
}
