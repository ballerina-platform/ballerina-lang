package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.parser.NodeCloner;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.*;
import org.wso2.ballerinalang.compiler.semantics.model.types.*;
import org.wso2.ballerinalang.compiler.tree.*;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.util.*;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.BiFunction;

import static org.ballerinalang.model.symbols.SymbolOrigin.SOURCE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

public class ConstantTypeChecker extends SimpleBLangNodeAnalyzer<ConstantTypeChecker.AnalyzerData> {
    private static final CompilerContext.Key<ConstantTypeChecker> CONSTANT_TYPE_CHECKER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final TypeChecker typeChecker;
    private final TypeResolver typeResolver;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;
    private final NodeCloner nodeCloner;

    private List<BLangTypeDefinition> resolvingtypeDefinitions = new ArrayList<>();

    public ConstantTypeChecker(CompilerContext context) {
        context.put(CONSTANT_TYPE_CHECKER_KEY, this);

        this.symTable = SymbolTable.getInstance(context);
        this.symEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.types = Types.getInstance(context);
        this.typeParamAnalyzer = TypeParamAnalyzer.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
        this.missingNodesHelper = BLangMissingNodesHelper.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);
        this.typeResolver = TypeResolver.getInstance(context);
        this.nodeCloner = NodeCloner.getInstance(context);
    }

    public static ConstantTypeChecker getInstance(CompilerContext context) {
        ConstantTypeChecker constTypeChecker = context.get(CONSTANT_TYPE_CHECKER_KEY);
        if (constTypeChecker == null) {
            constTypeChecker = new ConstantTypeChecker(context);
        }

        return constTypeChecker;
    }

    public BType checkConstExpr(BLangExpression expr, SymbolEnv env, AnalyzerData data) {
        return checkConstExpr(expr, env, symTable.noType, data);
    }

    public BType checkConstExpr(BLangExpression expr, AnalyzerData data) {
        return checkConstExpr(expr, data.env, symTable.noType, data);
    }

    public BType checkConstExpr(BLangExpression expr, SymbolEnv env, BType expType, AnalyzerData data) {
        return checkConstExpr(expr, env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    public BType checkConstExpr(BLangExpression expr, BType expType, AnalyzerData data) {
        return checkConstExpr(expr, data.env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    public BType checkConstExpr(BLangExpression expr, SymbolEnv env) {
        return checkConstExpr(expr, env, symTable.noType, new Stack<>());
    }

    public BType checkConstExpr(BLangExpression expr, SymbolEnv env, BType expType, Stack<SymbolEnv> prevEnvs) {
        final AnalyzerData data = new AnalyzerData();
        data.env = env;
        data.prevEnvs = prevEnvs;
        data.commonAnalyzerData.queryFinalClauses = new Stack<>();
        data.commonAnalyzerData.queryEnvs = new Stack<>();
        return checkConstExpr(expr, env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    public BType checkConstExpr(BLangExpression expr, SymbolEnv env, BType expType, DiagnosticCode diagCode,
                                AnalyzerData data) {
        if (expr.typeChecked) {
            return expr.getBType();
        }

        SymbolEnv prevEnv = data.env;
        BType preExpType = data.expType;
        DiagnosticCode preDiagCode = data.diagCode;
        data.env = env;
        data.diagCode = diagCode;
        data.expType = expType;
        data.isTypeChecked = true;
        expr.expectedType = expType;

        expr.accept(this, data);

        expr.setTypeCheckedType(data.resultType);
        expr.typeChecked = data.isTypeChecked;
        data.env = prevEnv;
        data.expType = preExpType;
        data.diagCode = preDiagCode;

        validateAndSetExprExpectedType(expr, data);

        return data.resultType;
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {

    }

    @Override
    public void visit(BLangPackage node, AnalyzerData data) {

    }

    @Override
    public void visit(BLangLiteral literalExpr, AnalyzerData data) {
        BType literalType = setLiteralValueAndGetType(literalExpr, data.expType, data);
        if (literalType == symTable.semanticError || literalExpr.isFiniteContext) {
            return;
        }
        BType resultType = types.checkType(literalExpr, literalType, symTable.noType);
        data.resultType = getFiniteType(literalExpr.value, data.constantSymbol, literalExpr.pos, resultType);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr, AnalyzerData data) {
        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        Name varName = names.fromIdNode(varRefExpr.variableName);
        if (varName == Names.IGNORE) {
            varRefExpr.setBType(this.symTable.anyType);

            // If the variable name is a wildcard('_'), the symbol should be ignorable.
            varRefExpr.symbol = new BVarSymbol(0, true, varName,
                    names.originalNameFromIdNode(varRefExpr.variableName),
                    data.env.enclPkg.symbol.pkgID, varRefExpr.getBType(), data.env.scope.owner,
                    varRefExpr.pos, VIRTUAL);

            data.resultType = varRefExpr.getBType();
            return;
        }

        Name compUnitName = typeChecker.getCurrentCompUnit(varRefExpr);
        varRefExpr.pkgSymbol =
                symResolver.resolvePrefixSymbol(data.env, names.fromIdNode(varRefExpr.pkgAlias), compUnitName);
        if (varRefExpr.pkgSymbol == symTable.notFoundSymbol) {
            varRefExpr.symbol = symTable.notFoundSymbol;
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.UNDEFINED_MODULE, varRefExpr.pkgAlias);
        }

        if (varRefExpr.pkgSymbol != symTable.notFoundSymbol) {
            BSymbol symbol = getSymbolOfVarRef(varRefExpr.pos, data.env, names.fromIdNode(varRefExpr.pkgAlias), varName, data);
            // if no symbol, check same for object attached function // swj: do we need this?
//            if (symbol == symTable.notFoundSymbol && data.env.enclType != null) {
//                Name objFuncName = names.fromString(Symbols
//                        .getAttachedFuncSymbolName(data.env.enclType.getBType().tsymbol.name.value, varName.value));
//                symbol = symResolver.resolveStructField(varRefExpr.pos, data.env, objFuncName,
//                        data.env.enclType.getBType().tsymbol);
//            }

            if ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT) {
                BConstantSymbol constSymbol = (BConstantSymbol) symbol;
                varRefExpr.symbol = constSymbol;
                actualType = symbol.type;
            } else {
                varRefExpr.symbol = symbol;
                dlog.error(varRefExpr.pos, DiagnosticErrorCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
            }
        }

        data.resultType = types.checkType(varRefExpr, actualType, data.expType);
    }

    public void visit(BLangListConstructorExpr listConstructor, AnalyzerData data) {
        boolean containErrors = false;
        List<BType> memberTypes = new ArrayList<>();
        for (BLangExpression expr : listConstructor.exprs) {
            if (expr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                BLangExpression spreadOpExpr = ((BLangListConstructorExpr.BLangListConstructorSpreadOpExpr) expr).expr;
                BType spreadOpExprType = checkConstExpr(spreadOpExpr, data);
                BType type = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(spreadOpExprType));
                if (type.tag != TypeTags.TUPLE) {
                    containErrors = true;
                    continue;
                }
                for (BType memberType : ((BTupleType) type).getTupleTypes()) {
                    memberTypes.add(memberType);
                }
                continue;
            }

            BType tupleMemberType = checkConstExpr(expr, data);
            if (tupleMemberType == symTable.semanticError) {
                containErrors = true;
                continue;
            }
            memberTypes.add(tupleMemberType);
        }

        if (containErrors) {
            data.resultType = symTable.semanticError;
            return;
        }
        BTupleType tupleType = new BTupleType(memberTypes);
        data.resultType = tupleType;
    }

    public void visit(BLangRecordLiteral recordLiteral, AnalyzerData data) {
        boolean containErrors = false;
        SymbolEnv env = data.env;
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, recordLiteral.pos, VIRTUAL, data);
        LinkedHashMap<String, BField> inferredFields = new LinkedHashMap<>();
        List<RecordLiteralNode.RecordField> computedFields = new ArrayList<>();
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                BLangRecordLiteral.BLangRecordKey key = keyValue.key;
                if (key.computedKey) {
                    // Computed key fields should be evaluated at the end.
                    computedFields.add(field);
                    continue;
                }
                BLangExpression keyValueExpr = keyValue.valueExpr;
                BType keyValueType = checkConstExpr(keyValueExpr, data);
                BLangExpression keyExpr = key.expr;
                if (!addFields(inferredFields, keyValueType, getKeyName(keyExpr), keyExpr.pos, recordSymbol, false)) {
                    containErrors = true;
                }
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                BType varRefType = checkConstExpr((varNameField), data);
                if (!addFields(inferredFields, Types.getReferredType(varRefType), getKeyName(varNameField),
                        varNameField.pos, recordSymbol, false)) {
                    containErrors = true;
                }
            } else { // Spread Field
                BLangExpression fieldExpr = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                BType spreadOpType = checkConstExpr(fieldExpr, data);
                BType type = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(spreadOpType));
                if (type.tag != TypeTags.RECORD) {
                    containErrors = true;
                    continue;
                }

                BRecordType recordType = (BRecordType) type;
                for (BField recField : recordType.fields.values()) {
                    if (!addFields(inferredFields, Types.getReferredType(recField.type), recField.name.value,
                            fieldExpr.pos, recordSymbol, false)) {
                        containErrors = true;
                    }
                }
            }
        }

        for (RecordLiteralNode.RecordField field : computedFields) {
            BLangRecordLiteral.BLangRecordKeyValueField keyValue = (BLangRecordLiteral.BLangRecordKeyValueField) field;
            BLangRecordLiteral.BLangRecordKey key = keyValue.key;
            BType fieldName = checkConstExpr(key.expr, data);
            if (fieldName.tag == TypeTags.FINITE || ((BFiniteType) fieldName).getValueSpace().size() == 1) {
                BLangLiteral fieldNameLiteral = (BLangLiteral) ((BFiniteType) fieldName).getValueSpace().iterator().next();
                if (fieldNameLiteral.getBType().tag == TypeTags.STRING) {
                    BType keyValueType = checkConstExpr(keyValue.valueExpr, data);
                    if (!addFields(inferredFields, Types.getReferredType(keyValueType),
                            fieldNameLiteral.getValue().toString(), key.pos, recordSymbol, true)) {
                        containErrors = true;
                    }
                    continue;
                }
            }
            dlog.error(key.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES, fieldName, symTable.stringType);
            containErrors = true;
        }

        if (containErrors) {
            data.resultType = symTable.semanticError;
            return;
        }

        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.fields = inferredFields;
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;
        recordType.sealed = true;
        data.resultType = recordType;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr, AnalyzerData data) {
        BType lhsType = checkConstExpr(binaryExpr.lhsExpr, data.expType, data);
        BType rhsType = checkConstExpr(binaryExpr.rhsExpr, data.expType, data);

        // Set error type as the actual type.

        if (lhsType != symTable.semanticError && rhsType != symTable.semanticError) {
            if (lhsType.tag == TypeTags.UNION && rhsType.tag == TypeTags.UNION) {
                BSymbol opSymbol = getOpSymbolBothUnion(binaryExpr.opKind, (BUnionType) lhsType, (BUnionType) rhsType, binaryExpr, data);
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
            }
            if (lhsType.tag == TypeTags.UNION && rhsType.tag != TypeTags.UNION) {
                BSymbol opSymbol = getOpSymbolLhsUnion(binaryExpr.opKind, (BUnionType) lhsType, rhsType, binaryExpr, data, false);
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
            }
            if (lhsType.tag != TypeTags.UNION && rhsType.tag == TypeTags.UNION) {
                BSymbol opSymbol = getOpSymbolLhsUnion(binaryExpr.opKind, (BUnionType) rhsType, lhsType, binaryExpr, data, true);
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
            }
            if (lhsType.tag != TypeTags.UNION && rhsType.tag != TypeTags.UNION) {
                BSymbol opSymbol = getOpSymbolBothNonUnion(binaryExpr.opKind, lhsType, rhsType, binaryExpr, data);
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
            }
        }
        BType actualType = data.resultType;
        BType resultType = types.checkType(binaryExpr, actualType, symTable.noType);
        if (resultType == symTable.semanticError) {
            data.resultType = symTable.semanticError;
            return;
        }
        Location pos = binaryExpr.pos;
        BConstantSymbol constantSymbol = data.constantSymbol;
        if (resultType.tag == TypeTags.UNION) {
            Iterator<BType> iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            BType expType = iterator.next();
            Object resolvedValue = calculateSingletonValue(getCorrespondingFiniteType(lhsType, expType),
                    getCorrespondingFiniteType(rhsType, expType), binaryExpr.opKind, expType, binaryExpr.pos);
            List<Object> valueList = new ArrayList<>();
            valueList.add(resolvedValue);
            while (iterator.hasNext()) {
                expType = iterator.next();
                valueList.add(calculateSingletonValue(getCorrespondingFiniteType(lhsType, expType), getCorrespondingFiniteType(rhsType, expType), binaryExpr.opKind, expType, binaryExpr.pos));
            }
            iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(valueList.size());
            for (int i = 0; i < valueList.size(); i++) {
                memberTypes.add(getFiniteType(valueList.get(i), constantSymbol, pos, iterator.next()));
            }
            data.resultType = BUnionType.create(null, memberTypes);
            return;
        }
        Object resolvedValue = calculateSingletonValue(getCorrespondingFiniteType(lhsType, resultType),
                getCorrespondingFiniteType(rhsType, resultType), binaryExpr.opKind, resultType, binaryExpr.pos);
        data.resultType = getFiniteType(resolvedValue, constantSymbol, pos, resultType);
    }

    public void visit(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        BType actualType;
        BType exprType = checkConstExpr(unaryExpr.expr, data);

        if (exprType == symTable.semanticError) {
            data.resultType = symTable.semanticError;
            return;
        }

        if (exprType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> memberTypes = ((BUnionType) exprType).getMemberTypes();
            LinkedHashSet<BType> resultMemberTypes = new LinkedHashSet<>(memberTypes.size());
            BSymbol firstValidOpSymbol = symTable.notFoundSymbol;
            for (BType memberType : memberTypes) {
                BSymbol resolvedSymbol = getUnaryOpSymbol(unaryExpr, memberType, data);
                if (data.resultType == symTable.semanticError) {
                    return;
                }
                if (firstValidOpSymbol == symTable.notFoundSymbol && resolvedSymbol != symTable.notFoundSymbol) {
                    firstValidOpSymbol = resolvedSymbol;
                }
                resultMemberTypes.add(data.resultType);
            }
            actualType = BUnionType.create(null, resultMemberTypes);
            unaryExpr.opSymbol = (BOperatorSymbol) firstValidOpSymbol;
        } else {
            BSymbol resolvedSymbol = getUnaryOpSymbol(unaryExpr, exprType, data);
            if (data.resultType == symTable.semanticError) {
                return;
            }
            unaryExpr.opSymbol = (BOperatorSymbol) resolvedSymbol;
            actualType = data.resultType;;
        }

        BType resultType = types.checkType(unaryExpr, actualType, data.expType);

        if (resultType.tag == TypeTags.UNION) {
            Iterator<BType> iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            BType expType = iterator.next();
            Object resolvedValue = evaluateUnaryOperator(getCorrespondingFiniteType(exprType, expType), expType,
                    unaryExpr.operator);
            List<Object> valueList = new ArrayList<>();
            valueList.add(resolvedValue);
            while (iterator.hasNext()) {
                expType = iterator.next();
                valueList.add(evaluateUnaryOperator(getCorrespondingFiniteType(exprType, expType), expType,
                        unaryExpr.operator));
            }
            iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(valueList.size());
            for (int i = 0; i < valueList.size(); i++) {
                memberTypes.add(getFiniteType(valueList.get(i), data.constantSymbol, data.constantSymbol.pos,
                        iterator.next()));
            }
            data.resultType = BUnionType.create(null, memberTypes);
            return;
        }

        Object resolvedValue = evaluateUnaryOperator(getCorrespondingFiniteType(exprType, resultType), resultType,
                unaryExpr.operator);
        data.resultType = getFiniteType(resolvedValue, data.constantSymbol, data.constantSymbol.pos, resultType);
    }

    private BSymbol getUnaryOpSymbol(BLangUnaryExpr unaryExpr, BType exprType, AnalyzerData data) {
        BSymbol symbol = symResolver.resolveUnaryOperator(unaryExpr.operator, exprType);
        if (symbol == symTable.notFoundSymbol) {
            symbol = symResolver.getUnaryOpsForTypeSets(unaryExpr.operator, exprType);
        }
        if (symbol == symTable.notFoundSymbol) {
            dlog.error(unaryExpr.pos, DiagnosticErrorCode.UNARY_OP_INCOMPATIBLE_TYPES,
                    unaryExpr.operator, exprType);
        } else {
            unaryExpr.opSymbol = (BOperatorSymbol) symbol;
            data.resultType = symbol.type.getReturnType();
        }
        return symbol;
    }

    private Object calculateSingletonValue(BFiniteType lhs, BFiniteType rhs, OperatorKind kind,
                                                       BType type, Location currentPos) {
        if (lhs == null || rhs == null) {
            // This is a compilation error.
            // This is to avoid NPE exceptions in sub-sequent validations.
            return new BLangConstantValue(null, type);
        }

        BLangLiteral lhsLiteral = (BLangLiteral) lhs.getValueSpace().iterator().next();
        BLangLiteral rhsLiteral = (BLangLiteral) rhs.getValueSpace().iterator().next();

        // See Types.isAllowedConstantType() for supported types.
        try {
            switch (kind) {
                case ADD:
                    return calculateAddition(lhsLiteral.value, rhsLiteral.value, type);
                case SUB:
                    return calculateSubtract(lhsLiteral.value, rhsLiteral.value, type);
                case MUL:
                    return calculateMultiplication(lhsLiteral.value, rhsLiteral.value, type);
                case DIV:
                    return calculateDivision(lhsLiteral.value, rhsLiteral.value, type);
                case BITWISE_AND:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a & b, type, currentPos);
                case BITWISE_OR:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a | b, type, currentPos);
                case BITWISE_LEFT_SHIFT:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a << b, type, currentPos);
                case BITWISE_RIGHT_SHIFT:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a >> b, type, currentPos);
                case BITWISE_UNSIGNED_RIGHT_SHIFT:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a >>> b, type, currentPos);
                case BITWISE_XOR:
                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a ^ b, type, currentPos);
                default:
                    dlog.error(currentPos, DiagnosticErrorCode.CONSTANT_EXPRESSION_NOT_SUPPORTED);
            }
        } catch (NumberFormatException nfe) {
            // Ignore. This will be handled as a compiler error.
        } catch (ArithmeticException ae) {
            dlog.error(currentPos, DiagnosticErrorCode.INVALID_CONST_EXPRESSION, ae.getMessage());
        }
        // This is a compilation error already logged.
        // This is to avoid NPE exceptions in sub-sequent validations.
        return new BLangConstantValue(null, type);
    }

    private Object evaluateUnaryOperator(BFiniteType finiteType, BType type, OperatorKind kind) {
        BLangLiteral lhsLiteral = (BLangLiteral) finiteType.getValueSpace().iterator().next();
        Object value = lhsLiteral.value;
        if (value == null) {
            // This is a compilation error.
            // This is to avoid NPE exceptions in sub-sequent validations.
            return new BLangConstantValue(null, type);
        }

        try {
            switch (kind) {
                case ADD:
                    return new BLangConstantValue(value, type);
                case SUB:
                    return calculateNegation(value, type);
                case BITWISE_COMPLEMENT:
                    return calculateBitWiseComplement(value, type);
                case NOT:
                    return calculateBooleanComplement(value, type);
            }
        } catch (ClassCastException ce) {
            // Ignore. This will be handled as a compiler error.
        }
        // This is a compilation error already logged.
        // This is to avoid NPE exceptions in sub-sequent validations.
        return new BLangConstantValue(null, type);
    }

    private BLangConstantValue calculateBitWiseOp(Object lhs, Object rhs,
                                                  BiFunction<Long, Long, Long> func, BType type, Location currentPos) {
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
                Long val = func.apply((Long) lhs, (Long) rhs);
                return new BLangConstantValue(val, type);
            default:
                dlog.error(currentPos, DiagnosticErrorCode.CONSTANT_EXPRESSION_NOT_SUPPORTED);

        }
        return new BLangConstantValue(null, type);
    }

    private Object calculateAddition(Object lhs, Object rhs, BType type) {
        // TODO : Handle overflow in numeric values.
        Object result = null;
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) lhs + (Long) rhs;
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs))
                        + Double.parseDouble(String.valueOf(rhs)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.add(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
            case TypeTags.STRING:
                result = String.valueOf(lhs) + String.valueOf(rhs);
                break;
        }
        return result;
    }

    private Object calculateSubtract(Object lhs, Object rhs, BType type) {
        Object result = null;
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) lhs - (Long) rhs;
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs))
                        - Double.parseDouble(String.valueOf(rhs)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.subtract(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return result;
    }

    private Object calculateMultiplication(Object lhs, Object rhs, BType type) {
        // TODO : Handle overflow in numeric values.
        Object result = null;
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) lhs * (Long) rhs;
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs))
                        * Double.parseDouble(String.valueOf(rhs)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.multiply(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return result;
    }

    private Object calculateDivision(Object lhs, Object rhs, BType type) {
        Object result = null;
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
            case TypeTags.BYTE: // Byte will be a compiler error.
                result = (Long) ((Long) lhs / (Long) rhs);
                break;
            case TypeTags.FLOAT:
                result = String.valueOf(Double.parseDouble(String.valueOf(lhs))
                        / Double.parseDouble(String.valueOf(rhs)));
                break;
            case TypeTags.DECIMAL:
                BigDecimal lhsDecimal = new BigDecimal(String.valueOf(lhs), MathContext.DECIMAL128);
                BigDecimal rhsDecimal = new BigDecimal(String.valueOf(rhs), MathContext.DECIMAL128);
                BigDecimal resultDecimal = lhsDecimal.divide(rhsDecimal, MathContext.DECIMAL128);
                result = resultDecimal.toPlainString();
                break;
        }
        return result;
    }


    private Object calculateNegationForInt(Object value) {
        return -1 * ((Long) (value));
    }

    private Object calculateNegationForFloat(Object value) {
        return String.valueOf(-1 * Double.parseDouble(String.valueOf(value)));
    }

    private Object calculateNegationForDecimal(Object value) {
        BigDecimal valDecimal = new BigDecimal(String.valueOf(value), MathContext.DECIMAL128);
        BigDecimal negDecimal = new BigDecimal(String.valueOf(-1), MathContext.DECIMAL128);
        BigDecimal resultDecimal = valDecimal.multiply(negDecimal, MathContext.DECIMAL128);
        return resultDecimal.toPlainString();
    }

    private Object calculateNegation(Object value, BType type) {
        Object result = null;

        switch (type.tag) {
            case TypeTags.INT:
                result = calculateNegationForInt(value);
                break;
            case TypeTags.FLOAT:
                result = calculateNegationForFloat(value);
                break;
            case TypeTags.DECIMAL:
                result = calculateNegationForDecimal(value);
                break;
        }

        return result;
    }

    private Object calculateBitWiseComplement(Object value, BType type) {
        Object result = null;
        if (Types.getReferredType(type).tag == TypeTags.INT) {
            result = ~((Long) (value));
        }
        return result;
    }

    private Object calculateBooleanComplement(Object value, BType type) {
        Object result = null;
        if (Types.getReferredType(type).tag == TypeTags.BOOLEAN) {
            result = !((Boolean) (value));
        }
        return result;
    }

    public void validateAndSetExprExpectedType(BLangExpression expr, AnalyzerData data) {
        if (data.resultType.tag == TypeTags.SEMANTIC_ERROR) {
            return;
        }

        // If the expected type is a map, but a record type is inferred due to the presence of `readonly` fields in
        // the mapping constructor expression, we don't override the expected type.
        if (expr.getKind() == NodeKind.RECORD_LITERAL_EXPR && expr.expectedType != null &&
                Types.getReferredType(expr.expectedType).tag == TypeTags.MAP
                && Types.getReferredType(expr.getBType()).tag == TypeTags.RECORD) {
            return;
        }

        expr.expectedType = data.resultType;
    }

    public BType getTypeOfLiteralWithFloatDiscriminator(BLangLiteral literalExpr, Object literalValue,
                                                        BType expType, AnalyzerData data) {
        String numericLiteral = NumericLiteralSupport.stripDiscriminator(String.valueOf(literalValue));
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }
        literalExpr.value = Double.parseDouble(numericLiteral);
        return symTable.floatType;
    }

    public BType getIntegerLiteralType(BLangLiteral literalExpr, Object literalValue, BType expType, AnalyzerData data) {
        if (!(literalValue instanceof Long)) {
            dlog.error(literalExpr.pos, DiagnosticErrorCode.OUT_OF_RANGE, literalExpr.originalValue,
                    literalExpr.getBType());
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }

        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        memberTypes.add(symTable.intType);
        memberTypes.add(symTable.floatType);

        if (!NumericLiteralSupport.hasHexIndicator(literalExpr.originalValue)) {
            memberTypes.add(symTable.decimalType);
        }
        return BUnionType.create(null, memberTypes);
    }

    public BType setLiteralValueAndGetType(BLangLiteral literalExpr, BType expType, AnalyzerData data) {
        literalExpr.isFiniteContext = false;
        Object literalValue = literalExpr.value;
        BType expectedType = Types.getReferredType(expType);

        if (literalExpr.getKind() == NodeKind.NUMERIC_LITERAL) {
            NodeKind kind = ((BLangNumericLiteral) literalExpr).kind;
            if (kind == NodeKind.INTEGER_LITERAL) {
                return getIntegerLiteralType(literalExpr, literalValue, expectedType, data);
            } else if (kind == NodeKind.DECIMAL_FLOATING_POINT_LITERAL) {
                if (NumericLiteralSupport.isFloatDiscriminated(literalExpr.originalValue)) {
                    return getTypeOfLiteralWithFloatDiscriminator(literalExpr, literalValue, expectedType, data);
                } else if (NumericLiteralSupport.isDecimalDiscriminated(literalExpr.originalValue)) {
                    return getTypeOfLiteralWithDecimalDiscriminator(literalExpr, literalValue, expectedType, data);
                } else {
                    return getTypeOfDecimalFloatingPointLiteral(literalExpr, literalValue, expectedType, data);
                }
            } else {
                return getTypeOfHexFloatingPointLiteral(literalExpr, literalValue, expectedType, data);
            }
        }

        // Get the type matching to the tag from the symbol table.
        BType literalType = symTable.getTypeFromTag(literalExpr.getBType().tag);
        if (literalType.tag == TypeTags.STRING && types.isCharLiteralValue((String) literalValue)) {
            if (expectedType.tag == TypeTags.CHAR_STRING) {
                return symTable.charStringType;
            }
            if (expectedType.tag == TypeTags.UNION) {
                Set<BType> memberTypes = new HashSet<>(types.getAllTypes(expectedType, true));
                for (BType memType : memberTypes) {
                    memType = Types.getReferredType(memType);
                    if (TypeTags.isStringTypeTag(memType.tag)) {
                        return setLiteralValueAndGetType(literalExpr, memType, data);
                    } else if (memType.tag == TypeTags.JSON || memType.tag == TypeTags.ANYDATA ||
                            memType.tag == TypeTags.ANY) {
                        return setLiteralValueAndGetType(literalExpr, symTable.charStringType, data);
                    } else if (memType.tag == TypeTags.FINITE && types.isAssignableToFiniteType(memType,
                            literalExpr)) {
                        setLiteralValueForFiniteType(literalExpr, symTable.charStringType, data);
                        return literalType;
                    }
                }
            }
            boolean foundMember = types.isAssignableToFiniteType(expectedType, literalExpr);
            if (foundMember) {
                setLiteralValueForFiniteType(literalExpr, literalType, data);
                return literalType;
            }
        } else {
            if (expectedType.tag == TypeTags.FINITE) {
                boolean foundMember = types.isAssignableToFiniteType(expectedType, literalExpr);
                if (foundMember) {
                    setLiteralValueForFiniteType(literalExpr, literalType, data);
                    return literalType;
                }
            } else if (expectedType.tag == TypeTags.UNION) {
                BUnionType unionType = (BUnionType) expectedType;
                boolean foundMember = types.getAllTypes(unionType, true)
                        .stream()
                        .anyMatch(memberType -> types.isAssignableToFiniteType(memberType, literalExpr));
                if (foundMember) {
                    setLiteralValueForFiniteType(literalExpr, literalType, data);
                    return literalType;
                }
            }
        }

        if (literalExpr.getBType().tag == TypeTags.BYTE_ARRAY) {
            // check whether this is a byte array
            literalType = new BArrayType(symTable.byteType);
        }

        return literalType;
    }

    public void setLiteralValueForFiniteType(BLangLiteral literalExpr, BType type, AnalyzerData data) {
        types.setImplicitCastExpr(literalExpr, type, data.expType);
        data.resultType = type;
        literalExpr.isFiniteContext = true;
    }

    public BType getTypeOfLiteralWithDecimalDiscriminator(BLangLiteral literalExpr, Object literalValue,
                                                          BType expType, AnalyzerData data) {
        literalExpr.value = NumericLiteralSupport.stripDiscriminator(String.valueOf(literalValue));
        return symTable.decimalType;
    }

    public BType getTypeOfDecimalFloatingPointLiteral(BLangLiteral literalExpr, Object literalValue, BType expType,
                                                      AnalyzerData data) {
        String numericLiteral = String.valueOf(literalValue);
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }

        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        memberTypes.add(symTable.floatType);
        memberTypes.add(symTable.decimalType);
        return BUnionType.create(null, memberTypes);
    }

    public BType getTypeOfHexFloatingPointLiteral(BLangLiteral literalExpr, Object literalValue, BType expType,
                                                  AnalyzerData data) {
        String numericLiteral = String.valueOf(literalValue);
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }
        literalExpr.value = Double.parseDouble(numericLiteral);
        return symTable.floatType;
    }

    private BType getFiniteType(Object value, BConstantSymbol constantSymbol, Location pos, BType type) {
        switch (type.tag) {
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
                BLangNumericLiteral numericLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
                return createFiniteType(constantSymbol, updateLiteral(numericLiteral, value, type, pos));
            case TypeTags.BYTE:
                BLangNumericLiteral byteLiteral = (BLangNumericLiteral) TreeBuilder.createNumericLiteralExpression();
                return createFiniteType(constantSymbol, updateLiteral(byteLiteral, value, symTable.intType, pos));
            case TypeTags.STRING:
            case TypeTags.NIL:
            case TypeTags.BOOLEAN:
                BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
                return createFiniteType(constantSymbol, updateLiteral(literal, value, type, pos));
            case TypeTags.UNION:
                return createFiniteType(constantSymbol, value, (BUnionType) type, pos);
            default:
                return type;
        }
    }

    private BLangLiteral updateLiteral(BLangLiteral literal, Object value, BType type, Location pos) {
        literal.value = value;
        literal.isConstant = true;
        literal.setBType(type);
        literal.pos = pos;
        return literal;
    }

    private BFiniteType createFiniteType(BConstantSymbol constantSymbol, BLangExpression expr) {
        BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, constantSymbol.flags, Names.EMPTY,
                constantSymbol.pkgID, null, constantSymbol.owner,
                constantSymbol.pos, VIRTUAL);
        BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
        finiteType.addValue(expr);
        finiteType.tsymbol.type = finiteType;
        return finiteType;
    }

    private BUnionType createFiniteType(BConstantSymbol constantSymbol, Object value, BUnionType type, Location pos) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(3);
        for (BType memberType : type.getMemberTypes()) {
            BTypeSymbol finiteTypeSymbol = Symbols.createTypeSymbol(SymTag.FINITE_TYPE, constantSymbol.flags, Names.EMPTY,
                    constantSymbol.pkgID, null, constantSymbol.owner,
                    constantSymbol.pos, VIRTUAL);
            BFiniteType finiteType = new BFiniteType(finiteTypeSymbol);
            BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
            Object memberValue;
            switch (memberType.tag) {
                case TypeTags.FLOAT:
                    memberValue =  value instanceof String? Double.parseDouble((String) value):((Long) value).doubleValue();
                    break;
                case TypeTags.DECIMAL:
                    memberValue = new BigDecimal(String.valueOf(value));
                    break;
                default:
                    memberValue = value;
            }
            finiteType.addValue(updateLiteral(literal, memberValue, memberType, pos));
            finiteType.tsymbol.type = finiteType;
            memberTypes.add(finiteType);
        }

        return BUnionType.create(null, memberTypes);
    }

    private BSymbol getSymbolOfVarRef(Location pos, SymbolEnv env, Name pkgAlias, Name varName, AnalyzerData data) {
        if (pkgAlias == Names.EMPTY && data.modTable.containsKey(varName.value)) {
            BLangNode node = data.modTable.get(varName.value);
            if (node.getKind() == NodeKind.CONSTANT) {
                if (!typeResolver.resolvedConstants.contains((BLangConstant) node)) {
                    typeResolver.resolveConstant(data.env, data.modTable, (BLangConstant) node);
                }
            } else {
                dlog.error(pos, DiagnosticErrorCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
                return symTable.notFoundSymbol;
            }
        }
        return symResolver.lookupMainSpaceSymbolInPackage(pos, env, pkgAlias, varName);
    }

    private boolean addFields(LinkedHashMap<String, BField> fields, BType keyValueType, String key, Location pos,
                           BRecordTypeSymbol recordSymbol, boolean overWriteValue) {
        Name fieldName = names.fromString(key);
        if (fields.containsKey(key)) {
            if (overWriteValue) {
                BField field = fields.get(key);
                field.type = keyValueType;
                field.symbol.type = keyValueType;
                return true;
            }
            dlog.error(pos, DiagnosticErrorCode.DUPLICATE_KEY_IN_RECORD_LITERAL, key);
            return false;
        }
        Set<Flag> flags = new HashSet<>();
        flags.add(Flag.REQUIRED);
        BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, recordSymbol.pkgID , keyValueType,
                recordSymbol, symTable.builtinPos, VIRTUAL);
        fields.put(fieldName.value, new BField(fieldName, null, fieldSymbol));
        return true;
    }

    private String getKeyName(BLangExpression key) {
        return key.getKind() == NodeKind.SIMPLE_VARIABLE_REF ?
                ((BLangSimpleVarRef) key).variableName.value : (String) ((BLangLiteral) key).value;
    }

    private BRecordTypeSymbol createRecordTypeSymbol(PackageID pkgID, Location location,
                                                     SymbolOrigin origin, AnalyzerData data) {
        SymbolEnv env = data.env;
        BRecordTypeSymbol recordSymbol =
                Symbols.createRecordSymbol(Flags.ANONYMOUS,
                        names.fromString(anonymousModelHelper.getNextAnonymousTypeKey(pkgID)),
                        pkgID, null, env.scope.owner, location, origin);

        BInvokableType bInvokableType = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        BInvokableSymbol initFuncSymbol = Symbols.createFunctionSymbol(
                Flags.PUBLIC, Names.EMPTY, Names.EMPTY, env.enclPkg.symbol.pkgID, bInvokableType, env.scope.owner,
                false, symTable.builtinPos, VIRTUAL);
        initFuncSymbol.retType = symTable.nilType;
        recordSymbol.initializerFunc = new BAttachedFunction(Names.INIT_FUNCTION_SUFFIX, initFuncSymbol,
                bInvokableType, location);

        recordSymbol.scope = new Scope(recordSymbol);
        recordSymbol.scope.define(
                names.fromString(recordSymbol.name.value + "." + recordSymbol.initializerFunc.funcName.value),
                recordSymbol.initializerFunc.symbol);
        return recordSymbol;
    }

    private BFiniteType getCorrespondingFiniteType(BType type, BType expType) {
        if (type.tag == TypeTags.UNION) {
            for (BType memberType: ((BUnionType) type).getMemberTypes()) {
                BFiniteType result = getCorrespondingFiniteType(memberType, expType);
                if (result != null) {
                    return result;
                }
            }
        } else if (type.tag == TypeTags.FINITE && types.isAssignable(type, expType)) {
            return (BFiniteType) type;
        }
        return null;
    }

    private BSymbol getOpSymbolBothUnion(OperatorKind opKind, BUnionType lhsType, BUnionType rhsType, BLangBinaryExpr binaryExpr, AnalyzerData data) {
        BSymbol firstValidOpSymbol = symTable.notFoundSymbol;
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        LinkedHashSet<BType> removableLhsMemberTypes = new LinkedHashSet<>();
        LinkedHashSet<BType> removableRhsMemberTypes = new LinkedHashSet<>();
        for (BType memberTypeRhs : rhsType.getMemberTypes()) {
            removableRhsMemberTypes.add(memberTypeRhs);
        }
        for (BType memberTypeLhs : lhsType.getMemberTypes()) {
            boolean isValidLhsMemberType = false;
            for (BType memberTypeRhs : rhsType.getMemberTypes()) {
                BSymbol resultantOpSymbol = getOpSymbol(opKind, memberTypeLhs, memberTypeRhs, binaryExpr, data);
                if (data.resultType != symTable.semanticError) {
                    memberTypes.add(data.resultType);
                    isValidLhsMemberType = true;
                    if (removableRhsMemberTypes.contains(memberTypeRhs)) {
                        removableRhsMemberTypes.remove(memberTypeRhs);
                    }
                }
                if (firstValidOpSymbol == symTable.notFoundSymbol && resultantOpSymbol != symTable.notFoundSymbol) {
                    firstValidOpSymbol = resultantOpSymbol;
                }
            }
            if (!isValidLhsMemberType) {
                removableLhsMemberTypes.add(memberTypeLhs);
            }
        }
        for (BType memberTypeRhs : removableRhsMemberTypes) {
            rhsType.remove(memberTypeRhs);
        }
        for (BType memberTypeLhs : removableLhsMemberTypes) {
            lhsType.remove(memberTypeLhs);
        }
        if (memberTypes.size() != 1) {
            data.resultType = BUnionType.create(null, memberTypes);
        } else if (memberTypes.size() == 1) {
            data.resultType = memberTypes.iterator().next();
        }
        return firstValidOpSymbol;
    }

    private BSymbol getOpSymbolLhsUnion(OperatorKind opKind, BUnionType lhsType, BType rhsType, BLangBinaryExpr binaryExpr, AnalyzerData data, boolean swap) {
        BSymbol firstValidOpSymbol = symTable.notFoundSymbol;
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        LinkedHashSet<BType> removableLhsMemberTypes = new LinkedHashSet<>();
        for (BType memberTypeLhs : lhsType.getMemberTypes()) {
            boolean isValidLhsMemberType = false;
            BSymbol resultantOpSymbol;
            if (swap) {
                resultantOpSymbol = getOpSymbol(opKind, rhsType, memberTypeLhs, binaryExpr, data);
            } else {
                resultantOpSymbol = getOpSymbol(opKind, memberTypeLhs, rhsType, binaryExpr, data);
            }
            if (data.resultType != symTable.semanticError) {
                memberTypes.add(data.resultType);
                isValidLhsMemberType = true;
            }
            if (firstValidOpSymbol == symTable.notFoundSymbol && resultantOpSymbol != symTable.notFoundSymbol) {
                firstValidOpSymbol = resultantOpSymbol;
            }

            if (!isValidLhsMemberType) {
                removableLhsMemberTypes.add(memberTypeLhs);
            }
        }
        for (BType memberTypeLhs : removableLhsMemberTypes) {
            lhsType.remove(memberTypeLhs);
        }
        if (memberTypes.size() != 1) {
            data.resultType = BUnionType.create(null, memberTypes);
        } else if (memberTypes.size() == 1) {
            data.resultType = memberTypes.iterator().next();
        }
        return firstValidOpSymbol;
    }

    private BSymbol getOpSymbolBothNonUnion(OperatorKind opKind, BType lhsType, BType rhsType, BLangBinaryExpr binaryExpr, AnalyzerData data) {
        BSymbol firstValidOpSymbol = symTable.notFoundSymbol;
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        return getOpSymbol(opKind, lhsType, rhsType, binaryExpr, data);
    }

    private BSymbol getOpSymbol(OperatorKind opKind, BType lhsType, BType rhsType, BLangBinaryExpr binaryExpr, AnalyzerData data) {
        BSymbol opSymbol = symResolver.resolveBinaryOperator(binaryExpr.opKind, lhsType, rhsType);
        if (lhsType != symTable.semanticError && rhsType != symTable.semanticError) {
            // Look up operator symbol if both rhs and lhs types aren't error or xml types

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBitwiseShiftOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
            }

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBinaryBitwiseOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
            }

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getArithmeticOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
            }

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBinaryEqualityForTypeSets(binaryExpr.opKind, lhsType, rhsType,
                        binaryExpr, data.env);
            }

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getBinaryComparisonOpForTypeSets(binaryExpr.opKind, lhsType, rhsType);
            }

            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver.getRangeOpsForTypeSets(binaryExpr.opKind, lhsType, rhsType);
            }

            if (opSymbol == symTable.notFoundSymbol) {
                DiagnosticErrorCode errorCode = DiagnosticErrorCode.BINARY_OP_INCOMPATIBLE_TYPES;

                if ((binaryExpr.opKind == OperatorKind.DIV || binaryExpr.opKind == OperatorKind.MOD) &&
                        lhsType.tag == TypeTags.INT &&
                        (rhsType.tag == TypeTags.DECIMAL || rhsType.tag == TypeTags.FLOAT)) {
                    errorCode = DiagnosticErrorCode.BINARY_OP_INCOMPATIBLE_TYPES_INT_FLOAT_DIVISION;
                }

                dlog.error(binaryExpr.pos, errorCode, binaryExpr.opKind, lhsType, rhsType);
                data.resultType = symTable.semanticError;
            } else {
                data.resultType = opSymbol.type.getReturnType();
            }
        }
        return opSymbol;
    }

    private BType getValidType(BUnionType unionType, BType actualType, AnalyzerData data) {
        List<BType> validTypeList = new ArrayList<>();
        DiagnosticCode prevDiagCode = data.diagCode;
        for (BType memberType : unionType.getMemberTypes()) {
            BType resultType = getValidType(memberType, actualType, data);
            if (resultType != symTable.semanticError) {
                validTypeList.add(resultType);
                prevDiagCode = data.diagCode;
            } else {
                data.diagCode = prevDiagCode;
            }
        }

        if (validTypeList.isEmpty()) {
            return symTable.semanticError;
        } else if (validTypeList.size() == 1) {
            return validTypeList.get(0);
        }
        BType selectedType = null;
        for (BType type : validTypeList) {
            if (type.tag == TypeTags.FINITE && ((BFiniteType) type).getValueSpace().size() == 1) {
                switch (((BFiniteType) type).getValueSpace().iterator().next().getBType().tag) {
                    case TypeTags.INT:
                        return type;
                    case TypeTags.FLOAT:
                        selectedType = type;
                        break;
                    case TypeTags.DECIMAL:
                        if (selectedType == null) {
                            selectedType = type;
                        }
                        break;
                    default:
                        data.diagCode = DiagnosticErrorCode.AMBIGUOUS_TYPES;
                        return validTypeList.get(0);
                }
                continue;
            }
            data.diagCode = DiagnosticErrorCode.AMBIGUOUS_TYPES;
            return validTypeList.get(0);
        }
        return selectedType;
    }

    private BType getValidType(BRecordType type, BType actualType, AnalyzerData data) {
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(actualType.tsymbol.pkgID, actualType.tsymbol.pos,
                VIRTUAL, data);
        LinkedHashMap<String, BField> validatedFields = new LinkedHashMap<>();
        BType refType = Types.getReferredType(actualType);
        if (refType.tag != TypeTags.RECORD) {
            return symTable.semanticError;
        }
        LinkedHashMap<String, BField> actualFields = new LinkedHashMap<>();
        for (String key : ((BRecordType) refType).fields.keySet()) {
            actualFields.put(key, ((BRecordType) refType).fields.get(key));
        }
        LinkedHashMap<String, BField> targetFields = type.fields;
        for (String key : targetFields.keySet()) {
            if (!actualFields.containsKey(key)) {
                return symTable.semanticError;
            }
            BType validFieldType = getValidType(targetFields.get(key).type, actualFields.get(key).type, data);
            if (validFieldType == symTable.semanticError) {
                return symTable.semanticError;
            }
            Name fieldName = names.fromString(key);
            Set<Flag> flags = new HashSet<>();
            flags.add(Flag.REQUIRED);
            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, recordSymbol.pkgID , validFieldType,
                    recordSymbol, symTable.builtinPos, VIRTUAL);
            validatedFields.put(key, new BField(fieldName, null, fieldSymbol));
            actualFields.remove(key);
        }
        BType restFieldType = type.restFieldType;
        if (actualFields.size() != 0 && restFieldType == null) {
            return symTable.semanticError;
        }
        for (String key : actualFields.keySet()) {
            BType validFieldType = getValidType(restFieldType, actualFields.get(key).type, data);
            if (validFieldType == symTable.semanticError) {
                return symTable.semanticError;
            }
            Name fieldName = names.fromString(key);
            Set<Flag> flags = new HashSet<>();
            flags.add(Flag.REQUIRED);
            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, recordSymbol.pkgID , validFieldType,
                    recordSymbol, symTable.builtinPos, VIRTUAL);
            validatedFields.put(key, new BField(fieldName, null, fieldSymbol));
        }
        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.fields = validatedFields;
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;
        recordType.sealed = true;
        return recordType;
    }

    public BType getValidType(BType expType, BType actualType, AnalyzerData data) {
        switch (expType.tag) {
            case TypeTags.INT:
            case TypeTags.SIGNED8_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED32_INT:
            case TypeTags.BYTE:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
            case TypeTags.NIL:
            case TypeTags.BOOLEAN:
            case TypeTags.FINITE:
            case TypeTags.ANY:
                if (actualType.tag == TypeTags.UNION) {
                    for (BType memberType : ((BUnionType) actualType).getMemberTypes()) {
                        if (types.isAssignable(memberType, expType)) {
                            return memberType;
                        }
                    }
                } else {
                    if (types.isAssignable(actualType, expType)) {
                        return actualType;
                    }
                }
                return symTable.semanticError;
            case TypeTags.UNION:
                return getValidType((BUnionType) expType, actualType, data);
            case TypeTags.RECORD:
                return getValidType((BRecordType) expType, actualType, data);
            case TypeTags.MAP:
                return getValidType((BMapType) expType, actualType, data);
            case TypeTags.ARRAY:
                return getValidType((BArrayType) expType, actualType, data);
            case TypeTags.TUPLE:
                return getValidType((BTupleType) expType, actualType, data);
            default:
                return symTable.semanticError;
        }
    }

    private BType getValidType(BMapType type, BType actualType, AnalyzerData data) {
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(actualType.tsymbol.pkgID, actualType.tsymbol.pos,
                VIRTUAL, data);
        BType inferredType = Types.getReferredType(actualType);
        if (inferredType.tag != TypeTags.RECORD) {
            return symTable.semanticError;
        }
        BType constraintType = type.constraint;
        LinkedHashMap<String, BField> fields = ((BRecordType) inferredType).fields;
        LinkedHashMap<String, BField> validatedFields = new LinkedHashMap<>();
        for (String key : fields.keySet()) {
            BField field = fields.get(key);
            BType validFieldType = getValidType(constraintType, field.type, data);
            if (validFieldType == symTable.semanticError) {
                return symTable.semanticError;
            }
            Name fieldName = names.fromString(key);
            Set<Flag> flags = new HashSet<>();
            flags.add(Flag.REQUIRED);
            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, recordSymbol.pkgID , validFieldType,
                    recordSymbol, symTable.builtinPos, VIRTUAL);
            validatedFields.put(key, new BField(fieldName, null, fieldSymbol));
        }
        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.fields = validatedFields;
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;
        recordType.sealed = true;
        return recordType;
    }

    private BType getValidType(BArrayType arrayType, BType actualType, AnalyzerData data) {
        BType exprType = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(actualType));
        if (exprType.tag != TypeTags.TUPLE) {
            return symTable.semanticError;
        }
        BTupleType exprTupleType = (BTupleType) exprType;
        List<BType> tupleTypes = exprTupleType.tupleTypes;
        if (arrayType.state == BArrayState.CLOSED && arrayType.size < tupleTypes.size()) {
            return symTable.semanticError;
        }
        BType eType = arrayType.eType;
        List<BType> validatedMemberTypes = new ArrayList<>(tupleTypes.size());
        for (BType type : tupleTypes) {
            BType validatedMemberType = getValidType(eType, type, data);
            if (validatedMemberType == symTable.semanticError) {
                return symTable.semanticError;
            }
            validatedMemberTypes.add(validatedMemberType);
        }
        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                data.env.scope.owner, null, SOURCE);
        BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, validatedMemberTypes);
        tupleTypeSymbol.type = resultTupleType;
        if (arrayType.state == BArrayState.CLOSED && arrayType.size > tupleTypes.size()) {
            fillMembers(resultTupleType, arrayType, data);
        }
        return resultTupleType;
    }

    private void fillMembers(BTupleType type, BType expType, AnalyzerData data) {
        BType refType = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(expType));
        List<BType> tupleTypes = type.getTupleTypes();
        int tupleMemberCount = tupleTypes.size();
        if (refType.tag == TypeTags.ARRAY) {
            BArrayType arrayType = (BArrayType) expType;
            int noOfFillMembers = arrayType.size - tupleMemberCount;
            BType fillMemberType = getFillMemberType(arrayType.eType, data).type;
            if (fillMemberType == symTable.semanticError) {
                return;
            }
            for (int i = 0; i < noOfFillMembers; i++) {
                tupleTypes.add(fillMemberType);
            }
        } else if (refType.tag == TypeTags.TUPLE) {
            List<BType> bTypeList = ((BTupleType) expType).tupleTypes;
            for (int i = tupleMemberCount; i < bTypeList.size(); i++) {
                BType fillMemberType = getFillMemberType(bTypeList.get(i), data).type;
                if (fillMemberType == symTable.semanticError) {
                    return;
                }
                tupleTypes.add(fillMemberType);
            }
        }
    }

    private BType fillMembers(BTupleType tupleType, AnalyzerData data) {
        List<BType> bTypeList = tupleType.tupleTypes;
        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                data.env.scope.owner, null, SOURCE);
        List<BType> tupleTypes = new ArrayList<>(bTypeList.size());
        for (int i = 0; i < bTypeList.size(); i++) {
            BType fillMemberType = getFillMemberType(bTypeList.get(i), data).type;
            if (fillMemberType == symTable.semanticError) {
                return symTable.semanticError;
            }
            tupleTypes.add(fillMemberType);
        }
        BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, tupleTypes);
        tupleTypeSymbol.type = resultTupleType;
        return resultTupleType;
    }

    private BType fillMembers(BRecordType recordType, AnalyzerData data) {
        LinkedHashMap<String, BField> fields = recordType.fields;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(data.constantSymbol.pkgID, data.constantSymbol.pos,
                VIRTUAL, data);
        LinkedHashMap<String, BField> newFields = new LinkedHashMap<>();
        for (String key : fields.keySet()) {
            BField field = fields.get(key);
            if ((field.symbol.flags & Flags.REQUIRED) != Flags.REQUIRED) {
                continue;
            }
            BType fillMemberType = getFillMemberType(fields.get(key).type, data).type;
            if (fillMemberType == symTable.semanticError) {
                return symTable.semanticError;
            }
            Name fieldName = names.fromString(key);
            Set<Flag> flags = new HashSet<>();
            flags.add(Flag.REQUIRED);
            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, recordSymbol.pkgID , fillMemberType,
                    recordSymbol, symTable.builtinPos, VIRTUAL);
            newFields.put(key, new BField(fieldName, null, fieldSymbol));
        }
        BRecordType resultRecordType = new BRecordType(recordSymbol);
        resultRecordType.fields = newFields;
        recordSymbol.type = resultRecordType;
        resultRecordType.tsymbol = recordSymbol;
        resultRecordType.sealed = true;
        return resultRecordType;
    }

    private BType fillMembers(BArrayType arrayType, AnalyzerData data) {
        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                data.env.scope.owner, null, SOURCE);
        if (arrayType.state == BArrayState.OPEN) {
            BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, new ArrayList<>());
            tupleTypeSymbol.type = resultTupleType;
            return resultTupleType;
        } else if (arrayType.state == BArrayState.INFERRED) {
            return symTable.semanticError;
        }
        BType fillMemberType = getFillMemberType(arrayType.eType, data).type;
        if (fillMemberType == symTable.semanticError) {
            return symTable.semanticError;
        }
        List<BType> tupleTypes = new ArrayList<>(arrayType.size);
        for (int i = 0; i < arrayType.size; i++) {
            tupleTypes.add(fillMemberType);
        }
        BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, tupleTypes);
        tupleTypeSymbol.type = resultTupleType;
        return resultTupleType;
    }

    private FillMemberType getFillMemberType(BType expType, AnalyzerData data) {
        BType refType = Types.getReferredType(expType);
        int precedence = 0;
        BType fillMemberType;
        switch (refType.tag) {
            case TypeTags.NIL:
                precedence = 2;
                return new FillMemberType(precedence,  symTable.nilType);
            case TypeTags.BOOLEAN:
                precedence = 3;
                return new FillMemberType(precedence, symTable.falseType);
            case TypeTags.INT:
                precedence = 4;
                return new FillMemberType(precedence, getFiniteType(0l, data.constantSymbol, null, symTable.intType));
            case TypeTags.FLOAT:
                precedence = 5;
                return new FillMemberType(precedence, getFiniteType(0.0d, data.constantSymbol, null, symTable.floatType));
            case TypeTags.DECIMAL:
                precedence = 6;
                return new FillMemberType(precedence, getFiniteType(new BigDecimal(0), data.constantSymbol, null, symTable.decimalType));
            case TypeTags.STRING:
            case TypeTags.CHAR_STRING:
                precedence = 7;
                return new FillMemberType(precedence, getFiniteType("", data.constantSymbol, null, symTable.stringType));
            case TypeTags.ARRAY:
                precedence = 8;
                return new FillMemberType(precedence, fillMembers((BArrayType) refType, data));
            case TypeTags.TUPLE:
                precedence = 9;
                return new FillMemberType(precedence, fillMembers((BTupleType) refType, data));
            case TypeTags.MAP:
                precedence = 10;
                return new FillMemberType(precedence, symTable.mapType);
            case TypeTags.RECORD:
                precedence = 11;
                return new FillMemberType(precedence, fillMembers((BRecordType) refType, data));
            case TypeTags.INTERSECTION:
                precedence = 12;
                return new FillMemberType(precedence, ((BIntersectionType) refType).getEffectiveType());
            case TypeTags.TABLE:
                precedence = 13;
                return new FillMemberType(precedence, symTable.tableType);
            case TypeTags.FINITE:
                precedence = 14;
                Set<BLangExpression> valueSpace = ((BFiniteType) refType).getValueSpace();
                LinkedHashSet<BType> typeList = new LinkedHashSet<>(valueSpace.size());
                for (BLangExpression expr : valueSpace) {
                    typeList.add(expr.getBType());
                }
                fillMemberType = getbType(typeList, data);
                return new FillMemberType(precedence, fillMemberType);
            case TypeTags.UNION:
                precedence = 15;
                fillMemberType = getbType(((BUnionType) expType).getMemberTypes(), data);
                return new FillMemberType(precedence, fillMemberType);
            case TypeTags.ANY:
                precedence = 16;
                return new FillMemberType(precedence,  symTable.nilType);
            case TypeTags.ANYDATA:
                precedence = 17;
                return new FillMemberType(precedence,  symTable.nilType);
            case TypeTags.BYTE:
                precedence = 18;
                return new FillMemberType(precedence, getFiniteType(0l, data.constantSymbol, null, symTable.intType));
            case TypeTags.SIGNED8_INT:
            case TypeTags.SIGNED16_INT:
            case TypeTags.SIGNED32_INT:
            case TypeTags.UNSIGNED8_INT:
            case TypeTags.UNSIGNED16_INT:
            case TypeTags.UNSIGNED32_INT:
                precedence = 19;
                return new FillMemberType(precedence, getFiniteType(0l, data.constantSymbol, null, symTable.intType));
            case TypeTags.JSON:
                precedence = 20;
                return new FillMemberType(precedence,  symTable.nilType);
            default:
                return new FillMemberType(precedence, symTable.semanticError);
        }
    }

    private BType getbType(LinkedHashSet<BType> typeList, AnalyzerData data) {
        int minPrecedence = Integer.MAX_VALUE;
        BType fillMemberType = symTable.semanticError;
        for (BType type : typeList) {
            FillMemberType returnType = getFillMemberType(type, data);
            if (minPrecedence > returnType.precedence) {
                fillMemberType = returnType.type;
                minPrecedence = returnType.precedence;
            }
        }
        return fillMemberType;
    }

    private BType getValidType(BTupleType tupleType, BType actualType, AnalyzerData data) {
        BType exprType = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(actualType));
        if (exprType.tag != TypeTags.TUPLE) {
            return symTable.semanticError;
        }
        List<BType> actualTupleTypes = ((BTupleType) exprType).tupleTypes;
        List<BType> expTupleTypes = tupleType.tupleTypes;

        int restTypeCount = actualTupleTypes.size() - expTupleTypes.size();
        List<BType> validatedMemberTypes = new ArrayList<>();
        int memberCount = expTupleTypes.size();
        if (restTypeCount < 0) {
            memberCount += restTypeCount;
        }
        for (int i = 0; i < memberCount; i++) {
            BType validatedMemberType = getValidType(expTupleTypes.get(i), actualTupleTypes.get(i), data);
            if (validatedMemberType == symTable.semanticError) {
                return symTable.semanticError;
            }
            validatedMemberTypes.add(validatedMemberType);
        }
        BType restType = tupleType.restType;
        if (restType == null & restTypeCount > 0) {
            return symTable.semanticError;
        }
        for (int i = expTupleTypes.size(); i < actualTupleTypes.size(); i++) {
            BType validatedMemberType = getValidType(restType, actualTupleTypes.get(i), data);
            if (validatedMemberType == symTable.semanticError) {
                return symTable.semanticError;
            }
            validatedMemberTypes.add(validatedMemberType);
        }
        BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                data.env.scope.owner, null, SOURCE);
        BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, validatedMemberTypes);
        tupleTypeSymbol.type = resultTupleType;
        if (restTypeCount < 0) {
            fillMembers(resultTupleType, tupleType, data);
        }
        return resultTupleType;
    }
    
    public BType getNarrowedType(BType type) {
        switch (type.tag) {
            case TypeTags.FINITE:
                return type;
            case TypeTags.UNION:
                return ((BUnionType) type).getMemberTypes().iterator().next();
            case TypeTags.RECORD:
                for (String key : ((BRecordType) type).fields.keySet()) {
                    BField field = ((BRecordType) type).fields.get(key);
                    field.type = getNarrowedType(field.type);
                }
                return type;
            case TypeTags.ARRAY:
                ((BArrayType) type).eType = getNarrowedType(((BArrayType) type).eType);
                return type;
            case TypeTags.TUPLE:
                List<BType> tupleTypes = ((BTupleType) type).tupleTypes;
                for (int i = 0; i < tupleTypes.size(); i++) {
                    tupleTypes.add(i, getNarrowedType(tupleTypes.get(i)));
                }
            default:
                return symTable.semanticError;
        }
    }

    public static class FillMemberType {
        public int precedence;
        public BType type;
        public FillMemberType(int precedence, BType type) {
            this.precedence = precedence;
            this.type = type;
        }
    }

    public static class AnalyzerData extends TypeChecker.AnalyzerData {
        public SymbolEnv env;
        boolean isTypeChecked;
        Stack<SymbolEnv> prevEnvs;
        Types.CommonAnalyzerData commonAnalyzerData = new Types.CommonAnalyzerData();
        DiagnosticCode diagCode;
        BType expType;
        BType resultType;
        Map<String, BLangNode> modTable;
        BConstantSymbol constantSymbol;
        boolean isValidating = false;
    }
}
