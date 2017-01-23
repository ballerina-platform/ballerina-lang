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

public class BallerinaUnqualifiedTypeNameImpl extends ASTWrapperPsiElement implements BallerinaUnqualifiedTypeName {

  public BallerinaUnqualifiedTypeNameImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitUnqualifiedTypeName(this);
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
  public BallerinaSimpleTypeArray getSimpleTypeArray() {
    return findChildByClass(BallerinaSimpleTypeArray.class);
  }

  @Override
  @Nullable
  public BallerinaSimpleTypeIterate getSimpleTypeIterate() {
    return findChildByClass(BallerinaSimpleTypeIterate.class);
  }

  @Override
  @Nullable
  public BallerinaWithFullSchemaType getWithFullSchemaType() {
    return findChildByClass(BallerinaWithFullSchemaType.class);
  }

  @Override
  @Nullable
  public BallerinaWithFullSchemaTypeArray getWithFullSchemaTypeArray() {
    return findChildByClass(BallerinaWithFullSchemaTypeArray.class);
  }

  @Override
  @Nullable
  public BallerinaWithFullSchemaTypeIterate getWithFullSchemaTypeIterate() {
    return findChildByClass(BallerinaWithFullSchemaTypeIterate.class);
  }

  @Override
  @Nullable
  public BallerinaWithScheamIdTypeArray getWithScheamIdTypeArray() {
    return findChildByClass(BallerinaWithScheamIdTypeArray.class);
  }

  @Override
  @Nullable
  public BallerinaWithScheamIdTypeIterate getWithScheamIdTypeIterate() {
    return findChildByClass(BallerinaWithScheamIdTypeIterate.class);
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

  @Override
  @Nullable
  public BallerinaWithSchemaURLTypeArray getWithSchemaURLTypeArray() {
    return findChildByClass(BallerinaWithSchemaURLTypeArray.class);
  }

  @Override
  @Nullable
  public BallerinaWithSchemaURLTypeIterate getWithSchemaURLTypeIterate() {
    return findChildByClass(BallerinaWithSchemaURLTypeIterate.class);
  }

}
