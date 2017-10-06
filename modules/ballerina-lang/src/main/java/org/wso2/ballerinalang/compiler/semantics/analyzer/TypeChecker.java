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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types.RecordKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConnectorInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
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
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private Types types;
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
        this.types = Types.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env) {
        return checkExpr(expr, env, Lists.of(symTable.noType));
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes) {
        return checkExpr(expr, env, expTypes, DiagnosticCode.INCOMPATIBLE_TYPES);
    }

    /**
     * Check the given list of expressions against the given expected types.
     *
     * @param exprs   list of expressions to be analyzed
     * @param env     current symbol environment
     * @param expType expected type
     * @return the actual types of the given list of expressions
     */
    public List<BType> checkExprs(List<BLangExpression> exprs, SymbolEnv env, BType expType) {
        List<BType> resTypes = new ArrayList<>(exprs.size());
        for (BLangExpression expr : exprs) {
            resTypes.add(checkExpr(expr, env, Lists.of(expType)).get(0));
        }
        return resTypes;
    }

    public List<BType> checkExpr(BLangExpression expr, SymbolEnv env, List<BType> expTypes, DiagnosticCode diagCode) {
        // TODO Check the possibility of using a try/finally here
        SymbolEnv prevEnv = this.env;
        List<BType> preExpTypes = this.expTypes;
        DiagnosticCode preDiagCode = this.diagCode;
        this.env = env;
        this.diagCode = diagCode;
        this.expTypes = expTypes;

        expr.accept(this);

        setExprType(expr, expTypes);
        this.env = prevEnv;
        this.expTypes = preExpTypes;
        this.diagCode = preDiagCode;
        return resultTypes;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        BType literalType = symTable.getTypeFromTag(literalExpr.typeTag);
        resultTypes = types.checkTypes(literalExpr, Lists.of(literalType), expTypes);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // Check whether the expected type is an array type
        // var a = []; and var a = [1,2,3,4]; are illegal statements, because we cannot infer the type here.
        BType actualType = symTable.errType;

        int expTypeTag = expTypes.get(0).tag;
        if (expTypeTag == TypeTags.NONE) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.ARRAY_LITERAL_NOT_ALLOWED);

        } else if (expTypeTag == TypeTags.JSON || expTypeTag == TypeTags.ANY) {
            checkExprs(arrayLiteral.exprs, this.env, expTypes.get(0));
            actualType = expTypes.get(0);

        } else if (expTypeTag != TypeTags.ARRAY && expTypeTag != TypeTags.ERROR) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expTypes.get(0));

        } else if (expTypeTag != TypeTags.ERROR) {
            BArrayType arrayType = (BArrayType) expTypes.get(0);
            checkExprs(arrayLiteral.exprs, this.env, arrayType.eType);
            actualType = new BArrayType(arrayType.eType);
        }

        resultTypes = types.checkTypes(arrayLiteral, Lists.of(actualType), expTypes);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        BType actualType = symTable.errType;
        int expTypeTag = expTypes.get(0).tag;
        if (expTypeTag == TypeTags.NONE || expTypeTag == TypeTags.ANY) {
            // var a = {}
            // Change the expected type to map
            expTypes = Lists.of(symTable.mapType);
        }

        if (expTypeTag == TypeTags.JSON ||
                expTypeTag == TypeTags.MAP ||
                expTypeTag == TypeTags.STRUCT ||
                expTypeTag == TypeTags.NONE ||
                expTypeTag == TypeTags.ANY) {
            recordLiteral.keyValuePairs.forEach(keyValuePair ->
                    checkRecLiteralKeyValue(keyValuePair, expTypes.get(0)));
            actualType = expTypes.get(0);
        } else if (expTypeTag != TypeTags.ERROR) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expTypes.get(0));
        }

        resultTypes = types.checkTypes(recordLiteral, Lists.of(actualType), expTypes);
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        // Set error type as the actual type.
        BType actualType = symTable.errType;

        Name varName = names.fromIdNode(varRefExpr.variableName);
        if (varName == Names.IGNORE) {
            if (varRefExpr.lhsVar) {
                varRefExpr.type = this.symTable.noType;
            } else {
                varRefExpr.type = this.symTable.errType;
                dlog.error(varRefExpr.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
            }
            varRefExpr.symbol = new BVarSymbol(0, varName, env.enclPkg.symbol.pkgID, actualType, env.scope.owner);
            resultTypes = Lists.of(varRefExpr.type);
            return;
        }

        varRefExpr.pkgSymbol =
                symResolver.resolveImportSymbol(varRefExpr.pos, env, names.fromIdNode(varRefExpr.pkgAlias));
        if (varRefExpr.pkgSymbol == symTable.notFoundSymbol) {
            actualType = symTable.errType;
            return;
        } else if (varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
            actualType = symTable.stringType;
        } else {
            BSymbol symbol = symResolver.lookupSymbol(varRefExpr.pos, env,
                    names.fromIdNode(varRefExpr.pkgAlias), varName, SymTag.VARIABLE);
            if (symbol == symTable.notFoundSymbol) {
                dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
            } else {
                BVarSymbol varSym = (BVarSymbol) symbol;
                checkSefReferences(varRefExpr.pos, env, varSym);
                varRefExpr.symbol = varSym;
                actualType = varSym.type;
            }
        }

        // Check type compatibility
        resultTypes = types.checkTypes(varRefExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType actualType = symTable.errType;
        // First analyze the variable reference expression.
        checkExpr(fieldAccessExpr.expr, this.env, Lists.of(symTable.noType));

        BType varRefType = fieldAccessExpr.expr.type;
        Name fieldName;
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                fieldName = names.fromIdNode(fieldAccessExpr.field);
                actualType = checkStructFieldAccess(fieldAccessExpr, fieldName, varRefType);
                break;
            case TypeTags.MAP:
                actualType = ((BMapType) varRefType).getConstraint();
                break;
            case TypeTags.JSON:
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.STRUCT) {
                    fieldName = names.fromIdNode(fieldAccessExpr.field);
                    checkStructFieldAccess(fieldAccessExpr, fieldName, constraintType);
                }
                actualType = symTable.jsonType;
                break;
            case TypeTags.ERROR:
                // Do nothing
                break;
            default:
                dlog.error(fieldAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS,
                        varRefType);
        }

        resultTypes = types.checkTypes(fieldAccessExpr, Lists.of(actualType), this.expTypes);
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        BType actualType = symTable.errType;
        // First analyze the variable reference expression.
        checkExpr(indexBasedAccessExpr.expr, this.env, Lists.of(symTable.noType));

        BType indexExprType;
        BType varRefType = indexBasedAccessExpr.expr.type;
        BLangExpression indexExpr = indexBasedAccessExpr.indexExpr;
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                indexExprType = checkIndexExprForStructFieldAccess(indexExpr);
                if (indexExprType.tag == TypeTags.STRING) {
                    String fieldName = (String) ((BLangLiteral) indexExpr).value;
                    actualType = checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), varRefType);
                }
                break;
            case TypeTags.MAP:
                indexExprType = checkExpr(indexExpr, this.env,
                        Lists.of(symTable.stringType)).get(0);
                if (indexExprType.tag == TypeTags.STRING) {
                    actualType = ((BMapType) varRefType).getConstraint();
                }
                break;
            case TypeTags.JSON:
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.STRUCT) {
                    indexExprType = checkIndexExprForStructFieldAccess(indexExpr);
                    if (indexExprType.tag != TypeTags.STRING) {
                        break;
                    }
                    String fieldName = (String) ((BLangLiteral) indexExpr).value;
                    checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), constraintType);
                } else {
                    indexExprType = checkExpr(indexExpr, this.env, Lists.of(symTable.noType)).get(0);
                    if (indexExprType.tag != TypeTags.STRING && indexExprType.tag != TypeTags.INT) {
                        dlog.error(indexExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.stringType,
                                indexExprType);
                        break;
                    }
                }
                actualType = symTable.jsonType;
                break;
            case TypeTags.ARRAY:
                indexExprType = checkExpr(indexExpr, this.env,
                        Lists.of(symTable.intType)).get(0);
                if (indexExprType.tag == TypeTags.INT) {
                    actualType = ((BArrayType) varRefType).getElementType();
                }
                break;
            case TypeTags.ERROR:
                // Do nothing
                break;
            default:
                dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEXING,
                        indexBasedAccessExpr.expr.type);
        }

        resultTypes = types.checkTypes(indexBasedAccessExpr, Lists.of(actualType), this.expTypes);
    }

    public void visit(BLangInvocation iExpr) {
        // Variable ref expression null means this is the leaf node of the variable ref expression tree
        // e.g. foo();, foo(), foo().k;
        if (iExpr.expr == null) {
            // This is a function invocation expression. e.g. foo()
            checkFunctionInvocationExpr(iExpr);
            return;
        }

        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);
        if (pkgAlias != Names.EMPTY) {
            dlog.error(iExpr.pos, DiagnosticCode.PKG_ALIAS_NOT_ALLOWED_HERE);
            return;
        }

        // Find the variable reference expression type
        checkExpr(iExpr.expr, this.env, Lists.of(symTable.noType));
        switch (iExpr.expr.type.tag) {
            case TypeTags.STRUCT:
                // Invoking a function bound to a struct
                // First check whether there exist a function with this name
                // Then perform arg and param matching
                checkFunctionInvocationExpr(iExpr, (BStructType) iExpr.expr.type);
                break;
            case TypeTags.CONNECTOR:
                checkActionInvocationExpr(iExpr);
                break;
            default:
                // TODO Handle this condition
        }


        // TODO other types of invocation expressions
        //TODO pkg alias should be null or empty here.

    }

    public void visit(BLangConnectorInit cIExpr) {
        Name connectorName = names.fromIdNode(cIExpr.connectorType.getTypeName());
        BSymbol symbol = symResolver.resolveConnector(cIExpr.pos, DiagnosticCode.UNDEFINED_CONNECTOR,
                this.env, names.fromIdNode(cIExpr.connectorType.pkgAlias), connectorName);
        if (symbol == symTable.errSymbol || symbol == symTable.notFoundSymbol) {
            resultTypes = getListWithErrorTypes(expTypes.size());;
            return;
        }

        BTypeSymbol connSymbol = (BTypeSymbol) symbol;
        List<BType> paramTypes = ((BConnectorType) connSymbol.type).paramTypes;

        if (paramTypes.size() > cIExpr.argsExpr.size()) {
            dlog.error(cIExpr.pos, DiagnosticCode.NOT_ENOUGH_ARGS_FUNC_CALL, connectorName);
            return;
        } else if (paramTypes.size() < cIExpr.argsExpr.size()) {
            dlog.error(cIExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, connectorName);
            return;
        } else {
            for (int i = 0; i < cIExpr.argsExpr.size(); i++) {
                checkExpr(cIExpr.argsExpr.get(i), this.env, Lists.of(paramTypes.get(i)));
            }
        }
        checkConnectorInitTypes(cIExpr, connSymbol.type, connectorName);
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        BType expType = checkExpr(ternaryExpr.expr, env, Lists.of(this.symTable.booleanType)).get(0);
        BType thenType = checkExpr(ternaryExpr.thenExpr, env, expTypes).get(0);
        BType elseType = checkExpr(ternaryExpr.elseExpr, env, expTypes).get(0);
        if (expType == symTable.errType || thenType == symTable.errType || elseType == symTable.errType) {
            resultTypes = Lists.of(symTable.errType);
        } else if (expTypes.get(0) == symTable.noType) {
            // TODO : Fix this.
            if (thenType == elseType) {
                resultTypes = Lists.of(thenType);
            } else {
                dlog.error(ternaryExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, thenType, elseType);
                resultTypes = Lists.of(symTable.errType);
            }
        } else {
            resultTypes = expTypes;
        }
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env).get(0);
        BType rhsType = checkExpr(binaryExpr.rhsExpr, env).get(0);

        // Set error type as the actual type.
        BType actualType = symTable.errType;

        // Look up operator symbol if both rhs and lhs types are error types
        if (lhsType != symTable.errType && rhsType != symTable.errType) {
            BSymbol opSymbol = symResolver.resolveBinaryOperator(binaryExpr.opKind, lhsType, rhsType);
            if (opSymbol == symTable.notFoundSymbol) {
                dlog.error(binaryExpr.pos, DiagnosticCode.BINARY_OP_INCOMPATIBLE_TYPES,
                        binaryExpr.opKind, lhsType, rhsType);
            } else {
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
                actualType = opSymbol.type.getReturnTypes().get(0);
            }
        }

        resultTypes = types.checkTypes(binaryExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        BType exprType = checkExpr(unaryExpr.expr, env).get(0);

        BType actualType = symTable.errType;

        if (exprType != symTable.errType) {
            // Handle typeof operator separately
            if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
                List<BType> paramTypes = Lists.of(unaryExpr.expr.type);
                List<BType> retTypes = Lists.of(symTable.typeType);
                BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
                if (unaryExpr.expr.type.tag == TypeTags.ANY) {
                    BOperatorSymbol symbol = new BOperatorSymbol(names.fromString(OperatorKind.TYPEOF.value()),
                            symTable.rootPkgSymbol.pkgID, opType, symTable.rootPkgSymbol, InstructionCodes.TYPEOF);
                    unaryExpr.opSymbol = symbol;
                    actualType = symbol.type.getReturnTypes().get(0);
                } else {
                    BOperatorSymbol symbol = new BOperatorSymbol(names.fromString(OperatorKind.TYPEOF.value()),
                            symTable.rootPkgSymbol.pkgID, opType, symTable.rootPkgSymbol, InstructionCodes.TYPELOAD);
                    unaryExpr.opSymbol = symbol;
                    actualType = symbol.type.getReturnTypes().get(0);
                }
            } else {
                BSymbol symbol = symResolver.resolveUnaryOperator(unaryExpr.pos,
                        unaryExpr.operator, exprType);
                if (symbol == symTable.notFoundSymbol) {
                    dlog.error(unaryExpr.pos, DiagnosticCode.UNARY_OP_INCOMPATIBLE_TYPES,
                            unaryExpr.operator, exprType);
                } else {
                    unaryExpr.opSymbol = (BOperatorSymbol) symbol;
                    actualType = symbol.type.getReturnTypes().get(0);
                }
            }
        }

        resultTypes = types.checkTypes(unaryExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangTypeCastExpr castExpr) {
        // Set error type as the actual type.
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());

        BType targetType = symResolver.resolveTypeNode(castExpr.typeNode, env);
        BType sourceType = checkExpr(castExpr.expr, env, Lists.of(symTable.noType)).get(0);

        if (sourceType == symTable.errType || targetType == symTable.errType) {
            resultTypes = Lists.of(symTable.errType);
            return;
        }

        // Lookup type explicit cast operator symbol
        BSymbol symbol = symResolver.resolveExplicitCastOperator(sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(castExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CAST, sourceType, targetType);
        } else {
            BCastOperatorSymbol castSym = (BCastOperatorSymbol) symbol;
            castExpr.castSymbol = castSym;
            actualTypes = getActualTypesOfCastExpr(castExpr, targetType, sourceType, castSym);
        }

        resultTypes = types.checkTypes(castExpr, actualTypes, expTypes);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        // Set error type as the actual type.
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());

        BType targetType = symResolver.resolveTypeNode(conversionExpr.typeNode, env);
        BType sourceType = checkExpr(conversionExpr.expr, env, Lists.of(symTable.noType)).get(0);

        // Lookup type conversion operator symbol
        BSymbol symbol = symResolver.resolveConversionOperator(sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION, sourceType, targetType);
        } else {
            BConversionOperatorSymbol conversionSym = (BConversionOperatorSymbol) symbol;
            conversionExpr.conversionSymbol = conversionSym;
            actualTypes = getActualTypesOfConversionExpr(conversionExpr, targetType, sourceType, conversionSym);
        }

        resultTypes = types.checkTypes(conversionExpr, actualTypes, expTypes);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
    }

    public void visit(BLangXMLQName bLangXMLQName) {
        String prefix = bLangXMLQName.prefix.value;
        resultTypes = Lists.of(types.checkType(bLangXMLQName, symTable.stringType, expTypes.get(0)));
        // TODO: check isLHS

        if (env.node.getKind() == NodeKind.XML_ATTRIBUTE && prefix.isEmpty()
                && bLangXMLQName.localname.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            ((BLangXMLAttribute) env.node).isNamespaceDeclr = true;
            return;
        }

        if (env.node.getKind() == NodeKind.XML_ATTRIBUTE && prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            ((BLangXMLAttribute) env.node).isNamespaceDeclr = true;
            return;
        }

        if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
            dlog.error(bLangXMLQName.pos, DiagnosticCode.INVALID_NAMESPACE_PREFIX, prefix);
            bLangXMLQName.type = symTable.errType;
            return;
        }

        BSymbol xmlnsSymbol = symResolver.lookupSymbol(env, names.fromIdNode(bLangXMLQName.prefix), SymTag.XMLNS);
        if (prefix.isEmpty() && xmlnsSymbol == symTable.notFoundSymbol) {
            return;
        }

        if (!prefix.isEmpty() && xmlnsSymbol == symTable.notFoundSymbol) {
            dlog.error(bLangXMLQName.pos, DiagnosticCode.UNDEFINED_SYMBOL, prefix);
            bLangXMLQName.type = symTable.errType;
            return;
        }
        bLangXMLQName.namespaceURI = ((BXMLNSSymbol) xmlnsSymbol).namespaceURI;
        bLangXMLQName.nsSymbol = (BXMLNSSymbol) xmlnsSymbol;
    }

    public void visit(BLangXMLAttribute bLangXMLAttribute) {
        SymbolEnv xmlAttributeEnv = SymbolEnv.getXMLAttributeEnv(bLangXMLAttribute, env);

        // check attribute name
        checkExpr(bLangXMLAttribute.name, xmlAttributeEnv, Lists.of(symTable.stringType));

        // check attribute value
        checkExpr(bLangXMLAttribute.value, xmlAttributeEnv, Lists.of(symTable.stringType));

        symbolEnter.defineNode(bLangXMLAttribute, env);
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(bLangXMLElementLiteral, env);

        // Visit in-line namespace declarations
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                checkExpr((BLangExpression) attribute, xmlElementEnv, Lists.of(symTable.noType));
            }
        });

        // Visit attributes.
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (attribute.name.getKind() != NodeKind.XML_QNAME
                    || !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                checkExpr((BLangExpression) attribute, xmlElementEnv, Lists.of(symTable.noType));
            }
        });

        Map<Name, BXMLNSSymbol> namespaces = symResolver.resolveAllNamespaces(xmlElementEnv);
        Name defaultNs = names.fromString(XMLConstants.DEFAULT_NS_PREFIX);
        if (namespaces.containsKey(defaultNs)) {
            bLangXMLElementLiteral.defaultNsSymbol = namespaces.remove(defaultNs);
        }
        bLangXMLElementLiteral.namespacesInScope.putAll(namespaces);

        // Visit the tag names
        validateTags(bLangXMLElementLiteral, xmlElementEnv);
        
        // Visit the children
        bLangXMLElementLiteral.modifiedChildren =
                concatSimilarKindXMLNodes(bLangXMLElementLiteral.children, xmlElementEnv);
        resultTypes = Lists.of(types.checkType(bLangXMLElementLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        bLangXMLTextLiteral.concatExpr = getStringTemplateConcatExpr(bLangXMLTextLiteral.textFragments);
        resultTypes = Lists.of(types.checkType(bLangXMLTextLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        bLangXMLCommentLiteral.concatExpr = getStringTemplateConcatExpr(bLangXMLCommentLiteral.textFragments);
        resultTypes = Lists.of(types.checkType(bLangXMLCommentLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        checkExpr((BLangExpression) bLangXMLProcInsLiteral.target, env, Lists.of(symTable.stringType));
        bLangXMLProcInsLiteral.dataConcatExpr = getStringTemplateConcatExpr(bLangXMLProcInsLiteral.dataFragments);
        resultTypes = Lists.of(types.checkType(bLangXMLProcInsLiteral, symTable.xmlType, expTypes.get(0)));
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        bLangXMLQuotedString.concatExpr = getStringTemplateConcatExpr(bLangXMLQuotedString.textFragments);
        resultTypes = Lists.of(types.checkType(bLangXMLQuotedString, symTable.stringType, expTypes.get(0)));
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        BType actualType = symTable.errType;

        // First analyze the variable reference expression.
        checkExpr(xmlAttributeAccessExpr.expr, env, Lists.of(symTable.xmlType));

        // Then analyze the index expression.
        BLangExpression indexExpr = xmlAttributeAccessExpr.indexExpr;
        if (indexExpr == null) {
            if (xmlAttributeAccessExpr.lhsVar) {
                dlog.error(xmlAttributeAccessExpr.pos, DiagnosticCode.XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED);
            } else {
                actualType = symTable.xmlAttributesType;
            }
            resultTypes = types.checkTypes(xmlAttributeAccessExpr, Lists.of(actualType), expTypes);
            return;
        }

        checkExpr(indexExpr, env, Lists.of(symTable.stringType)).get(0);
        if (indexExpr.getKind() == NodeKind.XML_QNAME) {
            ((BLangXMLQName) indexExpr).isUsedInXML = true;;
        }

        if (indexExpr.type.tag == TypeTags.STRING) {
            actualType = symTable.stringType;
        }

        xmlAttributeAccessExpr.namespaces.putAll(symResolver.resolveAllNamespaces(env));
        resultTypes = types.checkTypes(xmlAttributeAccessExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.concatExpr = getStringTemplateConcatExpr(stringTemplateLiteral.exprs);
        resultTypes = Lists.of(types.checkType(stringTemplateLiteral, symTable.stringType, expTypes.get(0)));
    }

    // Private methods

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    private void setExprType(BLangExpression expr, List<BType> expTypes) {
        int expected = expTypes.size();
        int actual = resultTypes.size();
        if (expr.isMultiReturnExpr()) {
            MultiReturnExpr multiReturnExpr = (MultiReturnExpr) expr;
            multiReturnExpr.setTypes(resultTypes);
        } else {
            if (expected > 1) {
                dlog.error(expr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, actual);
                resultTypes = getListWithErrorTypes(expected);
            }
        }

        if (resultTypes.size() > 0) {
            expr.type = resultTypes.get(0);
        }
    }

    private List<BType> getListWithErrorTypes(int count) {
        List<BType> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(symTable.errType);
        }

        return list;
    }

    private List<BType> getActualTypesOfCastExpr(BLangTypeCastExpr castExpr,
                                                 BType targetType,
                                                 BType sourceType,
                                                 BCastOperatorSymbol castSymbol) {
        // If this cast is an unsafe cast, then there MUST to be two expected types/variables
        // If this is an safe cast, then the error variable is optional
        int expected = expTypes.size();
        List<BType> actualTypes = getListWithErrorTypes(expected);
        if (castSymbol.safe && expected == 1) {
            actualTypes = Lists.of(castSymbol.type.getReturnTypes().get(0));

        } else if (!castSymbol.safe && expected == 1) {
            dlog.error(castExpr.pos, DiagnosticCode.UNSAFE_CAST_ATTEMPT, sourceType, targetType);

        } else if (expected == 2) {
            actualTypes = castSymbol.type.getReturnTypes();

        } else if (expected == 0 || expected > 2) {
            dlog.error(castExpr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, 2);
        }

        return actualTypes;
    }

    private List<BType> getActualTypesOfConversionExpr(BLangTypeConversionExpr castExpr,
                                                       BType targetType,
                                                       BType sourceType,
                                                       BConversionOperatorSymbol conversionSymbol) {
        // If this cast is an unsafe conversion, then there MUST to be two expected types/variables
        // If this is an safe cast, then the error variable is optional
        int expected = expTypes.size();
        List<BType> actualTypes = getListWithErrorTypes(expected);
        if (conversionSymbol.safe && expected == 1) {
            actualTypes = Lists.of(conversionSymbol.type.getReturnTypes().get(0));
        } else if (!conversionSymbol.safe && expected == 1) {
            dlog.error(castExpr.pos, DiagnosticCode.UNSAFE_CONVERSION_ATTEMPT, sourceType, targetType);

        } else if (expected == 2) {
            actualTypes = conversionSymbol.type.getReturnTypes();

        } else if (expected == 0 || expected > 2) {
            dlog.error(castExpr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, 2);
        }

        return actualTypes;
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr) {
        Name funcName = names.fromIdNode(iExpr.name);
        BSymbol funcSymbol = symResolver.resolveFunction(iExpr.pos, this.env,
                names.fromIdNode(iExpr.pkgAlias), funcName);
        if (funcSymbol == symTable.notFoundSymbol) {
            // Check for function pointer.
            Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);
            BSymbol functionPointer = symResolver.lookupSymbol(iExpr.pos, env, pkgAlias, funcName, SymTag.VARIABLE);
            if (functionPointer.type.tag != TypeTags.INVOKABLE) {
                dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, funcName);
                resultTypes = getListWithErrorTypes(expTypes.size());
                return;
            }
            iExpr.functionPointerInvocation = true;
            funcSymbol = functionPointer;
        }

        // Set the resolved function symbol in the invocation expression.
        // This is used in the code generation phase.
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr, BStructType structType) {
        Name funcName = getFuncSymbolName(iExpr, structType);
        BPackageSymbol packageSymbol = (BPackageSymbol) structType.tsymbol.owner;
        BSymbol funcSymbol = symResolver.lookupMemberSymbol(iExpr.pos, packageSymbol.scope, this.env,
                funcName, SymTag.FUNCTION);
        if (funcSymbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION_IN_STRUCT, iExpr.name.value, structType);
            resultTypes = getListWithErrorTypes(expTypes.size());
            return;
        }

        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkInvocationParamAndReturnType(BLangInvocation iExpr) {
        BSymbol funcSymbol = iExpr.symbol;
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());
        List<BType> paramTypes = ((BInvokableType) funcSymbol.type).getParameterTypes();
        if (iExpr.argExprs.size() == 1 && iExpr.argExprs.get(0).getKind() == NodeKind.INVOCATION) {
            checkExpr(iExpr.argExprs.get(0), this.env, paramTypes);

        } else if (paramTypes.size() > iExpr.argExprs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.NOT_ENOUGH_ARGS_FUNC_CALL, iExpr.name.value);

        } else if (paramTypes.size() < iExpr.argExprs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);

        } else {
            for (int i = 0; i < iExpr.argExprs.size(); i++) {
                checkExpr(iExpr.argExprs.get(i), this.env, Lists.of(paramTypes.get(i)));
            }
            actualTypes = funcSymbol.type.getReturnTypes();
        }

        checkInvocationReturnTypes(iExpr, actualTypes);
    }

    private void checkActionInvocationExpr(BLangInvocation iExpr) {
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) iExpr.expr;
        Name varName = names.fromIdNode(varRef.variableName);
        Name pkgAlias = names.fromIdNode(varRef.pkgAlias);
        BSymbol symbol = symResolver.lookupSymbol(iExpr.pos, env, pkgAlias, varName, SymTag.VARIABLE);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.value);
            resultTypes = getListWithErrorTypes(expTypes.size());;
            return;
        }
        iExpr.expr.symbol = (BVarSymbol) symbol;

        Name actionName = names.fromIdNode(iExpr.name);
        BSymbol actionSym = symResolver.lookupMemberSymbol(iExpr.pos, symbol.type.tsymbol.scope,
                env, actionName, SymTag.ACTION);
        if (actionSym == symTable.errSymbol || actionSym == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_ACTION, actionName.value);
            resultTypes = actualTypes;
            return;
        }

        iExpr.symbol = actionSym;

        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkInvocationReturnTypes(BLangInvocation iExpr, List<BType> actualTypes) {
        List<BType> newActualTypes = actualTypes;
        List<BType> newExpTypes = this.expTypes;
        int expected = this.expTypes.size();
        int actual = actualTypes.size();
        if (expected == 1 && actual > 1) {
            dlog.error(iExpr.pos, DiagnosticCode.MULTI_VAL_IN_SINGLE_VAL_CONTEXT, iExpr.name.value);
            newActualTypes = getListWithErrorTypes(expected);
        } else if (expected == 0) {
            // This could be from a expression statement. e.g foo();
            if (this.env.node.getKind() != NodeKind.EXPRESSION_STATEMENT) {
                dlog.error(iExpr.pos, DiagnosticCode.DOES_NOT_RETURN_VALUE, iExpr.name.value);
            }
            newExpTypes = newActualTypes;
        } else if (expected != actual) {
            // Special case actual == 0 scenario.. VOID Function
            dlog.error(iExpr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, actual);
            newActualTypes = getListWithErrorTypes(expected);
        }

        resultTypes = types.checkTypes(iExpr, newActualTypes, newExpTypes);
    }

    private Name getFuncSymbolName(BLangInvocation iExpr, BStructType structType) {
        return names.fromString(structType + Names.DOT.value + iExpr.name);
    }

    private void checkConnectorInitTypes(BLangConnectorInit iExpr, BType actualType, Name connName) {
        int expected = expTypes.size();
        if (expTypes.size() > 1) {
            dlog.error(iExpr.pos, DiagnosticCode.MULTI_VAL_IN_SINGLE_VAL_CONTEXT, connName);
            resultTypes = getListWithErrorTypes(expected);
            return;
        }
        resultTypes = types.checkTypes(iExpr, Lists.of(actualType), expTypes);
    }

    private void checkRecLiteralKeyValue(BLangRecordKeyValue keyValuePair, BType recType) {
        BType fieldType = symTable.errType;
        BLangExpression valueExpr = keyValuePair.valueExpr;
        switch (recType.tag) {
            case TypeTags.STRUCT:
                fieldType = checkStructLiteralKeyExpr(keyValuePair.key, recType, RecordKind.STRUCT);
                break;
            case TypeTags.MAP:
                fieldType = checkMapLiteralKeyExpr(keyValuePair.key.expr, recType, RecordKind.STRUCT);
                break;
            case TypeTags.JSON:
                fieldType = checkJSONLiteralKeyExpr(keyValuePair.key, recType, RecordKind.STRUCT);

                // First visit the expression having field type, as the expected type.
                checkExpr(valueExpr, this.env, Lists.of(fieldType)).get(0);

                // Again check the type compatibility with JSON
                if (valueExpr.impCastExpr == null) {
                    types.checkTypes(valueExpr, Lists.of(valueExpr.type), Lists.of(symTable.jsonType));
                } else {
                    BType valueType = valueExpr.type;
                    types.checkTypes(valueExpr, valueExpr.impCastExpr.types, Lists.of(symTable.jsonType));
                    valueExpr.type = valueType;
                }
                resultTypes = Lists.of(valueExpr.type);
                return;
        }

        checkExpr(valueExpr, this.env, Lists.of(fieldType));
    }

    private BType checkStructLiteralKeyExpr(BLangRecordKey key, BType recordType, RecordKind recKind) {
        Name fieldName;
        BLangExpression keyExpr = key.expr;

        if (checkRecLiteralKeyExpr(keyExpr, recKind).tag != TypeTags.STRING) {
            return symTable.errType;

        } else if (keyExpr.getKind() == NodeKind.STRING_TEMPLATE_LITERAL) {
            // keys of the struct literal can only be string literals and identifiers
            dlog.error(keyExpr.pos, DiagnosticCode.STRING_TEMPLATE_LIT_NOT_ALLOWED);
            return symTable.errType;

        } else if (keyExpr.getKind() == NodeKind.LITERAL) {
            Object literalValue = ((BLangLiteral) keyExpr).value;
            fieldName = names.fromString((String) literalValue);

        } else {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        }

        // Check weather the struct field exists
        BSymbol fieldSymbol = symResolver.resolveStructField(keyExpr.pos, this.env,
                fieldName, recordType.tsymbol);
        if (fieldSymbol == symTable.notFoundSymbol) {
            return symTable.errType;
        }

        // Setting the struct field symbol for future use in Desugar and code generator.
        key.fieldSymbol = (BVarSymbol) fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkJSONLiteralKeyExpr(BLangRecordKey key, BType recordType, RecordKind recKind) {
        BJSONType type = (BJSONType) recordType;
        
        // If the JSON is constrained with a struct, get the field type from the struct
        if (type.constraint.tag != TypeTags.NONE && type.constraint.tag != TypeTags.ERROR) {
            return checkStructLiteralKeyExpr(key, type.constraint, recKind);
        }

        if (checkRecLiteralKeyExpr(key.expr, recKind).tag != TypeTags.STRING) {
            return symTable.errType;
        }

        // If the JSON is not constrained, field type is always JSON.
        return symTable.jsonType;
    }

    private BType checkMapLiteralKeyExpr(BLangExpression keyExpr, BType recordType, RecordKind recKind) {
        if (checkRecLiteralKeyExpr(keyExpr, recKind).tag != TypeTags.STRING) {
            return symTable.errType;
        }

        // TODO constrained map
        return symTable.anyType;
    }

    private BType checkRecLiteralKeyExpr(BLangExpression keyExpr, RecordKind recKind) {
        // keys of the record literal can only be string literals, identifiers or string template literals
        if (keyExpr.getKind() != NodeKind.LITERAL &&
                keyExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                keyExpr.getKind() != NodeKind.STRING_TEMPLATE_LITERAL) {
            dlog.error(keyExpr.pos, DiagnosticCode.INVALID_FIELD_NAME_RECORD_LITERAL, recKind.value);
            return symTable.errType;

        } else if (keyExpr.getKind() == NodeKind.LITERAL ||
                keyExpr.getKind() == NodeKind.STRING_TEMPLATE_LITERAL) {
            return checkExpr(keyExpr, this.env, Lists.of(symTable.stringType)).get(0);
        }

        // If the key expression is an identifier then we simply set the type as string.
        keyExpr.type = symTable.stringType;
        return keyExpr.type;
    }

    private BType checkIndexExprForStructFieldAccess(BLangExpression indexExpr) {
        if (indexExpr.getKind() != NodeKind.LITERAL) {
            dlog.error(indexExpr.pos, DiagnosticCode.INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS);
            return symTable.errType;
        }

        return checkExpr(indexExpr, this.env, Lists.of(symTable.stringType)).get(0);
    }

    private BType checkStructFieldAccess(BLangVariableReference varReferExpr, Name fieldName, BType structType) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, this.env,
                fieldName, structType.tsymbol);
        if (fieldSymbol == symTable.notFoundSymbol) {
            return symTable.errType;
        }

        // Setting the field symbol. This is used during the code generation phase
        varReferExpr.symbol = (BVarSymbol) fieldSymbol;
        return fieldSymbol.type;
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

        if (startTagName.getKind() == NodeKind.XML_QNAME && startTagName.getKind() == NodeKind.XML_QNAME
                && startTagName.equals(endTagName)) {
            return;
        }

        if (startTagName.getKind() != NodeKind.XML_QNAME && startTagName.getKind() != NodeKind.XML_QNAME) {
            return;
        }

        dlog.error(startTagName.pos, DiagnosticCode.XML_TAGS_MISMATCH);
    }

    private BLangExpression getStringTemplateConcatExpr(List<BLangExpression> exprs) {
        BLangExpression concatExpr = null;
        for (BLangExpression expr : exprs) {
            checkExpr((BLangExpression) expr, env);
            if (concatExpr == null) {
                concatExpr = expr;
                continue;
            }

            BSymbol opSymbol = symResolver.resolveBinaryOperator(OperatorKind.ADD, symTable.stringType, expr.type);
            if (opSymbol == symTable.notFoundSymbol && expr.type != symTable.errType) {
                dlog.error(expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.stringType, expr.type);
            }

            concatExpr = getBinaryAddExppression(concatExpr, expr, opSymbol);
        }

        return concatExpr;
    }

    /**
     * Concatenate the consecutive text type nodes, and get the reduced set of children.
     * 
     * @param exprs Child nodes
     * @param xmlElementEnv 
     * @return Reduced set of children
     */
    private List<BLangExpression> concatSimilarKindXMLNodes(List<BLangExpression> exprs, SymbolEnv xmlElementEnv) {
        List<BLangExpression> newChildren = new ArrayList<BLangExpression>();
        BLangExpression strConcatExpr = null;

        for (BLangExpression expr : exprs) {
            BType exprType = checkExpr((BLangExpression) expr, xmlElementEnv).get(0);
            if (exprType == symTable.xmlType) {
                if (strConcatExpr != null) {
                    newChildren.add(getXMLTextLiteral(strConcatExpr));
                    strConcatExpr = null;
                }
                newChildren.add(expr);
                continue;
            }

            BSymbol opSymbol = symResolver.resolveBinaryOperator(OperatorKind.ADD, symTable.stringType, exprType);
            if (opSymbol == symTable.notFoundSymbol && exprType != symTable.errType) {
                dlog.error(expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.xmlType, exprType);
            }

            if (strConcatExpr == null) {
                strConcatExpr = expr;
                continue;
            }
            strConcatExpr = getBinaryAddExppression(strConcatExpr, expr, opSymbol);
        }

        // Add remaining concatenated text nodes as children
        if (strConcatExpr != null) {
            newChildren.add(getXMLTextLiteral(strConcatExpr));
        }

        return newChildren;
    }

    private BLangExpression getBinaryAddExppression(BLangExpression lExpr, BLangExpression rExpr, BSymbol opSymbol) {
        BLangBinaryExpr binaryExpressionNode = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpressionNode.lhsExpr = lExpr;
        binaryExpressionNode.rhsExpr = rExpr;
        binaryExpressionNode.pos = rExpr.pos;
        binaryExpressionNode.opKind = OperatorKind.ADD;
        if (opSymbol != symTable.notFoundSymbol) {
            binaryExpressionNode.type = opSymbol.type.getReturnTypes().get(0);
            binaryExpressionNode.opSymbol = (BOperatorSymbol) opSymbol;
        } else {
            binaryExpressionNode.type = symTable.errType;
        }

        types.checkType(binaryExpressionNode, binaryExpressionNode.type, symTable.stringType);
        return binaryExpressionNode;
    }

    private BLangExpression getXMLTextLiteral(BLangExpression contentExpr) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.concatExpr = contentExpr;
        xmlTextLiteral.pos = contentExpr.pos;
        return xmlTextLiteral;
    }
}
