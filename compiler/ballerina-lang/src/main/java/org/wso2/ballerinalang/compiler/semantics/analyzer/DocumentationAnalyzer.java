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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.DocReferenceErrorType;
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.DocumentationReferenceType;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.parser.BLangReferenceParserListener;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.ReferenceParserErrorListener;
import org.wso2.ballerinalang.compiler.parser.antlr4.SilentParserErrorStrategy;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

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

    // Used to parse the content inside backticks for Ballerina Flavored Markdown.
    private BLangReferenceParserListener listener;
    private BallerinaParser parser;

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
        setupReferenceParser();
    }

    private void setupReferenceParser() {
        ANTLRInputStream ais = new ANTLRInputStream();
        BallerinaLexer lexer = new BallerinaLexer(ais);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ReferenceParserErrorListener());
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        SilentParserErrorStrategy errorStrategy = new SilentParserErrorStrategy();
        parser = new BallerinaParser(tokenStream);
        this.listener = new BLangReferenceParserListener();
        parser.addParseListener(this.listener);
        parser.setErrorHandler(errorStrategy);
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
    public void visit(BLangEndpoint endpointNode) {

    }

    @Override
    public void visit(BLangConstant constant) {
        validateNoParameters(constant);
        validateReturnParameter(constant, null, false);
        validateReferences(constant);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        validateNoParameters(varNode);
        validateReturnParameter(varNode, null, false);
        validateReferences(varNode);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        validateParameters(funcNode, funcNode.getParameters(),
                funcNode.restParam, DiagnosticCode.UNDOCUMENTED_PARAMETER,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER,
                DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED);

        validateReferences(funcNode);

        boolean hasReturn = true;
        if (funcNode.returnTypeNode.getKind() == NodeKind.VALUE_TYPE) {
            hasReturn = ((BLangValueType) funcNode.returnTypeNode).typeKind != TypeKind.NIL;
        }
        validateReturnParameter(funcNode, funcNode, hasReturn);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangService serviceNode) {
        validateNoParameters(serviceNode);
        validateReturnParameter(serviceNode, null, false);
        validateReferences(serviceNode);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BLangType typeNode = typeDefinition.getTypeNode();
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            List<? extends SimpleVariableNode> fields = ((BLangObjectTypeNode) typeNode).getFields();
            validateParameters(typeDefinition, fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);
            validateReferences(typeDefinition);

            ((BLangObjectTypeNode) typeDefinition.getTypeNode()).getFunctions().forEach(this::analyzeNode);
        } else if (typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            List<? extends SimpleVariableNode> fields = ((BLangRecordTypeNode) typeNode).getFields();
            validateParameters(typeDefinition, fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);
            validateReferences(typeDefinition);
        }
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

    private void validateReferences(DocumentableNode documentableNode) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        LinkedList<BLangMarkdownReferenceDocumentation> references = documentation.getReferences();
        for (BLangMarkdownReferenceDocumentation reference : references) {
            DocReferenceErrorType status = invokeDocumentationReferenceParser(reference);
            if (status != DocReferenceErrorType.NO_ERROR) {
                // Log warning only if not backticked content.
                if (status != DocReferenceErrorType.BACKTICK_IDENTIFIER_ERROR) {
                    dlog.warning(reference.pos, DiagnosticCode.INVALID_DOCUMENTATION_IDENTIFIER,
                            reference.referenceName);
                }
                continue;
            }
            status = validateIdentifier(reference, documentableNode);
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
            return symResolver.lookupSymbolInPackage(pos, env, pkgName, identifierName, tag);
        }

        // Check for type in the environment.
        BSymbol typeSymbol = symResolver.lookupSymbolInPackage(pos, env, pkgName, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            return symTable.notFoundSymbol;
        }

        if (typeSymbol.tag == SymTag.OBJECT) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
            // If the type is available at the global scope or package level then lets dive in to the scope of the type
            // `pkgEnv` is `env` if no package was identified or else it's the package's environment
            String functionID = typeName + "." + identifierName;
            Name functionName = names.fromString(functionID);
            return symResolver.lookupMemberSymbol(pos, objectTypeSymbol.methodScope, pkgEnv, functionName, tag);
        }

        return symTable.notFoundSymbol;
    }

    private DocReferenceErrorType invokeDocumentationReferenceParser(BLangMarkdownReferenceDocumentation reference) {
        ANTLRInputStream ais = new ANTLRInputStream(reference.referenceName);
        BallerinaLexer lexer = new BallerinaLexer(ais);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new ReferenceParserErrorListener());
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        this.listener.reset();
        parser.setInputStream(tokenStream);

        // Invoke function identifier rule for backticked block for cases such as `function()` if is Function is true.
        if (reference.getType() == DocumentationReferenceType.BACKTICK_CONTENT) {
            parser.documentationFullyqualifiedFunctionIdentifier();
            if (this.listener.getState()) {
                return DocReferenceErrorType.BACKTICK_IDENTIFIER_ERROR;
            }
        } else {
            // Else the normal rule to capture type `identifier` type references.
            parser.documentationFullyqualifiedIdentifier();
            if (this.listener.getState()) {
                return DocReferenceErrorType.IDENTIFIER_ERROR;
            }
        }
        // If brackets are used with keywords other than function, its invalid.
        if ((reference.getType() != DocumentationReferenceType.FUNCTION) && this.listener.hasBrackets()) {
            return DocReferenceErrorType.IDENTIFIER_ERROR;
        }
        reference.qualifier = listener.getPkgName();
        reference.typeName = listener.getTypeName();
        reference.identifier = listener.getIdentifier();

        return DocReferenceErrorType.NO_ERROR;
    }


    private void validateParameters(DocumentableNode documentableNode,
                                    List<? extends SimpleVariableNode> actualParameters,
                                    BLangSimpleVariable restParam,
                                    DiagnosticCode undocumentedParameter, DiagnosticCode noSuchParameter,
                                    DiagnosticCode parameterAlreadyDefined) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        // Create a new map to add parameter name and parameter node as key-value pairs.
        Map<String, BLangMarkdownParameterDocumentation> documentedParameterMap = new HashMap<>();
        documentation.parameters.forEach(parameter -> {
            String parameterName = parameter.getParameterName().getValue();
            // Check for parameters which are documented multiple times.
            if (documentedParameterMap.containsKey(parameterName)) {
                dlog.warning(parameter.pos, parameterAlreadyDefined, parameterName);
            } else {
                documentedParameterMap.put(parameterName, parameter);
            }
        });

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
                // Check whether the parameter is public. Otherwise it is not mandatory to document it except if it is a
                // public function parameter.
                if (Symbols.isFlagOn(((BLangSimpleVariable) parameter).symbol.flags, Flags.PUBLIC)) {
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
            dlog.warning(node.pos, DiagnosticCode.UNDOCUMENTED_RETURN_PARAMETER);
        } else if (returnParameter != null && !isExpected) {
            dlog.warning(returnParameter.pos, DiagnosticCode.NO_DOCUMENTABLE_RETURN_PARAMETER);
        } else if (returnParameter != null) {
            returnParameter.setReturnType(((BLangFunction) node).getReturnTypeNode().type);
        }
    }
}
