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
import org.ballerinalang.model.tree.DocumentableNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.model.tree.types.DocumentationReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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


    //Building regex to match Lexer's Unquoted identifier
    private static String identifierString;

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
        instantiateDocumentationReferenceGrammer();
    }

    private static void instantiateDocumentationReferenceGrammer() {
        String initialChar = "a-zA-Z_";
        String unicodeNonidentifierChar = "\u0000-\u007F\uE000-\uF8FF\u200E\u200F\u2028\u2029\u00A1-\u00A7" +
            "\u00A9\u00AB-\u00AC\u00AE\u00B0-\u00B1\u00B6-\u00B7\u00BB\u00BF\u00D7\u00F7\u2010-" +
            "\u2027\u2030-\u205E\u2190-\u2BFF\u3001-\u3003\u3008-\u3020\u3030\uFD3E-\uFD3F\uFE45-" +
            "\uFE46\uDB80-\uDBBF\uDBC0-\uDBFF\uDC00-\uDFFF";
        String digit = "0-9";
        String identifierInitialChar = "[" + initialChar + "]|[^" + unicodeNonidentifierChar + "]";
        String identifierFollowingChar = "[" + initialChar + digit + "]|[^" + unicodeNonidentifierChar + "]";
        identifierString = "((?:" + identifierInitialChar + ")(?:" + identifierFollowingChar + ")*)";
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
        references.forEach(reference -> {
            StringBuilder pckgName = new StringBuilder();
            StringBuilder typeName = new StringBuilder();
            StringBuilder identifier = new StringBuilder();
            boolean validateFunctionInsideBackticks = (reference.type == DocumentationReferenceType.BACKTICK_CONTENT);
            boolean isValidIdentifierString = validateStringForQualifiedIdentifier(validateFunctionInsideBackticks,
                                                                                    reference.getReferenceName(),
                                                                                    pckgName,
                                                                                    typeName,
                                                                                    identifier);

            //Do not warn if backticked content is not matched.
            if (!isValidIdentifierString) {
                if (reference.type != DocumentationReferenceType.BACKTICK_CONTENT) {
                    dlog.warning(reference.pos, DiagnosticCode.INVALID_DOCUMENTATION_IDENTIFIER,
                            reference.getReferenceName());
                }
                return;
            }
            boolean isValidIdentifier = validateIdentifier(reference.getPosition(), documentableNode,
                                                           reference.getType(),
                                                           pckgName.toString(),
                                                           typeName.toString(),
                                                           identifier.toString());
            if (!isValidIdentifier) {
                dlog.warning(reference.pos, DiagnosticCode.INVALID_DOCUMENTATION_REFERENCE,
                        reference.getReferenceName(), reference.getType().getValue());
            }
        });
    }

    private boolean validateIdentifier(DiagnosticPos pos,
                                       DocumentableNode documentableNode,
                                       DocumentationReferenceType type,
                                       String packageId, String typeID,
                                       String identifier) {

        BSymbol symbol = null;
        //Lookup namespace to validate the identifier
        switch (type) {
            case PARAMETER:
                //Parameters are only available for function nodes.
                if (documentableNode.getKind() == NodeKind.FUNCTION) {
                    BLangFunction funcNode = (BLangFunction) documentableNode;
                    SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.env);
                    symbol = resolveFullyQualifiedSymbol(pos, funcEnv, packageId, typeID, identifier, SymTag.VARIABLE);
                } else {
                    symbol = symTable.notFoundSymbol;
                }
                break;
            case SERVICE:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.SERVICE);
                break;
            case TYPE:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.TYPE);
                break;
            case VARIABLE:
            case VAR:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.VARIABLE);
                break;
            case ANNOTATION:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.ANNOTATION);
                break;
            case MODULE:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.IMPORT);
                break;
            case CONST:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.CONSTANT);
                break;
            case BACKTICK_CONTENT:
            case FUNCTION:
                symbol = resolveFullyQualifiedSymbol(pos, this.env, packageId, typeID, identifier, SymTag.FUNCTION);
                break;
        }

        return symbol != symTable.notFoundSymbol;
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

            if (pkgSymbol instanceof BPackageSymbol) {
                BPackageSymbol symbol = (BPackageSymbol) pkgSymbol;
                pkgEnv = symTable.pkgEnvMap.get(symbol);
            }
        }

        //If there is no type in the reference we need to search in the package level and the current scope only.
        if (typeName == Names.EMPTY) {
            return symResolver.lookupSymbolInPackage(pos, env, pkgName, identifierName, tag);
        }

        //Check for type in the environment
        BSymbol typeSymbol = symResolver.lookupSymbolInPackage(pos, env, pkgName, typeName, SymTag.TYPE);
        if (typeSymbol == symTable.notFoundSymbol) {
            return symTable.notFoundSymbol;
        }

        if (typeSymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) typeSymbol;
            //If the type is available at the global scope or package level then lets dive in to the scope of the type
            //`pkgEnv` is `env` if no package was identified or else it's the package's environment
            String functionID = typeName + "." + identifierName;
            Name functionName = names.fromString(functionID);
            return symResolver.lookupMemberSymbol(pos, objectTypeSymbol.methodScope, pkgEnv, functionName, tag);
        }

        return symTable.notFoundSymbol;
    }

    private boolean validateStringForQualifiedIdentifier(boolean validateFunctioninsideQuotes,
                                                         String identifierContent,
                                                         StringBuilder pckgName,
                                                         StringBuilder typeName,
                                                         StringBuilder identifier) {

        //Check if a qualified identifier
        String qualifierStage = identifierString + ":(.*)";
        //Check for type identifier
        String typeStage = identifierString + "\\." + identifierString +
                (validateFunctioninsideQuotes ? "(?:\\(\\))" :  "");
        //Check for `function()` or other identifier reference stage
        String identifierStage = identifierString + (validateFunctioninsideQuotes ? "(?:\\(\\))" :  "");


        //Pattern set
        Pattern qualifierPattern = Pattern.compile(qualifierStage);
        Pattern typePattern = Pattern.compile(typeStage);
        Pattern unqualifiedIdentifierPattern = Pattern.compile(identifierStage);

        //Matchers set

        if (qualifierPatternMatcher(identifierContent, qualifierPattern, typePattern, unqualifiedIdentifierPattern,
                pckgName, typeName, identifier)) {
            return true;
        //If no package name is there, validate for only typename stage
        } else if (typePatternMatcher(identifierContent, typePattern, unqualifiedIdentifierPattern, typeName,
                identifier)) {
                return true;
        //Directly validate for an identifier
        } else {
            return identifierOnlyPatternMatcher(identifierContent, unqualifiedIdentifierPattern, identifier);
        }

    }

    private boolean qualifierPatternMatcher(String identifierString,
                                            Pattern qualifierPattern,
                                            Pattern typePattern,
                                            Pattern unqualifiedIdentifierPattern,
                                            StringBuilder packageName,
                                            StringBuilder typeName,
                                            StringBuilder identifier) {

        StringBuilder typeOrIdentifierStage = new StringBuilder();
        if (patternMatcher(identifierString, qualifierPattern, packageName, typeOrIdentifierStage)) {
            if (typePatternMatcher(typeOrIdentifierStage.toString(), typePattern, unqualifiedIdentifierPattern,
                    typeName, identifier)) {
                return true;
                //If type name is not there, directly validate for an unqualified Identifier
            } else {
                return identifierOnlyPatternMatcher(typeOrIdentifierStage.toString(), unqualifiedIdentifierPattern,
                        identifier);
            }
        }
        return  false;
    }

    private boolean typePatternMatcher(String identifierString, Pattern typePattern,
                                       Pattern unqualifiedIdentifierPattern,
                                       StringBuilder typeName, StringBuilder identifier) {
        StringBuilder identifierOnlyStage = new StringBuilder();
        if (patternMatcher(identifierString, typePattern, typeName, identifierOnlyStage)) {
            //If Type is there, try to match the function name part. Group 2 contains string after the '.'
            return identifierOnlyPatternMatcher(identifierOnlyStage.toString(), unqualifiedIdentifierPattern,
                    identifier);
        }
        return false;
    }

    private boolean identifierOnlyPatternMatcher(String identifierString, Pattern unqualifiedIdentifierPattern,
                                                 StringBuilder identifier) {
        return patternMatcher(identifierString, unqualifiedIdentifierPattern, identifier, null);
    }

    private boolean patternMatcher(String identifierString, Pattern pattern, StringBuilder token,
                                   StringBuilder contentForNextStage) {
        Matcher matcher = pattern.matcher(identifierString);
        if (matcher.matches()) {
            token.append(matcher.group(1));
            if (matcher.groupCount() > 1) {
                contentForNextStage.append(matcher.group(2));
            }
            return true;
        }
        return false;
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
