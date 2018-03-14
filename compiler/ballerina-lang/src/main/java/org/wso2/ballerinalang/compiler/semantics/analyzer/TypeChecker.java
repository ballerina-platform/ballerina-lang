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
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.types.Type;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types.RecordKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BStructSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTransformerSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangStreamlet;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeofExpr;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
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
    private IterableAnalyzer iterableAnalyzer;
    private BLangDiagnosticLog dlog;

    private SymbolEnv env;

    /**
     * Expected types or inherited types.
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
        this.iterableAnalyzer = IterableAnalyzer.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
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
                expTypeTag == TypeTags.TABLE ||
                expTypeTag == TypeTags.NONE ||
                expTypeTag == TypeTags.STREAM ||
                expTypeTag == TypeTags.STREAMLET ||
                expTypeTag == TypeTags.ANY) {
            recordLiteral.keyValuePairs.forEach(keyValuePair ->
                    checkRecLiteralKeyValue(keyValuePair, expTypes.get(0)));
            actualType = expTypes.get(0);

            // TODO Following check can be moved the code analyzer.
            if (expTypeTag == TypeTags.STRUCT) {
                validateStructInitalizer(recordLiteral.pos);
            }
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

        varRefExpr.pkgSymbol = symResolver.resolveImportSymbol(varRefExpr.pos,
                env, names.fromIdNode(varRefExpr.pkgAlias));
        if (varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
            actualType = symTable.stringType;
        } else if (varRefExpr.pkgSymbol != symTable.notFoundSymbol) {
            BSymbol symbol = symResolver.lookupSymbolInPackage(varRefExpr.pos, env,
                    names.fromIdNode(varRefExpr.pkgAlias), varName, SymTag.VARIABLE_NAME);
            if ((symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) {
                BVarSymbol varSym = (BVarSymbol) symbol;
                checkSefReferences(varRefExpr.pos, env, varSym);
                varRefExpr.symbol = varSym;
                actualType = varSym.type;
            } else {
                dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
            }
        }

        // Check type compatibility
        resultTypes = types.checkTypes(varRefExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // First analyze the variable reference expression.
        BType actualType = symTable.errType;
        BType varRefType = getTypeOfExprInFieldAccess(fieldAccessExpr.expr);
        Name fieldName = names.fromIdNode(fieldAccessExpr.field);
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                actualType = checkStructFieldAccess(fieldAccessExpr, fieldName, varRefType);
                break;
            case TypeTags.MAP:
                actualType = ((BMapType) varRefType).getConstraint();
                break;
            case TypeTags.JSON:
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.STRUCT) {
                    BType fieldType = checkStructFieldAccess(fieldAccessExpr, fieldName, constraintType);

                    // If the type of the field is struct, treat it as constraint JSON type.
                    if (fieldType.tag == TypeTags.STRUCT) {
                        actualType = new BJSONType(TypeTags.JSON, fieldType, symTable.jsonType.tsymbol);
                        break;
                    }
                }
                actualType = symTable.jsonType;
                break;
            case TypeTags.ENUM:
                // Enumerator access expressions only allow enum type name as the first part e.g state.INSTALLED,
                BEnumType enumType = (BEnumType) varRefType;
                if (fieldAccessExpr.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF ||
                        !((BLangSimpleVarRef) fieldAccessExpr.expr).variableName.value.equals(
                                enumType.tsymbol.name.value)) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.INVALID_ENUM_EXPR, enumType.tsymbol.name.value);
                    break;
                }

                BSymbol symbol = symResolver.lookupMemberSymbol(fieldAccessExpr.pos,
                        enumType.tsymbol.scope, this.env, fieldName, SymTag.VARIABLE);
                if (symbol == symTable.notFoundSymbol) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, fieldName.value);
                    break;
                }
                fieldAccessExpr.symbol = (BVarSymbol) symbol;
                actualType = fieldAccessExpr.expr.type;

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
                    BType fieldType =
                            checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), constraintType);

                    // If the type of the field is struct, treat it as constraint JSON type.
                    if (fieldType.tag == TypeTags.STRUCT) {
                        actualType = new BJSONType(TypeTags.JSON, fieldType, symTable.jsonType.tsymbol);
                        break;
                    }
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
            case TypeTags.XML:
                if (indexBasedAccessExpr.lhsVar) {
                    dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
                    break;
                }
                indexExprType = checkExpr(indexExpr, this.env, Lists.of(symTable.intType)).get(0);
                if (indexExprType.tag == TypeTags.INT) {
                    actualType = symTable.xmlType;
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
        final List<BType> exprTypes = checkExpr(iExpr.expr, this.env, Lists.of(symTable.noType));
        if (isIterableOperationInvocation(iExpr)) {
            iExpr.iterableOperationInvocation = true;
            iterableAnalyzer.handlerIterableOperation(iExpr, expTypes, env);
            resultTypes = iExpr.iContext.operations.getLast().resultTypes;
            return;
        }
        if (iExpr.actionInvocation) {
            if (exprTypes.size() != 1) {
                dlog.error(iExpr.expr.pos, DiagnosticCode.SINGLE_VALUE_RETURN_EXPECTED);
            }
            checkActionInvocationExpr(iExpr, exprTypes.size() > 0 ? exprTypes.get(0) : symTable.errType);
            return;
        }
        switch (iExpr.expr.type.tag) {
            case TypeTags.STRUCT:
                // Invoking a function bound to a struct
                // First check whether there exist a function with this name
                // Then perform arg and param matching
                checkFunctionInvocationExpr(iExpr, (BStructType) iExpr.expr.type);
                break;
            case TypeTags.CONNECTOR:
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION_SYNTAX);
                resultTypes = getListWithErrorTypes(expTypes.size());
                return;
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.BLOB:
            case TypeTags.XML:
            case TypeTags.MAP:
                checkFunctionInvocationExpr(iExpr, iExpr.expr.type);
                break;
            case TypeTags.JSON:
                checkFunctionInvocationExpr(iExpr, symTable.jsonType);
                break;
            case TypeTags.TABLE:
                checkFunctionInvocationExpr(iExpr, symTable.tableType);
                break;
            case TypeTags.STREAM:
                checkFunctionInvocationExpr(iExpr, symTable.streamType);
                break;
            case TypeTags.STREAMLET:
                checkFunctionInvocationExpr(iExpr, symTable.streamletType);
                break;
            case TypeTags.ARRAY:
            case TypeTags.TUPLE_COLLECTION:
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_INVOCATION, iExpr.expr.type);
                break;
            case TypeTags.NONE:
                dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, iExpr.name);
                break;
            default:
                // TODO Handle this condition
        }

        // TODO other types of invocation expressions
        //TODO pkg alias should be null or empty here.

    }

    public void visit(BLangTypeInit cIExpr) {
        Name connectorName = names.fromIdNode(cIExpr.userDefinedType.getTypeName());
        BSymbol symbol = symResolver.resolveConnector(cIExpr.pos, DiagnosticCode.UNDEFINED_CONNECTOR,
                this.env, names.fromIdNode(cIExpr.userDefinedType.pkgAlias), connectorName);
        if (symbol == symTable.errSymbol || symbol == symTable.notFoundSymbol) {
            resultTypes = getListWithErrorTypes(expTypes.size());
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

    public void visit(BLangTypeofExpr accessExpr) {
        BType actualType = symTable.typeType;
        accessExpr.resolvedType = symResolver.resolveTypeNode(accessExpr.typeNode, env);
        resultTypes = types.checkTypes(accessExpr, Lists.of(actualType), expTypes);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        BType exprType = null;
        BType actualType = symTable.errType;
        if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            // Handle typeof operator separately
            if (unaryExpr.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) unaryExpr.expr;
                Name varRefName = names.fromIdNode((varRef).variableName);
                Name pkgAlias = names.fromIdNode((varRef).pkgAlias);
                // Resolve symbol for BLangSimpleVarRef
                BSymbol varRefSybmol = symResolver.lookupSymbolInPackage(unaryExpr.pos, env, pkgAlias,
                        varRefName, SymTag.VARIABLE);
                if (varRefSybmol == symTable.notFoundSymbol) {
                    // Resolve symbol for User Defined Type ( converted from BLangSimpleVarRef )
                    BLangTypeofExpr typeAccessExpr = getTypeAccessExpression(varRef);
                    unaryExpr.expr = typeAccessExpr;
                    actualType = typeAccessExpr.type;
                    resultTypes = types.checkTypes(unaryExpr, Lists.of(actualType), expTypes);
                    return;
                } else {
                    // Check type if resolved as BLangSimpleVarRef
                    exprType = checkExpr(unaryExpr.expr, env).get(0);
                }
            } else {
                // Check type if resolved as non BLangSimpleVarRef Expression
                exprType = checkExpr(unaryExpr.expr, env).get(0);
            }
            if (exprType != symTable.errType) {
                List<BType> paramTypes = Lists.of(exprType);
                List<BType> retTypes = Lists.of(symTable.typeType);
                BInvokableType opType = new BInvokableType(paramTypes, retTypes, null);
                if (!types.isValueType(exprType)) {
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
            }
        } else {
            exprType = checkExpr(unaryExpr.expr, env).get(0);
            if (exprType != symTable.errType) {
                BSymbol symbol = symResolver.resolveUnaryOperator(unaryExpr.pos, unaryExpr.operator, exprType);
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

        // Lookup type explicit cast operator symbol
        BSymbol symbol = symResolver.resolveExplicitCastOperator(sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            BSymbol conversionSymbol = symResolver.resolveConversionOperator(sourceType, targetType);
            if (conversionSymbol == symTable.notFoundSymbol) {
                dlog.error(castExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CAST, sourceType, targetType);
            } else {
                dlog.error(castExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CAST_WITH_SUGGESTION,
                        sourceType, targetType);
            }
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

        if (conversionExpr.transformerInvocation == null) {
            // Lookup for built-in type conversion operator symbol
            BSymbol symbol = symResolver.resolveConversionOperator(sourceType, targetType);
            if (symbol == symTable.notFoundSymbol) {
                // If not found, look for unnamed transformers for the given types
                actualTypes = checkUnNamedTransformerInvocation(conversionExpr, sourceType, targetType);
            } else {
                BConversionOperatorSymbol conversionSym = (BConversionOperatorSymbol) symbol;
                conversionExpr.conversionSymbol = conversionSym;
                actualTypes = getActualTypesOfConversionExpr(conversionExpr, targetType, sourceType, conversionSym);
            }
        } else {
            actualTypes = checkNamedTransformerInvocation(conversionExpr, sourceType, targetType);
        }

        resultTypes = types.checkTypes(conversionExpr, actualTypes, expTypes);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.type = bLangLambdaFunction.function.symbol.type;
        resultTypes = types.checkTypes(bLangLambdaFunction, Lists.of(bLangLambdaFunction.type), expTypes);
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
            ((BLangXMLQName) indexExpr).isUsedInXML = true;
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

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        checkExpr(intRangeExpression.startExpr, env, Lists.of(symTable.intType));
        checkExpr(intRangeExpression.endExpr, env, Lists.of(symTable.intType));
        resultTypes = Lists.of(new BArrayType(symTable.intType));
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        BType actualType = symTable.errType;
        int expTypeTag = expTypes.get(0).tag;

        if (expTypeTag == TypeTags.TABLE) {
            actualType = expTypes.get(0);
        } else if (expTypeTag != TypeTags.ERROR) {
            dlog.error(tableQueryExpression.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION, expTypes.get(0));
        }

        BLangTableQuery tableQuery = (BLangTableQuery) tableQueryExpression.getTableQuery();
        tableQuery.accept(this);

        resultTypes = types.checkTypes(tableQueryExpression, Lists.of(actualType), expTypes);
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        BLangStreamingInput streamingInput = (BLangStreamingInput) tableQuery.getStreamingInput();
        streamingInput.accept(this);

        BLangJoinStreamingInput joinStreamingInput = (BLangJoinStreamingInput) tableQuery.getJoinStreamingInput();
        if (joinStreamingInput != null) {
            joinStreamingInput.accept(this);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        List<? extends SelectExpressionNode> selectExprList = selectClause.getSelectExpressions();
        selectExprList.forEach(selectExpr -> ((BLangSelectExpression) selectExpr).accept(this));

        BLangGroupBy groupBy = (BLangGroupBy) selectClause.getGroupBy();
        if (groupBy != null) {
            groupBy.accept(this);
        }

        BLangHaving having = (BLangHaving) selectClause.getHaving();
        if (having != null) {
            having.accept(this);
        }
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        BLangExpression expr = (BLangExpression) selectExpression.getExpression();
        expr.accept(this);
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        groupBy.getVariables().forEach(expr -> ((BLangExpression) expr).accept(this));
    }

    @Override
    public void visit(BLangHaving having) {
        BLangExpression expr = (BLangExpression) having.getExpression();
        expr.accept(this);
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        for (ExpressionNode expr : orderBy.getVariables()) {
            ((BLangExpression) expr).accept(this);
        }
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        BLangStreamingInput streamingInput = (BLangStreamingInput) joinStreamingInput.getStreamingInput();
        streamingInput.accept(this);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        BLangExpression varRef = (BLangExpression) streamingInput.getStreamReference();
        varRef.accept(this);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangRestArgExpression) {
        resultTypes = checkExpr(bLangRestArgExpression.expr, env, expTypes);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        resultTypes = checkExpr(bLangNamedArgsExpression.expr, env, expTypes);
        bLangNamedArgsExpression.type = bLangNamedArgsExpression.expr.type;
    }

    public void visit(BLangStreamlet streamletNode){
        /* ignore */
    }

    // Private methods

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    private void setExprType(BLangExpression expr, List<BType> expTypes) {
        int expected = expTypes.size();
        if (expr instanceof MultiReturnExpr) {
            MultiReturnExpr multiReturnExpr = (MultiReturnExpr) expr;
            multiReturnExpr.setTypes(resultTypes);
        } else {
            if (expected > 1) {
                dlog.error(expr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, 1);
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
        int actual = conversionSymbol.type.getReturnTypes().size();

        List<BType> actualTypes = getListWithErrorTypes(expected);
        if (conversionSymbol.safe && expected == 1) {
            actualTypes = Lists.of(conversionSymbol.type.getReturnTypes().get(0));

        } else if (!conversionSymbol.safe && expected == 1) {
            dlog.error(castExpr.pos, DiagnosticCode.UNSAFE_CONVERSION_ATTEMPT, sourceType, targetType);

        } else if (expected != actual) {
            dlog.error(castExpr.pos, DiagnosticCode.ASSIGNMENT_COUNT_MISMATCH, expected, actual);
        } else {
            actualTypes = conversionSymbol.type.getReturnTypes();
        }

        return actualTypes;
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr) {
        Name funcName = names.fromIdNode(iExpr.name);
        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);
        BSymbol funcSymbol = symResolver.lookupSymbolInPackage(iExpr.pos, env, pkgAlias, funcName, SymTag.VARIABLE);
        if (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, funcName);
            resultTypes = getListWithErrorTypes(expTypes.size());
            return;
        }
        if (funcSymbol.tag == SymTag.VARIABLE) {
            // Check for function pointer.
            iExpr.functionPointerInvocation = true;
        }
        // Set the resolved function symbol in the invocation expression.
        // This is used in the code generation phase.
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr, BStructType structType) {
        String funcName = iExpr.name.value;
        Name uniqueFuncName = names.fromString(
                Symbols.getAttachedFuncSymbolName(structType.tsymbol.name.value, funcName));
        BPackageSymbol packageSymbol = (BPackageSymbol) structType.tsymbol.owner;
        BSymbol funcSymbol = symResolver.lookupMemberSymbol(iExpr.pos, packageSymbol.scope, this.env,
                uniqueFuncName, SymTag.FUNCTION);
        if (funcSymbol == symTable.notFoundSymbol) {
            // Check, any function pointer in struct field with given name.
            funcSymbol = symResolver.resolveStructField(iExpr.pos, env, names.fromIdNode(iExpr.name),
                    iExpr.expr.symbol.type.tsymbol);
            if (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE) {
                dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION_IN_STRUCT, funcName, structType);
                resultTypes = getListWithErrorTypes(expTypes.size());
                return;
            }
            iExpr.functionPointerInvocation = true;
        } else {
            // Attached function found
            // Check for the explicit initializer function invocation
            BStructSymbol.BAttachedFunction initializerFunc = ((BStructSymbol) structType.tsymbol).initializerFunc;
            if (initializerFunc != null && initializerFunc.funcName.value.equals(funcName)) {
                dlog.error(iExpr.pos, DiagnosticCode.STRUCT_INITIALIZER_INVOKED, structType.tsymbol.toString());
            }
        }
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr, BType bType) {
        Name funcName = names.fromString(
                Symbols.getAttachedFuncSymbolName(bType.toString(), iExpr.name.value));
        BPackageSymbol packageSymbol = (BPackageSymbol) bType.tsymbol.owner;
        BSymbol funcSymbol = symResolver.lookupMemberSymbol(iExpr.pos, packageSymbol.scope, this.env,
                funcName, SymTag.FUNCTION);
        if (funcSymbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, funcName);
            resultTypes = getListWithErrorTypes(expTypes.size());
            return;
        }
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private boolean isIterableOperationInvocation(BLangInvocation iExpr) {
        switch (iExpr.expr.type.tag) {
            case TypeTags.ARRAY:
            case TypeTags.MAP:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.STREAM:
            case TypeTags.STREAMLET:
            case TypeTags.TABLE:
            case TypeTags.TUPLE_COLLECTION:
                return IterableKind.getFromString(iExpr.name.value) != IterableKind.UNDEFINED;
        }
        return false;
    }

    private void checkInvocationParamAndReturnType(BLangInvocation iExpr) {
        List<BType> paramTypes = ((BInvokableType) iExpr.symbol.type).getParameterTypes();
        int requiredParamsCount;
        if (iExpr.symbol.tag == SymTag.VARIABLE) {
            // Here we assume function pointers can have only required params.
            // And assume that named params and rest params are not supported.
            requiredParamsCount = paramTypes.size();
        } else {
            requiredParamsCount = ((BInvokableSymbol) iExpr.symbol).params.size();
        }

        // Split the different argument types: required args, named args and rest args
        int i = 0;
        BLangExpression vararg = null;
        for (BLangExpression expr : iExpr.argExprs) {
            switch (expr.getKind()) {
                case NAMED_ARGS_EXPR:
                    iExpr.namedArgs.add((BLangNamedArgsExpression) expr);
                    break;
                case REST_ARGS_EXPR:
                    vararg = expr;
                    break;
                default:
                    if (i < requiredParamsCount) {
                        iExpr.requiredArgs.add(expr);
                    } else {
                        iExpr.restArgs.add(expr);
                    }
                    i++;
                    break;
            }
        }

        List<BType> actualTypes = checkInvocationArgs(iExpr, paramTypes, requiredParamsCount, vararg);
        checkInvocationReturnTypes(iExpr, actualTypes);
    }

    private List<BType> checkInvocationArgs(BLangInvocation iExpr, List<BType> paramTypes, int requiredParamsCount,
                                            BLangExpression vararg) {
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());
        BInvokableSymbol invocableSymbol = (BInvokableSymbol) iExpr.symbol;

        // Check whether the expected param count and the actual args counts are matching.
        if (requiredParamsCount > iExpr.requiredArgs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.NOT_ENOUGH_ARGS_FUNC_CALL, iExpr.name.value);
            return actualTypes;
        } else if (invocableSymbol.restParam == null && (vararg != null || !iExpr.restArgs.isEmpty())) {
            dlog.error(iExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
            return actualTypes;
        }

        // If the one and only argument is a function call, the return types of that inner function should match the
        // formal parameters of the outer function.
        if (iExpr.argExprs.size() == 1 && iExpr.argExprs.get(0).getKind() == NodeKind.INVOCATION) {
            checkExpr(iExpr.requiredArgs.get(0), this.env, ((BInvokableType) invocableSymbol.type).paramTypes);
        } else {
            checkRequiredArgs(iExpr.requiredArgs, paramTypes);
        }
        checkNamedArgs(iExpr.namedArgs, invocableSymbol.defaultableParams);
        checkRestArgs(iExpr.restArgs, vararg, invocableSymbol.restParam);
        return invocableSymbol.type.getReturnTypes();
    }

    private void checkRequiredArgs(List<BLangExpression> requiredArgExprs, List<? extends Type> reqiredParamTypes) {
        for (int i = 0; i < requiredArgExprs.size(); i++) {
            checkExpr(requiredArgExprs.get(i), this.env, Lists.of((BType) reqiredParamTypes.get(i)));
        }
    }

    private void checkNamedArgs(List<BLangExpression> namedArgExprs, List<BVarSymbol> defaultableParams) {
        for (BLangExpression expr : namedArgExprs) {
            BLangIdentifier argName = ((NamedArgNode) expr).getName();
            BVarSymbol varSym = defaultableParams.stream().filter(param -> param.getName().value.equals(argName.value))
                    .findAny().orElse(null);
            if (varSym == null) {
                dlog.error(expr.pos, DiagnosticCode.UNDEFINED_PARAMETER, argName);
                break;
            }

            checkExpr(expr, this.env, Lists.of(varSym.type));
        }
    }

    private void checkRestArgs(List<BLangExpression> restArgExprs, BLangExpression vararg, BVarSymbol restParam) {
        if (vararg != null && !restArgExprs.isEmpty()) {
            dlog.error(vararg.pos, DiagnosticCode.INVALID_REST_ARGS);
            return;
        }

        if (vararg != null) {
            checkExpr(vararg, this.env, Lists.of(restParam.type));
            restArgExprs.add(vararg);
            return;
        }

        for (BLangExpression arg : restArgExprs) {
            checkExpr(arg, this.env, Lists.of(((BArrayType) restParam.type).eType));
        }
    }

    private void checkActionInvocationExpr(BLangInvocation iExpr, BType conType) {
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());
        if (conType == symTable.errType
                || !(conType.tag == TypeTags.STRUCT || conType.tag == TypeTags.CONNECTOR)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION);
            resultTypes = actualTypes;
            return;
        }

        BSymbol conSymbol;
        if (iExpr.expr.getKind() == NodeKind.INVOCATION) {
            final BInvokableSymbol invokableSymbol = (BInvokableSymbol) ((BLangInvocation) iExpr.expr).symbol;
            conSymbol = ((BInvokableType) invokableSymbol.type).retTypes.get(0).tsymbol;
        } else {
            conSymbol = iExpr.expr.symbol;
            if (conSymbol.tag == SymTag.ENDPOINT) {
                conSymbol = ((BEndpointVarSymbol) conSymbol).attachedConnector;
            } else if (conSymbol.tag == SymTag.VARIABLE) {
                conSymbol = conSymbol.type.tsymbol;
            }
        }
        if (conSymbol == null
                || conSymbol == symTable.notFoundSymbol
                || conSymbol == symTable.errSymbol
                || conSymbol.tag != SymTag.CONNECTOR) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION);
            resultTypes = actualTypes;
            return;
        }

        Name actionName = names.fromIdNode(iExpr.name);
        BSymbol actionSym = symResolver.lookupMemberSymbol(iExpr.pos, conSymbol.type.tsymbol.scope,
                env, actionName, SymTag.ACTION);
        if (actionSym == symTable.errSymbol || actionSym == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_ACTION, actionName, conSymbol.type);
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

    private void checkConnectorInitTypes(BLangTypeInit iExpr, BType actualType, Name connName) {
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
                fieldType = checkMapLiteralKeyExpr(keyValuePair.key.expr, recType, RecordKind.MAP);
                break;
            case TypeTags.JSON:
                fieldType = checkJSONLiteralKeyExpr(keyValuePair.key, recType, RecordKind.JSON);

                // If the field is again a struct, treat that literal expression as another constraint JSON.
                if (fieldType.tag == TypeTags.STRUCT) {
                    fieldType = new BJSONType(TypeTags.JSON, fieldType, symTable.jsonType.tsymbol);
                }

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
            dlog.error(keyExpr.pos, DiagnosticCode.UNDEFINED_STRUCT_FIELD, fieldName, recordType.tsymbol);
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
            dlog.error(varReferExpr.pos, DiagnosticCode.UNDEFINED_STRUCT_FIELD, fieldName, structType.tsymbol);
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

            concatExpr = getBinaryAddExpr(concatExpr, expr, opSymbol);
        }

        return concatExpr;
    }

    /**
     * Concatenate the consecutive text type nodes, and get the reduced set of children.
     *
     * @param exprs         Child nodes
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
            strConcatExpr = getBinaryAddExpr(strConcatExpr, expr, opSymbol);
        }

        // Add remaining concatenated text nodes as children
        if (strConcatExpr != null) {
            newChildren.add(getXMLTextLiteral(strConcatExpr));
        }

        return newChildren;
    }

    private BLangExpression getBinaryAddExpr(BLangExpression lExpr, BLangExpression rExpr, BSymbol opSymbol) {
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

    private List<BType> checkUnNamedTransformerInvocation(BLangTypeConversionExpr conversionExpr, BType sourceType,
                                                          BType targetType) {
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());

        // Check whether a transformer is available for the two types
        BSymbol symbol = symResolver.resolveTransformer(env, sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            // check whether a casting is possible, to provide user a hint.
            BSymbol castSymbol = symResolver.resolveExplicitCastOperator(sourceType, targetType);
            if (castSymbol == symTable.notFoundSymbol) {
                dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION, sourceType, targetType);
            } else {
                dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION_WITH_SUGGESTION, sourceType,
                        targetType);
            }
        } else {
            BTransformerSymbol transformerSymbol = (BTransformerSymbol) symbol;
            conversionExpr.conversionSymbol = transformerSymbol;
            if (conversionExpr.conversionSymbol.safe) {
                ((BInvokableType) transformerSymbol.type).retTypes.add(symTable.errStructType);
            }
            actualTypes = getActualTypesOfConversionExpr(conversionExpr, targetType, sourceType, transformerSymbol);
        }

        return actualTypes;
    }

    private List<BType> checkNamedTransformerInvocation(BLangTypeConversionExpr conversionExpr, BType sourceType,
                                                        BType targetType) {
        List<BType> actualTypes = getListWithErrorTypes(expTypes.size());
        BLangInvocation transformerInvocation = conversionExpr.transformerInvocation;
        BSymbol transformerSymbol = symResolver.lookupSymbolInPackage(transformerInvocation.pos, env,
                names.fromIdNode(transformerInvocation.pkgAlias), names.fromIdNode(transformerInvocation.name),
                SymTag.TRANSFORMER);
        if (transformerSymbol == symTable.notFoundSymbol) {
            dlog.error(conversionExpr.pos, DiagnosticCode.UNDEFINED_TRANSFORMER, transformerInvocation.name);
        } else {
            conversionExpr.conversionSymbol =
                    (BConversionOperatorSymbol) (transformerInvocation.symbol = transformerSymbol);

            // Check the transformer invocation. Expected type for the transformer is the target type
            // of the cast conversion operator, but not the lhs type.
            List<BType> prevExpType = expTypes;
            expTypes = Lists.of(targetType);
            checkInvocationParamAndReturnType(transformerInvocation);
            expTypes = prevExpType;

            if (transformerInvocation.type != symTable.errType) {
                BInvokableType transformerSymType = (BInvokableType) transformerSymbol.type;
                transformerInvocation.types = transformerSymType.retTypes;
                actualTypes = getActualTypesOfConversionExpr(conversionExpr, targetType, sourceType,
                        conversionExpr.conversionSymbol);
            }
        }

        return actualTypes;
    }

    private BLangTypeofExpr getTypeAccessExpression(BLangSimpleVarRef varRef) {
        BLangUserDefinedType userDefinedType = new BLangUserDefinedType();
        userDefinedType.pkgAlias = varRef.pkgAlias;
        userDefinedType.typeName = varRef.variableName;
        userDefinedType.pos = varRef.pos;
        BLangTypeofExpr typeAccessExpr = (BLangTypeofExpr) TreeBuilder.createTypeAccessNode();
        typeAccessExpr.typeNode = userDefinedType;
        typeAccessExpr.resolvedType = symResolver.resolveTypeNode(userDefinedType, env);
        typeAccessExpr.pos = varRef.pos;
        typeAccessExpr.type = symTable.typeType;
        return typeAccessExpr;
    }

    private BType getTypeOfExprInFieldAccess(BLangExpression expr) {
        // First check whether variable expression is of type enum.
        if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) expr;
            BSymbol symbol = symResolver.lookupSymbolInPackage(varRef.pos, env,
                    names.fromIdNode(varRef.pkgAlias), names.fromIdNode(varRef.variableName), SymTag.ENUM);
            if (symbol != symTable.notFoundSymbol) {
                expr.type = symbol.type;
                return symbol.type;
            }
        }

        checkExpr(expr, this.env, Lists.of(symTable.noType));
        return expr.type;
    }

    private void validateStructInitalizer(DiagnosticPos pos) {
        BStructType bStructType = (BStructType) expTypes.get(0);
        BStructSymbol bStructSymbol = (BStructSymbol) bStructType.tsymbol;
        if (bStructSymbol.initializerFunc == null) {
            return;
        }

        boolean samePkg = this.env.enclPkg.symbol.pkgID == bStructSymbol.pkgID;
        if (!samePkg && Symbols.isPrivate(bStructSymbol.initializerFunc.symbol)) {
            dlog.error(pos, DiagnosticCode.ATTEMPT_CREATE_NON_PUBLIC_INITIALIZER);
        }
    }
}
