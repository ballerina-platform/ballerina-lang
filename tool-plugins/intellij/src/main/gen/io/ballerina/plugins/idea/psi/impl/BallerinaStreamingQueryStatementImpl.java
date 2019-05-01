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

public class BallerinaStreamingQueryStatementImpl extends ASTWrapperPsiElement implements BallerinaStreamingQueryStatement {

  public BallerinaStreamingQueryStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitStreamingQueryStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaJoinStreamingInput getJoinStreamingInput() {
    return findChildByClass(BallerinaJoinStreamingInput.class);
  }

  @Override
  @Nullable
  public BallerinaOrderByClause getOrderByClause() {
    return findChildByClass(BallerinaOrderByClause.class);
  }

  @Override
  @Nullable
  public BallerinaOutputRateLimit getOutputRateLimit() {
    return findChildByClass(BallerinaOutputRateLimit.class);
  }

  @Override
  @Nullable
  public BallerinaPatternClause getPatternClause() {
    return findChildByClass(BallerinaPatternClause.class);
  }

  @Override
  @Nullable
  public BallerinaSelectClause getSelectClause() {
    return findChildByClass(BallerinaSelectClause.class);
  }

  @Override
  @Nullable
  public BallerinaStreamingAction getStreamingAction() {
    return findChildByClass(BallerinaStreamingAction.class);
  }

  @Override
  @Nullable
  public BallerinaStreamingInput getStreamingInput() {
    return findChildByClass(BallerinaStreamingInput.class);
  }

  @Override
  @NotNull
  public PsiElement getFrom() {
    return findNotNullChildByType(FROM);
  }

}
