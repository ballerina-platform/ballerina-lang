/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class BallerinaBuiltInReferenceTypeNameImpl extends ASTWrapperPsiElement implements BallerinaBuiltInReferenceTypeName {

  public BallerinaBuiltInReferenceTypeNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitBuiltInReferenceTypeName(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaErrorTypeName getErrorTypeName() {
    return findChildByClass(BallerinaErrorTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaFunctionTypeName getFunctionTypeName() {
    return findChildByClass(BallerinaFunctionTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaFutureTypeName getFutureTypeName() {
    return findChildByClass(BallerinaFutureTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaJsonTypeName getJsonTypeName() {
    return findChildByClass(BallerinaJsonTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaMapTypeName getMapTypeName() {
    return findChildByClass(BallerinaMapTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaServiceTypeName getServiceTypeName() {
    return findChildByClass(BallerinaServiceTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaStreamTypeName getStreamTypeName() {
    return findChildByClass(BallerinaStreamTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaTableTypeName getTableTypeName() {
    return findChildByClass(BallerinaTableTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaTypeDescReferenceTypeName getTypeDescReferenceTypeName() {
    return findChildByClass(BallerinaTypeDescReferenceTypeName.class);
  }

  @Override
  @Nullable
  public BallerinaXmlTypeName getXmlTypeName() {
    return findChildByClass(BallerinaXmlTypeName.class);
  }

}
