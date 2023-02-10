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

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.DocReferenceErrorType;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticWarningCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStructureTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
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
public class DocumentationAnalyzer extends SimpleBLangNodeAnalyzer<DocumentationAnalyzer.AnalyzerData> {

    private static final CompilerContext.Key<DocumentationAnalyzer> DOCUMENTATION_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private final BLangDiagnosticLog dlog;
    private final SymbolResolver symResolver;
    private final SymbolTable symTable;
    private final Names names;

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
        final AnalyzerData data = new AnalyzerData();
        visitNode(pkgNode, data);
        return pkgNode;
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        // Ignore
    }

    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        this.dlog.setCurrentPackageId(pkgNode.packageID);
        data.env = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        pkgNode.topLevelNodes.forEach(topLevelNode -> visitNode((BLangNode) topLevelNode, data));
        pkgNode.completedPhases.add(CompilerPhase.CODE_ANALYZE);
        pkgNode.getTestablePkgs().forEach(t -> visitNode(t, data));
    }

    @Override
    public void visit(BLangConstant constant, AnalyzerData data) {
        validateNoParameters(constant);
        validateReturnParameter(constant, null, false);
        validateReferences(constant, data);
        validateDeprecationDocumentation(constant.markdownDocumentationAttachment,
                Symbols.isFlagOn(constant.symbol.flags, Flags.DEPRECATED), constant.pos);
        validateDeprecatedParametersDocumentation(constant.markdownDocumentationAttachment, constant.pos);
    }

    @Override
    public void visit(BLangSimpleVariable varNode, AnalyzerData data) {
        validateNoParameters(varNode);
        validateReturnParameter(varNode, null, false);
        validateReferences(varNode, data);
        validateDeprecationDocumentation(varNode.markdownDocumentationAttachment, false, varNode.pos);
        validateDeprecatedParametersDocumentation(varNode.markdownDocumentationAttachment, varNode.pos);
    }

    @Override
    public void visit(BLangTupleVariable tupleVariableNode, AnalyzerData data) {
        validateNoParameters(tupleVariableNode);
        validateReturnParameter(tupleVariableNode, null, false);
        validateReferences(tupleVariableNode, data);
        validateDeprecationDocumentation(tupleVariableNode.markdownDocumentationAttachment, false,
                tupleVariableNode.pos);
        validateDeprecatedParametersDocumentation(tupleVariableNode.markdownDocumentationAttachment,
                tupleVariableNode.pos);
    }

    @Override
    public void visit(BLangResourceFunction funcNode, AnalyzerData data) {
        visit((BLangFunction) funcNode, data);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable, AnalyzerData data) {
        validateNoParameters(bLangRecordVariable);
        validateReturnParameter(bLangRecordVariable, null, false);
        validateReferences(bLangRecordVariable, data);
        validateDeprecationDocumentation(bLangRecordVariable.markdownDocumentationAttachment, false,
                bLangRecordVariable.pos);
        validateDeprecatedParametersDocumentation(bLangRecordVariable.markdownDocumentationAttachment,
                bLangRecordVariable.pos);
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable, AnalyzerData data) {
        validateNoParameters(bLangErrorVariable);
        validateReturnParameter(bLangErrorVariable, null, false);
        validateReferences(bLangErrorVariable, data);
        validateDeprecationDocumentation(bLangErrorVariable.markdownDocumentationAttachment, false,
                bLangErrorVariable.pos);
        validateDeprecatedParametersDocumentation(bLangErrorVariable.markdownDocumentationAttachment,
                bLangErrorVariable.pos);
    }

    @Override
    public void visit(BLangFunction funcNode, AnalyzerData data) {
        validateParameters(funcNode, funcNode.getParameters(),
                funcNode.restParam, DiagnosticWarningCode.UNDOCUMENTED_PARAMETER,
                DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_PARAMETER,
                DiagnosticWarningCode.PARAMETER_ALREADY_DOCUMENTED);

        validateDeprecatedParameters(funcNode, funcNode.getParameters(), funcNode.restParam
        );
        validateReferences(funcNode, data);

        boolean hasReturn = true;
        if (funcNode.returnTypeNode.getKind() == NodeKind.VALUE_TYPE) {
            hasReturn = ((BLangValueType) funcNode.returnTypeNode).typeKind != TypeKind.NIL;
        }
        validateReturnParameter(funcNode, funcNode, hasReturn);
        validateDeprecationDocumentation(funcNode.markdownDocumentationAttachment,
                Symbols.isFlagOn(funcNode.symbol.flags, Flags.DEPRECATED), funcNode.pos);
    }

    @Override
    public void visit(BLangService serviceNode, AnalyzerData data) {
        validateNoParameters(serviceNode);
        validateReturnParameter(serviceNode, null, false);
        validateReferences(serviceNode, data);
        validateDeprecationDocumentation(serviceNode.markdownDocumentationAttachment, false, serviceNode.pos);
        validateDeprecatedParametersDocumentation(serviceNode.markdownDocumentationAttachment, serviceNode.pos);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition, AnalyzerData data) {
        BLangType typeNode = typeDefinition.getTypeNode();
        NodeKind kind = typeDefinition.typeNode.getKind();
        if (kind == NodeKind.OBJECT_TYPE || kind == NodeKind.RECORD_TYPE) {
            validateDocumentationOfObjectOrRecord(typeDefinition, ((BLangStructureTypeNode) typeNode).fields, data);
        }
        if (kind == NodeKind.OBJECT_TYPE) {
            ((BLangObjectTypeNode) typeDefinition.getTypeNode()).getFunctions().forEach(t -> visitNode(t, data));
        }
        if (typeDefinition.symbol != null) {
            validateDeprecationDocumentation(typeDefinition.markdownDocumentationAttachment,
                    Symbols.isFlagOn(typeDefinition.symbol.flags, Flags.DEPRECATED), typeDefinition.pos);
        }
        validateDeprecatedParametersDocumentation(typeDefinition.markdownDocumentationAttachment, typeDefinition.pos);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition, AnalyzerData data) {
        validateParameters(classDefinition, classDefinition.fields, null, DiagnosticWarningCode.UNDOCUMENTED_FIELD,
                DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticWarningCode.FIELD_ALREADY_DOCUMENTED);
        validateReturnParameter(classDefinition, null, false);
        validateReferences(classDefinition, data);
        for (BLangSimpleVariable field : classDefinition.fields) {
            validateReferences(field, data);
            validateDeprecationDocumentation(field.getMarkdownDocumentationAttachment(),
                    Symbols.isFlagOn(field.symbol.flags, Flags.DEPRECATED),
                    field.getPosition());
        }

        classDefinition.functions.forEach(t -> visitNode(t, data));

        validateDeprecationDocumentation(classDefinition.markdownDocumentationAttachment,
                Symbols.isFlagOn(classDefinition.symbol.flags, Flags.DEPRECATED), classDefinition.pos);
        validateDeprecatedParametersDocumentation(classDefinition.markdownDocumentationAttachment, classDefinition.pos);

    }

    private void validateDeprecationDocumentation(BLangMarkdownDocumentation documentation,
                                                  boolean isDeprecationAnnotationAvailable,
                                                  Location pos) {
        if (documentation == null) {
            return;
        }

        BLangMarkDownDeprecationDocumentation deprecationDocumentation =
                documentation.getDeprecationDocumentation();

        boolean isDeprecationDocumentationAvailable = deprecationDocumentation != null
                && deprecationDocumentation.isCorrectDeprecationLine;

        if (isDeprecationDocumentationAvailable && !isDeprecationAnnotationAvailable) {
            dlog.warning(deprecationDocumentation.pos, DiagnosticWarningCode.INVALID_DEPRECATION_DOCUMENTATION);
        } else if (!isDeprecationDocumentationAvailable && isDeprecationAnnotationAvailable) {
            dlog.warning(pos, DiagnosticWarningCode.DEPRECATION_DOCUMENTATION_SHOULD_BE_AVAILABLE);
        }
    }

    public void validateDeprecatedParametersDocumentation(BLangMarkdownDocumentation documentation,
                                                          Location location) {
        if (documentation == null) {
            return;
        }

        BLangMarkDownDeprecatedParametersDocumentation deprecatedParametersDocumentation =
                documentation.getDeprecatedParametersDocumentation();
        if (deprecatedParametersDocumentation != null) {
            dlog.warning(location, DiagnosticWarningCode.DEPRECATED_PARAMETERS_DOCUMENTATION_NOT_ALLOWED);
        }
    }

    private void validateReferences(DocumentableNode documentableNode, AnalyzerData data) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentation.getReferences();
        for (BLangMarkdownReferenceDocumentation reference : references) {
            if (reference.hasParserWarnings) {
                continue;
            }
            DocReferenceErrorType status = validateIdentifier(reference, documentableNode, data);
            if (status != DocReferenceErrorType.NO_ERROR) {
                if (status == DocReferenceErrorType.REFERENCE_ERROR) {
                    dlog.warning(reference.pos, DiagnosticWarningCode.INVALID_DOCUMENTATION_REFERENCE,
                            reference.referenceName, reference.getType().getValue());
                } else {
                    dlog.warning(reference.pos, DiagnosticWarningCode.INVALID_USAGE_OF_PARAMETER_REFERENCE,
                            reference.referenceName);
                }
            }
        }
    }

    private void validateDocumentationOfObjectOrRecord(BLangTypeDefinition typeDefinition,
                                                       List<BLangSimpleVariable> fields, AnalyzerData data) {
        validateParameters(typeDefinition, fields, null, DiagnosticWarningCode.UNDOCUMENTED_FIELD,
                DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticWarningCode.FIELD_ALREADY_DOCUMENTED);
        validateReturnParameter(typeDefinition, null, false);
        validateReferences(typeDefinition, data);
        for (BLangSimpleVariable field : fields) {
            validateReferences(field, data);
            validateDeprecationDocumentation(field.getMarkdownDocumentationAttachment(),
                    Symbols.isFlagOn(field.symbol.flags, Flags.DEPRECATED),
                    field.getPosition());
        }
    }

    private DocReferenceErrorType validateIdentifier(BLangMarkdownReferenceDocumentation reference,
                                                     DocumentableNode documentableNode, AnalyzerData data) {
        long tag = -1;
        SymbolEnv env = data.env;
        // Lookup namespace to validate the identifier.
        switch (reference.getType()) {
            case PARAMETER:
                // Parameters are only available for function nodes.
                if (documentableNode.getKind() == NodeKind.FUNCTION) {
                    BLangFunction funcNode = (BLangFunction) documentableNode;
                    env = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, data.env);
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

    private BSymbol resolveFullyQualifiedSymbol(Location location, SymbolEnv env, String packageId,
                                                String type, String identifier, long tag) {
        Name identifierName = names.fromString(identifier);
        Name pkgName = names.fromString(packageId);
        Name typeName = names.fromString(type);
        SymbolEnv pkgEnv = env;

        if (pkgName != Names.EMPTY) {
            BSymbol pkgSymbol = symResolver.resolvePrefixSymbol(env, pkgName,
                    names.fromString(location.lineRange().filePath()));

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
                return symResolver.lookupPrefixSpaceSymbolInPackage(location, env, pkgName, identifierName);
            } else if ((tag & SymTag.ANNOTATION) == SymTag.ANNOTATION) {
                return symResolver.lookupAnnotationSpaceSymbolInPackage(location, env, pkgName, identifierName);
            } else if ((tag & SymTag.MAIN) == SymTag.MAIN) {
                return symResolver.lookupMainSpaceSymbolInPackage(location, env, pkgName, identifierName);
            }
        }

        // Check for type in the environment.
        BSymbol typeSymbol = symResolver.lookupMainSpaceSymbolInPackage(location, env, pkgName, typeName);
        if (typeSymbol == symTable.notFoundSymbol) {
            return symTable.notFoundSymbol;
        }

        if (typeSymbol.tag == SymTag.OBJECT) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
            // If the type is available at the global scope or package level then lets dive in to the scope of the type
            // `pkgEnv` is `env` if no package was identified or else it's the package's environment
            String functionID = typeName + "." + identifierName;
            Name functionName = names.fromString(functionID);
            return symResolver.lookupMemberSymbol(location, objectTypeSymbol.scope, pkgEnv, functionName, tag);
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
                param.setSymbol(parameter.symbol);
                documentedParameterMap.remove(name);
            } else {
                // Check whether the g is public and whether there is no field documentation.
                // It is mandatory to document only if it is public.
                if (Symbols.isFlagOn(parameter.symbol.flags, Flags.PUBLIC) &&
                        parameter.markdownDocumentationAttachment == null) {
                    // Add warnings for undocumented parameters.
                    dlog.warning(parameter.pos, undocumentedParameter, name);
                }

                if (documentableNode.getKind() == NodeKind.FUNCTION) {
                    dlog.warning(parameter.pos, undocumentedParameter, name);
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
                dlog.warning(parameterDocumentation.pos,
                        DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_PARAMETER, parameter));
    }

    private void validateReturnParameter(DocumentableNode documentableNode, BLangNode node, boolean isExpected) {
        BLangMarkdownDocumentation documentationAttachment = documentableNode.getMarkdownDocumentationAttachment();
        if (documentationAttachment == null) {
            return;
        }

        BLangMarkdownReturnParameterDocumentation returnParameter = documentationAttachment.getReturnParameter();
        if (returnParameter == null && isExpected) {
            dlog.warning(documentationAttachment.pos, DiagnosticWarningCode.UNDOCUMENTED_RETURN_PARAMETER);
        } else if (returnParameter != null && !isExpected) {
            dlog.warning(returnParameter.pos, DiagnosticWarningCode.NO_DOCUMENTABLE_RETURN_PARAMETER);
        } else if (returnParameter != null) {
            returnParameter.setReturnType(((BLangFunction) node).getReturnTypeNode().getBType());
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
                                              BLangSimpleVariable restParam) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        Map<String, BLangMarkdownParameterDocumentation> documentedDeprecatedParameterMap = new HashMap<>();
        if (documentation.deprecatedParametersDocumentation != null) {
            documentedDeprecatedParameterMap =
                    getDocumentedParameters(documentation.deprecatedParametersDocumentation.parameters,
                            new ArrayList<>(), DiagnosticWarningCode.PARAMETER_ALREADY_DOCUMENTED);
        }

        for (BLangSimpleVariable parameter : actualParameters) {
            validateDeprecatedParameter(documentedDeprecatedParameterMap, parameter);
        }

        if (restParam != null) {
            validateDeprecatedParameter(documentedDeprecatedParameterMap, restParam);
        }

        documentedDeprecatedParameterMap.forEach((name, node) -> dlog.warning(node.pos,
                DiagnosticWarningCode.NO_SUCH_DOCUMENTABLE_PARAMETER, name));
    }

    private void validateDeprecatedParameter(
            Map<String, BLangMarkdownParameterDocumentation> documentedDeprecatedParameterMap,
            BLangSimpleVariable parameter) {
        String name = parameter.getName().value;
        if (!documentedDeprecatedParameterMap.containsKey(name)) {
            if (Symbols.isFlagOn(parameter.symbol.flags, Flags.DEPRECATED)) {
                dlog.warning(parameter.annAttachments.get(0).pos,
                        DiagnosticWarningCode.DEPRECATION_DOCUMENTATION_SHOULD_BE_AVAILABLE);
            }
        } else {
            if (!Symbols.isFlagOn(parameter.symbol.flags, Flags.DEPRECATED)) {
                dlog.warning(documentedDeprecatedParameterMap.get(name).pos,
                        DiagnosticWarningCode.INVALID_DEPRECATION_DOCUMENTATION);
            }
            documentedDeprecatedParameterMap.remove(name);
        }
    }

    /**
     * @since 2.0.0
     */
    public static class AnalyzerData {
        SymbolEnv env;
    }
}
