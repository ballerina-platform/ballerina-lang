/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.compiler.plugins;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.AnnotationNode;
import org.ballerinalang.model.tree.EndpointNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ResourceNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.TypeDefinition;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.nio.file.Path;
import java.util.List;

/**
 * The Ballerina compiler plugin interface.
 * <p>
 * The Ballerina compiler invokes plugins as soon as it guarantees
 * that AST is free of compilation errors.
 *
 * @since 0.962.0
 */
public interface CompilerPlugin {

    default void setCompilerContext(CompilerContext context) {
    }

    /**
     * Initializes the compiler plugin.
     * <p>
     * Ballerina compiler invokes this method once for a given compilation.
     *
     * @param diagnosticLog the diagnostic logger which gives compiler plugins
     *                      a mechanism to log errors, warnings and messages
     */
    void init(DiagnosticLog diagnosticLog);

    /**
     * Processes a package node.
     * <p>
     * Ballerina compiler invokes this method for each and every package node in the AST.
     *
     * @param packageNode package node
     */
    void process(BLangPackage packageNode);

    /**
     * Processes a testable package node.
     * <p>
     * Ballerina compiler invokes this method for each and every testable package node in the AST.
     *
     * @param testablePackageNode package node
     */
    void process(BLangTestablePackage testablePackageNode);

    /**
     * Processes a list of annotations attached to a service node.
     *
     * @param serviceNode the service node being annotated
     * @param annotations a list of annotations attached to the service node
     */
    void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to a resource node.
     *
     * @param resourceNode the resource node being annotated
     * @param annotations  a list of annotations attached to the resource node
     */
    void process(ResourceNode resourceNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to a action node.
     *
     * @param actionNode  the action node being annotated
     * @param annotations a list of annotations attached to the action node
     */
    void process(ActionNode actionNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to a object node.
     *
     * @param typeDefinition  the object node being annotated
     * @param annotations a list of annotations attached to the object node
     */
    void process(TypeDefinition typeDefinition, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to a function node.
     *
     * @param functionNode the function node being annotated
     * @param annotations  a list of annotations attached to the function node
     */
    void process(FunctionNode functionNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to a variable node.
     *
     * @param variableNode the variable node being annotated
     * @param annotations  a list of annotations attached to the variable node
     */
    void process(SimpleVariableNode variableNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to an annotation node.
     *
     * @param annotationNode the annotation node being annotated
     * @param annotations    a list of annotations attached to the annotation node
     */
    void process(AnnotationNode annotationNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Processes a list of annotations attached to an endpoint node.
     *
     * @param endpointNode the endpoint node being annotated
     * @param annotations  a list of annotations attached to the transformer node
     */
    void process(EndpointNode endpointNode, List<AnnotationAttachmentNode> annotations);

    /**
     * Notifies when the code generated phase is completed.
     *
     * @param packageID packageId of the generated code
     * @param binaryPath path to the generated binary file (balx)
     */
    void codeGenerated(PackageID packageID, Path binaryPath);
}
