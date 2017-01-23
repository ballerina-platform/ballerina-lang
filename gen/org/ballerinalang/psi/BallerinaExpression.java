// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaExpression extends PsiElement {

  @NotNull
  List<BallerinaActionInvocation> getActionInvocationList();

  @NotNull
  List<BallerinaArgumentList> getArgumentListList();

  @NotNull
  List<BallerinaBacktickString> getBacktickStringList();

  @NotNull
  List<BallerinaExpressionList> getExpressionListList();

  @NotNull
  List<BallerinaFunctionName> getFunctionNameList();

  @NotNull
  List<BallerinaLiteralValue> getLiteralValueList();

  @NotNull
  List<BallerinaMapInitKeyValueList> getMapInitKeyValueListList();

  @NotNull
  List<BallerinaPackageName> getPackageNameList();

  @Nullable
  BallerinaTypeName getTypeName();

  @NotNull
  List<BallerinaVariableReference> getVariableReferenceList();

}
