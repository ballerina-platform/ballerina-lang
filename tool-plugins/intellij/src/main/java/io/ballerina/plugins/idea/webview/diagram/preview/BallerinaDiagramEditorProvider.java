/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.plugins.idea.webview.diagram.preview;

import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.lang.LanguageUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import io.ballerina.plugins.idea.BallerinaFileType;
import io.ballerina.plugins.idea.BallerinaLanguage;
import org.jetbrains.annotations.NotNull;

/**
 * Ballerina diagram editor provider implementation.
 */
public class BallerinaDiagramEditorProvider extends WeighedFileEditorProvider {
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        final FileType fileType = file.getFileType();

        return (fileType == BallerinaFileType.INSTANCE || (fileType == ScratchFileType.INSTANCE
                && LanguageUtil.getLanguageForPsi(project, file) == BallerinaLanguage.INSTANCE));
    }

    @NotNull
    @Override
    public FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new BallerinaDiagramEditor(project, file);
    }

    @NotNull
    @Override
    public String getEditorTypeId() {
        return "ballerina-diagram-preview";
    }

    @NotNull
    @Override
    public FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}
