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

package io.ballerina.plugins.idea.actions;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import io.ballerina.plugins.idea.BallerinaIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Handles creating new Ballerina file.
 */
public class BallerinaCreateFileAction extends CreateFileFromTemplateAction implements DumbAware {

    private static final String BALLERINA_EMPTY_FILE = "Ballerina File";
    private static final String BALLERINA_MAIN = "Ballerina Main";
    private static final String BALLERINA_SERVICE = "Ballerina Service";

    private static final String NEW_BALLERINA_FILE = "New Ballerina File";
    private static final String DEFAULT_BALLERINA_TEMPLATE_PROPERTY = "Empty file";

    public BallerinaCreateFileAction() {
        super(NEW_BALLERINA_FILE, "", BallerinaIcons.ICON);
    }

    @Override
    protected void buildDialog(Project project, PsiDirectory directory,
                               @NotNull CreateFileFromTemplateDialog.Builder builder) {
        builder.setTitle(NEW_BALLERINA_FILE).addKind(BALLERINA_MAIN, BallerinaIcons.ICON, BALLERINA_MAIN)
                .addKind(BALLERINA_SERVICE, BallerinaIcons.ICON, BALLERINA_SERVICE)
                .addKind(BALLERINA_EMPTY_FILE, BallerinaIcons.ICON, BALLERINA_EMPTY_FILE);
    }

    @Nullable
    @Override
    protected String getDefaultTemplateProperty() {
        return DEFAULT_BALLERINA_TEMPLATE_PROPERTY;
    }

    @NotNull
    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName) {
        return NEW_BALLERINA_FILE;
    }


    @Override
    protected void postProcess(PsiFile createdElement, String templateName, Map<String, String> customProperties) {

    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BallerinaCreateFileAction;
    }
}
