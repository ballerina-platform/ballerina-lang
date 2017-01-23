// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaResourceDefinition extends PsiElement {

  @NotNull
  List<BallerinaAnnotation> getAnnotationList();

  @NotNull
  BallerinaFunctionBody getFunctionBody();

  @NotNull
  BallerinaParameterList getParameterList();

  @NotNull
  PsiElement getIdentifier();

}
