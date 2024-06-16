/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */

package io.ballerina.utils;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.component.ForStatementNode;
import io.ballerina.component.IfStatementNode;
import io.ballerina.component.Node;
import org.ballerinalang.model.symbols.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Locale;

/**
 * utils class for ActionInvocationParser.
 */
public class ParserUtil {

    public static final String TEST_DIR = "tests";
    public static final String MAIN_FUNC_NAME = "main";
    public static final String CONFIG_URL_KEY = "url";

    public static final String CLIENT_URL_ATTR_NAME = "url";
    public static final String ACTION_PATH_ATTR_NAME = "path";

    public static boolean isSourceUnit(BLangCompilationUnit compilationUnit) {

        return !(TEST_DIR.equals(compilationUnit.getName().split("/")[0]));
    }

    public static boolean isRecordObject(BSymbol bSymbol) {

        return bSymbol.type != null && bSymbol.type.tsymbol != null
                && (bSymbol.type.tsymbol.tag & TypeTags.RECORD) == TypeTags.RECORD;
    }

    public static boolean isReturnClient(BInvokableSymbol bSymbol) {

        if (bSymbol != null && bSymbol.getReturnType() != null) {
            BType bType = isClientType(bSymbol.getReturnType());
            if (bType != null) {
                bSymbol.retType = bType;
                return true;
            }
        }
        return false;
    }

    public static boolean isMainMethod(FunctionDefinitionNode funcNode) {

        return MAIN_FUNC_NAME.equals(funcNode.functionName().text());
    }

    public static boolean isActionInvocation(BLangExpression expression) {

        return expression instanceof BLangInvocation.BLangActionInvocation;
    }

    public static boolean isURLAttrFromConfig(BLangRecordLiteral.RecordField recordField) {

        if (recordField instanceof BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
            if (recordKeyValue.key.expr instanceof BLangSimpleVarRef simpleVarRef) {
                String recordValue = simpleVarRef.variableName.value;
                return recordValue != null && recordValue.toLowerCase(Locale.ENGLISH).contains(CONFIG_URL_KEY);
            }
        }
        return false;
    }

    public static boolean isClientObject(BSymbol bSymbol) {

        if (bSymbol != null && bSymbol.type != null) {
            BType bType = isClientType(bSymbol.type);
            if (bType != null) {
                bSymbol.type = bType;
                return true;
            }
        }
        return false;
    }

    private static BType isClientType(BType type) {

        if (type instanceof BUnionType bUnionType) {
            for (BType bType : bUnionType.getMemberTypes()) {
                if (bType.tsymbol != null && SymbolKind.OBJECT.equals(bType.tsymbol.kind) &&
                        (bType.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT) {
                    return bType;
                }
            }
        } else {
            if (type.tsymbol != null && SymbolKind.OBJECT.equals(type.tsymbol.kind) &&
                    (type.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT) {
                return type;
            }
        }
        return null;
    }

    public static BLangExpression getURLExpressionFromArgs(BLangTypeInit connectorInitExpr) {

        BSymbol bSymbol = ((BLangInvocation) connectorInitExpr.initInvocation).symbol;
        if (bSymbol instanceof BInvokableSymbol bInvokableSymbol) {
            List<BVarSymbol> params = bInvokableSymbol.getParameters();
            for (int i = 0; i < params.size(); i++) {
                if (connectorInitExpr.argsExpr.size() > i &&
                        (CLIENT_URL_ATTR_NAME.equals(params.get(i).name.value.toLowerCase(Locale.ENGLISH))
                                || isRecordObject(params.get(i).type.tsymbol))) {
                    return connectorInitExpr.argsExpr.get(i);
                }
            }
        }
        return null;
    }

    public static BLangExpression getPathExpressionFromArgs(BLangInvocation actionInvocation) {

        BSymbol bSymbol = actionInvocation.symbol;
        if (bSymbol != null) {
            if (bSymbol instanceof BInvokableSymbol bInvokableSymbol) {
                List<BVarSymbol> params = bInvokableSymbol.getParameters();
                for (int i = 0; i < params.size(); i++) {
                    if (actionInvocation.argExprs.size() > i &&
                            (ACTION_PATH_ATTR_NAME.equals(params.get(i).name.value.toLowerCase(Locale.ENGLISH)))) {
                        return actionInvocation.argExprs.get(i);
                    }
                }
            }
        }
        return null;
    }

    public static void getReducedTree(Node parentNode) {

        Node currentNode = parentNode.getNextNode();
        if (currentNode == null) {
            return;
        }

        getReducedTree(currentNode);

        if (currentNode instanceof IfStatementNode ifStatementNode) {
            Node ifBody = ifStatementNode.getIfBody();
            Node elseBody = ifStatementNode.getElseBody();

            if (ifBody != null) {
                getReducedTree(ifBody);
            }

            if (elseBody != null) {
                getReducedTree(elseBody);
            }
        }

        if (currentNode instanceof IfStatementNode ifStatementNode) {
            if (ifStatementNode.getIfBody() == null && ifStatementNode.getElseBody() == null) {
                if (ifStatementNode.hasNext()) {
                    parentNode.setNextNode(ifStatementNode.getNextNode());
                } else {
                    parentNode.setNextNode(null);
                }
            }
        } else if (currentNode instanceof ForStatementNode forStatementNode) {
            if (forStatementNode.getForBody() == null) {
                if (forStatementNode.hasNext()) {
                    parentNode.setNextNode(forStatementNode.getNextNode());
                } else {
                    parentNode.setNextNode(null);
                }
            }
        }
    }
}
