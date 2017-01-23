// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaStatement extends PsiElement {

  @Nullable
  BallerinaActionInvocationStatement getActionInvocationStatement();

  @Nullable
  BallerinaAssignmentStatement getAssignmentStatement();

  @Nullable
  BallerinaBreakStatement getBreakStatement();

  @Nullable
  BallerinaCommentStatement getCommentStatement();

  @Nullable
  BallerinaForkJoinStatement getForkJoinStatement();

  @Nullable
  BallerinaFunctionInvocationStatement getFunctionInvocationStatement();

  @Nullable
  BallerinaIfElseStatement getIfElseStatement();

  @Nullable
  BallerinaIterateStatement getIterateStatement();

  @Nullable
  BallerinaReplyStatement getReplyStatement();

  @Nullable
  BallerinaReturnStatement getReturnStatement();

  @Nullable
  BallerinaThrowStatement getThrowStatement();

  @Nullable
  BallerinaTryCatchStatement getTryCatchStatement();

  @Nullable
  BallerinaWhileStatement getWhileStatement();

  @Nullable
  BallerinaWorkerInteractionStatement getWorkerInteractionStatement();

}
