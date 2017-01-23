// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaJoinClause extends PsiElement {

  @NotNull
  BallerinaJoinConditions getJoinConditions();

  @NotNull
  List<BallerinaStatement> getStatementList();

  @NotNull
  BallerinaTypeName getTypeName();

  @NotNull
  PsiElement getIdentifier();

}
