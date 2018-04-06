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
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types.RecordKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BEnumType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

/**
 * @since 0.94
 */
public class TypeChecker extends BLangNodeVisitor {

    private static final CompilerContext.Key<TypeChecker> TYPE_CHECKER_KEY =
            new CompilerContext.Key<>();

    private static final String TABLE_CONFIG = "TableConfig";

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
    private BType expType;
    private BType resultType;

    private DiagnosticCode diagCode;

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

    public BType checkExpr(BLangExpression expr, SymbolEnv env) {
        return checkExpr(expr, env, symTable.noType);
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType) {
        return checkExpr(expr, env, expType, DiagnosticCode.INCOMPATIBLE_TYPES);
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
            resTypes.add(checkExpr(expr, env, expType));
        }
        return resTypes;
    }

    public BType checkExpr(BLangExpression expr, SymbolEnv env, BType expType, DiagnosticCode diagCode) {
        // TODO Check the possibility of using a try/finally here
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;
        this.env = env;
        this.diagCode = diagCode;
        this.expType = expType;

        expr.accept(this);

        expr.type = resultType;
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;
        return resultType;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        BType literalType = symTable.getTypeFromTag(literalExpr.typeTag);
        if (this.expType.tag == TypeTags.FINITE) {
            BFiniteType expType = (BFiniteType) this.expType;
            boolean foundMember = expType.valueSpace
                    .stream()
                    .map(memberLiteral -> {
                        if (((BLangLiteral) memberLiteral).value == null) {
                            return literalExpr.value == null;
                        } else {
                            return ((BLangLiteral) memberLiteral).value.equals(literalExpr.value);
                        }
                    })
                    .anyMatch(found -> found);

            boolean foundMemberType = expType.memberTypes
                    .stream()
                    .map(memberType -> types.isAssignable(literalType, memberType))
                    .anyMatch(foundType -> foundType);

            if (foundMember || foundMemberType) {
                types.setImplicitCastExpr(literalExpr, literalType, symTable.anyType);
                resultType = literalType;
            } else {
                dlog.error(literalExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, literalType);
                resultType = symTable.errType;
            }
            return;
        }
        resultType = types.checkType(literalExpr, literalType, expType);
    }

    public void visit(BLangTableLiteral tableLiteral) {
        BType actualType = symTable.rootScope.lookup(new Name(TABLE_CONFIG)).symbol.type;
        checkExpr(tableLiteral.configurationExpr, env, actualType);
        resultType = types.checkType(tableLiteral, expType, symTable.noType);
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // Check whether the expected type is an array type
        // var a = []; and var a = [1,2,3,4]; are illegal statements, because we cannot infer the type here.
        BType actualType = symTable.errType;

        if (expType.tag == TypeTags.ANY) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INVALID_ARRAY_LITERAL, expType);
            resultType = symTable.errType;
            return;
        }

        int expTypeTag = expType.tag;
        if (expTypeTag == TypeTags.JSON || expTypeTag == TypeTags.ANY) {
            checkExprs(arrayLiteral.exprs, this.env, expType);
            actualType = expType;

        } else if (expTypeTag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) expType;
            checkExprs(arrayLiteral.exprs, this.env, arrayType.eType);
            actualType = new BArrayType(arrayType.eType);

        } else if (expTypeTag != TypeTags.ERROR) {
            List<BType> resTypes = checkExprs(arrayLiteral.exprs, this.env, symTable.noType);
            Set<BType> arrayLitExprTypeSet = new HashSet<>(resTypes);
            BType[] uniqueExprTypes = arrayLitExprTypeSet.toArray(new BType[0]);
            if (uniqueExprTypes.length == 0) {
                actualType = symTable.anyType;
            } else if (uniqueExprTypes.length == 1) {
                actualType = resTypes.get(0);
            } else {
                BType superType = uniqueExprTypes[0];
                for (int i = 1; i < uniqueExprTypes.length; i++) {
                    if (types.isAssignable(superType, uniqueExprTypes[i])) {
                        superType = uniqueExprTypes[i];
                    } else if (!types.isAssignable(uniqueExprTypes[i], superType)) {
                        superType = symTable.anyType;
                        break;
                    }
                }
                actualType = superType;
            }
            actualType = new BArrayType(actualType);
        }

        resultType = types.checkType(arrayLiteral, actualType, expType);
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        BType actualType = symTable.errType;
        int expTypeTag = expType.tag;
        BType originalExpType = expType;
        if (expTypeTag == TypeTags.NONE || expTypeTag == TypeTags.ANY) {
            // Change the expected type to map,
            expType = symTable.mapType;
        }
        if (expTypeTag == TypeTags.ANY
                || (expTypeTag == TypeTags.MAP && recordLiteral.keyValuePairs.isEmpty())
                || (expTypeTag == TypeTags.STRUCT && originalExpType.tsymbol.kind == SymbolKind.OBJECT)) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_RECORD_LITERAL, originalExpType);
            resultType = symTable.errType;
            return;
        }

        List<BType> matchedTypeList = getRecordCompatibleType(expType);

        if (matchedTypeList.isEmpty()) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expType);
        } else if (matchedTypeList.size() > 1) {
            dlog.error(recordLiteral.pos, DiagnosticCode.AMBIGUOUS_TYPES, expType);
        } else {
            recordLiteral.keyValuePairs
                    .forEach(keyValuePair -> checkRecLiteralKeyValue(keyValuePair, matchedTypeList.get(0)));
            actualType = matchedTypeList.get(0);

            // TODO Following check can be moved the code analyzer.
            if (expTypeTag == TypeTags.STRUCT) {
                validateStructInitalizer(recordLiteral.pos);
            }
        }

        resultType = types.checkType(recordLiteral, actualType, expType);
    }

    private List<BType> getRecordCompatibleType(BType bType) {
        Set<BType> expTypes = bType.tag == TypeTags.UNION ? ((BUnionType) bType).memberTypes : new HashSet<BType>() {
            {
                add(bType);
            }
        };

        return expTypes.stream()
                .filter(type -> type.tag == TypeTags.JSON ||
                        type.tag == TypeTags.MAP ||
                        type.tag == TypeTags.STRUCT ||
                        type.tag == TypeTags.NONE ||
                        type.tag == TypeTags.ANY)
                .collect(Collectors.toList());
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
            resultType = varRefExpr.type;
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
                if (env.enclInvokable != null && env.enclInvokable.flagSet.contains(Flag.LAMBDA) &&
                        env.enclEnv.enclEnv != null) {
                    BLangVariable closureVar = symResolver.findClosureVar(env.enclEnv, symbol);
                    if (closureVar != symTable.notFoundVariable) {
                        ((BLangFunction) env.enclInvokable).closureVarList.add(closureVar);
                    }
                }
            } else {
                dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
            }
        }

        // Check type compatibility
        resultType = types.checkType(varRefExpr, actualType, expType);
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // First analyze the variable reference expression.
        fieldAccessExpr.expr.lhsVar = fieldAccessExpr.lhsVar;
        BType varRefType = getTypeOfExprInFieldAccess(fieldAccessExpr.expr);

        if (fieldAccessExpr.fieldKind == FieldKind.ALL && varRefType.tag != TypeTags.XML) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_GET_ALL_FIELDS, varRefType);
        }

        varRefType = getSafeType(varRefType, fieldAccessExpr.safeNavigate, fieldAccessExpr.pos);
        Name fieldName = names.fromIdNode(fieldAccessExpr.field);

        // Get the effective types of the expression. If there are errors/nill propagating from parent
        // expressions, then the effective type will include those as well.
        BType actualType = checkFieldAccessExpr(fieldAccessExpr, varRefType, fieldName);
        actualType = getAccessExprFinalType(fieldAccessExpr, actualType);

        resultType = types.checkType(fieldAccessExpr, actualType, this.expType);
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        // First analyze the variable reference expression.
        indexBasedAccessExpr.expr.lhsVar = indexBasedAccessExpr.lhsVar;
        checkExpr(indexBasedAccessExpr.expr, this.env, symTable.noType);

        BType varRefType = indexBasedAccessExpr.expr.type;
        varRefType = getSafeType(varRefType, indexBasedAccessExpr.safeNavigate, indexBasedAccessExpr.pos);

        // Get the effective types of the expression. If there are errors/nill propagating from parent
        // expressions, then the effective type will include those as well.
        BType actualType = checkIndexAccessExpr(indexBasedAccessExpr, varRefType);
        actualType = getAccessExprFinalType(indexBasedAccessExpr, actualType);

        this.resultType = this.types.checkType(indexBasedAccessExpr, actualType, this.expType);
        indexBasedAccessExpr.childType = this.resultType;
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
        final BType exprType = checkExpr(iExpr.expr, this.env, symTable.noType);
        if (isIterableOperationInvocation(iExpr)) {
            iExpr.iterableOperationInvocation = true;
            iterableAnalyzer.handlerIterableOperation(iExpr, expType, env);
            resultType = iExpr.iContext.operations.getLast().resultType;
            return;
        }
        if (iExpr.actionInvocation) {
            checkActionInvocationExpr(iExpr, exprType);
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
                resultType = symTable.errType;
                break;
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.BLOB:
            case TypeTags.XML:
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
            case TypeTags.FUTURE:
                checkFunctionInvocationExpr(iExpr, symTable.futureType);
                break;
            case TypeTags.NONE:
                dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, iExpr.name);
                break;
            case TypeTags.MAP:
                // allow map function for both constrained / un constrained maps
                checkFunctionInvocationExpr(iExpr, this.symTable.mapType);
                break;
            case TypeTags.ERROR:
                break;
            case TypeTags.INTERMEDIATE_COLLECTION:
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_INVOCATION_WITH_NAME, iExpr.name,
                        iExpr.expr.type);
                resultType = symTable.errType;
                break;
            default:
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_INVOCATION, iExpr.expr.type);
                resultType = symTable.errType;
                break;
        }

        if (iExpr.symbol != null) {
            iExpr.childType = ((BInvokableSymbol) iExpr.symbol).type.getReturnType();
        } else {
            iExpr.childType = iExpr.type;
        }
    }

    public void visit(BLangTypeInit cIExpr) {
        if ((expType.tag == TypeTags.ANY && cIExpr.userDefinedType == null)
                || (expType.tag == TypeTags.STRUCT && expType.tsymbol.kind != SymbolKind.OBJECT)) {
            dlog.error(cIExpr.pos, DiagnosticCode.INVALID_TYPE_NEW_LITERAL, expType);
            resultType = symTable.errType;
            return;
        }
        BType actualType;
        if (cIExpr.userDefinedType != null) {
            actualType = symResolver.resolveTypeNode(cIExpr.userDefinedType, env);
        } else {
            actualType = expType;
        }

        if (actualType == symTable.errType) {
            //TODO dlog error?
            resultType = symTable.errType;
            return;
        }

        if (actualType.tag != TypeTags.STRUCT) {
            //TODO dlog error?
            resultType = symTable.errType;
            return;
        }

        cIExpr.objectInitInvocation.symbol = ((BStructSymbol) actualType.tsymbol).initializerFunc.symbol;
        cIExpr.objectInitInvocation.type = symTable.nilType;
        checkInvocationParam(cIExpr.objectInitInvocation);

        resultType = types.checkType(cIExpr, actualType, expType);
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        BType condExprType = checkExpr(ternaryExpr.expr, env, this.symTable.booleanType);
        BType thenType = checkExpr(ternaryExpr.thenExpr, env, expType);
        BType elseType = checkExpr(ternaryExpr.elseExpr, env, expType);
        if (condExprType == symTable.errType || thenType == symTable.errType || elseType == symTable.errType) {
            resultType = symTable.errType;
        } else if (expType == symTable.noType) {
            if (thenType == elseType) {
                resultType = thenType;
            } else {
                dlog.error(ternaryExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, thenType, elseType);
                resultType = symTable.errType;
            }
        } else {
            resultType = expType;
        }
    }

    public void visit(BLangAwaitExpr awaitExpr) {
        BType actualType;
        BType expType = checkExpr(awaitExpr.expr, env, this.symTable.noType);
        if (expType == symTable.errType) {
            actualType = symTable.errType;
        } else {
            actualType = ((BFutureType) expType).constraint;
        }
        resultType = types.checkType(awaitExpr, actualType, this.expType);
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env);
        BType rhsType = checkExpr(binaryExpr.rhsExpr, env);

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
                actualType = opSymbol.type.getReturnType();
            }
        }

        resultType = types.checkType(binaryExpr, actualType, expType);
    }

    public void visit(BLangElvisExpr elvisExpr) {
        BType lhsType = checkExpr(elvisExpr.lhsExpr, env);
        BType actualType = symTable.errType;
        if (lhsType != symTable.errType) {
            if (lhsType.tag == TypeTags.UNION && lhsType.isNullable()) {
                BUnionType unionType = (BUnionType) lhsType;
                HashSet<BType> memberTypes = new HashSet<BType>();
                Iterator<BType> iterator = unionType.getMemberTypes().iterator();
                while (iterator.hasNext()) {
                    BType memberType = iterator.next();
                    if (memberType != symTable.nilType) {
                        memberTypes.add(memberType);
                    }
                }
                if (memberTypes.size() == 1) {
                    BType[] memberArray = new BType[1];
                    memberTypes.toArray(memberArray);
                    actualType = memberArray[0];
                } else {
                    actualType = new BUnionType(null, memberTypes, false);
                }
            } else {
                dlog.error(elvisExpr.pos, DiagnosticCode.OPERATOR_NOT_SUPPORTED,
                        OperatorKind.ELVIS, lhsType);
            }
        }
        BType rhsReturnType = checkExpr(elvisExpr.rhsExpr, env, expType);
        BType lhsReturnType = types.checkType(elvisExpr.lhsExpr.pos, actualType, expType,
                DiagnosticCode.INCOMPATIBLE_TYPES);
        if (rhsReturnType == symTable.errType || lhsReturnType == symTable.errType) {
            resultType = symTable.errType;
        } else if (expType == symTable.noType) {
            if (types.isSameType(rhsReturnType, lhsReturnType)) {
                resultType = lhsReturnType;
            } else {
                dlog.error(elvisExpr.rhsExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, lhsReturnType, rhsReturnType);
                resultType = symTable.errType;
            }
        } else {
            resultType = expType;
        }
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        // Handle Tuple Expression.
        if (expType.tag == TypeTags.TUPLE) {
            BTupleType tupleType = (BTupleType) this.expType;
            // Fix this.
            List<BType> expTypes = getListWithErrorTypes(bracedOrTupleExpr.expressions.size());
            if (tupleType.tupleTypes.size() != bracedOrTupleExpr.expressions.size()) {
                dlog.error(bracedOrTupleExpr.pos, DiagnosticCode.SYNTAX_ERROR,
                        "tuple and expression size does not match");
            } else {
                expTypes = tupleType.tupleTypes;
            }
            List<BType> results = new ArrayList<>();
            for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
                results.add(checkExpr(bracedOrTupleExpr.expressions.get(i), env, expTypes.get(i)));
            }
            resultType = new BTupleType(results);
        } else if (bracedOrTupleExpr.expressions.size() > 1) {
            // This is a tuple.
            List<BType> results = new ArrayList<>();
            for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
                results.add(checkExpr(bracedOrTupleExpr.expressions.get(i), env, symTable.noType));
            }
            resultType = new BTupleType(results);
        } else {
            // This is a braced expression.
            bracedOrTupleExpr.isBracedExpr = true;
            final BLangExpression expr = bracedOrTupleExpr.expressions.get(0);
            final BType actualType = checkExpr(expr, env, symTable.noType);
            types.setImplicitCastExpr(expr, actualType, expType);
            resultType = actualType;
        }
    }

    public void visit(BLangTypeofExpr accessExpr) {
        BType actualType = symTable.typeDesc;
        accessExpr.resolvedType = symResolver.resolveTypeNode(accessExpr.typeNode, env);
        resultType = types.checkType(accessExpr, actualType, expType);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        BType exprType;
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
                    resultType = types.checkType(unaryExpr, actualType, expType);
                    return;
                } else {
                    // Check type if resolved as BLangSimpleVarRef
                    exprType = checkExpr(unaryExpr.expr, env);
                }
            } else {
                // Check type if resolved as non BLangSimpleVarRef Expression
                exprType = checkExpr(unaryExpr.expr, env);
            }
            if (exprType != symTable.errType) {
                unaryExpr.opSymbol = Symbols.createTypeofOperatorSymbol(exprType, types, symTable, names);
                actualType = unaryExpr.opSymbol.type.getReturnType();
            }
        } else if (OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
            exprType = checkExpr(unaryExpr.expr, env);
            if (exprType != symTable.errType) {
                actualType = exprType;
            }
        } else {
            exprType = checkExpr(unaryExpr.expr, env);
            if (exprType != symTable.errType) {
                BSymbol symbol = symResolver.resolveUnaryOperator(unaryExpr.pos, unaryExpr.operator, exprType);
                if (symbol == symTable.notFoundSymbol) {
                    dlog.error(unaryExpr.pos, DiagnosticCode.UNARY_OP_INCOMPATIBLE_TYPES,
                            unaryExpr.operator, exprType);
                } else {
                    unaryExpr.opSymbol = (BOperatorSymbol) symbol;
                    actualType = symbol.type.getReturnType();
                }
            }
        }

        resultType = types.checkType(unaryExpr, actualType, expType);
    }

    public void visit(BLangTypeConversionExpr conversionExpr) {
        // Set error type as the actual type.
        BType actualType;

        BType targetType = symResolver.resolveTypeNode(conversionExpr.typeNode, env);
        conversionExpr.targetType = targetType;
        BType sourceType = checkExpr(conversionExpr.expr, env, symTable.noType);

        if (conversionExpr.transformerInvocation == null) {
            // Lookup for built-in type conversion operator symbol
            BSymbol symbol = symResolver.resolveConversionOperator(sourceType, targetType);
            if (symbol == symTable.notFoundSymbol) {
                // If not found, look for unnamed transformers for the given types
                actualType = checkUnNamedTransformerInvocation(conversionExpr, sourceType, targetType);
            } else {
                BConversionOperatorSymbol conversionSym = (BConversionOperatorSymbol) symbol;
                conversionExpr.conversionSymbol = conversionSym;
                actualType = conversionSym.type.getReturnType();
            }
        } else {
            actualType = checkNamedTransformerInvocation(conversionExpr, sourceType, targetType);
        }

        resultType = types.checkType(conversionExpr, actualType, expType);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.type = bLangLambdaFunction.function.symbol.type;
        resultType = types.checkType(bLangLambdaFunction, bLangLambdaFunction.type, expType);
    }

    public void visit(BLangXMLQName bLangXMLQName) {
        String prefix = bLangXMLQName.prefix.value;
        resultType = types.checkType(bLangXMLQName, symTable.stringType, expType);
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
        checkExpr(bLangXMLAttribute.name, xmlAttributeEnv, symTable.stringType);

        // check attribute value
        checkExpr(bLangXMLAttribute.value, xmlAttributeEnv, symTable.stringType);

        symbolEnter.defineNode(bLangXMLAttribute, env);
    }

    public void visit(BLangXMLElementLiteral bLangXMLElementLiteral) {
        SymbolEnv xmlElementEnv = SymbolEnv.getXMLElementEnv(bLangXMLElementLiteral, env);

        // Visit in-line namespace declarations
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (attribute.name.getKind() == NodeKind.XML_QNAME
                    && ((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                checkExpr((BLangExpression) attribute, xmlElementEnv, symTable.noType);
            }
        });

        // Visit attributes.
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (attribute.name.getKind() != NodeKind.XML_QNAME
                    || !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                checkExpr((BLangExpression) attribute, xmlElementEnv, symTable.noType);
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
        resultType = types.checkType(bLangXMLElementLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        bLangXMLTextLiteral.concatExpr = getStringTemplateConcatExpr(bLangXMLTextLiteral.textFragments);
        resultType = types.checkType(bLangXMLTextLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        bLangXMLCommentLiteral.concatExpr = getStringTemplateConcatExpr(bLangXMLCommentLiteral.textFragments);
        resultType = types.checkType(bLangXMLCommentLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        checkExpr(bLangXMLProcInsLiteral.target, env, symTable.stringType);
        bLangXMLProcInsLiteral.dataConcatExpr = getStringTemplateConcatExpr(bLangXMLProcInsLiteral.dataFragments);
        resultType = types.checkType(bLangXMLProcInsLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        bLangXMLQuotedString.concatExpr = getStringTemplateConcatExpr(bLangXMLQuotedString.textFragments);
        resultType = types.checkType(bLangXMLQuotedString, symTable.stringType, expType);
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        BType actualType = symTable.errType;

        // First analyze the variable reference expression.
        checkExpr(xmlAttributeAccessExpr.expr, env, symTable.xmlType);

        // Then analyze the index expression.
        BLangExpression indexExpr = xmlAttributeAccessExpr.indexExpr;
        if (indexExpr == null) {
            if (xmlAttributeAccessExpr.lhsVar) {
                dlog.error(xmlAttributeAccessExpr.pos, DiagnosticCode.XML_ATTRIBUTE_MAP_UPDATE_NOT_ALLOWED);
            } else {
                actualType = symTable.xmlAttributesType;
            }
            resultType = types.checkType(xmlAttributeAccessExpr, actualType, expType);
            return;
        }

        checkExpr(indexExpr, env, symTable.stringType);
        if (indexExpr.getKind() == NodeKind.XML_QNAME) {
            ((BLangXMLQName) indexExpr).isUsedInXML = true;
        }

        if (indexExpr.type.tag == TypeTags.STRING) {
            actualType = symTable.stringType;
        }

        xmlAttributeAccessExpr.namespaces.putAll(symResolver.resolveAllNamespaces(env));
        resultType = types.checkType(xmlAttributeAccessExpr, actualType, expType);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.concatExpr = getStringTemplateConcatExpr(stringTemplateLiteral.exprs);
        resultType = types.checkType(stringTemplateLiteral, symTable.stringType, expType);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        checkExpr(intRangeExpression.startExpr, env, symTable.intType);
        checkExpr(intRangeExpression.endExpr, env, symTable.intType);
        resultType = new BArrayType(symTable.intType);
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        BType actualType = symTable.errType;
        int expTypeTag = expType.tag;

        if (expTypeTag == TypeTags.TABLE) {
            actualType = expType;
        } else if (expTypeTag != TypeTags.ERROR) {
            dlog.error(tableQueryExpression.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION, expType);
        }

        BLangTableQuery tableQuery = (BLangTableQuery) tableQueryExpression.getTableQuery();
        tableQuery.accept(this);

        resultType = types.checkType(tableQueryExpression, actualType, expType);
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
        resultType = checkExpr(bLangRestArgExpression.expr, env, expType);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        resultType = checkExpr(bLangNamedArgsExpression.expr, env, expType);
        bLangNamedArgsExpression.type = bLangNamedArgsExpression.expr.type;
    }

    public void visit(BLangForever foreverStatement) {
        /* ignore */
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        SymbolEnv matchExprEnv = SymbolEnv.createBlockEnv((BLangBlockStmt) TreeBuilder.createBlockNode(), env);
        checkExpr(bLangMatchExpression.expr, matchExprEnv);
        Set<BType> matchExprTypes = new LinkedHashSet<>();
        bLangMatchExpression.patternClauses.forEach(pattern -> {
            if (!pattern.variable.name.value.endsWith(Names.IGNORE.value)) {
                symbolEnter.defineNode(pattern.variable, matchExprEnv);
            }
            checkExpr(pattern.expr, matchExprEnv);
            pattern.variable.type = symResolver.resolveTypeNode(pattern.variable.typeNode, matchExprEnv);
            matchExprTypes.add(pattern.expr.type);
        });

        if (matchExprTypes.size() == 1) {
            bLangMatchExpression.type = matchExprTypes.toArray(new BType[matchExprTypes.size()])[0];
        } else {
            bLangMatchExpression.type =
                    new BUnionType(null, matchExprTypes, matchExprTypes.contains(symTable.nilType));
        }

        types.checkTypes(bLangMatchExpression, Lists.of(bLangMatchExpression.type), Lists.of(expType));
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        BType exprType = checkExpr(checkedExpr.expr, env, symTable.noType);
        if (exprType.tag != TypeTags.UNION) {
            if (types.isAssignable(exprType, symTable.errStructType)) {
                dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS);
            } else {
                dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS);
            }
            checkedExpr.type = symTable.errType;
            return;
        }

        BUnionType unionType = (BUnionType) exprType;
        // Filter out the list of types which are not equivalent with the error type.
        Map<Boolean, List<BType>> resultTypeMap = unionType.memberTypes.stream()
                .collect(Collectors.groupingBy(memberType -> types.isAssignable(memberType, symTable.errStructType)));

        // This list will be used in the desugar phase
        checkedExpr.equivalentErrorTypeList = resultTypeMap.get(true);
        if (checkedExpr.equivalentErrorTypeList == null ||
                checkedExpr.equivalentErrorTypeList.size() == 0) {
            // No member types in this union is equivalent to the error type
            dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS);
            checkedExpr.type = symTable.errType;
            return;
        }

        List<BType> nonErrorTypeList = resultTypeMap.get(false);
        if (nonErrorTypeList == null || nonErrorTypeList.size() == 0) {
            // All member types in the union are equivalent to the error type.
            // Checked expression requires at least one type which is not equivalent to the error type.
            dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS);
            checkedExpr.type = symTable.errType;
            return;
        }

        BType actualType;
        if (nonErrorTypeList.size() == 1) {
            actualType = nonErrorTypeList.get(0);
        } else {
            actualType = new BUnionType(null, new LinkedHashSet<>(nonErrorTypeList),
                    nonErrorTypeList.contains(symTable.nilType));
        }

        resultType = types.checkType(checkedExpr, actualType, expType);
    }

    // Private methods

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    public List<BType> getListWithErrorTypes(int count) {
        List<BType> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(symTable.errType);
        }

        return list;
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr) {
        Name funcName = names.fromIdNode(iExpr.name);
        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);

        BSymbol funcSymbol = symTable.notFoundSymbol;
        // if no package alias, check for same object attached function
        if (pkgAlias == Names.EMPTY && env.enclObject != null) {
            Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(
                    env.enclObject.name.value, iExpr.name.value));
            funcSymbol = symResolver.lookupSymbol(env, objFuncName, SymTag.VARIABLE);
            if (funcSymbol != symTable.notFoundSymbol) {
                iExpr.exprSymbol = symResolver.lookupSymbol(env, Names.SELF, SymTag.VARIABLE);
            }
        }

        // if no such function found, then try resolving in package
        if (funcSymbol == symTable.notFoundSymbol) {
            funcSymbol = symResolver.lookupSymbolInPackage(iExpr.pos, env, pkgAlias, funcName, SymTag.VARIABLE);
        }

        if (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, funcName);
            resultType = symTable.errType;
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
            // Check functions defined within the struct.
            funcSymbol = symResolver.resolveStructField(iExpr.pos, env, uniqueFuncName, structType.tsymbol);
            if (funcSymbol == symTable.notFoundSymbol) {
                // Check, any function pointer in struct field with given name.
                funcSymbol = symResolver.resolveStructField(iExpr.pos, env, names.fromIdNode(iExpr.name),
                        structType.tsymbol);
                if (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE) {
                    dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION_IN_STRUCT, funcName, structType);
                    resultType = symTable.errType;
                    return;
                }
                if ((funcSymbol.flags & Flags.ATTACHED) != Flags.ATTACHED) {
                    iExpr.functionPointerInvocation = true;
                }
            }
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
            resultType = symTable.errType;
            return;
        }
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private boolean isIterableOperationInvocation(BLangInvocation iExpr) {
        final IterableKind iterableKind = IterableKind.getFromString(iExpr.name.value);
        switch (iExpr.expr.type.tag) {
            case TypeTags.ARRAY:
            case TypeTags.MAP:
            case TypeTags.JSON:
            case TypeTags.STREAM:
            case TypeTags.TABLE:
            case TypeTags.INTERMEDIATE_COLLECTION:
                return iterableKind != IterableKind.UNDEFINED;
            case TypeTags.XML: {
                // This has been done as there are an iterable operation and a function both named "select"
                // "select" function is applicable over XML type and select iterable operation is applicable over
                // Table type. In order to avoid XML.select being confused for iterable function select at
                // TypeChecker#visit(BLangInvocation iExpr) following condition is checked.
                // TODO: There should be a proper way to resolve the conflict
                return iterableKind != IterableKind.SELECT
                        && iterableKind != IterableKind.UNDEFINED;
            }
        }
        return false;
    }

    private void checkInvocationParamAndReturnType(BLangInvocation iExpr) {
        BType actualType = checkInvocationParam(iExpr);
        if (iExpr.expr != null) {
            actualType = getAccessExprFinalType(iExpr, actualType);
        }

        resultType = types.checkType(iExpr, actualType, this.expType);
    }

    private BType checkInvocationParam(BLangInvocation iExpr) {
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
                    iExpr.namedArgs.add(expr);
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

        return checkInvocationArgs(iExpr, paramTypes, requiredParamsCount, vararg);
    }

    private BType checkInvocationArgs(BLangInvocation iExpr, List<BType> paramTypes, int requiredParamsCount,
                                      BLangExpression vararg) {
        BType actualType = symTable.errType;
        BInvokableSymbol invocableSymbol = (BInvokableSymbol) iExpr.symbol;

        // Check whether the expected param count and the actual args counts are matching.
        if (requiredParamsCount > iExpr.requiredArgs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.NOT_ENOUGH_ARGS_FUNC_CALL, iExpr.name.value);
            return actualType;
        } else if (invocableSymbol.restParam == null && (vararg != null || !iExpr.restArgs.isEmpty())) {
            dlog.error(iExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
            return actualType;
        }

        checkRequiredArgs(iExpr.requiredArgs, paramTypes);
        checkNamedArgs(iExpr.namedArgs, invocableSymbol.defaultableParams);
        checkRestArgs(iExpr.restArgs, vararg, invocableSymbol.restParam);

        if (iExpr.async) {
            return this.generateFutureType(invocableSymbol);
        } else {
            return invocableSymbol.type.getReturnType();
        }
    }

    private BFutureType generateFutureType(BInvokableSymbol invocableSymbol) {
        BType retType = invocableSymbol.type.getReturnType();
        return new BFutureType(TypeTags.FUTURE, retType, null);
    }

    private void checkRequiredArgs(List<BLangExpression> requiredArgExprs, List<BType> requiredParamTypes) {
        for (int i = 0; i < requiredArgExprs.size(); i++) {
            checkExpr(requiredArgExprs.get(i), this.env, requiredParamTypes.get(i));
        }
    }

    private void checkNamedArgs(List<BLangExpression> namedArgExprs, List<BVarSymbol> defaultableParams) {
        for (BLangExpression expr : namedArgExprs) {
            BLangIdentifier argName = ((NamedArgNode) expr).getName();
            BVarSymbol varSym = defaultableParams.stream().filter(param -> {
                // identifying field param from the symbol TODO any alternative?
                if (param.field) {
                    return param.originalName.value.equals(argName.value);
                }
                return param.getName().value.equals(argName.value);
            })
                    .findAny().orElse(null);
            if (varSym == null) {
                dlog.error(expr.pos, DiagnosticCode.UNDEFINED_PARAMETER, argName);
                break;
            }

            checkExpr(expr, this.env, varSym.type);
        }
    }

    private void checkRestArgs(List<BLangExpression> restArgExprs, BLangExpression vararg, BVarSymbol restParam) {
        if (vararg != null && !restArgExprs.isEmpty()) {
            dlog.error(vararg.pos, DiagnosticCode.INVALID_REST_ARGS);
            return;
        }

        if (vararg != null) {
            checkExpr(vararg, this.env, restParam.type);
            restArgExprs.add(vararg);
            return;
        }

        for (BLangExpression arg : restArgExprs) {
            checkExpr(arg, this.env, ((BArrayType) restParam.type).eType);
        }
    }

    private void checkActionInvocationExpr(BLangInvocation iExpr, BType conType) {
        BType actualType = symTable.errType;
        if (conType == symTable.errType || conType.tag != TypeTags.STRUCT || iExpr.expr.symbol.tag != SymTag.ENDPOINT) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION);
            resultType = actualType;
            return;
        }

        final BEndpointVarSymbol epSymbol = (BEndpointVarSymbol) iExpr.expr.symbol;
        if (!epSymbol.interactable) {
            dlog.error(iExpr.pos, DiagnosticCode.ENDPOINT_NOT_SUPPORT_INTERACTIONS, epSymbol.name);
            resultType = actualType;
            return;
        }

        BSymbol conSymbol = epSymbol.clientSymbol;
        if (conSymbol == null
                || conSymbol == symTable.notFoundSymbol
                || conSymbol == symTable.errSymbol
                || !(conSymbol.tag == SymTag.OBJECT || conSymbol.tag == SymTag.STRUCT)) {
            // TODO : Remove struct dependency.
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION);
            resultType = actualType;
            return;
        }

        Name actionName = names.fromIdNode(iExpr.name);
        Name uniqueFuncName = names.fromString(
                Symbols.getAttachedFuncSymbolName(conSymbol.name.value, actionName.value));
        BPackageSymbol packageSymbol = (BPackageSymbol) conSymbol.owner;
        BSymbol actionSym = symResolver.lookupMemberSymbol(iExpr.pos, packageSymbol.scope, this.env,
                uniqueFuncName, SymTag.FUNCTION);
        if (actionSym == symTable.notFoundSymbol) {
            actionSym = symResolver.resolveStructField(iExpr.pos, env, uniqueFuncName, (BTypeSymbol) conSymbol);
        }
        if (actionSym == symTable.errSymbol || actionSym == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_ACTION, actionName, epSymbol.name, conSymbol.type);
            resultType = actualType;
            return;
        }
        iExpr.symbol = actionSym;
        checkInvocationParamAndReturnType(iExpr);
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
                checkExpr(valueExpr, this.env, fieldType);

                // Again check the type compatibility with JSON
                if (valueExpr.impConversionExpr == null) {
                    types.checkTypes(valueExpr, Lists.of(valueExpr.type), Lists.of(symTable.jsonType));
                } else {
                    BType valueType = valueExpr.type;
                    types.checkType(valueExpr, valueExpr.impConversionExpr.type, symTable.jsonType);
                    valueExpr.type = valueType;
                }
                resultType = valueExpr.type;
                return;
        }

        checkExpr(valueExpr, this.env, fieldType);
    }

    private BType checkStructLiteralKeyExpr(BLangRecordKey key, BType recordType, RecordKind recKind) {
        Name fieldName;
        BLangExpression keyExpr = key.expr;

        if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        } else {
            // keys of the struct literal can only be a varRef (identifier)
            dlog.error(keyExpr.pos, DiagnosticCode.INVALID_STRUCT_LITERAL_KEY);
            return symTable.errType;
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

        return ((BMapType) recordType).constraint;
    }

    private BType checkRecLiteralKeyExpr(BLangExpression keyExpr, RecordKind recKind) {
        // If the key is not at identifier (i.e: varRef), check the expression
        if (keyExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return checkExpr(keyExpr, this.env, symTable.stringType);
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

        return checkExpr(indexExpr, this.env, symTable.stringType);
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
        checkExpr(startTagName, xmlElementEnv, symTable.stringType);
        BLangExpression endTagName = (BLangExpression) bLangXMLElementLiteral.endTagName;
        if (endTagName != null) {
            checkExpr(endTagName, xmlElementEnv, symTable.stringType);
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
            BType exprType = checkExpr(expr, xmlElementEnv);
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
            binaryExpressionNode.type = opSymbol.type.getReturnType();
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

    private BType checkUnNamedTransformerInvocation(BLangTypeConversionExpr conversionExpr, BType sourceType,
                                                    BType targetType) {
        BType actualType = symTable.errType;

        // Check whether a transformer is available for the two types
        BSymbol symbol = symResolver.resolveTransformer(env, sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            // check whether a casting is possible, to provide user a hint.
            BSymbol castSymbol = symResolver.resolveConversionOperator(sourceType, targetType);
            if (castSymbol == symTable.notFoundSymbol) {
                dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION, sourceType, targetType);
            } else {
                dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION_WITH_SUGGESTION, sourceType,
                        targetType);
            }
        } else {
            BTransformerSymbol transformerSymbol = (BTransformerSymbol) symbol;
            conversionExpr.conversionSymbol = transformerSymbol;
            actualType = transformerSymbol.type.getReturnType();
        }

        return actualType;
    }

    private BType checkNamedTransformerInvocation(BLangTypeConversionExpr conversionExpr, BType sourceType,
                                                  BType targetType) {
        BType actualType = symTable.errType;
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
            BType prevExpType = expType;
            expType = targetType;
            checkInvocationParamAndReturnType(transformerInvocation);
            expType = prevExpType;

            if (transformerInvocation.type != symTable.errType) {
                BInvokableType transformerSymType = (BInvokableType) transformerSymbol.type;
                transformerInvocation.type = transformerSymType.retType;
                actualType = conversionExpr.conversionSymbol.type.getReturnType();
            }
        }

        return actualType;
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
        typeAccessExpr.type = symTable.typeDesc;
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

        checkExpr(expr, this.env, symTable.noType);
        return expr.type;
    }

    private void validateStructInitalizer(DiagnosticPos pos) {
        BStructType bStructType = (BStructType) expType;
        BStructSymbol bStructSymbol = (BStructSymbol) bStructType.tsymbol;
        if (bStructSymbol.initializerFunc == null) {
            return;
        }

        boolean samePkg = this.env.enclPkg.symbol.pkgID == bStructSymbol.pkgID;
        if (!samePkg && Symbols.isPrivate(bStructSymbol.initializerFunc.symbol)) {
            dlog.error(pos, DiagnosticCode.ATTEMPT_CREATE_NON_PUBLIC_INITIALIZER);
        }
    }

    private BType getAccessExprFinalType(BLangAccessExpression accessExpr, BType actualType) {
        // Cache the actual type of the field. This will be used in desuagr phase to create safe navigation.
        accessExpr.childType = actualType;

        BUnionType unionType = new BUnionType(null, new LinkedHashSet<>(), false);
        if (actualType.tag == TypeTags.UNION) {
            unionType.memberTypes.addAll(((BUnionType) actualType).memberTypes);
        } else {
            unionType.memberTypes.add(actualType);
        }

        BType parentType = accessExpr.expr.type;
        if (parentType.isNullable() && actualType.tag != TypeTags.JSON) {
            unionType.memberTypes.add(symTable.nilType);
            unionType.setNullable(true);
        }

        if (accessExpr.safeNavigate && (parentType.tag == TypeTags.ERROR || (parentType.tag == TypeTags.UNION &&
                ((BUnionType) parentType).memberTypes.contains(symTable.errStructType)))) {
            unionType.memberTypes.add(symTable.errStructType);
        }

        if (!unionType.isNullable() && unionType.memberTypes.size() == 1) {
            return unionType.memberTypes.toArray(new BType[0])[0];
        }

        return unionType;
    }

    private BType checkFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName) {
        BType actualType = symTable.errType;
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
            case TypeTags.XML:
                if (fieldAccessExpr.lhsVar) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
                    break;
                }
                actualType = symTable.xmlType;
                break;
            case TypeTags.ERROR:
                // Do nothing
                break;
            default:
                dlog.error(fieldAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS,
                        varRefType);
        }

        return actualType;
    }

    private BType checkIndexAccessExpr(BLangIndexBasedAccess indexBasedAccessExpr, BType varRefType) {
        BLangExpression indexExpr = indexBasedAccessExpr.indexExpr;
        BType actualType = symTable.errType;
        BType indexExprType;
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                indexExprType = checkIndexExprForStructFieldAccess(indexExpr);
                if (indexExprType.tag == TypeTags.STRING) {
                    String fieldName = (String) ((BLangLiteral) indexExpr).value;
                    actualType = checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), varRefType);
                }
                break;
            case TypeTags.MAP:
                indexExprType = checkExpr(indexExpr, this.env, symTable.stringType);
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
                    indexExprType = checkExpr(indexExpr, this.env, symTable.noType);
                    if (indexExprType.tag != TypeTags.STRING && indexExprType.tag != TypeTags.INT) {
                        dlog.error(indexExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.stringType,
                                indexExprType);
                        break;
                    }
                }
                actualType = symTable.jsonType;
                break;
            case TypeTags.ARRAY:
                indexExprType = checkExpr(indexExpr, this.env, symTable.intType);
                if (indexExprType.tag == TypeTags.INT) {
                    actualType = ((BArrayType) varRefType).getElementType();
                }
                break;
            case TypeTags.XML:
                if (indexBasedAccessExpr.lhsVar) {
                    dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
                    break;
                }

                checkExpr(indexExpr, this.env);
                actualType = symTable.xmlType;
                break;
            case TypeTags.ERROR:
                // Do nothing
                break;
            default:
                dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEXING,
                        indexBasedAccessExpr.expr.type);
        }

        return actualType;
    }

    private BType getSafeType(BType type, boolean safeNavigate, DiagnosticPos pos) {
        if (type.tag != TypeTags.UNION) {
            return type;
        }

        // Extract the types without the error and null, and revisit access expression
        Set<BType> varRefMemberTypes = ((BUnionType) type).memberTypes;
        List<BType> lhsTypes;

        if (safeNavigate) {
            if (!varRefMemberTypes.contains(symTable.errStructType)) {
                dlog.error(pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
            }
            lhsTypes = varRefMemberTypes.stream().filter(memberType -> {
                return memberType != symTable.errStructType && memberType != symTable.nilType;
            }).collect(Collectors.toList());
        } else {
            lhsTypes = varRefMemberTypes.stream().filter(memberType -> {
                return memberType != symTable.nilType;
            }).collect(Collectors.toList());
        }

        if (lhsTypes.size() == 1) {
            type = lhsTypes.get(0);
        }

        return type;
    }
}
