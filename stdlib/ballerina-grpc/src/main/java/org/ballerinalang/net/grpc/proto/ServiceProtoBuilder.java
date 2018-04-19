/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.net.grpc.proto;

import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.grpc.MessageConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.proto.ServiceProtoConstants.ANN_MESSAGE_LISTENER;

/**
 * This class validates annotations attached to Ballerina service and resource nodes.
 *
 * @since 1.0
 */
@SupportedAnnotationPackages(
        value = PROTOCOL_STRUCT_PACKAGE_GRPC
)
public class ServiceProtoBuilder extends AbstractCompilerPlugin {

    private boolean canProcess;
    private DiagnosticLog dlog;
    private Map<String, File> serviceFileMap = new HashMap<>();
    private static final PrintStream error = System.err;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
        this.canProcess = false;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        for (AnnotationAttachmentNode annotationNode : annotations) {
            if (ANN_MESSAGE_LISTENER.equals(annotationNode.getAnnotationName().getValue())) {
                return;
            }
        }

        try {
            File fileDefinition = ServiceProtoUtils.generateProtoDefinition(serviceNode);
            ServiceProtoUtils.writeServiceFiles(Paths.get("."), serviceNode.getName().getValue(), fileDefinition);
            serviceFileMap.put(serviceNode.getName().getValue(), fileDefinition);
        } catch (GrpcServerException e) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
        }
    }

    @Override
    public void codeGenerated(Path binaryPath) {
        if (canProcess) {
            if (binaryPath == null) {
                error.print("Error while generating service proto file. Binary file path is null");
                return;
            }
            Path filePath = binaryPath.toAbsolutePath();
            Path parentDirPath = filePath.getParent();
            if (parentDirPath == null) {
                parentDirPath = filePath;
            }
            Path targetDirPath = Paths.get(parentDirPath.toString(), "grpc");
            for (Map.Entry<String, File> entry : serviceFileMap.entrySet()) {
                try {
                    ServiceProtoUtils.writeServiceFiles(targetDirPath, entry.getKey(), entry.getValue());
                } catch (GrpcServerException e) {
                    error.print("Error while generating service proto file. " + e.getMessage());
                }
            }
        }
    }
}
