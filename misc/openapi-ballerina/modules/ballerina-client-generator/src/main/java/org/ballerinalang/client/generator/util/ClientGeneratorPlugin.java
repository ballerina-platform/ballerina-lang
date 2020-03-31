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

package org.ballerinalang.client.generator.util;

import org.ballerinalang.client.generator.CodeGenerator;
import org.ballerinalang.client.generator.GeneratorConstants;
import org.ballerinalang.client.generator.exception.ClientGeneratorException;
import org.ballerinalang.client.generator.model.ClientContextHolder;
import org.ballerinalang.client.generator.model.FileDefinitionHolder;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Compiler plugin for ballerina client code generation.
 */
@SupportedAnnotationPackages(value = {"ballerina/openapi"})
public class ClientGeneratorPlugin extends AbstractCompilerPlugin {
    List<BLangSimpleVariable> endpoints;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.endpoints = new ArrayList<>();
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        PrintStream err = System.err;
        AnnotationAttachmentNode config = GeneratorUtils
                .getAnnotationFromList("ClientConfig", GeneratorConstants.OPENAPI_PKG_ALIAS,
                        annotations);

        // Generate client only if requested by providing the client config annotation
        if (isClientGenerationEnabled(config)) {
            try {
                // Build client context.
                ClientContextHolder context = ClientContextHolder.buildContext((BLangService) serviceNode, endpoints);
                String fileName = serviceNode.getName().getValue().toLowerCase(Locale.ENGLISH) + "_client.bal";
                String generatedSource = CodeGenerator.generateOutput(GeneratorConstants.GenType.CLIENT, context);
                FileDefinitionHolder.getInstance().addDefinition(fileName, generatedSource);
            } catch (ClientGeneratorException e) {
                err.println("Client code was not generated: " + e.getMessage());
            }
        }
    }

    @Override
    public void process(PackageNode packageNode) {
        // Collect endpoints throughout the package.
        for (CompilationUnitNode compilationUnitNode : packageNode.getCompilationUnits()) {
            for (TopLevelNode topLevelNode : compilationUnitNode.getTopLevelNodes()) {
                if (topLevelNode instanceof BLangSimpleVariable
                        && ((BLangSimpleVariable) topLevelNode).getFlags().contains(Flag.LISTENER)
                        && isAnClientEnabledEndpoint((BLangSimpleVariable) topLevelNode)) {
                    endpoints.add((BLangSimpleVariable) topLevelNode);
                }
            }
        }
    }

    @Override
    public void codeGenerated(PackageID packageID, Path binaryPath) {
        PrintStream err = System.err;
        Map<String, String> definitionMap = FileDefinitionHolder.getInstance().getFileDefinitionMap();
        if (binaryPath == null) {
            err.print("Error while generating client for the service. Binary file path is null");
            return;
        }
        Path filePath = binaryPath.toAbsolutePath();
        Path parentDirPath = filePath.getParent();
        if (parentDirPath == null) {
            parentDirPath = filePath;
        }
        Path targetDirPath = Paths.get(parentDirPath.toString(), "client");
        for (Map.Entry<String, String> entry : definitionMap.entrySet()) {
            try {
                CodeGenerator.writeFile(targetDirPath, entry.getKey(), entry.getValue());
            } catch (ClientGeneratorException e) {
                err.print(e.getMessage());
            } finally {
                FileDefinitionHolder.getInstance().removeFileDefinition(entry.getKey());
            }
        }
    }

    private boolean isClientGenerationEnabled(AnnotationAttachmentNode ann) {
        boolean isClientRequested;
        if (ann == null) {
            return false;
        }

        BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) ann).getExpression());
        Map<String, String[]> attrs = GeneratorUtils.getKeyValuePairAsMap(bLiteral.getFields());
        String val = attrs.get("generate")[0];
        isClientRequested = Boolean.parseBoolean(val);

        return isClientRequested;
    }

    private boolean isAnClientEnabledEndpoint(BLangSimpleVariable endpoint) {
        boolean isClientEnabledEndpoint = false;
        for (BLangAnnotationAttachment annAttachment : endpoint.annAttachments) {
            if (annAttachment.annotationName.getValue().equals("ClientEndpoint")) {
                isClientEnabledEndpoint = true;
                break;
            }
        }
        return isClientEnabledEndpoint;
    }
}
