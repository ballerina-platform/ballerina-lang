/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.psi.scopeprocessors;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.completion.inserthandlers.ColonInsertHandler;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaImportDeclaration;
import io.ballerina.plugins.idea.psi.BallerinaPackageReference;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
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
            // Add un-imported packages. These will be auto-imported.
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
