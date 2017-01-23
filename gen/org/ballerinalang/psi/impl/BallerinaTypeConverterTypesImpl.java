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

public class BallerinaTypeConverterTypesImpl extends ASTWrapperPsiElement implements BallerinaTypeConverterTypes {

  public BallerinaTypeConverterTypesImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitTypeConverterTypes(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaSimpleType getSimpleType() {
    return findChildByClass(BallerinaSimpleType.class);
  }

  @Override
  @Nullable
  public BallerinaWithFullSchemaType getWithFullSchemaType() {
    return findChildByClass(BallerinaWithFullSchemaType.class);
  }

  @Override
  @Nullable
  public BallerinaWithScheamURLType getWithScheamURLType() {
    return findChildByClass(BallerinaWithScheamURLType.class);
  }

  @Override
  @Nullable
  public BallerinaWithSchemaIdType getWithSchemaIdType() {
    return findChildByClass(BallerinaWithSchemaIdType.class);
  }

}
