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
package org.ballerinalang.plugins.idea.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.ballerinalang.plugins.idea.psi.BallerinaTypes.*;
import org.ballerinalang.plugins.idea.psi.*;

public class BallerinaStatementImpl extends BallerinaCompositeElementImpl implements BallerinaStatement {

  public BallerinaStatementImpl(ASTNode node) {
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
    return PsiTreeUtil.getChildOfType(this, BallerinaAbortStatement.class);
  }

  @Override
  @Nullable
  public BallerinaAssignmentStatement getAssignmentStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaAssignmentStatement.class);
  }

  @Override
  @Nullable
  public BallerinaBreakStatement getBreakStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaBreakStatement.class);
  }

  @Override
  @Nullable
  public BallerinaCompoundAssignmentStatement getCompoundAssignmentStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaCompoundAssignmentStatement.class);
  }

  @Override
  @Nullable
  public BallerinaExpressionStmt getExpressionStmt() {
    return PsiTreeUtil.getChildOfType(this, BallerinaExpressionStmt.class);
  }

  @Override
  @Nullable
  public BallerinaForeachStatement getForeachStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaForeachStatement.class);
  }

  @Override
  @Nullable
  public BallerinaForeverStatement getForeverStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaForeverStatement.class);
  }

  @Override
  @Nullable
  public BallerinaForkJoinStatement getForkJoinStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaForkJoinStatement.class);
  }

  @Override
  @Nullable
  public BallerinaIfElseStatement getIfElseStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaIfElseStatement.class);
  }

  @Override
  @Nullable
  public BallerinaLockStatement getLockStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaLockStatement.class);
  }

  @Override
  @Nullable
  public BallerinaNamespaceDeclarationStatement getNamespaceDeclarationStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaNamespaceDeclarationStatement.class);
  }

  @Override
  @Nullable
  public BallerinaNextStatement getNextStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaNextStatement.class);
  }

  @Override
  @Nullable
  public BallerinaPostIncrementStatement getPostIncrementStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaPostIncrementStatement.class);
  }

  @Override
  @Nullable
  public BallerinaReturnStatement getReturnStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaReturnStatement.class);
  }

  @Override
  @Nullable
  public BallerinaStreamingQueryStatement getStreamingQueryStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaStreamingQueryStatement.class);
  }

  @Override
  @Nullable
  public BallerinaThrowStatement getThrowStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaThrowStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTransactionStatement getTransactionStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaTransactionStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTryCatchStatement getTryCatchStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaTryCatchStatement.class);
  }

  @Override
  @Nullable
  public BallerinaVariableDefinitionStatement getVariableDefinitionStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaVariableDefinitionStatement.class);
  }

  @Override
  @Nullable
  public BallerinaWhileStatement getWhileStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaWhileStatement.class);
  }

  @Override
  @Nullable
  public BallerinaWorkerInteractionStatement getWorkerInteractionStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaWorkerInteractionStatement.class);
  }

  @Override
  @Nullable
  public BallerinaMatchStatement getMatchStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaMatchStatement.class);
  }

  @Override
  @Nullable
  public BallerinaRetryStatement getRetryStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaRetryStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTupleDestructuringStatement getTupleDestructuringStatement() {
    return PsiTreeUtil.getChildOfType(this, BallerinaTupleDestructuringStatement.class);
  }

}
