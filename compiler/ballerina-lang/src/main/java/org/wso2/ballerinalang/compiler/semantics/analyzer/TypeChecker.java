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
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types.RecordKind;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BEndpointVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.BArrayState;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MAX_VALUE;
import static org.wso2.ballerinalang.compiler.semantics.model.SymbolTable.BBYTE_MIN_VALUE;

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

        Object literalValue = literalExpr.value;
        if (TypeTags.FLOAT == expType.tag && TypeTags.INT == literalType.tag) {
            literalType = symTable.floatType;
            literalExpr.value = ((Long) literalValue).doubleValue();
        }

        if (TypeTags.BYTE == expType.tag && TypeTags.INT == literalType.tag) {
            if (!isByteLiteralValue((Long) literalValue)) {
                dlog.error(literalExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, literalType);
                return;
            }
            literalType = symTable.byteType;
            literalExpr.value = ((Long) literalValue).byteValue();
        }

        // check whether this is a byte array
        if (TypeTags.BYTE_ARRAY == literalExpr.typeTag) {
            literalType = new BArrayType(symTable.byteType);
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
                    .map(memberType -> types.isAssignableToFiniteType(memberType, literalExpr))
                    .anyMatch(foundType -> foundType);
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
        if (expType.tag == symTable.errType.tag) {
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
        if (tableConstraint.tag != TypeTags.ERROR) {
            List<String> columnNames = new ArrayList<>();
            for (BField field : ((BRecordType) tableConstraint).fields) {
                columnNames.add(field.getName().getValue());
            }
            for (BLangTableLiteral.BLangTableColumn column : tableLiteral.columns) {
                boolean contains = columnNames.contains(column.columnName);
                if (!contains) {
                    dlog.error(tableLiteral.pos, DiagnosticCode.UNDEFINED_TABLE_COLUMN, column.columnName,
                            tableConstraint);
                }
            }
        }
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
                resultType = symTable.errType;
                return;
            }
            checkExprs(arrayLiteral.exprs, this.env, arrayType.eType);
            actualType = arrayType;

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
            actualType = new BArrayType(actualType, null, arrayLiteral.exprs.size(), BArrayState.UNSEALED);

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
        if (expTypeTag == TypeTags.ANY || expTypeTag == TypeTags.OBJECT) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_RECORD_LITERAL, originalExpType);
            resultType = symTable.errType;
            return;
        }

        List<BType> matchedTypeList = getRecordCompatibleType(expType, recordLiteral);

        if (matchedTypeList.isEmpty()) {
            dlog.error(recordLiteral.pos, DiagnosticCode.INVALID_LITERAL_FOR_TYPE, expType);
        } else if (matchedTypeList.size() > 1) {
            dlog.error(recordLiteral.pos, DiagnosticCode.AMBIGUOUS_TYPES, expType);
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
                                    && isRecordLiteralCompatible((BRecordType) type, recordLiteral)))
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

    private boolean isRecordLiteralCompatible(BRecordType bRecordType, BLangRecordLiteral recordLiteral) {
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

            // If a required field is missing and it's not defaultable, it's a compile error
            if (!hasField && !Symbols.isFlagOn(field.symbol.flags, Flags.OPTIONAL) &&
                    (!types.defaultValueExists(pos, field.type) &&
                            !Symbols.isFlagOn(field.symbol.flags, Flags.DEFAULTABLE))) {
                dlog.error(pos, DiagnosticCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
            }
        });
    }

    private List<BType> getArrayCompatibleTypes(BType expType, BType actualType) {
        Set<BType> expTypes =
                expType.tag == TypeTags.UNION ? ((BUnionType) expType).memberTypes : new HashSet<BType>() {
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
            // if no symbol, check same for object attached function
            if (symbol == symTable.notFoundSymbol && env.enclTypeDefinition != null) {
                Name objFuncName = names.fromString(Symbols
                        .getAttachedFuncSymbolName(env.enclTypeDefinition.name.value, varName.value));
                symbol = symResolver.resolveStructField(varRefExpr.pos, env, objFuncName,
                        env.enclTypeDefinition.symbol.type.tsymbol);
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

    private boolean isFunctionArgument(BSymbol symbol, List<BLangVariable> params) {
        return params.stream().anyMatch(param -> (param.symbol.name.equals(symbol.name) &&
                param.type.tag == symbol.type.tag));
    }

    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        // First analyze the variable reference expression.
        fieldAccessExpr.expr.lhsVar = fieldAccessExpr.lhsVar;
        BType varRefType = getTypeOfExprInFieldAccess(fieldAccessExpr.expr);

        // Accessing all fields using * is only supported for XML.
        if (fieldAccessExpr.fieldKind == FieldKind.ALL && varRefType.tag != TypeTags.XML) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_GET_ALL_FIELDS, varRefType);
        }

        // error lifting on lhs is not supported
        if (fieldAccessExpr.lhsVar && fieldAccessExpr.safeNavigate) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.INVALID_ERROR_LIFTING_ON_LHS);
            resultType = symTable.errType;
            return;
        }

        varRefType = getSafeType(varRefType, fieldAccessExpr);
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
        indexBasedAccessExpr.expr.lhsVar = indexBasedAccessExpr.lhsVar;
        checkExpr(indexBasedAccessExpr.expr, this.env, symTable.noType);

        BType varRefType = indexBasedAccessExpr.expr.type;
        varRefType = getSafeType(varRefType, indexBasedAccessExpr);
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
        varRefType = getSafeType(varRefType, iExpr);
        switch (varRefType.tag) {
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
                // Invoking a function bound to a struct
                // First check whether there exist a function with this name
                // Then perform arg and param matching
                checkFunctionInvocationExpr(iExpr, (BStructureType) varRefType);
                break;
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.INT:
            case TypeTags.FLOAT:
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
            iExpr.originalType = ((BInvokableSymbol) iExpr.symbol).type.getReturnType();
        } else {
            iExpr.originalType = iExpr.type;
        }
    }

    public void visit(BLangTypeInit cIExpr) {
        if ((expType.tag == TypeTags.ANY && cIExpr.userDefinedType == null)
                || expType.tag == TypeTags.RECORD) {
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

        if (actualType.tag != TypeTags.OBJECT) {
            dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INFER_OBJECT_TYPE_FROM_LHS, actualType);
            resultType = symTable.errType;
            return;
        }

        if ((actualType.tsymbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
            dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INITIALIZE_ABSTRACT_OBJECT, actualType.tsymbol);
            cIExpr.objectInitInvocation.argExprs.forEach(expr -> checkExpr(expr, env, symTable.noType));
            resultType = symTable.errType;
            return;
        }

        if (((BObjectTypeSymbol) actualType.tsymbol).initializerFunc != null) {
            cIExpr.objectInitInvocation.symbol = ((BObjectTypeSymbol) actualType.tsymbol).initializerFunc.symbol;
            checkInvocationParam(cIExpr.objectInitInvocation);
        } else if (cIExpr.objectInitInvocation.argExprs.size() > 0) {
            // If the initializerFunc null then this is a default constructor invocation. Hence should not 
            // pass any arguments.
            dlog.error(cIExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, cIExpr.objectInitInvocation.exprSymbol);
            cIExpr.objectInitInvocation.argExprs.forEach(expr -> checkExpr(expr, env, symTable.noType));
            resultType = symTable.errType;
            return;
        }

        cIExpr.objectInitInvocation.type = symTable.nilType;
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
        } else if (expType.tag == TypeTags.FUTURE) {
            actualType = ((BFutureType) expType).constraint;
        } else {
            dlog.error(awaitExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, symTable.futureType, expType);
            return;
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
                opSymbol = getBinaryEqualityForTypeSets(binaryExpr.opKind, lhsType, rhsType,
                        binaryExpr);
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

    private BSymbol getBinaryEqualityForTypeSets(OperatorKind opKind, BType lhsType,
                                                 BType rhsType, BLangBinaryExpr binaryExpr) {
        if (opKind != OperatorKind.EQUAL && opKind != OperatorKind.NOT_EQUAL) {
            return symTable.notFoundSymbol;
        }
        if (types.isIntersectionExist(lhsType, rhsType)) {
            if ((!types.isValueType(lhsType) && !types.isValueType(rhsType)) ||
                    (types.isValueType(lhsType) && types.isValueType(rhsType))) {
                return symResolver.createReferenceEqualityOperator(opKind, lhsType, rhsType);
            } else {
                types.setImplicitCastExpr(binaryExpr.rhsExpr, rhsType, symTable.anyType);
                types.setImplicitCastExpr(binaryExpr.lhsExpr, lhsType, symTable.anyType);
                return symResolver.createReferenceEqualityOperator(opKind, symTable.anyType, symTable.anyType);
            }
        } else {
            return symTable.notFoundSymbol;
        }
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
        for (int i = 0; i < bracedOrTupleExpr.expressions.size(); i++) {
            results.add(checkExpr(bracedOrTupleExpr.expressions.get(i), env, symTable.noType));
        }
        if (expType.tag == TypeTags.TYPEDESC) {
            bracedOrTupleExpr.isTypedescExpr = true;
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
            BType actualType = new BTupleType(results);

            if (expType.tag == TypeTags.ANY) {
                dlog.error(bracedOrTupleExpr.pos, DiagnosticCode.INVALID_TUPLE_LITERAL, expType);
                resultType = symTable.errType;
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
            final BType actualType = results.get(0);
            BLangExpression expression = bracedOrTupleExpr.expressions.get(0);
            resultType = types.checkType(expression, actualType, expType);
        }
    }

    public void visit(BLangTypedescExpr accessExpr) {
        BType actualType = symTable.typeDesc;
        accessExpr.resolvedType = symResolver.resolveTypeNode(accessExpr.typeNode, env);
        resultType = types.checkType(accessExpr, actualType, expType);
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        BType exprType;
        BType actualType = symTable.errType;
        if (OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
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
        BType actualType = symTable.errType;

        BType targetType = symResolver.resolveTypeNode(conversionExpr.typeNode, env);
        conversionExpr.targetType = targetType;
        BType sourceType = checkExpr(conversionExpr.expr, env, symTable.noType);

        // Lookup for built-in type conversion operator symbol
        BSymbol symbol = symResolver.resolveConversionOperator(sourceType, targetType);
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CONVERSION, sourceType, targetType);
        } else {
            BConversionOperatorSymbol conversionSym = (BConversionOperatorSymbol) symbol;
            conversionExpr.conversionSymbol = conversionSym;
            actualType = conversionSym.type.getReturnType();
        }

        resultType = types.checkType(conversionExpr, actualType, expType);
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
        if (expType.tag != TypeTags.INVOKABLE) {
            dlog.error(bLangArrowFunction.pos, DiagnosticCode.ARROW_EXPRESSION_CANNOT_INFER_TYPE_FROM_LHS);
            resultType = symTable.errType;
            return;
        }

        BInvokableType expectedInvocation = (BInvokableType) this.expType;
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

        Set<BType> matchExprTypes = getMatchExpressionTypes(bLangMatchExpression);

        BType actualType;
        if (matchExprTypes.contains(symTable.errType)) {
            actualType = symTable.errType;
        } else if (matchExprTypes.size() == 1) {
            actualType = matchExprTypes.toArray(new BType[matchExprTypes.size()])[0];
        } else {
            actualType = new BUnionType(null, matchExprTypes, matchExprTypes.contains(symTable.nilType));
        }

        resultType = types.checkType(bLangMatchExpression, actualType, expType);
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

    private BType populateArrowExprReturn(BLangArrowFunction bLangArrowFunction, BType expectedRetType) {
        SymbolEnv arrowFunctionEnv = SymbolEnv.createArrowFunctionSymbolEnv(bLangArrowFunction, env);
        bLangArrowFunction.params.forEach(param -> symbolEnter.defineNode(param, arrowFunctionEnv));
        return checkExpr(bLangArrowFunction.expression, arrowFunctionEnv, expectedRetType);
    }

    private void populateArrowExprParamTypes(BLangArrowFunction bLangArrowFunction, List<BType> paramTypes) {
        if (paramTypes.size() != bLangArrowFunction.params.size()) {
            dlog.error(bLangArrowFunction.pos, DiagnosticCode.ARROW_EXPRESSION_MISMATCHED_PARAMETER_LENGTH,
                    paramTypes.size(), bLangArrowFunction.params.size());
            resultType = symTable.errType;
            bLangArrowFunction.params.forEach(param -> param.type = symTable.errType);
            return;
        }

        for (int i = 0; i < bLangArrowFunction.params.size(); i++) {
            BLangVariable paramIdentifier = bLangArrowFunction.params.get(i);
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
            list.add(symTable.errType);
        }

        return list;
    }

    private void checkFunctionInvocationExpr(BLangInvocation iExpr) {
        Name funcName = names.fromIdNode(iExpr.name);
        Name pkgAlias = names.fromIdNode(iExpr.pkgAlias);

        BSymbol funcSymbol = symTable.notFoundSymbol;
        // if no package alias, check for same object attached function
        if (pkgAlias == Names.EMPTY && env.enclTypeDefinition != null) {
            Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(
                    env.enclTypeDefinition.name.value, iExpr.name.value));
            funcSymbol = symResolver.resolveStructField(iExpr.pos, env, objFuncName,
                    env.enclTypeDefinition.symbol.type.tsymbol);
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
            iExpr.argExprs.forEach(arg -> checkExpr(arg, env));
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

    private void checkFunctionInvocationExpr(BLangInvocation iExpr, BStructureType structType) {
        // check for same object attached function
        Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(structType
                .tsymbol.name.value, iExpr.name.value));
        BSymbol funcSymbol = symResolver.resolveStructField(iExpr.pos, env, objFuncName, structType.tsymbol);

        if (funcSymbol == symTable.notFoundSymbol) {
            // Check, any function pointer in struct field with given name.
            funcSymbol = symResolver.resolveStructField(iExpr.pos, env, names.fromIdNode(iExpr.name),
                    structType.tsymbol);
            if (structType.tag == TypeTags.OBJECT &&
                    (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE)) {
                dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION_IN_OBJECT, iExpr.name.value, structType);
                resultType = symTable.errType;
                return;
            }

            if (structType.tag == TypeTags.RECORD) {
                if (funcSymbol == symTable.notFoundSymbol) {
                    dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, iExpr.name.value,
                               structType.getKind().typeName(), structType.tsymbol);
                    resultType = symTable.errType;
                    return;
                }
                if (funcSymbol.type.tag != TypeTags.INVOKABLE) {
                    dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_POINTER_INVOCATION, iExpr.name.value,
                               structType);
                    resultType = symTable.errType;
                    return;
                }
            }

            if ((funcSymbol.flags & Flags.ATTACHED) != Flags.ATTACHED) {
                iExpr.functionPointerInvocation = true;
            }
        } else {
            // Attached function found
            // Check for the explicit initializer function invocation
            if (structType.tag == TypeTags.RECORD) {
                BAttachedFunction initializerFunc = ((BRecordTypeSymbol) structType.tsymbol).initializerFunc;
                if (initializerFunc != null && initializerFunc.funcName.value.equals(iExpr.name.value)) {
                    dlog.error(iExpr.pos, DiagnosticCode.RECORD_INITIALIZER_INVOKED, structType.tsymbol.toString());
                }
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
            case TypeTags.RECORD:
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

    private void checkActionInvocationExpr(BLangInvocation iExpr, BType conType) {
        BType actualType = symTable.errType;
        if (conType == symTable.errType || conType.tag != TypeTags.OBJECT
                || iExpr.expr.symbol.tag != SymTag.ENDPOINT) {
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
                || !(conSymbol.type.tag == TypeTags.OBJECT || conSymbol.type.tag == TypeTags.RECORD)) {
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
            case TypeTags.RECORD:
                fieldType = checkStructLiteralKeyExpr(keyValuePair.key, recType);
                break;
            case TypeTags.MAP:
                fieldType = checkMapLiteralKeyExpr(keyValuePair.key.expr, recType, RecordKind.MAP);
                break;
            case TypeTags.JSON:
                fieldType = checkJSONLiteralKeyExpr(keyValuePair.key, recType, RecordKind.JSON);

                // If the field is again a struct, treat that literal expression as another constraint JSON.
                if (fieldType.tag == TypeTags.OBJECT || fieldType.tag == TypeTags.RECORD) {
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

    private BType checkStructLiteralKeyExpr(BLangRecordKey key, BType recordType) {
        Name fieldName;
        BLangExpression keyExpr = key.expr;

        if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        } else {
            // keys of the struct literal can only be a varRef (identifier)
            dlog.error(keyExpr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_KEY);
            return symTable.errType;
        }

        // Check whether the struct field exists
        BSymbol fieldSymbol = symResolver.resolveStructField(keyExpr.pos, this.env,
                fieldName, recordType.tsymbol);
        if (fieldSymbol == symTable.notFoundSymbol) {
            if (((BRecordType) recordType).sealed) {
                dlog.error(keyExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, fieldName,
                        recordType.tsymbol.type.getKind().typeName(), recordType.tsymbol);
                return symTable.errType;
            }

            return ((BRecordType) recordType).restFieldType;
        }

        return fieldSymbol.type;
    }

    private BType checkJSONLiteralKeyExpr(BLangRecordKey key, BType recordType, RecordKind recKind) {
        BJSONType type = (BJSONType) recordType;

        // If the JSON is constrained with a struct, get the field type from the struct
        if (type.constraint.tag != TypeTags.NONE && type.constraint.tag != TypeTags.ERROR) {
            return checkStructLiteralKeyExpr(key, type.constraint);
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
            indexExpr.type = symTable.errType;
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
                return symTable.errType;
            }

            // Setting the field symbol. This is used during the code generation phase
            varReferExpr.symbol = fieldSymbol;
            return fieldSymbol.type;
        }

        // Assuming this method is only used for objects and records
        if (((BRecordType) structType).sealed) {
            dlog.error(varReferExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD, fieldName,
                    structType.tsymbol.type.getKind().typeName(), structType.tsymbol);
            return symTable.errType;
        }

        return ((BRecordType) structType).restFieldType;
    }

    private BType checkTupleFieldType(BLangIndexBasedAccess indexBasedAccessExpr, BType varRefType, int indexValue) {
        List<BType> tupleTypes = ((BTupleType) varRefType).tupleTypes;
        if (indexValue < 0 || tupleTypes.size() <= indexValue) {
            dlog.error(indexBasedAccessExpr.pos,
                    DiagnosticCode.TUPLE_INDEX_OUT_OF_RANGE, indexValue, tupleTypes.size());
            return symTable.errType;
        }
        return tupleTypes.get(indexValue);
    }

    private BType checkIndexExprForTupleFieldAccess(BLangExpression indexExpr) {
        if (indexExpr.getKind() != NodeKind.LITERAL) {
            indexExpr.type = symTable.errType;
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

    private BType getTypeOfExprInFieldAccess(BLangExpression expr) {
        checkExpr(expr, this.env, symTable.noType);
        return expr.type;
    }

    private BType getAccessExprFinalType(BLangAccessExpression accessExpr, BType actualType) {
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
        if (accessExpr.safeNavigate && (parentType.tag == TypeTags.ERROR || (parentType.tag == TypeTags.UNION &&
                ((BUnionType) parentType).memberTypes.contains(symTable.errStructType)))) {
            unionType.memberTypes.add(symTable.errStructType);
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

    private BType checkFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName) {
        BType actualType = symTable.errType;
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
            case TypeTags.JSON:
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.OBJECT || constraintType.tag == TypeTags.RECORD) {
                    BType fieldType = checkStructFieldAccess(fieldAccessExpr, fieldName, constraintType);

                    // If the type of the field is struct, treat it as constraint JSON type.
                    if (fieldType.tag == TypeTags.OBJECT || fieldType.tag == TypeTags.RECORD) {
                        actualType = new BJSONType(TypeTags.JSON, fieldType, symTable.jsonType.tsymbol);
                        break;
                    }
                }
                actualType = symTable.jsonType;
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
                BType constraintType = ((BJSONType) varRefType).constraint;
                if (constraintType.tag == TypeTags.OBJECT || constraintType.tag == TypeTags.RECORD) {
                    indexExprType = checkIndexExprForStructFieldAccess(indexExpr);
                    if (indexExprType.tag != TypeTags.STRING) {
                        break;
                    }
                    String fieldName = (String) ((BLangLiteral) indexExpr).value;
                    BType fieldType =
                            checkStructFieldAccess(indexBasedAccessExpr, names.fromString(fieldName), constraintType);

                    // If the type of the field is struct, treat it as constraint JSON type.
                    if (fieldType.tag == TypeTags.OBJECT || fieldType.tag == TypeTags.RECORD) {
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
                    indexExpr.type = symTable.errType;
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
            case TypeTags.ERROR:
                indexBasedAccessExpr.indexExpr.type = symTable.errType;
                break;
            default:
                indexBasedAccessExpr.indexExpr.type = symTable.errType;
                dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEXING,
                        indexBasedAccessExpr.expr.type);
        }

        return actualType;
    }

    private BType getSafeType(BType type, BLangAccessExpression accessExpr) {
        if (accessExpr.safeNavigate && type == symTable.errStructType) {
            dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
            return symTable.errType;
        }

        if (type.tag != TypeTags.UNION) {
            return type;
        }

        // Extract the types without the error and null, and revisit access expression
        Set<BType> varRefMemberTypes = ((BUnionType) type).memberTypes;
        List<BType> lhsTypes;

        boolean nullable = false;
        if (accessExpr.safeNavigate) {
            if (!varRefMemberTypes.contains(symTable.errStructType)) {
                dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
                return symTable.errType;
            }

            lhsTypes = varRefMemberTypes.stream().filter(memberType -> {
                return memberType != symTable.errStructType && memberType != symTable.nilType;
            }).collect(Collectors.toList());

            if (lhsTypes.isEmpty()) {
                dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
                return symTable.errType;
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

    private Set<BType> getMatchExpressionTypes(BLangMatchExpression bLangMatchExpression) {
        List<BType> exprTypes = getTypesList(bLangMatchExpression.expr.type);
        Set<BType> matchExprTypes = new LinkedHashSet<>();
        for (BType type : exprTypes) {
            boolean assignable = false;
            for (BLangMatchExprPatternClause pattern : bLangMatchExpression.patternClauses) {
                BType patternExprType = pattern.expr.type;

                // Type of the pattern expression, becomes one of the types of the whole but expression
                matchExprTypes.addAll(getTypesList(patternExprType));

                if (type.tag == TypeTags.ERROR || patternExprType.tag == TypeTags.ERROR) {
                    return new HashSet<>(Lists.of(symTable.errType));
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
}
