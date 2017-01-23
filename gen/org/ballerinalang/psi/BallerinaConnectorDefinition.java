// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaConnectorDefinition extends PsiElement {

  @NotNull
  List<BallerinaAnnotation> getAnnotationList();

  @NotNull
  BallerinaConnectorBody getConnectorBody();

  @NotNull
  BallerinaParameterList getParameterList();

  @NotNull
  PsiElement getIdentifier();

}
