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

public class BallerinaWorkerInteractionStatementImpl extends ASTWrapperPsiElement implements BallerinaWorkerInteractionStatement {

  public BallerinaWorkerInteractionStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitWorkerInteractionStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaTriggerWorker getTriggerWorker() {
    return findChildByClass(BallerinaTriggerWorker.class);
  }

  @Override
  @Nullable
  public BallerinaWorkerReply getWorkerReply() {
    return findChildByClass(BallerinaWorkerReply.class);
  }

}
