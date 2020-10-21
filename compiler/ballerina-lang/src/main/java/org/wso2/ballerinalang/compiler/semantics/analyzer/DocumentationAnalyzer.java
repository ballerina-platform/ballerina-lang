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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.DocReferenceErrorType;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Analyzes markdown documentations.
 *
 * @since 0.981.0
 */
public class DocumentationAnalyzer extends BLangNodeVisitor {

    private static final CompilerContext.Key<DocumentationAnalyzer> DOCUMENTATION_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private BLangDiagnosticLog dlog;
    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private SymbolEnv env;
    private Names names;

    public static DocumentationAnalyzer getInstance(CompilerContext context) {
        DocumentationAnalyzer documentationAnalyzer = context.get(DOCUMENTATION_ANALYZER_KEY);
        if (documentationAnalyzer == null) {
            documentationAnalyzer = new DocumentationAnalyzer(context);
        }
        return documentationAnalyzer;
    }

    private DocumentationAnalyzer(CompilerContext context) {
        context.put(DOCUMENTATION_ANALYZER_KEY, this);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        this.env = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode));
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
        pkgNode.getTestablePkgs().forEach(this::analyze);
        return pkgNode;
    }

    private void analyzeNode(BLangNode node) {
        node.accept(this);
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {

    }

    @Override
    public void visit(BLangAnnotation annotationNode) {

    }

    @Override
    public void visit(BLangLetExpression letExpression) {

    }

    @Override
    public void visit(BLangConstant constant) {
        validateNoParameters(constant);
        validateReturnParameter(constant, null, false);
        validateReferences(constant);
        validateDeprecationDocumentation(constant.markdownDocumentationAttachment,
                Symbols.isFlagOn(constant.symbol.flags, Flags.DEPRECATED), constant.pos);
        validateDeprecatedParametersDocumentation(constant.markdownDocumentationAttachment, constant.pos);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        validateNoParameters(varNode);
        validateReturnParameter(varNode, null, false);
        validateReferences(varNode);
        validateDeprecationDocumentation(varNode.markdownDocumentationAttachment, false, varNode.pos);
        validateDeprecatedParametersDocumentation(varNode.markdownDocumentationAttachment, varNode.pos);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        validateParameters(funcNode, funcNode.getParameters(),
                funcNode.restParam, DiagnosticCode.UNDOCUMENTED_PARAMETER,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER,
                DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED);

        validateDeprecatedParameters(funcNode, funcNode.getParameters(), funcNode.restParam,
                DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED, DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER);
        validateReferences(funcNode);

        boolean hasReturn = true;
        if (funcNode.returnTypeNode.getKind() == NodeKind.VALUE_TYPE) {
            hasReturn = ((BLangValueType) funcNode.returnTypeNode).typeKind != TypeKind.NIL;
        }
        validateReturnParameter(funcNode, funcNode, hasReturn);
        validateDeprecationDocumentation(funcNode.markdownDocumentationAttachment,
                Symbols.isFlagOn(funcNode.symbol.flags, Flags.DEPRECATED), funcNode.pos);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangService serviceNode) {
        validateNoParameters(serviceNode);
        validateReturnParameter(serviceNode, null, false);
        validateReferences(serviceNode);
        validateDeprecationDocumentation(serviceNode.markdownDocumentationAttachment, false, serviceNode.pos);
        validateDeprecatedParametersDocumentation(serviceNode.markdownDocumentationAttachment, serviceNode.pos);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BLangType typeNode = typeDefinition.getTypeNode();
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            List<BLangSimpleVariable> fields = ((BLangObjectTypeNode) typeNode).fields;
            validateParameters(typeDefinition, fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);
            validateReferences(typeDefinition);
            for (SimpleVariableNode field : fields) {
                validateReferences(field);
                validateDeprecationDocumentation(field.getMarkdownDocumentationAttachment(),
                        Symbols.isFlagOn(((BLangSimpleVariable) field).symbol.flags, Flags.DEPRECATED),
                        (DiagnosticPos) field.getPosition());
            }

            ((BLangObjectTypeNode) typeDefinition.getTypeNode()).getFunctions().forEach(this::analyzeNode);
        } else if (typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            List<BLangSimpleVariable> fields = ((BLangRecordTypeNode) typeNode).fields;
            validateParameters(typeDefinition, fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);
            validateReferences(typeDefinition);

            for (SimpleVariableNode field : fields) {
                validateReferences(field);
            }
        }
        if (typeDefinition.symbol != null) {
            validateDeprecationDocumentation(typeDefinition.markdownDocumentationAttachment,
                    Symbols.isFlagOn(typeDefinition.symbol.flags, Flags.DEPRECATED), typeDefinition.pos);
        }
        validateDeprecatedParametersDocumentation(typeDefinition.markdownDocumentationAttachment, typeDefinition.pos);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        validateParameters(classDefinition, classDefinition.fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
        validateReturnParameter(classDefinition, null, false);
        validateReferences(classDefinition);
        for (SimpleVariableNode field : classDefinition.fields) {
            validateReferences(field);
            validateDeprecationDocumentation(field.getMarkdownDocumentationAttachment(),
                    Symbols.isFlagOn(((BLangSimpleVariable) field).symbol.flags, Flags.DEPRECATED),
                    (DiagnosticPos) field.getPosition());
        }

        classDefinition.functions.forEach(this::analyzeNode);

        validateDeprecationDocumentation(classDefinition.markdownDocumentationAttachment,
                Symbols.isFlagOn(classDefinition.symbol.flags, Flags.DEPRECATED), classDefinition.pos);
        validateDeprecatedParametersDocumentation(classDefinition.markdownDocumentationAttachment, classDefinition.pos);

    }

    @Override
    public void visit(BLangResource resourceNode) {
        validateParameters(resourceNode, resourceNode.getParameters(),
                resourceNode.restParam, DiagnosticCode.UNDOCUMENTED_PARAMETER,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER,
                DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED);

        validateReturnParameter(resourceNode, null, false);
        validateReferences(resourceNode);
    }

    private void validateDeprecationDocumentation(BLangMarkdownDocumentation documentation,
                                                  boolean isDeprecationAnnotationAvailable,
                                                  DiagnosticPos pos) {
        if (documentation == null) {
            return;
        }

        BLangMarkDownDeprecationDocumentation deprecationDocumentation =
                documentation.getDeprecationDocumentation();

        boolean isDeprecationDocumentationAvailable = false;
        if (deprecationDocumentation != null && deprecationDocumentation.isCorrectDeprecationLine) {
            isDeprecationDocumentationAvailable = true;
        }

        if (isDeprecationDocumentationAvailable && !isDeprecationAnnotationAvailable) {
            dlog.error(deprecationDocumentation.pos, DiagnosticCode.INVALID_DEPRECATION_DOCUMENTATION);
        } else if (!isDeprecationDocumentationAvailable && isDeprecationAnnotationAvailable) {
            dlog.error(pos, DiagnosticCode.DEPRECATION_DOCUMENTATION_SHOULD_BE_AVAILABLE);
        }
    }

    public void validateDeprecatedParametersDocumentation(BLangMarkdownDocumentation documentation, DiagnosticPos pos) {
        if (documentation == null) {
            return;
        }

        BLangMarkDownDeprecatedParametersDocumentation deprecatedParametersDocumentation =
                documentation.getDeprecatedParametersDocumentation();
        if (deprecatedParametersDocumentation != null) {
            dlog.error(pos, DiagnosticCode.DEPRECATED_PARAMETERS_DOCUMENTATION_NOT_ALLOWED);
        }
    }

    private void validateReferences(DocumentableNode documentableNode) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentation.getReferences();
        for (BLangMarkdownReferenceDocumentation reference : references) {
            if (reference.hasParserWarnings) {
                continue;
            }
            DocReferenceErrorType status = validateIdentifier(reference, documentableNode);
            if (status != DocReferenceErrorType.NO_ERROR) {
                if (status == DocReferenceErrorType.REFERENCE_ERROR) {
                    dlog.warning(reference.pos, DiagnosticCode.INVALID_DOCUMENTATION_REFERENCE,
                            reference.referenceName, reference.getType().getValue());
                } else {
                    dlog.warning(reference.pos, DiagnosticCode.INVALID_USAGE_OF_PARAMETER_REFERENCE,
                            reference.referenceName);
                }
            }
        }
    }

    private DocReferenceErrorType validateIdentifier(BLangMarkdownReferenceDocumentation reference,
                                                     DocumentableNode documentableNode) {
        int tag = -1;
        SymbolEnv env = this.env;
        // Lookup namespace to validate the identifier.
        switch (reference.getType()) {
            case PARAMETER:
                // Parameters are only available for function nodes.
                if (documentableNode.getKind() == NodeKind.FUNCTION) {
                    BLangFunction funcNode = (BLangFunction) documentableNode;
                    env = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.env);
                    tag = SymTag.VARIABLE;
                    break;
                } else {
                    return DocReferenceErrorType.PARAMETER_REFERENCE_ERROR;
                }
            case SERVICE:
                tag = SymTag.SERVICE;
                break;
            case TYPE:
                tag = SymTag.TYPE;
                break;
            case VARIABLE:
            case VAR:
                tag = SymTag.VARIABLE;
                break;
            case ANNOTATION:
                tag = SymTag.ANNOTATION;
                break;
            case MODULE:
                tag = SymTag.IMPORT;
                break;
            case CONST:
                tag = SymTag.CONSTANT;
                break;
            case BACKTICK_CONTENT:
            case FUNCTION:
                tag = SymTag.FUNCTION;
                break;
        }

        BSymbol symbol = resolveFullyQualifiedSymbol(reference.pos, env, reference.qualifier, reference.typeName,
                reference.identifier, tag);
        return (symbol != symTable.notFoundSymbol) ?
                DocReferenceErrorType.NO_ERROR : DocReferenceErrorType.REFERENCE_ERROR;
    }

    private BSymbol resolveFullyQualifiedSymbol(DiagnosticPos pos, SymbolEnv env, String packageId, String type,
                                                String identifier, int tag) {
        Name identifierName = names.fromString(identifier);
        Name pkgName = names.fromString(packageId);
        Name typeName = names.fromString(type);
        SymbolEnv pkgEnv = env;

        if (pkgName != Names.EMPTY) {
            BSymbol pkgSymbol = symResolver.resolvePrefixSymbol(env, pkgName,
                    names.fromString(pos.getSource().getCompilationUnitName()));

            if (pkgSymbol == symTable.notFoundSymbol) {
                return symTable.notFoundSymbol;
            }

            if (pkgSymbol.tag == SymTag.PACKAGE) {
                BPackageSymbol symbol = (BPackageSymbol) pkgSymbol;
                pkgEnv = symTable.pkgEnvMap.get(symbol);
            }
        }

        // If there is no type in the reference we need to search in the package level and the current scope only.
        if (typeName == Names.EMPTY) {
            if ((tag & SymTag.IMPORT) == SymTag.IMPORT) {
                return symResolver.lookupPrefixSpaceSymbolInPackage(pos, env, pkgName, identifierName);
            } else if ((tag & SymTag.ANNOTATION) == SymTag.ANNOTATION) {
                return symResolver.lookupAnnotationSpaceSymbolInPackage(pos, env, pkgName, identifierName);
            } else if ((tag & SymTag.MAIN) == SymTag.MAIN) {
                return symResolver.lookupMainSpaceSymbolInPackage(pos, env, pkgName, identifierName);
            }
        }

        // Check for type in the environment.
        BSymbol typeSymbol = symResolver.lookupMainSpaceSymbolInPackage(pos, env, pkgName, typeName);
        if (typeSymbol == symTable.notFoundSymbol) {
            return symTable.notFoundSymbol;
        }

        if (typeSymbol.tag == SymTag.OBJECT) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
            // If the type is available at the global scope or package level then lets dive in to the scope of the type
            // `pkgEnv` is `env` if no package was identified or else it's the package's environment
            String functionID = typeName + "." + identifierName;
            Name functionName = names.fromString(functionID);
            return symResolver.lookupMemberSymbol(pos, objectTypeSymbol.scope, pkgEnv, functionName, tag);
        }

        return symTable.notFoundSymbol;
    }

    private void validateParameters(DocumentableNode documentableNode,
                                    List<BLangSimpleVariable> actualParameters,
                                    BLangSimpleVariable restParam,
                                    DiagnosticCode undocumentedParameter, DiagnosticCode noSuchParameter,
                                    DiagnosticCode parameterAlreadyDefined) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        List<String> fieldsDocumentedAtFieldLevel = getDocumentedFields(actualParameters);

        // Create a new map to add parameter name and parameter node as key-value pairs.
        Map<String, BLangMarkdownParameterDocumentation> documentedParameterMap =
                getDocumentedParameters(documentation.parameters, fieldsDocumentedAtFieldLevel,
                        parameterAlreadyDefined);

        // Iterate through actual parameters.
        actualParameters.forEach(parameter -> {
            String name = parameter.getName().getValue();
            // Get parameter documentation if available.
            BLangMarkdownParameterDocumentation param = documentedParameterMap.get(name);
            if (param != null) {
                // Set the symbol in the documentation node.
                param.setSymbol(((BLangSimpleVariable) parameter).symbol);
                documentedParameterMap.remove(name);
            } else {
                // Check whether the g is public and whether there is no field documentation.
                // It is mandatory to document only if it is public.
                if (Symbols.isFlagOn(((BLangSimpleVariable) parameter).symbol.flags, Flags.PUBLIC) &&
                        ((BLangSimpleVariable) parameter).markdownDocumentationAttachment == null) {
                    // Add warnings for undocumented parameters.
                    dlog.warning(((BLangNode) parameter).pos, undocumentedParameter, name);
                }

                // If the parameter is a public function parameter, the parameter should be documented.
                if (documentableNode.getKind() == NodeKind.FUNCTION) {
                    BLangFunction function = (BLangFunction) documentableNode;
                    if (function.flagSet.contains(Flag.PUBLIC)) {
                        dlog.warning(((BLangNode) parameter).pos, undocumentedParameter, name);
                    }
                }
            }
        });

        // Check rest parameter.
        if (restParam != null) {
            String name = restParam.getName().value;
            BLangMarkdownParameterDocumentation param = documentedParameterMap.get(name);
            if (param != null) {
                // Set the symbol in the documentation node.
                param.setSymbol(restParam.symbol);
                documentedParameterMap.remove(name);
            } else {
                // Add warnings for undocumented parameters.
                dlog.warning(restParam.pos, undocumentedParameter, name);
            }
        }

        // Add warnings for the entries left in the map.
        documentedParameterMap.forEach((name, node) -> dlog.warning(node.pos, noSuchParameter, name));
    }

    private void validateNoParameters(DocumentableNode documentableNode) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }
        Map<String, BLangMarkdownParameterDocumentation> parameterDocumentations =
                documentation.getParameterDocumentations();
        if (parameterDocumentations.isEmpty()) {
            return;
        }
        parameterDocumentations.forEach((parameter, parameterDocumentation) ->
                dlog.warning(parameterDocumentation.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER, parameter));
    }

    private void validateReturnParameter(DocumentableNode documentableNode, BLangNode node, boolean isExpected) {
        BLangMarkdownDocumentation documentationAttachment = documentableNode.getMarkdownDocumentationAttachment();
        if (documentationAttachment == null) {
            return;
        }

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        if (returnParameter == null && isExpected) {
            dlog.warning(documentationAttachment.pos, DiagnosticCode.UNDOCUMENTED_RETURN_PARAMETER);
        } else if (returnParameter != null && !isExpected) {
            dlog.warning(returnParameter.pos, DiagnosticCode.NO_DOCUMENTABLE_RETURN_PARAMETER);
        } else if (returnParameter != null) {
            returnParameter.setReturnType(((BLangFunction) node).getReturnTypeNode().type);
        }
    }

    // List that holds fields that are documented at field level.
    private List<String> getDocumentedFields(List<? extends SimpleVariableNode> actualParameters) {
        List<String> fieldsDocumentedAtFieldLevel = new ArrayList<>();
        for (SimpleVariableNode field : actualParameters) {
            if (field.getMarkdownDocumentationAttachment() == null) {
                continue;
            }
            fieldsDocumentedAtFieldLevel.add(field.getName().getValue());
        }
        return fieldsDocumentedAtFieldLevel;
    }

    private Map<String, BLangMarkdownParameterDocumentation> getDocumentedParameters(
            LinkedList<BLangMarkdownParameterDocumentation> deprecatedParameters,
            List<String> fieldsDocumentedFields,
            DiagnosticCode parameterAlreadyDefined) {
        Map<String, BLangMarkdownParameterDocumentation> documentedDeprecatedParameterMap = new HashMap<>();

        for (BLangMarkdownParameterDocumentation parameter : deprecatedParameters) {
            String parameterName = parameter.getParameterName().getValue();
            // Check for parameters which are documented multiple times.
            if (documentedDeprecatedParameterMap.containsKey(parameterName) ||
                    fieldsDocumentedFields.contains(parameterName)) {
                dlog.warning(parameter.pos, parameterAlreadyDefined, parameterName);
                continue;
            }
            documentedDeprecatedParameterMap.put(parameterName, parameter);
        }
        return documentedDeprecatedParameterMap;
    }

    private void validateDeprecatedParameters(DocumentableNode documentableNode,
                                             List<BLangSimpleVariable> actualParameters,
                                             BLangSimpleVariable restParam,
                                             DiagnosticCode parameterAlreadyDefined,
                                             DiagnosticCode noSuchParameter) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        Map<String, BLangMarkdownParameterDocumentation> documentedDeprecatedParameterMap = new HashMap<>();
        if (documentation.deprecatedParametersDocumentation != null) {
            documentedDeprecatedParameterMap =
                    getDocumentedParameters(documentation.deprecatedParametersDocumentation.parameters,
                            new ArrayList<>(), parameterAlreadyDefined);
        }

        for (BLangSimpleVariable parameter : actualParameters) {
            validateDeprecatedParameter(documentedDeprecatedParameterMap, parameter);
        }

        if (restParam != null) {
            validateDeprecatedParameter(documentedDeprecatedParameterMap, restParam);
        }

        documentedDeprecatedParameterMap.forEach((name, node) -> dlog.warning(node.pos, noSuchParameter, name));
    }

    private void validateDeprecatedParameter(
            Map<String, BLangMarkdownParameterDocumentation> documentedDeprecatedParameterMap,
            BLangSimpleVariable parameter) {
        String name = parameter.getName().value;
        if (!documentedDeprecatedParameterMap.containsKey(name)) {
            if (Symbols.isFlagOn(parameter.symbol.flags, Flags.DEPRECATED)) {
                dlog.error(parameter.annAttachments.get(0).pos,
                        DiagnosticCode.DEPRECATION_DOCUMENTATION_SHOULD_BE_AVAILABLE);
            }
        } else {
            if (!Symbols.isFlagOn(parameter.symbol.flags, Flags.DEPRECATED)) {
                dlog.error(documentedDeprecatedParameterMap.get(name).pos,
                        DiagnosticCode.INVALID_DEPRECATION_DOCUMENTATION);
            }
            documentedDeprecatedParameterMap.remove(name);
        }
    }
}
