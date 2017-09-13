/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.tree.expressions.XMLAttributeNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.MultiReturnExpr;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;

/**
 * @since 0.94
 */
public class TypeChecker extends BLangNodeVisitor {

    private static final CompilerContext.Key<TypeChecker> TYPE_CHECKER_KEY =
            new CompilerContext.Key<>();

    private Names names;
    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private SymbolResolver symResolver;
    private DiagnosticLog dlog;

    private SymbolEnv env;

    /**
     * Expected types or inherited types
     */
    private List<BType> expTypes;

    private DiagnosticCode diagCode;
    private List<BType> resultTypes;


    public static TypeChecker getInstance(CompilerContext context) {
        TypeChecker typeChecker = context.get(TYPE_CHECKER_KEY);
        if (typeChecker == null) {
            typeChecker = new TypeChecker(context);
        }

        return typeChecker;
    }

    public TypeChecker(CompilerContext context) {
        context.put(TYPE_CHECKER_KEY, this);

        this.names = Names.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env) {
        return checkExpr(expr, env, Lists.of(symTable.noType));
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes) {
        return checkExpr(expr, env, expTypes, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes, DiagnosticCode diagCode) {
        checkAssignmentMismatch(expr, expTypes);

        SymbolEnv prevEnv = this.env;
        List<BType> preExpTypes = this.expTypes;
        DiagnosticCode preDiagCode = this.diagCode;

        // TODO Check the possibility of using a try/finally here
        this.env = env;
        this.expTypes = expTypes;
        this.diagCode = diagCode;
        expr.accept(this);
        this.env = prevEnv;
        this.expTypes = preExpTypes;
        this.diagCode = preDiagCode;

        assignExprType(expr);
        return resultTypes;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        resultTypes = Lists.of(checkType(literalExpr,
                symTable.getTypeFromTag(literalExpr.typeTag),
                expTypes.get(0), diagCode));
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        Name varName = names.fromIdNode(varRefExpr.variableName);
        BSymbol symbol = symResolver.lookupSymbol(env, varName, SymTag.VARIABLE);

        if (symbol == symTable.notFoundSymbol) {
            dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
            varRefExpr.type = symTable.errType;
            return;
        }

        BVarSymbol varSym = (BVarSymbol) symbol;
        checkSefReferences(varRefExpr.pos, env, varSym);

        // Check type compatibility
        resultTypes = Lists.of(checkType(varRefExpr, varSym.type, expTypes.get(0)));
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        throw new AssertionError();
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env).get(0);
        BType rhsType = checkExpr(binaryExpr.rhsExpr, env).get(0);

        // Look up operator symbol
        BSymbol symbol = symResolver.resolveBinaryOperator(binaryExpr.pos,
                binaryExpr.opKind, lhsType, rhsType);
        if (symbol == symTable.notFoundSymbol) {
            binaryExpr.type = symTable.errType;
            return;
        }

        // type check return type with the exp type
        BOperatorSymbol opSymbol = (BOperatorSymbol) symbol;
        resultTypes = Lists.of(checkType(binaryExpr, opSymbol.type.getReturnTypes().get(0), expTypes.get(0)));
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        throw new AssertionError();
    }

    public void visit(BLangTypeCastExpr castExpr) {
        BType targetType = symResolver.resolveTypeNode(castExpr.typeNode, env);
        BType sourceType = checkExpr(castExpr.expr, env, Lists.of(symTable.noType)).get(0);

        // Lookup type explicit cast operator symbol
        BSymbol symbol = symResolver.resolveExplicitCastOperator(castExpr.pos, sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            castExpr.type = symTable.errType;
            return;
        }

        BCastOperatorSymbol castSym = (BCastOperatorSymbol) symbol;
        // If this cast is an unsafe cast, then there MUST to be two l variables
        // resulting two expected types
        // If this is an safe cast, then the error variable is optional
        if (!castSym.safe && expTypes.size() < 2) {
            dlog.error(castExpr.pos, DiagnosticCode.UNSAFE_CAST_ATTEMPT, sourceType, targetType);
        } else if (castSym.safe && expTypes.size() < 2) {
            expTypes.add(symTable.errStructType);
        }

        resultTypes = checkTypes(castExpr, castSym.type.getReturnTypes(), expTypes);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        throw new AssertionError();
    }

    public void visit(BLangXMLQName bLangXMLQName) {
        String prefix = bLangXMLQName.prefix.value;
        resultTypes = Lists.of(checkType(bLangXMLQName, symTable.stringType, expTypes.get(0)));
        // TODO: check isLHS
        
        if (env.node instanceof XMLAttributeNode && prefix.isEmpty()
                && bLangXMLQName.localname.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            ((BLangXMLAttribute) env.node).isDefaultNs = true;
            return;
        }
        
        if (prefix.isEmpty()) {
            return;
        }
        
        if (env.node instanceof XMLAttributeNode && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            ((BLangXMLAttribute) env.node).isNamespaceDeclr = true;
            return;
        }
        
        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            dlog.error(bLangXMLQName.pos, DiagnosticCode.INVALID_NAMESPACE_PREFIX, prefix);
            bLangXMLQName.type = symTable.errType;
            return;
        }

        BSymbol xmlnsSymbol = symResolver.lookupSymbol(env, names.fromIdNode(bLangXMLQName.prefix), SymTag.XMLNS);
        if (xmlnsSymbol == symTable.notFoundSymbol) {
            dlog.error(bLangXMLQName.pos, DiagnosticCode.UNDEFINED_SYMBOL, prefix);
            bLangXMLQName.type = symTable.errType;
            return;
        }

        bLangXMLQName.namespaceUri = ((BXMLNSSymbol) xmlnsSymbol).namespaceUri;
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(bLangXMLAttribute, env);

        // check attribute name
        checkExpr((BLangExpression) bLangXMLAttribute.name, xmlAttributeEnv, Lists.of(symTable.stringType));

        // check attribute value
        checkExpr((BLangExpression) bLangXMLAttribute.value, xmlAttributeEnv, Lists.of(symTable.stringType));

        if (bLangXMLAttribute.isNamespaceDeclr) {
            BLangXMLQName attrQName = (BLangXMLQName) bLangXMLAttribute.name;

            BXMLNSSymbol xmlnsSymbol = Symbols.createXMLNSSymbol(names.fromIdNode(attrQName.localname),
                    (BLangExpression) bLangXMLAttribute.value, env.scope.owner);
            symbolEnter.defineSymbol(attrQName.pos, xmlnsSymbol, env);
        } else if (bLangXMLAttribute.isDefaultNs) {
            ((BLangXMLElementLiteral) env.node).defaultNamespaceUri = bLangXMLAttribute.value;
        }
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(bLangXMLElementLiteral, env);

        if (bLangXMLElementLiteral.isRoot) {
            addNamespacesInScope(bLangXMLElementLiteral, xmlElementEnv);
        }

        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            checkExpr((BLangExpression) attribute, xmlElementEnv, Lists.of(symTable.noType));
        });

        // set the default namespace
        if (bLangXMLElementLiteral.defaultNamespaceUri == null && bLangXMLElementLiteral.isRoot) {
            BSymbol defaultNsSymbol =
                    symResolver.lookupSymbol(env, names.fromString(XMLConstants.DEFAULT_NS_PREFIX), SymTag.XMLNS);
            if (defaultNsSymbol != symTable.notFoundSymbol) {
                bLangXMLElementLiteral.defaultNamespaceUri = ((BXMLNSSymbol) defaultNsSymbol).namespaceUri;
            }
        } else if (bLangXMLElementLiteral.defaultNamespaceUri == null && !bLangXMLElementLiteral.isRoot) {
            bLangXMLElementLiteral.defaultNamespaceUri = ((BLangXMLElementLiteral) env.node).defaultNamespaceUri;
        }

        validateTags(bLangXMLElementLiteral, xmlElementEnv);

        bLangXMLElementLiteral.children.forEach(child -> {
            // TODO: expType type can be either XML or string
            checkExpr((BLangExpression) child, xmlElementEnv, Lists.of(symTable.xmlType));
        });

        resultTypes = Lists.of(checkType(bLangXMLElementLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        bLangXMLTextLiteral.textFragments.forEach(expr -> {
            checkExpr((BLangExpression) expr, env, Lists.of(symTable.stringType));
        });
        resultTypes = Lists.of(checkType(bLangXMLTextLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        bLangXMLCommentLiteral.textFragments.forEach(expr -> {
            checkExpr((BLangExpression) expr, env, Lists.of(symTable.stringType));
        });
        resultTypes = Lists.of(checkType(bLangXMLCommentLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        checkExpr((BLangExpression) bLangXMLProcInsLiteral.target, env, Lists.of(symTable.stringType));
        bLangXMLProcInsLiteral.dataFragments.forEach(expr -> {
            checkExpr((BLangExpression) expr, env, Lists.of(symTable.stringType));
        });
        resultTypes = Lists.of(checkType(bLangXMLProcInsLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        bLangXMLQuotedString.textFragments.forEach(expr -> {
            checkExpr((BLangExpression) expr, env, Lists.of(symTable.stringType));
        });
        resultTypes = Lists.of(checkType(bLangXMLQuotedString, symTable.stringType, expTypes.get(0)));
    }

    // Private methods

    private List<BType> checkTypes(BLangExpression node, List<BType> types, List<BType> expTypes) {
        checkAssignmentMismatch(node, expTypes);
        if (types.size() != expTypes.size()) {
            dlog.error(node.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expTypes.size(), types.size());
        }

        List<BType> resTypes = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            resTypes.add(checkType(node, types.get(i), expTypes.get(i)));
        }

        if (node.isMultiReturnExpr()) {
            MultiReturnExpr multiReturnExpr = (MultiReturnExpr) node;
            multiReturnExpr.setTypes(resTypes);
        }

        return resTypes;
    }

    private BType checkType(BLangExpression node, BType type, BType expType) {
        return checkType(node, type, expType, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    private BType checkType(BLangExpression node, BType type, BType expType, DiagnosticCode diagCode) {
        node.type = type;
        if (expType.tag == TypeTags.ERROR) {
            return expType;
        } else if (expType.tag == TypeTags.NONE) {
            return type;
        } else if (type.tag == expType.tag) {
            return type;
        }

        // TODO Add more logic to check type compatibility assignability etc.

        // e.g. incompatible types: expected 'int', found 'string'
        dlog.error(node.pos, diagCode, expType, type);
        return node.type = symTable.errType;
    }

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    private void assignExprType(BLangExpression expr) {
        if (!expr.isMultiReturnExpr()) {
            expr.type = resultTypes.get(0);
        }  // TODO support multi return exprs
    }

    private void checkAssignmentMismatch(BLangExpression expr, List<BType> expTypes) {
        if (!expr.isMultiReturnExpr() && expTypes.size() > 1) {
            dlog.error(expr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expTypes.size(), 1);

            // Fill the result type with the error type
            resultTypes = new ArrayList<>(expTypes.size());
            for (int i = 0; i < expTypes.size(); i++) {
                resultTypes.add(symTable.errType);
            }
        }
    }

    private void addNamespacesInScope(BLangXMLElementLiteral bLangXMLElementLiteral, SymbolEnv env) {
        if (env == null) {
            return;
        }

        env.scope.entries.forEach((name, scopeEntry) -> {
            if (scopeEntry.symbol instanceof BXMLNSSymbol) {
                BXMLNSSymbol nsSymbol = (BXMLNSSymbol) scopeEntry.symbol;
                bLangXMLElementLiteral.namespaces.put(nsSymbol.name.value, nsSymbol.namespaceUri);
            }
        });
        addNamespacesInScope(bLangXMLElementLiteral, env.enclEnv);
    }

    private void validateTags(BLangXMLElementLiteral bLangXMLElementLiteral, SymbolEnv xmlElementEnv) {
        // check type for start and end tags
        BLangExpression startTagName = (BLangExpression) bLangXMLElementLiteral.startTagName;
        checkExpr(startTagName, xmlElementEnv, Lists.of(symTable.stringType));
        BLangExpression endTagName = (BLangExpression) bLangXMLElementLiteral.endTagName;
        if (endTagName != null) {
            checkExpr(endTagName, xmlElementEnv, Lists.of(symTable.stringType));
        }

        if (endTagName == null) {
            return;
        }

        if (startTagName instanceof BLangXMLQName && startTagName instanceof BLangXMLQName
                && startTagName.equals(endTagName)) {
            return;
        }

        if (!(startTagName instanceof BLangXMLQName) && !(startTagName instanceof BLangXMLQName)) {
            return;
        }

        dlog.error(startTagName.pos, DiagnosticCode.XML_TAGS_MISMATCH);
    }
}
