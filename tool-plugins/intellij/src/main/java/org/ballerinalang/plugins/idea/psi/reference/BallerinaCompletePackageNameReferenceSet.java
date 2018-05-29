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
 *
 */

package org.ballerinalang.plugins.idea.psi.reference;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import io.ballerina.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.psi.BallerinaImportDeclaration;
import org.ballerinalang.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.sdk.BallerinaPathModificationTracker;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;

/**
 * Reference set used to resolve packages.
 */
public class BallerinaCompletePackageNameReferenceSet extends FileReferenceSet {

    public BallerinaCompletePackageNameReferenceSet(@NotNull PsiElement element) {
        super(element);
    }

    //    public String getSeparatorString() {
    //        return ".";
    //    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {

        // Todo - If the org name is ballerina, then the context should be src in SDK. If there are no org specified,
        // the local project should be the context (hide directories starting with ".").
        PsiFile file = getContainingFile();
        if (file == null || !file.isValid() || isAbsolutePathReference()) {
            return Collections.emptyList();
        }

        PsiManager psiManager = file.getManager();
        Module module = ModuleUtilCore.findModuleForPsiElement(file);
        Project project = file.getProject();

        LinkedHashSet<VirtualFile> sourceRoots = new LinkedHashSet<>();
        PsiElement element = getElement();
        BallerinaImportDeclaration ballerinaImportDeclaration = PsiTreeUtil.getParentOfType(element,
                BallerinaImportDeclaration.class);
        if (ballerinaImportDeclaration != null) {
            BallerinaOrgName ballerinaOrgName = PsiTreeUtil.getChildOfType(ballerinaImportDeclaration,
                    BallerinaOrgName.class);
            if (ballerinaOrgName == null) {
                if (module != null) {
                    VirtualFile moduleFile = module.getModuleFile();
                    if (moduleFile != null) {
                        sourceRoots.add(moduleFile.getParent());
                    }
                }
            } else {
                String organizationName = ballerinaOrgName.getText();
                if (BallerinaConstants.BALLERINA_ORG_NAME.equals(organizationName)) {
                    // Add source roots in SDK.
                    sourceRoots.addAll(BallerinaSdkUtil.getSourcesPathsToLookup(project, module));
                } else {
                    ContainerUtil.addIfNotNull(sourceRoots,
                            BallerinaPathModificationTracker.getOrganizationInUserRepo(organizationName));
                }
            }
        }
        return ContainerUtil.mapNotNull(sourceRoots, psiManager::findDirectory);
    }

    @Override
    protected Condition<PsiFileSystemItem> getReferenceCompletionFilter() {
        return psiFileSystemItem -> psiFileSystemItem.isDirectory() && !psiFileSystemItem.getName().startsWith(".");
    }

    public boolean absoluteUrlNeedsStartSlash() {
        return false;
    }

    @Override
    public boolean isEndingSlashNotAllowed() {
        return true;
    }

    @Override
    public boolean couldBeConvertedTo(boolean relative) {
        return false;
    }

    @Override
    public boolean isAbsolutePathReference() {
        return super.isAbsolutePathReference();
    }

    @NotNull
    @Override
    public FileReference createFileReference(TextRange range, int index, String text) {
        return new BallerinaCompletePackageNameReference(this, range, index, text);
    }
}
