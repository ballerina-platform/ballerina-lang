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

public class BallerinaForkJoinStatementImpl extends ASTWrapperPsiElement implements BallerinaForkJoinStatement {

  public BallerinaForkJoinStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitForkJoinStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaJoinClause getJoinClause() {
    return findChildByClass(BallerinaJoinClause.class);
  }

  @Override
  @Nullable
  public BallerinaTimeoutClause getTimeoutClause() {
    return findChildByClass(BallerinaTimeoutClause.class);
  }

  @Override
  @NotNull
  public BallerinaTypeName getTypeName() {
    return findNotNullChildByClass(BallerinaTypeName.class);
  }

  @Override
  @NotNull
  public List<BallerinaWorkerDeclaration> getWorkerDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, BallerinaWorkerDeclaration.class);
  }

  @Override
  @NotNull
  public PsiElement getIdentifier() {
    return findNotNullChildByType(IDENTIFIER);
  }

}
