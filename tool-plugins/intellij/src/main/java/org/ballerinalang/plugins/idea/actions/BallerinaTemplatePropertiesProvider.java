/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.plugins.idea.actions;

import com.intellij.ide.fileTemplates.DefaultTemplatePropertiesProvider;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiDirectory;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;

import java.util.Properties;

/**
 * Ballerina file template properties provider class which provides package name.
 */
public class BallerinaTemplatePropertiesProvider implements DefaultTemplatePropertiesProvider {

    private static final String BALLERINA_PACKAGE_NAME = FileTemplate.ATTRIBUTE_PACKAGE_NAME;

    @Override
    public void fillProperties(PsiDirectory directory, Properties props) {
        Module module = ModuleUtil.findModuleForPsiElement(directory);
        boolean isBallerinaModule = BallerinaSdkService.getInstance(directory.getProject()).isBallerinaModule(module);
        if (isBallerinaModule) {
            String packageForDirectory = BallerinaUtil.suggestPackageNameForDirectory(directory);
            props.setProperty(BALLERINA_PACKAGE_NAME, packageForDirectory);
        }
    }
}
