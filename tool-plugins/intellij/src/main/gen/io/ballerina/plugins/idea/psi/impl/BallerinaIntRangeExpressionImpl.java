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
import io.ballerina.plugins.idea.psi.*;

public class BallerinaIntRangeExpressionImpl extends BallerinaCompositeElementImpl implements BallerinaIntRangeExpression {

  public BallerinaIntRangeExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitIntRangeExpression(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BallerinaExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaExpression.class);
  }

  @Override
  @Nullable
  public PsiElement getLeftBracket() {
    return findChildByType(LEFT_BRACKET);
  }

  @Override
  @Nullable
  public PsiElement getLeftParenthesis() {
    return findChildByType(LEFT_PARENTHESIS);
  }

  @Override
  @NotNull
  public PsiElement getRange() {
    return notNullChild(findChildByType(RANGE));
  }

  @Override
  @Nullable
  public PsiElement getRightBracket() {
    return findChildByType(RIGHT_BRACKET);
  }

  @Override
  @Nullable
  public PsiElement getRightParenthesis() {
    return findChildByType(RIGHT_PARENTHESIS);
  }

}
