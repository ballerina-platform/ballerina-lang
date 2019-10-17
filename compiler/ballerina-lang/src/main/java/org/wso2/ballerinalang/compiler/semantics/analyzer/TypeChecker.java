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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.elements.TableColumnFlag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.clauses.OrderByVariableNode;
import org.ballerinalang.model.tree.clauses.SelectExpressionNode;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.BLangBuiltInMethod;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAnnotationSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BErrorTypeSymbol;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
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
import org.wso2.ballerinalang.compiler.util.ClosureVarSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.NumericLiteralSupport;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.compiler.tree.BLangInvokableNode.DEFAULT_WORKER_NAME;
import static org.wso2.ballerinalang.compiler.util.Constants.WORKER_LAMBDA_VAR_PREFIX;

/**
 * @since 0.94
 */
public class TypeChecker extends BLangNodeVisitor {

    private static final CompilerContext.Key<TypeChecker> TYPE_CHECKER_KEY =
            new CompilerContext.Key<>();
    private static final String TABLE_TNAME = "table";

    private Names names;
    private SymbolTable symTable;
    private SymbolEnter symbolEnter;
    private SymbolResolver symResolver;
    private Types types;
    private BLangDiagnosticLog dlog;
    private SymbolEnv env;
    private boolean isTypeChecked;
    private TypeNarrower typeNarrower;
    private TypeParamAnalyzer typeParamAnalyzer;

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
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeNarrower = TypeNarrower.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
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

        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        DiagnosticCode preDiagCode = this.diagCode;
        this.env = env;
        this.diagCode = diagCode;
        this.expType = expType;
        this.isTypeChecked = true;
        expr.expectedType = expType;

        expr.accept(this);

        expr.type = resultType;
        expr.typeChecked = isTypeChecked;
        this.env = prevEnv;
        this.expType = preExpType;
        this.diagCode = preDiagCode;
        if (resultType.tag != TypeTags.SEMANTIC_ERROR) {
            expr.expectedType = resultType;
        }
        return resultType;
    }


    // Expressions

    public void visit(BLangLiteral literalExpr) {
        BType literalType = setLiteralValueAndGetType(literalExpr, expType);
        if (literalType == symTable.semanticError || literalExpr.isFiniteContext) {
            return;
        }
        resultType = types.checkType(literalExpr, literalType, expType);
    }

    private BType setLiteralValueAndGetType(BLangLiteral literalExpr, BType expType) {
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
                if (!types.isByteLiteralValue((Long) literalValue)) {
                    dlog.error(literalExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, literalType);
                    resultType = symTable.semanticError;
                    return resultType;
                }
                literalType = symTable.byteType;
            } else if (expType.tag == TypeTags.FINITE && types.isAssignableToFiniteType(expType, literalExpr)) {
                BFiniteType finiteType = (BFiniteType) expType;
                if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.INT)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr, symTable.intType);
                    setLiteralValueForFiniteType(literalExpr, valueType);
                    return valueType;
                } else if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.BYTE)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr, symTable.byteType);
                    setLiteralValueForFiniteType(literalExpr, valueType);
                    return valueType;
                } else if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.FLOAT)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr, symTable.floatType);
                    setLiteralValueForFiniteType(literalExpr, valueType);
                    return valueType;
                } else if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.DECIMAL)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr, symTable.decimalType);
                    setLiteralValueForFiniteType(literalExpr, valueType);
                    return valueType;
                }
            } else if (expType.tag == TypeTags.UNION) {
                Set<BType> memberTypes = ((BUnionType) expType).getMemberTypes();
                if (memberTypes.stream()
                        .anyMatch(memType -> memType.tag == TypeTags.INT || memType.tag == TypeTags.JSON ||
                                memType.tag == TypeTags.ANYDATA || memType.tag == TypeTags.ANY)) {
                    return setLiteralValueAndGetType(literalExpr, symTable.intType);
                }

                BType finiteType = getFiniteTypeWithValuesOfSingleType((BUnionType) expType, symTable.intType);
                if (finiteType != symTable.semanticError) {
                    BType setType = setLiteralValueAndGetType(literalExpr, finiteType);
                    if (literalExpr.isFiniteContext) {
                        // i.e., a match was found for a finite type
                        return setType;
                    }
                }

                if (memberTypes.stream().anyMatch(memType -> memType.tag == TypeTags.BYTE)) {
                    return setLiteralValueAndGetType(literalExpr, symTable.byteType);
                }

                finiteType = getFiniteTypeWithValuesOfSingleType((BUnionType) expType, symTable.byteType);
                if (finiteType != symTable.semanticError) {
                    BType setType = setLiteralValueAndGetType(literalExpr, finiteType);
                    if (literalExpr.isFiniteContext) {
                        // i.e., a match was found for a finite type
                        return setType;
                    }
                }

                if (memberTypes.stream().anyMatch(memType -> memType.tag == TypeTags.FLOAT)) {
                    return setLiteralValueAndGetType(literalExpr, symTable.floatType);
                }

                finiteType = getFiniteTypeWithValuesOfSingleType((BUnionType) expType, symTable.floatType);
                if (finiteType != symTable.semanticError) {
                    BType setType = setLiteralValueAndGetType(literalExpr, finiteType);
                    if (literalExpr.isFiniteContext) {
                        // i.e., a match was found for a finite type
                        return setType;
                    }
                }

                if (memberTypes.stream().anyMatch(memType -> memType.tag == TypeTags.DECIMAL)) {
                    return setLiteralValueAndGetType(literalExpr, symTable.decimalType);
                }

                finiteType = getFiniteTypeWithValuesOfSingleType((BUnionType) expType, symTable.decimalType);
                if (finiteType != symTable.semanticError) {
                    BType setType = setLiteralValueAndGetType(literalExpr, finiteType);
                    if (literalExpr.isFiniteContext) {
                        // i.e., a match was found for a finite type
                        return setType;
                    }
                }
            }
        } else if (literalType.tag == TypeTags.FLOAT) {
            String literal = String.valueOf(literalValue);
            String numericLiteral = NumericLiteralSupport.stripDiscriminator(literal);
            boolean isDiscriminatedFloat = NumericLiteralSupport.isFloatDiscriminated(literal);

            if (expType.tag == TypeTags.DECIMAL) {
                // It's illegal to assign discriminated float literal or hex literal to a decimal LHS.
                if (isDiscriminatedFloat || NumericLiteralSupport.isHexLiteral(numericLiteral)) {
                    dlog.error(literalExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, symTable.floatType);
                    resultType = symTable.semanticError;
                    return resultType;
                }
                // LHS expecting decimal value and RHS offer non discriminated float, consider RHS as decimal.
                literalType = symTable.decimalType;
                literalExpr.value = numericLiteral;
            } else if (expType.tag == TypeTags.FLOAT) {
                literalExpr.value = Double.parseDouble(String.valueOf(numericLiteral));
            } else if (expType.tag == TypeTags.FINITE && types.isAssignableToFiniteType(expType, literalExpr)) {
                BFiniteType finiteType = (BFiniteType) expType;
                if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.FLOAT)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr, symTable.floatType);
                    setLiteralValueForFiniteType(literalExpr, valueType);
                    return valueType;
                } else if (!isDiscriminatedFloat
                        && literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.DECIMAL)) {
                    BType valueType = setLiteralValueAndGetType(literalExpr, symTable.decimalType);
                    setLiteralValueForFiniteType(literalExpr, valueType);
                    return valueType;
                }
            } else if (expType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) expType;
                BType unionMember = getAndSetAssignableUnionMember(literalExpr, unionType, symTable.floatType);
                if (unionMember != symTable.noType) {
                    return unionMember;
                }
            }
        } else if (literalType.tag == TypeTags.DECIMAL) {
            return decimalLiteral(literalValue, literalExpr, expType);
        } else {
            if (this.expType.tag == TypeTags.FINITE) {
                boolean foundMember = types.isAssignableToFiniteType(this.expType, literalExpr);
                if (foundMember) {
                    setLiteralValueForFiniteType(literalExpr, literalType);
                    return literalType;
                }
            } else if (this.expType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) this.expType;
                boolean foundMember = unionType.getMemberTypes()
                        .stream()
                        .anyMatch(memberType -> types.isAssignableToFiniteType(memberType, literalExpr));
                if (foundMember) {
                    setLiteralValueForFiniteType(literalExpr, literalType);
                    return literalType;
                }
            }
        }

        if (literalExpr.type.tag == TypeTags.BYTE_ARRAY) {
            // check whether this is a byte array
            literalType = new BArrayType(symTable.byteType);
        }

        return literalType;
    }

    private BType getAndSetAssignableUnionMember(BLangLiteral literalExpr, BUnionType expType, BType desiredType) {
        Set<BType> memberTypes = expType.getMemberTypes();
        if (memberTypes.stream()
                .anyMatch(memType -> memType.tag == desiredType.tag
                        || memType.tag == TypeTags.JSON
                        || memType.tag == TypeTags.ANYDATA
                        || memType.tag == TypeTags.ANY)) {
            return setLiteralValueAndGetType(literalExpr, desiredType);
        }

        BType finiteType = getFiniteTypeWithValuesOfSingleType(expType, symTable.floatType);
        if (finiteType != symTable.semanticError) {
            BType setType = setLiteralValueAndGetType(literalExpr, finiteType);
            if (literalExpr.isFiniteContext) {
                // i.e., a match was found for a finite type
                return setType;
            }
        }

        if (memberTypes.stream().anyMatch(memType -> memType.tag == TypeTags.DECIMAL)) {
            return setLiteralValueAndGetType(literalExpr, symTable.decimalType);
        }

        finiteType = getFiniteTypeWithValuesOfSingleType(expType, symTable.decimalType);
        if (finiteType != symTable.semanticError) {
            BType setType = setLiteralValueAndGetType(literalExpr, finiteType);
            if (literalExpr.isFiniteContext) {
                // i.e., a match was found for a finite type
                return setType;
            }
        }
        return symTable.noType;
    }

    private boolean literalAssignableToFiniteType(BLangLiteral literalExpr, BFiniteType finiteType,
                                                  int targetMemberTypeTag) {
        return finiteType.valueSpace.stream()
                .anyMatch(valueExpr -> valueExpr.type.tag == targetMemberTypeTag &&
                        types.checkLiteralAssignabilityBasedOnType((BLangLiteral) valueExpr, literalExpr));
    }

    private BType decimalLiteral(Object literalValue, BLangLiteral literalExpr, BType expType) {
        String literal = String.valueOf(literalValue);
        if (expType.tag == TypeTags.FLOAT && NumericLiteralSupport.isDecimalDiscriminated(literal)) {
            dlog.error(literalExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, symTable.decimalType);
            resultType = symTable.semanticError;
            return resultType;
        }
        if (expType.tag == TypeTags.FINITE && types.isAssignableToFiniteType(expType, literalExpr)) {
            BFiniteType finiteType = (BFiniteType) expType;
            if (literalAssignableToFiniteType(literalExpr, finiteType, TypeTags.DECIMAL)) {
                BType valueType = setLiteralValueAndGetType(literalExpr, symTable.decimalType);
                setLiteralValueForFiniteType(literalExpr, valueType);
                return valueType;
            }
        } else if (expType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) expType;
            BType unionMember = getAndSetAssignableUnionMember(literalExpr, unionType, symTable.decimalType);
            if (unionMember != symTable.noType) {
                return unionMember;
            }
        }
        literalExpr.value = NumericLiteralSupport.stripDiscriminator(literal);
        resultType = symTable.decimalType;
        return symTable.decimalType;
    }

    private void setLiteralValueForFiniteType(BLangLiteral literalExpr, BType type) {
        types.setImplicitCastExpr(literalExpr, type, this.expType);
        this.resultType = type;
        literalExpr.isFiniteContext = true;
    }

    private BType getFiniteTypeWithValuesOfSingleType(BUnionType unionType, BType matchType) {
        List<BFiniteType> finiteTypeMembers = unionType.getMemberTypes().stream()
                .filter(memType -> memType.tag == TypeTags.FINITE)
                .map(memFiniteType -> (BFiniteType) memFiniteType)
                .collect(Collectors.toList());

        if (finiteTypeMembers.isEmpty()) {
            return symTable.semanticError;
        }

        int tag = matchType.tag;
        Set<BLangExpression> matchedValueSpace = new LinkedHashSet<>();

        for (BFiniteType finiteType : finiteTypeMembers) {
            matchedValueSpace.addAll(finiteType.valueSpace.stream()
                                             .filter(expression -> expression.type.tag == tag)
                                             .collect(Collectors.toSet()));
        }

        if (matchedValueSpace.isEmpty()) {
            return symTable.semanticError;
        }

        return new BFiniteType(null, matchedValueSpace);
    }

    public void visit(BLangTableLiteral tableLiteral) {
        if (expType.tag == symTable.semanticError.tag) {
            return;
        }
        if (expType.getKind() != TypeKind.TABLE) {
            dlog.error(tableLiteral.pos, DiagnosticCode.CANNOT_INFER_TABLE_TYPE);
            resultType = symTable.semanticError;
            return;
        }
        BType tableConstraint = ((BTableType) expType).getConstraint();
        if (tableConstraint.tag == TypeTags.NONE) {
            dlog.error(tableLiteral.pos, DiagnosticCode.TABLE_CANNOT_BE_CREATED_WITHOUT_CONSTRAINT);
            return;
        } else if (tableConstraint.tag != TypeTags.RECORD) {
            dlog.error(tableLiteral.pos, DiagnosticCode.TABLE_CONSTRAINT_MUST_BE_A_RECORD_TYPE);
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

    @Override
    public void visit(BLangListConstructorExpr listConstructor) {
        // Check whether the expected type is an array type
        // var a = []; and var a = [1,2,3,4]; are illegal statements, because we cannot infer the type here.
        BType actualType = symTable.semanticError;

        if ((expType.tag == TypeTags.ANY || expType.tag == TypeTags.ANYDATA || expType.tag == TypeTags.NONE)
                && listConstructor.exprs.isEmpty()) {
            dlog.error(listConstructor.pos, DiagnosticCode.INVALID_LIST_CONSTRUCTOR, expType);
            resultType = symTable.semanticError;
            return;
        }

        int expTypeTag = expType.tag;
        if (expTypeTag == TypeTags.JSON) {
            checkExprs(listConstructor.exprs, this.env, expType);
            actualType = expType;
        } else if (expTypeTag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) expType;
            if (arrayType.state == BArrayState.OPEN_SEALED) {
                arrayType.size = listConstructor.exprs.size();
                arrayType.state = BArrayState.CLOSED_SEALED;
            } else if (arrayType.state != BArrayState.UNSEALED && arrayType.size != listConstructor.exprs.size()) {
                dlog.error(listConstructor.pos,
                        DiagnosticCode.MISMATCHING_ARRAY_LITERAL_VALUES, arrayType.size, listConstructor.exprs.size());
                resultType = symTable.semanticError;
                return;
            }
            checkExprs(listConstructor.exprs, this.env, arrayType.eType);
            actualType = arrayType;
        } else if (expTypeTag == TypeTags.UNION) {
            Set<BType> expTypes = ((BUnionType) expType).getMemberTypes();
            List<BType> matchedTypeList = expTypes.stream()
                    .filter(type -> type.tag == TypeTags.ARRAY || type.tag == TypeTags.TUPLE)
                    .collect(Collectors.toList());
            if (matchedTypeList.isEmpty()) {
                dlog.error(listConstructor.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, actualType);
            } else if (matchedTypeList.size() == 1) {
                // If only one type in the union is an array or tuple, use that as the expected type
                actualType = matchedTypeList.get(0);
                if (actualType.tag == TypeTags.ARRAY) {
                    checkExprs(listConstructor.exprs, this.env, ((BArrayType) actualType).eType);
                } else {
                    BTupleType tupleType = (BTupleType) actualType;
                    List<BType> results = new ArrayList<>();
                    BType restType = null;
                    for (int i = 0; i < listConstructor.exprs.size(); i++) {
                        BType expType, actType;
                        if (i < tupleType.tupleTypes.size()) {
                            expType = tupleType.tupleTypes.get(i);
                            actType = checkExpr(listConstructor.exprs.get(i), env, expType);
                            results.add(expType.tag != TypeTags.NONE ? expType : actType);
                        } else {
                            if (tupleType.restType != null) {
                                restType = checkExpr(listConstructor.exprs.get(i), env, tupleType.restType);
                            } else {
                                // tuple type size != list constructor exprs
                                dlog.error(listConstructor.pos, DiagnosticCode.SYNTAX_ERROR,
                                        "tuple and expression size does not match");
                                resultType = symTable.semanticError;
                                return;
                            }
                        }
                    }
                    actualType = new BTupleType(results);
                    ((BTupleType) actualType).restType = restType;
                }
            } else {
                // If more than one array type, visit the literal to get its type and use that type to filter the
                // compatible array types in the union
                actualType = checkArrayLiteralExpr(listConstructor);
            }
        } else if (expTypeTag == TypeTags.TYPEDESC) {
            // i.e typedesc t = [int, string]
            List<BType> results = new ArrayList<>();
            listConstructor.isTypedescExpr = true;
            for (int i = 0; i < listConstructor.exprs.size(); i++) {
                results.add(checkExpr(listConstructor.exprs.get(i), env, symTable.noType));
            }
            List<BType> actualTypes = new ArrayList<>();
            for (int i = 0; i < listConstructor.exprs.size(); i++) {
                final BLangExpression expr = listConstructor.exprs.get(i);
                if (expr.getKind() == NodeKind.TYPEDESC_EXPRESSION) {
                    actualTypes.add(((BLangTypedescExpr) expr).resolvedType);
                } else if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    actualTypes.add(((BLangSimpleVarRef) expr).symbol.type);
                } else {
                    actualTypes.add(results.get(i));
                }
            }
            if (actualTypes.size() == 1) {
                listConstructor.typedescType = actualTypes.get(0);
            } else {
                listConstructor.typedescType = new BTupleType(actualTypes);
            }
            resultType = new BTypedescType(listConstructor.typedescType, null);
            return;
        } else if (expTypeTag == TypeTags.TUPLE) {
            BTupleType tupleType = (BTupleType) this.expType;
            if ((tupleType.restType != null && (tupleType.tupleTypes.size() > listConstructor.exprs.size()))
                    || (tupleType.restType == null && tupleType.tupleTypes.size() != listConstructor.exprs.size())) {
                dlog.error(listConstructor.pos, DiagnosticCode.SYNTAX_ERROR,
                        "tuple and expression size does not match");
                return;
            }
            List<BType> expTypes = tupleType.tupleTypes;
            List<BType> results = new ArrayList<>();
            BType restType = null;

            // [int, boolean, string...] x = [1, true];
            if (tupleType.restType != null && listConstructor.exprs.size() < expTypes.size() + 1) {
                restType = tupleType.restType;
            }

            for (int i = 0; i < listConstructor.exprs.size(); i++) {
                // Infer type from lhs since lhs might be union
                // TODO: Need to fix with tuple casting
                BType expType, actType;
                if (i < expTypes.size()) {
                    expType = expTypes.get(i);
                    actType = checkExpr(listConstructor.exprs.get(i), env, expType);
                    results.add(expType.tag != TypeTags.NONE ? expType : actType);
                } else {
                    restType = checkExpr(listConstructor.exprs.get(i), env, tupleType.restType);
                }
            }
            actualType = new BTupleType(results);
            ((BTupleType) actualType).restType = restType;
        } else if (listConstructor.exprs.size() > 1) {
            // This is an array.
            List<BType> narrowTypes = new ArrayList<>();
            for (int i = 0; i < listConstructor.exprs.size(); i++) {
                narrowTypes.add(checkExpr(listConstructor.exprs.get(i), env, symTable.noType));
            }
            Set<BType> narrowTypesSet = new LinkedHashSet<>(narrowTypes);
            LinkedHashSet<BType> broadTypesSet = new LinkedHashSet<>();
            BType[] uniqueNarrowTypes = narrowTypesSet.toArray(new BType[0]);
            BType broadType;
            for (BType t1 : uniqueNarrowTypes) {
                broadType = t1;
                for (BType t2 : uniqueNarrowTypes) {
                    if (types.isAssignable(t2, t1)) {
                        broadType = t1;
                    } else if (types.isAssignable(t1, t2)) {
                        broadType = t2;
                    }
                }
                broadTypesSet.add(broadType);
            }
            BType eType;
            if (broadTypesSet.size() > 1) {
                // union type
                eType = BUnionType.create(null, broadTypesSet);
            } else {
                eType = broadTypesSet.toArray(new BType[0])[0];
            }
            BArrayType arrayType = new BArrayType(eType);
            checkExprs(listConstructor.exprs, this.env, arrayType.eType);
            actualType = arrayType;
        } else if (expTypeTag != TypeTags.SEMANTIC_ERROR) {
            actualType = checkArrayLiteralExpr(listConstructor);
        }

        resultType = types.checkType(listConstructor, actualType, expType);
    }

    private BType checkArrayLiteralExpr(BLangListConstructorExpr listConstructorExpr) {
        Set<BType> expTypes;
        if (expType.tag == TypeTags.UNION) {
            expTypes = ((BUnionType) expType).getMemberTypes();
        } else {
            expTypes = new LinkedHashSet<>();
            expTypes.add(expType);
        }
        BType actualType = symTable.noType;
        List<BType> listCompatibleTypes = new ArrayList<>();
        for (BType type : expTypes) {
            if (type.tag == TypeTags.TUPLE) {
                BTupleType tupleType = (BTupleType) type;
                if (checkTupleType(listConstructorExpr, tupleType)) {
                    listCompatibleTypes.add(tupleType);
                }
            } else {
                BType[] uniqueExprTypes = checkArrayExpr(listConstructorExpr, this.env);
                BType arrayLiteralType;
                if (uniqueExprTypes.length == 0) {
                    arrayLiteralType = symTable.anyType;
                } else if (uniqueExprTypes.length == 1) {
                    arrayLiteralType = uniqueExprTypes[0];
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
                if (arrayLiteralType.tag != TypeTags.SEMANTIC_ERROR) {
                    if (type.tag == TypeTags.ARRAY && ((BArrayType) type).state != BArrayState.UNSEALED) {
                        actualType = new BArrayType(arrayLiteralType, null,
                                ((BArrayType) type).state == BArrayState.CLOSED_SEALED
                                        ? listConstructorExpr.exprs.size() : ((BArrayType) type).size,
                                ((BArrayType) type).state);
                    } else {
                        if (type.tag == TypeTags.ARRAY
                                && types.isAssignable(arrayLiteralType, ((BArrayType) type).eType)) {
                            arrayLiteralType = ((BArrayType) type).eType;
                        }
                        actualType = new BArrayType(arrayLiteralType);
                    }
                    listCompatibleTypes.addAll(getListCompatibleTypes(type, actualType));
                }
            }
        }

        if (listCompatibleTypes.isEmpty()) {
            dlog.error(listConstructorExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES, expType, actualType);
            actualType = symTable.semanticError;
        } else if (listCompatibleTypes.size() > 1) {
            dlog.error(listConstructorExpr.pos, DiagnosticCode.AMBIGUOUS_TYPES, expType);
            actualType = symTable.semanticError;
        } else if (listCompatibleTypes.get(0).tag == TypeTags.ANY) {
            dlog.error(listConstructorExpr.pos, DiagnosticCode.INVALID_ARRAY_LITERAL, expType);
            actualType = symTable.semanticError;
        } else if (listCompatibleTypes.get(0).tag == TypeTags.ARRAY) {
            checkExpr(listConstructorExpr, this.env, listCompatibleTypes.get(0));
        } else if (listCompatibleTypes.get(0).tag == TypeTags.TUPLE) {
            actualType = listCompatibleTypes.get(0);
            setTupleType(listConstructorExpr, actualType);
        }
        return actualType;
    }

    private BType[] checkArrayExpr(BLangListConstructorExpr expr, SymbolEnv env) {
        List<BType> types = new ArrayList<>();
        SymbolEnv prevEnv = this.env;
        BType preExpType = this.expType;
        this.env = env;
        this.expType = symTable.noType;
        for (BLangExpression e : expr.exprs) {
            e.accept(this);
            types.add(resultType);
        }
        this.env = prevEnv;
        this.expType = preExpType;
        LinkedHashSet<BType> typesSet = new LinkedHashSet<>(types);
        return typesSet.toArray(new BType[0]);
    }

    private boolean checkTupleType(BLangExpression expression, BType type) {
        if (type.tag == TypeTags.TUPLE && expression.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR
                || expression.getKind() == NodeKind.TUPLE_LITERAL_EXPR) {
            BTupleType tupleType = (BTupleType) type;
            BLangListConstructorExpr tupleExpr = (BLangListConstructorExpr) expression;

            if (tupleType.restType == null && tupleType.tupleTypes.size() != tupleExpr.exprs.size()) {
                return false;
            }

            for (int i = 0; i < tupleExpr.exprs.size(); i++) {
                BLangExpression expr = tupleExpr.exprs.get(i);
                if (i < tupleType.tupleTypes.size()) {
                    if (!checkTupleType(expr, tupleType.tupleTypes.get(i))) {
                        return false;
                    }
                } else {
                    if (tupleType.restType == null || !checkTupleType(expr, tupleType.restType)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            BType sourceType = checkExpr(expression, env);
            if (expression.getKind() == NodeKind.LITERAL && type.getKind() == TypeKind.FINITE) {
                if (types.isAssignableToFiniteType(type, (BLangLiteral) expression)) {
                    return true;
                }
            } else if (expression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef simpleVariable = (BLangSimpleVarRef) expression;
                if (simpleVariable.symbol.getKind() == SymbolKind.CONSTANT) {
                    sourceType = simpleVariable.symbol.type;
                }
            }
            return types.isAssignable(sourceType, type);
        }
    }

    private void setTupleType(BLangExpression expression, BType type) {
        if (type.tag == TypeTags.TUPLE && expression.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR
                || expression.getKind() == NodeKind.TUPLE_LITERAL_EXPR) {
            BTupleType tupleType = (BTupleType) type;
            BLangListConstructorExpr tupleExpr = (BLangListConstructorExpr) expression;
            tupleExpr.type = type;
            if (tupleType.tupleTypes.size() == tupleExpr.exprs.size()) {
                for (int i = 0; i < tupleExpr.exprs.size(); i++) {
                    setTupleType(tupleExpr.exprs.get(i), tupleType.tupleTypes.get(i));
                }
            }
        } else {
            checkExpr(expression, env);
        }
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
            Set<BType> expTypes = ((BUnionType) bType).getMemberTypes();

            List<BType> possibleTypes =
                    expTypes.stream()
                            .filter(type -> type.tag == TypeTags.MAP ||
                                    (type.tag == TypeTags.RECORD &&
                                            (!((BRecordType) type).sealed ||
                                                    isCompatibleClosedRecordLiteral((BRecordType) type,
                                                            recordLiteral))))
                            .collect(Collectors.toList());

            if (expTypes.stream().anyMatch(type -> type.tag == TypeTags.JSON) &&
                    expTypes.stream().noneMatch(type -> type.tag == TypeTags.MAP &&
                            ((BMapType) type).constraint.tag == TypeTags.JSON)) {
                possibleTypes.add(new BMapType(TypeTags.MAP, symTable.jsonType, null));
            }

            return possibleTypes;
        }


        switch (expType.tag) {
            case TypeTags.JSON:
                return Collections.singletonList(new BMapType(TypeTags.MAP, symTable.jsonType, null));
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return Collections.singletonList(bType);
            default:
                return Collections.emptyList();
        }
    }

    private boolean isCompatibleClosedRecordLiteral(BRecordType bRecordType, BLangRecordLiteral recordLiteral) {
        if (!hasRequiredRecordFields(recordLiteral.getKeyValuePairs(), bRecordType)) {
            return false;
        }

        for (BLangRecordKeyValue literalKeyValuePair : recordLiteral.getKeyValuePairs()) {
            boolean matched = false;
            for (BField field : bRecordType.getFields()) {
                matched = field.getName().getValue().equals(getFieldName(literalKeyValuePair.key));
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
                    .anyMatch(keyVal -> field.name.value.equals(getFieldName(keyVal.key)));

            // If a required field is missing, it's a compile error
            if (!hasField && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)) {
                dlog.error(pos, DiagnosticCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
            }
        });
    }

    private String getFieldName(BLangRecordKey key) {
        BLangExpression keyExpression = key.expr;

        if (key.computedKey) {
            return null;
        }

        if (keyExpression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) keyExpression).variableName.value;
        } else if (keyExpression.getKind() == NodeKind.LITERAL) {
            BLangLiteral literal = (BLangLiteral) keyExpression;
            if (literal.type.tag != TypeTags.STRING) {
                return null;
            }
            return (String) literal.value;
        }
        return null;
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

    private List<BType> getListCompatibleTypes(BType expType, BType actualType) {
        Set<BType> expTypes =
                expType.tag == TypeTags.UNION ? ((BUnionType) expType).getMemberTypes() : new LinkedHashSet<BType>() {
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
        BType actualType = BUnionType.create(null, symTable.errorType, symTable.nilType);
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
        if (!syncSendExpr.expr.type.isAnydata()) {
            this.dlog.error(syncSendExpr.pos, DiagnosticCode.INVALID_TYPE_FOR_SEND, syncSendExpr.expr.type);
        }

        String workerName = syncSendExpr.workerIdentifier.getValue();
        if (!this.workerExists(this.env, workerName)) {
            this.dlog.error(syncSendExpr.pos, DiagnosticCode.UNDEFINED_WORKER, workerName);
        }

        syncSendExpr.expectedType = expType;

        // Type checking against the matching receive is done during code analysis.
        // When the expected type is noType, set the result type as nil to avoid variable assignment is required errors.
        resultType = expType == symTable.noType ? symTable.nilType : expType;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveExpr) {
        BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode(workerReceiveExpr.workerIdentifier),
                                                  SymTag.VARIABLE);

        if (workerReceiveExpr.isChannel) {
            this.dlog.error(workerReceiveExpr.pos, DiagnosticCode.UNDEFINED_ACTION);
            return;
        }
        // TODO Need to remove this cached env
        workerReceiveExpr.env = this.env;

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
                varRefExpr.type = this.symTable.anyType;
            } else {
                varRefExpr.type = this.symTable.semanticError;
                dlog.error(varRefExpr.pos, DiagnosticCode.UNDERSCORE_NOT_ALLOWED);
            }
            varRefExpr.symbol = new BVarSymbol(0, varName, env.enclPkg.symbol.pkgID, varRefExpr.type, env.scope.owner);
            resultType = varRefExpr.type;
            return;
        }

        Name compUnitName = getCurrentCompUnit(varRefExpr);
        varRefExpr.pkgSymbol =
                symResolver.resolvePrefixSymbol(env, names.fromIdNode(varRefExpr.pkgAlias), compUnitName);
        if (varRefExpr.pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(varRefExpr.pos, DiagnosticCode.UNDEFINED_MODULE, varRefExpr.pkgAlias);
        }

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
                markAndRegisterClosureVariable(symbol, varRefExpr.pos);
            } else if ((symbol.tag & SymTag.TYPE) == SymTag.TYPE) {
                actualType = new BTypedescType(symbol.type, null);
                varRefExpr.symbol = symbol;
            } else if ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
                varRefExpr.symbol = symbol;
                BType symbolType = symbol.type;
                if (symbolType != symTable.noType && expType.tag == TypeTags.FINITE ||
                        (expType.tag == TypeTags.UNION && ((BUnionType) expType).getMemberTypes().stream()
                                .anyMatch(memType -> memType.tag == TypeTags.FINITE &&
                                        types.isAssignable(symbolType, memType)))) {
                    actualType = symbolType;
                } else {
                    actualType = ((BConstantSymbol) symbol).literalType;
                }

                // If the constant is on the LHS, modifications are not allowed.
                // E.g. m.k = "10"; // where `m` is a constant.
                if (varRefExpr.lhsVar || varRefExpr.compoundAssignmentLhsVar) {
                    actualType = symTable.semanticError;
                    dlog.error(varRefExpr.pos, DiagnosticCode.CANNOT_UPDATE_CONSTANT_VALUE);
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
            if (((BLangVariableReference) recordRefField.variableReference).symbol == null ||
                    !isValidVariableReference(recordRefField.variableReference)) {
                unresolvedReference = true;
                continue;
            }
            BVarSymbol bVarSymbol = (BVarSymbol) ((BLangVariableReference) recordRefField.variableReference).symbol;
            fields.add(new BField(names.fromIdNode(recordRefField.variableName), varRefExpr.pos,
                    new BVarSymbol(0, names.fromIdNode(recordRefField.variableName), env.enclPkg.symbol.pkgID,
                            bVarSymbol.type, recordSymbol)));
        }

        if (varRefExpr.restParam != null) {
            BLangExpression restParam = (BLangExpression) varRefExpr.restParam;
            checkExpr(restParam, env);
            unresolvedReference = !isValidVariableReference(restParam);
        }

        if (unresolvedReference) {
            resultType = symTable.semanticError;
            return;
        }

        BRecordType bRecordType = new BRecordType(recordSymbol);
        bRecordType.fields = fields;
        recordSymbol.type = bRecordType;
        varRefExpr.symbol = new BVarSymbol(0, Names.EMPTY, env.enclPkg.symbol.pkgID, bRecordType, env.scope.owner);

        if (varRefExpr.restParam == null) {
            bRecordType.sealed = true;
        } else {
            bRecordType.restFieldType = symTable.mapType;
        }

        resultType = bRecordType;
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        if (varRefExpr.typeNode != null) {
            BType bType = symResolver.resolveTypeNode(varRefExpr.typeNode, env);
            varRefExpr.type = bType;
            checkIndirectErrorVarRef(varRefExpr);
            resultType = bType;
            return;
        }

        if (varRefExpr.reason != null) {
            varRefExpr.reason.lhsVar = true;
            checkExpr(varRefExpr.reason, env);
        }

        BErrorTypeSymbol errorTSymbol = Symbols.createErrorSymbol(0, Names.EMPTY, env.enclPkg.symbol.pkgID,
                null, env.scope.owner);
        boolean unresolvedReference = false;

        for (BLangNamedArgsExpression detailItem : varRefExpr.detail) {
            BLangVariableReference refItem = (BLangVariableReference) detailItem.expr;
            refItem.lhsVar = true;
            checkExpr(refItem, env);

            if (!isValidVariableReference(refItem)) {
                unresolvedReference = true;
                continue;
            }

            if (refItem.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                    || refItem.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
                dlog.error(refItem.pos, DiagnosticCode.INVALID_VARIABLE_REFERENCE_IN_BINDING_PATTERN, refItem);
                unresolvedReference = true;
                continue;
            }

            if (refItem.symbol == null) {
                unresolvedReference = true;
                continue;
            }
        }

        if (varRefExpr.restVar != null) {
            varRefExpr.restVar.lhsVar = true;
            if (varRefExpr.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                checkExpr(varRefExpr.restVar, env);
                unresolvedReference = unresolvedReference
                        || varRefExpr.restVar.symbol == null
                        || !isValidVariableReference(varRefExpr.restVar);

            } else if (varRefExpr.restVar.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                    || varRefExpr.restVar.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
                unresolvedReference = checkErrorRestParamVarRef(varRefExpr, unresolvedReference);
            }
        }

        if (unresolvedReference) {
            resultType = symTable.semanticError;
            return;
        }

        BType errorRefRestFieldType;
        if (varRefExpr.restVar == null) {
            errorRefRestFieldType = symTable.pureType;
        } else if (varRefExpr.restVar.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                && ((BLangSimpleVarRef) varRefExpr.restVar).variableName.value.equals(Names.IGNORE.value)) {
            errorRefRestFieldType = symTable.pureType;
        } else if (varRefExpr.restVar.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR
            || varRefExpr.restVar.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            errorRefRestFieldType = varRefExpr.restVar.type;
        } else if (varRefExpr.restVar.type.tag == TypeTags.MAP) {
            errorRefRestFieldType = ((BMapType) varRefExpr.restVar.type).constraint;
        } else {
            dlog.error(varRefExpr.restVar.pos, DiagnosticCode.INCOMPATIBLE_TYPES,
                    varRefExpr.restVar.type, symTable.detailType);
            resultType = symTable.semanticError;
            return;
        }

        BType errorDetailType = getCompatibleDetailType(errorRefRestFieldType);
        resultType = new BErrorType(errorTSymbol, varRefExpr.reason.type, errorDetailType);
    }

    private void checkIndirectErrorVarRef(BLangErrorVarRef varRefExpr) {
        for (BLangNamedArgsExpression detailItem : varRefExpr.detail) {
            checkExpr(detailItem.expr, env);
            checkExpr(detailItem, env, detailItem.expr.type);
        }

        if (varRefExpr.restVar != null) {
            checkExpr(varRefExpr.restVar, env);
        }
    }

    private BRecordType getCompatibleDetailType(BType errorRefRestFieldType) {

        PackageID packageID = env.enclPkg.packageID;
        BRecordTypeSymbol detailSymbol = new BRecordTypeSymbol(SymTag.RECORD, Flags.PUBLIC, Names.EMPTY,
                packageID, null, env.scope.owner);
        detailSymbol.scope = new Scope(env.scope.owner);
        BRecordType detailType = new BRecordType(detailSymbol);

        int flags = Flags.asMask(new HashSet<>(Lists.of(Flag.OPTIONAL, Flag.PUBLIC)));
        BField messageField = new BField(Names.DETAIL_MESSAGE, null,
                new BVarSymbol(flags, Names.DETAIL_MESSAGE, packageID, symTable.stringType, detailSymbol));
        detailType.fields.add(messageField);
        detailSymbol.scope.define(Names.DETAIL_MESSAGE, messageField.symbol);

        BField causeField = new BField(Names.DETAIL_CAUSE, null,
                new BVarSymbol(flags, Names.DETAIL_CAUSE, packageID, symTable.errorType, detailSymbol));
        detailType.fields.add(causeField);
        detailSymbol.scope.define(Names.DETAIL_CAUSE, causeField.symbol);

        detailType.restFieldType = errorRefRestFieldType;

        // TODO : Remove this. Had to add this due to BIR codegen requires this.
        BInvokableType invokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initSymbol = Symbols.createFunctionSymbol(0, Names.INIT_FUNCTION_SUFFIX, packageID,
                invokableType, detailSymbol, false);
        detailSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initSymbol, invokableType);
        detailSymbol.scope.define(initSymbol.name, initSymbol);
        return detailType;
    }

    private boolean checkErrorRestParamVarRef(BLangErrorVarRef varRefExpr, boolean unresolvedReference) {
        BLangAccessExpression accessExpression = (BLangAccessExpression) varRefExpr.restVar;
        Name exprName = names.fromIdNode(((BLangSimpleVarRef) accessExpression.expr).variableName);
        BSymbol fSym = symResolver.lookupSymbol(env, exprName, SymTag.VARIABLE);
        if (fSym != null) {
            if (fSym.type.getKind() == TypeKind.MAP) {
                BType constraint = ((BMapType) fSym.type).constraint;
                if (types.isAssignable(constraint, symTable.pureType)) {
                    varRefExpr.restVar.type = constraint;
                } else {
                    varRefExpr.restVar.type = symTable.pureType;
                }
            } else {
                throw new UnsupportedOperationException("rec field base access");
            }
        } else {
            unresolvedReference = true;
        }
        return unresolvedReference;
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        List<BType> results = new ArrayList<>();
        for (int i = 0; i < varRefExpr.expressions.size(); i++) {
            ((BLangVariableReference) varRefExpr.expressions.get(i)).lhsVar = true;
            results.add(checkExpr(varRefExpr.expressions.get(i), env, symTable.noType));
        }
        BTupleType actualType = new BTupleType(results);
        if (varRefExpr.restParam != null) {
            BLangExpression restExpr = (BLangExpression) varRefExpr.restParam;
            ((BLangVariableReference) restExpr).lhsVar = true;
            BType checkedType = checkExpr(restExpr, env, symTable.noType);
            if (checkedType.tag != TypeTags.ARRAY) {
                dlog.error(varRefExpr.pos, DiagnosticCode.INVALID_TYPE_FOR_REST_DESCRIPTOR, checkedType);
                resultType = symTable.semanticError;
                return;
            }
            actualType.restType = ((BArrayType) checkedType).eType;
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

        if (env.enclEnv.node != null && env.enclEnv.node.getKind() == NodeKind.TRANSACTION) {
            // if enclosing env's node is a transaction
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
        ((BLangVariableReference) fieldAccessExpr.expr).compoundAssignmentLhsVar =
                fieldAccessExpr.compoundAssignmentLhsVar;
        BType varRefType = getTypeOfExprInFieldAccess(fieldAccessExpr.expr);

        BType actualType;
        // Accessing all fields using * is only supported for XML.
        if (fieldAccessExpr.fieldKind == FieldKind.ALL && varRefType.tag != TypeTags.XML) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_GET_ALL_FIELDS, varRefType);
            actualType = symTable.semanticError;
        } else {
            if (fieldAccessExpr.optionalFieldAccess) {
                if (fieldAccessExpr.lhsVar || fieldAccessExpr.compoundAssignmentLhsVar) {
                    dlog.error(fieldAccessExpr.pos, DiagnosticCode.OPTIONAL_FIELD_ACCESS_NOT_REQUIRED_ON_LHS);
                    resultType = symTable.semanticError;
                    return;
                }
                actualType = checkOptionalFieldAccessExpr(fieldAccessExpr, varRefType,
                                                          names.fromIdNode(fieldAccessExpr.field));
            } else {
                actualType = checkFieldAccessExpr(fieldAccessExpr, varRefType, names.fromIdNode(fieldAccessExpr.field));
            }
        }
        resultType = types.checkType(fieldAccessExpr, actualType, this.expType);
    }

    public void visit(BLangIndexBasedAccess indexBasedAccessExpr) {
        // First analyze the variable reference expression.
        ((BLangVariableReference) indexBasedAccessExpr.expr).lhsVar = indexBasedAccessExpr.lhsVar;
        ((BLangVariableReference) indexBasedAccessExpr.expr).compoundAssignmentLhsVar =
                indexBasedAccessExpr.compoundAssignmentLhsVar;
        checkExpr(indexBasedAccessExpr.expr, this.env, symTable.noType);

        BType actualType = checkIndexAccessExpr(indexBasedAccessExpr);

        // If this is on lhs, no need to do type checking further. And null/error
        // will not propagate from parent expressions
        if (indexBasedAccessExpr.lhsVar) {
            indexBasedAccessExpr.originalType = actualType;
            indexBasedAccessExpr.type = actualType;
            resultType = actualType;
            return;
        }

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

        if (iExpr.actionInvocation) {
            checkActionInvocationExpr(iExpr, exprType);
            return;
        }

        BType varRefType = iExpr.expr.type;

        switch (varRefType.tag) {
            case TypeTags.OBJECT:
                // Invoking a function bound to an object
                // First check whether there exist a function with this name
                // Then perform arg and param matching
                checkObjectFunctionInvocationExpr(iExpr, (BObjectType) varRefType);
                break;
            case TypeTags.RECORD:
                boolean methodFound = checkFieldFunctionPointer(iExpr);
                if (!methodFound) {
                    checkInLangLib(iExpr, varRefType);
                }
                break;
            case TypeTags.NONE:
                dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, iExpr.name);
                break;
            case TypeTags.SEMANTIC_ERROR:
                break;
            default:
                checkInLangLib(iExpr, varRefType);
        }
    }

    private void checkInLangLib(BLangInvocation iExpr, BType varRefType) {
        boolean langLibMethodExists = checkLangLibMethodInvocationExpr(iExpr, varRefType);
        if (!langLibMethodExists) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, iExpr.name.value);
            resultType = symTable.semanticError;
        }
    }

    private boolean checkFieldFunctionPointer(BLangInvocation iExpr) {
        BType type = checkExpr(iExpr.expr, this.env);
        if (type == symTable.semanticError) {
            return false;
        }
        BSymbol funcSymbol = symResolver.resolveStructField(iExpr.pos, env, names.fromIdNode(iExpr.name), type.tsymbol);
        if (funcSymbol == symTable.notFoundSymbol) {
            return false;
        }
        iExpr.symbol = funcSymbol;
        iExpr.type = ((BInvokableSymbol) funcSymbol).retType;
        checkInvocationParamAndReturnType(iExpr);
        iExpr.functionPointerInvocation = true;
        return true;
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
                    cIExpr.initInvocation.type = ((BInvokableSymbol) cIExpr.initInvocation.symbol).retType;
                } else if (!cIExpr.initInvocation.argExprs.isEmpty()) {
                    // If the initializerFunc is null then this is a default constructor invocation. Hence should not
                    // pass any arguments.
                    dlog.error(cIExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, cIExpr.initInvocation.exprSymbol);
                    cIExpr.initInvocation.argExprs.forEach(expr -> checkExpr(expr, env, symTable.noType));
                    resultType = symTable.semanticError;
                    return;
                }
                break;
            case TypeTags.STREAM:
                if (!cIExpr.initInvocation.argExprs.isEmpty()) {
                    dlog.error(cIExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, cIExpr.initInvocation.name);
                    resultType = symTable.semanticError;
                    return;
                }
                break;
            case TypeTags.UNION:
                List<BType> matchingMembers = findMembersWithMatchingInitFunc(cIExpr, (BUnionType) actualType);
                BType matchedType = getMatchingType(matchingMembers, cIExpr, actualType);
                cIExpr.initInvocation.type = symTable.nilType;

                if (matchedType.tag == TypeTags.OBJECT
                        && ((BObjectTypeSymbol) matchedType.tsymbol).initializerFunc != null) {
                    cIExpr.initInvocation.symbol = ((BObjectTypeSymbol) matchedType.tsymbol).initializerFunc.symbol;
                    checkInvocationParam(cIExpr.initInvocation);
                    cIExpr.initInvocation.type = ((BInvokableSymbol) cIExpr.initInvocation.symbol).retType;
                    actualType = matchedType;
                    break;
                }
                types.checkType(cIExpr, matchedType, expType);
                cIExpr.type = matchedType;
                resultType = matchedType;
                return;
            default:
                dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INFER_OBJECT_TYPE_FROM_LHS, actualType);
                resultType = symTable.semanticError;
                return;
        }

        if (cIExpr.initInvocation.type == null) {
            cIExpr.initInvocation.type = symTable.nilType;
        }
        BType actualTypeInitType = getObjectConstructorReturnType(actualType, cIExpr.initInvocation.type);
        resultType = types.checkType(cIExpr, actualTypeInitType, expType);
    }

    private BType getObjectConstructorReturnType(BType objType, BType initRetType) {
        if (initRetType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> retTypeMembers = new LinkedHashSet<>();
            retTypeMembers.add(objType);

            retTypeMembers.addAll(((BUnionType) initRetType).getMemberTypes());
            retTypeMembers.remove(symTable.nilType);

            BUnionType unionType = BUnionType.create(null, retTypeMembers);
            unionType.tsymbol = Symbols.createTypeSymbol(SymTag.UNION_TYPE, 0,
                                                         Names.EMPTY, env.enclPkg.symbol.pkgID, unionType,
                                                         env.scope.owner);
            return unionType;
        } else if (initRetType.tag == TypeTags.NIL) {
            return objType;
        }
        return symTable.semanticError;
    }

    private List<BType> findMembersWithMatchingInitFunc(BLangTypeInit cIExpr, BUnionType lhsUnionType) {
        boolean containsSingleObject = lhsUnionType.getMemberTypes().stream()
                .filter(x -> x.tag == TypeTags.OBJECT)
                .count() == 1;

        List<BType> matchingLhsMemberTypes = new ArrayList<>();
        for (BType memberType : lhsUnionType.getMemberTypes()) {
            if (memberType.tag != TypeTags.OBJECT) {
                // member is not an object.
                continue;
            }
            if ((memberType.tsymbol.flags & Flags.ABSTRACT) == Flags.ABSTRACT) {
                dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INITIALIZE_ABSTRACT_OBJECT, lhsUnionType.tsymbol);
            }

            if (containsSingleObject) {
                return Collections.singletonList(memberType);
            }

            BAttachedFunction initializerFunc = ((BObjectTypeSymbol) memberType.tsymbol).initializerFunc;
            if (isArgsMatchesFunction(cIExpr.argsExpr, initializerFunc)) {
                matchingLhsMemberTypes.add(memberType);
            }
        }
        return matchingLhsMemberTypes;
    }

    private BType getMatchingType(List<BType> matchingLhsMembers, BLangTypeInit cIExpr, BType lhsUnion) {
        if (matchingLhsMembers.isEmpty()) {
            // No union type member found which matches with initializer expression.
            dlog.error(cIExpr.pos, DiagnosticCode.CANNOT_INFER_OBJECT_TYPE_FROM_LHS, lhsUnion);
            resultType = symTable.semanticError;
            return symTable.semanticError;
        } else if (matchingLhsMembers.size() == 1) {
            // We have a correct match.
            return matchingLhsMembers.get(0).tsymbol.type;
        } else {
            // Multiple matches found.
            dlog.error(cIExpr.pos, DiagnosticCode.AMBIGUOUS_TYPES, lhsUnion);
            resultType = symTable.semanticError;
            return symTable.semanticError;
        }
    }

    private boolean isArgsMatchesFunction(List<BLangExpression> invocationArguments, BAttachedFunction function) {
        invocationArguments.forEach(expr -> checkExpr(expr, env, symTable.noType));

        if (function == null) {
            return invocationArguments.isEmpty();
        }

        if (function.symbol.params.isEmpty() && invocationArguments.isEmpty()) {
            return true;
        }

        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        List<BLangExpression> positionalArgs = new ArrayList<>();
        for (BLangExpression argument : invocationArguments) {
            if (argument.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                namedArgs.add((BLangNamedArgsExpression) argument);
            } else {
                positionalArgs.add(argument);
            }
        }

        List<BVarSymbol> requiredParams = function.symbol.params.stream()
                .filter(param -> !param.defaultableParam)
                .collect(Collectors.toList());
        // Given named and positional arguments are less than required parameters.
        if (requiredParams.size() > invocationArguments.size()) {
            return false;
        }

        List<BVarSymbol> defaultableParams = function.symbol.params.stream()
                .filter(param -> param.defaultableParam)
                .collect(Collectors.toList());

        int givenRequiredParamCount = 0;
        for (int i = 0; i < positionalArgs.size(); i++) {
            if (function.symbol.params.size() > i) {
                givenRequiredParamCount++;
                BVarSymbol functionParam = function.symbol.params.get(i);
                // check the type compatibility of positional args against function params.
                if (!types.isAssignable(positionalArgs.get(i).type, functionParam.type)) {
                    return false;
                }
                requiredParams.remove(functionParam);
                defaultableParams.remove(functionParam);
                continue;
            }

            if (function.symbol.restParam != null) {
                BType restParamType = ((BArrayType) function.symbol.restParam.type).eType;
                if (!types.isAssignable(positionalArgs.get(i).type, restParamType)) {
                    return false;
                }
                continue;
            }

            // additional positional args given for function with no rest param
            return false;
        }

        for (BLangNamedArgsExpression namedArg : namedArgs) {
            boolean foundNamedArg = false;
            // check the type compatibility of named args against function params.
            List<BVarSymbol> params = function.symbol.params;
            for (int i = givenRequiredParamCount; i < params.size(); i++) {
                BVarSymbol functionParam = params.get(i);
                if (!namedArg.name.value.equals(functionParam.name.value)) {
                    continue;
                }
                foundNamedArg = true;
                BType namedArgExprType = checkExpr(namedArg.expr, env);
                if (!types.isAssignable(functionParam.type, namedArgExprType)) {
                    // Name matched, type mismatched.
                    return false;
                }
                requiredParams.remove(functionParam);
                defaultableParams.remove(functionParam);
            }
            if (!foundNamedArg) {
                return false;
            }
        }

        // all required params are not given by positional or named args.
        return requiredParams.size() <= 0;
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
                BUnionType constraintTypeForMap = BUnionType.create(null, memberTypesForMap);
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
                BUnionType constraintType = BUnionType.create(null, memberTypes);
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
            if (bType.tag == TypeTags.FUTURE) {
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
            BSymbol symbol = symResolver.lookupSymbol(env, names.fromIdNode
                            (((BLangSimpleVarRef) keyVal.keyExpr).variableName), SymTag.VARIABLE);
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
                resultType = BUnionType.create(null, memberTypes);
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
        for (BType memberType : unionType.getMemberTypes()) {
            if (memberType.tag == TypeTags.FUTURE) {
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
                resultTypes.addAll(((BUnionType) exprType).getMemberTypes());
            } else {
                resultTypes.add(exprType);
            }
            resultTypes.add(symTable.errorType);
            actualType = BUnionType.create(null, resultTypes);
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
            resultType = BUnionType.create(null, lhsResultType, rhsResultType);
            return;
        }

        checkDecimalCompatibilityForBinaryArithmeticOverLiteralValues(binaryExpr);

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
                if ((binaryExpr.opKind == OperatorKind.EQUAL || binaryExpr.opKind == OperatorKind.NOT_EQUAL) &&
                        (couldHoldTableValues(lhsType, new ArrayList<>()) &&
                                 couldHoldTableValues(rhsType, new ArrayList<>()))) {
                    dlog.error(binaryExpr.pos, DiagnosticCode.EQUALITY_NOT_YET_SUPPORTED, TABLE_TNAME);
                }

                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
                actualType = opSymbol.type.getReturnType();
            }
        }

        resultType = types.checkType(binaryExpr, actualType, expType);
    }

    private void checkDecimalCompatibilityForBinaryArithmeticOverLiteralValues(BLangBinaryExpr binaryExpr) {
        if (expType.tag != TypeTags.DECIMAL) {
            return;
        }

        switch (binaryExpr.opKind) {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                checkExpr(binaryExpr.lhsExpr, env, expType);
                checkExpr(binaryExpr.rhsExpr, env, expType);
                break;
            default:
                break;
        }
    }

    public void visit(BLangElvisExpr elvisExpr) {
        BType lhsType = checkExpr(elvisExpr.lhsExpr, env);
        BType actualType = symTable.semanticError;
        if (lhsType != symTable.semanticError) {
            if (lhsType.tag == TypeTags.UNION && lhsType.isNullable()) {
                BUnionType unionType = (BUnionType) lhsType;
                LinkedHashSet<BType> memberTypes = unionType.getMemberTypes().stream()
                        .filter(type -> type.tag != TypeTags.NIL)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (memberTypes.size() == 1) {
                    actualType = memberTypes.toArray(new BType[0])[0];
                } else {
                    actualType = BUnionType.create(null, memberTypes);
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
    public void visit(BLangGroupExpr groupExpr) {
        resultType = checkExpr(groupExpr.expression, env, expType);
    }

    public void visit(BLangTypedescExpr accessExpr) {
        accessExpr.resolvedType = symResolver.resolveTypeNode(accessExpr.typeNode, env);
        int resolveTypeTag = accessExpr.resolvedType.tag;
        final BType actualType;

        if (resolveTypeTag != TypeTags.TYPEDESC && resolveTypeTag != TypeTags.NONE) {
            actualType = new BTypedescType(accessExpr.resolvedType, null);
        } else {
            actualType = accessExpr.resolvedType;
        }
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
        } else if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            exprType = checkExpr(unaryExpr.expr, env);
            if (exprType != symTable.semanticError) {
                actualType = new BTypedescType(exprType, null);
            }
        } else {
            exprType = OperatorKind.ADD.equals(unaryExpr.operator) ? checkExpr(unaryExpr.expr, env, expType) :
                    checkExpr(unaryExpr.expr, env);
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

        // Annotation such as <@untainted [T]>, where T is not provided,
        // it's merely a annotation on contextually expected type.
        if (conversionExpr.typeNode == null && !conversionExpr.annAttachments.isEmpty()) {
            BType expType = checkExpr(conversionExpr.expr, env, this.expType);
            resultType = expType;
            return;
        }

        BType targetType = symResolver.resolveTypeNode(conversionExpr.typeNode, env);
        conversionExpr.targetType = targetType;
        BType expType = requireTypeInference(conversionExpr.expr) ? targetType : symTable.noType;
        BType sourceType = checkExpr(conversionExpr.expr, env, expType);

        BSymbol symbol = symResolver.resolveTypeCastOperator(conversionExpr.expr, sourceType, targetType);

        if (symbol == symTable.notFoundSymbol) {
            dlog.error(conversionExpr.pos, DiagnosticCode.INCOMPATIBLE_TYPES_CAST, sourceType, targetType);
        } else {
            conversionExpr.conversionSymbol = (BOperatorSymbol) symbol;
            // We reach this block only if the cast is valid, so we set the target type as the actual type.
            actualType = targetType;
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
            BType invokableType = unionType.getMemberTypes().stream().filter(type -> type.tag == TypeTags.INVOKABLE)
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

        // Visit in-line namespace declarations and define the namespace.
        for (BLangXMLAttribute attribute : bLangXMLElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME && isXmlNamespaceAttribute(attribute)) {
                BLangXMLQuotedString value = attribute.value;
                if (value.getKind() == NodeKind.XML_QUOTED_STRING && value.textFragments.size() > 1) {
                    dlog.error(value.pos, DiagnosticCode.INVALID_XML_NS_INTERPOLATION);
                }
                checkExpr(attribute, xmlElementEnv, symTable.noType);
            }
        }

        // Visit attributes, this may depend on the namespace defined in previous attribute iteration.
        bLangXMLElementLiteral.attributes.forEach(attribute -> {
            if (!(attribute.name.getKind() == NodeKind.XML_QNAME && isXmlNamespaceAttribute(attribute))) {
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

    private boolean isXmlNamespaceAttribute(BLangXMLAttribute attribute) {
        BLangXMLQName attrName = (BLangXMLQName) attribute.name;
        return (attrName.prefix.value.isEmpty()
                    && attrName.localname.value.equals(XMLConstants.XMLNS_ATTRIBUTE))
                || attrName.prefix.value.equals(XMLConstants.XMLNS_ATTRIBUTE);
    }

    public void visit(BLangXMLTextLiteral bLangXMLTextLiteral) {
        checkStringTemplateExprs(bLangXMLTextLiteral.textFragments, false);
        resultType = types.checkType(bLangXMLTextLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLCommentLiteral bLangXMLCommentLiteral) {
        checkStringTemplateExprs(bLangXMLCommentLiteral.textFragments, false);
        resultType = types.checkType(bLangXMLCommentLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLProcInsLiteral bLangXMLProcInsLiteral) {
        checkExpr(bLangXMLProcInsLiteral.target, env, symTable.stringType);
        checkStringTemplateExprs(bLangXMLProcInsLiteral.dataFragments, false);
        resultType = types.checkType(bLangXMLProcInsLiteral, symTable.xmlType, expType);
    }

    public void visit(BLangXMLQuotedString bLangXMLQuotedString) {
        checkStringTemplateExprs(bLangXMLQuotedString.textFragments, false);
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
                actualType = BUnionType.create(null, symTable.mapStringType, symTable.nilType);
            }
            resultType = types.checkType(xmlAttributeAccessExpr, actualType, expType);
            return;
        }

        checkExpr(indexExpr, env, symTable.stringType);

        if (indexExpr.type.tag == TypeTags.STRING) {
            if (xmlAttributeAccessExpr.lhsVar) {
                actualType = symTable.stringType;
            } else {
                actualType = BUnionType.create(null, symTable.stringType, symTable.nilType);
            }
        }

        xmlAttributeAccessExpr.namespaces.putAll(symResolver.resolveAllNamespaces(env));
        resultType = types.checkType(xmlAttributeAccessExpr, actualType, expType);
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        checkStringTemplateExprs(stringTemplateLiteral.exprs, false);
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
            actualType = matchExprTypes.toArray(new BType[0])[0];
        } else {
            actualType = BUnionType.create(null, matchExprTypes);
        }

        resultType = types.checkType(bLangMatchExpression, actualType, expType);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        visitCheckAndCheckPanicExpr(checkedExpr);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkedExpr) {
        visitCheckAndCheckPanicExpr(checkedExpr);
    }

    private void visitCheckAndCheckPanicExpr(BLangCheckedExpr checkedExpr) {
        String operatorType = checkedExpr.getKind() == NodeKind.CHECK_EXPR ? "check" : "checkpanic";
        boolean firstVisit = checkedExpr.expr.type == null;
        BType exprExpType;
        if (expType == symTable.noType) {
            exprExpType = symTable.noType;
        } else {
            exprExpType = BUnionType.create(null, expType, symTable.errorType);
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
                dlog.error(checkedExpr.expr.pos,
                        DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS, operatorType);
            } else if (exprType != symTable.semanticError) {
                dlog.error(checkedExpr.expr.pos,
                        DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS, operatorType);
            }
            checkedExpr.type = symTable.semanticError;
            return;
        }

        BUnionType unionType = (BUnionType) exprType;
        // Filter out the list of types which are not equivalent with the error type.
        Map<Boolean, List<BType>> resultTypeMap = unionType.getMemberTypes().stream()
                .collect(Collectors.groupingBy(memberType -> types.isAssignable(memberType, symTable.errorType)));

        // This list will be used in the desugar phase
        checkedExpr.equivalentErrorTypeList = resultTypeMap.get(true);
        if (checkedExpr.equivalentErrorTypeList == null ||
                checkedExpr.equivalentErrorTypeList.size() == 0) {
            // No member types in this union is equivalent to the error type
            dlog.error(checkedExpr.expr.pos,
                    DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_NO_ERROR_TYPE_IN_RHS, operatorType);
            checkedExpr.type = symTable.semanticError;
            return;
        }

        List<BType> nonErrorTypeList = resultTypeMap.get(false);
        if (nonErrorTypeList == null || nonErrorTypeList.size() == 0) {
            // All member types in the union are equivalent to the error type.
            // Checked expression requires at least one type which is not equivalent to the error type.
            dlog.error(checkedExpr.expr.pos,
                    DiagnosticCode.CHECKED_EXPR_INVALID_USAGE_ALL_ERROR_TYPES_IN_RHS, operatorType);
            checkedExpr.type = symTable.semanticError;
            return;
        }

        BType actualType;
        if (nonErrorTypeList.size() == 1) {
            actualType = nonErrorTypeList.get(0);
        } else {
            actualType = BUnionType.create(null, new LinkedHashSet<>(nonErrorTypeList));
        }

        resultType = types.checkType(checkedExpr, actualType, expType);
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

    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        checkExpr(annotAccessExpr.expr, this.env, symTable.typeDesc);

        BType actualType = symTable.semanticError;
        BSymbol symbol =
                this.symResolver.resolveAnnotation(annotAccessExpr.pos, env,
                        names.fromString(annotAccessExpr.pkgAlias.getValue()),
                        names.fromString(annotAccessExpr.annotationName.getValue()));
        if (symbol == this.symTable.notFoundSymbol) {
            this.dlog.error(annotAccessExpr.pos, DiagnosticCode.UNDEFINED_ANNOTATION,
                            annotAccessExpr.annotationName.getValue());
        } else {
            annotAccessExpr.annotationSymbol = (BAnnotationSymbol) symbol;
            BType annotType = ((BAnnotationSymbol) symbol).attachedType == null ? symTable.trueType :
                    ((BAnnotationSymbol) symbol).attachedType.type;
            actualType = BUnionType.create(null, annotType, symTable.nilType);
        }

        this.resultType = this.types.checkType(annotAccessExpr, actualType, this.expType);
    }

    // Private methods

    private boolean isValidVariableReference(BLangExpression varRef) {
        switch (varRef.getKind()) {
            case SIMPLE_VARIABLE_REF:
            case RECORD_VARIABLE_REF:
            case TUPLE_VARIABLE_REF:
            case ERROR_VARIABLE_REF:
            case FIELD_BASED_ACCESS_EXPR:
            case INDEX_BASED_ACCESS_EXPR:
            case XML_ATTRIBUTE_ACCESS_EXPR:
                return true;
            default:
                dlog.error(varRef.pos, DiagnosticCode.INVALID_RECORD_BINDING_PATTERN, varRef.type);
                return false;
        }
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

        BSymbol pkgSymbol = symResolver.resolvePrefixSymbol(env, pkgAlias, getCurrentCompUnit(iExpr));
        if (pkgSymbol == symTable.notFoundSymbol) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_MODULE, pkgAlias);
        } else {
            if (funcSymbol == symTable.notFoundSymbol) {
                funcSymbol =
                        symResolver.lookupSymbolInPackage(iExpr.pos, env, pkgAlias, funcName, SymTag.VARIABLE);
            }
            if (funcSymbol == symTable.notFoundSymbol) {
                funcSymbol = symResolver.lookupSymbolInPackage(iExpr.pos, env, pkgAlias, funcName, SymTag.CONSTRUCTOR);
            }
        }

        if ((funcSymbol.tag & SymTag.ERROR) == SymTag.ERROR
            || ((funcSymbol.tag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR && funcSymbol.type.tag == TypeTags.ERROR)) {
            iExpr.symbol = funcSymbol;
            iExpr.type = funcSymbol.type;
            checkErrorConstructorInvocation(iExpr);
            return;
        } else if (funcSymbol == symTable.notFoundSymbol || isNotFunction(funcSymbol)) {
            dlog.error(iExpr.pos, DiagnosticCode.UNDEFINED_FUNCTION, funcName);
            iExpr.argExprs.forEach(arg -> checkExpr(arg, env));
            resultType = symTable.semanticError;
            return;
        }
        if (isFunctionPointer(funcSymbol)) {
            iExpr.functionPointerInvocation = true;
            markAndRegisterClosureVariable(funcSymbol, iExpr.pos);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION_SYNTAX);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.RESOURCE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_RESOURCE_FUNCTION_INVOCATION);
        }

        if (PackageID.isLangLibPackageID(pkgSymbol.pkgID)) {
            // This will enable, type param support, if the function is called directly.
            this.env = SymbolEnv.createInvocationEnv(iExpr, this.env);
        }
        // Set the resolved function symbol in the invocation expression.
        // This is used in the code generation phase.
        iExpr.symbol = funcSymbol;
        checkInvocationParamAndReturnType(iExpr);
    }

    private void markAndRegisterClosureVariable(BSymbol symbol, DiagnosticPos pos) {
        BLangInvokableNode encInvokable = env.enclInvokable;
        if (symbol.owner instanceof BPackageSymbol) {
            return;
        }
        if (encInvokable != null && encInvokable.flagSet.contains(Flag.LAMBDA)
                && !isFunctionArgument(symbol, encInvokable.requiredParams)) {
            SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
            BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name, SymTag.VARIABLE);
            if (resolvedSymbol != symTable.notFoundSymbol && !encInvokable.flagSet.contains(Flag.ATTACHED)) {
                resolvedSymbol.closure = true;
                ((BLangFunction) encInvokable).closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
            }
        }
        if (env.node.getKind() == NodeKind.ARROW_EXPR
                && !isFunctionArgument(symbol, ((BLangArrowFunction) env.node).params)) {
            SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
            BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name, SymTag.VARIABLE);
            if (resolvedSymbol != symTable.notFoundSymbol) {
                resolvedSymbol.closure = true;
                ((BLangArrowFunction) env.node).closureVarSymbols.add(new ClosureVarSymbol(resolvedSymbol, pos));
            }
        }

        // Iterate through parent nodes until a function node is met to find if the variable is used inside
        // a transaction block to mark it as a closure
        BLangNode node = env.node;
        SymbolEnv cEnv = env;
        while (node != null && node.getKind() != NodeKind.FUNCTION) {
            if (node.getKind() == NodeKind.TRANSACTION) {
                SymbolEnv encInvokableEnv = findEnclosingInvokableEnv(env, encInvokable);
                BSymbol resolvedSymbol = symResolver.lookupClosureVarSymbol(encInvokableEnv, symbol.name,
                        SymTag.VARIABLE);
                if (resolvedSymbol != symTable.notFoundSymbol) {
                    resolvedSymbol.closure = true;
                }
                break;
            } else {
                SymbolEnv enclEnv = cEnv.enclEnv;
                if (enclEnv == null) {
                    break;
                }
                cEnv = enclEnv;
                node = cEnv.node;
            }
        }
    }

    private boolean isNotFunction(BSymbol funcSymbol) {
        if ((funcSymbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION) {
            return false;
        }

        if (isFunctionPointer(funcSymbol)) {
            return false;
        }

        return true;
    }

    private boolean isFunctionPointer(BSymbol funcSymbol) {
        if ((funcSymbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION) {
            return false;
        }
        return (funcSymbol.tag & SymTag.FUNCTION) == SymTag.VARIABLE
                && funcSymbol.kind == SymbolKind.FUNCTION
                && (funcSymbol.flags & Flags.NATIVE) != Flags.NATIVE;
    }

    private void checkErrorConstructorInvocation(BLangInvocation iExpr) {
        BType expectedType = this.expType;

        if (expType.getKind() == TypeKind.UNION) {
            BType[] errorMembers = ((BUnionType) expectedType).getMemberTypes()
                    .stream()
                    .filter(memberType -> types.isAssignable(memberType, symTable.errorType))
                    .toArray(BType[]::new);
            if (errorMembers.length > 0) {
                expectedType = BUnionType.create(null, errorMembers);
            }
        }

        if (expType.getKind() == TypeKind.UNION && iExpr.symbol.type == symTable.errorType) {
            BUnionType unionType = (BUnionType) expType;
            long count = unionType.getMemberTypes().stream()
                    .filter(member -> types.isAssignable(iExpr.symbol.type, member))
                    .count();
            // More than one compatible members found, this is ambiguous.
            if (count > 1) {
                dlog.error(iExpr.pos, DiagnosticCode.CANNOT_INFER_ERROR_TYPE, this.expType);
                return;
            }
        } else if (!types.isAssignable(expectedType, symTable.errorType)) {
            if ((iExpr.symbol.tag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR) {
                expectedType = iExpr.type;
            } else if (expectedType != symTable.noType) {
                // Cannot infer error type from error constructor. 'T1|T2|T3 e = error("r", a="b", b="c");
                dlog.error(iExpr.pos, DiagnosticCode.CANNOT_INFER_ERROR_TYPE, this.expType);
                resultType = symTable.semanticError;
                return;
            } else {
                // var e = <error> error("r");
                expectedType = symTable.errorType;
            }
        }

        BErrorType expectedError = getExpectedErrorType(iExpr.pos, expectedType, iExpr.symbol);
        if (expectedError == null) {
            return;
        }

        if (iExpr.argExprs.isEmpty() && checkNoArgErrorCtorInvocation(expectedError, iExpr.name, iExpr.pos)) {
            return;
        }

        if (nonNamedArgsGiven(iExpr) && (iExpr.symbol.tag & SymTag.CONSTRUCTOR) == SymTag.CONSTRUCTOR) {
            dlog.error(iExpr.argExprs.get(0).pos, DiagnosticCode.INDIRECT_ERROR_CTOR_REASON_NOT_ALLOWED);
            resultType = symTable.semanticError;
            return;
        }

        boolean reasonArgGiven = checkErrorReasonArg(iExpr, expectedError);

        if (expectedError.detailType.tag == TypeTags.RECORD) {
            BRecordType targetErrorDetailRec = (BRecordType) expectedError.detailType;
            BRecordType recordType = createErrorDetailRecordType(iExpr, reasonArgGiven, targetErrorDetailRec);
            if (resultType == symTable.semanticError) {
                return;
            }

            if (!types.isAssignable(recordType, targetErrorDetailRec)) {
                dlog.error(iExpr.pos, DiagnosticCode.INVALID_ERROR_CONSTRUCTOR_DETAIL, iExpr);
                resultType = symTable.semanticError;
                return;
            }
        } else {
            // This is when there is a semantic error in error type, bail out!
            return;
        }
        setErrorReasonParam(iExpr, reasonArgGiven, expectedError);
        setErrorDetailArgsToNamedArgsList(iExpr);

        resultType = expectedError;
        if (iExpr.symbol == symTable.errorType.tsymbol) {
            iExpr.symbol = ((BErrorTypeSymbol) expectedError.tsymbol).ctorSymbol;
        }
    }

    private BErrorType getExpectedErrorType(DiagnosticPos pos, BType expType, BSymbol iExprSymbol) {
        // Direct error ctor invocation.
        if (iExprSymbol == symTable.errorType.tsymbol) {
            if (expType.tag == TypeTags.UNION) {
                List<BType> matchedErrors = ((BUnionType) expType).getMemberTypes().stream()
                        .filter(m -> types.isAssignable(m, iExprSymbol.type))
                        .collect(Collectors.toList());
                if (matchedErrors.size() == 1) {
                    return (BErrorType) matchedErrors.get(0);
                } else {
                    // More than one matched, Cannot infer error type from error constructor.
                    // 'Error0|Error1|T3 e = error(...);
                    dlog.error(pos, DiagnosticCode.CANNOT_INFER_ERROR_TYPE, expType);
                    resultType = symTable.semanticError;
                    return null;
                }
            }
            return (BErrorType) expType;
        } else {
            // Indirect error constructor.
            return (BErrorType) iExprSymbol.type;
        }
    }

    private boolean nonNamedArgsGiven(BLangInvocation iExpr) {
        return iExpr.argExprs.stream().anyMatch(arg -> arg.getKind() != NodeKind.NAMED_ARGS_EXPR);
    }

    private boolean checkErrorReasonArg(BLangInvocation iExpr, BErrorType ctorType) {
        // User defined error
        if (iExpr.type != symTable.errorType) {
            if (ctorType.reasonType.getKind() != TypeKind.FINITE) {
                dlog.error(iExpr.pos, DiagnosticCode.INDIRECT_ERROR_CTOR_NOT_ALLOWED_ON_NON_CONST_REASON,
                        iExpr.type);
                return false;
            } else {
                BFiniteType reasonType = (BFiniteType) ctorType.reasonType;
                if (reasonType.valueSpace.size() > 1) {
                    dlog.error(iExpr.pos, DiagnosticCode.INDIRECT_ERROR_CTOR_NOT_ALLOWED_ON_NON_CONST_REASON,
                            iExpr.type);
                    return false;
                }
            }
        }
        if (iExpr.argExprs.isEmpty()) {
            return false;
        }

        // if present, error reason should be the first and only positional argument to error constructor.
        BLangExpression firstErrorArg = iExpr.argExprs.get(0);
        if (firstErrorArg.getKind() != NodeKind.NAMED_ARGS_EXPR) {
            checkExpr(firstErrorArg, env, ctorType.reasonType, DiagnosticCode.INVALID_ERROR_REASON_TYPE);
            return true;
        } else if (iExpr.type == symTable.errorType) {
            dlog.error(iExpr.pos, DiagnosticCode.DIRECT_ERROR_CTOR_REASON_NOT_PROVIDED);
        }
        return false;
    }

    private boolean checkNoArgErrorCtorInvocation(BErrorType errorType, BLangIdentifier name, DiagnosticPos pos) {
        if (errorType.reasonType.tag != TypeTags.FINITE) {
            dlog.error(pos, DiagnosticCode.INDIRECT_ERROR_CTOR_NOT_ALLOWED_ON_NON_CONST_REASON, name);
            resultType = symTable.semanticError;
            return true;
        } else {
            BFiniteType finiteType = (BFiniteType) errorType.reasonType;
            if (finiteType.valueSpace.size() != 1) {
                if (errorType == symTable.errorType) {
                    dlog.error(pos, DiagnosticCode.CANNOT_INFER_ERROR_TYPE, expType.tsymbol.name);
                } else {
                    dlog.error(pos, DiagnosticCode.INDIRECT_ERROR_CTOR_NOT_ALLOWED_ON_NON_CONST_REASON,
                            expType.tsymbol.name);
                }
                resultType = symTable.semanticError;
                return true;
            }
        }
        return false;
    }

    private void setErrorDetailArgsToNamedArgsList(BLangInvocation iExpr) {
        List<BLangExpression> namedArgPositions = new ArrayList<>(iExpr.argExprs.size());
        for (int i = 0; i < iExpr.argExprs.size(); i++) {
            BLangExpression argExpr = iExpr.argExprs.get(i);
            checkExpr(argExpr, env, symTable.pureType);
            if (argExpr.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                iExpr.requiredArgs.add(argExpr);
                namedArgPositions.add(argExpr);
            } else {
                dlog.error(argExpr.pos, DiagnosticCode.ERROR_DETAIL_ARG_IS_NOT_NAMED_ARG);
                resultType = symTable.semanticError;
            }
        }

        for (BLangExpression expr : namedArgPositions) {
            // This check is to filter out additional field assignments when Error detail type is a open record.
            iExpr.argExprs.remove(expr);
        }
    }

    private void setErrorReasonParam(BLangInvocation iExpr, boolean reasonArgGiven, BErrorType ctorType) {
        if (!reasonArgGiven && ctorType.reasonType.getKind() == TypeKind.FINITE) {
            BFiniteType finiteType = (BFiniteType) ctorType.reasonType;
            BLangExpression reasonExpr = (BLangExpression) finiteType.valueSpace.toArray()[0];
            iExpr.requiredArgs.add(reasonExpr);
            return;
        }
        iExpr.requiredArgs.add(iExpr.argExprs.get(0));
        iExpr.argExprs.remove(0);
    }

    /**
     * Create a error detail record using all metadata from {@code targetErrorDetailsType} and put actual error details
     * from {@code iExpr} expression.
     *
     * @param iExpr error constructor invocation
     * @param reasonArgGiven error reason is provided as first argument
     * @param targetErrorDetailsType target error details type to extract metadata such as pkgId from
     * @return error detail record
     */
    // todo: try to re-use recrod literal checking
    private BRecordType createErrorDetailRecordType(BLangInvocation iExpr, boolean reasonArgGiven,
                                                    BRecordType targetErrorDetailsType) {
        List<BLangNamedArgsExpression> namedArgs = getProvidedErrorDetails(iExpr, reasonArgGiven);
        if (namedArgs == null) {
            // error in provided error details
            return null;
        }
        BRecordTypeSymbol recordTypeSymbol = new BRecordTypeSymbol(
                SymTag.RECORD, targetErrorDetailsType.tsymbol.flags, Names.EMPTY, targetErrorDetailsType.tsymbol.pkgID,
                symTable.recordType, null);
        BRecordType recordType = new BRecordType(recordTypeSymbol);
        recordType.sealed = targetErrorDetailsType.sealed;
        recordType.restFieldType = targetErrorDetailsType.restFieldType;

        Set<Name> availableErrorDetailFields = new HashSet<>();
        for (BLangNamedArgsExpression arg : namedArgs) {
            Name fieldName = names.fromIdNode(arg.name);
            BField field = new BField(fieldName, arg.pos, new BVarSymbol(0, fieldName, null, arg.type, null));
            recordType.fields.add(field);
            availableErrorDetailFields.add(fieldName);
        }

        for (BField field : targetErrorDetailsType.fields) {
            boolean notRequired = (field.symbol.flags & Flags.REQUIRED) != Flags.REQUIRED;
            if (notRequired && !availableErrorDetailFields.contains(field.name)) {
                BField defaultableField = new BField(field.name, iExpr.pos,
                        new BVarSymbol(field.symbol.flags, field.name, null, field.type, null));
                recordType.fields.add(defaultableField);
            }
        }

        return recordType;
    }

    private List<BLangNamedArgsExpression> getProvidedErrorDetails(BLangInvocation iExpr, boolean reasonArgGiven) {
        List<BLangNamedArgsExpression> namedArgs = new ArrayList<>();
        for (int i = reasonArgGiven ? 1 : 0; i < iExpr.argExprs.size(); i++) {
            BLangExpression argExpr = iExpr.argExprs.get(i);
            checkExpr(argExpr, env);
            if (argExpr.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                dlog.error(argExpr.pos, DiagnosticCode.ERROR_DETAIL_ARG_IS_NOT_NAMED_ARG);
                resultType = symTable.semanticError;
                return null;
            }

            namedArgs.add((BLangNamedArgsExpression) argExpr);
        }
        return namedArgs;
    }

    private void checkObjectFunctionInvocationExpr(BLangInvocation iExpr, BObjectType objectType) {
        if (objectType.getKind() == TypeKind.SERVICE &&
                !(iExpr.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                (Names.SELF.equals(((BLangSimpleVarRef) iExpr.expr).symbol.name)))) {
            dlog.error(iExpr.pos, DiagnosticCode.SERVICE_FUNCTION_INVALID_INVOCATION);
            return;
        }
        // check for object attached function
        Name funcName =
                names.fromString(Symbols.getAttachedFuncSymbolName(objectType.tsymbol.name.value, iExpr.name.value));
        BSymbol funcSymbol =
                symResolver.resolveObjectMethod(iExpr.pos, env, funcName, (BObjectTypeSymbol) objectType.tsymbol);
        if (funcSymbol == symTable.notFoundSymbol || funcSymbol.type.tag != TypeTags.INVOKABLE) {
            if (!checkLangLibMethodInvocationExpr(iExpr, objectType)) {
                dlog.error(iExpr.name.pos, DiagnosticCode.UNDEFINED_FUNCTION_IN_OBJECT, iExpr.name.value, objectType);
                resultType = symTable.semanticError;
                return;
            }
        } else {
            iExpr.symbol = funcSymbol;
        }

        // __init method can be called in a method-call-expr only when the expression
        // preceding the . is self
        if (iExpr.name.value.equals(Names.USER_DEFINED_INIT_SUFFIX.value) &&
                !(iExpr.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                (Names.SELF.equals(((BLangSimpleVarRef) iExpr.expr).symbol.name)))) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_INIT_INVOCATION);
        }

        if (Symbols.isFlagOn(funcSymbol.flags, Flags.REMOTE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_ACTION_INVOCATION_SYNTAX);
        }
        if (Symbols.isFlagOn(funcSymbol.flags, Flags.RESOURCE)) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_RESOURCE_FUNCTION_INVOCATION);
        }
        checkInvocationParamAndReturnType(iExpr);
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

    private boolean checkLangLibMethodInvocationExpr(BLangInvocation iExpr, BType bType) {

        Name funcName = names.fromString(iExpr.name.value);
        BSymbol funcSymbol = symResolver.lookupLangLibMethod(bType, funcName);

        if (funcSymbol == symTable.notFoundSymbol) {
            return false;
        }

        iExpr.symbol = funcSymbol;
        iExpr.langLibInvocation = true;
        SymbolEnv enclEnv = this.env;
        this.env = SymbolEnv.createInvocationEnv(iExpr, this.env);
        iExpr.argExprs.add(0, iExpr.expr);
        checkInvocationParamAndReturnType(iExpr);
        this.env = enclEnv;

        return true;
    }

    private void checkInvocationParamAndReturnType(BLangInvocation iExpr) {
        BType actualType = checkInvocationParam(iExpr);
        resultType = types.checkType(iExpr, actualType, this.expType);
    }

    private BType checkInvocationParam(BLangInvocation iExpr) {
        if (iExpr.symbol.type.tag != TypeTags.INVOKABLE) {
            dlog.error(iExpr.pos, DiagnosticCode.INVALID_FUNCTION_INVOCATION, iExpr.symbol.type);
            return symTable.noType;
        }

        List<BType> paramTypes = ((BInvokableType) iExpr.symbol.type).getParameterTypes();
        Map<String, BVarSymbol> params = ((BInvokableSymbol) iExpr.symbol).params
                .stream().collect(Collectors.toMap(a -> a.name.getValue(), a -> a));
        int parameterCount;
        if (iExpr.symbol.tag == SymTag.VARIABLE) {
            // Here we assume function pointers can have only required params.
            // And assume that named params and rest params are not supported.
            parameterCount = paramTypes.size();
        } else {
            parameterCount = ((BInvokableSymbol) iExpr.symbol).params.size();
        }

        iExpr.requiredArgs = new ArrayList<>();

        // Split the different argument types: required args, named args and rest args
        int i = 0;
        BLangExpression vararg = null;
        boolean foundNamedArg = false;
        for (BLangExpression expr : iExpr.argExprs) {
            switch (expr.getKind()) {
                case NAMED_ARGS_EXPR:
                    BVarSymbol varSymbol = params.get(((BLangNamedArgsExpression) expr).name.value);
                    if (!env.enclPkg.packageID.equals(iExpr.symbol.pkgID)
                            && (varSymbol != null && !Symbols.isFlagOn(varSymbol.flags, Flags.PUBLIC))) {
                        // can not provide a named arg, if the arg is not public and the caller is not from same package
                        dlog.error(expr.pos, DiagnosticCode.NON_PUBLIC_ARG_ACCESSED_WITH_NAMED_ARG,
                                ((BLangNamedArgsExpression) expr).name.value, iExpr.toString());
                    }
                    foundNamedArg = true;
                    if (i < parameterCount) {
                        iExpr.requiredArgs.add(expr);
                    } else {
                        // can not provide a rest parameters as named args
                        dlog.error(expr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
                    }
                    i++;
                    break;
                case REST_ARGS_EXPR:
                    if (foundNamedArg) {
                        dlog.error(expr.pos, DiagnosticCode.REST_ARG_DEFINED_AFTER_NAMED_ARG);
                        continue;
                    }
                    vararg = expr;
                    break;
                default: // positional args
                    if (foundNamedArg) {
                        dlog.error(expr.pos, DiagnosticCode.POSITIONAL_ARG_DEFINED_AFTER_NAMED_ARG);
                    }
                    if (i < parameterCount) {
                        iExpr.requiredArgs.add(expr);
                    } else {
                        iExpr.restArgs.add(expr);
                    }
                    i++;
                    break;
            }
        }

        return checkInvocationArgs(iExpr, paramTypes, vararg);
    }

    private BType checkInvocationArgs(BLangInvocation iExpr, List<BType> paramTypes, BLangExpression vararg) {
        BType actualType = symTable.semanticError;
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) iExpr.symbol;
        List<BVarSymbol> nonRestParams = new ArrayList<>(invokableSymbol.params);
        checkNonRestArgs(nonRestParams, iExpr, paramTypes);

        // Check whether the expected param count and the actual args counts are matching.
        if (invokableSymbol.restParam == null && (vararg != null || !iExpr.restArgs.isEmpty())) {
            dlog.error(iExpr.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
            return actualType;
        }

        checkRestArgs(iExpr.restArgs, vararg, invokableSymbol.restParam);
        BType retType = typeParamAnalyzer.getReturnTypeParams(env, invokableSymbol.type.getReturnType());

        if (iExpr.async) {
            return this.generateFutureType(invokableSymbol, retType);
        } else {
            return retType;
        }
    }

    private BFutureType generateFutureType(BInvokableSymbol invocableSymbol, BType retType) {

        boolean isWorkerStart = invocableSymbol.name.value.startsWith(WORKER_LAMBDA_VAR_PREFIX);
        return new BFutureType(TypeTags.FUTURE, retType, null, isWorkerStart);
    }

    private void checkNonRestArgs(List<BVarSymbol> nonRestParams, BLangInvocation iExpr, List<BType> paramTypes) {
        List<BLangExpression> nonRestArgs = iExpr.requiredArgs;
        List<BVarSymbol> requiredParams = nonRestParams.stream()
                .filter(param -> !param.defaultableParam)
                .collect(Collectors.toList());

        List<BVarSymbol> valueProvidedParams = new ArrayList<>();
        for (int i = 0; i < nonRestArgs.size(); i++) {
            BLangExpression arg = nonRestArgs.get(i);
            final BType expectedType = paramTypes.get(i);

            // Special case handling for the first param because for parameterized invocations, we have added the
            // value on which the function is invoked as the first param of the function call. If we run checkExpr()
            // on it, it will recursively add the first param to argExprs again, resulting in a too many args in
            // function call error.
            if (i == 0 && arg.typeChecked && iExpr.expr != null && iExpr.expr == arg) {
                types.checkType(arg.pos, arg.type, expectedType, DiagnosticCode.INCOMPATIBLE_TYPES);
                types.setImplicitCastExpr(arg, arg.type, expectedType);
            }

            if (iExpr.symbol.tag == SymTag.VARIABLE) {
                if (i < paramTypes.size()) {
                    checkTypeParamExpr(arg, this.env, expectedType);
                    if (nonRestParams.size() > i) {
                        requiredParams.remove(nonRestParams.get(i));
                    }
                    continue;
                }
                // if no such parameter, too many arg have been given.
                dlog.error(arg.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
                return;
            }

            if (arg.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                // if arg is positional, corresponding parameter in the same position should be of same type.
                if (i < nonRestParams.size()) {
                    BVarSymbol param = nonRestParams.get(i);
                    checkTypeParamExpr(arg, this.env, param.type);
                    valueProvidedParams.add(param);
                    requiredParams.remove(param);
                    continue;
                }
                // if no such parameter, too many arg have been given.
                dlog.error(arg.pos, DiagnosticCode.TOO_MANY_ARGS_FUNC_CALL, iExpr.name.value);
                return;
            }

            if (arg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                // if arg is named, function should have a parameter with this name.
                BLangIdentifier argName = ((NamedArgNode) arg).getName();
                BVarSymbol varSym = nonRestParams.stream()
                        .filter(param -> param.getName().value.equals(argName.value))
                        .findAny()
                        .orElse(null);
                if (varSym == null) {
                    dlog.error(arg.pos, DiagnosticCode.UNDEFINED_PARAMETER, argName);
                    break;
                }
                requiredParams.remove(varSym);
                if (valueProvidedParams.contains(varSym)) {
                    dlog.error(arg.pos, DiagnosticCode.DUPLICATE_NAMED_ARGS, varSym.name.value);
                    continue;
                }
                checkTypeParamExpr(arg, this.env, varSym.type);
                valueProvidedParams.add(varSym);
            }
        }
        for (BVarSymbol reqParam : requiredParams) {
            // log an error if any of the required parameters are not given.
            dlog.error(iExpr.pos, DiagnosticCode.MISSING_REQUIRED_PARAMETER, reqParam.name, iExpr.name.value);
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
            BType restType = ((BArrayType) restParam.type).eType;
            checkExpr(arg, this.env, restType);
            typeParamAnalyzer.checkForTypeParamsInArg(arg.type, env, restType);
        }
    }

    private void checkTypeParamExpr(BLangExpression arg, SymbolEnv env, BType expectedType) {

        if (typeParamAnalyzer.notRequireTypeParams(env)) {
            checkExpr(arg, env, expectedType);
            return;
        }
        if (requireTypeInference(arg)) {
            // Need to infer the type. Calculate matching bound type, with no type.
            BType expType = typeParamAnalyzer.getMatchingBoundType(expectedType, env);
            BType inferredType = checkExpr(arg, env, expType);
            typeParamAnalyzer.checkForTypeParamsInArg(inferredType, this.env, expectedType);
            return;
        }
        checkExpr(arg, env, expectedType);
        typeParamAnalyzer.checkForTypeParamsInArg(arg.type, this.env, expectedType);
    }

    private boolean requireTypeInference(BLangExpression expr) {

        switch (expr.getKind()) {
            case GROUP_EXPR:
                return requireTypeInference(((BLangGroupExpr) expr).expression);
            case ARROW_EXPR:
            case LIST_CONSTRUCTOR_EXPR:
            case RECORD_LITERAL_EXPR:
                return true;
            default:
                return false;
        }
    }

    private void checkRecLiteralKeyValue(BLangRecordKeyValue keyValuePair, BType recType) {
        BType fieldType = symTable.semanticError;
        BLangExpression valueExpr = keyValuePair.valueExpr;
        switch (recType.tag) {
            case TypeTags.RECORD:
                fieldType = checkRecordLiteralKeyExpr(keyValuePair.key, (BRecordType) recType);
                break;
            case TypeTags.MAP:
                fieldType = checkValidJsonOrMapLiteralKeyExpr(keyValuePair.key) ? ((BMapType) recType).constraint :
                        symTable.semanticError;
                break;
            case TypeTags.JSON:
                fieldType = checkValidJsonOrMapLiteralKeyExpr(keyValuePair.key) ? symTable.jsonType :
                        symTable.semanticError;

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

    private BType checkRecordLiteralKeyExpr(BLangRecordKey key, BRecordType recordType) {
        Name fieldName;
        BLangExpression keyExpr = key.expr;

        if (key.computedKey) {
            checkExpr(keyExpr, this.env, symTable.stringType);

            if (keyExpr.type == symTable.semanticError) {
                return symTable.semanticError;
            }

            LinkedHashSet<BType> fieldTypes = recordType.fields.stream()
                    .map(field -> field.type)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (recordType.restFieldType.tag != TypeTags.NONE) {
                fieldTypes.add(recordType.restFieldType);
            }

            return BUnionType.create(null, fieldTypes);
        } else if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        } else if (keyExpr.getKind() == NodeKind.LITERAL && ((BLangLiteral) keyExpr).type.tag == TypeTags.STRING) {
            fieldName = names.fromString((String) ((BLangLiteral) keyExpr).value);
        } else {
            dlog.error(keyExpr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_KEY);
            return symTable.semanticError;
        }

        // Check whether the struct field exists
        BSymbol fieldSymbol = symResolver.resolveStructField(keyExpr.pos, this.env,
                fieldName, recordType.tsymbol);
        if (fieldSymbol == symTable.notFoundSymbol) {
            if (recordType.sealed) {
                dlog.error(keyExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE, fieldName,
                        recordType.tsymbol.type.getKind().typeName(), recordType.tsymbol);
                return symTable.semanticError;
            }

            return recordType.restFieldType;
        }

        return fieldSymbol.type;
    }

    private boolean checkValidJsonOrMapLiteralKeyExpr(BLangRecordKey key) {
        BLangExpression keyExpr = key.expr;
        if (key.computedKey) {
            checkExpr(keyExpr, this.env, symTable.stringType);

            if (keyExpr.type == symTable.semanticError) {
                return false;
            }
            return true;
        } else if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF ||
                (keyExpr.getKind() == NodeKind.LITERAL && ((BLangLiteral) keyExpr).type.tag == TypeTags.STRING)) {
            return true;
        }
        dlog.error(keyExpr.pos, DiagnosticCode.INVALID_RECORD_LITERAL_KEY);
        return false;
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

    private BType checkIndexExprForObjectFieldAccess(BLangExpression indexExpr) {
        if (indexExpr.getKind() != NodeKind.LITERAL && indexExpr.getKind() != NodeKind.NUMERIC_LITERAL) {
            indexExpr.type = symTable.semanticError;
            dlog.error(indexExpr.pos, DiagnosticCode.INVALID_INDEX_EXPR_STRUCT_FIELD_ACCESS);
            return indexExpr.type;
        }

        return checkExpr(indexExpr, this.env, symTable.stringType);
    }

    private BType addNilForNillableIndexBasedAccess(BType actualType) {
        // index based map/record access always returns a nil-able type for optional/rest fields.
        if (actualType.isNullable()) {
            return actualType;
        }

        return BUnionType.create(null, actualType, symTable.nilType);
    }

    private BType checkRecordRequiredFieldAccess(BLangVariableReference varReferExpr, Name fieldName,
                                                 BRecordType recordType) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, this.env, fieldName, recordType.tsymbol);

        if (fieldSymbol == symTable.notFoundSymbol || Symbols.isOptional(fieldSymbol)) {
            return symTable.semanticError;
        }

        // Set the field symbol to use during the code generation phase.
        varReferExpr.symbol = fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkRecordOptionalFieldAccess(BLangVariableReference varReferExpr, Name fieldName,
                                                 BRecordType recordType) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, this.env, fieldName, recordType.tsymbol);

        if (fieldSymbol == symTable.notFoundSymbol || !Symbols.isOptional(fieldSymbol)) {
            return symTable.semanticError;
        }

        // Set the field symbol to use during the code generation phase.
        varReferExpr.symbol = fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkRecordRestFieldAccess(BLangVariableReference varReferExpr, Name fieldName,
                                             BRecordType recordType) {
        BSymbol fieldSymbol = symResolver.resolveStructField(varReferExpr.pos, this.env, fieldName, recordType.tsymbol);

        if (fieldSymbol != symTable.notFoundSymbol) {
            // The field should not exist as a required or optional field.
            return symTable.semanticError;
        }

        if (recordType.sealed) {
            return symTable.semanticError;
        }

        return recordType.restFieldType;
    }

    private BType checkObjectFieldAccess(BLangFieldBasedAccess bLangFieldBasedAccess,
                                         Name fieldName, BObjectType objectType) {
        BSymbol fieldSymbol = symResolver.resolveStructField(bLangFieldBasedAccess.pos,
                this.env, fieldName, objectType.tsymbol);

        if (fieldSymbol != symTable.notFoundSymbol) {
            // Setting the field symbol. This is used during the code generation phase
            bLangFieldBasedAccess.symbol = fieldSymbol;
            return fieldSymbol.type;
        }

        // check if it is an attached function pointer call
        Name objFuncName = names.fromString(Symbols.getAttachedFuncSymbolName(objectType.tsymbol.name.value,
                fieldName.value));
        fieldSymbol = symResolver.resolveObjectField(bLangFieldBasedAccess.pos, env, objFuncName, objectType.tsymbol);

        if (fieldSymbol == symTable.notFoundSymbol) {
            dlog.error(bLangFieldBasedAccess.field.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE, fieldName,
                    objectType.tsymbol.type.getKind().typeName(), objectType.tsymbol);
            return symTable.semanticError;
        }

        // Setting the field symbol. This is used during the code generation phase
        bLangFieldBasedAccess.symbol = fieldSymbol;
        return fieldSymbol.type;
    }

    private BType checkTupleFieldType(BType tupleType, int indexValue) {
        BTupleType bTupleType = (BTupleType) tupleType;
        if (bTupleType.tupleTypes.size() <= indexValue && bTupleType.restType != null) {
            return bTupleType.restType;
        } else if (indexValue < 0 || bTupleType.tupleTypes.size() <= indexValue) {
            return symTable.semanticError;
        }
        return bTupleType.tupleTypes.get(indexValue);
    }

    private void validateTags(BLangXMLElementLiteral bLangXMLElementLiteral, SymbolEnv xmlElementEnv) {
        // check type for start and end tags
        BLangExpression startTagName = bLangXMLElementLiteral.startTagName;
        checkExpr(startTagName, xmlElementEnv, symTable.stringType);
        BLangExpression endTagName = bLangXMLElementLiteral.endTagName;
        if (endTagName == null) {
            return;
        }

        checkExpr(endTagName, xmlElementEnv, symTable.stringType);
        if (startTagName.getKind() == NodeKind.XML_QNAME && endTagName.getKind() == NodeKind.XML_QNAME &&
                startTagName.equals(endTagName)) {
            return;
        }

        if (startTagName.getKind() != NodeKind.XML_QNAME && endTagName.getKind() != NodeKind.XML_QNAME) {
            return;
        }

        dlog.error(bLangXMLElementLiteral.pos, DiagnosticCode.XML_TAGS_MISMATCH);
    }

    private void checkStringTemplateExprs(List<BLangExpression> exprs, boolean allowXml) {
        for (BLangExpression expr : exprs) {
            checkExpr(expr, env);

            BType type = expr.type;

            if (type == symTable.semanticError) {
                continue;
            }

            if (type.tag >= TypeTags.JSON) {
                if (allowXml) {
                    if (type.tag != TypeTags.XML) {
                        dlog.error(expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES,
                                   BUnionType.create(null, symTable.intType, symTable.floatType, symTable.decimalType,
                                                     symTable.stringType, symTable.booleanType, symTable.xmlType),
                                   type);
                    }
                    continue;
                }

                dlog.error(expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES,
                           BUnionType.create(null, symTable.intType, symTable.floatType, symTable.decimalType,
                                             symTable.stringType, symTable.booleanType), type);
            }
        }
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
        List<BLangExpression> tempConcatExpressions = new ArrayList<>();

        for (BLangExpression expr : exprs) {
            BType exprType = checkExpr(expr, xmlElementEnv);
            if (exprType == symTable.xmlType) {
                if (!tempConcatExpressions.isEmpty()) {
                    newChildren.add(getXMLTextLiteral(tempConcatExpressions));
                    tempConcatExpressions = new ArrayList<>();
                }
                newChildren.add(expr);
                continue;
            }

            BType type = expr.type;
            if (type.tag >= TypeTags.JSON) {
                if (type != symTable.semanticError) {
                    dlog.error(expr.pos, DiagnosticCode.INCOMPATIBLE_TYPES,
                               BUnionType.create(null, symTable.intType, symTable.floatType, symTable.decimalType,
                                                 symTable.stringType, symTable.booleanType, symTable.xmlType),
                               type);
                }
                continue;
            }

            tempConcatExpressions.add(expr);
        }

        // Add remaining concatenated text nodes as children
        if (!tempConcatExpressions.isEmpty()) {
            newChildren.add(getXMLTextLiteral(tempConcatExpressions));
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

    private BLangExpression getXMLTextLiteral(List<BLangExpression> exprs) {
        BLangXMLTextLiteral xmlTextLiteral = (BLangXMLTextLiteral) TreeBuilder.createXMLTextLiteralNode();
        xmlTextLiteral.textFragments = exprs;
        xmlTextLiteral.pos = exprs.get(0).pos;
        xmlTextLiteral.type = symTable.xmlType;
        return xmlTextLiteral;
    }

    private BType getTypeOfExprInFieldAccess(BLangExpression expr) {
        checkExpr(expr, this.env, symTable.noType);
        return expr.type;
    }

    private BType getAccessExprFinalType(BLangAccessExpression accessExpr, BType actualType) {

        // Cache the actual type of the field. This will be used in desuagr phase to create safe navigation.
        accessExpr.originalType = actualType;

        BUnionType unionType = BUnionType.create(null, actualType);

        if (returnsNull(accessExpr)) {
            unionType.add(symTable.nilType);
        }

        BType parentType = accessExpr.expr.type;
        if (accessExpr.errorSafeNavigation
                && (parentType.tag == TypeTags.SEMANTIC_ERROR || (parentType.tag == TypeTags.UNION
                && ((BUnionType) parentType).getMemberTypes().contains(symTable.errorType)))) {
            unionType.add(symTable.errorType);
        }

        // If there's only one member, and the one an only member is:
        //    a) nilType OR
        //    b) not-nullable 
        // then return that only member, as the return type.
        if (unionType.getMemberTypes().size() == 1) {
            return unionType.getMemberTypes().toArray(new BType[0])[0];
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

    private BType checkObjectFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName) {
        if (varRefType.tag == TypeTags.OBJECT) {
            return checkObjectFieldAccess(fieldAccessExpr, fieldName, (BObjectType) varRefType);
        }

        // If the type is not an object, it needs to be a union of objects.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkObjectFieldAccess(fieldAccessExpr, fieldName, (BObjectType) memType);

            if (individualFieldType == symTable.semanticError) {
                return individualFieldType;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkRecordFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName) {
        if (varRefType.tag == TypeTags.RECORD) {
            return checkRecordRequiredFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType);
        }

        // If the type is not an record, it needs to be a union of records.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkRecordFieldAccessExpr(fieldAccessExpr, memType, fieldName);

            if (individualFieldType == symTable.semanticError) {
                return individualFieldType;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkRecordFieldAccessLhsExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType,
                                                Name fieldName) {
        if (varRefType.tag == TypeTags.RECORD) {
            BType fieldType = checkRecordRequiredFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType);
            if (fieldType != symTable.semanticError) {
                return fieldType;
            }

            // For the LHS, the field could be optional.
            return checkRecordOptionalFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType);
        }

        // If the type is not an record, it needs to be a union of records.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkRecordFieldAccessLhsExpr(fieldAccessExpr, memType, fieldName);

            if (individualFieldType == symTable.semanticError) {
                return symTable.semanticError;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkOptionalRecordFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType,
                                                     Name fieldName) {
        if (varRefType.tag == TypeTags.RECORD) {
            BType fieldType = checkRecordRequiredFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType);
            if (fieldType != symTable.semanticError) {
                return fieldType;
            }

            fieldType = checkRecordOptionalFieldAccess(fieldAccessExpr, fieldName, (BRecordType) varRefType);
            if (fieldType == symTable.semanticError) {
                return fieldType;
            }
            return BUnionType.create(null, fieldType, symTable.nilType);
        }

        // If the type is not an record, it needs to be a union of records.
        // Resultant field type is calculated here.
        Set<BType> memberTypes = ((BUnionType) varRefType).getMemberTypes();

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : memberTypes) {
            BType individualFieldType = checkOptionalRecordFieldAccessExpr(fieldAccessExpr, memType, fieldName);

            if (individualFieldType == symTable.semanticError) {
                continue;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.isEmpty()) {
            return symTable.semanticError;
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }

        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType, Name fieldName) {
        BType actualType = symTable.semanticError;

        if (types.isSubTypeOfBaseType(varRefType, TypeTags.OBJECT)) {
            actualType = checkObjectFieldAccessExpr(fieldAccessExpr, varRefType, fieldName);
            fieldAccessExpr.originalType = actualType;
        } else if (types.isSubTypeOfBaseType(varRefType, TypeTags.RECORD)) {
            actualType = checkRecordFieldAccessExpr(fieldAccessExpr, varRefType, fieldName);

            if (actualType != symTable.semanticError) {
                fieldAccessExpr.originalType = actualType;
                return actualType;
            }

            if (!fieldAccessExpr.lhsVar) {
                dlog.error(fieldAccessExpr.pos,
                        DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_NON_REQUIRED_FIELD, varRefType,
                        fieldName);
                return actualType;
            }

            // If this is an LHS expression, check if there is a required and/ optional field by the specified field
            // name in all records.
            actualType = checkRecordFieldAccessLhsExpr(fieldAccessExpr, varRefType, fieldName);
            fieldAccessExpr.originalType = actualType;
            if (actualType == symTable.semanticError) {
                dlog.error(fieldAccessExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE, fieldName,
                        varRefType.tsymbol.type.getKind().typeName(), varRefType);
            }
        } else if (types.isLax(varRefType)) {
            if (fieldAccessExpr.lhsVar) {
                dlog.error(fieldAccessExpr.pos,
                        DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS_FOR_ASSIGNMENT, varRefType);
                return symTable.semanticError;
            }
            BType laxFieldAccessType = getLaxFieldAccessType(varRefType);
            actualType = BUnionType.create(null, laxFieldAccessType, symTable.errorType);
            fieldAccessExpr.originalType = laxFieldAccessType;
        } else if (fieldAccessExpr.expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                hasLaxOriginalType(((BLangFieldBasedAccess) fieldAccessExpr.expr))) {
            BType laxFieldAccessType =
                    getLaxFieldAccessType(((BLangFieldBasedAccess) fieldAccessExpr.expr).originalType);
            actualType = BUnionType.create(null, laxFieldAccessType, symTable.errorType);
            fieldAccessExpr.errorSafeNavigation = true;
            fieldAccessExpr.originalType = laxFieldAccessType;
        } else if (varRefType.tag == TypeTags.XML) {
            if (fieldAccessExpr.lhsVar) {
                dlog.error(fieldAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
            }
            actualType = symTable.xmlType;
            fieldAccessExpr.originalType = actualType;
        } else if (varRefType.tag != TypeTags.SEMANTIC_ERROR) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_FIELD_ACCESS, varRefType);
        }

        return actualType;
    }

    private boolean hasLaxOriginalType(BLangFieldBasedAccess fieldBasedAccess) {
        return fieldBasedAccess.originalType != null && types.isLax(fieldBasedAccess.originalType);
    }

    private BType getLaxFieldAccessType(BType exprType) {
        switch (exprType.tag) {
            case TypeTags.JSON:
                return symTable.jsonType;
            case TypeTags.MAP:
                return ((BMapType) exprType).constraint;
            case TypeTags.UNION:
                BUnionType unionType = (BUnionType) exprType;
                LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
                unionType.getMemberTypes().forEach(bType -> memberTypes.add(getLaxFieldAccessType(bType)));
                return memberTypes.size() == 1 ? memberTypes.iterator().next() : BUnionType.create(null, memberTypes);
        }
        return symTable.semanticError;
    }

    private BType checkOptionalFieldAccessExpr(BLangFieldBasedAccess fieldAccessExpr, BType varRefType,
                                               Name fieldName) {
        BType actualType = symTable.semanticError;

        boolean nillableExprType = false;
        BType effectiveType = varRefType;

        if (varRefType.tag == TypeTags.UNION) {
            Set<BType> memTypes = ((BUnionType) varRefType).getMemberTypes();

            if (memTypes.contains(symTable.nilType)) {
                LinkedHashSet<BType> nilRemovedSet = new LinkedHashSet<>();
                for (BType bType : memTypes) {
                    if (bType != symTable.nilType) {
                        nilRemovedSet.add(bType);
                    } else {
                        nillableExprType = true;
                    }
                }

                effectiveType = nilRemovedSet.size() == 1 ? nilRemovedSet.iterator().next() :
                        BUnionType.create(null, nilRemovedSet);
            }
        }

        if (types.isSubTypeOfBaseType(effectiveType, TypeTags.RECORD)) {
            actualType = checkOptionalRecordFieldAccessExpr(fieldAccessExpr, effectiveType, fieldName);
            if (actualType == symTable.semanticError) {
                dlog.error(fieldAccessExpr.pos,
                        DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS_FOR_FIELD,
                        varRefType, fieldName);
            }
            fieldAccessExpr.nilSafeNavigation = nillableExprType;
            fieldAccessExpr.originalType = getSafeType(actualType, fieldAccessExpr);
        } else if (types.isLax(effectiveType)) {
            BType laxFieldAccessType = getLaxFieldAccessType(effectiveType);
            actualType = couldHoldNonMappingJson(effectiveType) ?
                    BUnionType.create(null, laxFieldAccessType, symTable.errorType) : laxFieldAccessType;
            fieldAccessExpr.originalType = laxFieldAccessType;
            fieldAccessExpr.nilSafeNavigation = true;
            nillableExprType = true;
        } else if (fieldAccessExpr.expr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR &&
                hasLaxOriginalType(((BLangFieldBasedAccess) fieldAccessExpr.expr))) {
            BType laxFieldAccessType =
                    getLaxFieldAccessType(((BLangFieldBasedAccess) fieldAccessExpr.expr).originalType);
            actualType = couldHoldNonMappingJson(effectiveType) ?
                    BUnionType.create(null, laxFieldAccessType, symTable.errorType) : laxFieldAccessType;
            fieldAccessExpr.errorSafeNavigation = true;
            fieldAccessExpr.originalType = laxFieldAccessType;
            fieldAccessExpr.nilSafeNavigation = true;
            nillableExprType = true;
        } else if (varRefType.tag != TypeTags.SEMANTIC_ERROR) {
            dlog.error(fieldAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_OPTIONAL_FIELD_ACCESS,
                    varRefType);
        }

        if (nillableExprType && !actualType.isNullable()) {
            actualType = BUnionType.create(null, actualType, symTable.nilType);
        }

        return actualType;
    }

    private boolean couldHoldNonMappingJson(BType type) {
        if (type.tag == TypeTags.JSON) {
            return true;
        }

        if (type.tag == TypeTags.MAP) {
            return false;
        }

        return ((BUnionType) type).getMemberTypes().stream().anyMatch(this::couldHoldNonMappingJson);
    }

    private BType checkIndexAccessExpr(BLangIndexBasedAccess indexBasedAccessExpr) {
        BType varRefType = indexBasedAccessExpr.expr.type;

        boolean nillableExprType = false;

        if (varRefType.tag == TypeTags.UNION) {
            Set<BType> memTypes = ((BUnionType) varRefType).getMemberTypes();

            if (memTypes.contains(symTable.nilType)) {
                LinkedHashSet<BType> nilRemovedSet = new LinkedHashSet<>();
                for (BType bType : memTypes) {
                    if (bType != symTable.nilType) {
                        nilRemovedSet.add(bType);
                    } else {
                        nillableExprType = true;
                    }
                }

                if (nillableExprType) {
                    varRefType = nilRemovedSet.size() == 1 ? nilRemovedSet.iterator().next() :
                            BUnionType.create(null, nilRemovedSet);

                    if (!types.isSubTypeOfMapping(varRefType)) {
                        // Member access is allowed on optional types only with mappings.
                        dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEXING,
                                indexBasedAccessExpr.expr.type);
                        return symTable.semanticError;
                    }

                    if (indexBasedAccessExpr.lhsVar) {
                        dlog.error(indexBasedAccessExpr.pos,
                                DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEX_ACCESS_FOR_ASSIGNMENT,
                                indexBasedAccessExpr.expr.type);
                        return symTable.semanticError;
                    }
                }
            }
        }


        BLangExpression indexExpr = indexBasedAccessExpr.indexExpr;
        BType actualType = symTable.semanticError;

        if (types.isSubTypeOfMapping(varRefType)) {
            checkExpr(indexExpr, this.env, symTable.stringType);

            if (indexExpr.type == symTable.semanticError) {
                return symTable.semanticError;
            }

            actualType = checkMappingIndexBasedAccess(indexBasedAccessExpr, varRefType);

            if (actualType == symTable.semanticError) {
                if (indexExpr.type.tag == TypeTags.STRING && isConst(indexExpr)) {
                    String fieldName = getConstFieldName(indexExpr);
                    dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.UNDEFINED_STRUCTURE_FIELD,
                               fieldName, indexBasedAccessExpr.expr.type);
                    return actualType;
                }

                dlog.error(indexExpr.pos, DiagnosticCode.INVALID_RECORD_INDEX_EXPR, indexExpr.type);
                return actualType;
            }

            indexBasedAccessExpr.nilSafeNavigation = nillableExprType;
            indexBasedAccessExpr.originalType = getSafeType(actualType, indexBasedAccessExpr);
        } else if (types.isSubTypeOfList(varRefType)) {
            checkExpr(indexExpr, this.env, symTable.intType);

            if (indexExpr.type == symTable.semanticError) {
                return symTable.semanticError;
            }

            actualType = checkListIndexBasedAccess(indexBasedAccessExpr, varRefType);
            indexBasedAccessExpr.originalType = actualType;

            if (actualType == symTable.semanticError) {
                if (indexExpr.type.tag == TypeTags.INT && isConst(indexExpr)) {
                    dlog.error(indexBasedAccessExpr.indexExpr.pos, DiagnosticCode.LIST_INDEX_OUT_OF_RANGE,
                               getConstIndex(indexExpr));
                    return actualType;
                }
                dlog.error(indexExpr.pos, DiagnosticCode.INVALID_LIST_INDEX_EXPR, indexExpr.type);
                return actualType;
            }
        } else if (types.isAssignable(varRefType, symTable.stringType)) {
            if (indexBasedAccessExpr.lhsVar) {
                dlog.error(indexBasedAccessExpr.pos,
                           DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEX_ACCESS_FOR_ASSIGNMENT,
                           indexBasedAccessExpr.expr.type);
                return symTable.semanticError;
            }

            checkExpr(indexExpr, this.env, symTable.intType);

            if (indexExpr.type == symTable.semanticError) {
                return symTable.semanticError;
            }

            indexBasedAccessExpr.originalType = symTable.stringType;
            actualType = symTable.stringType;
        } else if (varRefType.tag == TypeTags.XML) {
            if (indexBasedAccessExpr.lhsVar) {
                indexExpr.type = symTable.semanticError;
                dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.CANNOT_UPDATE_XML_SEQUENCE);
                return actualType;
            }

            checkExpr(indexExpr, this.env);
            actualType = symTable.xmlType;
            indexBasedAccessExpr.originalType = actualType;
        } else if (varRefType == symTable.semanticError) {
            indexBasedAccessExpr.indexExpr.type = symTable.semanticError;
            return symTable.semanticError;
        } else {
            indexBasedAccessExpr.indexExpr.type = symTable.semanticError;
            dlog.error(indexBasedAccessExpr.pos, DiagnosticCode.OPERATION_DOES_NOT_SUPPORT_INDEXING,
                    indexBasedAccessExpr.expr.type);
            return symTable.semanticError;
        }

        if (nillableExprType && !actualType.isNullable()) {
            actualType = BUnionType.create(null, actualType, symTable.nilType);
        }

        return actualType;
    }

    private Long getConstIndex(BLangExpression indexExpr) {
        return indexExpr.getKind() == NodeKind.NUMERIC_LITERAL ? (Long) ((BLangLiteral) indexExpr).value :
                (Long) ((BConstantSymbol) ((BLangSimpleVarRef) indexExpr).symbol).value.value;
    }

    private String getConstFieldName(BLangExpression indexExpr) {
        return indexExpr.getKind() == NodeKind.LITERAL ? (String) ((BLangLiteral) indexExpr).value :
                (String) ((BConstantSymbol) ((BLangSimpleVarRef) indexExpr).symbol).value.value;
    }

    private BType checkArrayIndexBasedAccess(BLangIndexBasedAccess indexBasedAccess, BType indexExprType,
                                             BArrayType arrayType) {
        BType actualType = symTable.semanticError;
        switch (indexExprType.tag) {
            case TypeTags.INT:
                BLangExpression indexExpr = indexBasedAccess.indexExpr;
                if (!isConst(indexExpr) || arrayType.state == BArrayState.UNSEALED) {
                    actualType = arrayType.eType;
                    break;
                }
                actualType = getConstIndex(indexExpr) >= arrayType.size ? symTable.semanticError : arrayType.eType;
                break;
            case TypeTags.FINITE:
                BFiniteType finiteIndexExpr = (BFiniteType) indexExprType;
                boolean validIndexExists = false;
                for (BLangExpression finiteMember : finiteIndexExpr.valueSpace) {
                    int indexValue = ((Long) ((BLangLiteral) finiteMember).value).intValue();
                    if (indexValue >= 0 &&
                            (arrayType.state == BArrayState.UNSEALED || indexValue < arrayType.size)) {
                        validIndexExists = true;
                        break;
                    }
                }
                if (!validIndexExists) {
                    return symTable.semanticError;
                }
                actualType = arrayType.eType;
                break;
            case TypeTags.UNION:
                // address the case where we have a union of finite types
                List<BFiniteType> finiteTypes = ((BUnionType) indexExprType).getMemberTypes().stream()
                        .filter(memType -> memType.tag == TypeTags.FINITE)
                        .map(matchedType -> (BFiniteType) matchedType)
                        .collect(Collectors.toList());

                BFiniteType finiteType;
                if (finiteTypes.size() == 1) {
                    finiteType = finiteTypes.get(0);
                } else {
                    Set<BLangExpression> valueSpace = new LinkedHashSet<>();
                    finiteTypes.forEach(constituent -> valueSpace.addAll(constituent.valueSpace));
                    finiteType = new BFiniteType(null, valueSpace);
                }

                BType elementType = checkArrayIndexBasedAccess(indexBasedAccess, finiteType, arrayType);
                if (elementType == symTable.semanticError) {
                    return symTable.semanticError;
                }
                actualType = arrayType.eType;
        }
        return actualType;
    }

    private BType checkListIndexBasedAccess(BLangIndexBasedAccess accessExpr, BType type) {
        if (type.tag == TypeTags.ARRAY) {
            return checkArrayIndexBasedAccess(accessExpr, accessExpr.indexExpr.type, (BArrayType) type);
        }

        if (type.tag == TypeTags.TUPLE) {
            return checkTupleIndexBasedAccess(accessExpr, (BTupleType) type, accessExpr.indexExpr.type);
        }

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : ((BUnionType) type).getMemberTypes()) {
            BType individualFieldType = checkListIndexBasedAccess(accessExpr, memType);

            if (individualFieldType == symTable.semanticError) {
                continue;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 0) {
            return symTable.semanticError;
        }

        if (fieldTypeMembers.size() == 1) {
            return fieldTypeMembers.iterator().next();
        }
        return BUnionType.create(null, fieldTypeMembers);
    }

    private BType checkTupleIndexBasedAccess(BLangIndexBasedAccess accessExpr, BTupleType tuple, BType currentType) {
        BType actualType = symTable.semanticError;
        BLangExpression indexExpr = accessExpr.indexExpr;
        switch (currentType.tag) {
            case TypeTags.INT:
                if (isConst(indexExpr)) {
                    actualType = checkTupleFieldType(tuple, getConstIndex(indexExpr).intValue());
                } else {
                    BTupleType tupleExpr = (BTupleType) accessExpr.expr.type;
                    LinkedHashSet<BType> tupleTypes = collectTupleFieldTypes(tupleExpr, new LinkedHashSet<>());
                    actualType = tupleTypes.size() == 1 ? tupleTypes.iterator().next() : BUnionType.create(null,
                                                                                                           tupleTypes);
                }
                break;
            case TypeTags.FINITE:
                BFiniteType finiteIndexExpr = (BFiniteType) currentType;
                LinkedHashSet<BType> possibleTypes = new LinkedHashSet<>();
                for (BLangExpression finiteMember : finiteIndexExpr.valueSpace) {
                    int indexValue = ((Long) ((BLangLiteral) finiteMember).value).intValue();
                    BType fieldType = checkTupleFieldType(tuple, indexValue);
                    if (fieldType.tag != TypeTags.SEMANTIC_ERROR) {
                        possibleTypes.add(fieldType);
                    }
                }
                if (possibleTypes.size() == 0) {
                    return symTable.semanticError;
                }
                actualType = possibleTypes.size() == 1 ? possibleTypes.iterator().next() :
                        BUnionType.create(null, possibleTypes);
                break;

            case TypeTags.UNION:
                LinkedHashSet<BType> possibleTypesByMember = new LinkedHashSet<>();
                List<BFiniteType> finiteTypes = new ArrayList<>();
                ((BUnionType) currentType).getMemberTypes().forEach(memType -> {
                    if (memType.tag == TypeTags.FINITE) {
                        finiteTypes.add((BFiniteType) memType);
                    } else {
                        BType possibleType = checkTupleIndexBasedAccess(accessExpr, tuple, memType);
                        if (possibleType.tag == TypeTags.UNION) {
                            possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                        } else {
                            possibleTypesByMember.add(possibleType);
                        }
                    }
                });

                BFiniteType finiteType;
                if (finiteTypes.size() == 1) {
                    finiteType = finiteTypes.get(0);
                } else {
                    Set<BLangExpression> valueSpace = new LinkedHashSet<>();
                    finiteTypes.forEach(constituent -> valueSpace.addAll(constituent.valueSpace));
                    finiteType = new BFiniteType(null, valueSpace);
                }

                BType possibleType = checkTupleIndexBasedAccess(accessExpr, tuple, finiteType);
                if (possibleType.tag == TypeTags.UNION) {
                    possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                } else {
                    possibleTypesByMember.add(possibleType);
                }

                if (possibleTypesByMember.contains(symTable.semanticError)) {
                    return symTable.semanticError;
                }
                actualType = possibleTypesByMember.size() == 1 ? possibleTypesByMember.iterator().next() :
                        BUnionType.create(null, possibleTypesByMember);
        }
        return actualType;
    }

    private LinkedHashSet<BType> collectTupleFieldTypes(BTupleType tupleType, LinkedHashSet<BType> memberTypes) {
        tupleType.tupleTypes
                .forEach(memberType -> {
                    if (memberType.tag == TypeTags.UNION) {
                        collectMemberTypes((BUnionType) memberType, memberTypes);
                    } else {
                        memberTypes.add(memberType);
                    }
                });
        return memberTypes;
    }

    private BType checkMappingIndexBasedAccess(BLangIndexBasedAccess accessExpr, BType type) {
        if (type.tag == TypeTags.MAP) {
            BType constraint = ((BMapType) type).constraint;
            return accessExpr.lhsVar ? constraint : addNilForNillableIndexBasedAccess(constraint);
        }

        if (type.tag == TypeTags.RECORD) {
            return checkRecordIndexBasedAccess(accessExpr, (BRecordType) type, accessExpr.indexExpr.type);
        }

        BType fieldType;

        boolean nonMatchedRecordExists = false;

        LinkedHashSet<BType> fieldTypeMembers = new LinkedHashSet<>();

        for (BType memType : ((BUnionType) type).getMemberTypes()) {
            BType individualFieldType = checkMappingIndexBasedAccess(accessExpr, memType);

            if (individualFieldType == symTable.semanticError) {
                nonMatchedRecordExists = true;
                continue;
            }

            fieldTypeMembers.add(individualFieldType);
        }

        if (fieldTypeMembers.size() == 0) {
            return symTable.semanticError;
        }

        if (fieldTypeMembers.size() == 1) {
            fieldType = fieldTypeMembers.iterator().next();
        } else {
            fieldType = BUnionType.create(null, fieldTypeMembers);
        }

        return nonMatchedRecordExists ? addNilForNillableIndexBasedAccess(fieldType) : fieldType;
    }

    private BType checkRecordIndexBasedAccess(BLangIndexBasedAccess accessExpr, BRecordType record, BType currentType) {
        BType actualType = symTable.semanticError;
        BLangExpression indexExpr = accessExpr.indexExpr;
        switch (currentType.tag) {
            case TypeTags.STRING:
                if (isConst(indexExpr)) {
                    String fieldName = getConstFieldName(indexExpr);
                    actualType = checkRecordRequiredFieldAccess(accessExpr, names.fromString(fieldName), record);
                    if (actualType != symTable.semanticError) {
                        return actualType;
                    }

                    actualType = checkRecordOptionalFieldAccess(accessExpr, names.fromString(fieldName), record);
                    if (actualType == symTable.semanticError) {
                        actualType = checkRecordRestFieldAccess(accessExpr, names.fromString(fieldName), record);
                        if (actualType == symTable.semanticError) {
                            return actualType;
                        }
                        return addNilForNillableIndexBasedAccess(actualType);
                    }

                    if (accessExpr.lhsVar) {
                        return actualType;
                    }
                    return addNilForNillableIndexBasedAccess(actualType);
                }

                LinkedHashSet<BType> fieldTypes = record.fields.stream()
                        .map(field -> field.type)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

                if (record.restFieldType.tag != TypeTags.NONE) {
                    fieldTypes.add(record.restFieldType);
                }

                if (fieldTypes.stream().noneMatch(BType::isNullable)) {
                    fieldTypes.add(symTable.nilType);
                }

                actualType = BUnionType.create(null, fieldTypes);
                break;
            case TypeTags.FINITE:
                BFiniteType finiteIndexExpr = (BFiniteType) currentType;
                LinkedHashSet<BType> possibleTypes = new LinkedHashSet<>();
                for (BLangExpression finiteMember : finiteIndexExpr.valueSpace) {
                    String fieldName = (String) ((BLangLiteral) finiteMember).value;
                    BType fieldType = checkRecordRequiredFieldAccess(accessExpr, names.fromString(fieldName), record);
                    if (fieldType == symTable.semanticError) {
                        fieldType = checkRecordOptionalFieldAccess(accessExpr, names.fromString(fieldName), record);
                        if (fieldType == symTable.semanticError) {
                            fieldType = checkRecordRestFieldAccess(accessExpr, names.fromString(fieldName), record);
                        }

                        if (fieldType != symTable.semanticError) {
                            fieldType = addNilForNillableIndexBasedAccess(fieldType);
                        }
                    }

                    if (fieldType.tag == TypeTags.SEMANTIC_ERROR) {
                        continue;
                    }
                    possibleTypes.add(fieldType);
                }

                if (possibleTypes.isEmpty()) {
                    return symTable.semanticError;
                }

                if (possibleTypes.stream().noneMatch(BType::isNullable)) {
                    possibleTypes.add(symTable.nilType);
                }

                actualType = possibleTypes.size() == 1 ? possibleTypes.iterator().next() :
                        BUnionType.create(null, possibleTypes);
                break;
            case TypeTags.UNION:
                LinkedHashSet<BType> possibleTypesByMember = new LinkedHashSet<>();
                List<BFiniteType> finiteTypes = new ArrayList<>();
                ((BUnionType) currentType).getMemberTypes().forEach(memType -> {
                    if (memType.tag == TypeTags.FINITE) {
                        finiteTypes.add((BFiniteType) memType);
                    } else {
                        BType possibleType = checkRecordIndexBasedAccess(accessExpr, record, memType);
                        if (possibleType.tag == TypeTags.UNION) {
                            possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                        } else {
                            possibleTypesByMember.add(possibleType);
                        }
                    }
                });

                BFiniteType finiteType;
                if (finiteTypes.size() == 1) {
                    finiteType = finiteTypes.get(0);
                } else {
                    Set<BLangExpression> valueSpace = new LinkedHashSet<>();
                    finiteTypes.forEach(constituent -> valueSpace.addAll(constituent.valueSpace));
                    finiteType = new BFiniteType(null, valueSpace);
                }

                BType possibleType = checkRecordIndexBasedAccess(accessExpr, record, finiteType);
                if (possibleType.tag == TypeTags.UNION) {
                    possibleTypesByMember.addAll(((BUnionType) possibleType).getMemberTypes());
                } else {
                    possibleTypesByMember.add(possibleType);
                }

                if (possibleTypesByMember.contains(symTable.semanticError)) {
                    return symTable.semanticError;
                }
                actualType = possibleTypesByMember.size() == 1 ? possibleTypesByMember.iterator().next() :
                        BUnionType.create(null, possibleTypesByMember);
        }
        return actualType;
    }

    private BType getSafeType(BType type, BLangAccessExpression accessExpr) {
        if (type.tag != TypeTags.UNION) {
            return type;
        }

        // Extract the types without the error and null, and revisit access expression
        List<BType> lhsTypes = new ArrayList<>(((BUnionType) type).getMemberTypes());

        if (accessExpr.errorSafeNavigation) {
            if (!lhsTypes.contains(symTable.errorType)) {
                dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
                return symTable.semanticError;
            }

            lhsTypes = lhsTypes.stream()
                    .filter(memberType -> memberType != symTable.errorType)
                    .collect(Collectors.toList());

            if (lhsTypes.isEmpty()) {
                dlog.error(accessExpr.pos, DiagnosticCode.SAFE_NAVIGATION_NOT_REQUIRED, type);
                return symTable.semanticError;
            }
        }

        if (accessExpr.nilSafeNavigation) {
            lhsTypes = lhsTypes.stream()
                    .filter(memberType -> memberType != symTable.nilType)
                    .collect(Collectors.toList());
        }

        if (lhsTypes.size() == 1) {
            return lhsTypes.get(0);
        }

        return BUnionType.create(null, new LinkedHashSet<>(lhsTypes));
    }

    private List<BType> getTypesList(BType type) {
        if (type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) type;
            return new ArrayList<>(unionType.getMemberTypes());
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

    private BSymbol getFunctionPointerCallSymbol(BLangInvocation iExpr) {
        if (iExpr.expr == null) {
            // shouldn't reach here
            return symTable.notFoundSymbol;
        }

        BSymbol varSymbol = ((BLangVariableReference) iExpr.expr).symbol;
        if (varSymbol == null) {
            return symTable.notFoundSymbol;
        }

        BType varType = varSymbol.type;
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
        if (type.isAnydata()) {
            retType = type;
        } else {
            retType = BUnionType.create(null, type, symTable.errorType);
        }
        return symResolver.createBuiltinMethodSymbol(BLangBuiltInMethod.FREEZE, type, retType);
    }

    private BSymbol getSymbolForIsFrozenBuiltinMethod(BLangInvocation iExpr) {
        BType type = iExpr.expr.type;
        if (!types.isLikeAnydataOrNotNil(type)) {
            return symTable.notFoundSymbol;
        }
        return symResolver.createBuiltinMethodSymbol(BLangBuiltInMethod.IS_FROZEN, type, symTable.booleanType);
    }

    private boolean couldHoldTableValues(BType type, List<BType> encounteredTypes) {
        if (encounteredTypes.contains(type)) {
            return false;
        }
        encounteredTypes.add(type);

        switch (type.tag) {
            case TypeTags.TABLE:
                return true;
            case TypeTags.UNION:
                return ((BUnionType) type).getMemberTypes().stream()
                        .anyMatch(bType -> couldHoldTableValues(bType, encounteredTypes));
            case TypeTags.MAP:
                return couldHoldTableValues(((BMapType) type).constraint, encounteredTypes);
            case TypeTags.RECORD:
                BRecordType recordType = (BRecordType) type;
                return recordType.fields.stream()
                        .anyMatch(field -> couldHoldTableValues(field.type, encounteredTypes)) ||
                        (!recordType.sealed && couldHoldTableValues(recordType.restFieldType, encounteredTypes));
            case TypeTags.ARRAY:
                return couldHoldTableValues(((BArrayType) type).eType, encounteredTypes);
            case TypeTags.TUPLE:
                return ((BTupleType) type).getTupleTypes().stream()
                        .anyMatch(bType -> couldHoldTableValues(bType, encounteredTypes));
        }
        return false;
    }

    private boolean isConst(BLangExpression expression) {

        if (ConstantAnalyzer.isValidConstantExpressionNode(expression)) {
            return true;
        }

        if (expression.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }

        return (((BLangSimpleVarRef) expression).symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT;
    }

    private Name getCurrentCompUnit(BLangNode node) {
        return names.fromString(node.pos.getSource().getCompilationUnitName());
    }
}
