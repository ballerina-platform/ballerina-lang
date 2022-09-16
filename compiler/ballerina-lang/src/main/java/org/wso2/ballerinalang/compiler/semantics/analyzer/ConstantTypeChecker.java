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
import org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.util.*;
import org.wso2.ballerinalang.util.Flags;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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

    public void visit(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        BType exprType;
        BType actualType = symTable.semanticError;
        if (OperatorKind.UNTAINT.equals(unaryExpr.operator)) {
            exprType = checkConstExpr(unaryExpr.expr, data);
            if (exprType != symTable.semanticError) {
                actualType = exprType;
            }
        } else if (OperatorKind.TYPEOF.equals(unaryExpr.operator)) {
            exprType = checkConstExpr(unaryExpr.expr, data);
            if (exprType != symTable.semanticError) {
                actualType = new BTypedescType(exprType, null);
            }
        } else {
            //allow both addition and subtraction operators to get expected type as Decimal
            boolean decimalAddNegate = data.expType.tag == TypeTags.DECIMAL &&
                    (OperatorKind.ADD.equals(unaryExpr.operator) || OperatorKind.SUB.equals(unaryExpr.operator));
            exprType = decimalAddNegate ? checkConstExpr(unaryExpr.expr, data.expType, data) :
                    checkConstExpr(unaryExpr.expr, data);
            if (exprType != symTable.semanticError) {
                BSymbol symbol = symResolver.resolveUnaryOperator(unaryExpr.operator, exprType);
                if (symbol == symTable.notFoundSymbol) {
                    symbol = symResolver.getUnaryOpsForTypeSets(unaryExpr.operator, exprType);
                }
                if (symbol == symTable.notFoundSymbol) {
                    dlog.error(unaryExpr.pos, DiagnosticErrorCode.UNARY_OP_INCOMPATIBLE_TYPES,
                            unaryExpr.operator, exprType);
                } else {
                    unaryExpr.opSymbol = (BOperatorSymbol) symbol;
                    actualType = symbol.type.getReturnType();
                }
            }
        }

        data.resultType = types.checkType(unaryExpr, actualType, data.expType);
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
            data.constExprValue = calculateConstValue(getCorrespondingFiniteType(lhsType, expType),
                    getCorrespondingFiniteType(rhsType, expType), binaryExpr.opKind, expType, binaryExpr.pos);
            List<Object> valueList = new ArrayList<>();
            valueList.add(data.constExprValue.value);
            while (iterator.hasNext()) {
                expType = iterator.next();
                valueList.add(calculateConstValue(getCorrespondingFiniteType(lhsType, expType), getCorrespondingFiniteType(rhsType, expType), binaryExpr.opKind, expType, binaryExpr.pos));
            }
            iterator = ((BUnionType) resultType).getMemberTypes().iterator();
            LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(valueList.size());
            for (int i = 0; i < valueList.size(); i++) {
                memberTypes.add(getFiniteType(valueList.get(i), constantSymbol, pos, iterator.next()));
            }
            data.resultType = BUnionType.create(null, memberTypes);
            return;
        }
        data.constExprValue = calculateConstValue(getCorrespondingFiniteType(lhsType, resultType),
                getCorrespondingFiniteType(rhsType, resultType), binaryExpr.opKind, resultType, binaryExpr.pos);
        data.resultType = getFiniteType(data.constExprValue, constantSymbol, pos, resultType);
    }

    private BLangConstantValue calculateConstValue(BFiniteType lhs, BFiniteType rhs, OperatorKind kind,
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
//                case BITWISE_AND:
//                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a & b, type, currentPos);
//                case BITWISE_OR:
//                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a | b, type, currentPos);
//                case BITWISE_LEFT_SHIFT:
//                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a << b, type, currentPos);
//                case BITWISE_RIGHT_SHIFT:
//                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a >> b, type, currentPos);
//                case BITWISE_UNSIGNED_RIGHT_SHIFT:
//                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a >>> b, type, currentPos);
//                case BITWISE_XOR:
//                    return calculateBitWiseOp(lhs, rhs, (a, b) -> a ^ b, type, currentPos);
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

    private BLangConstantValue calculateBitWiseOp(BLangConstantValue lhs, BLangConstantValue rhs,
                                                  BiFunction<Long, Long, Long> func, BType type, Location currentPos) {
        switch (Types.getReferredType(type).tag) {
            case TypeTags.INT:
                Long val = func.apply((Long) lhs.value, (Long) rhs.value);
                return new BLangConstantValue(val, type);
            default:
                dlog.error(currentPos, DiagnosticErrorCode.CONSTANT_EXPRESSION_NOT_SUPPORTED);

        }
        return new BLangConstantValue(null, type);
    }

    private BLangConstantValue calculateAddition(Object lhs, Object rhs, BType type) {
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
        return new BLangConstantValue(result, type);
    }

    private BLangConstantValue calculateSubtract(Object lhs, Object rhs, BType type) {
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
        return new BLangConstantValue(result, type);
    }

    private BLangConstantValue calculateMultiplication(Object lhs, Object rhs, BType type) {
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
        return new BLangConstantValue(result, type);
    }

    private BLangConstantValue calculateDivision(Object lhs, Object rhs, BType type) {
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
        return new BLangConstantValue(result, type);
    }


    private Object calculateNegationForInt(BLangConstantValue value) {
        return -1 * ((Long) (value.value));
    }

    private Object calculateNegationForFloat(BLangConstantValue value) {
        return String.valueOf(-1 * Double.parseDouble(String.valueOf(value.value)));
    }

    private Object calculateNegationForDecimal(BLangConstantValue value) {
        BigDecimal valDecimal = new BigDecimal(String.valueOf(value.value), MathContext.DECIMAL128);
        BigDecimal negDecimal = new BigDecimal(String.valueOf(-1), MathContext.DECIMAL128);
        BigDecimal resultDecimal = valDecimal.multiply(negDecimal, MathContext.DECIMAL128);
        return resultDecimal.toPlainString();
    }

    private BLangConstantValue calculateNegation(BLangConstantValue value) {
        Object result = null;
        BType constSymbolValType = value.type;
        int constSymbolValTypeTag = constSymbolValType.tag;

        switch (constSymbolValTypeTag) {
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

        return new BLangConstantValue(result, constSymbolValType);
    }

    private BLangConstantValue calculateBitWiseComplement(BLangConstantValue value, BType type) {
        Object result = null;
        if (Types.getReferredType(type).tag == TypeTags.INT) {
            result = ~((Long) (value.value));
        }
        return new BLangConstantValue(result, type);
    }

    private BLangConstantValue calculateBooleanComplement(BLangConstantValue value, BType type) {
        Object result = null;
        if (Types.getReferredType(type).tag == TypeTags.BOOLEAN) {
            result = !((Boolean) (value.value));
        }
        return new BLangConstantValue(result, type);
    }

//    public BType checkConstExpr(BLangExpression expr, SymbolEnv env, Stack<SymbolEnv> prevEnvs,
//                           Types.CommonAnalyzerData commonAnalyzerData) {
//        return checkConstExpr(expr, env, symTable.noType, prevEnvs, commonAnalyzerData);
//    }



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

    private BType checkMappingConstructorCompatibility(BType bType, BLangRecordLiteral mappingConstructor,
                                                      AnalyzerData data) {
        int tag = bType.tag;
        if (tag == TypeTags.UNION) {
            boolean prevNonErrorLoggingCheck = data.commonAnalyzerData.nonErrorLoggingCheck;
            data.commonAnalyzerData.nonErrorLoggingCheck = true;
            int errorCount = this.dlog.errorCount();
            this.dlog.mute();

            List<BType> compatibleTypes = new ArrayList<>();
            boolean erroredExpType = false;
            for (BType memberType : ((BUnionType) bType).getMemberTypes()) {
                if (memberType == symTable.semanticError) {
                    if (!erroredExpType) {
                        erroredExpType = true;
                    }
                    continue;
                }

                BType listCompatibleMemType = getMappingConstructorCompatibleNonUnionType(memberType, data);
                if (listCompatibleMemType == symTable.semanticError) {
                    continue;
                }

                dlog.resetErrorCount();
                BType memCompatibiltyType = checkMappingConstructorCompatibility(listCompatibleMemType,
                        mappingConstructor, data);

                if (memCompatibiltyType != symTable.semanticError && dlog.errorCount() == 0 &&
                        isUniqueType(compatibleTypes, memCompatibiltyType)) {
                    compatibleTypes.add(memCompatibiltyType);
                }
            }

            data.commonAnalyzerData.nonErrorLoggingCheck = prevNonErrorLoggingCheck;
            dlog.setErrorCount(errorCount);
            if (!prevNonErrorLoggingCheck) {
                this.dlog.unmute();
            }

            if (compatibleTypes.isEmpty()) {
                if (!erroredExpType) {
                    reportIncompatibleMappingConstructorError(mappingConstructor, bType, data);
                }
                validateSpecifiedFields(mappingConstructor, symTable.semanticError, data);
                return symTable.semanticError;
            } else if (compatibleTypes.size() != 1) {
                dlog.error(mappingConstructor.pos, DiagnosticErrorCode.AMBIGUOUS_TYPES, bType);
                validateSpecifiedFields(mappingConstructor, symTable.semanticError, data);
                return symTable.semanticError;
            }

            return checkMappingConstructorCompatibility(compatibleTypes.get(0), mappingConstructor, data);
        }

        if (tag == TypeTags.TYPEREFDESC) {
            BType refType = Types.getReferredType(bType);
            BType compatibleType = checkMappingConstructorCompatibility(refType, mappingConstructor, data);
            return compatibleType == refType ? bType : compatibleType;
        }

        if (tag == TypeTags.INTERSECTION) {
            return checkMappingConstructorCompatibility(((BIntersectionType) bType).effectiveType, mappingConstructor,
                    data);
        }

        BType possibleType = getMappingConstructorCompatibleNonUnionType(bType, data);

        switch (possibleType.tag) {
            case TypeTags.MAP:
                return validateSpecifiedFields(mappingConstructor, possibleType, data) ? possibleType :
                        symTable.semanticError;
            case TypeTags.RECORD:
                boolean isSpecifiedFieldsValid = validateSpecifiedFields(mappingConstructor, possibleType, data);

                boolean hasAllRequiredFields = validateRequiredFields((BRecordType) possibleType,
                        mappingConstructor.fields,
                        mappingConstructor.pos, data);

                return isSpecifiedFieldsValid && hasAllRequiredFields ? possibleType : symTable.semanticError;
            case TypeTags.READONLY:
                return checkReadOnlyMappingType(mappingConstructor, data);
        }
        reportIncompatibleMappingConstructorError(mappingConstructor, bType, data);
        validateSpecifiedFields(mappingConstructor, symTable.semanticError, data);
        return symTable.semanticError;
    }

    private boolean validateRequiredFields(BRecordType type, List<RecordLiteralNode.RecordField> specifiedFields,
                                           Location pos, AnalyzerData data) {
        HashSet<String> specFieldNames = getFieldNames(specifiedFields, data);
        boolean hasAllRequiredFields = true;

        for (BField field : type.fields.values()) {
            String fieldName = field.name.value;
            if (!specFieldNames.contains(fieldName) && Symbols.isFlagOn(field.symbol.flags, Flags.REQUIRED)
                    && !types.isNeverTypeOrStructureTypeWithARequiredNeverMember(field.type)) {
                // Check if `field` is explicitly assigned a value in the record literal
                // If a required field is missing, it's a compile error
                dlog.error(pos, DiagnosticErrorCode.MISSING_REQUIRED_RECORD_FIELD, field.name);
                if (hasAllRequiredFields) {
                    hasAllRequiredFields = false;
                }
            }
        }
        return hasAllRequiredFields;
    }

    private HashSet<String> getFieldNames(List<RecordLiteralNode.RecordField> specifiedFields, AnalyzerData data) {
        HashSet<String> fieldNames = new HashSet<>();

        for (RecordLiteralNode.RecordField specifiedField : specifiedFields) {
            if (specifiedField.isKeyValueField()) {
                String name = getKeyValueFieldName((BLangRecordLiteral.BLangRecordKeyValueField) specifiedField);
                if (name == null) {
                    continue; // computed key
                }

                fieldNames.add(name);
            } else if (specifiedField.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                fieldNames.add(getVarNameFieldName((BLangRecordLiteral.BLangRecordVarNameField) specifiedField));
            } else {
                fieldNames.addAll(getSpreadOpFieldRequiredFieldNames(
                        (BLangRecordLiteral.BLangRecordSpreadOperatorField) specifiedField, data));
            }
        }

        return fieldNames;
    }

    private List<String> getSpreadOpFieldRequiredFieldNames(BLangRecordLiteral.BLangRecordSpreadOperatorField field,
                                                            AnalyzerData data) {
        BType spreadType = Types.getReferredType(checkConstExpr(field.expr, data));

        if (spreadType.tag != TypeTags.RECORD) {
            return Collections.emptyList();
        }

        List<String> fieldNames = new ArrayList<>();
        for (BField bField : ((BRecordType) spreadType).getFields().values()) {
            if (!Symbols.isOptional(bField.symbol)) {
                fieldNames.add(bField.name.value);
            }
        }
        return fieldNames;
    }

    private String getVarNameFieldName(BLangRecordLiteral.BLangRecordVarNameField field) {
        return field.variableName.value;
    }

    private String getKeyValueFieldName(BLangRecordLiteral.BLangRecordKeyValueField field) {
        BLangRecordLiteral.BLangRecordKey key = field.key;
        if (key.computedKey) {
            return null;
        }

        BLangExpression keyExpr = key.expr;

        if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            return ((BLangSimpleVarRef) keyExpr).variableName.value;
        } else if (keyExpr.getKind() == NodeKind.LITERAL) {
            return (String) ((BLangLiteral) keyExpr).value;
        }
        return null;
    }

    public BType getEffectiveMappingType(BLangRecordLiteral recordLiteral, BType applicableMappingType,
                                         AnalyzerData data) {
        BType refType = Types.getReferredType(applicableMappingType);
        if (applicableMappingType == symTable.semanticError ||
                (refType.tag == TypeTags.RECORD && Symbols.isFlagOn(applicableMappingType.flags,
                        Flags.READONLY))) {
            return applicableMappingType;
        }

        Map<String, RecordLiteralNode.RecordField> readOnlyFields = new LinkedHashMap<>();
        LinkedHashMap<String, BField> applicableTypeFields =
                refType.tag == TypeTags.RECORD ? ((BRecordType) refType).fields :
                        new LinkedHashMap<>();

        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                continue;
            }

            String name;
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField = (BLangRecordLiteral.BLangRecordKeyValueField) field;

                if (!keyValueField.readonly) {
                    continue;
                }

                BLangExpression keyExpr = keyValueField.key.expr;
                if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                    name = ((BLangSimpleVarRef) keyExpr).variableName.value;
                } else {
                    name = (String) ((BLangLiteral) keyExpr).value;
                }
            } else {
                BLangRecordLiteral.BLangRecordVarNameField varNameField = (BLangRecordLiteral.BLangRecordVarNameField) field;

                if (!varNameField.readonly) {
                    continue;
                }
                name = varNameField.variableName.value;
            }

            if (applicableTypeFields.containsKey(name) &&
                    Symbols.isFlagOn(applicableTypeFields.get(name).symbol.flags, Flags.READONLY)) {
                continue;
            }

            readOnlyFields.put(name, field);
        }

        if (readOnlyFields.isEmpty()) {
            return applicableMappingType;
        }

        PackageID pkgID = data.env.enclPkg.symbol.pkgID;
        Location pos = recordLiteral.pos;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, pos, VIRTUAL, data);

        LinkedHashMap<String, BField> newFields = new LinkedHashMap<>();

        for (Map.Entry<String, RecordLiteralNode.RecordField> readOnlyEntry : readOnlyFields.entrySet()) {
            RecordLiteralNode.RecordField field = readOnlyEntry.getValue();

            String key = readOnlyEntry.getKey();
            Name fieldName = names.fromString(key);

            BType readOnlyFieldType;
            if (field.isKeyValueField()) {
                readOnlyFieldType = ((BLangRecordLiteral.BLangRecordKeyValueField) field).valueExpr.getBType();
            } else {
                // Has to be a varname field.
                readOnlyFieldType = ((BLangRecordLiteral.BLangRecordVarNameField) field).getBType();
            }

            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(new HashSet<Flag>() {{
                add(Flag.REQUIRED);
                add(Flag.READONLY);
            }}), fieldName, pkgID, readOnlyFieldType, recordSymbol,
                    ((BLangNode) field).pos, VIRTUAL);
            newFields.put(key, new BField(fieldName, null, fieldSymbol));
            recordSymbol.scope.define(fieldName, fieldSymbol);
        }

        BRecordType recordType = new BRecordType(recordSymbol, recordSymbol.flags);
        if (refType.tag == TypeTags.MAP) {
            recordType.sealed = false;
            recordType.restFieldType = ((BMapType) refType).constraint;
        } else {
            BRecordType applicableRecordType = (BRecordType) refType;
            boolean allReadOnlyFields = true;

            for (Map.Entry<String, BField> origEntry : applicableRecordType.fields.entrySet()) {
                String fieldName = origEntry.getKey();
                BField field = origEntry.getValue();

                if (readOnlyFields.containsKey(fieldName)) {
                    // Already defined.
                    continue;
                }

                BVarSymbol origFieldSymbol = field.symbol;
                long origFieldFlags = origFieldSymbol.flags;

                if (allReadOnlyFields && !Symbols.isFlagOn(origFieldFlags, Flags.READONLY)) {
                    allReadOnlyFields = false;
                }

                BVarSymbol fieldSymbol = new BVarSymbol(origFieldFlags, field.name, pkgID,
                        origFieldSymbol.type, recordSymbol, field.pos, VIRTUAL);
                newFields.put(fieldName, new BField(field.name, null, fieldSymbol));
                recordSymbol.scope.define(field.name, fieldSymbol);
            }

            recordType.sealed = applicableRecordType.sealed;
            recordType.restFieldType = applicableRecordType.restFieldType;

            if (recordType.sealed && allReadOnlyFields) {
                recordType.flags |= Flags.READONLY;
                recordType.tsymbol.flags |= Flags.READONLY;
            }

        }

        recordType.fields = newFields;
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(recordType, pkgID, symTable,
                pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, data.env,
                names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordType, recordSymbol, recordTypeNode, data.env);

        if (refType.tag == TypeTags.RECORD) {
            BRecordType applicableRecordType = (BRecordType) refType;
            BTypeSymbol applicableRecordTypeSymbol = applicableRecordType.tsymbol;
            BLangUserDefinedType origTypeRef = new BLangUserDefinedType(
                    ASTBuilderUtil.createIdentifier(
                            pos,
                            TypeDefBuilderHelper.getPackageAlias(data.env, pos.lineRange().filePath(),
                                    applicableRecordTypeSymbol.pkgID)),
                    ASTBuilderUtil.createIdentifier(pos, applicableRecordTypeSymbol.name.value));
            origTypeRef.pos = pos;
            origTypeRef.setBType(applicableRecordType);
            recordTypeNode.typeRefs.add(origTypeRef);
        } else if (refType.tag == TypeTags.MAP) {
            recordLiteral.expectedType = applicableMappingType;
        }

        return recordType;
    }

    private BType checkReadOnlyMappingType(BLangRecordLiteral mappingConstructor, AnalyzerData data) {
        if (!data.commonAnalyzerData.nonErrorLoggingCheck) {
            BType inferredType = defineInferredRecordType(mappingConstructor, symTable.readonlyType, data);

            if (inferredType == symTable.semanticError) {
                return symTable.semanticError;
            }
            return checkMappingConstructorCompatibility(inferredType, mappingConstructor, data);
        }

        for (RecordLiteralNode.RecordField field : mappingConstructor.fields) {
            BLangExpression exprToCheck;

            if (field.isKeyValueField()) {
                exprToCheck = ((BLangRecordLiteral.BLangRecordKeyValueField) field).valueExpr;
            } else if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                exprToCheck = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
            } else {
                exprToCheck = (BLangRecordLiteral.BLangRecordVarNameField) field;
            }

            if (exprIncompatible(symTable.readonlyType, exprToCheck, data)) {
                return symTable.semanticError;
            }
        }

        return symTable.readonlyType;
    }

    private boolean exprIncompatible(BType eType, BLangExpression expr, AnalyzerData data) {
        if (expr.typeChecked) {
            return expr.getBType() == symTable.semanticError;
        }

        BLangExpression exprToCheck = expr;

        if (data.commonAnalyzerData.nonErrorLoggingCheck) {
            expr.cloneAttempt++;
            exprToCheck = nodeCloner.cloneNode(expr);
        }

        return checkConstExpr(exprToCheck, eType, data) == symTable.semanticError;
    }

    public BType defineInferredRecordType(BLangRecordLiteral recordLiteral, BType expType, AnalyzerData data) {
        SymbolEnv env = data.env;
        PackageID pkgID = env.enclPkg.symbol.pkgID;
        BRecordTypeSymbol recordSymbol = createRecordTypeSymbol(pkgID, recordLiteral.pos, VIRTUAL, data);

        Map<String, TypeChecker.FieldInfo> nonRestFieldTypes = new LinkedHashMap<>();
        List<BType> restFieldTypes = new ArrayList<>();

        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValue = (BLangRecordLiteral.BLangRecordKeyValueField) field;
                BLangRecordLiteral.BLangRecordKey key = keyValue.key;
                BLangExpression expression = keyValue.valueExpr;
                BLangExpression keyExpr = key.expr;
                if (key.computedKey) {
                    checkConstExpr(keyExpr, symTable.stringType, data);
                    BType exprType = checkConstExpr(expression, expType, data);
                    if (isUniqueType(restFieldTypes, exprType)) {
                        restFieldTypes.add(exprType);
                    }
                } else {
                    addToNonRestFieldTypes(nonRestFieldTypes, getKeyName(keyExpr),
                            keyValue.readonly ? checkConstExpr(expression, symTable.readonlyType, data) :
                                    checkConstExpr(expression, expType, data),
                            true, keyValue.readonly);
                }
            } else if (field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP) {
                BType spreadOpType = checkConstExpr(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr,
                        expType, data);
                BType type = Types.getReferredType(spreadOpType);

                if (type.tag == TypeTags.MAP) {
                    BType constraintType = ((BMapType) type).constraint;

                    if (isUniqueType(restFieldTypes, constraintType)) {
                        restFieldTypes.add(constraintType);
                    }
                }

                if (type.tag != TypeTags.RECORD) {
                    continue;
                }

                BRecordType recordType = (BRecordType) type;
                for (BField recField : recordType.fields.values()) {
                    addToNonRestFieldTypes(nonRestFieldTypes, recField.name.value, recField.type,
                            !Symbols.isOptional(recField.symbol), false);
                }

                if (!recordType.sealed) {
                    BType restFieldType = recordType.restFieldType;
                    if (isUniqueType(restFieldTypes, restFieldType)) {
                        restFieldTypes.add(restFieldType);
                    }
                }
            } else {
                BLangRecordLiteral.BLangRecordVarNameField varNameField = (BLangRecordLiteral.BLangRecordVarNameField) field;
                addToNonRestFieldTypes(nonRestFieldTypes, getKeyName(varNameField), varNameField.readonly ?
                                checkConstExpr(varNameField, symTable.readonlyType, data) :
                                checkConstExpr(varNameField, expType, data),
                        true, varNameField.readonly);
            }
        }

        LinkedHashMap<String, BField> fields = new LinkedHashMap<>();
        boolean allReadOnlyNonRestFields = true;

        for (Map.Entry<String, TypeChecker.FieldInfo> entry : nonRestFieldTypes.entrySet()) {
            TypeChecker.FieldInfo fieldInfo = entry.getValue();
            List<BType> types = fieldInfo.types;

            if (types.contains(symTable.semanticError)) {
                return symTable.semanticError;
            }

            String key = entry.getKey();
            Name fieldName = names.fromString(key);
            BType type = types.size() == 1 ? types.get(0) : BUnionType.create(null, types.toArray(new BType[0]));

            Set<Flag> flags = new HashSet<>();

            if (fieldInfo.required) {
                flags.add(Flag.REQUIRED);
            } else {
                flags.add(Flag.OPTIONAL);
            }

            if (fieldInfo.readonly) {
                flags.add(Flag.READONLY);
            } else if (allReadOnlyNonRestFields) {
                allReadOnlyNonRestFields = false;
            }

            BVarSymbol fieldSymbol = new BVarSymbol(Flags.asMask(flags), fieldName, pkgID, type, recordSymbol,
                    symTable.builtinPos, VIRTUAL);
            fields.put(fieldName.value, new BField(fieldName, null, fieldSymbol));
            recordSymbol.scope.define(fieldName, fieldSymbol);
        }

        BRecordType recordType = new BRecordType(recordSymbol);
        recordType.fields = fields;

        if (restFieldTypes.contains(symTable.semanticError)) {
            return symTable.semanticError;
        }

        if (restFieldTypes.isEmpty()) {
            recordType.sealed = true;
            recordType.restFieldType = symTable.noType;
        } else if (restFieldTypes.size() == 1) {
            recordType.restFieldType = restFieldTypes.get(0);
        } else {
            recordType.restFieldType = BUnionType.create(null, restFieldTypes.toArray(new BType[0]));
        }
        recordSymbol.type = recordType;
        recordType.tsymbol = recordSymbol;

        if (expType == symTable.readonlyType || (recordType.sealed && allReadOnlyNonRestFields)) {
            recordType.flags |= Flags.READONLY;
            recordSymbol.flags |= Flags.READONLY;
        }

        BLangRecordTypeNode recordTypeNode = TypeDefBuilderHelper.createRecordTypeNode(recordType, pkgID, symTable,
                recordLiteral.pos);
        recordTypeNode.initFunction = TypeDefBuilderHelper.createInitFunctionForRecordType(recordTypeNode, env,
                names, symTable);
        TypeDefBuilderHelper.createTypeDefinitionForTSymbol(recordType, recordSymbol, recordTypeNode, env);

        return recordType;
    }

    private String getKeyName(BLangExpression key) {
        return key.getKind() == NodeKind.SIMPLE_VARIABLE_REF ?
                ((BLangSimpleVarRef) key).variableName.value : (String) ((BLangLiteral) key).value;
    }

    private void addToNonRestFieldTypes(Map<String, TypeChecker.FieldInfo> nonRestFieldTypes, String keyString,
                                        BType exprType, boolean required, boolean readonly) {
        if (!nonRestFieldTypes.containsKey(keyString)) {
            nonRestFieldTypes.put(keyString, new TypeChecker.FieldInfo(new ArrayList<BType>() {{ add(exprType); }}, required,
                    readonly));
            return;
        }

        TypeChecker.FieldInfo fieldInfo = nonRestFieldTypes.get(keyString);
        List<BType> typeList = fieldInfo.types;

        if (isUniqueType(typeList, exprType)) {
            typeList.add(exprType);
        }

        if (required && !fieldInfo.required) {
            fieldInfo.required = true;
        }
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

    private boolean validateSpecifiedFields(BLangRecordLiteral mappingConstructor, BType possibleType,
                                            AnalyzerData data) {
        boolean isFieldsValid = true;

        for (RecordLiteralNode.RecordField field : mappingConstructor.fields) {
            BType checkedType = checkMappingField(field, Types.getReferredType(possibleType), data);
            if (isFieldsValid && checkedType == symTable.semanticError) {
                isFieldsValid = false;
            }
        }

        return isFieldsValid;
    }

    private BType checkMappingField(RecordLiteralNode.RecordField field, BType mappingType, AnalyzerData data) {
        BType fieldType = symTable.semanticError;
        boolean keyValueField = field.isKeyValueField();
        boolean spreadOpField = field.getKind() == NodeKind.RECORD_LITERAL_SPREAD_OP;

        boolean readOnlyConstructorField = false;
        String fieldName = null;
        Location pos = null;

        BLangExpression valueExpr = null;

        if (keyValueField) {
            valueExpr = ((BLangRecordLiteral.BLangRecordKeyValueField) field).valueExpr;
        } else if (!spreadOpField) {
            valueExpr = (BLangRecordLiteral.BLangRecordVarNameField) field;
        }

        switch (mappingType.tag) {
            case TypeTags.RECORD:
                if (keyValueField) {
                    BLangRecordLiteral.BLangRecordKeyValueField keyValField = (BLangRecordLiteral.BLangRecordKeyValueField) field;
                    BLangRecordLiteral.BLangRecordKey key = keyValField.key;
                    TypeChecker.TypeSymbolPair typeSymbolPair = checkRecordLiteralKeyExpr(key.expr, key.computedKey,
                            (BRecordType) mappingType, data);
                    fieldType = typeSymbolPair.determinedType;
                    key.fieldSymbol = typeSymbolPair.fieldSymbol;
                    readOnlyConstructorField = keyValField.readonly;
                    pos = key.expr.pos;
                    fieldName = getKeyValueFieldName(keyValField);
                } else if (spreadOpField) {
                    BLangExpression spreadExpr = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                    checkConstExpr(spreadExpr, data);

                    BType spreadExprType = Types.getReferredType(spreadExpr.getBType());
                    if (spreadExprType.tag == TypeTags.MAP) {
                        return types.checkType(spreadExpr.pos, ((BMapType) spreadExprType).constraint,
                                getAllFieldType((BRecordType) mappingType),
                                DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                    }

                    if (spreadExprType.tag != TypeTags.RECORD) {
                        dlog.error(spreadExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_SPREAD_OP,
                                spreadExprType);
                        return symTable.semanticError;
                    }

                    boolean errored = false;
                    for (BField bField : ((BRecordType) spreadExprType).fields.values()) {
                        BType specFieldType = bField.type;
                        BSymbol fieldSymbol = symResolver.resolveStructField(spreadExpr.pos, data.env, bField.name,
                                mappingType.tsymbol);
                        BType expectedFieldType = checkRecordLiteralKeyByName(spreadExpr.pos, fieldSymbol, bField.name,
                                (BRecordType) mappingType);
                        if (expectedFieldType != symTable.semanticError &&
                                !types.isAssignable(specFieldType, expectedFieldType)) {
                            dlog.error(spreadExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_FIELD,
                                    expectedFieldType, bField.name, specFieldType);
                            if (!errored) {
                                errored = true;
                            }
                        }
                    }
                    return errored ? symTable.semanticError : symTable.noType;
                } else {
                    BLangRecordLiteral.BLangRecordVarNameField varNameField = (BLangRecordLiteral.BLangRecordVarNameField) field;
                    TypeChecker.TypeSymbolPair typeSymbolPair = checkRecordLiteralKeyExpr(varNameField, false,
                            (BRecordType) mappingType, data);
                    fieldType = typeSymbolPair.determinedType;
                    readOnlyConstructorField = varNameField.readonly;
                    pos = varNameField.pos;
                    fieldName = getVarNameFieldName(varNameField);
                }
                break;
            case TypeTags.MAP:
                if (spreadOpField) {
                    BLangExpression spreadExp = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
                    BType spreadOpType = checkConstExpr(spreadExp, data);
                    BType spreadOpMemberType = checkSpreadFieldWithMapType(spreadOpType);
                    if (spreadOpMemberType.tag == symTable.semanticError.tag) {
                        dlog.error(spreadExp.pos, DiagnosticErrorCode.INCOMPATIBLE_TYPES_SPREAD_OP,
                                spreadOpType);
                        return symTable.semanticError;
                    }

                    return types.checkType(spreadExp.pos, spreadOpMemberType, ((BMapType) mappingType).constraint,
                            DiagnosticErrorCode.INCOMPATIBLE_TYPES);
                }

                boolean validMapKey;
                if (keyValueField) {
                    BLangRecordLiteral.BLangRecordKeyValueField keyValField = (BLangRecordLiteral.BLangRecordKeyValueField) field;
                    BLangRecordLiteral.BLangRecordKey key = keyValField.key;
                    validMapKey = checkValidJsonOrMapLiteralKeyExpr(key.expr, key.computedKey, data);
                    readOnlyConstructorField = keyValField.readonly;
                    pos = key.pos;
                    fieldName = getKeyValueFieldName(keyValField);
                } else {
                    BLangRecordLiteral.BLangRecordVarNameField varNameField = (BLangRecordLiteral.BLangRecordVarNameField) field;
                    validMapKey = checkValidJsonOrMapLiteralKeyExpr(varNameField, false, data);
                    readOnlyConstructorField = varNameField.readonly;
                    pos = varNameField.pos;
                    fieldName = getVarNameFieldName(varNameField);
                }

                fieldType = validMapKey ? ((BMapType) mappingType).constraint : symTable.semanticError;
                break;
        }


        if (readOnlyConstructorField) {
            if (types.isSelectivelyImmutableType(fieldType, data.env.enclPkg.packageID)) {
                fieldType =
                        ImmutableTypeCloner.getImmutableIntersectionType(pos, types, fieldType, data.env, symTable,
                                anonymousModelHelper, names, new HashSet<>());
            } else if (!types.isInherentlyImmutableType(fieldType)) {
                dlog.error(pos, DiagnosticErrorCode.INVALID_READONLY_MAPPING_FIELD, fieldName, fieldType);
                fieldType = symTable.semanticError;
            }
        }

        if (spreadOpField) {
            // If we reach this point for a spread operator it is due to the mapping type being a semantic error.
            // In such a scenario, valueExpr would be null here, and fieldType would be symTable.semanticError.
            // We set the spread op expression as the valueExpr here, to check it against symTable.semanticError.
            valueExpr = ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr;
        }

        BLangExpression exprToCheck = valueExpr;
        if (data.commonAnalyzerData.nonErrorLoggingCheck) {
            exprToCheck = nodeCloner.cloneNode(valueExpr);
        } else {
            ((BLangNode) field).setBType(fieldType);
        }

        return checkConstExpr(exprToCheck, data.env, fieldType, data);
    }

    private BType checkSpreadFieldWithMapType(BType spreadOpType) {
        switch (spreadOpType.tag) {
            case TypeTags.RECORD:
                List<BType> types = new ArrayList<>();
                BRecordType recordType = (BRecordType) spreadOpType;

                for (BField recField : recordType.fields.values()) {
                    types.add(recField.type);
                }

                if (!recordType.sealed) {
                    types.add(recordType.restFieldType);
                }

                return getRepresentativeBroadType(types);
            case TypeTags.MAP:
                return ((BMapType) spreadOpType).constraint;
            case TypeTags.TYPEREFDESC:
                return checkSpreadFieldWithMapType(Types.getReferredType(spreadOpType));
            case TypeTags.INTERSECTION:
                return checkSpreadFieldWithMapType(((BIntersectionType) spreadOpType).effectiveType);
            default:
                return symTable.semanticError;
        }
    }

    private BType getRepresentativeBroadType(List<BType> inferredTypeList) {
        for (int i = 0; i < inferredTypeList.size(); i++) {
            BType type = inferredTypeList.get(i);
            if (type.tag == TypeTags.SEMANTIC_ERROR) {
                return type;
            }

            for (int j = i + 1; j < inferredTypeList.size(); j++) {
                BType otherType = inferredTypeList.get(j);

                if (otherType.tag == TypeTags.SEMANTIC_ERROR) {
                    return otherType;
                }

                if (types.isAssignable(otherType, type)) {
                    inferredTypeList.remove(j);
                    j -= 1;
                    continue;
                }

                if (types.isAssignable(type, otherType)) {
                    inferredTypeList.remove(i);
                    i -= 1;
                    break;
                }
            }
        }

        if (inferredTypeList.size() == 1) {
            return inferredTypeList.get(0);
        }

        return BUnionType.create(null, inferredTypeList.toArray(new BType[0]));
    }

    private boolean checkValidJsonOrMapLiteralKeyExpr(BLangExpression keyExpr, boolean computedKey, AnalyzerData data) {
        if (computedKey) {
            checkConstExpr(keyExpr, symTable.stringType, data);

            if (keyExpr.getBType() == symTable.semanticError) {
                return false;
            }
            return true;
        } else if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF ||
                (keyExpr.getKind() == NodeKind.LITERAL && ((BLangLiteral) keyExpr).getBType().tag == TypeTags.STRING)) {
            return true;
        }
        dlog.error(keyExpr.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL_KEY);
        return false;
    }

    private BType getAllFieldType(BRecordType recordType) {
        LinkedHashSet<BType> possibleTypes = new LinkedHashSet<>();

        for (BField field : recordType.fields.values()) {
            possibleTypes.add(field.type);
        }

        BType restFieldType = recordType.restFieldType;

        if (restFieldType != null && restFieldType != symTable.noType) {
            possibleTypes.add(restFieldType);
        }

        return BUnionType.create(null, possibleTypes);
    }

    private TypeChecker.TypeSymbolPair checkRecordLiteralKeyExpr(BLangExpression keyExpr, boolean computedKey,
                                                                 BRecordType recordType, AnalyzerData data) {
        Name fieldName;

        if (computedKey) {
            checkConstExpr(keyExpr, symTable.stringType, data);

            if (keyExpr.getBType() == symTable.semanticError) {
                return new TypeChecker.TypeSymbolPair(null, symTable.semanticError);
            }

            LinkedHashSet<BType> fieldTypes = recordType.fields.values().stream()
                    .map(field -> field.type)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            if (recordType.restFieldType.tag != TypeTags.NONE) {
                fieldTypes.add(recordType.restFieldType);
            }

            return new TypeChecker.TypeSymbolPair(null, BUnionType.create(null, fieldTypes));
        } else if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
            fieldName = names.fromIdNode(varRef.variableName);
        } else if (keyExpr.getKind() == NodeKind.LITERAL && keyExpr.getBType().tag == TypeTags.STRING) {
            fieldName = names.fromString((String) ((BLangLiteral) keyExpr).value);
        } else {
            dlog.error(keyExpr.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL_KEY);
            return new TypeChecker.TypeSymbolPair(null, symTable.semanticError);
        }

        // Check whether the struct field exists
        BSymbol fieldSymbol = symResolver.resolveStructField(keyExpr.pos, data.env, fieldName, recordType.tsymbol);
        BType type = checkRecordLiteralKeyByName(keyExpr.pos, fieldSymbol, fieldName, recordType);

        return new TypeChecker.TypeSymbolPair(fieldSymbol instanceof BVarSymbol ? (BVarSymbol) fieldSymbol : null, type);
    }

    private BType checkRecordLiteralKeyByName(Location location, BSymbol fieldSymbol, Name key,
                                              BRecordType recordType) {
        if (fieldSymbol != symTable.notFoundSymbol) {
            return fieldSymbol.type;
        }

        if (recordType.sealed) {
            dlog.error(location, DiagnosticErrorCode.UNDEFINED_STRUCTURE_FIELD_WITH_TYPE, key,
                    recordType.tsymbol.type.getKind().typeName(), recordType);
            return symTable.semanticError;
        }

        return recordType.restFieldType;
    }

    private void reportIncompatibleMappingConstructorError(BLangRecordLiteral mappingConstructorExpr, BType expType,
                                                           AnalyzerData data) {
        if (expType == symTable.semanticError) {
            return;
        }

        if (expType.tag != TypeTags.UNION) {
            dlog.error(mappingConstructorExpr.pos,
                    DiagnosticErrorCode.MAPPING_CONSTRUCTOR_COMPATIBLE_TYPE_NOT_FOUND, expType);
            return;
        }

        BUnionType unionType = (BUnionType) expType;
        BType[] memberTypes = types.getAllTypes(unionType, true).toArray(new BType[0]);

        // Special case handling for `T?` where T is a record type. This is done to give more user friendly error
        // messages for this common scenario.
        if (memberTypes.length == 2) {
            BRecordType recType = null;

            if (memberTypes[0].tag == TypeTags.RECORD && memberTypes[1].tag == TypeTags.NIL) {
                recType = (BRecordType) memberTypes[0];
            } else if (memberTypes[1].tag == TypeTags.RECORD && memberTypes[0].tag == TypeTags.NIL) {
                recType = (BRecordType) memberTypes[1];
            }

            if (recType != null) {
                validateSpecifiedFields(mappingConstructorExpr, recType, data);
                validateRequiredFields(recType, mappingConstructorExpr.fields, mappingConstructorExpr.pos, data);
                return;
            }
        }

        // By this point, we know there aren't any types to which we can assign the mapping constructor. If this is
        // case where there is at least one type with which we can use mapping constructors, but this particular
        // mapping constructor is incompatible, we give an incompatible mapping constructor error.
        for (BType bType : memberTypes) {
            if (isMappingConstructorCompatibleType(bType)) {
                dlog.error(mappingConstructorExpr.pos, DiagnosticErrorCode.INCOMPATIBLE_MAPPING_CONSTRUCTOR,
                        unionType);
                return;
            }
        }

        dlog.error(mappingConstructorExpr.pos,
                DiagnosticErrorCode.MAPPING_CONSTRUCTOR_COMPATIBLE_TYPE_NOT_FOUND, unionType);
    }

    private boolean isMappingConstructorCompatibleType(BType type) {
        return Types.getReferredType(type).tag == TypeTags.RECORD
                || Types.getReferredType(type).tag == TypeTags.MAP;
    }

    private boolean isUniqueType(Iterable<BType> typeList, BType type) {
        boolean isRecord = type.tag == TypeTags.RECORD;

        for (BType bType : typeList) {

            if (isRecord) {
                if (type == bType) {
                    return false;
                }
            } else if (types.isSameType(type, bType)) {
                return false;
            }
        }
        return true;
    }

    private BType getMappingConstructorCompatibleNonUnionType(BType type, AnalyzerData data) {
        switch (type.tag) {
            case TypeTags.MAP:
            case TypeTags.RECORD:
            case TypeTags.READONLY:
                return type;
            case TypeTags.JSON:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.mapJsonType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.mapJsonType, data.env,
                                symTable, anonymousModelHelper, names);
            case TypeTags.ANYDATA:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.mapAnydataType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.mapAnydataType,
                                data.env, symTable, anonymousModelHelper, names);
            case TypeTags.ANY:
                return !Symbols.isFlagOn(type.flags, Flags.READONLY) ? symTable.mapAllType :
                        ImmutableTypeCloner.getEffectiveImmutableType(null, types, symTable.mapAllType, data.env,
                                symTable, anonymousModelHelper, names);
            case TypeTags.INTERSECTION:
                return ((BIntersectionType) type).effectiveType;
            case TypeTags.TYPEREFDESC:
                return getMappingConstructorCompatibleNonUnionType(((BTypeReferenceType) type).referredType, data);
        }
        return symTable.semanticError;
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
        BLangConstantValue constExprValue;
        BConstantSymbol constantSymbol;
        boolean isValidating = false;
    }
}
