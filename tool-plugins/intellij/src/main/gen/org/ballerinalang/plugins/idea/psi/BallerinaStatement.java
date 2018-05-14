/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// This is a generated file. Not intended for manual editing.
package org.ballerinalang.plugins.idea.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaStatement extends BallerinaCompositeElement {

  @Nullable
  BallerinaAbortStatement getAbortStatement();

  @Nullable
  BallerinaAssignmentStatement getAssignmentStatement();

  @Nullable
  BallerinaBreakStatement getBreakStatement();

  @Nullable
  BallerinaCompoundAssignmentStatement getCompoundAssignmentStatement();

  @Nullable
  BallerinaExpressionStmt getExpressionStmt();

  @Nullable
  BallerinaForeachStatement getForeachStatement();

  @Nullable
  BallerinaForeverStatement getForeverStatement();

  @Nullable
  BallerinaForkJoinStatement getForkJoinStatement();

  @Nullable
  BallerinaIfElseStatement getIfElseStatement();

  @Nullable
  BallerinaLockStatement getLockStatement();

  @Nullable
  BallerinaNamespaceDeclarationStatement getNamespaceDeclarationStatement();

  @Nullable
  BallerinaNextStatement getNextStatement();

  @Nullable
  BallerinaPostIncrementStatement getPostIncrementStatement();

  @Nullable
  BallerinaReturnStatement getReturnStatement();

  @Nullable
  BallerinaStreamingQueryStatement getStreamingQueryStatement();

  @Nullable
  BallerinaThrowStatement getThrowStatement();

  @Nullable
  BallerinaTransactionStatement getTransactionStatement();

  @Nullable
  BallerinaTryCatchStatement getTryCatchStatement();

  @Nullable
  BallerinaVariableDefinitionStatement getVariableDefinitionStatement();

  @Nullable
  BallerinaWhileStatement getWhileStatement();

  @Nullable
  BallerinaWorkerInteractionStatement getWorkerInteractionStatement();

  @Nullable
  BallerinaMatchStatement getMatchStatement();

  @Nullable
  BallerinaRetryStatement getRetryStatement();

  @Nullable
  BallerinaTupleDestructuringStatement getTupleDestructuringStatement();

}
