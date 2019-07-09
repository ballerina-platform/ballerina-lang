/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class BallerinaCompoundOperatorImpl extends ASTWrapperPsiElement implements BallerinaCompoundOperator {

  public BallerinaCompoundOperatorImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitCompoundOperator(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getCompoundAdd() {
    return findChildByType(COMPOUND_ADD);
  }

  @Override
  @Nullable
  public PsiElement getCompoundBitAnd() {
    return findChildByType(COMPOUND_BIT_AND);
  }

  @Override
  @Nullable
  public PsiElement getCompoundBitOr() {
    return findChildByType(COMPOUND_BIT_OR);
  }

  @Override
  @Nullable
  public PsiElement getCompoundBitXor() {
    return findChildByType(COMPOUND_BIT_XOR);
  }

  @Override
  @Nullable
  public PsiElement getCompoundDiv() {
    return findChildByType(COMPOUND_DIV);
  }

  @Override
  @Nullable
  public PsiElement getCompoundLeftShift() {
    return findChildByType(COMPOUND_LEFT_SHIFT);
  }

  @Override
  @Nullable
  public PsiElement getCompoundLogicalShift() {
    return findChildByType(COMPOUND_LOGICAL_SHIFT);
  }

  @Override
  @Nullable
  public PsiElement getCompoundMul() {
    return findChildByType(COMPOUND_MUL);
  }

  @Override
  @Nullable
  public PsiElement getCompoundRightShift() {
    return findChildByType(COMPOUND_RIGHT_SHIFT);
  }

  @Override
  @Nullable
  public PsiElement getCompoundSub() {
    return findChildByType(COMPOUND_SUB);
  }

}
