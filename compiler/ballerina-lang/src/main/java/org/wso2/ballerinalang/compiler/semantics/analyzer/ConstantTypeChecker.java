package org.wso2.ballerinalang.compiler.semantics.analyzer;

import io.ballerina.tools.diagnostics.DiagnosticCode;
import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.parser.BLangMissingNodesHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.*;
import org.wso2.ballerinalang.compiler.semantics.model.types.*;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.expressions.*;
import org.wso2.ballerinalang.compiler.util.*;

import java.util.*;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

public class ConstantTypeChecker extends SimpleBLangNodeAnalyzer<ConstantTypeChecker.AnalyzerData> {
    private static final CompilerContext.Key<ConstantTypeChecker> CONSTANT_TYPE_CHECKER_KEY = new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final Names names;
    private final SymbolResolver symResolver;
    private final SymbolEnter symEnter;
    private final BLangDiagnosticLog dlog;
    private final Types types;
    private final ConstantValueResolver constResolver;
    private final TypeParamAnalyzer typeParamAnalyzer;
    private final TypeChecker typeChecker;
    private final TypeResolver typeResolver;
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
        this.typeChecker = TypeChecker.getInstance(context);
        this.typeResolver = TypeResolver.getInstance(context);
    }

    public static ConstantTypeChecker getInstance(CompilerContext context) {
        ConstantTypeChecker constTypeChecker = context.get(CONSTANT_TYPE_CHECKER_KEY);
        if (constTypeChecker == null) {
            constTypeChecker = new ConstantTypeChecker(context);
        }

        return constTypeChecker;
    }

    public BType checkConstExpr(BLangExpression expr, AnalyzerData data) {
        return checkConstExpr(expr, data.env, symTable.noType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
    }

    public BType checkConstExpr(BLangExpression expr, BType expType, AnalyzerData data) {
        return checkConstExpr(expr, data.env, expType, DiagnosticErrorCode.INCOMPATIBLE_TYPES, data);
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

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {

    }

    @Override
    public void visit(BLangPackage node, AnalyzerData data) {

    }

//
//    private Name getCurrentCompUnit(BLangNode node) {
//        return names.fromString(node.pos.lineRange().filePath());
//    }

    @Override
    public void visit(BLangLiteral literalExpr, AnalyzerData data) {

        BType literalType = typeChecker.setLiteralValueAndGetType(literalExpr, data.expType, data);
        if (literalType == symTable.semanticError || literalExpr.isFiniteContext) {
            return;
        }
        data.resultType = types.checkType(literalExpr, literalType, data.expType);
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

    private BSymbol getSymbolOfVarRef(Location pos, SymbolEnv env, Name pkgAlias, Name varName, AnalyzerData data) {
        if (pkgAlias == Names.EMPTY && data.modTable.containsKey(varName)) {
            BLangNode node = data.modTable.get(varName);
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

    public void visit(BLangRecordLiteral recordLiteral, AnalyzerData data) {
        BType expType = data.expType;
        int expTypeTag = Types.getReferredType(expType).tag;

        if (expTypeTag == TypeTags.NONE || expTypeTag == TypeTags.READONLY) {
            expType = typeChecker.defineInferredRecordType(recordLiteral, expType, data);
        } else if (expTypeTag == TypeTags.OBJECT) {
            dlog.error(recordLiteral.pos, DiagnosticErrorCode.INVALID_RECORD_LITERAL, expType);
            data.resultType = symTable.semanticError;
            return;
        }

        data.resultType = typeChecker.getEffectiveMappingType(recordLiteral,
                typeChecker.checkMappingConstructorCompatibility(expType, recordLiteral, data), data);
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

    //
    @Override
    public void visit(BLangBinaryExpr binaryExpr, ConstantTypeChecker.AnalyzerData data) {
        BType lhsType = checkConstExpr(binaryExpr.lhsExpr, data);
        BType rhsType = checkConstExpr(binaryExpr.rhsExpr, data);

        // Set error type as the actual type.
        BType actualType = symTable.semanticError;

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
        data.resultType = types.checkType(binaryExpr, actualType, data.expType);
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
        boolean hasInferredType;
    }
}
