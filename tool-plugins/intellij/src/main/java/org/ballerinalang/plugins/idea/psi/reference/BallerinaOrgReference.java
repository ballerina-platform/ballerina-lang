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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.sdk.BallerinaPathModificationTracker;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Responsible for resolving organizations in imports.
 */
public class BallerinaOrgReference extends BallerinaCachedReference<BallerinaIdentifier> {

    public BallerinaOrgReference(@NotNull BallerinaIdentifier element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> organizationList = new LinkedList<>();
        organizationList.add(BallerinaCompletionUtils.createOrganizationLookup("ballerina"));
        List<VirtualFile> organizations = BallerinaPathModificationTracker.getAllOrganizationsInUserRepo();
        for (VirtualFile organization : organizations) {
            organizationList.add(BallerinaCompletionUtils.createOrganizationLookup(organization.getName()));
        }
        return organizationList.toArray(new LookupElement[organizationList.size()]);
    }
}
