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

public class BallerinaStatementImpl extends ASTWrapperPsiElement implements BallerinaStatement {

  public BallerinaStatementImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull BallerinaVisitor visitor) {
    visitor.visitStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof BallerinaVisitor) accept((BallerinaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public BallerinaActionInvocationStatement getActionInvocationStatement() {
    return findChildByClass(BallerinaActionInvocationStatement.class);
  }

  @Override
  @Nullable
  public BallerinaAssignmentStatement getAssignmentStatement() {
    return findChildByClass(BallerinaAssignmentStatement.class);
  }

  @Override
  @Nullable
  public BallerinaBreakStatement getBreakStatement() {
    return findChildByClass(BallerinaBreakStatement.class);
  }

  @Override
  @Nullable
  public BallerinaCommentStatement getCommentStatement() {
    return findChildByClass(BallerinaCommentStatement.class);
  }

  @Override
  @Nullable
  public BallerinaForkJoinStatement getForkJoinStatement() {
    return findChildByClass(BallerinaForkJoinStatement.class);
  }

  @Override
  @Nullable
  public BallerinaFunctionInvocationStatement getFunctionInvocationStatement() {
    return findChildByClass(BallerinaFunctionInvocationStatement.class);
  }

  @Override
  @Nullable
  public BallerinaIfElseStatement getIfElseStatement() {
    return findChildByClass(BallerinaIfElseStatement.class);
  }

  @Override
  @Nullable
  public BallerinaIterateStatement getIterateStatement() {
    return findChildByClass(BallerinaIterateStatement.class);
  }

  @Override
  @Nullable
  public BallerinaReplyStatement getReplyStatement() {
    return findChildByClass(BallerinaReplyStatement.class);
  }

  @Override
  @Nullable
  public BallerinaReturnStatement getReturnStatement() {
    return findChildByClass(BallerinaReturnStatement.class);
  }

  @Override
  @Nullable
  public BallerinaThrowStatement getThrowStatement() {
    return findChildByClass(BallerinaThrowStatement.class);
  }

  @Override
  @Nullable
  public BallerinaTryCatchStatement getTryCatchStatement() {
    return findChildByClass(BallerinaTryCatchStatement.class);
  }

  @Override
  @Nullable
  public BallerinaWhileStatement getWhileStatement() {
    return findChildByClass(BallerinaWhileStatement.class);
  }

  @Override
  @Nullable
  public BallerinaWorkerInteractionStatement getWorkerInteractionStatement() {
    return findChildByClass(BallerinaWorkerInteractionStatement.class);
  }

}
