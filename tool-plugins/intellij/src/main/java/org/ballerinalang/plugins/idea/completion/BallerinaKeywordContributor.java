package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.project.DumbAware;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import org.ballerinalang.plugins.idea.psi.reference.BallerinaNameReferenceReference;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * Keyword contributor which is responsible for keyword completion.
 */
public class BallerinaKeywordContributor extends CompletionContributor implements DumbAware {

    public BallerinaKeywordContributor() {
        extend(CompletionType.BASIC, isBallerinaNameReference(), new BallerinaKeywordCompletionProvider());
    }

    public PsiElementPattern.Capture<PsiElement> isBallerinaNameReference() {
        return psiElement().withReference(BallerinaNameReferenceReference.class);
    }
}
