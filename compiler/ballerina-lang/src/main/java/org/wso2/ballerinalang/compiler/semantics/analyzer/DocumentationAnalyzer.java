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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownBReferenceDocumentation;
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

import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.HashMap;
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
    public void visit(BLangEndpoint endpointNode) {

    }

    @Override
    public void visit(BLangConstant constant) {
        validateNoParameters(constant);
        validateReturnParameter(constant, null, false);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        validateNoParameters(varNode);
        validateReturnParameter(varNode, null, false);
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
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        BLangType typeNode = typeDefinition.getTypeNode();
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE) {
            List<? extends SimpleVariableNode> fields = ((BLangObjectTypeNode) typeNode).getFields();
            validateParameters(typeDefinition, fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);

            ((BLangObjectTypeNode) typeDefinition.getTypeNode()).getFunctions().forEach(this::analyzeNode);
        } else if (typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            List<? extends SimpleVariableNode> fields = ((BLangRecordTypeNode) typeNode).getFields();
            validateParameters(typeDefinition, fields, null, DiagnosticCode.UNDOCUMENTED_FIELD,
                    DiagnosticCode.NO_SUCH_DOCUMENTABLE_FIELD, DiagnosticCode.FIELD_ALREADY_DOCUMENTED);
            validateReturnParameter(typeDefinition, null, false);
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
        validateParameters(resourceNode, resourceNode.getParameters(),
                resourceNode.restParam, DiagnosticCode.UNDOCUMENTED_PARAMETER,
                DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER,
                DiagnosticCode.PARAMETER_ALREADY_DOCUMENTED);

        validateReturnParameter(resourceNode, null, false);
    }

    private void validateReferences(DocumentableNode documentableNode) {
        BLangMarkdownDocumentation documentation = documentableNode.getMarkdownDocumentationAttachment();
        if (documentation == null) {
            return;
        }

        LinkedList<BLangMarkdownBReferenceDocumentation> references = documentation.getReferences();
        references.forEach(reference -> {
            StringBuilder pckgName = new StringBuilder();
            StringBuilder typeName = new StringBuilder();
            StringBuilder identifier = new StringBuilder();
            boolean isValidIdentifierString = validateStringForQualifiedIdentifier(reference.getReferenceName(),
		                                                                            pckgName,
                                                                                    typeName,
                                                                                    identifier);
            if (!isValidIdentifierString) {
                //dlog.warning(reference.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER,
                        //reference.getReferenceName());
                int i = 0;
            }
            boolean isValidIdentifier = validateIdentifier(reference.getPosition(), documentableNode, reference.getType(),
                                                           pckgName.toString(),
                                                           typeName.toString(),
                                                           identifier.toString());
            if (!isValidIdentifier) {
                //dlog.warning(reference.pos, DiagnosticCode.NO_SUCH_DOCUMENTABLE_PARAMETER, reference.getReferenceName());
                int i = 0;
            }
        });
    }

    private boolean validateIdentifier(DiagnosticPos pos,
								       DocumentableNode documentableNode,
								       DocumentationReferenceType type,
								       String id, String packageId,
								       String identifier) {

        BSymbol symbol = null;
        Name identifierName = names.fromString(identifier);
        Name pckgName = names.fromString(packageId);

        //Lookup namespace to validate the identifier
        switch (type) {
            case PARAMETER:
                //Parameters are only available for function nodes.
                if (documentableNode.getKind() == NodeKind.FUNCTION) {
                    BLangFunction funcNode = (BLangFunction) documentableNode;
                    SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.env);
                    symbol = symResolver.lookupSymbolInPackage(pos, funcEnv, pckgName, identifierName, SymTag.VARIABLE);
                }
                break;
            case SERVICE:
                symbol = symResolver.lookupSymbolInPackage(pos, this.env, pckgName, identifierName, SymTag.SERVICE);
                break;
	        case FUNCTION:
	        	symbol = symResolver.lookupSymbolInPackage(pos, this.env, pckgName, identifierName, SymTag.FUNCTION);
	        	break;
        }

        return symbol != symTable.notFoundSymbol;
    }

    private boolean validateStringForQualifiedIdentifier(String identifierContent,
                                                         StringBuilder pckgName,
                                                         StringBuilder typeName,
                                                         StringBuilder identifier) {
        //Building regex to match Lexer's Unquoted identifier
        String initialChar = "a-zA-Z_";
        String unicodeNonidentifierChar = "\u0000-\u007F\uE000-\uF8FF\u200E\u200F\u2028\u2029\u00A1-\u00A7" +
                "\u00A9\u00AB-\u00AC\u00AE\u00B0-\u00B1\u00B6-\u00B7\u00BB\u00BF\u00D7\u00F7\u2010-" +
                "\u2027\u2030-\u205E\u2190-\u2BFF\u3001-\u3003\u3008-\u3020\u3030\uFD3E-\uFD3F\uFE45-" +
                "\uFE46\uDB80-\uDBBF\uDBC0-\uDBFF\uDC00-\uDFFF";
        String digit = "0-9";
        String identifierInitialChar = "[" + initialChar + "]|[^" + unicodeNonidentifierChar + "]";
        String identifierFollowingChar = "[" + initialChar + digit + "]|[^" + unicodeNonidentifierChar + "]";
        String identifierString = "((?:" + identifierInitialChar + ")(?:" + identifierFollowingChar + ")*)";

        //Check if a qualified identifier
        String qualifierStage = identifierString + ":(.*)";
        //Check for type identifier
        String typeStage = identifierString + "\\." + identifierString + "(?:\\(\\))?";
	    //Check for `function()` reference stage
	    String funcIdentifierStage = identifierString + "(\\(\\))?";

	    //Pattern set
	    Pattern qualifierPattern = Pattern.compile(qualifierStage);
	    Pattern typePattern = Pattern.compile(typeStage);
	    Pattern functionIdentifierPattern = Pattern.compile(funcIdentifierStage);

	    //Matchers set
	    Matcher qualifierPatternMatcher = qualifierPattern.matcher(identifierContent);
	    Matcher typePatternMatcher = typePattern.matcher(identifierContent);
	    Matcher functionIdentifierPatternMatcher = functionIdentifierPattern.matcher(identifierContent);

	    if (qualifierPatternMatcher.matches()) {
	    	pckgName.append(qualifierPatternMatcher.group(1));
	    	//Match the remaining part of the identifier for type Group 2 has the remaining part if package name successfully
		    //matches
	    	Matcher typePatternMatcherForQualifiedMatch = typePattern.matcher(qualifierPatternMatcher.group(2));
	    	Matcher functionIdentifierPatternMatcherForQualifiedMatch =
				    functionIdentifierPattern.matcher(qualifierPatternMatcher.group(2));
	    	if (typePatternMatcherForQualifiedMatch.matches()) {
	    		typeName.append(typePatternMatcherForQualifiedMatch.group(1));
	    		//If Type is there, try to match the function name part. Group 2 contains string after the '.'
			    Matcher functionIdentifierPatternMatcherForQualifiedTypeMatch =
					    functionIdentifierPattern.matcher(typePatternMatcherForQualifiedMatch.group(2));
			    if (functionIdentifierPatternMatcherForQualifiedTypeMatch.matches()) {
			    	identifier.append(functionIdentifierPatternMatcherForQualifiedTypeMatch.group(1));
			    	return true;
			    } else {
			    	return false;
			    }
			//If type name is not there, directly validate for an unqualified Identifier
		    } else if (functionIdentifierPatternMatcherForQualifiedMatch.matches()) {
			    identifier.append(functionIdentifierPatternMatcherForQualifiedMatch.group(1));
			    return true;
		    } else {
	    		return false;
		    }
		//If no package name is there, do the above steps starting from the type name
	    } else if (typePatternMatcher.matches()) {
	    	typeName.append(typePatternMatcher.group(1));
		    Matcher functionIdentifierPatternMatcherForTypeMatch =
				    functionIdentifierPattern.matcher(typePatternMatcher.group(2));
		    if (functionIdentifierPatternMatcherForTypeMatch.matches()) {
			    identifier.append(functionIdentifierPatternMatcherForTypeMatch.group(1));
			    return true;
		    } else {
			    return false;
		    }
		//Directly validate for an identifier
	    } else if (functionIdentifierPatternMatcher.matches()) {
		    identifier.append(functionIdentifierPatternMatcher.group(1));
		    return true;
	    }

        return false;
    }

    //This function is used to create an identifier string from regex patterns used in
    //validateStringForQualifiedIdentifier function.
    private void getIdentifierFromMatch(boolean qualifiedMatch,
                                        StringBuilder pckgName,
                                        StringBuilder identifier,
                                        Matcher matcher) {
        if (qualifiedMatch) {
            pckgName.append(matcher.group(1));
            identifier.append(matcher.group(4));
        } else {
            identifier.append(matcher.group(1));
        }
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
