package org.ballerinalang.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.BallerinaFieldDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaServiceEndpointAttachments;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing fields in anonymous configs in services.
 */
public class BallerinaAnonymousServiceConfigProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaAnonymousServiceConfigProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                                    boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaServiceEndpointAttachments;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            List<BallerinaFieldDefinition> fieldDefinitions = BallerinaPsiImplUtil.resolveConfig(myElement);
            BallerinaTypeDefinition typeDefinition = null;
            for (BallerinaFieldDefinition fieldDefinition : fieldDefinitions) {
                PsiElement identifier = fieldDefinition.getIdentifier();
                if (typeDefinition == null) {
                    typeDefinition = PsiTreeUtil.getParentOfType(identifier, BallerinaTypeDefinition.class);
                }
                if (typeDefinition == null || typeDefinition.getIdentifier() == null) {
                    continue;
                }
                String defaultValue = "";
                if (fieldDefinition.getExpression() != null) {
                    defaultValue = fieldDefinition.getExpression().getText();
                }
                if (myResult != null) {
                    myResult.addElement(BallerinaCompletionUtils.createFieldLookupElement(identifier,
                            typeDefinition.getIdentifier(), fieldDefinition.getTypeName().getText(), defaultValue,
                            null, true));
                } else if (myElement.getText().equals(identifier.getText())) {
                    add(identifier);
                    if (!isCompletion()) {
                        return false;
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean isCompletion() {
        return myIsCompletion;
    }

    @Override
    protected boolean crossOff(@NotNull PsiElement e) {
        return false;
    }
}
