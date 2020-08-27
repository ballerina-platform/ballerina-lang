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
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
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
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
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
     * @param context  {@link LSContext}
     * @return {@link String}   Default return statement
     */
    public static String generateReturnValue(ImportsAcceptor importsAcceptor, PackageID currentPkgId,
                                             BLangNode bLangNode, String template,
                                             LSContext context) {
        if (bLangNode.type == null && bLangNode instanceof BLangTupleDestructure) {
            // Check for tuple assignment eg. (int, int)
            List<String> list = new ArrayList<>();
            for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                if (bLangExpression.type != null) {
                    list.add(generateReturnValue(importsAcceptor, currentPkgId, bLangExpression.type, "{%1}", context));
                }
            }
            return template.replace("{%1}", "(" + String.join(", ", list) + ")");
        } else if (bLangNode instanceof BLangLiteral) {
            return template.replace("{%1}", ((BLangLiteral) bLangNode).getValue().toString());
        } else if (bLangNode instanceof BLangAssignment) {
            return template.replace("{%1}", "0");
        }
        return (bLangNode.type != null)
                ? generateReturnValue(importsAcceptor, currentPkgId, bLangNode.type, template, context)
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
    public static String generateTypeDefinition(ImportsAcceptor importsAcceptor, PackageID currentPkgId,
                                                BLangNode bLangNode, LSContext context) {
        return generateTypeDefinition(importsAcceptor, 1, currentPkgId, bLangNode, context);
    }

    /**
     * Returns signature of the return type.
     *
     * @param importsAcceptor imports acceptor
     * @param currentPkgId    current package id
     * @param bType           {@link BType}
     * @return return type signature
     */
    public static String generateTypeDefinition(ImportsAcceptor importsAcceptor, PackageID currentPkgId,
                                                BType bType, LSContext context) {
        return generateTypeDefinition(importsAcceptor, 1, currentPkgId, bType, context);
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
        boolean skipFirstParam = CommonUtil.skipFirstParam(ctx, symbol);
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
                    String argType = generateTypeDefinition(null, symbol.pkgID, bType, ctx);
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
     * @param context  {@link LSContext}
     * @return {@link List} List of arguments
     */
    public static List<String> getFuncArguments(ImportsAcceptor importsAcceptor,
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
                Set<String> argNames = CommonUtil.getAllNameEntries(compilerContext);
                if (bLangExpression instanceof BLangSimpleVarRef) {
                    BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) bLangExpression;
                    String varName = simpleVarRef.variableName.value;
                    String argType = lookupVariableReturnType(importsAcceptor, currentPkgId, varName, parent, context);
                    list.add(argType + " " + varName);
                    argNames.add(varName);
                } else if (bLangExpression instanceof BLangInvocation) {
                    String argType = generateTypeDefinition(importsAcceptor, currentPkgId, bLangExpression, context);
                    String argName = CommonUtil.generateVariableName(bLangExpression, argNames);
                    list.add(argType + " " + argName);
                    argNames.add(argName);
                } else {
                    String argType = generateTypeDefinition(importsAcceptor, currentPkgId, bLangExpression, context);
                    String argName = CommonUtil.generateName(argCounter++, argNames);
                    list.add(argType + " " + argName);
                    argNames.add(argName);
                }
            }
        }
        return (!list.isEmpty()) ? list : null;
    }

    private static String generateTypeDefinition(ImportsAcceptor importsAcceptor, int wsOffset,
                                                 PackageID currentPkgId,
                                                 BLangNode bLangNode,
                                                 LSContext context) {
        if (bLangNode instanceof BLangTupleDestructure && bLangNode.type == null) {
            // Check for tuple assignment eg. (int, int)
            List<String> list = new ArrayList<>();
            for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                if (bLangExpression.type != null) {
                    list.add(generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, bLangExpression.type,
                                                    context));
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
                                                                                                    currentPkgId,
                                                                                                    funcType, context);
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
                ? generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, bLangNode.type, context) : null;
    }

    private static String generateTypeDefinition(ImportsAcceptor importsAcceptor, int wsOffset,
                                                 PackageID currentPkgId,
                                                 BType bType, LSContext context) {
        if ((bType.tsymbol == null || bType.tsymbol.name.value.isEmpty()) && bType instanceof BArrayType) {
            // Check for array assignment eg.  int[]
            return generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, ((BArrayType) bType).eType,
                                          context) + "[]";
        } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
            // Check for constrained map assignment eg. map<Student>
            BTypeSymbol tSymbol = ((BMapType) bType).constraint.tsymbol;
            if (tSymbol != null) {
                String constraint = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, tSymbol, context);
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
                    return generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, type.get(), context) + "?";
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
                list.add(generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, memberType, context));
            }
            if (addErrorTypeAtEnd) {
                list.add("error");
            }
            return String.join("|", list);
        } else if (bType instanceof BXMLType) {
            BXMLType xmlType = (BXMLType) bType;
            String cType = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, xmlType.constraint, context);
            return "xml<" + cType + ">";
        } else if (bType instanceof BTupleType) {
            // Check for tuple type assignment eg. int, string
            List<String> list = new ArrayList<>();
            for (BType memberType : ((BTupleType) bType).tupleTypes) {
                list.add(generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, memberType, context));
            }
            return "[" + String.join(", ", list) + "]";
        } else if (bType instanceof BNilType) {
            return "()";
        } else if (bType instanceof BStreamType) {
            BStreamType streamType = (BStreamType) bType;
            String constraint = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, streamType.constraint,
                                                       context);
            String error = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, streamType.error, context);
            return "stream<" + constraint + ", " + error + ">";
        } else if (bType instanceof BRecordType) {
            BRecordType recordType = (BRecordType) bType;
            if (recordType.tsymbol != null && recordType.tsymbol.name != null &&
                    (recordType.tsymbol.name.value.isEmpty() || recordType.tsymbol.name.value.startsWith("$"))) {
                StringBuilder sb = new StringBuilder();
                sb.append("record").append(" ").append("{|");
                for (BField field : recordType.fields.values()) {
                    sb.append(" ").append(field.type).append(" ").append(field.name)
                            .append(Symbols.isOptional(field.symbol) ? "?" : "")
                            .append(";");
                }
                if (recordType.sealed) {
                    sb.append(" ").append("|}");
                    return sb.toString();
                }
                sb.append(" ").append(recordType.restFieldType).append("...").append(";").append(" ").append("|}");
                return sb.toString();
            }
            return generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, bType.tsymbol, context);
        }
        return (bType.tsymbol != null) ? generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId,
                                                                bType.tsymbol, context) :
                "any";
    }

    private static String generateTypeDefinition(ImportsAcceptor importsAcceptor, int wsOffset,
                                                 PackageID currentPkgId, BTypeSymbol tSymbol, LSContext context) {
        if (tSymbol != null) {
            // Generate function type text
            if (tSymbol instanceof BInvokableTypeSymbol) {
                BInvokableTypeSymbol invokableTypeSymbol = (BInvokableTypeSymbol) tSymbol;
                StringJoiner params = new StringJoiner(", ");
                int argCounter = 1;
                Set<String> argNames = new HashSet<>(); // To avoid name duplications
                for (BVarSymbol param : invokableTypeSymbol.params) {
                    BType bType = param.type;
                    String argName = CommonUtil.generateName(argCounter++, argNames);
                    String argType = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, bType, context);
                    params.add(argType + " " + argName);
                    argNames.add(argName);
                }
                BVarSymbol restParam = invokableTypeSymbol.restParam;
                if (restParam != null && (restParam.type instanceof BArrayType)) {
                    BArrayType type = (BArrayType) restParam.type;
                    params.add(generateTypeDefinition(importsAcceptor, wsOffset, type.eType.tsymbol.pkgID, type.eType,
                                                      context) +
                                       "... "
                                       + restParam.getName().getValue());
                }
                String returnType = (invokableTypeSymbol.returnType != null)
                        ? generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId,
                                                 invokableTypeSymbol.returnType, context)
                        : "";
                return "function (" + params.toString() + ")" + (returnType.isEmpty() ? "" : " returns " + returnType);
            } else if (tSymbol instanceof BObjectTypeSymbol && tSymbol.name.value.startsWith("$")) {
                // Anon Object
                StringBuilder builder = new StringBuilder();
                BObjectTypeSymbol objectTypeSymbol = (BObjectTypeSymbol) tSymbol;
                boolean isAbstract = (objectTypeSymbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT;
                if (isAbstract) {
                    builder.append("abstract ");
                }
                builder.append("object {");
                if (objectTypeSymbol.attachedFuncs != null && !objectTypeSymbol.attachedFuncs.isEmpty()) {
                    builder.append(CommonUtil.LINE_SEPARATOR);
                    String objWS = new String(new char[wsOffset]).replace("\0", "\t");
                    int newOffset = ++wsOffset;
                    String funcWS = new String(new char[wsOffset]).replace("\0", "\t");
                    objectTypeSymbol.attachedFuncs.forEach(func -> {
                        builder.append(funcWS);
                        builder.append(generateTypeDefinition(importsAcceptor, newOffset, currentPkgId, func, context));
                        builder.append(";");
                        builder.append(CommonUtil.LINE_SEPARATOR);
                    });
                    builder.append(objWS);
                    builder.append("}");
                } else {
                    builder.append("}");
                }
                return builder.toString();
            } else {
                String pkgPrefix = CommonUtil.getPackagePrefix(importsAcceptor, currentPkgId, tSymbol.pkgID, context);
                return pkgPrefix + tSymbol.name.getValue();
            }
        }
        return "any";
    }

    private static String generateTypeDefinition(ImportsAcceptor importsAcceptor, int wsOffset,
                                                 PackageID currentPkgId, BAttachedFunction func,
                                                 LSContext context) {
        StringBuilder builder = new StringBuilder();
        String modifiers = parseModifiers(func.symbol.flags);
        modifiers = modifiers.isEmpty() ? "" : modifiers + " ";
        builder.append(modifiers);
        builder.append("function ");
        builder.append(func.funcName);
        builder.append("(");

        // Params
        BInvokableType type = func.type;
        StringJoiner params = new StringJoiner(", ");
        int argCounter = 1;
        Set<String> argNames = new HashSet<>(); // To avoid name duplications
        List<BType> parameterTypes = type.getParameterTypes();
        for (BType bType : parameterTypes) {
            String argName = CommonUtil.generateName(argCounter++, argNames);
            String argType = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, bType, context);
            params.add(argType + " " + argName);
            argNames.add(argName);
        }
        BType restParam = type.restType;
        if ((restParam instanceof BArrayType)) {
            String restType = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId,
                                                     ((BArrayType) restParam).eType, context);
            String argName = CommonUtil.generateName(argCounter++, argNames);
            params.add(restType + "... " + argName);
        }

        builder.append(")");

        if (type.retType != null) {
            builder.append(" returns (");
            String retType = generateTypeDefinition(importsAcceptor, wsOffset, currentPkgId, type.retType, context);
            builder.append(retType);
            builder.append(")");
        }

        return builder.toString();
    }

    private static String parseModifiers(int flagValue) {
        StringJoiner joiner = new StringJoiner(" ");
        if ((flagValue & Flags.PUBLIC) == Flags.PUBLIC) {
            joiner.add("public");
        }
        if ((flagValue & Flags.PRIVATE) == Flags.PRIVATE) {
            joiner.add("private");
        }
        if ((flagValue & Flags.ABSTRACT) == Flags.ABSTRACT) {
            joiner.add("abstract");
        }
        if ((flagValue & Flags.REMOTE) == Flags.REMOTE) {
            joiner.add("remote");
        }
        return joiner.toString();
    }

    private static String lookupVariableReturnType(ImportsAcceptor importsAcceptor,
                                                   PackageID currentPkgId,
                                                   String variableName, BLangNode parent,
                                                   LSContext context) {
        // Recursively find BLangBlockStmt to get scope-entries
        if (parent instanceof BLangBlockStmt || parent instanceof BLangFunctionBody) {
            Scope scope = parent instanceof BLangBlockStmt ? ((BLangBlockStmt) parent).scope
                    : ((BLangFunctionBody) parent).scope;
            if (scope != null) {
                for (Map.Entry<Name, Scope.ScopeEntry> entry : scope.entries.entrySet()) {
                    String key = entry.getKey().getValue();
                    BSymbol symbol = entry.getValue().symbol;
                    if (variableName.equals(key) && symbol instanceof BVarSymbol) {
                        return generateTypeDefinition(importsAcceptor, currentPkgId, symbol.type, context);
                    }
                }
            }
        }
        return (parent != null && parent.parent != null)
                ? lookupVariableReturnType(importsAcceptor, currentPkgId, variableName, parent.parent, context)
                : "any";
    }

    public static String generateReturnValue(ImportsAcceptor importsAcceptor, PackageID currentPkgId,
                                             BType bType,
                                             String template, LSContext context) {
        if (bType instanceof BArrayType) {
            String arrDef = "[" + generateReturnValue(importsAcceptor, currentPkgId, ((BArrayType) bType).eType,
                                                      "{%1}", context) + "]";
            return template.replace("{%1}", arrDef);
        } else if (bType instanceof BFiniteType) {
            // Check for finite set assignment
            BFiniteType bFiniteType = (BFiniteType) bType;
            Set<BLangExpression> valueSpace = bFiniteType.getValueSpace();
            if (!valueSpace.isEmpty()) {
                return generateReturnValue(importsAcceptor, currentPkgId, valueSpace.stream().findFirst().get(),
                                           template, context);
            }
        } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
            // Check for constrained map assignment eg. map<Student>
            BType constraintType = ((BMapType) bType).constraint;
            String mapDef = "{key: " + generateReturnValue(importsAcceptor, currentPkgId, constraintType, "{%1}",
                                                           context) +
                    "}";
            return template.replace("{%1}", mapDef);
        } else if (bType instanceof BUnionType) {
            BUnionType bUnionType = (BUnionType) bType;
            Set<BType> memberTypes = bUnionType.getMemberTypes();
            if (memberTypes.size() == 2 && memberTypes.stream().anyMatch(bType1 -> bType1 instanceof BNilType)) {
                Optional<BType> type = memberTypes.stream()
                        .filter(bType1 -> !(bType1 instanceof BNilType)).findFirst();
                if (type.isPresent()) {
                    return generateReturnValue(importsAcceptor, currentPkgId, type.get(), template, context);
                }
            }
            if (!memberTypes.isEmpty()) {
                BType firstBType = memberTypes.stream().findFirst().get();
                return generateReturnValue(importsAcceptor, currentPkgId, firstBType, template, context);
            }
        } else if (bType instanceof BTupleType) {
            BTupleType bTupleType = (BTupleType) bType;
            List<BType> tupleTypes = bTupleType.tupleTypes;
            List<String> list = new ArrayList<>();
            for (BType type : tupleTypes) {
                list.add(generateReturnValue(importsAcceptor, currentPkgId, type, "{%1}", context));
            }
            return template.replace("{%1}", "(" + String.join(", ", list) + ")");
        } else if (bType instanceof BObjectType && ((BObjectType) bType).tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol bStruct = (BObjectTypeSymbol) ((BObjectType) bType).tsymbol;
            List<String> list = new ArrayList<>();
            for (BVarSymbol param : bStruct.initializerFunc.symbol.params) {
                list.add(generateReturnValue(param.type.tsymbol, "{%1}"));
            }
            String pkgPrefix = CommonUtil.getPackagePrefix(importsAcceptor, currentPkgId, bStruct.pkgID, context);
            String paramsStr = String.join(", ", list);
            String newObjStr = "new " + pkgPrefix + bStruct.name.getValue() + "(" + paramsStr + ")";
            return template.replace("{%1}", newObjStr);
        } else if (bType instanceof BRecordType) {
            BRecordType recordType = (BRecordType) bType;
            StringJoiner sb = new StringJoiner(", ");
            if (CommonUtil.isInvalidSymbol(recordType.tsymbol)) {
                for (BField field : recordType.fields.values()) {
                    sb.add(field.name.value + ": " + generateReturnValue(field.type.tsymbol, "{%1}"));
                }
            }
            return template.replace("{%1}", "{" + sb.toString() + "}");
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
            case "error":
                result = "error(\"\")";
                break;
            default:
                result = "()";
                break;
        }
        return template.replace("{%1}", result);
    }
}
