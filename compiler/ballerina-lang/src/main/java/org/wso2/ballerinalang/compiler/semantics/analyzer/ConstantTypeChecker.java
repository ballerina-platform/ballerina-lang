package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ConstantTypeChecker {
    private static final CompilerContext.Key<ConstantTypeChecker> CONSTANT_TYPE_CHECKER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final ConstantValueResolver constResolver;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private BLangAnonymousModelHelper anonymousModelHelper;
    private BLangMissingNodesHelper missingNodesHelper;

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
        this.constResolver = ConstantValueResolver.getInstance(context);
    }

    public static ConstantTypeChecker getInstance(CompilerContext context) {
        ConstantTypeChecker constTypeChecker = context.get(CONSTANT_TYPE_CHECKER_KEY);
        if (constTypeChecker == null) {
            constTypeChecker = new ConstantTypeChecker(context);
        }

        return constTypeChecker;
    }

    public BType checkConstExpr(BLangExpression expr, ConstantTypeChecker.AnalyzerData data) {

    }

    public BType checkConstExpr(BLangExpression expr, BType expType, ConstantTypeChecker.AnalyzerData data) {

    }

    public BType checkConstExpr(BLangExpression expr, SymbolEnv env, BType expType, DiagnosticCode diagCode,
                           TypeChecker.AnalyzerData data) {
        if (expr.typeChecked) {
            return expr.getBType();
        }

        if (expType.tag == TypeTags.INTERSECTION) {
            expType = ((BIntersectionType) expType).effectiveType;
        }

        SymbolEnv prevEnv = data.env;
        BType preExpType = data.expType;
        DiagnosticCode preDiagCode = data.diagCode;
        data.env = env;
        data.diagCode = diagCode;
        data.expType = expType;
        data.isTypeChecked = true;

        BType referredExpType = Types.getReferredType(expType);
        if (referredExpType.tag == TypeTags.INTERSECTION) {
            expType = ((BIntersectionType) referredExpType).effectiveType;
        }
        expr.expectedType = expType;

        expr.accept(this, data);

        BType resultRefType = Types.getReferredType(data.resultType);
        if (resultRefType.tag == TypeTags.INTERSECTION) {
            data.resultType = ((BIntersectionType) resultRefType).effectiveType;
        }

        expr.setTypeCheckedType(data.resultType);
        expr.typeChecked = data.isTypeChecked;
        data.env = prevEnv;
        data.expType = preExpType;
        data.diagCode = preDiagCode;

//        validateAndSetExprExpectedType(expr, data);

        return data.resultType;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr, ConstantTypeChecker.AnalyzerData data) {
        // Bitwise operator should be applied for the future types in the wait expression
        if (data.expType.tag == TypeTags.FUTURE && binaryExpr.opKind == OperatorKind.BITWISE_OR) {
            BType lhsResultType = checkConstExpr(binaryExpr.lhsExpr, data.expType, data);
            BType rhsResultType = checkConstExpr(binaryExpr.rhsExpr, data.expType, data);
            // Return if both or atleast one of lhs and rhs types are errors
            if (lhsResultType == symTable.semanticError || rhsResultType == symTable.semanticError) {
                data.resultType = symTable.semanticError;
                return;
            }
            data.resultType = BUnionType.create(null, lhsResultType, rhsResultType);
            return;
        }

        SymbolEnv rhsExprEnv;
        BType lhsType;
        BType referredExpType = Types.getReferredType(binaryExpr.expectedType);
        if (referredExpType.tag == TypeTags.FLOAT || referredExpType.tag == TypeTags.DECIMAL ||
                isOptionalFloatOrDecimal(referredExpType)) {
            lhsType = checkAndGetType(binaryExpr.lhsExpr, data.env, binaryExpr, data);
        } else {
            lhsType = checkExpr(binaryExpr.lhsExpr, data);
        }

        if (binaryExpr.opKind == OperatorKind.AND) {
            rhsExprEnv = typeNarrower.evaluateTruth(binaryExpr.lhsExpr, binaryExpr.rhsExpr, data.env, true);
        } else if (binaryExpr.opKind == OperatorKind.OR) {
            rhsExprEnv = typeNarrower.evaluateFalsity(binaryExpr.lhsExpr, binaryExpr.rhsExpr, data.env, true);
        } else {
            rhsExprEnv = data.env;
        }

        BType rhsType;

        if (referredExpType.tag == TypeTags.FLOAT || referredExpType.tag == TypeTags.DECIMAL ||
                isOptionalFloatOrDecimal(referredExpType)) {
            rhsType = checkAndGetType(binaryExpr.rhsExpr, rhsExprEnv, binaryExpr, data);
        } else {
            rhsType = checkExpr(binaryExpr.rhsExpr, rhsExprEnv, data);
        }

        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

        //noinspection SwitchStatementWithTooFewBranches
        switch (binaryExpr.opKind) {
            // Do not lookup operator symbol for xml sequence additions
            case ADD:
                BType leftConstituent = getXMLConstituents(lhsType);
                BType rightConstituent = getXMLConstituents(rhsType);

                if (leftConstituent != null && rightConstituent != null) {
                    actualType = new BXMLType(BUnionType.create(null, leftConstituent, rightConstituent), null);
                    break;
                }
                // Fall through
            default:
                if (lhsType != symTable.semanticError && rhsType != symTable.semanticError) {
                    // Look up operator symbol if both rhs and lhs types aren't error or xml types
                    BSymbol opSymbol = symResolver.resolveBinaryOperator(binaryExpr.opKind, lhsType, rhsType);

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
                    } else {
                        binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
                        actualType = opSymbol.type.getReturnType();
                    }
                }
        }

        data.resultType = types.checkType(binaryExpr, actualType, data.expType);
    }

    public static class AnalyzerData {
        public SymbolEnv env;
        boolean isTypeChecked;
        Stack<SymbolEnv> prevEnvs;
        Types.CommonAnalyzerData commonAnalyzerData = new Types.CommonAnalyzerData();
        DiagnosticCode diagCode;
        BType expType;
        BType resultType;
    }
}
