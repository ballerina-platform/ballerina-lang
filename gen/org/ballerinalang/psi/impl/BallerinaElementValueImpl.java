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

public class BallerinaElementValueImpl extends ASTWrapperPsiElement implements BallerinaElementValue {

  public BallerinaElementValueImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitElementValue(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaAnnotation getAnnotation() {
    return findChildByClass(BallerinaAnnotation.class);
  }

  @Override
  @Nullable
  public BallerinaElementValueArrayInitializer getElementValueArrayInitializer() {
    return findChildByClass(BallerinaElementValueArrayInitializer.class);
  }

  @Override
  @Nullable
  public BallerinaExpression getExpression() {
    return findChildByClass(BallerinaExpression.class);
  }

}
