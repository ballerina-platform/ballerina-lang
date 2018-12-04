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

public class BallerinaAttachmentPointImpl extends BallerinaCompositeElementImpl implements BallerinaAttachmentPoint {

  public BallerinaAttachmentPointImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitAttachmentPoint(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getTypeParameter() {
    return findChildByType(TYPE_PARAMETER);
  }

  @Override
  @Nullable
  public PsiElement getAnnotation() {
    return findChildByType(ANNOTATION);
  }

  @Override
  @Nullable
  public PsiElement getClient() {
    return findChildByType(CLIENT);
  }

  @Override
  @Nullable
  public PsiElement getFunction() {
    return findChildByType(FUNCTION);
  }

  @Override
  @Nullable
  public PsiElement getListener() {
    return findChildByType(LISTENER);
  }

  @Override
  @Nullable
  public PsiElement getObject() {
    return findChildByType(OBJECT);
  }

  @Override
  @Nullable
  public PsiElement getRemote() {
    return findChildByType(REMOTE);
  }

  @Override
  @Nullable
  public PsiElement getResource() {
    return findChildByType(RESOURCE);
  }

  @Override
  @Nullable
  public PsiElement getService() {
    return findChildByType(SERVICE);
  }

  @Override
  @Nullable
  public PsiElement getType() {
    return findChildByType(TYPE);
  }

}
