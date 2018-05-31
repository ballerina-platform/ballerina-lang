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

package io.ballerina.plugins.idea.runconfig.ui;

import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Producer;
import com.intellij.util.TextFieldCompletionProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Provides support to lists packages in settings UIs.
 */
public class BallerinaPackageFieldCompletionProvider extends TextFieldCompletionProvider {

    @NotNull
    private final Producer<Module> myModuleProducer;

    BallerinaPackageFieldCompletionProvider(@NotNull Producer<Module> moduleProducer) {
        myModuleProducer = moduleProducer;
    }

    @Override
    protected void addCompletionVariants(@NotNull String text, int offset, @NotNull String prefix,
                                         @NotNull CompletionResultSet result) {
        Module module = myModuleProducer.produce();
        if (module != null) {
            Project project = module.getProject();
            VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
            for (VirtualFile contentRoot : contentRoots) {
                addDirectories(result, contentRoot.getPath(), contentRoot);
            }
        }
    }

    private void addDirectories(@NotNull CompletionResultSet result, @NotNull String root,
                                @NotNull VirtualFile directory) {
        VirtualFile[] children = directory.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                if (child.getName().startsWith(".")) {
                    continue;
                }
                // Path delimiter will be '/' on Linux and Windows OS.
                String relativePath = child.getPath().replaceFirst(root + File.separator, "");
                result.addElement(LookupElementBuilder.create(relativePath));
                addDirectories(result, root, child);
            }
        }
    }
}
