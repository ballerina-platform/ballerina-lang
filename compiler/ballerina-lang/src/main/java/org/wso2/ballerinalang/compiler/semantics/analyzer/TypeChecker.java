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
import org.ballerinalang.model.elements.TableColumnFlag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.BLangBuiltInMethod;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BChannelType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKey;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangRecordKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MIN_VALUE;
import static org.wso2.ballerinalang.compiler.tree.BLangInvokableNode.DEFAULT_WORKER_NAME;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

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
    private boolean isTypeChecked;
    private TypeNarrower typeNarrower;

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
        this.typeNarrower = TypeNarrower.getInstance(context);
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
        if (expr.typeChecked) {
            return expr.type;
        }

        // TODO Check the possibility of using a try/finally here
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;
        this.env = env;
        this.diagCode = diagCode;
        this.expType = expType;
        this.isTypeChecked = true;

        expr.accept(this);

        expr.type = resultType;
        expr.typeChecked = isTypeChecked;
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;
        return resultType;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        // Get the type matching to the tag from the symbol table.
        BType literalType = symTable.getTypeFromTag(literalExpr.type.tag);

        Object literalValue = literalExpr.value;
        literalExpr.isJSONContext = types.isJSONContext(expType);

        if (literalType.tag == TypeTags.INT) {
            if (expType.tag == TypeTags.FLOAT) {
                literalType = symTable.floatType;
                literalExpr.value = ((Long) literalValue).doubleValue();
            } else if (expType.tag == TypeTags.DECIMAL) {
                literalType = symTable.decimalType;
                literalExpr.value = String.valueOf(literalValue);
            } else if (expType.tag == TypeTags.BYTE) {
                if (!isByteLiteralValue((Long) literalValue)) {
                    dlog.error(literalExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, literalType);
                    resultType = symTable.semanticError;
                    return;
                }
                literalType = symTable.byteType;
                literalExpr.value = ((Long) literalValue).byteValue();
            }
        }

        // check whether this is a byte array
        if (literalExpr.type.tag == TypeTags.BYTE_ARRAY) {
            literalType = new BArrayType(symTable.byteType);
        }

        // Check whether this belongs to decimal type or float type
        if (literalType.tag == TypeTags.FLOAT) {
            if (expType.tag == TypeTags.DECIMAL) {
                literalType = symTable.decimalType;
                literalExpr.value = String.valueOf(literalValue);
            } else if (expType.tag == TypeTags.FLOAT) {
                literalExpr.value = Double.parseDouble(String.valueOf(literalValue));
            }
        }

        if (this.expType.tag == TypeTags.FINITE) {
            BFiniteType expType = (BFiniteType) this.expType;
            boolean foundMember = types.isAssignableToFiniteType(expType, literalExpr);
            if (foundMember) {
                types.setImplicitCastExpr(literalExpr, literalType, this.expType);
                resultType = literalType;
                return;
            }
        } else if (this.expType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) this.expType;
            boolean foundMember = unionType.memberTypes
                    .stream()
                    .anyMatch(memberType -> types.isAssignableToFiniteType(memberType, literalExpr));
            if (foundMember) {
                types.setImplicitCastExpr(literalExpr, literalType, this.expType);
                resultType = literalType;
                return;
            }
        }

        resultType = types.checkType(literalExpr, literalType, expType);
    }

    private static boolean isByteLiteralValue(Long longObject) {
        return (longObject.intValue() >= BBYTE_MIN_VALUE && longObject.intValue() <= BBYTE_MAX_VALUE);
    }

    public void visit(BLangTableLiteral tableLiteral) {
        if (expType.tag == symTable.semanticError.tag) {
            return;
        }
        BType tableConstraint = ((BTableType) expType).getConstraint();
        if (tableConstraint.tag == TypeTags.NONE) {
            dlog.error(tableLiteral.pos, DiagnosticCode.TABLE_CANNOT_BE_CREATED_WITHOUT_CONSTRAINT);
            return;
        }
        validateTableColumns(tableConstraint, tableLiteral);
        checkExprs(tableLiteral.tableDataRows, this.env, tableConstraint);
        resultType = types.checkType(tableLiteral, expType, symTable.noType);
    }

    private void validateTableColumns(BType tableConstraint, BLangTableLiteral tableLiteral) {
        if (tableConstraint.tag != TypeTags.SEMANTIC_ERROR) {
            List<String> columnNames = new ArrayList<>();
            for (BField field : ((BRecordType) tableConstraint).fields) {
                columnNames.add(field.getName().getValue());
                //Check for valid column types
                if (!(field.type.tag == TypeTags.INT || field.type.tag == TypeTags.STRING ||
                        field.type.tag == TypeTags.FLOAT || field.type.tag == TypeTags.DECIMAL ||
                        field.type.tag == TypeTags.XML || field.type.tag == TypeTags.JSON ||
                        field.type.tag == TypeTags.BOOLEAN || field.type.tag == TypeTags.ARRAY)) {
                    dlog.error(tableLiteral.pos, DiagnosticCode.FIELD_NOT_ALLOWED_WITH_TABLE_COLUMN,
                            field.name.value, field.type);
                }
                //Check for valid array types as columns
                if (field.type.tag == TypeTags.ARRAY) {
                    BType arrayType = ((BArrayType) field.type).eType;
                    if (!(arrayType.tag == TypeTags.INT || arrayType.tag == TypeTags.FLOAT ||
                            arrayType.tag == TypeTags.DECIMAL || arrayType.tag == TypeTags.STRING ||
                            arrayType.tag == TypeTags.BOOLEAN || arrayType.tag == TypeTags.BYTE)) {
                        dlog.error(tableLiteral.pos, DiagnosticCode.FIELD_NOT_ALLOWED_WITH_TABLE_COLUMN,
                                field.name.value, field.type);
                    }
                }
            }
            for (BLangTableLiteral.BLangTableColumn column : tableLiteral.columns) {
                boolean contains = columnNames.contains(column.columnName);
                if (!contains) {
                    dlog.error(column.pos, DiagnosticCode.UNDEFINED_TABLE_COLUMN, column.columnName, tableConstraint);
                }
                //Check for valid primary key column types
                if (column.flagSet.contains(TableColumnFlag.PRIMARYKEY)) {
                    for (BField field : ((BRecordType) tableConstraint).fields) {
                        if (field.name.value.equals(column.columnName)) {
                            if (!(field.type.tag == TypeTags.INT || field.type.tag == TypeTags.STRING)) {
                                dlog.error(column.pos, DiagnosticCode.TYPE_NOT_ALLOWED_WITH_PRIMARYKEY,
                                        column.columnName, field.type);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public void visit(BLangArrayLiteral arrayLiteral) {
        // Check whether the expected type is an array type
        // var a = []; and var a = [1,2,3,4]; are illegal statements, because we cannot infer the type here.
        BType actualType = symTable.semanticError;

        if (expType.tag == TypeTags.ANY) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INVALID_ARRAY_LITERAL, expType);
            resultType = symTable.semanticError;
            return;
        }

        int expTypeTag = expType.tag;
        if (expTypeTag == TypeTags.JSON) {
            checkExprs(arrayLiteral.exprs, this.env, expType);
            actualType = expType;

        } else if (expTypeTag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) expType;
            if (arrayType.state == BArrayState.OPEN_SEALED) {
                arrayType.size = arrayLiteral.exprs.size();
                arrayType.state = BArrayState.CLOSED_SEALED;
            } else if (arrayType.state != BArrayState.UNSEALED && arrayType.size != arrayLiteral.exprs.size()) {
                dlog.error(arrayLiteral.pos,
                        DiagnosticCode.MISMATCHING_ARRAY_LITERAL_VALUES, arrayType.size, arrayLiteral.exprs.size());
                resultType = symTable.semanticError;
                return;
            }
            checkExprs(arrayLiteral.exprs, this.env, arrayType.eType);
            actualType = arrayType;

        } else if (expTypeTag == TypeTags.UNION) {
            Set<BType> expTypes = ((BUnionType) expType).memberTypes;
            List<BArrayType> matchedTypeList = expTypes.stream()
                    .filter(type -> type.tag == TypeTags.ARRAY)
                    .map(BArrayType.class::cast)
                    .collect(Collectors.toList());

            if (matchedTypeList.isEmpty()) {
                dlog.error(arrayLiteral.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, actualType);
            } else if (matchedTypeList.size() == 1) {
                // If only one type in the union is an array, use that as the expected type
                actualType = matchedTypeList.get(0);
                checkExprs(arrayLiteral.exprs, this.env, ((BArrayType) actualType).eType);
            } else {
                // If more than one array type, visit the literal to get its type and use that type to filter the
                // compatible array types in the union
                actualType = checkArrayLiteralExpr(arrayLiteral);
            }
        } else if (expTypeTag != TypeTags.SEMANTIC_ERROR) {
            actualType = checkArrayLiteralExpr(arrayLiteral);
        }

        resultType = types.checkType(arrayLiteral, actualType, expType);
    }

    private BType checkArrayLiteralExpr(BLangArrayLiteral arrayLiteral) {
        List<BType> resTypes = checkExprs(arrayLiteral.exprs, this.env, symTable.noType);
        Set<BType> arrayLitExprTypeSet = new LinkedHashSet<>(resTypes);
        BType[] uniqueExprTypes = arrayLitExprTypeSet.toArray(new BType[0]);
        BType arrayLiteralType;
        if (uniqueExprTypes.length == 0) {
            arrayLiteralType = symTable.anyType;
        } else if (uniqueExprTypes.length == 1) {
            arrayLiteralType = resTypes.get(0);
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
            arrayLiteralType = superType;
        }
        BType actualType = new BArrayType(arrayLiteralType, null, arrayLiteral.exprs.size(), BArrayState.UNSEALED);

        List<BType> arrayCompatibleType = getArrayCompatibleTypes(expType, actualType);

        if (arrayCompatibleType.isEmpty()) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, actualType);
        } else if (arrayCompatibleType.size() > 1) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.AMBIGUOUS_TYPES, expType);
        } else if (arrayCompatibleType.get(0).tag == TypeTags.ANY) {
            dlog.error(arrayLiteral.pos, DiagnosticCode.INVALID_ARRAY_LITERAL, expType);
        } else if (arrayCompatibleType.get(0).tag == TypeTags.ARRAY) {
            checkExprs(arrayLiteral.exprs, this.env, ((BArrayType) arrayCompatibleType.get(0)).eType);
        }
        return actualType;
    }

    public void visit(BLangRecordLiteral recordLiteral) {
        BType actualType = symTable.semanticError;
        int expTypeTag = expType.tag;
        BType originalExpType = expType;
        if (expTypeTag == TypeTags.NONE || expTypeTag == TypeTags.ANY) {
            // Change the expected type to map,
            expType = symTable.mapType;
        }
        if (expTypeTag == TypeTags.ANY || expTypeTag == TypeTags.ANYDATA || expTypeTag == TypeTags.OBJECT) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_RECORD_LITERAL, originalExpType);
            resultType = symTable.semanticError;
            return;
        }

        List<BType> matchedTypeList = getRecordCompatibleType(expType, recordLiteral);

        if (matchedTypeList.isEmpty()) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expType);
            recordLiteral.keyValuePairs
                    .forEach(keyValuePair -> checkRecLiteralKeyValue(keyValuePair, symTable.errorType));
        } else if (matchedTypeList.size() > 1) {
            dlog.error(recordLiteral.pos, DiagnosticCode.AMBIGUOUS_TYPES, expType);
            recordLiteral.keyValuePairs
                    .forEach(keyValuePair -> checkRecLiteralKeyValue(keyValuePair, symTable.errorType));
        } else {
            recordLiteral.keyValuePairs
                    .forEach(keyValuePair -> checkRecLiteralKeyValue(keyValuePair, matchedTypeList.get(0)));
            actualType = matchedTypeList.get(0);
        }

        resultType = types.checkType(recordLiteral, actualType, expType);

        // If the record literal is of record type and types are validated for the fields, check if there are any
        // required fields missing.
        if (recordLiteral.type.tag == TypeTags.RECORD) {
            checkMissingRequiredFields((BRecordType) recordLiteral.type, recordLiteral.keyValuePairs,
                    recordLiteral.pos);
        }
    }

    private List<BType> getRecordCompatibleType(BType bType, BLangRecordLiteral recordLiteral) {

        if (bType.tag == TypeTags.UNION) {
            Set<BType> expTypes = ((BUnionType) bType).memberTypes;
            return expTypes.stream()
                    .filter(type -> type.tag == TypeTags.JSON ||
                            type.tag == TypeTags.MAP ||
                            (type.tag == TypeTags.RECORD && !((BRecordType) type).sealed) ||
                            (type.tag == TypeTags.RECORD
                                    && ((BRecordType) type).sealed
                                    && isCompatibleClosedRecordLiteral((BRecordType) type, recordLiteral)))
                    .collect(Collectors.toList());
        } else {
            switch (expType.tag) {
                case TypeTags.JSON:
                case TypeTags.MAP:
                case TypeTags.RECORD:
                    return new ArrayList<>(Collections.singleton(expType));
                default:
                    return Collections.emptyList();
            }
        }
    }

    private boolean isCompatibleClosedRecordLiteral(BRecordType bRecordType, BLangRecordLiteral recordLiteral) {
        if (!hasRequiredRecordFields(recordLiteral.getKeyValuePairs(), bRecordType)) {
            return false;
        }

        for (BLangRecordKeyValue literalKeyValuePair : recordLiteral.getKeyValuePairs()) {
            boolean matched = false;
            for (BField field : bRecordType.getFields()) {
                matched = ((BLangSimpleVarRef) literalKeyValuePair.getKey()).variableName.value
                        .equals(field.getName().getValue());
                if (matched) {
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
    }

    private void checkMissingRequiredFields(BRecordType type, List<BLangRecordKeyValue> keyValuePairs,
                                            DiagnosticPos pos) {
        type.fields.forEach(field -> {
            // Check if `field` is explicitly assigned a value in the record literal
            boolean hasField = keyValuePairs.stream()
                    .filter(keyVal -> keyVal.key.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF)
                    .anyMatch(keyVal -> field.name.value
                            .equals(((BLangSimpleVarRef) keyVal.key.expr).variableName.value));

            // If a required field is missing, it's a compile error
            if (!hasField && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                dlog.error(pos, DiagnosticCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
            }
        });
    }

    private boolean hasRequiredRecordFields(List<BLangRecordKeyValue> keyValuePairs, BRecordType targetRecType) {
        for (BField field : targetRecType.fields) {
            boolean hasField = keyValuePairs.stream()
                    .filter(keyVal -> keyVal.key.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF)
                    .anyMatch(keyVal -> field.name.value
                            .equals(((BLangSimpleVarRef) keyVal.key.expr).variableName.value));

            if (!hasField && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                return false;
            }
        }
        return true;
    }

    private List<BType> getArrayCompatibleTypes(BType expType, BType actualType) {
        Set<BType> expTypes =
                expType.tag == TypeTags.UNION ? ((BUnionType) expType).memberTypes : new LinkedHashSet<BType>() {
                    {
                        add(expType);
                    }
                };

        return expTypes.stream()
                .filter(type -> types.isAssignable(actualType, type) ||
                        type.tag == TypeTags.NONE ||
                        type.tag == TypeTags.ANY)
                .collect(Collectors.toList());
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        if (workerFlushExpr.workerIdentifier != null) {
            String workerName = workerFlushExpr.workerIdentifier.getValue();
            if (!this.workerExists(this.env, workerName)) {
                this.dlog.error(workerFlushExpr.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
            }
        }
        BType actualType = new BUnionType(null, new LinkedHashSet<BType>() {
            {
                add(symTable.nilType);
                add(symTable.errorType);
            }
        }, false);
        resultType = types.checkType(workerFlushExpr, actualType, expType);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(syncSendExpr.workerIdentifier),
                                                  SymTag.VARIABLE);

        if (symTable.notFoundSymbol.equals(symbol)) {
            syncSendExpr.workerType = symTable.semanticError;
        } else {
            syncSendExpr.workerType = symbol.type;
        }

        // TODO Need to remove this cached env
        syncSendExpr.env = this.env;
        checkExpr(syncSendExpr.expr, this.env);

        // Validate if the send expression type is anydata
        if (!types.isAnydata(syncSendExpr.expr.type)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticCode.INVALID_TYPE_FOR_SEND, syncSendExpr.expr.type);
        }

        String workerName = syncSendExpr.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }

        if (expType == symTable.noType) {
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
            memberTypes.add(symTable.errorType);
            memberTypes.add(symTable.nilType);
            resultType = new BUnionType(null, memberTypes, true);
        } else {
            resultType = expType;
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveExpr) {
        BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(workerReceiveExpr.workerIdentifier),
                                                  SymTag.VARIABLE);

        // TODO Need to remove this cached env
        workerReceiveExpr.env = this.env;
        if (workerReceiveExpr.isChannel || symbol.getType().tag == TypeTags.CHANNEL) {
            visitChannelReceive(workerReceiveExpr, symbol);
            return;
        }

        if (symTable.notFoundSymbol.equals(symbol)) {
            workerReceiveExpr.workerType = symTable.semanticError;
        } else {
            workerReceiveExpr.workerType = symbol.type;
        }
        // The receive expression cannot be assigned to var, since we cannot infer the type.
        if (symTable.noType == this.expType) {
            this.dlog.error(workerReceiveExpr.pos, DiagnosticCode.INVALID_USAGE_OF_RECEIVE_EXPRESSION);
        }
        // We cannot predict the type of the receive expression as it depends on the type of the data sent by the other
        // worker/channel. Since receive is an expression now we infer the type of it from the lhs of the statement.
        workerReceiveExpr.type = this.expType;
        resultType = this.expType;
    }

    private void visitChannelReceive(BLangWorkerReceive workerReceiveNode, BSymbol symbol) {
        workerReceiveNode.isChannel = true;
        if (symbol == null) {
            symbol = symResolver.lookupSymbol(env, names.fromString(workerReceiveNode.getWorkerName().getValue()),
                                              SymTag.VARIABLE);
        }

        if (symTable.notFoundSymbol.equals(symbol)) {
            dlog.error(workerReceiveNode.pos, DiagnosticCode.UNDEFINED_SYMBOL, workerReceiveNode.workerIdentifier);
            return;
        }

        if (TypeTags.CHANNEL != symbol.type.tag) {
            dlog.error(workerReceiveNode.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.channelType, symbol.type);
            return;
        }

        BType constraint = ((BChannelType) symbol.type).constraint;
        if (this.expType.tag != constraint.tag) {
            dlog.error(workerReceiveNode.pos, DiagnosticCode.INCOMPATIBLE_TYPES, constraint, this.expType);
            return;
        }

        if (workerReceiveNode.keyExpr != null) {
            checkExpr(workerReceiveNode.keyExpr, env);
        }

        // We cannot predict the type of the receive expression as it depends on the type of the data sent by the other
        // worker/channel. Since receive is an expression now we infer the type of it from the lhs of the statement.
        resultType = this.expType;
    }

    private boolean workerExists(SymbolEnv env, String workerName) {
        //TODO: move this method to CodeAnalyzer
        if (workerName.equals(DEFAULT_WORKER_NAME)) {
           return true;
        }
        BSymbol symbol = this.symResolver.lookupSymbol(env, new Name(workerName), SymTag.VARIABLE);
        return symbol != this.symTable.notFoundSymbol &&
               symbol.type.tag == TypeTags.FUTURE &&
               ((BFutureType) symbol.type).workerDerivative;
    }

    public void visit(BLangSimpleVarRef varRefExpr) {
        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        Name varName = names.fromIdNode(varRefExpr.variableName);
        if (varName == Names.IGNORE) {
            if (varRefExpr.lhsVar) {
                varRefExpr.type = this.symTable.noType;
            } else {
                varRefExpr.type = this.symTable.semanticError;
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
            // if no symbol, check same for object attached function
            if (symbol == symTable.notFoundSymbol && env.enclType != null) {
                Name objFuncName = names.fromString(Symbols
                        .getAttachedFuncSymbolName(env.enclType.type.tsymbol.name.value, varName.value));
                symbol = symResolver.resolveStructField(varRefExpr.pos, env, objFuncName,
                        env.enclType.type.tsymbol);
            }
            if ((symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) {
                BVarSymbol varSym = (BVarSymbol) symbol;
                checkSefReferences(varRefExpr.pos, env, varSym);
                varRefExpr.symbol = varSym;
                actualType = varSym.type;
                BLangInvokableNode encInvokable = env.enclInvokable;
                if (encInvokable != null && encInvokable.flagSet.contains(Flag.LAMBDA) &&
                        !(symbol.owner instanceof BPackageSymbol)) {
                    SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
                    BSymbol closureVarSymbol = symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name,
                            SymTag.VARIABLE_NAME);
                    if (closureVarSymbol != symTable.notFoundSymbol &&
                            !isFunctionArgument(closureVarSymbol, env.enclInvokable.requiredParams)) {
                        ((BLangFunction) env.enclInvokable).closureVarSymbols.add((BVarSymbol) closureVarSymbol);
                    }
                }
                if (env.node.getKind() == NodeKind.ARROW_EXPR && !(symbol.owner instanceof BPackageSymbol)) {
                    // The owner of the variable ref should be an invokable symbol.
                    // It's set here because the arrow expression changes to an invokable only at desugar
                    // and is not an invokable at this phase.
                    symbol.owner = Symbols.createInvokableSymbol(SymTag.FUNCTION, 0, null,
                            env.enclPkg.packageID, null, symbol.owner);
                    SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
                    BSymbol closureVarSymbol = symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name,
                            SymTag.VARIABLE_NAME);
                    if (closureVarSymbol != symTable.notFoundSymbol &&
                            !isFunctionArgument(closureVarSymbol, ((BLangArrowFunction) env.node).params)) {
                        ((BLangArrowFunction) env.node).closureVarSymbols.add((BVarSymbol) closureVarSymbol);
                    }
                }
            } else if ((symbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                actualType = symTable.typeDesc;
                varRefExpr.symbol = symbol;
            } else if ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
                varRefExpr.symbol = symbol;
                if (types.isAssignable(symbol.type, expType)) {
                    actualType = symbol.type;
                } else {
                    actualType = ((BConstantSymbol) symbol).literalValueType;
                }
            } else {
                dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_SYMBOL, varName.toString());
            }
        }

        // Check type compatibility
        if (expType.tag == TypeTags.ARRAY && isArrayOpenSealedType((BArrayType) expType)) {
            dlog.error(varRefExpr.pos, DiagnosticCode.SEALED_ARRAY_TYPE_CAN_NOT_INFER_SIZE);
            return;

        }
        resultType = types.checkType(varRefExpr, actualType, expType);
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        List<BField> fields = new ArrayList<>();
        BRecordTypeSymbol recordSymbol = Symbols.createRecordSymbol(0, Names.EMPTY, env.enclPkg.symbol.pkgID,
                null, env.scope.owner);
        boolean unresolvedReference = false;
        for (BLangRecordVarRef.BLangRecordVarRefKeyValue recordRefField : varRefExpr.recordRefFields) {
            ((BLangVariableReference) recordRefField.variableReference).lhsVar = true;
            checkExpr(recordRefField.variableReference, env);
            BVarSymbol bVarSymbol = getVarSymbolForRecordVariableReference(recordRefField.variableReference);
            if (bVarSymbol == null) {
                unresolvedReference = true;
                continue;
            }
            fields.add(new BField(names.fromIdNode(recordRefField.variableName),
                    new BVarSymbol(0, names.fromIdNode(recordRefField.variableName), env.enclPkg.symbol.pkgID,
                            bVarSymbol.type, recordSymbol)));
        }

        if (varRefExpr.restParam != null) {
            BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
            checkExpr(restParam, env);
            BVarSymbol bVarSymbol = getVarSymbolForRecordVariableReference(restParam);
            unresolvedReference = (bVarSymbol == null);
        }

        if (unresolvedReference) {
            resultType = symTable.semanticError;
            return;
        }

        BRecordType bRecordType = new BRecordType(recordSymbol);
        bRecordType.fields = fields;
        recordSymbol.type = bRecordType;
        varRefExpr.symbol = new BVarSymbol(0, Names.EMPTY, env.enclPkg.symbol.pkgID, bRecordType, env.scope.owner);

        if (varRefExpr.isClosed) {
            bRecordType.sealed = true;
        } else {
            bRecordType.restFieldType = symTable.mapType;
        }

        resultType = bRecordType;
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        // TODO: Complete
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        List<BType> results = new ArrayList<>();
        for (int i = 0; i < varRefExpr.expressions.size(); i++) {
            ((BLangVariableReference) varRefExpr.expressions.get(i)).lhsVar = true;
            results.add(checkExpr(varRefExpr.expressions.get(i), env, symTable.noType));
        }
        BType actualType = new BTupleType(results);
        resultType = types.checkType(varRefExpr, actualType, expType);
    }

    /**
     * This method will recursively check if a multidimensional array has at least one open sealed dimension.
     *
     * @param arrayType array to check if open sealed
     * @return true if at least one dimension is open sealed
     */
    public boolean isArrayOpenSealedType(BArrayType arrayType) {
        if (arrayType.state == BArrayState.OPEN_SEALED) {
            return true;
        }
        if (arrayType.eType.tag == TypeTags.ARRAY) {
            return isArrayOpenSealedType((BArrayType) arrayType.eType);
        }
        return false;
    }

    /**
     * This method will recursively traverse and find the symbol environment of a lambda node (which is given as the
     * enclosing invokable node) which is needed to lookup closure variables. The variable lookup will start from the
     * enclosing invokable node's environment, which are outside of the scope of a lambda function.
     */
    private SymbolEnv findEnclosingInvokableEnv(SymbolEnv env, BLangInvokableNode encInvokable) {
        if (env.enclEnv.node != null && env.enclEnv.node.getKind() == NodeKind.ARROW_EXPR) {
            // if enclosing env's node is arrow expression
            return env.enclEnv;
        }
        if (env.enclInvokable != null && env.enclInvokable == encInvokable) {
            return findEnclosingInvokableEnv(env.enclEnv, encInvokable);
        }
        return env;
    }

    private boolean isFunctionArgument(BSymbol symbol, List<BLangSimpleVariable> params) {
        return params.stream().anyMatch(param -> (param.symbol.name.equals(symbol.name) &&
                param.type.tag == symbol.type.tag));
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // First analyze the variable reference expression.
        ((BLangVariableReference) fieldAccessExpr.expr).lhsVar = fieldAccessExpr.lhsVar;
        BType varRefType = getTypeOfExprInFieldAccess(fieldAccessExpr.expr);

        // Accessing all fields using * is only supported for XML.
        if (fieldAccessExpr.fieldKind == FieldKind.ALL && varRefType.tag != TypeTags.XML) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_GET_ALL_FIELDS, varRefType);
        }

        // error lifting on lhs is not supported
        if (fieldAccessExpr.lhsVar && fieldAccessExpr.safeNavigate) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.INVALID_ERROR_LIFTING_ON_LHS);
            resultType = symTable.semanticError;
            return;
        }

        if (isSafeNavigable(fieldAccessExpr, varRefType)) {
            varRefType = getSafeType(varRefType, fieldAccessExpr);
        }

        Name fieldName = names.fromIdNode(fieldAccessExpr.field);
        BType actualType = checkFieldAccessExpr(fieldAccessExpr, varRefType, fieldName);

        // If this is on lhs, no need to do type checking further. And null/error
        // will not propagate from parent expressions
        if (fieldAccessExpr.lhsVar) {
            fieldAccessExpr.originalType = actualType;
            fieldAccessExpr.type = actualType;
            resultType = actualType;
            return;
        }

        // Get the effective types of the expression. If there are errors/nill propagating from parent
        // expressions, then the effective type will include those as well.
        actualType = getAccessExprFinalType(fieldAccessExpr, actualType);
        resultType = types.checkType(fieldAccessExpr, actualType, this.expType);
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        // First analyze the variable reference expression.
        ((BLangVariableReference) indexBasedAccessExpr.expr).lhsVar = indexBasedAccessExpr.lhsVar;
        checkExpr(indexBasedAccessExpr.expr, this.env, symTable.noType);

        BType varRefType = indexBasedAccessExpr.expr.type;

        if (isSafeNavigable(indexBasedAccessExpr, varRefType)) {
            varRefType = getSafeType(varRefType, indexBasedAccessExpr);
        }

        BType actualType = checkIndexAccessExpr(indexBasedAccessExpr, varRefType);

        // If this is on lhs, no need to do type checking further. And null/error
        // will not propagate from parent expressions
        if (indexBasedAccessExpr.lhsVar) {
            indexBasedAccessExpr.originalType = actualType;
            indexBasedAccessExpr.type = actualType;
            resultType = actualType;
            return;
        }

        // Get the effective types of the expression. If there are errors/nil propagating from parent
        // expressions, then the effective type will include those as well.
        actualType = getAccessExprFinalType(indexBasedAccessExpr, actualType);
        this.resultType = this.types.checkType(indexBasedAccessExpr, actualType, this.expType);
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

        BType varRefType = iExpr.expr.type;

        if (isSafeNavigable(iExpr, varRefType)) {
            varRefType = getSafeType(varRefType, iExpr);
        }

        BLangBuiltInMethod builtInFunction = BLangBuiltInMethod.getFromString(iExpr.name.value);
        // Returns if the function is a builtin function
        if (BLangBuiltInMethod.UNDEFINED != builtInFunction && builtInFunction.isExternal() &&
                checkBuiltinFunctionInvocation(iExpr, builtInFunction, varRefType) != symTable.notFoundSymbol) {
            return;
        }

        switch (varRefType.tag) {
            case TypeTags.OBJECT:
                // Invoking a function bound to an object
                // First check whether there exist a function with this name
                // Then perform arg and param matching
                checkObjectFunctionInvocationExpr(iExpr, (BObjectType) varRefType);
                break;
            case TypeTags.RECORD:
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.XML:
                checkFunctionInvocationExpr(iExpr, varRefType);
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
            case TypeTags.SEMANTIC_ERROR:
                break;
            case TypeTags.INTERMEDIATE_COLLECTION:
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_INVOCATION_WITH_NAME, iExpr.name,
                        iExpr.expr.type);
                resultType = symTable.semanticError;
                break;
            default:
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_INVOCATION, iExpr.expr.type);
                resultType = symTable.semanticError;
                break;
        }

        if (iExpr.symbol != null) {
            iExpr.originalType = ((BInvokableSymbol) iExpr.symbol).type.getReturnType();
        } else {
            iExpr.originalType = iExpr.type;
        }
    }

    public void visit(BLangTypeInit cIExpr) {
        if ((expType.tag == TypeTags.ANY && cIExpr.userDefinedType == null) || expType.tag == TypeTags.RECORD) {
            dlog.error(cIExpr.pos, DiagnosticCode.INVALID_TYPE_NEW_LITERAL, expType);
            resultType = symTable.semanticError;
            return;
        }

        BType actualType;
        if (cIExpr.userDefinedType != null) {
            actualType = symResolver.resolveTypeNode(cIExpr.userDefinedType, env);
        } else {
            actualType = expType;
        }

        if (actualType == symTable.semanticError) {
            //TODO dlog error?
            resultType = symTable.semanticError;
            return;
        }

        switch (actualType.tag) {
            case TypeTags.OBJECT:
                if ((actualType.tsymbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
                    dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INITIALIZE_ABSTRACT_OBJECT, actualType.tsymbol);
                    cIExpr.initInvocation.argExprs.forEach(expr -> checkExpr(expr, env, symTable.noType));
                    resultType = symTable.semanticError;
                    return;
                }

                if (((BObjectTypeSymbol) actualType.tsymbol).initializerFunc != null) {
                    cIExpr.initInvocation.symbol = ((BObjectTypeSymbol) actualType.tsymbol).initializerFunc.symbol;
                    checkInvocationParam(cIExpr.initInvocation);
                } else if (cIExpr.initInvocation.argExprs.size() > 0) {
                    // If the initializerFunc is null then this is a default constructor invocation. Hence should not
                    // pass any arguments.
                    dlog.error(cIExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, cIExpr.initInvocation.exprSymbol);
                    cIExpr.initInvocation.argExprs.forEach(expr -> checkExpr(expr, env, symTable.noType));
                    resultType = symTable.semanticError;
                    return;
                }
                break;
            case TypeTags.STREAM:
            case TypeTags.CHANNEL:
                if (cIExpr.initInvocation.argExprs.size() > 0) {
                    dlog.error(cIExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, cIExpr.initInvocation.name);
                    resultType = symTable.semanticError;
                    return;
                }
                break;
            default:
                dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INFER_OBJECT_TYPE_FROM_LHS, actualType);
                resultType = symTable.semanticError;
                return;
        }

        cIExpr.initInvocation.type = symTable.nilType;
        resultType = types.checkType(cIExpr, actualType, expType);
    }

    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        switch (expType.tag) {
            case TypeTags.RECORD:
                checkTypesForRecords(waitForAllExpr);
                break;
            case TypeTags.MAP:
                checkTypesForMap(waitForAllExpr.keyValuePairs, ((BMapType) expType).constraint);
                LinkedHashSet<BType> memberTypesForMap = collectWaitExprTypes(waitForAllExpr.keyValuePairs);
                if (memberTypesForMap.size() == 1) {
                    resultType = new BMapType(TypeTags.MAP,
                            memberTypesForMap.iterator().next(), symTable.mapType.tsymbol);
                    break;
                }
                BUnionType constraintTypeForMap = new BUnionType(null, memberTypesForMap, false);
                resultType = new BMapType(TypeTags.MAP, constraintTypeForMap, symTable.mapType.tsymbol);
                break;
            case TypeTags.NONE:
            case TypeTags.ANY:
                checkTypesForMap(waitForAllExpr.keyValuePairs, expType);
                LinkedHashSet<BType> memberTypes = collectWaitExprTypes(waitForAllExpr.keyValuePairs);
                if (memberTypes.size() == 1) {
                    resultType = new BMapType(TypeTags.MAP, memberTypes.iterator().next(), symTable.mapType.tsymbol);
                    break;
                }
                BUnionType constraintType = new BUnionType(null, memberTypes, false);
                resultType = new BMapType(TypeTags.MAP, constraintType, symTable.mapType.tsymbol);
                break;
            default:
                dlog.error(waitForAllExpr.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expType);
                resultType = symTable.semanticError;
                break;
        }
        waitForAllExpr.type = resultType;

        if (resultType != null && resultType != symTable.semanticError) {
            types.setImplicitCastExpr(waitForAllExpr, waitForAllExpr.type, expType);
        }
    }

    private LinkedHashSet<BType> collectWaitExprTypes(List<BLangWaitForAllExpr.BLangWaitKeyValue> keyVals) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyVal : keyVals) {
            BType bType = keyVal.keyExpr != null ? keyVal.keyExpr.type : keyVal.valueExpr.type;
            if (bType.tag == TypeTags.UNION) {
                memberTypes = collectMemberTypes((BUnionType) bType, memberTypes);
            } else if (bType.tag == TypeTags.FUTURE) {
                memberTypes.add(((BFutureType) bType).constraint);
            } else {
                memberTypes.add(bType);
            }
        }
        return memberTypes;
    }

    private void checkTypesForMap(List<BLangWaitForAllExpr.BLangWaitKeyValue> keyValuePairs, BType expType) {
        keyValuePairs.forEach(keyVal -> checkWaitKeyValExpr(keyVal, expType));
    }

    private void checkTypesForRecords(BLangWaitForAllExpr waitExpr) {
        List<BLangWaitForAllExpr.BLangWaitKeyValue> rhsFields = waitExpr.getKeyValuePairs();
        Map<String, BType> lhsFields = new HashMap<>();
        ((BRecordType) expType).getFields().forEach(field -> lhsFields.put(field.name.value, field.type));

        // check if the record is sealed, if so check if the fields in wait collection is more than the fields expected
        // by the lhs record
        if (((BRecordType) expType).sealed && rhsFields.size() > lhsFields.size()) {
            dlog.error(waitExpr.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expType);
            resultType = symTable.semanticError;
            return;
        }

        for (BLangWaitForAllExpr.BLangWaitKeyValue keyVal : rhsFields) {
            String key = keyVal.key.value;
            if (!lhsFields.containsKey(key)) {
                // Check if the field is sealed if so you cannot have dynamic fields
                if (((BRecordType) expType).sealed) {
                    dlog.error(waitExpr.pos, DiagnosticCode.INVALID_FIELD_NAME_RECORD_LITERAL, key, expType);
                    resultType = symTable.semanticError;
                } else {
                    // Else if the record is an open record, then check if the rest field type matches the expression
                    BType restFieldType = ((BRecordType) expType).restFieldType;
                    checkWaitKeyValExpr(keyVal, restFieldType);
                }
            } else {
                checkWaitKeyValExpr(keyVal, lhsFields.get(key));
            }
        }
        // If the record literal is of record type and types are validated for the fields, check if there are any
        // required fields missing.
        checkMissingReqFieldsForWait(((BRecordType) expType), rhsFields, waitExpr.pos);

        if (symTable.semanticError != resultType) {
            resultType = expType;
        }
    }

    private void checkMissingReqFieldsForWait(BRecordType type, List<BLangWaitForAllExpr.BLangWaitKeyValue> keyValPairs,
                                              DiagnosticPos pos) {
        type.fields.forEach(field -> {
            // Check if `field` is explicitly assigned a value in the record literal
            boolean hasField = keyValPairs.stream().anyMatch(keyVal -> field.name.value.equals(keyVal.key.value));

            // If a required field is missing, it's a compile error
            if (!hasField && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                dlog.error(pos, DiagnosticCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
            }
        });
    }

    private void checkWaitKeyValExpr(BLangWaitForAllExpr.BLangWaitKeyValue keyVal, BType type) {
        BLangExpression expr;
        if (keyVal.keyExpr != null) {
            BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(keyVal.keyExpr.variableName),
                                                      SymTag.VARIABLE);
            keyVal.keyExpr.symbol = symbol;
            keyVal.keyExpr.type = symbol.type;
            expr = keyVal.keyExpr;
        } else {
            expr = keyVal.valueExpr;
        }
        BFutureType futureType = new BFutureType(TypeTags.FUTURE, type, null);
        checkExpr(expr, env, futureType);
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        BType condExprType = checkExpr(ternaryExpr.expr, env, this.symTable.booleanType);

        SymbolEnv thenEnv = typeNarrower.evaluateTruth(ternaryExpr.expr, ternaryExpr.thenExpr, env);
        BType thenType = checkExpr(ternaryExpr.thenExpr, thenEnv, expType);

        SymbolEnv elseEnv = typeNarrower.evaluateFalsity(ternaryExpr.expr, ternaryExpr.elseExpr, env);
        BType elseType = checkExpr(ternaryExpr.elseExpr, elseEnv, expType);

        if (condExprType == symTable.semanticError || thenType == symTable.semanticError ||
                elseType == symTable.semanticError) {
            resultType = symTable.semanticError;
        } else if (expType == symTable.noType) {
            if (types.isAssignable(elseType, thenType)) {
                resultType = thenType;
            } else if (types.isAssignable(thenType, elseType)) {
                resultType = elseType;
            } else {
                dlog.error(ternaryExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, thenType, elseType);
                resultType = symTable.semanticError;
            }
        } else {
            resultType = expType;
        }
    }

    public void visit(BLangWaitExpr waitExpr) {
        expType = new BFutureType(TypeTags.FUTURE, expType, null);
        checkExpr(waitExpr.getExpression(), env, expType);
        // Handle union types in lhs
        if (resultType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> memberTypes = collectMemberTypes((BUnionType) resultType, new LinkedHashSet<>());
            if (memberTypes.size() == 1) {
                resultType = memberTypes.toArray(new BType[0])[0];
            } else {
                resultType = new BUnionType(null, memberTypes, false);
            }
        } else if (resultType != symTable.semanticError) {
            // Handle other types except for semantic errors
            resultType = ((BFutureType) resultType).constraint;
        }
        waitExpr.type = resultType;

        if (resultType != null && resultType != symTable.semanticError) {
            types.setImplicitCastExpr(waitExpr, waitExpr.type, ((BFutureType) expType).constraint);
        }
    }

    private LinkedHashSet<BType> collectMemberTypes(BUnionType unionType, LinkedHashSet<BType> memberTypes) {
        for (BType memberType : unionType.memberTypes) {
            if (memberType.tag == TypeTags.UNION) {
                collectMemberTypes((BUnionType) memberType, memberTypes);
            } else if (memberType.tag == TypeTags.FUTURE) {
                memberTypes.add(((BFutureType) memberType).constraint);
            } else {
                memberTypes.add(memberType);
            }
        }
        return memberTypes;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        boolean firstVisit = trapExpr.expr.type == null;
        BType actualType;
        BType exprType = checkExpr(trapExpr.expr, env, expType);
        boolean definedWithVar = expType == symTable.noType;

        if (trapExpr.expr.getKind() == NodeKind.WORKER_RECEIVE) {
            if (firstVisit) {
                isTypeChecked = false;
                resultType = expType;
                return;
            } else {
                expType = trapExpr.type;
                exprType = trapExpr.expr.type;
            }
        }

        if (expType == symTable.semanticError) {
            actualType = symTable.semanticError;
        } else {
            LinkedHashSet<BType> resultTypes = new LinkedHashSet<>();
            if (exprType.tag == TypeTags.UNION) {
                resultTypes.addAll(((BUnionType) exprType).memberTypes);
            } else {
                resultTypes.add(exprType);
            }
            resultTypes.add(symTable.errorType);
            actualType = new BUnionType(null, resultTypes, resultTypes.contains(symTable.nilType));
        }

        resultType = types.checkType(trapExpr, actualType, expType);
        if (definedWithVar && resultType != null && resultType != symTable.semanticError) {
            types.setImplicitCastExpr(trapExpr.expr, trapExpr.expr.type, resultType);
        }
    }

    public void visit(BLangBinaryExpr binaryExpr) {
        // Bitwise operator should be applied for the future types in the wait expression
        if (expType.tag == TypeTags.FUTURE && binaryExpr.opKind == OperatorKind.BITWISE_OR) {
            BType lhsResultType = checkExpr(binaryExpr.lhsExpr, env, expType);
            BType rhsResultType = checkExpr(binaryExpr.rhsExpr, env, expType);
            // Return if both or atleast one of lhs and rhs types are errors
            if (lhsResultType == symTable.semanticError || rhsResultType == symTable.semanticError) {
                resultType = symTable.semanticError;
                return;
            }
            resultType = new BUnionType(null, new LinkedHashSet<BType>() {
                {
                    add(lhsResultType);
                    add(rhsResultType);
                }
            }, false);
            return;
        }

        SymbolEnv rhsExprEnv;
        BType lhsType = checkExpr(binaryExpr.lhsExpr, env);
        if (binaryExpr.opKind == OperatorKind.AND) {
            rhsExprEnv = typeNarrower.evaluateTruth(binaryExpr.lhsExpr, binaryExpr.rhsExpr, env);
        } else if (binaryExpr.opKind == OperatorKind.OR) {
            rhsExprEnv = typeNarrower.evaluateFalsity(binaryExpr.lhsExpr, binaryExpr.rhsExpr, env);
        } else {
            rhsExprEnv = env;
        }

        BType rhsType = checkExpr(binaryExpr.rhsExpr, rhsExprEnv);

        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        // Look up operator symbol if both rhs and lhs types are error types
        if (lhsType != symTable.semanticError && rhsType != symTable.semanticError) {
            BSymbol opSymbol = symResolver.resolveBinaryOperator(binaryExpr.opKind, lhsType, rhsType);

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBinaryEqualityForTypeSets(binaryExpr.opKind, lhsType, rhsType, binaryExpr);
            }

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
        BType actualType = symTable.semanticError;
        if (lhsType != symTable.semanticError) {
            if (lhsType.tag == TypeTags.UNION && lhsType.isNullable()) {
                BUnionType unionType = (BUnionType) lhsType;
                LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
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
        if (rhsReturnType == symTable.semanticError || lhsReturnType == symTable.semanticError) {
            resultType = symTable.semanticError;
        } else if (expType == symTable.noType) {
            if (types.isSameType(rhsReturnType, lhsReturnType)) {
                resultType = lhsReturnType;
            } else {
                dlog.error(elvisExpr.rhsExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, lhsReturnType, rhsReturnType);
                resultType = symTable.semanticError;
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
                // Infer type from lhs since lhs might be union
                // TODO: Need to fix with tuple casting
                BType expType = expTypes.get(i);
                BType actualType = checkExpr(bracedOrTupleExpr.expressions.get(i), env, expType);
                results.add(expType.tag != TypeTags.NONE ? expType : actualType);
            }
            resultType = new BTupleType(results);
            return;
        }

        List<BType> results = new ArrayList<>();
        if (expType.tag == TypeTags.TYPEDESC) {
            bracedOrTupleExpr.isTypedescExpr = true;
            for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
                results.add(checkExpr(bracedOrTupleExpr.expressions.get(i), env, symTable.noType));
            }
            List<BType> actualTypes = new ArrayList<>();
            for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
                final BLangExpression expr = bracedOrTupleExpr.expressions.get(i);
                if (expr.getKind() == NodeKind.TYPEDESC_EXPRESSION) {
                    actualTypes.add(((BLangTypedescExpr) expr).resolvedType);
                } else if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    actualTypes.add(((BLangSimpleVarRef) expr).symbol.type);
                } else {
                    actualTypes.add(results.get(i));
                }
            }
            if (actualTypes.size() == 1) {
                bracedOrTupleExpr.typedescType = actualTypes.get(0);
            } else {
                bracedOrTupleExpr.typedescType = new BTupleType(actualTypes);
            }
            resultType = symTable.typeDesc;
        } else if (bracedOrTupleExpr.expressions.size() > 1) {
            // This is a tuple.
            for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
                results.add(checkExpr(bracedOrTupleExpr.expressions.get(i), env, symTable.noType));
            }
            BType actualType = new BTupleType(results);

            if (expType.tag == TypeTags.ANY || expType.tag == TypeTags.ANYDATA) {
                dlog.error(bracedOrTupleExpr.pos, DiagnosticCode.INVALID_TUPLE_LITERAL, expType);
                resultType = symTable.semanticError;
                return;
            }

            List<BType> tupleCompatibleType = getArrayCompatibleTypes(expType, actualType);
            if (tupleCompatibleType.isEmpty()) {
                dlog.error(bracedOrTupleExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, actualType);
            } else if (tupleCompatibleType.size() > 1) {
                dlog.error(bracedOrTupleExpr.pos, DiagnosticCode.AMBIGUOUS_TYPES, expType);
            } else if (tupleCompatibleType.get(0).tag == TypeTags.ANY) {
                dlog.error(bracedOrTupleExpr.pos, DiagnosticCode.INVALID_TUPLE_LITERAL, expType);
            } else {
                resultType = types.checkType(bracedOrTupleExpr, actualType, expType);
            }
        } else {
            // This is a braced expression.
            bracedOrTupleExpr.isBracedExpr = true;
            resultType = checkExpr(bracedOrTupleExpr.expressions.get(0), env, expType);
        }
    }

    public void visit(BLangTypedescExpr accessExpr) {
        BType actualType = symTable.typeDesc;
        accessExpr.resolvedType = symResolver.resolveTypeNode(accessExpr.typeNode, env);
        resultType = types.checkType(accessExpr, actualType, expType);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        BType exprType;
        BType actualType = symTable.semanticError;
        if (OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
            exprType = checkExpr(unaryExpr.expr, env);
            if (exprType != symTable.semanticError) {
                actualType = exprType;
            }
        } else {
            exprType = checkExpr(unaryExpr.expr, env);
            if (exprType != symTable.semanticError) {
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
        BType actualType = symTable.semanticError;

        BType targetType = symResolver.resolveTypeNode(conversionExpr.typeNode, env);
        conversionExpr.targetType = targetType;
        BType expType = conversionExpr.expr.getKind() == NodeKind.RECORD_LITERAL_EXPR ? targetType : symTable.noType;
        BType sourceType = checkExpr(conversionExpr.expr, env, expType);

        if (targetType.tag == TypeTags.ERROR || targetType.tag == TypeTags.FUTURE) {
            dlog.error(conversionExpr.pos, DiagnosticCode.TYPE_ASSERTION_NOT_YET_SUPPORTED, targetType);
        } else {
            BSymbol symbol = symResolver.resolveTypeCastOrAssertionOperator(sourceType, targetType);

            if (symbol == symTable.notFoundSymbol) {
                dlog.error(conversionExpr.pos, DiagnosticCode.INVALID_EXPLICIT_TYPE_FOR_EXPRESSION, sourceType,
                           targetType);
            } else {
                BOperatorSymbol conversionSym = (BOperatorSymbol) symbol;
                conversionExpr.conversionSymbol = conversionSym;
                actualType = conversionSym.type.getReturnType();
            }
        }
        resultType = types.checkType(conversionExpr, actualType, this.expType);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.type = bLangLambdaFunction.function.symbol.type;
        // creating a copy of the env to visit the lambda function later
        bLangLambdaFunction.cachedEnv = env.createClone();
        env.enclPkg.lambdaFunctions.add(bLangLambdaFunction);
        resultType = types.checkType(bLangLambdaFunction, bLangLambdaFunction.type, expType);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        BType expectedType = expType;
        if (expectedType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) expectedType;
            BType invokableType = unionType.memberTypes.stream().filter(type -> type.tag == TypeTags.INVOKABLE)
                    .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                                if (list.size() != 1) {
                                    return null;
                                }
                                return list.get(0);
                            }
                    ));

            if (invokableType != null) {
                expectedType = invokableType;
            }
        }
        if (expectedType.tag != TypeTags.INVOKABLE) {
            dlog.error(bLangArrowFunction.pos, DiagnosticCode.ARROW_EXPRESSION_CANNOT_INFER_TYPE_FROM_LHS);
            resultType = symTable.semanticError;
            return;
        }

        BInvokableType expectedInvocation = (BInvokableType) expectedType;
        populateArrowExprParamTypes(bLangArrowFunction, expectedInvocation.paramTypes);
        bLangArrowFunction.expression.type = populateArrowExprReturn(bLangArrowFunction, expectedInvocation.retType);
        // if function return type is none, assign the inferred return type
        if (expectedInvocation.retType.tag == TypeTags.NONE) {
            expectedInvocation.retType = bLangArrowFunction.expression.type;
        }
        resultType = bLangArrowFunction.funcType = expectedInvocation;
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
            bLangXMLQName.type = symTable.semanticError;
            return;
        }

        BSymbol xmlnsSymbol = symResolver.lookupSymbol(env, names.fromIdNode(bLangXMLQName.prefix), SymTag.XMLNS);
        if (prefix.isEmpty() && xmlnsSymbol == symTable.notFoundSymbol) {
            return;
        }

        if (!prefix.isEmpty() && xmlnsSymbol == symTable.notFoundSymbol) {
            dlog.error(bLangXMLQName.pos, DiagnosticCode.UNDEFINED_SYMBOL, prefix);
            bLangXMLQName.type = symTable.semanticError;
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
                checkExpr(attribute, xmlElementEnv, symTable.noType);
            }
        });

        // Visit attributes.
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (attribute.name.getKind() != NodeKind.XML_QNAME
                    || !((BLangXMLQName) attribute.name).prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                checkExpr(attribute, xmlElementEnv, symTable.noType);
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
        BType actualType = symTable.semanticError;

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
        BType actualType = symTable.semanticError;
        int expTypeTag = expType.tag;

        if (expTypeTag == TypeTags.TABLE) {
            actualType = expType;
        } else if (expTypeTag != TypeTags.SEMANTIC_ERROR) {
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
        for (OrderByVariableNode orderByVariableNode : orderBy.getVariables()) {
            ((BLangOrderByVariable) orderByVariableNode).accept(this);
        }
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
        BLangExpression expression = (BLangExpression) orderByVariable.getVariableReference();
        expression.accept(this);
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

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        SymbolEnv matchExprEnv = SymbolEnv.createBlockEnv((BLangBlockStmt) TreeBuilder.createBlockNode(), env);
        checkExpr(bLangMatchExpression.expr, matchExprEnv);

        // Type check and resolve patterns and their expressions
        bLangMatchExpression.patternClauses.forEach(pattern -> {
            if (!pattern.variable.name.value.endsWith(Names.IGNORE.value)) {
                symbolEnter.defineNode(pattern.variable, matchExprEnv);
            }
            checkExpr(pattern.expr, matchExprEnv, expType);
            pattern.variable.type = symResolver.resolveTypeNode(pattern.variable.typeNode, matchExprEnv);
        });

        LinkedHashSet<BType> matchExprTypes = getMatchExpressionTypes(bLangMatchExpression);

        BType actualType;
        if (matchExprTypes.contains(symTable.semanticError)) {
            actualType = symTable.semanticError;
        } else if (matchExprTypes.size() == 1) {
            actualType = matchExprTypes.toArray(new BType[matchExprTypes.size()])[0];
        } else {
            actualType = new BUnionType(null, matchExprTypes, matchExprTypes.contains(symTable.nilType));
        }

        resultType = types.checkType(bLangMatchExpression, actualType, expType);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        boolean firstVisit = checkedExpr.expr.type == null;
        BType exprExpType;
        if (expType == symTable.noType) {
            exprExpType = symTable.noType;
        } else {
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
            boolean nillable;
            if (expType.tag == TypeTags.UNION) {
                nillable = expType.isNullable();
                memberTypes.addAll(((BUnionType) expType).memberTypes);
            } else {
                nillable = expType == symTable.nilType;
                memberTypes.add(expType);
            }
            memberTypes.add(symTable.errorType);
            exprExpType = new BUnionType(null, memberTypes, nillable);
        }

        BType exprType = checkExpr(checkedExpr.expr, env, exprExpType);
        if (checkedExpr.expr.getKind() == NodeKind.WORKER_RECEIVE) {
            if (firstVisit) {
                isTypeChecked = false;
                resultType = expType;
                return;
            } else {
                expType = checkedExpr.type;
                exprType = checkedExpr.expr.type;
            }
        }

        if (exprType.tag != TypeTags.UNION) {
            if (types.isAssignable(exprType, symTable.errorType)) {
                dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS);
            } else if (exprType != symTable.semanticError) {
                dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS);
            }
            checkedExpr.type = symTable.semanticError;
            return;
        }

        BUnionType unionType = (BUnionType) exprType;
        // Filter out the list of types which are not equivalent with the error type.
        Map<Boolean, List<BType>> resultTypeMap = unionType.memberTypes.stream()
                .collect(Collectors.groupingBy(memberType -> types.isAssignable(memberType, symTable.errorType)));

        // This list will be used in the desugar phase
        checkedExpr.equivalentErrorTypeList = resultTypeMap.get(true);
        if (checkedExpr.equivalentErrorTypeList == null ||
                checkedExpr.equivalentErrorTypeList.size() == 0) {
            // No member types in this union is equivalent to the error type
            dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS);
            checkedExpr.type = symTable.semanticError;
            return;
        }

        List<BType> nonErrorTypeList = resultTypeMap.get(false);
        if (nonErrorTypeList == null || nonErrorTypeList.size() == 0) {
            // All member types in the union are equivalent to the error type.
            // Checked expression requires at least one type which is not equivalent to the error type.
            dlog.error(checkedExpr.expr.pos, DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS);
            checkedExpr.type = symTable.semanticError;
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

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        if (expType.tag == TypeTags.NONE) {
            Optional.ofNullable(errorConstructorExpr.reasonExpr)
                    .map(expr -> checkExpr(expr, env, symTable.stringType))
                    .orElseThrow(AssertionError::new);

            // If no expected type, the reason type will be inferred as from the constructor
            Optional.ofNullable(errorConstructorExpr.detailsExpr)
                    .ifPresent(expr -> checkExpr(expr, env, symTable.noType));
            resultType = new BErrorType(null, symTable.stringType, errorConstructorExpr.detailsExpr == null ?
                    symTable.mapType : errorConstructorExpr.detailsExpr.type);
            return;
        }

        if (expType.tag != TypeTags.ERROR) {
            dlog.error(errorConstructorExpr.pos, DiagnosticCode.CANNOT_INFER_ERROR_TYPE, expType);
            resultType = symTable.semanticError;
            return;
        }

        // No matter what, message expression has to exist and it's type should be string type.
        Optional.ofNullable(errorConstructorExpr.reasonExpr)
                .map(expr -> checkExpr(expr, env, symTable.stringType))
                .orElseThrow(AssertionError::new);

        Optional.ofNullable(errorConstructorExpr.detailsExpr)
                .ifPresent(expr -> checkExpr(expr, env, ((BErrorType) expType).detailType));
        resultType = expType;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        resultType = serviceConstructorExpr.serviceNode.symbol.type;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.typeNode.type = symResolver.resolveTypeNode(typeTestExpr.typeNode, env);
        checkExpr(typeTestExpr.expr, env);

        resultType = types.checkType(typeTestExpr, symTable.booleanType, expType);
    }

    // Private methods

    private BVarSymbol getVarSymbolForRecordVariableReference(BLangExpression varRef) {
        if (varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return (BVarSymbol) ((BLangSimpleVarRef) varRef).symbol;
        }
        if (varRef.getKind() == NodeKind.RECORD_VARIABLE_REF) {
            return (BVarSymbol) ((BLangRecordVarRef) varRef).symbol;
        }
        if (varRef.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
            return (BVarSymbol) ((BLangTupleVarRef) varRef).symbol;
        }
        if (varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            return (BVarSymbol) ((BLangFieldBasedAccess) varRef).symbol;
        }
        if (varRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
            return (BVarSymbol) ((BLangIndexBasedAccess) varRef).symbol;
        }
        if (varRef.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            return (BVarSymbol) ((BLangXMLAttributeAccess) varRef).symbol;
        }

        dlog.error(varRef.pos, DiagnosticCode.INVALID_RECORD_BINDING_PATTERN, varRef.type);
        return null;
    }

    private BType populateArrowExprReturn(BLangArrowFunction bLangArrowFunction, BType expectedRetType) {
        SymbolEnv arrowFunctionEnv = SymbolEnv.createArrowFunctionSymbolEnv(bLangArrowFunction, env);
        bLangArrowFunction.params.forEach(param -> symbolEnter.defineNode(param, arrowFunctionEnv));
        return checkExpr(bLangArrowFunction.expression, arrowFunctionEnv, expectedRetType);
    }

    private void populateArrowExprParamTypes(BLangArrowFunction bLangArrowFunction, List<BType> paramTypes) {
        if (paramTypes.size() != bLangArrowFunction.params.size()) {
            dlog.error(bLangArrowFunction.pos, DiagnosticCode.ARROW_EXPRESSION_MISMATCHED_PARAMETER_LENGTH,
                    paramTypes.size(), bLangArrowFunction.params.size());
            resultType = symTable.semanticError;
            bLangArrowFunction.params.forEach(param -> param.type = symTable.semanticError);
            return;
        }

        for (int i = 0; i < bLangArrowFunction.params.size(); i++) {
            BLangSimpleVariable paramIdentifier = bLangArrowFunction.params.get(i);
            BType bType = paramTypes.get(i);
            BLangValueType valueTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
            valueTypeNode.setTypeKind(bType.getKind());
            paramIdentifier.setTypeNode(valueTypeNode);
            paramIdentifier.type = bType;
        }
    }

    private void checkSefReferences(DiagnosticPos pos, SymbolEnv env, BVarSymbol varSymbol) {
        if (env.enclVarSym == varSymbol) {
            dlog.error(pos, DiagnosticCode.SELF_REFERENCE_VAR, varSymbol.name);
        }
    }

    public List<BType> getListWithErrorTypes(int count) {
        List<BType> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add(symTable.semanticError);
        }

        return list;
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr) {
        Name funcName = names.fromIdNode(iExpr.name);
        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);
        BSymbol funcSymbol = symTable.notFoundSymbol;
        // TODO: we may not need this section now, with the mandatory 'self'
        // if no package alias, check for same object attached function
        if (pkgAlias == Names.EMPTY && env.enclType != null) {
            Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(
                    env.enclType.type.tsymbol.name.value, funcName.value));
            funcSymbol = symResolver.resolveStructField(iExpr.pos, env, objFuncName,
                    env.enclType.type.tsymbol);
            if (funcSymbol != symTable.notFoundSymbol) {
                iExpr.exprSymbol = symResolver.lookupSymbol(env, Names.SELF, SymTag.VARIABLE);
            }
        }

        // if no such function found, then try resolving in package
        if (funcSymbol == symTable.notFoundSymbol) {
            funcSymbol = symResolver.lookupSymbolInPackage(iExpr.pos, env, pkgAlias, funcName, SymTag.VARIABLE);
        }

        if (funcSymbol == symTable.notFoundSymbol || (funcSymbol.tag & SymTag.FUNCTION) != SymTag.FUNCTION) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, funcName);
            iExpr.argExprs.forEach(arg -> checkExpr(arg, env));
            resultType = symTable.semanticError;
            return;
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION_SYNTAX);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.RESOURCE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_RESOURCE_FUNCTION_INVOCATION);
        }
        // Set the resolved function symbol in the invocation expression.
        // This is used in the code generation phase.
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkObjectFunctionInvocationExpr(BLangInvocation iExpr, BObjectType objectType) {
        // check for object attached function
        Name funcName =
                names.fromString(Symbols.getAttachedFuncSymbolName(objectType.tsymbol.name.value, iExpr.name.value));
        BSymbol funcSymbol =
                symResolver.resolveObjectMethod(iExpr.pos, env, funcName, (BObjectTypeSymbol) objectType.tsymbol);
        if (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION_IN_OBJECT, iExpr.name.value, objectType);
            resultType = symTable.semanticError;
            return;
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION_SYNTAX);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.RESOURCE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_RESOURCE_FUNCTION_INVOCATION);
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
            resultType = symTable.semanticError;
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
            case TypeTags.RECORD:
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
        BType safeType = getSafeType(iExpr.symbol.type, iExpr);
        List<BType> paramTypes = ((BInvokableType) safeType).getParameterTypes();
        int requiredParamsCount;
        if (iExpr.symbol.tag == SymTag.VARIABLE) {
            // Here we assume function pointers can have only required params.
            // And assume that named params and rest params are not supported.
            requiredParamsCount = paramTypes.size();
        } else {
            requiredParamsCount = ((BInvokableSymbol) iExpr.symbol).params.size();
        }

        iExpr.requiredArgs = new ArrayList<>();

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
        BType actualType = symTable.semanticError;
        BInvokableSymbol invocableSymbol = (BInvokableSymbol) iExpr.symbol;

        // Check whether the expected param count and the actual args counts are matching.
        if (requiredParamsCount > iExpr.requiredArgs.size()) {
            dlog.error(iExpr.pos, DiagnosticCode.NOT_ENOUGH_ARGS_FUNC_CALL, iExpr.name.value);
            return actualType;
        } else if (invocableSymbol.restParam == null && (vararg != null || !iExpr.restArgs.isEmpty())) {
            if (invocableSymbol.defaultableParams.isEmpty()) {
                dlog.error(iExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
            } else {
                dlog.error(iExpr.pos, DiagnosticCode.DEFAULTABLE_ARG_PASSED_AS_REQUIRED_ARG, iExpr.name.value);
            }
            return actualType;
        }

        checkRequiredArgs(iExpr.requiredArgs, paramTypes);
        checkNamedArgs(iExpr.namedArgs, invocableSymbol.defaultableParams);
        checkRestArgs(iExpr.restArgs, vararg, invocableSymbol.restParam);

        if (iExpr.async) {
            return this.generateFutureType(invocableSymbol);
        } else {
            return getSafeType(invocableSymbol.type, iExpr).getReturnType();
        }
    }

    private BFutureType generateFutureType(BInvokableSymbol invocableSymbol) {
        BType retType = invocableSymbol.type.getReturnType();
        boolean isWorkerStart = invocableSymbol.name.value.startsWith(WORKER_LAMBDA_VAR_PREFIX);
        return new BFutureType(TypeTags.FUTURE, retType, null, isWorkerStart);
    }

    private void checkRequiredArgs(List<BLangExpression> requiredArgExprs, List<BType> requiredParamTypes) {
        for (int i = 0; i < requiredArgExprs.size(); i++) {
            checkExpr(requiredArgExprs.get(i), this.env, requiredParamTypes.get(i));
        }
    }

    private void checkNamedArgs(List<BLangExpression> namedArgExprs, List<BVarSymbol> defaultableParams) {
        for (BLangExpression expr : namedArgExprs) {
            BLangIdentifier argName = ((NamedArgNode) expr).getName();
            BVarSymbol varSym = defaultableParams.stream()
                    .filter(param -> param.getName().value.equals(argName.value))
                    .findAny()
                    .orElse(null);
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

    private BSymbol checkBuiltinFunctionInvocation(BLangInvocation iExpr, BLangBuiltInMethod function, BType... args) {
        Name funcName = names.fromString(iExpr.name.value);

        BSymbol funcSymbol = symResolver.resolveBuiltinOperator(funcName, args);

        if (funcSymbol == symTable.notFoundSymbol) {
            funcSymbol = getSymbolForBuiltinMethodWithDynamicRetType(iExpr, function);
            if (funcSymbol == symTable.notFoundSymbol || funcSymbol == symTable.invalidUsageSymbol) {
                resultType = symTable.semanticError;
                return funcSymbol;
            }
        }

        iExpr.builtinMethodInvocation = true;
        iExpr.builtInMethod = function;
        iExpr.symbol = funcSymbol;

        checkInvocationParamAndReturnType(iExpr);
        if (resultType != null && resultType != symTable.semanticError && iExpr.impConversionExpr == null) {
            types.setImplicitCastExpr(iExpr, resultType, expType);
        }
        return funcSymbol;
    }

    private void checkActionInvocationExpr(BLangInvocation iExpr, BType epType) {
        BType actualType = symTable.semanticError;
        if (epType == symTable.semanticError || epType.tag != TypeTags.OBJECT
                || ((BLangVariableReference) iExpr.expr).symbol.tag != SymTag.ENDPOINT) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION);
            resultType = actualType;
            return;
        }

        final BVarSymbol epSymbol = (BVarSymbol) ((BLangVariableReference) iExpr.expr).symbol;

        Name remoteFuncQName = names
                .fromString(Symbols.getAttachedFuncSymbolName(epType.tsymbol.name.value, iExpr.name.value));
        Name actionName = names.fromIdNode(iExpr.name);
        BSymbol remoteFuncSymbol = symResolver
                .lookupMemberSymbol(iExpr.pos, ((BObjectTypeSymbol) epSymbol.type.tsymbol).methodScope, env,
                        remoteFuncQName, SymTag.FUNCTION);
        if (remoteFuncSymbol == symTable.notFoundSymbol || !Symbols.isFlagOn(remoteFuncSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_ACTION, actionName, epSymbol.type.tsymbol.name);
            resultType = actualType;
            return;
        }
        iExpr.symbol = remoteFuncSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void checkRecLiteralKeyValue(BLangRecordKeyValue keyValuePair, BType recType) {
        BType fieldType = symTable.semanticError;
        BLangExpression valueExpr = keyValuePair.valueExpr;
        switch (recType.tag) {
            case TypeTags.RECORD:
                fieldType = checkStructLiteralKeyExpr(keyValuePair.key, recType);
                break;
            case TypeTags.MAP:
                fieldType = checkMapLiteralKeyExpr(keyValuePair.key.expr, recType);
                break;
            case TypeTags.JSON:
                fieldType = checkJSONLiteralKeyExpr(keyValuePair.key);

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
            case TypeTags.ERROR:
                checkExpr(valueExpr, this.env, fieldType);
        }

        checkExpr(valueExpr, this.env, fieldType);
    }

    private BType checkStructLiteralKeyExpr(BLangRecordKey key, BType recordType) {
        Name fieldName;
        BLangExpression keyExpr = key.expr;

        if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        } else {
            // keys of the struct literal can only be a varRef (identifier)
            dlog.error(keyExpr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_KEY);
            return symTable.semanticError;
        }

        // Check whether the struct field exists
        BSymbol fieldSymbol = symResolver.resolveStructField(keyExpr.pos, this.env,
                fieldName, recordType.tsymbol);
        if (fieldSymbol == symTable.notFoundSymbol) {
            if (((BRecordType) recordType).sealed) {
                dlog.error(keyExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, fieldName,
                        recordType.tsymbol.type.getKind().typeName(), recordType.tsymbol);
                return symTable.semanticError;
            }

            return ((BRecordType) recordType).restFieldType;
        }

        return fieldSymbol.type;
    }

    private BType checkJSONLiteralKeyExpr(BLangRecordKey key) {
        if (checkRecLiteralKeyExpr(key.expr).tag != TypeTags.STRING) {
            return symTable.semanticError;
        }

        return symTable.jsonType;
    }

    private BType checkMapLiteralKeyExpr(BLangExpression keyExpr, BType recordType) {
        if (checkRecLiteralKeyExpr(keyExpr).tag != TypeTags.STRING) {
            return symTable.semanticError;
        }

        return ((BMapType) recordType).constraint;
    }

    private BType checkRecLiteralKeyExpr(BLangExpression keyExpr) {
        // If the key is not at identifier (i.e: varRef), check the expression
        if (keyExpr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return checkExpr(keyExpr, this.env, symTable.stringType);
        }

        // If the key expression is an identifier then we simply set the type as string.
        keyExpr.type = symTable.stringType;
        return keyExpr.type;
    }

    private BType checkIndexExprForStructFieldAccess(BLangExpression indexExpr) {
        if (indexExpr.getKind() != NodeKind.LITERAL && indexExpr.getKind() != NodeKind.NUMERIC_LITERAL) {
            indexExpr.type = symTable.semanticError;
            dlog.error(indexExpr.pos, DiagnosticCode.INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS);
            return indexExpr.type;
        }

        return checkExpr(indexExpr, this.env, symTable.stringType);
    }

    private BType checkTypeForIndexBasedAccess(BLangIndexBasedAccess indexBasedAccessExpr, BType actualType) {
        // index based map/record access always returns a nil-able type
        if (actualType.tag == TypeTags.ANY || actualType.tag == TypeTags.JSON) {
            return actualType;
        }

        if (indexBasedAccessExpr.leafNode && indexBasedAccessExpr.lhsVar) {
            return actualType;
        }

        BUnionType type = new BUnionType(null, new LinkedHashSet<>(getTypesList(actualType)), true);
        type.memberTypes.add(symTable.nilType);
        return type;
    }

    private BType checkStructFieldAccess(BLangVariableReference varReferExpr, Name fieldName, BType structType) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, this.env, fieldName, structType.tsymbol);

        if (fieldSymbol != symTable.notFoundSymbol) {
            // Setting the field symbol. This is used during the code generation phase
            varReferExpr.symbol = fieldSymbol;
            return fieldSymbol.type;
        }

        if (structType.tag == TypeTags.OBJECT) {
            // check if it is an attached function pointer call
            Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(structType.tsymbol.name.value,
                    fieldName.value));
            fieldSymbol = symResolver.resolveObjectField(varReferExpr.pos, env, objFuncName, structType.tsymbol);

            if (fieldSymbol == symTable.notFoundSymbol) {
                dlog.error(varReferExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, fieldName,
                        structType.tsymbol.type.getKind().typeName(), structType.tsymbol);
                return symTable.semanticError;
            }

            // Setting the field symbol. This is used during the code generation phase
            varReferExpr.symbol = fieldSymbol;
            return fieldSymbol.type;
        }

        // Assuming this method is only used for objects and records
        if (((BRecordType) structType).sealed) {
            dlog.error(varReferExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, fieldName,
                    structType.tsymbol.type.getKind().typeName(), structType.tsymbol);
            return symTable.semanticError;
        }

        return ((BRecordType) structType).restFieldType;
    }

    private BType checkTupleFieldType(BLangIndexBasedAccess indexBasedAccessExpr, BType varRefType, int indexValue) {
        List<BType> tupleTypes = ((BTupleType) varRefType).tupleTypes;
        if (indexValue < 0 || tupleTypes.size() <= indexValue) {
            dlog.error(indexBasedAccessExpr.pos,
                    DiagnosticCode.TUPLE_INDEX_OUT_OF_RANGE, indexValue, tupleTypes.size());
            return symTable.semanticError;
        }
        return tupleTypes.get(indexValue);
    }

    private BType checkIndexExprForTupleFieldAccess(BLangExpression indexExpr) {
        if (indexExpr.getKind() != NodeKind.LITERAL && indexExpr.getKind() != NodeKind.NUMERIC_LITERAL) {
            indexExpr.type = symTable.semanticError;
            dlog.error(indexExpr.pos, DiagnosticCode.INVALID_INDEX_EXPR_TUPLE_FIELD_ACCESS);
            return indexExpr.type;
        }

        return checkExpr(indexExpr, this.env, symTable.intType);
    }

    private void validateTags(BLangXMLElementLiteral bLangXMLElementLiteral, SymbolEnv xmlElementEnv) {
        // check type for start and end tags
        BLangExpression startTagName = bLangXMLElementLiteral.startTagName;
        checkExpr(startTagName, xmlElementEnv, symTable.stringType);
        BLangExpression endTagName = bLangXMLElementLiteral.endTagName;
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
            checkExpr(expr, env);
            if (concatExpr == null) {
                concatExpr = expr;
                continue;
            }

            BSymbol opSymbol = symResolver.resolveBinaryOperator(OperatorKind.ADD, symTable.stringType, expr.type);
            if (opSymbol == symTable.notFoundSymbol && expr.type != symTable.semanticError) {
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
        List<BLangExpression> newChildren = new ArrayList<>();
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
            if (opSymbol == symTable.notFoundSymbol && exprType != symTable.semanticError) {
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
            binaryExpressionNode.type = symTable.semanticError;
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

    private BType getTypeOfExprInFieldAccess(BLangExpression expr) {
        checkExpr(expr, this.env, symTable.noType);
        return expr.type;
    }

    private BType getAccessExprFinalType(BLangAccessExpression accessExpr, BType actualType) {
        if (!isSafeNavigableExpr(accessExpr)) {
            return actualType;
        }

        // Cache the actual type of the field. This will be used in desuagr phase to create safe navigation.
        accessExpr.originalType = actualType;

        BUnionType unionType = new BUnionType(null, new LinkedHashSet<>(), false);
        if (actualType.tag == TypeTags.UNION) {
            unionType.memberTypes.addAll(((BUnionType) actualType).memberTypes);
            unionType.setNullable(actualType.isNullable());
        } else {
            unionType.memberTypes.add(actualType);
        }

        if (returnsNull(accessExpr)) {
            unionType.memberTypes.add(symTable.nilType);
            unionType.setNullable(true);
        }

        BType parentType = accessExpr.expr.type;
        if (accessExpr.safeNavigate && (parentType.tag == TypeTags.SEMANTIC_ERROR || (parentType.tag == TypeTags.UNION
                && ((BUnionType) parentType).memberTypes.contains(symTable.errorType)))) {
            unionType.memberTypes.add(symTable.errorType);
        }

        // If there's only one member, and the one an only member is:
        //    a) nilType OR
        //    b) not-nullable 
        // then return that only member, as the return type.
        if (unionType.memberTypes.size() == 1 &&
                (!unionType.isNullable() || unionType.memberTypes.contains(symTable.nilType))) {
            return unionType.memberTypes.toArray(new BType[0])[0];
        }

        return unionType;
    }

    private boolean returnsNull(BLangAccessExpression accessExpr) {
        BType parentType = accessExpr.expr.type;
        if (parentType.isNullable() && parentType.tag != TypeTags.JSON) {
            return true;
        }

        // Check whether this is a map access by index. If not, null is not a possible return type.
        if (parentType.tag != TypeTags.MAP) {
            return false;
        }

        // A map access with index, returns nullable type
        if (accessExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR && accessExpr.expr.type.tag == TypeTags.MAP) {
            BType constraintType = ((BMapType) accessExpr.expr.type).constraint;

            // JSON and any is special cased here, since those are two union types, with null within them.
            // Therefore return 'type' will not include null.
            return constraintType != null && constraintType.tag != TypeTags.ANY && constraintType.tag != TypeTags.JSON;
        }

        return false;
    }

    private boolean isSafeNavigableExpr(BLangAccessExpression accessExpr) {
        return !(accessExpr.getKind() == NodeKind.INVOCATION && ((BLangInvocation) accessExpr).builtinMethodInvocation
                && ((BLangInvocation) accessExpr).builtInMethod == BLangBuiltInMethod.IS_FROZEN);
    }

    private BType checkFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName) {
        BType actualType = symTable.semanticError;
        switch (varRefType.tag) {
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
                actualType = checkStructFieldAccess(fieldAccessExpr, fieldName, varRefType);
                break;
            case TypeTags.MAP:
                actualType = ((BMapType) varRefType).getConstraint();
                break;
            case TypeTags.STREAM:
                BType streamConstraintType = ((BStreamType) varRefType).constraint;
                if (streamConstraintType.tag == TypeTags.RECORD) {
                    actualType = checkStructFieldAccess(fieldAccessExpr, fieldName, streamConstraintType);
                }
                break;
            case TypeTags.TABLE:
                BType tableConstraintType = ((BTableType) varRefType).constraint;
                if (tableConstraintType.tag == TypeTags.RECORD) {
                    actualType = checkStructFieldAccess(fieldAccessExpr, fieldName, tableConstraintType);
                }
                break;
            case TypeTags.JSON:
                actualType = symTable.jsonType;
                break;
            case TypeTags.XML:
                if (fieldAccessExpr.lhsVar) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
                    break;
                }
                actualType = symTable.xmlType;
                break;
            case TypeTags.SEMANTIC_ERROR:
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
        BType actualType = symTable.semanticError;
        BType indexExprType;
        switch (varRefType.tag) {
            case TypeTags.OBJECT:
                indexExprType = checkIndexExprForStructFieldAccess(indexExpr);
                if (indexExprType.tag == TypeTags.STRING) {
                    String fieldName = (String) ((BLangLiteral) indexExpr).value;
                    actualType = checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), varRefType);
                }
                break;
            case TypeTags.RECORD:
                indexExprType = checkIndexExprForStructFieldAccess(indexExpr);
                if (indexExprType.tag == TypeTags.STRING) {
                    String fieldName = (String) ((BLangLiteral) indexExpr).value;
                    actualType = checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), varRefType);
                    actualType = checkTypeForIndexBasedAccess(indexBasedAccessExpr, actualType);
                }
                break;
            case TypeTags.MAP:
                indexExprType = checkExpr(indexExpr, this.env, symTable.stringType);
                if (indexExprType.tag == TypeTags.STRING) {
                    actualType = ((BMapType) varRefType).getConstraint();
                    actualType = checkTypeForIndexBasedAccess(indexBasedAccessExpr, actualType);
                }
                break;
            case TypeTags.JSON:
                indexExprType = checkExpr(indexExpr, this.env, symTable.noType);
                if (indexExprType.tag != TypeTags.STRING && indexExprType.tag != TypeTags.INT) {
                    dlog.error(indexExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.stringType, indexExprType);
                    break;
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
                    indexExpr.type = symTable.semanticError;
                    dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
                    break;
                }

                checkExpr(indexExpr, this.env);
                actualType = symTable.xmlType;
                break;
            case TypeTags.TUPLE:
                indexExprType = checkIndexExprForTupleFieldAccess(indexExpr);
                if (indexExprType.tag == TypeTags.INT) {
                    int indexValue = ((Long) ((BLangLiteral) indexExpr).value).intValue();
                    actualType = checkTupleFieldType(indexBasedAccessExpr, varRefType, indexValue);
                }
                break;
            case TypeTags.SEMANTIC_ERROR:
                indexBasedAccessExpr.indexExpr.type = symTable.semanticError;
                break;
            default:
                indexBasedAccessExpr.indexExpr.type = symTable.semanticError;
                dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEXING,
                        indexBasedAccessExpr.expr.type);
        }

        return actualType;
    }

    private BType getSafeType(BType type, BLangAccessExpression accessExpr) {
        if (type.tag != TypeTags.UNION) {
            return type;
        }

        // Extract the types without the error and null, and revisit access expression
        Set<BType> varRefMemberTypes = ((BUnionType) type).memberTypes;
        List<BType> lhsTypes;

        boolean nullable = false;
        if (accessExpr.safeNavigate) {
            if (!varRefMemberTypes.contains(symTable.errorType)) {
                dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
                return symTable.semanticError;
            }

            lhsTypes = varRefMemberTypes.stream().filter(memberType -> {
                return memberType != symTable.errorType && memberType != symTable.nilType;
            }).collect(Collectors.toList());

            if (lhsTypes.isEmpty()) {
                dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
                return symTable.semanticError;
            }
        } else {
            lhsTypes = varRefMemberTypes.stream().filter(memberType -> {
                return memberType != symTable.nilType;
            }).collect(Collectors.toList());
        }

        if (lhsTypes.size() == 1) {
            return lhsTypes.get(0);
        }

        return new BUnionType(null, new LinkedHashSet<>(lhsTypes), nullable);
    }

    private List<BType> getTypesList(BType type) {
        if (type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) type;
            return new ArrayList<>(unionType.memberTypes);
        } else {
            return Lists.of(type);
        }
    }

    private LinkedHashSet<BType> getMatchExpressionTypes(BLangMatchExpression bLangMatchExpression) {
        List<BType> exprTypes = getTypesList(bLangMatchExpression.expr.type);
        LinkedHashSet<BType> matchExprTypes = new LinkedHashSet<>();
        for (BType type : exprTypes) {
            boolean assignable = false;
            for (BLangMatchExprPatternClause pattern : bLangMatchExpression.patternClauses) {
                BType patternExprType = pattern.expr.type;

                // Type of the pattern expression, becomes one of the types of the whole but expression
                matchExprTypes.addAll(getTypesList(patternExprType));

                if (type.tag == TypeTags.SEMANTIC_ERROR || patternExprType.tag == TypeTags.SEMANTIC_ERROR) {
                    return new LinkedHashSet<BType>() {
                        {
                            add(symTable.semanticError);
                        }
                    };
                }

                assignable = this.types.isAssignable(type, pattern.variable.type);
                if (assignable) {
                    break;
                }
            }

            // If the matching expr type is not matching to any pattern, it becomes one of the types
            // returned by the whole but expression
            if (!assignable) {
                matchExprTypes.add(type);
            }
        }

        return matchExprTypes;
    }

    private BSymbol getSymbolForBuiltinMethodWithDynamicRetType(BLangInvocation iExpr, BLangBuiltInMethod function) {
        switch (function) {
            case CLONE:
            case FREEZE:
                return getSymbolForAnydataReturningBuiltinMethods(iExpr);
            case IS_FROZEN:
                return getSymbolForIsFrozenBuiltinMethod(iExpr);
            case STAMP:
                List<BLangExpression> functionArgList = iExpr.argExprs;
                // Resolve the type of the variables passed as arguments to stamp in-built function.
                for (BLangExpression expression : functionArgList) {
                    checkExpr(expression, env, symTable.noType);
                }
                return symResolver.createSymbolForStampOperator(iExpr.pos, new Name(function.getName()),
                        functionArgList, iExpr.expr);
            case CONVERT:
                functionArgList = iExpr.argExprs;
                // Resolve the type of the variables passed as arguments to convert in-built function.
                for (BLangExpression expression : functionArgList) {
                    checkExpr(expression, env, symTable.noType);
                }
                return symResolver.createSymbolForConvertOperator(iExpr.pos, new Name(function.getName()),
                                                                  functionArgList, iExpr.expr);
            case CALL:
                return getFunctionPointerCallSymbol(iExpr);
            case DETAIL:
                return symResolver.createSymbolForDetailBuiltInMethod(iExpr.name, iExpr.expr.type);
            default:
                return symTable.notFoundSymbol;
        }
    }

    private BSymbol getFunctionPointerCallSymbol(BLangInvocation iExpr) {
        if (iExpr.expr == null) {
            // shouldn't reach here
            return symTable.notFoundSymbol;
        }

        BSymbol varSymbol = ((BLangVariableReference) iExpr.expr).symbol;
        if (varSymbol == null) {
            return symTable.notFoundSymbol;
        }

        BType varType = getSafeType(varSymbol.type, iExpr);
        if (varType.tag != TypeTags.INVOKABLE) {
            return symTable.notFoundSymbol;
        }

        if (varSymbol.kind != SymbolKind.FUNCTION) {
            varSymbol = new BInvokableSymbol(SymTag.VARIABLE, 0, varSymbol.name, env.enclPkg.symbol.pkgID, varType,
                    env.scope.owner);
            varSymbol.kind = SymbolKind.FUNCTION;
        }

        iExpr.symbol = varSymbol;
        return varSymbol;
    }

    private BSymbol getSymbolForAnydataReturningBuiltinMethods(BLangInvocation iExpr) {
        BType type = iExpr.expr.type;
        if (!types.isLikeAnydataOrNotNil(type)) {
            return symTable.notFoundSymbol;
        }

        BType retType;
        if (types.isAnydata(type)) {
            retType = type;
        } else {
            if (type.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) type;
                if (unionType.getMemberTypes().stream().noneMatch(memType -> memType.tag == TypeTags.ERROR)) {
                    unionType.memberTypes.add(symTable.errorType);
                }
                retType = unionType;
            } else {
                retType = new BUnionType(null, new LinkedHashSet<BType>() {{
                    add(type);
                    add(symTable.errorType);
                }}, false);
            }
        }
        return symResolver.createBuiltinMethodSymbol(BLangBuiltInMethod.FREEZE, type, retType, InstructionCodes.FREEZE);
    }

    private BSymbol getSymbolForIsFrozenBuiltinMethod(BLangInvocation iExpr) {
        BType type = iExpr.expr.type;
        if (!types.isLikeAnydataOrNotNil(type)) {
            return symTable.notFoundSymbol;
        }
        return symResolver.createBuiltinMethodSymbol(BLangBuiltInMethod.IS_FROZEN, type, symTable.booleanType,
                InstructionCodes.IS_FROZEN);
    }

    private boolean isSafeNavigable(BLangAccessExpression fieldAccessExpr, BType varRefType) {
        // If the expression is safe navigable, then the type should be an union. Otherwise safe navigation is not
        // required.
        if (fieldAccessExpr.safeNavigate && varRefType.tag != TypeTags.UNION && varRefType != symTable.semanticError) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, varRefType);
            return false;
        }
        return true;
    }
}
