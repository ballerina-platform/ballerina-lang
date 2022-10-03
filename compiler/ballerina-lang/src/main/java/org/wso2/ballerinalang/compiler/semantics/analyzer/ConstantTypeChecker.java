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
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
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
        BType literalType = setLiteralValueAndGetType(literalExpr, data);
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

        data.resultType = actualType;
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

            BType tupleMemberType = checkConstExpr(expr, data.expType, data);
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
                BType keyValueType = checkConstExpr(keyValueExpr, data.expType, data);
                BLangExpression keyExpr = key.expr;
                if (!addFields(inferredFields, keyValueType, getKeyName(keyExpr), keyExpr.pos, recordSymbol, false)) {
                    containErrors = true;
                }
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangRecordLiteral.BLangRecordVarNameField varNameField =
                        (BLangRecordLiteral.BLangRecordVarNameField) field;
                BType varRefType = checkConstExpr(varNameField, data.expType, data);
                if (!addFields(inferredFields, Types.getReferredType(varRefType), getKeyName(varNameField),
                        varNameField.pos, recordSymbol, false)) {
                    containErrors = true;
                }
            } else { // Spread Field
                BLangExpression fieldExpr = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                BType spreadOpType = checkConstExpr(fieldExpr, data.expType, data);
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
            if (fieldName.tag == TypeTags.FINITE && ((BFiniteType) fieldName).getValueSpace().size() == 1) {
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
                BSymbol opSymbol = getOpSymbolBothUnion(binaryExpr.opKind, (BUnionType) lhsType, (BUnionType) rhsType,
                        binaryExpr, data);
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
            }
            if (lhsType.tag == TypeTags.UNION && rhsType.tag != TypeTags.UNION) {
                BSymbol opSymbol = getOpSymbolLhsUnion(binaryExpr.opKind, (BUnionType) lhsType, rhsType, binaryExpr,
                        data, false);
                binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
            }
            if (lhsType.tag != TypeTags.UNION && rhsType.tag == TypeTags.UNION) {
                BSymbol opSymbol = getOpSymbolLhsUnion(binaryExpr.opKind, (BUnionType) rhsType, lhsType, binaryExpr,
                        data, true);
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
                valueList.add(calculateSingletonValue(getCorrespondingFiniteType(lhsType, expType),
                        getCorrespondingFiniteType(rhsType, expType), binaryExpr.opKind, expType, binaryExpr.pos));
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
        BType actualType = checkConstExpr(unaryExpr.expr, data.expType, data);
        BType resultType = symTable.semanticError;
        BSymbol opSymbol = symTable.notFoundSymbol;
        if (actualType.tag == TypeTags.UNION) {
            LinkedHashSet<BType> resolvedTypes = new LinkedHashSet<>(3);
            for (BType memberType : ((BUnionType) actualType).getMemberTypes()) {
                BSymbol resolvedSymbol = getUnaryOpSymbol(unaryExpr, memberType, data);
                if (resolvedSymbol != symTable.notFoundSymbol) {
                    opSymbol = resolvedSymbol;
                    resolvedTypes.add(data.resultType);
                }
            }
            if (resolvedTypes.size() == 1) {
                resultType = resolvedTypes.iterator().next();
            } else {
                resultType = BUnionType.create(null, resolvedTypes);
            }
        } else {
            opSymbol = getUnaryOpSymbol(unaryExpr, actualType, data);
            resultType = data.resultType;
        }

        if (opSymbol == symTable.notFoundSymbol) {
            data.resultType = symTable.semanticError;
            return;
        }

        Location pos = unaryExpr.pos;
        BConstantSymbol constantSymbol = data.constantSymbol;
        if (resultType.tag == TypeTags.UNION) {
            Iterator<BType> iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            BType expType = iterator.next();
            Object resolvedValue = evaluateUnaryOperator(getCorrespondingFiniteType(actualType, expType), expType,
                    unaryExpr.operator);
            List<Object> valueList = new ArrayList<>();
            valueList.add(resolvedValue);
            while (iterator.hasNext()) {
                expType = iterator.next();
                valueList.add(evaluateUnaryOperator(getCorrespondingFiniteType(actualType, expType), expType,
                        unaryExpr.operator));
            }
            iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(valueList.size());
            for (int i = 0; i < valueList.size(); i++) {
                memberTypes.add(getFiniteType(valueList.get(i), constantSymbol, pos, iterator.next()));
            }
            data.resultType = BUnionType.create(null, memberTypes);
            return;
        }
        Object resolvedValue = evaluateUnaryOperator(getCorrespondingFiniteType(actualType, resultType), resultType,
                unaryExpr.operator);
        data.resultType = getFiniteType(resolvedValue, constantSymbol, pos, resultType);
    }

    private BSymbol getUnaryOpSymbol(BLangUnaryExpr unaryExpr, BType type, AnalyzerData data) {
        if (type == symTable.semanticError) {
            return symTable.notFoundSymbol;
        }
        BType exprType = ((BFiniteType) type).getValueSpace().iterator().next().getBType();
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
                                                        AnalyzerData data) {
        String numericLiteral = NumericLiteralSupport.stripDiscriminator(String.valueOf(literalValue));
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        }
        literalExpr.value = Double.parseDouble(numericLiteral);
        return symTable.floatType;
    }

    public BType getIntegerLiteralType(BLangLiteral literalExpr, Object literalValue, AnalyzerData data) {
        if (!(literalValue instanceof Long)) {
            dlog.error(literalExpr.pos, DiagnosticErrorCode.OUT_OF_RANGE, literalExpr.originalValue,
                    literalExpr.getBType());
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        } else if (data.expType == symTable.noType) {
            // Special case the missing type node scenarios.
            return symTable.intType;
        }

        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        memberTypes.add(symTable.intType);
        memberTypes.add(symTable.floatType);

        if (!NumericLiteralSupport.hasHexIndicator(literalExpr.originalValue)) {
            memberTypes.add(symTable.decimalType);
        }
        return BUnionType.create(null, memberTypes);
    }

    public BType setLiteralValueAndGetType(BLangLiteral literalExpr, AnalyzerData data) {
        literalExpr.isFiniteContext = false;
        Object literalValue = literalExpr.value;
        //BType expectedType = Types.getReferredType(expType);

        if (literalExpr.getKind() == NodeKind.NUMERIC_LITERAL) {
            NodeKind kind = ((BLangNumericLiteral) literalExpr).kind;
            if (kind == NodeKind.INTEGER_LITERAL) {
                return getIntegerLiteralType(literalExpr, literalValue, data);
            } else if (kind == NodeKind.DECIMAL_FLOATING_POINT_LITERAL) {
                if (NumericLiteralSupport.isFloatDiscriminated(literalExpr.originalValue)) {
                    return getTypeOfLiteralWithFloatDiscriminator(literalExpr, literalValue, data);
                } else if (NumericLiteralSupport.isDecimalDiscriminated(literalExpr.originalValue)) {
                    return getTypeOfLiteralWithDecimalDiscriminator(literalExpr, literalValue);
                } else {
                    return getTypeOfDecimalFloatingPointLiteral(literalExpr, literalValue, data);
                }
            } else {
                return getTypeOfHexFloatingPointLiteral(literalExpr, literalValue, data);
            }
        }

        // Get the type matching to the tag from the symbol table.
        BType literalType = symTable.getTypeFromTag(literalExpr.getBType().tag);
        if (literalType.tag == TypeTags.STRING && types.isCharLiteralValue((String) literalValue)) {
            boolean foundMember = types.isAssignableToFiniteType(symTable.noType, literalExpr);
            if (foundMember) {
                setLiteralValueForFiniteType(literalExpr, literalType, data);
                return literalType;
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

    public BType getTypeOfLiteralWithDecimalDiscriminator(BLangLiteral literalExpr, Object literalValue) {
        literalExpr.value = NumericLiteralSupport.stripDiscriminator(String.valueOf(literalValue));
        return symTable.decimalType;
    }

    public BType getTypeOfDecimalFloatingPointLiteral(BLangLiteral literalExpr, Object literalValue,
                                                      AnalyzerData data) {
        String numericLiteral = String.valueOf(literalValue);
        if (!types.validateFloatLiteral(literalExpr.pos, numericLiteral)) {
            data.resultType = symTable.semanticError;
            return symTable.semanticError;
        } else if (data.expType == symTable.noType) {
            return symTable.floatType;
        }

        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>();
        memberTypes.add(symTable.floatType);
        memberTypes.add(symTable.decimalType);
        return BUnionType.create(null, memberTypes);
    }

    public BType getTypeOfHexFloatingPointLiteral(BLangLiteral literalExpr, Object literalValue,
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

    public static class GetConstantValidType implements TypeVisitor {

        private static final CompilerContext.Key<ConstantTypeChecker.GetConstantValidType> GET_CONSTANT_VALID_TYPE_KEY =
                new CompilerContext.Key<>();

        private final SymbolTable symTable;
        private final Types types;
        private final ConstantTypeChecker constantTypeChecker;
        private final Names names;
        private final ConstantTypeChecker.FillMembers fillMembers;

        private AnalyzerData data;

        public GetConstantValidType(CompilerContext context) {
            context.put(GET_CONSTANT_VALID_TYPE_KEY, this);

            this.symTable = SymbolTable.getInstance(context);
            this.types = Types.getInstance(context);
            this.constantTypeChecker = ConstantTypeChecker.getInstance(context);
            this.names = Names.getInstance(context);
            this.fillMembers = FillMembers.getInstance(context);
        }

        public static ConstantTypeChecker.GetConstantValidType getInstance(CompilerContext context) {
            ConstantTypeChecker.GetConstantValidType getConstantValidType = context.get(GET_CONSTANT_VALID_TYPE_KEY);
            if (getConstantValidType == null) {
                getConstantValidType = new ConstantTypeChecker.GetConstantValidType(context);
            }

            return getConstantValidType;
        }

        public BType getValidType(BType type, BType expType, AnalyzerData data) {
            BType preExpType = data.expType;
            data.expType = expType;
            type.accept(this);
            data.expType = preExpType;

            return data.resultType;
        }

        @Override
        public void visit(BAnnotationType bAnnotationType) {

        }

        @Override
        public void visit(BArrayType bArrayType) {
            BType exprType = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(data.expType));
            if (exprType.tag != TypeTags.TUPLE) {
                data.resultType = symTable.semanticError;
                return;
            }
            BTupleType exprTupleType = (BTupleType) exprType;
            List<BType> tupleTypes = exprTupleType.tupleTypes;
            if (bArrayType.state == BArrayState.CLOSED && bArrayType.size < tupleTypes.size()) {
                data.resultType = symTable.semanticError;
                return;
            }
            BType eType = bArrayType.eType;
            List<BType> validatedMemberTypes = new ArrayList<>(tupleTypes.size());
            for (BType type : tupleTypes) {
                BType validatedMemberType = getValidType(eType, type, data);
                if (validatedMemberType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                validatedMemberTypes.add(validatedMemberType);
            }
            BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                    Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                    data.env.scope.owner, null, SOURCE);
            BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, validatedMemberTypes);
            tupleTypeSymbol.type = resultTupleType;
            if (bArrayType.state == BArrayState.CLOSED && bArrayType.size > tupleTypes.size()) {
                fillMembers.addFillMembers(resultTupleType, bArrayType, data);
            }
            data.resultType = resultTupleType;
        }

        @Override
        public void visit(BBuiltInRefType bBuiltInRefType) {

        }

        @Override
        public void visit(BAnyType bAnyType) {

        }

        @Override
        public void visit(BAnydataType bAnydataType) {

        }

        @Override
        public void visit(BErrorType bErrorType) {

        }

        @Override
        public void visit(BFiniteType bFiniteType) {

        }

        @Override
        public void visit(BInvokableType bInvokableType) {

        }

        @Override
        public void visit(BJSONType bjsonType) {

        }

        @Override
        public void visit(BMapType bMapType) {
            BRecordTypeSymbol recordSymbol = constantTypeChecker.createRecordTypeSymbol(data.expType.tsymbol.pkgID,
                    data.expType.tsymbol.pos, VIRTUAL, data);
            BType inferredType = Types.getReferredType(data.expType);
            if (inferredType.tag != TypeTags.RECORD) {
                data.resultType =  symTable.semanticError;
                return;
            }
            BType constraintType = bMapType.constraint;
            LinkedHashMap<String, BField> fields = ((BRecordType) inferredType).fields;
            LinkedHashMap<String, BField> validatedFields = new LinkedHashMap<>();
            for (String key : fields.keySet()) {
                BField field = fields.get(key);
                BType validFieldType = getValidType(constraintType, field.type, data);
                if (validFieldType == symTable.semanticError) {
                    data.resultType =  symTable.semanticError;
                    return;
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
            data.resultType = recordType;
        }

        @Override
        public void visit(BStreamType bStreamType) {

        }

        @Override
        public void visit(BTypedescType bTypedescType) {

        }

        @Override
        public void visit(BTypeReferenceType bTypeReferenceType) {

        }

        @Override
        public void visit(BParameterizedType bTypedescType) {

        }

        @Override
        public void visit(BNeverType bNeverType) {

        }

        @Override
        public void visit(BNilType bNilType) {

        }

        @Override
        public void visit(BNoType bNoType) {

        }

        @Override
        public void visit(BPackageType bPackageType) {

        }

        @Override
        public void visit(BStructureType bStructureType) {

        }

        @Override
        public void visit(BTupleType bTupleType) {
            BType exprType = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(data.expType));
            if (exprType.tag != TypeTags.TUPLE) {
                data.resultType = symTable.semanticError;
                return;
            }
            List<BType> actualTupleTypes = ((BTupleType) exprType).tupleTypes;
            List<BType> expTupleTypes = bTupleType.tupleTypes;

            int restTypeCount = actualTupleTypes.size() - expTupleTypes.size();
            List<BType> validatedMemberTypes = new ArrayList<>();
            int memberCount = expTupleTypes.size();
            if (restTypeCount < 0) {
                memberCount += restTypeCount;
            }
            for (int i = 0; i < memberCount; i++) {
                BType validatedMemberType = getValidType(expTupleTypes.get(i), actualTupleTypes.get(i), data);
                if (validatedMemberType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                validatedMemberTypes.add(validatedMemberType);
            }
            BType restType = bTupleType.restType;
            if (restType == null & restTypeCount > 0) {
                data.resultType = symTable.semanticError;
                return;
            }
            for (int i = expTupleTypes.size(); i < actualTupleTypes.size(); i++) {
                BType validatedMemberType = getValidType(restType, actualTupleTypes.get(i), data);
                if (validatedMemberType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                validatedMemberTypes.add(validatedMemberType);
            }
            BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                    Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                    data.env.scope.owner, null, SOURCE);
            BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, validatedMemberTypes);
            tupleTypeSymbol.type = resultTupleType;
            if (restTypeCount < 0) {
                fillMembers.addFillMembers(resultTupleType, bTupleType, data);
            }
            data.resultType = resultTupleType;
        }

        @Override
        public void visit(BUnionType bUnionType) {
            List<BType> validTypeList = new ArrayList<>();
            DiagnosticCode prevDiagCode = data.diagCode;
            for (BType memberType : bUnionType.getMemberTypes()) {
                BType resultType = getValidType(memberType, data.expType, data);
                if (resultType != symTable.semanticError) {
                    validTypeList.add(resultType);
                    prevDiagCode = data.diagCode;
                } else {
                    data.diagCode = prevDiagCode;
                }
            }

            if (validTypeList.isEmpty()) {
                data.resultType = symTable.semanticError;
                return;
            } else if (validTypeList.size() == 1) {
                data.resultType = validTypeList.get(0);
                return;
            }
            BType selectedType = null;
            for (BType type : validTypeList) {
                if (type.tag == TypeTags.FINITE && ((BFiniteType) type).getValueSpace().size() == 1) {
                    switch (((BFiniteType) type).getValueSpace().iterator().next().getBType().tag) {
                        case TypeTags.INT:
                            data.resultType = type;
                            return;
                        case TypeTags.FLOAT:
                            selectedType = type;
                            break;
                        case TypeTags.DECIMAL:
                            if (selectedType == null) {
                                selectedType = type;
                            } else if (selectedType.tag == TypeTags.DECIMAL) {
                                data.diagCode = DiagnosticErrorCode.AMBIGUOUS_TYPES;
                                data.resultType = validTypeList.get(0);
                                return;
                            }
                            break;
                        default:
                            data.diagCode = DiagnosticErrorCode.AMBIGUOUS_TYPES;
                            data.resultType = validTypeList.get(0);
                            return;
                    }
                    continue;
                }
                data.diagCode = DiagnosticErrorCode.AMBIGUOUS_TYPES;
                data.resultType = validTypeList.get(0);
                return;
            }
            data.resultType = selectedType;
            return;
        }

        @Override
        public void visit(BIntersectionType bIntersectionType) {

        }

        @Override
        public void visit(BXMLType bxmlType) {

        }

        @Override
        public void visit(BTableType bTableType) {

        }

        @Override
        public void visit(BRecordType bRecordType) {
            BRecordTypeSymbol recordSymbol = constantTypeChecker.createRecordTypeSymbol(data.expType.tsymbol.pkgID,
                    data.expType.tsymbol.pos,
                    VIRTUAL, data);
            LinkedHashMap<String, BField> validatedFields = new LinkedHashMap<>();
            BType refType = Types.getReferredType(data.expType);
            if (refType.tag != TypeTags.RECORD) {
                data.resultType = symTable.semanticError;
                return;
            }
            LinkedHashMap<String, BField> actualFields = new LinkedHashMap<>();
            for (String key : ((BRecordType) refType).fields.keySet()) {
                actualFields.put(key, ((BRecordType) refType).fields.get(key));
            }
            LinkedHashMap<String, BField> targetFields = bRecordType.fields;
            for (String key : targetFields.keySet()) {
                if (!actualFields.containsKey(key)) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                BType validFieldType = getValidType(targetFields.get(key).type, actualFields.get(key).type, data);
                if (validFieldType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                Name fieldName = names.fromString(key);
                Set<Flag> flags = new HashSet<>();
                flags.add(Flag.REQUIRED);
                BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, recordSymbol.pkgID , validFieldType,
                        recordSymbol, symTable.builtinPos, VIRTUAL);
                validatedFields.put(key, new BField(fieldName, null, fieldSymbol));
                actualFields.remove(key);
            }
            BType restFieldType = bRecordType.restFieldType;
            if (actualFields.size() != 0 && restFieldType == null) {
                data.resultType = symTable.semanticError;
                return;
            }
            for (String key : actualFields.keySet()) {
                BType validFieldType = getValidType(restFieldType, actualFields.get(key).type, data);
                if (validFieldType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
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
            data.resultType = recordType;
        }

        @Override
        public void visit(BObjectType bObjectType) {

        }

        @Override
        public void visit(BType bType) {
            switch (bType.tag) {
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
                    if (data.expType.tag == TypeTags.UNION) {
                        for (BType memberType : ((BUnionType) data.expType).getMemberTypes()) {
                            if (types.isAssignable(memberType, data.expType)) {
                                data.resultType =  memberType;
                                return;
                            }
                        }
                    } else {
                        if (types.isAssignable(bType, data.expType)) {
                            data.resultType = bType;
                            return;
                        }
                    }
                default:
                    data.resultType = symTable.semanticError;
            }
        }

        @Override
        public void visit(BFutureType bFutureType) {

        }

        @Override
        public void visit(BHandleType bHandleType) {

        }
    }

    public static class FillMembers implements TypeVisitor {

        private static final CompilerContext.Key<ConstantTypeChecker.FillMembers> FILL_MEMBERS_KEY =
                new CompilerContext.Key<>();

        private final SymbolTable symTable;
        private final Types types;
        private final ConstantTypeChecker constantTypeChecker;
        private final Names names;
        private final BLangDiagnosticLog dlog;

        private AnalyzerData data;

        public FillMembers(CompilerContext context) {
            context.put(FILL_MEMBERS_KEY, this);

            this.symTable = SymbolTable.getInstance(context);
            this.types = Types.getInstance(context);
            this.constantTypeChecker = ConstantTypeChecker.getInstance(context);
            this.names = Names.getInstance(context);
            this.dlog = BLangDiagnosticLog.getInstance(context);;
        }

        public static ConstantTypeChecker.FillMembers getInstance(CompilerContext context) {
            ConstantTypeChecker.FillMembers fillMembers = context.get(FILL_MEMBERS_KEY);
            if (fillMembers == null) {
                fillMembers = new ConstantTypeChecker.FillMembers(context);
            }

            return fillMembers;
        }

        public void addFillMembers(BTupleType type, BType expType, AnalyzerData data) {
            BType refType = Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(expType));
            List<BType> tupleTypes = type.getTupleTypes();
            int tupleMemberCount = tupleTypes.size();
            if (refType.tag == TypeTags.ARRAY) {
                BArrayType arrayType = (BArrayType) expType;
                int noOfFillMembers = arrayType.size - tupleMemberCount;
                BType fillMemberType = getFillMembers(arrayType.eType, data);
                if (fillMemberType == symTable.semanticError) {
                    return;
                }
                for (int i = 0; i < noOfFillMembers; i++) {
                    tupleTypes.add(fillMemberType);
                }
            } else if (refType.tag == TypeTags.TUPLE) {
                List<BType> bTypeList = ((BTupleType) expType).tupleTypes;
                for (int i = tupleMemberCount; i < bTypeList.size(); i++) {
                    BType fillMemberType = getFillMembers(bTypeList.get(i), data);
                    if (fillMemberType == symTable.semanticError) {
                        return;
                    }
                    tupleTypes.add(fillMemberType);
                }
            }
        }

        public BType getFillMembers(BType type, AnalyzerData data) {
            data.resultType = symTable.semanticError;
            type.accept(this);

            if (data.resultType == symTable.semanticError) {
                dlog.error(data.constantSymbol.pos, DiagnosticErrorCode.INVALID_LIST_CONSTRUCTOR_ELEMENT_TYPE,
                        data.expType);
            }
            return data.resultType;
        }

        @Override
        public void visit(BAnnotationType bAnnotationType) {

        }

        @Override
        public void visit(BArrayType arrayType) {
            BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                    Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                    data.env.scope.owner, null, SOURCE);
            if (arrayType.state == BArrayState.OPEN) {
                BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, new ArrayList<>());
                tupleTypeSymbol.type = resultTupleType;
                data.resultType = resultTupleType;
                return;
            } else if (arrayType.state == BArrayState.INFERRED) {
                data.resultType = symTable.semanticError;
                return;
            }
            BType fillMemberType = getFillMembers(arrayType.eType, data);
            if (fillMemberType == symTable.semanticError) {
                data.resultType = symTable.semanticError;
                return;
            }
            List<BType> tupleTypes = new ArrayList<>(arrayType.size);
            for (int i = 0; i < arrayType.size; i++) {
                tupleTypes.add(fillMemberType);
            }
            BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, tupleTypes);
            tupleTypeSymbol.type = resultTupleType;
            data.resultType = resultTupleType;
        }

        @Override
        public void visit(BBuiltInRefType bBuiltInRefType) {

        }

        @Override
        public void visit(BAnyType bAnyType) {
            data.resultType = symTable.nilType;
        }

        @Override
        public void visit(BAnydataType bAnydataType) {
            data.resultType = symTable.nilType;
        }

        @Override
        public void visit(BErrorType bErrorType) {

        }

        @Override
        public void visit(BFiniteType finiteType) {
            Set<BLangExpression> valueSpace = finiteType.getValueSpace();
            LinkedHashSet<BType> typeList = new LinkedHashSet<>(valueSpace.size());
            for (BLangExpression expr : valueSpace) {
                typeList.add(expr.getBType());
            }
            data.resultType = getFillMembers(BUnionType.create(null, typeList), data);;
        }

        @Override
        public void visit(BInvokableType bInvokableType) {

        }

        @Override
        public void visit(BJSONType bjsonType) {
            data.resultType = symTable.nilType;
        }

        @Override
        public void visit(BMapType bMapType) {
            data.resultType = symTable.mapType;
        }

        @Override
        public void visit(BStreamType bStreamType) {

        }

        @Override
        public void visit(BTypedescType bTypedescType) {

        }

        @Override
        public void visit(BTypeReferenceType bTypeReferenceType) {

        }

        @Override
        public void visit(BParameterizedType bTypedescType) {

        }

        @Override
        public void visit(BNeverType bNeverType) {

        }

        @Override
        public void visit(BNilType bNilType) {
            data.resultType = symTable.nilType;
        }

        @Override
        public void visit(BNoType bNoType) {

        }

        @Override
        public void visit(BPackageType bPackageType) {

        }

        @Override
        public void visit(BStructureType bStructureType) {

        }

        @Override
        public void visit(BTupleType tupleType) {
            List<BType> bTypeList = tupleType.tupleTypes;
            BTypeSymbol tupleTypeSymbol = Symbols.createTypeSymbol(SymTag.TUPLE_TYPE, Flags.asMask(EnumSet.of(Flag.PUBLIC)),
                    Names.EMPTY, data.env.enclPkg.symbol.pkgID, null,
                    data.env.scope.owner, null, SOURCE);
            List<BType> tupleTypes = new ArrayList<>(bTypeList.size());
            for (int i = 0; i < bTypeList.size(); i++) {
                BType fillMemberType = getFillMembers(bTypeList.get(i), data);
                if (fillMemberType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
                }
                tupleTypes.add(fillMemberType);
            }
            BTupleType resultTupleType = new BTupleType(tupleTypeSymbol, tupleTypes);
            tupleTypeSymbol.type = resultTupleType;
            data.resultType = resultTupleType;
            return;
        }

        @Override
        public void visit(BUnionType unionType) {
            LinkedHashSet<BType> memberTypes = unionType.getMemberTypes();
            if (memberTypes.size() == 1) {
                getFillMembers(memberTypes.iterator().next(), data);
            }
            if (memberTypes.size() > 2 || !unionType.isNullable()) {
                data.resultType = symTable.semanticError;
                return;
            }
            data.resultType = symTable.nilType;
        }

        @Override
        public void visit(BIntersectionType intersectionType) {
            data.resultType = getFillMembers(intersectionType.getEffectiveType(), data);
        }

        @Override
        public void visit(BXMLType bxmlType) {

        }

        @Override
        public void visit(BTableType bTableType) {
            data.resultType = symTable.tableType;
        }

        @Override
        public void visit(BRecordType recordType) {
            LinkedHashMap<String, BField> fields = recordType.fields;
            BRecordTypeSymbol recordSymbol = constantTypeChecker.createRecordTypeSymbol(data.constantSymbol.pkgID,
                    data.constantSymbol.pos, VIRTUAL, data);
            LinkedHashMap<String, BField> newFields = new LinkedHashMap<>();
            for (String key : fields.keySet()) {
                BField field = fields.get(key);
                if ((field.symbol.flags & Flags.REQUIRED) != Flags.REQUIRED) {
                    continue;
                }
                BType fillMemberType = getFillMembers(fields.get(key).type, data);
                if (fillMemberType == symTable.semanticError) {
                    data.resultType = symTable.semanticError;
                    return;
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
            data.resultType = resultRecordType;
        }

        @Override
        public void visit(BObjectType bObjectType) {

        }

        @Override
        public void visit(BType type) {
            BType refType = Types.getReferredType(type);
            switch (refType.tag) {
                case TypeTags.BOOLEAN:
                    data.resultType = symTable.falseType;
                    return;
                case TypeTags.INT:
                case TypeTags.SIGNED8_INT:
                case TypeTags.SIGNED16_INT:
                case TypeTags.SIGNED32_INT:
                case TypeTags.UNSIGNED8_INT:
                case TypeTags.UNSIGNED16_INT:
                case TypeTags.UNSIGNED32_INT:
                case TypeTags.BYTE:
                    data.resultType = constantTypeChecker.getFiniteType(0l, data.constantSymbol, null, symTable.intType);
                    return;
                case TypeTags.FLOAT:
                    data.resultType = constantTypeChecker.getFiniteType(0.0d, data.constantSymbol, null, symTable.floatType);
                    return;
                case TypeTags.DECIMAL:
                    data.resultType = constantTypeChecker.getFiniteType(new BigDecimal(0), data.constantSymbol, null, symTable.decimalType);
                    return;
                case TypeTags.STRING:
                case TypeTags.CHAR_STRING:
                    data.resultType = constantTypeChecker.getFiniteType("", data.constantSymbol, null, symTable.stringType);
                    return;
                default:
                    data.resultType = symTable.semanticError;
            }
        }

        @Override
        public void visit(BFutureType bFutureType) {

        }

        @Override
        public void visit(BHandleType bHandleType) {

        }
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

    public BLangConstantValue getConstantValue(BType type) {
        switch (type.tag) {
            case TypeTags.FINITE:
                BLangExpression expr  =((BFiniteType) type).getValueSpace().iterator().next();
                return new BLangConstantValue (((BLangLiteral) expr).value, expr.getBType());
            case TypeTags.INTERSECTION:
                return getConstantValue(((BIntersectionType) type).getEffectiveType());
            case TypeTags.RECORD:
                Map<String, BLangConstantValue> fields = new HashMap<>();
                LinkedHashMap<String, BField> recordFields = ((BRecordType) type).fields;
                for (String key : recordFields.keySet()) {
                    BLangConstantValue constantValue = getConstantValue(recordFields.get(key).type);
                    fields.put(key, constantValue);
                }
                return new BLangConstantValue(recordFields, type);
            case TypeTags.TUPLE:
                List<BLangConstantValue> members = new ArrayList<>();
                List<BType> tupleTypes = ((BTupleType) type).tupleTypes;
                for (BType memberType : tupleTypes) {
                    BLangConstantValue constantValue = getConstantValue(memberType);
                    members.add(constantValue);
                }
                return new BLangConstantValue(members, type);
            default:
                return null;
        }
    }

    public static class ResolveConstantExpressionType extends SimpleBLangNodeAnalyzer<ConstantTypeChecker.AnalyzerData> {

        private static final CompilerContext.Key<ConstantTypeChecker.ResolveConstantExpressionType>
                RESOLVE_CONSTANT_EXPRESSION_TYPE = new CompilerContext.Key<>();

        private final SymbolTable symTable;
        private final Types types;
        private final ConstantTypeChecker constantTypeChecker;

        private List<BLangTypeDefinition> resolvingtypeDefinitions = new ArrayList<>();

        public ResolveConstantExpressionType(CompilerContext context) {
            context.put(RESOLVE_CONSTANT_EXPRESSION_TYPE, this);

            this.symTable = SymbolTable.getInstance(context);
            this.types = Types.getInstance(context);
            this.constantTypeChecker = ConstantTypeChecker.getInstance(context);
        }

        public static ResolveConstantExpressionType getInstance(CompilerContext context) {
            ResolveConstantExpressionType resolveConstantExpressionType = context.get(RESOLVE_CONSTANT_EXPRESSION_TYPE);
            if (resolveConstantExpressionType == null) {
                resolveConstantExpressionType = new ResolveConstantExpressionType(context);
            }

            return resolveConstantExpressionType;
        }

        public BType resolveConstExpr(BLangExpression expr, BType expType, AnalyzerData data) {
            return resolveConstExpr(expr, data.env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
        }

        public BType resolveConstExpr(BLangExpression expr, SymbolEnv env, BType expType, DiagnosticCode diagCode,
                                    AnalyzerData data) {

            SymbolEnv prevEnv = data.env;
            BType preExpType = data.expType;
            DiagnosticCode preDiagCode = data.diagCode;
            data.env = env;
            data.diagCode = diagCode;
            data.expType = expType;
            expr.expectedType = expType;

            expr.accept(this, data);

            data.env = prevEnv;
            data.expType = preExpType;
            data.diagCode = preDiagCode;

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
            updateBlangExprType(literalExpr, data);
        }

        private void updateBlangExprType(BLangExpression expression, AnalyzerData data) {
            BType expressionType = expression.getBType();
            if (expressionType.tag != TypeTags.UNION) {
                return;
            }

            BType targetType = symTable.noType;
            BType expType = data.expType;
            if (expType.tag == TypeTags.FINITE) {
                targetType = ((BFiniteType) expType).getValueSpace().iterator().next().getBType();
            }

            for (BType memberType : ((BUnionType) expressionType).getMemberTypes()) {
                BType type = ((BFiniteType) memberType).getValueSpace().iterator().next().getBType();

                if (type.tag == targetType.tag) {
                    expression.setBType(memberType);
                    return;
                }
            }
        }

        @Override
        public void visit(BLangSimpleVarRef varRefExpr, AnalyzerData data) {

        }

        public void visit(BLangListConstructorExpr listConstructor, AnalyzerData data) {
            BType resolvedType = data.expType;
            BTupleType tupleType = (BTupleType) ((resolvedType.tag == TypeTags.INTERSECTION)?
                    ((BIntersectionType) resolvedType).effectiveType : resolvedType);
            List<BType> resolvedMemberType = tupleType.tupleTypes;
            listConstructor.setBType(data.expType);
            int currentListIndex = 0;
            for (BLangExpression memberExpr : listConstructor.exprs) {
                if (memberExpr.getKind() == NodeKind.LIST_CONSTRUCTOR_SPREAD_OP) {
                    BLangListConstructorExpr.BLangListConstructorSpreadOpExpr spreadOp = (BLangListConstructorExpr.BLangListConstructorSpreadOpExpr) memberExpr;
                    BTupleType type = (BTupleType) Types.getReferredType(types.getTypeWithEffectiveIntersectionTypes(spreadOp.expr.getBType()));
                    spreadOp.setBType(spreadOp.expr.getBType());
                    currentListIndex += type.tupleTypes.size();
                    continue;
                }
                resolveConstExpr(memberExpr, resolvedMemberType.get(currentListIndex), data);
                currentListIndex++;
            }
        }

        public void visit(BLangRecordLiteral recordLiteral, AnalyzerData data) {
            BType expFieldType;
            BType resolvedType = data.expType;
            recordLiteral.setBType(data.expType);
            for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
                if (field.isKeyValueField()) {
                    BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                            (BLangRecordLiteral.BLangRecordKeyValueField) field;
                    BLangRecordLiteral.BLangRecordKey key = keyValue.key;
                    if (key.computedKey) {
                        BLangRecordLiteral.BLangRecordKeyValueField computedKeyValue = (BLangRecordLiteral.BLangRecordKeyValueField) field;
                        BLangRecordLiteral.BLangRecordKey computedKey = computedKeyValue.key;
                        BType fieldName = constantTypeChecker.checkConstExpr(computedKey.expr, data);
                        BLangLiteral fieldNameLiteral = (BLangLiteral) ((BFiniteType) fieldName).getValueSpace().iterator().next();
                        expFieldType = getResolvedFieldType(constantTypeChecker.getKeyName(fieldNameLiteral), resolvedType);
                        resolveConstExpr(computedKey.expr, expFieldType, data);
                        continue;
                    }
                    BLangExpression keyValueExpr = keyValue.valueExpr;
                    expFieldType = getResolvedFieldType(constantTypeChecker.getKeyName(key.expr), resolvedType);
                    resolveConstExpr(keyValueExpr, expFieldType, data);
                } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    BLangRecordLiteral.BLangRecordVarNameField varNameField =
                            (BLangRecordLiteral.BLangRecordVarNameField) field;
                    expFieldType = getResolvedFieldType(constantTypeChecker.getKeyName(varNameField), resolvedType);
                    resolveConstExpr(varNameField, expFieldType, data);
                } else {
                    // Spread Field
                    // Spread fields are not required to resolve separately since they are constant references.
                    BLangRecordLiteral.BLangRecordSpreadOperatorField spreadField =
                            (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
                    spreadField.setBType(spreadField.expr.getBType());
                }
            }
        }

        private BType getResolvedFieldType(Object targetKey, BType resolvedType) {
            BRecordType recordType = (BRecordType) ((resolvedType.tag == TypeTags.INTERSECTION)?
                    ((BIntersectionType) resolvedType).effectiveType : resolvedType);
            for (String key : recordType.getFields().keySet()) {
                if (key.equals(targetKey)) {
                    return recordType.getFields().get(key).type;
                }
            }
            return null;
        }

        @Override
        public void visit(BLangBinaryExpr binaryExpr, AnalyzerData data) {
            switch (binaryExpr.opKind) {
                case OR:
                case AND:
                case ADD:
                case SUB:
                case MUL:
                case DIV:
                case MOD:
                    resolveConstExpr(binaryExpr.lhsExpr, data.expType, data);
                    resolveConstExpr(binaryExpr.rhsExpr, data.expType, data);
                    updateBlangExprType(binaryExpr, data);
                    BInvokableType invokableType = (BInvokableType) binaryExpr.opSymbol.type;
                    ArrayList<BType> paramTypes = new ArrayList<>(2);
                    paramTypes.add(binaryExpr.lhsExpr.getBType());
                    paramTypes.add(binaryExpr.rhsExpr.getBType());
                    invokableType.paramTypes = paramTypes;
                    invokableType.retType = binaryExpr.getBType();
                    return;
            }
        }

        public void visit(BLangUnaryExpr unaryExpr, AnalyzerData data) {
            updateBlangExprType(unaryExpr.expr, data);
            updateBlangExprType(unaryExpr, data);
            BInvokableType invokableType = (BInvokableType) unaryExpr.opSymbol.type;
            ArrayList<BType> paramTypes = new ArrayList<>(1);
            paramTypes.add(unaryExpr.expr.getBType());
            invokableType.paramTypes = paramTypes;
            invokableType.retType = unaryExpr.getBType();
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
