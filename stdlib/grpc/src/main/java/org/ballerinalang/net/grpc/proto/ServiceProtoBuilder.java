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
import org.ballerinalang.compiler.plugins.SupportEndpointTypes;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.expressions.LiteralNode;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructureTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
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

import static org.ballerinalang.net.grpc.GrpcConstants.DESCRIPTOR_MAP;
import static org.ballerinalang.net.grpc.GrpcConstants.ORG_NAME;
import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcConstants.SERVICE_ENDPOINT_TYPE;
import static org.ballerinalang.net.grpc.builder.utils.BalGenerationUtils.bytesToHex;

/**
 * This class validates annotations attached to Ballerina service and resource nodes.
 *
 * @since 1.0
 */
@SupportEndpointTypes(
        value = {@SupportEndpointTypes.EndpointType(orgName = ORG_NAME, packageName = PROTOCOL_PACKAGE_GRPC, name =
                SERVICE_ENDPOINT_TYPE)}
)
public class ServiceProtoBuilder extends AbstractCompilerPlugin {

    private DiagnosticLog dlog;
    private static final PrintStream error = System.err;

    private SymbolResolver symResolver = null;
    private SymbolTable symTable = null;
    private Names names = null;

    @Override
    public void setCompilerContext(CompilerContext context) {
        this.symResolver = SymbolResolver.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
    }

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dlog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        try {
            if (ServiceDefinitionValidator.validate(serviceNode, dlog)) {
                Optional<BLangVariable> descriptorMapVar = ((ArrayList) ((BLangPackage) ((BLangService) serviceNode)
                        .parent).globalVars).stream().filter(var -> ((BLangSimpleVariable) var).getName().getValue()
                        .equals(DESCRIPTOR_MAP)).findFirst();
                Optional<BLangVariable> descriptorKey = ((ArrayList) ((BLangPackage) ((BLangService) serviceNode)
                        .parent).globalVars).stream().filter(var -> ((BLangSimpleVariable) var).getName().getValue()
                        .equals("DESCRIPTOR_KEY")).findFirst();

                if (descriptorKey.isPresent() && descriptorMapVar.isPresent()) {
                    String rootDescriptor = ((BLangRecordLiteral) descriptorMapVar.get().getInitialExpression())
                            .getKeyValuePairs().get(0).getValue().toString();
                    addDescriptorAnnotation(serviceNode, rootDescriptor);
                } else {
                    File fileDefinition = ServiceProtoUtils.generateProtoDefinition(serviceNode);
                    addDescriptorAnnotation(serviceNode,
                            bytesToHex(fileDefinition.getFileDescriptorProto().toByteArray()));
                    FileDefinitionHolder.getInstance().addDefinition(serviceNode.getName().getValue(), fileDefinition);
                }
            }
        } catch (GrpcServerException e) {
            dlog.logDiagnostic(Diagnostic.Kind.ERROR, serviceNode.getPosition(), e.getMessage());
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
        Path targetDirPath = Paths.get(parentDirPath.toString(), "grpc");
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
        for (AnnotationAttachmentNode annonNodes : serviceNode.getAnnotationAttachments()) {
            if ("ServiceDescriptor".equals(annonNodes.getAnnotationName().getValue())) {
                //serviceNode.getAnnotationAttachments().remove(annonNodes);
                return;
            }
        }
        BLangService service = (BLangService) serviceNode;
        DiagnosticPos pos = service.pos;
        // Create Annotation Attachment.
        BLangAnnotationAttachment annoAttachment = (BLangAnnotationAttachment) TreeBuilder.createAnnotAttachmentNode();
        serviceNode.addAnnotationAttachment(annoAttachment);
        final SymbolEnv pkgEnv = symTable.pkgEnvMap.get(service.symbol.getEnclosingSymbol());
        BSymbol annSymbol = symResolver.lookupSymbolInPackage(service.pos, pkgEnv,
                names.fromString("grpc"), names.fromString("ServiceDescriptor"), SymTag.ANNOTATION);
        if (annSymbol instanceof BAnnotationSymbol) {
            annoAttachment.annotationSymbol = (BAnnotationSymbol) annSymbol;
        }
        IdentifierNode identifierNode = TreeBuilder.createIdentifierNode();
        if (identifierNode instanceof BLangIdentifier) {
            annoAttachment.annotationName = (BLangIdentifier) identifierNode;
        }
        annoAttachment.annotationName.value = "ServiceDescriptor";
        annoAttachment.pos = pos;
        BLangRecordLiteral literalNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        annoAttachment.expr = literalNode;
        BLangIdentifier pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        pkgAlias.setValue("grpc");
        annoAttachment.pkgAlias = pkgAlias;
        annoAttachment.attachPoint = AttachPoint.SERVICE;
        literalNode.pos = pos;
        BStructureTypeSymbol bStructSymbol = null;
        BSymbol annTypeSymbol = symResolver.lookupSymbolInPackage(service.pos, pkgEnv,
                names.fromString("grpc"), names.fromString("ServiceDescriptorData"), SymTag.STRUCT);
        if (annTypeSymbol instanceof BStructureTypeSymbol) {
            bStructSymbol = (BStructureTypeSymbol) annTypeSymbol;
            literalNode.type = bStructSymbol.type;
        }

        //Add Root Descriptor
        BLangRecordLiteral.BLangRecordKeyValue descriptorKeyValue = (BLangRecordLiteral.BLangRecordKeyValue)
                TreeBuilder.createRecordKeyValue();
        literalNode.keyValuePairs.add(descriptorKeyValue);

        BLangLiteral keyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        keyLiteral.value = "descriptor";
        keyLiteral.typeTag = TypeTags.STRING;
        keyLiteral.type = symTable.stringType;

        BLangLiteral valueLiteral = null;
        LiteralNode literalExpression = TreeBuilder.createLiteralExpression();
        if (literalExpression.getKind() == NodeKind.LITERAL) {
            valueLiteral = (BLangLiteral) literalExpression;
            if (rootDescriptor != null) {
                valueLiteral.value = rootDescriptor;
            }
            valueLiteral.typeTag = TypeTags.STRING;
            valueLiteral.type = symTable.stringType;
        }

        descriptorKeyValue.key = new BLangRecordLiteral.BLangRecordKey(keyLiteral);
        BSymbol fieldSymbol = symResolver.resolveStructField(service.pos, pkgEnv,
                names.fromString("descriptor"), bStructSymbol);
        if (fieldSymbol instanceof BVarSymbol) {
            descriptorKeyValue.key.fieldSymbol = (BVarSymbol) fieldSymbol;
        }
        if (valueLiteral != null) {
            descriptorKeyValue.valueExpr = valueLiteral;
        }

        //Add Descriptor Map
        BSymbol mapVarSymbol = symResolver.lookupSymbol(pkgEnv, names.fromString(DESCRIPTOR_MAP), SymTag.VARIABLE);
        if (mapVarSymbol == null || mapVarSymbol.type.tag == TypeTags.NONE) {
            return;
        }
        BLangSimpleVarRef mapVarRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        //mapVarRef.varSymbol = (BVarSymbol) mapVarSymbol;
        mapVarRef.symbol = mapVarSymbol;
        mapVarRef.type = symTable.mapType;
        BLangIdentifier descriptorMapNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        descriptorMapNode.setValue(DESCRIPTOR_MAP);
        mapVarRef.variableName = descriptorMapNode;
        mapVarRef.pkgSymbol = pkgEnv.scope.owner;

        BLangRecordLiteral.BLangRecordKeyValue mapKeyValue = (BLangRecordLiteral.BLangRecordKeyValue) TreeBuilder
                .createRecordKeyValue();
        literalNode.keyValuePairs.add(mapKeyValue);

        BLangLiteral mapKeyLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();
        mapKeyLiteral.value = "descMap";
        mapKeyLiteral.typeTag = TypeTags.MAP;
        mapKeyLiteral.type = symTable.mapType;
        mapKeyLiteral.pos = pos;

        mapKeyValue.key = new BLangRecordLiteral.BLangRecordKey(mapKeyLiteral);
        mapKeyValue.valueExpr = mapVarRef;
        mapKeyValue.pos = pos;
    }
}
