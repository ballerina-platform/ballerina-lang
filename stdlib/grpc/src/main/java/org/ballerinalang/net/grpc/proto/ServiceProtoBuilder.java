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
import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.net.grpc.GrpcConstants.ANN_FIELD_DESCRIPTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.ANN_FIELD_DESC_MAP;
import static org.ballerinalang.net.grpc.GrpcConstants.ANN_RECORD_DESCRIPTOR_DATA;
import static org.ballerinalang.net.grpc.GrpcConstants.ANN_SERVICE_DESCRIPTOR;
import static org.ballerinalang.net.grpc.GrpcConstants.CALLER;
import static org.ballerinalang.net.grpc.GrpcConstants.DESCRIPTOR_MAP;
import static org.ballerinalang.net.grpc.GrpcConstants.LISTENER;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.ROOT_DESCRIPTOR;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.bytesToHex;

/**
 * This class validates annotations attached to Ballerina service and resource nodes.
 *
 * @since 1.0
 */
@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(packageName = PROTOCOL_PACKAGE_GRPC, name = LISTENER),
        paramTypes = {@SupportedResourceParamTypes.Type(packageName = PROTOCOL_PACKAGE_GRPC, name = CALLER)})
public class ServiceProtoBuilder extends AbstractCompilerPlugin {

    private DiagnosticLog dlog;
    private static final PrintStream error = System.err;

    private SymbolResolver symResolver = null;
    private SymbolTable symTable = null;
    private Names names = null;

    @Override
    public void setCompilerContext(CompilerContext context) {
        super.setCompilerContext(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode service, List<AnnotationAttachmentNode> annotations) {
        try {
            final BLangService serviceNode = (BLangService) service;

            if (ServiceDefinitionValidator.validate(serviceNode, dlog)) {
                Optional<BLangConstant> rootDescriptor = Optional.empty();
                Optional<BLangFunction> descriptorMapFunc = Optional.empty();
                BLangNode serviceParentNode = serviceNode.parent;
                if (serviceParentNode instanceof BLangPackage) {
                    BLangPackage packageNode = (BLangPackage) serviceParentNode;
                    rootDescriptor = ((ArrayList) packageNode.constants).stream().filter(
                            var -> ROOT_DESCRIPTOR.equals(((BLangConstant) var).getName().getValue())).findFirst();
                    descriptorMapFunc = ((ArrayList) packageNode.functions).stream().filter(
                            var -> DESCRIPTOR_MAP.equals(((BLangFunction) var).getName().getValue())).findFirst();
                }

                for (AnnotationAttachmentNode annonNodes : serviceNode.getAnnotationAttachments()) {
                    if (ANN_SERVICE_DESCRIPTOR.equals(annonNodes.getAnnotationName().getValue())) {
                        return;
                    }
                }
                if (rootDescriptor.isPresent() && descriptorMapFunc.isPresent()) {
                    addDescriptorAnnotation(serviceNode, (String) ((BLangLiteral) rootDescriptor.get().expr)
                            .getValue());
                } else {
                    File fileDefinition = ServiceProtoUtils.generateProtoDefinition(serviceNode);
                    addDescriptorAnnotation(serviceNode,
                            bytesToHex(fileDefinition.getFileDescriptorProto().toByteArray()));
                    FileDefinitionHolder.getInstance().addDefinition(serviceNode.getName().getValue(), fileDefinition);
                }
            }
        } catch (GrpcServerException e) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, service.getPosition(), e.getMessage());
        }
    }

    @Override
    public void codeGenerated(PackageID packageID, Path binaryPath) {
        Map<String, File> definitionMap = FileDefinitionHolder.getInstance().getDefinitionMap();
        if (definitionMap.size() == 0) {
            return;
        }
        if (binaryPath == null) {
            error.print("Error while generating service proto file. Binary file path is null");
            return;
        }
        Path filePath = binaryPath.toAbsolutePath();
        Path parentDirPath = filePath.getParent();
        if (parentDirPath == null) {
            parentDirPath = filePath;
        }
        Path targetDirPath = Paths.get(parentDirPath.toString(), PROTOCOL_PACKAGE_GRPC);
        for (Map.Entry<String, File> entry : definitionMap.entrySet()) {
            try {
                ServiceProtoUtils.writeServiceFiles(targetDirPath, entry.getKey(), entry.getValue());
            } catch (GrpcServerException e) {
                error.print(e.getMessage());
            } finally {
                FileDefinitionHolder.getInstance().removeDefinition(entry.getKey());
            }
        }
    }

    private void addDescriptorAnnotation(ServiceNode serviceNode, String rootDescriptor) {
        BLangService service = (BLangService) serviceNode;
        DiagnosticPos pos = service.pos;
        // Create Annotation Attachment.
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        serviceNode.addAnnotationAttachment(annoAttachment);
        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get(service.symbol.getEnclosingSymbol());
        BSymbol annSymbol = symResolver.lookupSymbolInPackage(service.pos, pkgEnv, names.fromString
                (PROTOCOL_PACKAGE_GRPC), names.fromString(ANN_SERVICE_DESCRIPTOR), SymTag.ANNOTATION);
        if (annSymbol instanceof BAnnotationSymbol) {
            annoAttachment.annotationSymbol = (BAnnotationSymbol) annSymbol;
        }
        annoAttachment.annotationName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        annoAttachment.annotationName.value = ANN_SERVICE_DESCRIPTOR;
        annoAttachment.annotationName.pos = pos;
        annoAttachment.pos = pos;
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        annoAttachment.expr = literalNode;
        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue(PROTOCOL_PACKAGE_GRPC);
        pkgAlias.pos = pos;
        annoAttachment.pkgAlias = pkgAlias;
        annoAttachment.attachPoints.add(AttachPoint.Point.SERVICE);
        literalNode.pos = pos;
        BStructureTypeSymbol bStructSymbol = null;
        BSymbol annTypeSymbol = symResolver.lookupSymbolInPackage(service.pos, pkgEnv, names.fromString
                (PROTOCOL_PACKAGE_GRPC), names.fromString(ANN_RECORD_DESCRIPTOR_DATA), SymTag.STRUCT);
        if (annTypeSymbol instanceof BStructureTypeSymbol) {
            bStructSymbol = (BStructureTypeSymbol) annTypeSymbol;
            literalNode.type = bStructSymbol.type;
        }

        //Add Root Descriptor
        BLangRecordLiteral.BLangRecordKeyValue descriptorKeyValue = (BLangRecordLiteral.BLangRecordKeyValue)
                TreeBuilder.createRecordKeyValue();
        literalNode.keyValuePairs.add(descriptorKeyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = ANN_FIELD_DESCRIPTOR;
        keyLiteral.type = symTable.stringType;

        BLangLiteral valueLiteral = null;
        LiteralNode literalExpression = TreeBuilder.createLiteralExpression();
        NodeKind nodeKind = literalExpression.getKind();
        if (nodeKind == NodeKind.LITERAL || nodeKind == NodeKind.NUMERIC_LITERAL) {
            valueLiteral = (BLangLiteral) literalExpression;
            if (rootDescriptor != null) {
                valueLiteral.value = rootDescriptor;
            }
            valueLiteral.type = symTable.stringType;
        }

        descriptorKeyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        BSymbol fieldSymbol = symResolver.resolveStructField(service.pos, pkgEnv,
                names.fromString(ANN_FIELD_DESCRIPTOR), bStructSymbol);
        if (fieldSymbol instanceof BVarSymbol) {
            descriptorKeyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        }
        if (valueLiteral != null) {
            descriptorKeyValue.valueExpr = valueLiteral;
        }

        //Add Descriptor Map
        BSymbol mapVarSymbol = symResolver.lookupSymbol(pkgEnv, names.fromString(DESCRIPTOR_MAP), SymTag.FUNCTION);
        if (mapVarSymbol == null || mapVarSymbol.type.tag == TypeTags.NONE) {
            return;
        }

        BLangInvocation functionRef = null;
        if (mapVarSymbol instanceof BInvokableSymbol) {
            functionRef = ASTBuilderUtil.createInvocationExpr(pos, (BInvokableSymbol) mapVarSymbol, new
                    ArrayList<>(), symResolver);
            functionRef.symbol = mapVarSymbol;
            functionRef.type = symTable.mapType;
            BLangIdentifier funcName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            funcName.setValue(DESCRIPTOR_MAP);
            funcName.pos = pos;
            functionRef.name = funcName;
            BLangIdentifier funcPkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
            funcPkgAlias.setValue("");
            funcPkgAlias.pos = pos;
            functionRef.pkgAlias = funcPkgAlias;
        }

        BLangRecordLiteral.BLangRecordKeyValue mapKeyValue = (BLangRecordLiteral.BLangRecordKeyValue) TreeBuilder
                .createRecordKeyValue();
        literalNode.keyValuePairs.add(mapKeyValue);

        BLangSimpleVarRef mapKeyLiteral = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        BLangIdentifier keyName = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        keyName.setValue(ANN_FIELD_DESC_MAP);
        keyName.pos = pos;
        mapKeyLiteral.variableName = keyName;
        mapKeyLiteral.type = symTable.mapType;
        mapKeyLiteral.pos = pos;

        mapKeyValue.key = new BLangRecordLiteral.BLangRecordKey(mapKeyLiteral);
        mapKeyValue.valueExpr = functionRef;
        mapKeyValue.pos = pos;
    }
}
