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

package org.ballerinalang.code.generator.util;

import org.ballerinalang.code.generator.CodeGenerator;
import org.ballerinalang.code.generator.GeneratorConstants;
import org.ballerinalang.code.generator.exception.CodeGeneratorException;
import org.ballerinalang.code.generator.model.ClientContextHolder;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Compiler plugin for ballerina client code generation.
 */
@SupportedAnnotationPackages(value = { "ballerina.swagger" })
public class ClientGeneratorPlugin extends AbstractCompilerPlugin {
    List<EndpointNode> endpoints;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.endpoints = new ArrayList<>();
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        CodeGenerator codegen = new CodeGenerator();
        PrintStream err = System.err;
        AnnotationAttachmentNode config = GeneratorUtils
                .getAnnotationFromList("ClientConfig", GeneratorConstants.SWAGGER_PKG_ALIAS,
                        annotations);

        // Generate client only if requested by providing the client config annotation
        if (isClientGenerationEnabled(config)) {
            try {
                ClientContextHolder context = ClientContextHolder.buildContext(serviceNode, endpoints);
                codegen.writeGeneratedSource(GeneratorConstants.GenType.CLIENT, context,
                        getOutputFilePath(serviceNode));
            } catch (CodeGeneratorException e) {
                err.println("Client code was not generated: " + e.getMessage());
            }
        }
    }

    @Override
    public void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode config = GeneratorUtils
                .getAnnotationFromList("ClientEndpoint", GeneratorConstants.SWAGGER_PKG_ALIAS,
                        annotations);
        if (config != null) {
            this.endpoints.add(endpointNode);
        }
    }

    private String getOutputFilePath(ServiceNode serviceNode) {
        String cUnit = serviceNode.getPosition().getSource().getCompilationUnitName();
        String dir = cUnit.substring(0, cUnit.lastIndexOf(File.separator) + 1);
        String file = serviceNode.getName().getValue() + "Client.bal";
        return dir + file;
    }

    private boolean isClientGenerationEnabled(AnnotationAttachmentNode ann) {
        boolean isClientRequested;
        if (ann == null) {
            return false;
        }

        BLangRecordLiteral bLiteral = ((BLangRecordLiteral) ((BLangAnnotationAttachment) ann).getExpression());
        List<BLangRecordLiteral.BLangRecordKeyValue> list = bLiteral.getKeyValuePairs();
        Map<String, String[]> attrs =  GeneratorUtils.getKeyValuePairAsMap(list);
        String val = attrs.get("generate")[0];
        isClientRequested = Boolean.parseBoolean(val);

        return isClientRequested;
    }
}
