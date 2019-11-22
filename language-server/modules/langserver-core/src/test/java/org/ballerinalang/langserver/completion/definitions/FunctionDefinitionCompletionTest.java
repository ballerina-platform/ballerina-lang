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

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
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
                {"actionInvocationSuggestion2.json", "function"},
                {"variableBoundItemSuggestions1.json", "function"},
                {"variableBoundItemSuggestions2.json", "function"},
                {"variableBoundItemSuggestions3.json", "function"},
                {"variableBoundItemSuggestions4.json", "function"},
                {"panicStatementErrorSuggestions.json", "function"},
                {"recordVarDef1.json", "function"},
                {"recordVarDef2.json", "function"},
                {"functionPointerAsParameter.json", "function"},
                {"matchStatementSuggestions1.json", "function"},
                {"matchStatementSuggestions3.json", "function"},
                {"matchStatementSuggestions4.json", "function"},
                {"matchStatementSuggestions5.json", "function"},
                {"matchStatementSuggestions6.json", "function"},
                {"matchStatementSuggestions7.json", "function"},
                {"matchStatementSuggestions8.json", "function"},
                {"matchStatementSuggestions9.json", "function"},
                {"matchStatementSuggestions10.json", "function"},
                {"errorLiftingSuggestions1.json", "function"},
                {"errorLiftingSuggestions2.json", "function"},
                {"iterableOperation1.json", "function"},
                {"iterableOperation2.json", "function"},
                {"iterableOperation3.json", "function"},
                {"iterableOperation4.json", "function"},
//                {"iterableOperation5.json", "function"},
                {"iterableOperation6.json", "function"},
                {"completionWithinWorkersInResource.json", "function"},
                {"suggestionsInWorkersWithinFunction.json", "function"},
                {"completionBeforeIfElse.json", "function"},
                {"completionWithinIf.json", "function"},
                {"completionWithinElse.json", "function"},
                {"completionWithinElseIf.json", "function"},
                {"completionBeforeWhile.json", "function"},
                {"completionWithinWhile.json", "function"},
                {"completionWithinTransaction.json", "function"},
                {"completionWithinTransactionOnRetry.json", "function"},
                {"completionAfterReturn.json", "function"},
                {"functionCompletionWithMissingImport.json", "function"},
                {"completionWithinRecord1.json", "function"},
                {"completionWithinRecord2.json", "function"},
                {"completionWithinRecord3.json", "function"},
                {"anonFunctionSnippetSuggestion1.json", "function"},
                {"anonFunctionSnippetSuggestion2.json", "function"},
                {"anonFunctionSnippetSuggestion3.json", "function"},
                {"typeGuardSuggestions1.json", "function"},
                {"typeGuardSuggestions2.json", "function"},
                {"typeGuardSuggestions3.json", "function"},
                {"typeGuardSuggestions4.json", "function"},
                {"typeGuardSuggestions5.json", "function"},
                {"typeguardDestruct1.json", "function"},
                {"completionAfterIf1.json", "function"},
                {"completionAfterIf2.json", "function"},
                {"workerDeclarationContext1.json", "function"},
                {"workerDeclarationContext2.json", "function"},
                {"workerDeclarationContext3.json", "function"},
                {"workerDeclarationContext4.json", "function"},
                {"workerDeclarationContext5.json", "function"},
                {"workerDeclarationContext6.json", "function"},
                {"completionWithinInvocationArgs1.json", "function"},
                {"completionWithinInvocationArgs2.json", "function"},
//                {"completionWithinInvocationArgs3.json", "function"},
                {"completionWithinInvocationArgs4.json", "function"},
                {"completionWithinInvocationArgs5.json", "function"},
                {"completionWithinInvocationArgs6.json", "function"},
                {"completionWithinInvocationArgs7.json", "function"},
                {"completionWithinInvocationArgs8.json", "function"},
                {"completionWithinInvocationArgs9.json", "function"},
                {"completionWithinInvocationArgs10.json", "function"},
                {"completionWithinInvocationArgs11.json", "function"},
                {"completionWithinInvocationArgs12.json", "function"},
                {"completionWithinInvocationArgs13.json", "function"},
                {"chainCompletion1.json", "function"},
                {"chainCompletion2.json", "function"},
                {"chainCompletion3.json", "function"},
                {"chainCompletion4.json", "function"},
                {"chainCompletion5.json", "function"},
                {"chainCompletion6.json", "function"},
                {"chainCompletion7.json", "function"},
                {"externalKeywordSuggestion1.json", "function"},
                {"externalKeywordSuggestion2.json", "function"},
//                {"ifWhileConditionContextCompletion1.json", "function"}, //TODO: Fix this
                {"ifWhileConditionContextCompletion2.json", "function"},
                {"ifWhileConditionContextCompletion3.json", "function"},
                {"ifWhileConditionContextCompletion4.json", "function"},
                {"ifWhileConditionContextCompletion5.json", "function"},
                {"newObjectCompletion1.json", "function"},
                {"newObjectCompletion2.json", "function"},
                {"newObjectCompletion3.json", "function"},
                {"delimiterBasedCompletionForCompleteSource1.json", "function"},
                {"delimiterBasedCompletionForCompleteSource2.json", "function"},
                {"functionParamAnnotationBodyCompletion1.json", "function"},
                {"functionParamAnnotationBodyCompletion2.json", "function"},
                {"functionParamAnnotationBodyCompletion3.json", "function"},
                {"functionParamAnnotationBodyCompletion4.json", "function"},
                {"completionWithTupleVariableDef.json", "function"},
                {"completionWithinComments.json", "function"},
                {"completionWithinLiterals.json", "function"},
                {"assignmentStmt1.json", "function"},
                {"assignmentStmt2.json", "function"},
                {"assignmentStmt3.json", "function"},
                {"assignmentStmt4.json", "function"},
                {"assignmentStmt5.json", "function"},
                {"assignmentStmt6.json", "function"},
                {"varDefStmt1.json", "function"},
                {"varDefStmt2.json", "function"},
                {"typeofKWSuggestion.json", "function"},
                {"typeofKWSuggestion2.json", "function"},
                {"forkJoinCompletion1.json", "function"},
                {"forkJoinCompletion2.json", "function"},
                {"forkJoinCompletion3.json", "function"},
                {"forkJoinCompletion4.json", "function"},
                {"packageContentSuggestionInvalid1.json", "function"},
                {"errorUnionSuggestion1.json", "function"},
                {"returnParameterContextSuggestion1.json", "function"},
                {"returnParameterContextSuggestion2.json", "function"},
                {"returnParameterContextSuggestion3.json", "function"},
                {"returnParameterContextSuggestion4.json", "function"},
                {"returnParameterContextSuggestion5.json", "function"},
                {"returnParameterContextSuggestion6.json", "function"},
                {"returnParameterContextSuggestion7.json", "function"},
                {"returnParameterContextSuggestion8.json", "function"},
                {"returnParameterContextSuggestion9.json", "function"},
                {"returnParameterContextSuggestion10.json", "function"},
                {"returnParameterContextSuggestion11.json", "function"},
                {"typeDescSuggestions1.json", "function"},
                {"typeDescSuggestions2.json", "function"},
                {"delimiterBasedCompletionOverArrays.json", "function"},
                {"delimiterBasedCompletionOverXML.json", "function"},
//                {"statementWithMissingSemiColon1.json", "function"},
//                {"statementWithMissingSemiColon2.json", "function"},
//                {"statementWithMissingSemiColon3.json", "function"},
//                {"statementWithMissingSemiColon4.json", "function"},
        };
    }
}
