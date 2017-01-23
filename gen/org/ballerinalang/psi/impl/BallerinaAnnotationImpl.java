// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.ballerinalang.psi.BallerinaTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.ballerinalang.psi.*;

public class BallerinaAnnotationImpl extends ASTWrapperPsiElement implements BallerinaAnnotation {

  public BallerinaAnnotationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitAnnotation(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public BallerinaAnnotationName getAnnotationName() {
    return findNotNullChildByClass(BallerinaAnnotationName.class);
  }

  @Override
  @Nullable
  public BallerinaElementValue getElementValue() {
    return findChildByClass(BallerinaElementValue.class);
  }

  @Override
  @Nullable
  public BallerinaElementValuePairs getElementValuePairs() {
    return findChildByClass(BallerinaElementValuePairs.class);
  }

}
