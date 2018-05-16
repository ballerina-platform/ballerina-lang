package org.ballerinalang.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.completion.inserthandlers.ColonInsertHandler;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.BallerinaImportDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaPackageReference;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Responsible for resolving and completing package names.
 */
public class BallerinaPackageNameProcessor extends BallerinaScopeProcessorBase {

    @Nullable
    private final CompletionResultSet myResult;
    @NotNull
    private final PsiElement myElement;

    public BallerinaPackageNameProcessor(@Nullable CompletionResultSet result, @NotNull PsiElement element,
                                         boolean isCompletion) {
        super(element, element, isCompletion);
        myResult = result;
        myElement = element;
    }

    protected boolean accept(@NotNull PsiElement element) {
        return element instanceof BallerinaFile;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        ProgressManager.checkCanceled();
        if (accept(element)) {
            // If we are looking for annotations in a package, don't suggest packages.
            if (myElement.getPrevSibling() instanceof BallerinaPackageReference) {
                return true;
            }
            List<BallerinaImportDeclaration> cachedImports = ((BallerinaFile) element).getCachedImports();
            for (BallerinaImportDeclaration cachedImport : cachedImports) {
                PsiElement identifier = cachedImport.getShortPackageName();
                if (identifier != null) {
                    if (myResult != null) {
                        myResult.addElement(BallerinaCompletionUtils.createPackageLookup(identifier,
                                ColonInsertHandler.INSTANCE_WITH_AUTO_POPUP));
                    } else if (myElement.getText().equals(identifier.getText())) {
                        add(identifier);
                    }
                }
            }
            // Todo - Add all un-imported imports.
            if (myResult != null) {
                Module module = ModuleUtilCore.findModuleForPsiElement(element);
                if (module != null) {
                    myResult.addAllElements(BallerinaPsiImplUtil.getAllUnImportedImports(element.getProject(), module,
                            cachedImports));
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
