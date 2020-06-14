/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.transactions.compiler;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.util.List;

/**
 * Compiler plugin for processing transaction participant function annotations.
 *
 * @since 0.990.0
 */
@SupportedAnnotationPackages(value = {"ballerina/transactions"})
@Deprecated
public class ParticipantFunctionCompilerPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dlog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }


    @Override
    public void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations) {
        if (annotations.size() > 1) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, functionNode.getPosition(),
                    "Transact-able function cannot have more than one transaction annotation");
        }
    }
}
