// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaServiceBodyDeclaration extends PsiElement {

  @NotNull
  List<BallerinaConnectorDeclaration> getConnectorDeclarationList();

  @NotNull
  List<BallerinaResourceDefinition> getResourceDefinitionList();

  @NotNull
  List<BallerinaVariableDeclaration> getVariableDeclarationList();

}
