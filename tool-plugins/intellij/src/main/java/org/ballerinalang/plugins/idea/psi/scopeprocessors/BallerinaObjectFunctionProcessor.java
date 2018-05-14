package org.ballerinalang.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.inserthandlers.SmartParenthesisInsertHandler;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectCallableUnitSignature;
import org.ballerinalang.plugins.idea.psi.BallerinaObjectFunctionDefinition;
import org.ballerinalang.plugins.idea.psi.BallerinaTypeDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Responsible for resolving and completing object functions.
 */
public class BallerinaObjectFunctionProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaObjectFunctionProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                            boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaTypeDefinition;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            PsiElement owner = ((BallerinaTypeDefinition) element).getIdentifier();
            if (owner == null) {
                return true;
            }
            Collection<BallerinaObjectFunctionDefinition> objectFunctionDefinitions =
                    PsiTreeUtil.findChildrenOfType(element, BallerinaObjectFunctionDefinition.class);

            for (BallerinaObjectFunctionDefinition objectFunctionDefinition : objectFunctionDefinitions) {
                BallerinaObjectCallableUnitSignature objectCallableUnitSignature =
                        objectFunctionDefinition.getObjectCallableUnitSignature();
                if (objectCallableUnitSignature == null) {
                    continue;
                }

                PsiElement identifier = objectCallableUnitSignature.getAnyIdentifierName().getIdentifier();
                if (identifier != null) {
                    if (myResult != null) {
                        myResult.addElement(BallerinaCompletionUtils.createFunctionLookupElement
                                (objectFunctionDefinition, owner, SmartParenthesisInsertHandler.INSTANCE));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                }
            }
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
