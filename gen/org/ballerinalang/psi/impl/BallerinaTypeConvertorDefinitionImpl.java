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

public class BallerinaTypeConvertorDefinitionImpl extends ASTWrapperPsiElement implements BallerinaTypeConvertorDefinition {

  public BallerinaTypeConvertorDefinitionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitTypeConvertorDefinition(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<BallerinaTypeConverterTypes> getTypeConverterTypesList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaTypeConverterTypes.class);
  }

  @Override
  @NotNull
  public BallerinaTypeConvertorBody getTypeConvertorBody() {
    return findNotNullChildByClass(BallerinaTypeConvertorBody.class);
  }

}
