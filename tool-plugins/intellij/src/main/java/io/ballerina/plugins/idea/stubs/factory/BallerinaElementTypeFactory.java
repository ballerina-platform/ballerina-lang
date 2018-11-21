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

package io.ballerina.plugins.idea.stubs.factory;

import com.intellij.psi.stubs.IStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaAliasStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaAnnotationDefinitionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaEndpointDefinitionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaFunctionDefinitionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaGlobalEndpointDefinitionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaGlobalVariableDefinitionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaNameReferenceStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaNamespaceDeclarationStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaOrgNameStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaPackageNameStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaPackageReferenceStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaPackageVersionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaTypeDefinitionStubElementType;
import io.ballerina.plugins.idea.stubs.types.BallerinaWorkerDefinitionStubElementType;
import org.jetbrains.annotations.NotNull;

/**
 * Responsible for creating stub elements.
 */
public class BallerinaElementTypeFactory {

    private BallerinaElementTypeFactory() {
    }

    public static IStubElementType stubFactory(@NotNull String name) {
        // NOTE - If the element type is wrong, an error will occur while loading the lexer in syntax highlighting.
        switch (name) {
            case "FUNCTION_DEFINITION":
                return new BallerinaFunctionDefinitionStubElementType(name);
            case "TYPE_DEFINITION":
                return new BallerinaTypeDefinitionStubElementType(name);
            case "GLOBAL_VARIABLE_DEFINITION":
                return new BallerinaGlobalVariableDefinitionStubElementType(name);
            case "ANNOTATION_DEFINITION":
                return new BallerinaAnnotationDefinitionStubElementType(name);
            case "GLOBAL_ENDPOINT_DEFINITION":
                return new BallerinaGlobalEndpointDefinitionStubElementType(name);
            case "ENDPOINT_DEFINITION":
                return new BallerinaEndpointDefinitionStubElementType(name);
            case "WORKER_DEFINITION":
                return new BallerinaWorkerDefinitionStubElementType(name);
            case "PACKAGE_NAME":
                return new BallerinaPackageNameStubElementType(name);
            case "ORG_NAME":
                return new BallerinaOrgNameStubElementType(name);
            case "PACKAGE_VERSION":
                return new BallerinaPackageVersionStubElementType(name);
            case "ALIAS":
                return new BallerinaAliasStubElementType(name);
            case "NAME_REFERENCE":
                return new BallerinaNameReferenceStubElementType(name);
            case "PACKAGE_REFERENCE":
                return new BallerinaPackageReferenceStubElementType(name);
            case "NAMESPACE_DECLARATION":
                return new BallerinaNamespaceDeclarationStubElementType(name);
        }
        throw new RuntimeException("Unknown element type: " + name);
    }
}
