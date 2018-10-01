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

public class BallerinaTimeScaleImpl extends BallerinaCompositeElementImpl implements BallerinaTimeScale {

  public BallerinaTimeScaleImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitTimeScale(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getDay() {
    return findChildByType(DAY);
  }

  @Override
  @Nullable
  public PsiElement getDays() {
    return findChildByType(DAYS);
  }

  @Override
  @Nullable
  public PsiElement getHour() {
    return findChildByType(HOUR);
  }

  @Override
  @Nullable
  public PsiElement getHours() {
    return findChildByType(HOURS);
  }

  @Override
  @Nullable
  public PsiElement getMinute() {
    return findChildByType(MINUTE);
  }

  @Override
  @Nullable
  public PsiElement getMinutes() {
    return findChildByType(MINUTES);
  }

  @Override
  @Nullable
  public PsiElement getMonth() {
    return findChildByType(MONTH);
  }

  @Override
  @Nullable
  public PsiElement getMonths() {
    return findChildByType(MONTHS);
  }

  @Override
  @Nullable
  public PsiElement getSecond() {
    return findChildByType(SECOND);
  }

  @Override
  @Nullable
  public PsiElement getSeconds() {
    return findChildByType(SECONDS);
  }

  @Override
  @Nullable
  public PsiElement getYear() {
    return findChildByType(YEAR);
  }

  @Override
  @Nullable
  public PsiElement getYears() {
    return findChildByType(YEARS);
  }

}
