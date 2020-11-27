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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.projects.Module;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LSContext;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.util.Name;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.ballerinalang.langserver.common.utils.CommonUtil.createModuleID;
import static org.ballerinalang.langserver.common.utils.CommonUtil.getModulePrefix;

/**
 * Function generator utilities.
 */
public class FunctionGenerator {

    public static final Pattern FULLY_QUALIFIED_MODULE_ID_PATTERN = Pattern.compile("([\\w]+)\\/([\\w.]+):([\\d.]+):");

    /**
     * Generate function code.
     *
     * @param name               function name
     * @param args               Function arguments
     * @param returnType         return type
     * @param returnDefaultValue default return value
     * @param modifiers          modifiers
     * @param prependLineFeed    prepend line feed or not
     * @param padding            padding for the function
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
     * @param bLangNode       BLangNode to evaluate
     * @param template        return statement to modify
     * @param context         {@link LSContext}
     * @return {@link String}   Default return statement
     */
    public static String generateReturnValue(ImportsAcceptor importsAcceptor,
                                             BLangNode bLangNode, String template,
                                             LSContext context) {
        if (bLangNode.type == null && bLangNode instanceof BLangTupleDestructure) {
            // Check for tuple assignment eg. (int, int)
            List<String> list = new ArrayList<>();
            for (BLangExpression bLangExpression : ((BLangTupleDestructure) bLangNode).varRef.expressions) {
                if (bLangExpression.type != null) {
                    list.add(generateReturnValue(importsAcceptor, bLangExpression.type, "{%1}", context));
                }
            }
            return template.replace("{%1}", "(" + String.join(", ", list) + ")");
        } else if (bLangNode instanceof BLangLiteral) {
            return template.replace("{%1}", ((BLangLiteral) bLangNode).getValue().toString());
        } else if (bLangNode instanceof BLangAssignment) {
            return template.replace("{%1}", "0");
        }
        return (bLangNode.type != null)
                ? generateReturnValue(importsAcceptor, bLangNode.type, template, context)
                : null;
    }

    /**
     * Returns signature of the return type.
     *
     * @param importsAcceptor imports acceptor
     * @param typeDescriptor  {@link BLangNode}
     * @return return type signature
     */
    public static String generateTypeDefinition(ImportsAcceptor importsAcceptor,
                                                TypeSymbol typeDescriptor, DocumentServiceContext context) {
        return processModuleIDsInText(importsAcceptor, context, typeDescriptor.signature());
    }

    private static String processModuleIDsInText(ImportsAcceptor importsAcceptor, DocumentServiceContext context,
                                                 String text) {
        Module module = context.workspace().module(context.filePath()).orElseThrow();
        String currentOrg = module.packageInstance().descriptor().org().value();
        String currentModule = module.descriptor().name().packageName().value();
        String currentVersion = module.packageInstance().descriptor().version().value().toString();

        StringBuilder newText = new StringBuilder();
        ModuleID currentModuleID = createModuleID(currentOrg, currentModule, currentVersion);
        Matcher matcher = FULLY_QUALIFIED_MODULE_ID_PATTERN.matcher(text);
        int nextStart = 0;
        // Matching Fully-Qualified-Module-IDs (eg.`abc/mod1:1.0.0`)
        // Purpose is to transform `int|abc/mod1:1.0.0:Person` into `int|mod1:Person` or `int|Person`
        // base on the current module id and identifying the potential imports required.
        while (matcher.find()) {
            // Append up-to start of the match
            newText.append(text, nextStart, matcher.start(1));
            // Append module prefix(empty when in same module) and identify imports
            ModuleID moduleID = createModuleID(matcher.group(1), matcher.group(2), matcher.group(3));
            newText.append(getModulePrefix(importsAcceptor, currentModuleID, moduleID, context));
            // Update next-start position
            nextStart = matcher.end(3) + 1;
        }
        // Append the remaining
        if (nextStart != 0) {
            newText.append(text.substring(nextStart));
        }
        return newText.length() > 0 ? newText.toString() : text;
    }

    /**
     * Get the function arguments from the function.
     *
     * @param importsAcceptor imports accepter
     * @param parent          Parent node
     * @param context         {@link LSContext}
     * @return {@link List} List of arguments
     */
    @Deprecated
    public static List<String> getFuncArguments(ImportsAcceptor importsAcceptor, BLangNode parent,
                                                CompletionContext context) {
        List<String> list = new ArrayList<>();
        if (parent instanceof BLangInvocation) {
            BLangInvocation bLangInvocation = (BLangInvocation) parent;
            if (bLangInvocation.argExprs.isEmpty()) {
                return null;
            }
            int argCounter = 1;
            for (BLangExpression bLangExpression : bLangInvocation.argExprs) {
                // TODO: Fix
//                Set<String> argNames = CommonUtil.getAllNameEntries(compilerContext);
                Set<String> argNames = new HashSet<>();
                if (bLangExpression instanceof BLangSimpleVarRef) {
                    BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) bLangExpression;
                    String varName = simpleVarRef.variableName.value;
                    String argType = lookupVariableReturnType(importsAcceptor, varName, parent, context);
                    list.add(argType + " " + varName);
                    argNames.add(varName);
                } else if (bLangExpression instanceof BLangInvocation) {
//                    String argType = generateTypeDefinition(importsAcceptor, bLangExpression, context);
                    String argName = CommonUtil.generateVariableName(bLangExpression, argNames);
//                    list.add(argType + " " + argName);
                    argNames.add(argName);
                } else {
//                    String argType = generateTypeDefinition(importsAcceptor, bLangExpression, context);
                    String argName = CommonUtil.generateName(argCounter++, argNames);
//                    list.add(argType + " " + argName);
                    argNames.add(argName);
                }
            }
        }
        return (!list.isEmpty()) ? list : null;
    }

    private static String lookupVariableReturnType(ImportsAcceptor importsAcceptor,
                                                   String variableName, BLangNode parent,
                                                   DocumentServiceContext context) {
        // Recursively find BLangBlockStmt to get scope-entries
        if (parent instanceof BLangBlockStmt || parent instanceof BLangFunctionBody) {
            Scope scope = parent instanceof BLangBlockStmt ? ((BLangBlockStmt) parent).scope
                    : ((BLangFunctionBody) parent).scope;
            if (scope != null) {
                for (Map.Entry<Name, Scope.ScopeEntry> entry : scope.entries.entrySet()) {
                    String key = entry.getKey().getValue();
                    BSymbol symbol = entry.getValue().symbol;
                    if (variableName.equals(key) && symbol instanceof BVarSymbol) {
//                        return generateTypeDefinition(importsAcceptor, symbol.type, context);
                        //TODO Fix this
                        return null;
                    }
                }
            }
        }

        return (parent != null && parent.parent != null)
                ? lookupVariableReturnType(importsAcceptor, variableName, parent.parent, context)
                : "any";
    }

    public static String generateReturnValue(ImportsAcceptor importsAcceptor,
                                             BType bType,
                                             String template, LSContext context) {
        if (bType instanceof BArrayType) {
            String arrDef = "[" + generateReturnValue(importsAcceptor, ((BArrayType) bType).eType,
                    "{%1}", context) + "]";
            return template.replace("{%1}", arrDef);
        } else if (bType instanceof BFiniteType) {
            // Check for finite set assignment
            BFiniteType bFiniteType = (BFiniteType) bType;
            Set<BLangExpression> valueSpace = bFiniteType.getValueSpace();
            if (!valueSpace.isEmpty()) {
                return generateReturnValue(importsAcceptor, valueSpace.stream().findFirst().get(),
                        template, context);
            }
        } else if (bType instanceof BMapType && ((BMapType) bType).constraint != null) {
            // Check for constrained map assignment eg. map<Student>
            BType constraintType = ((BMapType) bType).constraint;
            String mapDef = "{key: " + generateReturnValue(importsAcceptor, constraintType, "{%1}",
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
                    return generateReturnValue(importsAcceptor, type.get(), template, context);
                }
            }
            if (!memberTypes.isEmpty()) {
                BType firstBType = memberTypes.stream().findFirst().get();
                return generateReturnValue(importsAcceptor, firstBType, template, context);
            }
        } else if (bType instanceof BTupleType) {
            BTupleType bTupleType = (BTupleType) bType;
            List<BType> tupleTypes = bTupleType.tupleTypes;
            List<String> list = new ArrayList<>();
            for (BType type : tupleTypes) {
                list.add(generateReturnValue(importsAcceptor, type, "{%1}", context));
            }
            return template.replace("{%1}", "(" + String.join(", ", list) + ")");
        } else if (bType instanceof BObjectType && ((BObjectType) bType).tsymbol instanceof BObjectTypeSymbol) {
            BObjectTypeSymbol bStruct = (BObjectTypeSymbol) ((BObjectType) bType).tsymbol;
            List<String> list = new ArrayList<>();
            for (BVarSymbol param : bStruct.initializerFunc.symbol.params) {
                list.add(generateReturnValue(param.type.tsymbol, "{%1}"));
            }
            //Fix this
//            String pkgPrefix = CommonUtil.getPackagePrefix(importsAcceptor, bStruct.pkgID, context);
            String pkgPrefix = "";
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
