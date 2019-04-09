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
package io.ballerina.plugins.idea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static io.ballerina.plugins.idea.psi.BallerinaTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import io.ballerina.plugins.idea.psi.*;

public class BallerinaStatementImpl extends ASTWrapperPsiElement implements BallerinaStatement {

  public BallerinaStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaAbortStatement getAbortStatement() {
    return findChildByClass(BallerinaAbortStatement.class);
  }

  @Override
  @Nullable
  public BallerinaAssignmentStatement getAssignmentStatement() {
    return findChildByClass(BallerinaAssignmentStatement.class);
  }

  @Override
  @Nullable
  public BallerinaBreakStatement getBreakStatement() {
    return findChildByClass(BallerinaBreakStatement.class);
  }

  @Override
  @Nullable
  public BallerinaCompoundAssignmentStatement getCompoundAssignmentStatement() {
    return findChildByClass(BallerinaCompoundAssignmentStatement.class);
  }

  @Override
  @Nullable
  public BallerinaContinueStatement getContinueStatement() {
    return findChildByClass(BallerinaContinueStatement.class);
  }

  @Override
  @Nullable
  public BallerinaErrorDestructuringStatement getErrorDestructuringStatement() {
    return findChildByClass(BallerinaErrorDestructuringStatement.class);
  }

  @Override
  @Nullable
  public BallerinaExpressionStmt getExpressionStmt() {
    return findChildByClass(BallerinaExpressionStmt.class);
  }

  @Override
  @Nullable
  public BallerinaForeachStatement getForeachStatement() {
    return findChildByClass(BallerinaForeachStatement.class);
  }

  @Override
  @Nullable
  public BallerinaForeverStatement getForeverStatement() {
    return findChildByClass(BallerinaForeverStatement.class);
  }

  @Override
  @Nullable
  public BallerinaForkJoinStatement getForkJoinStatement() {
    return findChildByClass(BallerinaForkJoinStatement.class);
  }

  @Override
  @Nullable
  public BallerinaIfElseStatement getIfElseStatement() {
    return findChildByClass(BallerinaIfElseStatement.class);
  }

  @Override
  @Nullable
  public BallerinaLockStatement getLockStatement() {
    return findChildByClass(BallerinaLockStatement.class);
  }

  @Override
  @Nullable
  public BallerinaNamespaceDeclarationStatement getNamespaceDeclarationStatement() {
    return findChildByClass(BallerinaNamespaceDeclarationStatement.class);
  }

  @Override
  @Nullable
  public BallerinaPanicStatement getPanicStatement() {
    return findChildByClass(BallerinaPanicStatement.class);
  }

  @Override
  @Nullable
  public BallerinaRecordDestructuringStatement getRecordDestructuringStatement() {
    return findChildByClass(BallerinaRecordDestructuringStatement.class);
  }

  @Override
  @Nullable
  public BallerinaRetryStatement getRetryStatement() {
    return findChildByClass(BallerinaRetryStatement.class);
  }

  @Override
  @Nullable
  public BallerinaReturnStatement getReturnStatement() {
    return findChildByClass(BallerinaReturnStatement.class);
  }

  @Override
  @Nullable
  public BallerinaStreamingQueryStatement getStreamingQueryStatement() {
    return findChildByClass(BallerinaStreamingQueryStatement.class);
  }

  @Override
  @Nullable
  public BallerinaThrowStatement getThrowStatement() {
    return findChildByClass(BallerinaThrowStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTransactionStatement getTransactionStatement() {
    return findChildByClass(BallerinaTransactionStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTryCatchStatement getTryCatchStatement() {
    return findChildByClass(BallerinaTryCatchStatement.class);
  }

  @Override
  @Nullable
  public BallerinaVariableDefinitionStatement getVariableDefinitionStatement() {
    return findChildByClass(BallerinaVariableDefinitionStatement.class);
  }

  @Override
  @Nullable
  public BallerinaWhileStatement getWhileStatement() {
    return findChildByClass(BallerinaWhileStatement.class);
  }

  @Override
  @Nullable
  public BallerinaWorkerSendAsyncStatement getWorkerSendAsyncStatement() {
    return findChildByClass(BallerinaWorkerSendAsyncStatement.class);
  }

  @Override
  @Nullable
  public BallerinaMatchStatement getMatchStatement() {
    return findChildByClass(BallerinaMatchStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTupleDestructuringStatement getTupleDestructuringStatement() {
    return findChildByClass(BallerinaTupleDestructuringStatement.class);
  }

}
