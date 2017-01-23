// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaIfElseStatement extends PsiElement {

  @Nullable
  BallerinaElseClause getElseClause();

  @NotNull
  List<BallerinaElseIfClause> getElseIfClauseList();

  @NotNull
  BallerinaExpression getExpression();

  @NotNull
  List<BallerinaStatement> getStatementList();

}
