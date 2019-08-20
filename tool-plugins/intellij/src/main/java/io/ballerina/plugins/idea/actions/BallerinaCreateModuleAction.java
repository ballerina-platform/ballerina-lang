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
package io.ballerina.plugins.idea.actions;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateDirectoryOrPackageAction;
import com.intellij.ide.actions.CreateDirectoryOrPackageHandler;
import com.intellij.ide.util.DirectoryChooserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import icons.BallerinaIcons;
import org.jetbrains.annotations.NotNull;

import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_CONFIG_FILE_NAME;
import static io.ballerina.plugins.idea.BallerinaConstants.BALLERINA_SRC_DIR_NAME;

/**
 * Handles creating new Ballerina modules.
 */
public class BallerinaCreateModuleAction extends CreateDirectoryOrPackageAction implements DumbAware {

    public BallerinaCreateModuleAction() {
        super();
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        final IdeView view = event.getData(LangDataKeys.IDE_VIEW);
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if (view == null || project == null) {
            return;
        }

        PsiDirectory dir = DirectoryChooserUtil.getOrChooseDirectory(view);
        if (dir == null) {
            return;
        }

        // This action is visible only for ballerina project root directory and src directory.
        if ((dir.findFile(BALLERINA_CONFIG_FILE_NAME) == null || dir.findSubdirectory(BALLERINA_SRC_DIR_NAME) == null)
                && !dir.getName().equals(BALLERINA_SRC_DIR_NAME)) {
            return;
        }

        // If user tries to create new module in the project root level.
        if (dir.findFile(BALLERINA_CONFIG_FILE_NAME) != null && dir.findSubdirectory(BALLERINA_SRC_DIR_NAME) != null) {
            dir = dir.findSubdirectory(BALLERINA_SRC_DIR_NAME);
        }

        final CreateDirectoryOrPackageHandler validator;
        final String message, title;
        validator = new CreateDirectoryOrPackageHandler(project, dir, true, "\\/");
        message = IdeBundle.message("prompt.enter.new.directory.name");
        title = IdeBundle.message("title.new.directory");
        String initialText = "";
        Messages.showInputDialog(project, message, title, null, initialText, validator,
                TextRange.from(initialText.length(), 0));

        final PsiElement result = validator.getCreatedElement();
        if (result != null) {
            view.selectElement(result);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Presentation presentation = event.getPresentation();

        Project project = event.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        IdeView view = event.getData(LangDataKeys.IDE_VIEW);
        if (view == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        final PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        // This action is visible only for ballerina project root directory and src directory.
        if ((dir.findFile(BALLERINA_CONFIG_FILE_NAME) == null || dir.findSubdirectory(BALLERINA_SRC_DIR_NAME) == null)
                && !dir.getName().equals(BALLERINA_SRC_DIR_NAME)) {
            presentation.setEnabledAndVisible(false);
            return;
        }

        presentation.setEnabledAndVisible(true);
        presentation.setText("Ballerina Module");
        presentation.setIcon(BallerinaIcons.ICON);
    }
}

