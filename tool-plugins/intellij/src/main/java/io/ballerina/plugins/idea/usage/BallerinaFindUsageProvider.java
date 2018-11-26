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

package io.ballerina.plugins.idea.usage;

import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import io.ballerina.plugins.idea.psi.BallerinaAnnotationDefinition;
import io.ballerina.plugins.idea.psi.BallerinaAnyIdentifierName;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaGlobalVariableDefinition;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.BallerinaNamedPattern;
import io.ballerina.plugins.idea.psi.BallerinaObjectFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaObjectInitializer;
import io.ballerina.plugins.idea.psi.BallerinaObjectParameter;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.BallerinaPackageReference;
import io.ballerina.plugins.idea.psi.BallerinaParameterWithType;
import io.ballerina.plugins.idea.psi.BallerinaRestParameter;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaVariableDefinitionStatement;
import io.ballerina.plugins.idea.psi.BallerinaWorkerDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Find usage provider for Ballerina.
 */
public class BallerinaFindUsageProvider implements FindUsagesProvider {

    @Nullable
    @Override
    public WordsScanner getWordsScanner() {
        return null;
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof BallerinaIdentifier;
    }

    @Nullable
    @Override
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @NotNull
    @Override
    public String getType(@NotNull PsiElement element) {
        PsiElement parent = element.getParent();
        PsiElement superParent = parent.getParent();
        if (parent instanceof BallerinaAnyIdentifierName) {
            if (superParent instanceof BallerinaCallableUnitSignature) {
                return "Function";
            }
        } else if (parent instanceof BallerinaPackageReference) {
            return "Package";
        } else if (parent instanceof BallerinaOrgName) {
            return "Organization";
        } else if (parent instanceof BallerinaGlobalVariableDefinition) {
            return "Global Variable";
        } else if (parent instanceof BallerinaTypeDefinition) {
            return "Type";
        } else if (parent instanceof BallerinaParameterWithType) {
            return "Parameter";
        } else if (parent instanceof BallerinaWorkerDefinition) {
            return "Worker";
        } else if (parent instanceof BallerinaAnnotationDefinition) {
            return "Annotation";
        } else if (parent instanceof BallerinaObjectFieldDefinition) {
            return "Object Field";
        } else if (parent instanceof BallerinaObjectParameter) {
            return "Object Parameter";
        } else if (parent instanceof BallerinaVariableDefinitionStatement) {
            return "Variable";
        } else if (parent instanceof BallerinaNamedPattern) {
            return "Variable";
        } else if (parent instanceof BallerinaRestParameter) {
            return "Parameter";
        } else if (parent instanceof BallerinaObjectInitializer) {
            return "Object Initializer";
        }
        return "";
    }

    @NotNull
    @Override
    public String getDescriptiveName(@NotNull PsiElement element) {
        return element.getText();
    }

    @NotNull
    @Override
    public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return element.getText();
    }
}
