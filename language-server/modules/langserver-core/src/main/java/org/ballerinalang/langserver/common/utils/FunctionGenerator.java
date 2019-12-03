/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.common.utils;

import org.ballerinalang.langserver.command.testgen.TestGenerator;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Function generator utilities.
 */
public class FunctionGenerator {

    /**
     * Generate function code.
     *
     * @param name               function name
     * @param args               Function arguments
     * @param returnType         return type
     * @param returnDefaultValue default return value
     * @param modifiers         modifiers
     * @param prependLineFeed   prepend line feed or not
     * @param padding           padding for the function
     * @return {@link String}       generated function signature
     */
    public static String createFunction(String name, String args, String returnType, String returnDefaultValue,
                                        String modifiers, boolean prependLineFeed, String padding) {
        String funcBody = CommonUtil.LINE_SEPARATOR;
        String funcReturnSignature = "";
        if (returnType != null) {
            funcBody = returnDefaultValue + funcBody;
            funcReturnSignature = " returns " + returnType + " ";
        }
        String lineFeed = prependLineFeed ? CommonUtil.LINE_SEPARATOR + CommonUtil.LINE_SEPARATOR
                : CommonUtil.LINE_SEPARATOR;
        return lineFeed + padding + modifiers + "function " + name + "(" + args + ")"
                + funcReturnSignature + "{" + CommonUtil.LINE_SEPARATOR + funcBody + padding + "}"
                + CommonUtil.LINE_SEPARATOR;
    }

    /**
     * Get the default function return statement.
     *
     * @param importsAcceptor imports acceptor
     * @param currentPkgId    current package id
     * @param bLangNode       BLangNode to evaluate
     * @param template        return statement to modify
     * @return {@link String}   Default return statement
     */
    public static String generateReturnValue(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                             BLangNode bLangNode, String template) {
        if (bLangNode.type == null && bLangNode instanceof BLangTupleDestructure) {
            // Check for tuple assignment eg. (int, int)
            List<String> list = new ArrayList<>();
            for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                if (bLangExpression.type != null) {
                    list.add(generateReturnValue(importsAcceptor, currentPkgId, bLangExpression.type, "{%1}"));
                }
            }
            return template.replace("{%1}", "(" + String.join(", ", list) + ")");
        } else if (bLangNode instanceof BLangLiteral) {
            return template.replace("{%1}", ((BLangLiteral) bLangNode).getValue().toString());
        } else if (bLangNode instanceof BLangAssignment) {
            return template.replace("{%1}", "0");
        }
        return (bLangNode.type != null)
                ? generateReturnValue(importsAcceptor, currentPkgId, bLangNode.type, template)
                : null;
    }

    /**
     * Returns signature of the return type.
     *
     * @param importsAcceptor imports acceptor
     * @param currentPkgId    current package id
     * @param bLangNode       {@link BLangNode}
     * @return return type signature
     */
    public static String generateTypeDefinition(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                                BLangNode bLangNode) {
        if (bLangNode instanceof BLangTupleDestructure && bLangNode.type == null) {
            // Check for tuple assignment eg. (int, int)
            List<String> list = new ArrayList<>();
            for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                if (bLangExpression.type != null) {
                    list.add(generateTypeDefinition(importsAcceptor, currentPkgId, bLangExpression.type));
                }
            }
            return "[" + String.join(", ", list) + "]";
        } else if (bLangNode instanceof BLangAssignment) {
            if (((BLangAssignment) bLangNode).declaredWithVar) {
                return "any";
            }
        } else if (bLangNode instanceof BLangFunctionTypeNode) {
            BLangFunctionTypeNode funcType = (BLangFunctionTypeNode) bLangNode;
            TestGenerator.TestFunctionGenerator generator = new TestGenerator.TestFunctionGenerator(importsAcceptor,
                    currentPkgId, funcType);
            String[] typeSpace = generator.getTypeSpace();
            String[] nameSpace = generator.getNamesSpace();
            StringJoiner params = new StringJoiner(", ");
            IntStream.range(0, typeSpace.length - 1).forEach(index -> {
                String type = typeSpace[index];
                String name = nameSpace[index];
                params.add(type + " " + name);
            });
            return "function (" + params.toString() + ") returns (" + typeSpace[typeSpace.length - 1] + ")";
        }
        return (bLangNode != null && bLangNode.type != null)
                ? generateTypeDefinition(importsAcceptor, currentPkgId, bLangNode.type) : null;
    }

    /**
     * Returns signature of the return type.
     *
     * @param importsAcceptor imports acceptor
     * @param currentPkgId    current package id
     * @param bType           {@link BType}
     * @return return type signature
     */
    public static String generateTypeDefinition(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                                BType bType) {
        if ((bType.tsymbol == null || bType.tsymbol.name.value.isEmpty()) && bType instanceof BArrayType) {
            // Check for array assignment eg.  int[]
            return generateTypeDefinition(importsAcceptor, currentPkgId, ((BArrayType) bType).eType.tsymbol) + "[]";
        } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
            // Check for constrained map assignment eg. map<Student>
            BTypeSymbol tSymbol = ((BMapType) bType).constraint.tsymbol;
            if (tSymbol != null) {
                String constraint = generateTypeDefinition(importsAcceptor, currentPkgId, tSymbol);
                return ("any".equals(constraint)) ? "map" : "map<" + constraint + ">";
            }
        } else if (bType instanceof BUnionType) {
            // Check for union type assignment eg. int | string
            List<String> list = new ArrayList<>();
            List<BType> memberTypes = new ArrayList<>(((BUnionType) bType).getMemberTypes());
            if (memberTypes.size() == 2 && memberTypes.stream().anyMatch(bType1 -> bType1 instanceof BNilType)) {
                Optional<BType> type = memberTypes.stream()
                        .filter(bType1 -> !(bType1 instanceof BNilType)).findFirst();
                if (type.isPresent()) {
                    return generateTypeDefinition(importsAcceptor, currentPkgId, type.get()) + "?";
                }
            }
            // Check for multiple error member types and add generic error to avoid flooding member types eg.http errors
            long errorTypesCount = memberTypes.stream().filter(t -> t instanceof BErrorType).count();
            boolean addErrorTypeAtEnd = false;
            if (errorTypesCount > 1) {
                memberTypes.removeIf(s -> s instanceof BErrorType);
                if (memberTypes.size() == 1 && memberTypes.get(0) instanceof BNilType) {
                    return "error?";
                } else {
                    addErrorTypeAtEnd = true;
                }
            }
            for (BType memberType : memberTypes) {
                list.add(generateTypeDefinition(importsAcceptor, currentPkgId, memberType));
            }
            if (addErrorTypeAtEnd) {
                list.add("error");
            }
            return String.join("|", list);
        } else if (bType instanceof BTupleType) {
            // Check for tuple type assignment eg. int, string
            List<String> list = new ArrayList<>();
            for (BType memberType : ((BTupleType) bType).tupleTypes) {
                list.add(generateTypeDefinition(importsAcceptor, currentPkgId, memberType));
            }
            return "[" + String.join(", ", list) + "]";
        } else if (bType instanceof BNilType) {
            return "()";
        } else if (bType instanceof BIntermediateCollectionType) {
            // TODO: 29/11/2018 fix this. A hack to infer type definition
            // We assume;
            // 1. Tuple of <key(string), value(string)> as a map(though it can be a record as well)
            // 2. Tuple of <index(int), value(string)> as an array
            BIntermediateCollectionType collectionType = (BIntermediateCollectionType) bType;
            List<String> list = new ArrayList<>();
            List<BType> tupleTypes = collectionType.tupleType.tupleTypes;
            if (tupleTypes.size() == 2) {
                BType leftType = tupleTypes.get(0);
                BType rightType = tupleTypes.get(1);
                switch (leftType.tsymbol.name.value) {
                    case "int":
                        return generateTypeDefinition(importsAcceptor, currentPkgId, rightType) + "[]";
                    case "string":
                    default:
                        return "map<" + generateTypeDefinition(importsAcceptor, currentPkgId, rightType) + ">";
                }
            }
            for (BType memberType : tupleTypes) {
                list.add(generateTypeDefinition(importsAcceptor, currentPkgId, memberType));
            }
            return "[" + String.join(", ", list) + "][]";
        } else if (bType instanceof BTableType) {
            return "table<record {}>";
        }
        return (bType.tsymbol != null) ? generateTypeDefinition(importsAcceptor, currentPkgId, bType.tsymbol) :
                "any";
    }

    /**
     * Get the list of function arguments from the invokable symbol.
     *
     * @param symbol Invokable symbol to extract the arguments
     * @param ctx Lang Server Operation context
     * @return {@link List} List of arguments
     */
    public static List<String> getFuncArguments(BInvokableSymbol symbol, LSContext ctx) {
        List<String> list = new ArrayList<>();
        int invocationType = (ctx == null || ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY) == null) ? -1
                : ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        boolean skipFirstParam = CommonUtil.skipFirstParam(symbol, invocationType);
        BVarSymbol restParam = symbol.restParam;
        if (symbol.kind == null && SymbolKind.RECORD == symbol.owner.kind || SymbolKind.FUNCTION == symbol.owner.kind) {
            if (symbol.type instanceof BInvokableType) {
                BInvokableType bInvokableType = (BInvokableType) symbol.type;
                if (bInvokableType.paramTypes.isEmpty()) {
                    return list;
                }
                int argCounter = 1;
                Set<String> argNames = new HashSet<>(); // To avoid name duplications
                List<BType> parameterTypes = bInvokableType.getParameterTypes();
                for (int i = 0; i < parameterTypes.size(); i++) {
                    if (i == 0 && skipFirstParam) {
                        continue;
                    }
                    BType bType = parameterTypes.get(i);
                    String argName = CommonUtil.generateName(argCounter++, argNames);
                    String argType = generateTypeDefinition(null, symbol.pkgID, bType);
                    list.add(argType + " " + argName);
                    argNames.add(argName);
                }
                if (restParam != null && (restParam.type instanceof BArrayType)) {
                    list.add(CommonUtil.getBTypeName(((BArrayType) restParam.type).eType, ctx, false) + "... "
                            + restParam.getName().getValue());
                }
            }
        } else {
            List<BVarSymbol> parameterDefs = new ArrayList<>(symbol.getParameters());
            for (int i = 0; i < parameterDefs.size(); i++) {
                if (i == 0 && skipFirstParam) {
                    continue;
                }
                BVarSymbol param = parameterDefs.get(i);
                list.add(CommonUtil.getBTypeName(param.type, ctx, true) + " " + param.getName());
            }
            if (restParam != null && (restParam.type instanceof BArrayType)) {
                list.add(CommonUtil.getBTypeName(((BArrayType) restParam.type).eType, ctx, false) + "... "
                        + restParam.getName().getValue());
            }
        }
        return (!list.isEmpty()) ? list : new ArrayList<>();
    }

    /**
     * Get the function arguments from the function.
     *
     * @param importsAcceptor imports accepter
     * @param currentPkgId current package ID
     * @param parent Parent node
     * @param context
     * @return {@link List} List of arguments
     */
    public static List<String> getFuncArguments(BiConsumer<String, String> importsAcceptor,
                                                PackageID currentPkgId, BLangNode parent,
                                                LSContext context) {
        List<String> list = new ArrayList<>();
        if (parent instanceof BLangInvocation) {
            BLangInvocation bLangInvocation = (BLangInvocation) parent;
            if (bLangInvocation.argExprs.isEmpty()) {
                return null;
            }
            int argCounter = 1;
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            for (BLangExpression bLangExpression : bLangInvocation.argExprs) {
                Set<String> argNames = CommonUtil.getAllNameEntries(bLangExpression, compilerContext);
                if (bLangExpression instanceof BLangSimpleVarRef) {
                    BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) bLangExpression;
                    String varName = simpleVarRef.variableName.value;
                    String argType = lookupVariableReturnType(importsAcceptor, currentPkgId, varName, parent);
                    list.add(argType + " " + varName);
                    argNames.add(varName);
                } else if (bLangExpression instanceof BLangInvocation) {
                    BLangInvocation invocation = (BLangInvocation) bLangExpression;
                    String functionName = invocation.name.value;
                    String argType = lookupFunctionReturnType(functionName, parent);
                    String argName = CommonUtil.generateVariableName(bLangExpression, argNames);
                    list.add(argType + " " + argName);
                    argNames.add(argName);
                } else {
                    String argType = generateTypeDefinition(importsAcceptor, currentPkgId, bLangExpression);
                    String argName = CommonUtil.generateName(argCounter++, argNames);
                    list.add(argType + " " + argName);
                    argNames.add(argName);
                }
            }
        }
        return (!list.isEmpty()) ? list : null;
    }

    private static String generateTypeDefinition(BiConsumer<String, String> importsAcceptor,
                                                 PackageID currentPkgId, BTypeSymbol tSymbol) {
        if (tSymbol != null) {
            String pkgPrefix = CommonUtil.getPackagePrefix(importsAcceptor, currentPkgId, tSymbol.pkgID);
            return pkgPrefix + tSymbol.name.getValue();
        }
        return "any";
    }

    private static String lookupVariableReturnType(BiConsumer<String, String> importsAcceptor,
                                                   PackageID currentPkgId,
                                                   String variableName, BLangNode parent) {
        if (parent instanceof BLangBlockStmt) {
            BLangBlockStmt blockStmt = (BLangBlockStmt) parent;
            Scope scope = blockStmt.scope;
            if (scope != null) {
                for (Map.Entry<Name, Scope.ScopeEntry> entry : scope.entries.entrySet()) {
                    String key = entry.getKey().getValue();
                    BSymbol symbol = entry.getValue().symbol;
                    if (variableName.equals(key) && symbol instanceof BVarSymbol) {
                        return generateTypeDefinition(importsAcceptor, currentPkgId, symbol.type);
                    }
                }
            }
        }
        return (parent != null && parent.parent != null)
                ? lookupVariableReturnType(importsAcceptor, currentPkgId, variableName, parent.parent)
                : "any";
    }

    private static String lookupFunctionReturnType(String functionName, BLangNode parent) {
        if (parent instanceof BLangPackage) {
            BLangPackage blockStmt = (BLangPackage) parent;
            List<BLangFunction> functions = blockStmt.functions;
            for (BLangFunction function : functions) {
                if (functionName.equals(function.name.getValue())) {
                    return generateTypeDefinition(null, ((BLangPackage) parent).packageID, function.returnTypeNode);
                }
            }
        }
        return (parent != null && parent.parent != null)
                ? lookupFunctionReturnType(functionName, parent.parent) : "any";
    }

    private static String generateReturnValue(BiConsumer<String, String> importsAcceptor, PackageID currentPkgId,
                                              BType bType,
                                              String template) {
        if (bType.tsymbol == null && bType instanceof BArrayType) {
            return template.replace("{%1}", "[" +
                    generateReturnValue(((BArrayType) bType).eType.tsymbol, "") + "]");
        } else if (bType instanceof BFiniteType) {
            // Check for finite set assignment
            BFiniteType bFiniteType = (BFiniteType) bType;
            Set<BLangExpression> valueSpace = bFiniteType.valueSpace;
            if (!valueSpace.isEmpty()) {
                return generateReturnValue(importsAcceptor, currentPkgId, valueSpace.stream().findFirst().get(),
                        template);
            }
        } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
            // Check for constrained map assignment eg. map<Student>
            BType constraintType = ((BMapType) bType).constraint;
            String mapDef = "{key: " + generateReturnValue(importsAcceptor, currentPkgId, constraintType, "{%1}") +
                    "}";
            return template.replace("{%1}", mapDef);
        } else if (bType instanceof BUnionType) {
            BUnionType bUnionType = (BUnionType) bType;
            Set<BType> memberTypes = bUnionType.getMemberTypes();
            if (memberTypes.size() == 2 && memberTypes.stream().anyMatch(bType1 -> bType1 instanceof BNilType)) {
                Optional<BType> type = memberTypes.stream()
                        .filter(bType1 -> !(bType1 instanceof BNilType)).findFirst();
                if (type.isPresent()) {
                    return generateReturnValue(importsAcceptor, currentPkgId, type.get(), "{%1}?");
                }
            }
            if (!memberTypes.isEmpty()) {
                BType firstBType = memberTypes.stream().findFirst().get();
                return generateReturnValue(importsAcceptor, currentPkgId, firstBType, template);
            }
        } else if (bType instanceof BTupleType) {
            BTupleType bTupleType = (BTupleType) bType;
            List<BType> tupleTypes = bTupleType.tupleTypes;
            List<String> list = new ArrayList<>();
            for (BType type : tupleTypes) {
                list.add(generateReturnValue(importsAcceptor, currentPkgId, type, "{%1}"));
            }
            return template.replace("{%1}", "(" + String.join(", ", list) + ")");
        } else if (bType instanceof BObjectType && ((BObjectType) bType).tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol bStruct = (BObjectTypeSymbol) ((BObjectType) bType).tsymbol;
            List<String> list = new ArrayList<>();
            for (BVarSymbol param : bStruct.initializerFunc.symbol.params) {
                list.add(generateReturnValue(param.type.tsymbol, "{%1}"));
            }
            String pkgPrefix = CommonUtil.getPackagePrefix(importsAcceptor, currentPkgId, bStruct.pkgID);
            String paramsStr = String.join(", ", list);
            String newObjStr = "new " + pkgPrefix + bStruct.name.getValue() + "(" + paramsStr + ")";
            return template.replace("{%1}", newObjStr);
        }
        return (bType.tsymbol != null) ? generateReturnValue(bType.tsymbol, template) :
                template.replace("{%1}", "()");
    }

    private static String generateReturnValue(BTypeSymbol tSymbol, String template) {
        String result;
        switch (tSymbol.name.getValue()) {
            case "int":
            case "any":
                result = "0";
                break;
            case "string":
                result = "\"\"";
                break;
            case "float":
                result = "0.0";
                break;
            case "json":
                result = "{}";
                break;
            case "map":
                result = "<map>{}";
                break;
            case "boolean":
                result = "false";
                break;
            case "xml":
                result = "xml ` `";
                break;
            case "byte":
                result = "0";
                break;
            case "table":
                result = "table{}";
                break;
            default:
                result = "()";
                break;
        }
        return template.replace("{%1}", result);
    }
}
