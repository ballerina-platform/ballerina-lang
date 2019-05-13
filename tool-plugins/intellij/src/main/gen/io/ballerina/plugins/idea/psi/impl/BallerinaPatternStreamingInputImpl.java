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

public class BallerinaPatternStreamingInputImpl extends ASTWrapperPsiElement implements BallerinaPatternStreamingInput {

  public BallerinaPatternStreamingInputImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitPatternStreamingInput(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BallerinaPatternStreamingEdgeInput> getPatternStreamingEdgeInputList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaPatternStreamingEdgeInput.class);
  }

  @Override
  @Nullable
  public BallerinaPatternStreamingInput getPatternStreamingInput() {
    return findChildByClass(BallerinaPatternStreamingInput.class);
  }

  @Override
  @Nullable
  public BallerinaTimeScale getTimeScale() {
    return findChildByClass(BallerinaTimeScale.class);
  }

  @Override
  @Nullable
  public PsiElement getAnd() {
    return findChildByType(AND);
  }

  @Override
  @Nullable
  public PsiElement getComma() {
    return findChildByType(COMMA);
  }

  @Override
  @Nullable
  public PsiElement getDecimalIntegerLiteral() {
    return findChildByType(DECIMAL_INTEGER_LITERAL);
  }

  @Override
  @Nullable
  public PsiElement getLeftParenthesis() {
    return findChildByType(LEFT_PARENTHESIS);
  }

  @Override
  @Nullable
  public PsiElement getNot() {
    return findChildByType(NOT);
  }

  @Override
  @Nullable
  public PsiElement getOr() {
    return findChildByType(OR);
  }

  @Override
  @Nullable
  public PsiElement getRightParenthesis() {
    return findChildByType(RIGHT_PARENTHESIS);
  }

  @Override
  @Nullable
  public PsiElement getBy() {
    return findChildByType(BY);
  }

  @Override
  @Nullable
  public PsiElement getFollowed() {
    return findChildByType(FOLLOWED);
  }

  @Override
  @Nullable
  public PsiElement getFor() {
    return findChildByType(FOR);
  }

  @Override
  @Nullable
  public PsiElement getForeach() {
    return findChildByType(FOREACH);
  }

}
