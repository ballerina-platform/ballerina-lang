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

public class BallerinaReferenceTypeImpl extends ASTWrapperPsiElement implements BallerinaReferenceType {

  public BallerinaReferenceTypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitReferenceType(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getDocannotation() {
    return findChildByType(DOCANNOTATION);
  }

  @Override
  @Nullable
  public PsiElement getDocconst() {
    return findChildByType(DOCCONST);
  }

  @Override
  @Nullable
  public PsiElement getDocfunction() {
    return findChildByType(DOCFUNCTION);
  }

  @Override
  @Nullable
  public PsiElement getDocmodule() {
    return findChildByType(DOCMODULE);
  }

  @Override
  @Nullable
  public PsiElement getDocparameter() {
    return findChildByType(DOCPARAMETER);
  }

  @Override
  @Nullable
  public PsiElement getDocservice() {
    return findChildByType(DOCSERVICE);
  }

  @Override
  @Nullable
  public PsiElement getDoctype() {
    return findChildByType(DOCTYPE);
  }

  @Override
  @Nullable
  public PsiElement getDocvar() {
    return findChildByType(DOCVAR);
  }

  @Override
  @Nullable
  public PsiElement getDocvariable() {
    return findChildByType(DOCVARIABLE);
  }

}
