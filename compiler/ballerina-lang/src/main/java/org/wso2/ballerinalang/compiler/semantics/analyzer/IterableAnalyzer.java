/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableContext;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.Operation;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIterableTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.Lists;

import java.util.Collections;
import java.util.List;

/**
 * {@code {@link IterableAnalyzer}} validates iterable collection related semantics.
 *
 * @since 0.961.0
 */
public class IterableAnalyzer {

    private static final CompilerContext.Key<IterableAnalyzer> ITERABLE_ANALYZER_KEY =
            new CompilerContext.Key<>();

    private SymbolTable symTable;
    private Types types;
    private TypeChecker typeChecker;
    private BLangDiagnosticLog dlog;

    private final BIterableTypeVisitor lambdaTypeChecker, terminalInputTypeChecker, terminalOutputTypeChecker;

    private IterableAnalyzer(CompilerContext context) {
        context.put(ITERABLE_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);

        this.lambdaTypeChecker = new LambdaBasedTypeChecker(dlog, symTable, types);
        this.terminalInputTypeChecker = new TerminalInputTypeChecker(dlog, symTable, types);
        this.terminalOutputTypeChecker = new TerminalOutputTypeChecker(dlog, symTable, types);
    }

    public static IterableAnalyzer getInstance(CompilerContext context) {
        IterableAnalyzer iterableAnalyzer = context.get(ITERABLE_ANALYZER_KEY);
        if (iterableAnalyzer == null) {
            iterableAnalyzer = new IterableAnalyzer(context);
        }
        return iterableAnalyzer;
    }

    public void handlerIterableOperation(BLangInvocation iExpr, BType expectedType, SymbolEnv env) {
        final IterableContext context;
        if (iExpr.expr.type.tag != TypeTags.INTERMEDIATE_COLLECTION) {
            context = new IterableContext(iExpr.expr, env);              // This is a new iteration chain.
        } else {
            context = ((BLangInvocation) iExpr.expr).iContext;      // Get context from previous invocation.
        }
        iExpr.iContext = context;

        final IterableKind iterableKind = IterableKind.getFromString(iExpr.name.value);
        final Operation iOperation = new Operation(iterableKind, iExpr, expectedType);
        iExpr.iContext.addOperation(iOperation);

        if (iterableKind.isLambdaRequired()) {
            handleLambdaBasedIterableOperation(context, iOperation);
        } else {
            handleSimpleTerminalOperations(iOperation);
        }
        validateIterableContext(context);
        if (iOperation.resultType != symTable.semanticError && context.foreachTypes.isEmpty()) {
            calculateForeachTypes(context);
        }
    }

    private void calculateForeachTypes(IterableContext context) {
        context.foreachTypes = context.collectionExpr.type.accept(lambdaTypeChecker, context.getFirstOperation());
    }

    private void handleSimpleTerminalOperations(Operation op) {
        if (op.iExpr.argExprs.size() > 0) {
            dlog.error(op.pos, DiagnosticCode.ITERABLE_NO_ARGS_REQUIRED, op.kind);
        }
        op.arity = 1;
        op.iExpr.requiredArgs = op.iExpr.argExprs;
        op.inputType = op.collectionType.accept(terminalInputTypeChecker, op).get(0);
        op.resultType = op.outputType = op.collectionType.accept(terminalOutputTypeChecker, op).get(0);
    }

    private void handleLambdaBasedIterableOperation(IterableContext context, Operation operation) {
        // Require at least one param.
        if (operation.iExpr.argExprs.size() != 1) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_LAMBDA_REQUIRED);
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        }

        BLangExpression bLangExpression = operation.iExpr.argExprs.get(0);
        BType expType = symTable.noType;
        if (bLangExpression.getKind() == NodeKind.ARROW_EXPR) {
            if (operation.kind == IterableKind.FOREACH) {
                dlog.error(operation.pos, DiagnosticCode.ARROW_EXPRESSION_NOT_SUPPORTED_ITERABLE_OPERATION,
                        IterableKind.FOREACH.getKind());
                operation.outputType = operation.resultType = symTable.semanticError;
                return;
            }

            operation.arity = inferExpectedArity(operation);
            List<BType> paramTypes = calculateProvidedElementTypes(operation);
            BType inputParam = paramTypes.size() > 1 ? new BTupleType(paramTypes) : paramTypes.get(0);
            expType = new BInvokableType(Collections.singletonList(inputParam), symTable.noType, null);
        }

        // Given param should be an invokable type (lambda type).
        final BType operationParams = typeChecker.checkExpr(bLangExpression, context.env, expType);
        if (operationParams == null || operationParams.tag != TypeTags.INVOKABLE) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_LAMBDA_REQUIRED);
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        }

        if (operation.kind == IterableKind.SELECT && operation.collectionType.tag != TypeTags.TABLE) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_OPERATION, IterableKind.SELECT.getKind());
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        }

        // Operation's inputType and OutputType is defined by this lambda function.
        operation.iExpr.requiredArgs = operation.iExpr.argExprs;
        operation.lambdaType = (BInvokableType) operationParams;

        // Process given/expected input and output types.
        if (operation.lambdaType.getParameterTypes().isEmpty() ||
                operation.lambdaType.getParameterTypes().size() > 1) {
            // Lambda should have a single arg.
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_LAMBDA_TUPLE_REQUIRED);
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        }

        // Param types of the user-specified lambda function.
        final List<BType> paramTypes = calculateParamTypesOfLambda(operation);
        final List<BType> givenRetTypes = calculatedGivenOutputArgs(operation);

        // Type of the elements in the collection.
        final List<BType> elementTypes = calculateProvidedElementTypes(operation);
        final List<BType> actualRetTypes = calculateExpectedOutputArgs(operation, givenRetTypes);

        operation.inputType = operation.lambdaType.getParameterTypes().get(0);

        // Cross Validate given and expected types and calculate output type;
        validateLambdaInputArgs(operation, elementTypes, paramTypes);
        validateLambdaReturnArgs(operation, actualRetTypes, givenRetTypes);
        if (operation.outputType == symTable.semanticError) {
            operation.resultType = symTable.semanticError;
            return;
        }
        // Assign actual output value.
        assignOutputAndResultType(operation, elementTypes, actualRetTypes);
    }

    /*
     * Calculates the parameter types expected by the user specified lambda function for the operation.
     */
    private List<BType> calculateParamTypesOfLambda(Operation operation) {
        final BType inputParam = operation.lambdaType.getParameterTypes().get(0);
        final List<BType> givenArgTypes;
        if (inputParam.tag == TypeTags.TUPLE) {
            final BTupleType bTupleType = (BTupleType) inputParam;
            givenArgTypes = bTupleType.tupleTypes;
        } else {
            givenArgTypes = operation.lambdaType.getParameterTypes();
        }
        operation.arity = givenArgTypes.size();
        return givenArgTypes;
    }

    private List<BType> calculatedGivenOutputArgs(Operation operation) {
        final List<BType> givenRetTypes;
        final BType lamdaReturnType = operation.lambdaType.getReturnType();
        if (lamdaReturnType == null || lamdaReturnType == symTable.nilType) {
            givenRetTypes = Collections.emptyList();
            operation.outputType = symTable.nilType;
        } else {
            final BType returnType = operation.outputType = operation.lambdaType.getReturnType();
            if (returnType.tag == TypeTags.TUPLE) {
                givenRetTypes = ((BTupleType) returnType).tupleTypes;
            } else {
                givenRetTypes = Lists.of(operation.lambdaType.getReturnType());
            }
        }
        return givenRetTypes;
    }

    private List<BType> calculateProvidedElementTypes(Operation operation) {
        // calculated lambda's args types. (By looking collection type)
        return operation.collectionType.accept(lambdaTypeChecker, operation);
    }

    private int inferExpectedArity(Operation operation) {
        if (operation.collectionType.getKind() == TypeKind.INTERMEDIATE_COLLECTION) {
            return ((BIntermediateCollectionType) operation.collectionType).tupleType.tupleTypes.size();
        }
        // If not an intermediate collection, infer input parameter as a tuple
        return 2;
    }

    private List<BType> calculateExpectedOutputArgs(Operation operation, List<BType> givenRetTypes) {
        // calculated lambda's return types. (By looking operation type)
        final List<BType> supportedRetTypes;
        switch (operation.kind) {
            case MAP:
            case SELECT:
                supportedRetTypes = givenRetTypes;
                break;
            case FILTER:
                supportedRetTypes = Lists.of(symTable.booleanType);
                break;
            case FOREACH:
            default:
                supportedRetTypes = Collections.emptyList();
                break;
        }
        return supportedRetTypes;
    }

    private void validateLambdaInputArgs(Operation operation, List<BType> elementTypes, List<BType> paramTypes) {
        if (elementTypes.get(0).tag == TypeTags.SEMANTIC_ERROR) {
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        }
        for (int i = 0; i < paramTypes.size(); i++) {
            if (paramTypes.get(i).tag == TypeTags.SEMANTIC_ERROR) {
                return;
            }
            BType result = types.checkType(operation.pos, elementTypes.get(i), paramTypes.get(i),
                                           DiagnosticCode.ITERABLE_LAMBDA_INCOMPATIBLE_TYPES);
            if (result.tag == TypeTags.SEMANTIC_ERROR) {
                operation.outputType = operation.resultType = symTable.semanticError;
            }
        }
    }

    private void validateLambdaReturnArgs(Operation operation, List<BType> supportedTypes, List<BType> givenTypes) {
        if (supportedTypes == givenTypes) {
            // Ignore this validation.
            return;
        }
        if (givenTypes.size() > supportedTypes.size()) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_TOO_MANY_RETURN_VARIABLES, operation.kind);
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        } else if (givenTypes.size() < supportedTypes.size()) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_NOT_ENOUGH_RETURN_VARIABLES, operation.kind);
            operation.outputType = operation.resultType = symTable.semanticError;
            return;
        }
        for (int i = 0; i < givenTypes.size(); i++) {
            if (givenTypes.get(i).tag == TypeTags.SEMANTIC_ERROR) {
                return;
            }
            BType result = types.checkType(operation.pos, givenTypes.get(i), supportedTypes.get(i),
                    DiagnosticCode.ITERABLE_LAMBDA_INCOMPATIBLE_TYPES);
            if (result.tag == TypeTags.SEMANTIC_ERROR) {
                operation.outputType = operation.resultType = symTable.semanticError;
            }
        }
    }

    private void assignOutputAndResultType(Operation op, List<BType> argTypes, List<BType> supportedRetTypes) {
        if (supportedRetTypes.isEmpty()) {
            op.outputType = op.resultType = symTable.nilType;
            return;
        }
        if (op.kind.isTerminal()) {
            op.outputType = op.resultType = supportedRetTypes.get(0);
            return;
        }
        if (op.kind == IterableKind.FILTER) {
            op.outputType = new BTupleType(argTypes);
            op.resultType = new BIntermediateCollectionType((BTupleType) op.outputType);
            return;
        }
        if (supportedRetTypes.size() == 1) {
            op.outputType = supportedRetTypes.get(0);
        } else {
            op.outputType = new BTupleType(supportedRetTypes);
        }
        op.resultType = new BIntermediateCollectionType(new BTupleType(supportedRetTypes));
    }

    /* Iterable Operation type checkers */

    /**
     * Type checker for Lambda based operations.
     *
     * @since 0.961.0
     */
    private static class LambdaBasedTypeChecker extends BIterableTypeVisitor {

        LambdaBasedTypeChecker(BLangDiagnosticLog dlog, SymbolTable symTable, Types types) {
            super(dlog, symTable, types);
        }

        @Override
        public List<BType> visit(BMapType type, Operation op) {
            if (op.arity == 0) {
                logNotEnoughVariablesError(op, 1);
                return Lists.of(symTable.semanticError);
            } else if (op.arity == 1) {
                return Lists.of(type.constraint);
            } else if (op.arity == 2) {
                return Lists.of(symTable.stringType, type.constraint);
            }
            logTooManyVariablesError(op);
            return Lists.of(symTable.semanticError);
        }

        @Override
        public List<BType> visit(BXMLType type, Operation op) {
            if (op.arity == 0) {
                logNotEnoughVariablesError(op, 1);
                return Lists.of(symTable.semanticError);
            } else if (op.arity == 1) {
                return Lists.of(symTable.xmlType);
            } else if (op.arity == 2) {
                return Lists.of(symTable.intType, symTable.xmlType);
            }
            logTooManyVariablesError(op);
            return Lists.of(symTable.semanticError);
        }

        @Override
        public List<BType> visit(BJSONType type, Operation op) {
            if (op.arity == 0) {
                logNotEnoughVariablesError(op, 1);
                return Lists.of(symTable.semanticError);
            } else if (op.arity == 1) {
                return Lists.of(symTable.jsonType);
            }
            logTooManyVariablesError(op);
            return Lists.of(symTable.semanticError);
        }

        @Override
        public List<BType> visit(BArrayType type, Operation op) {
            if (op.arity == 0) {
                logNotEnoughVariablesError(op, 1);
                return Lists.of(symTable.semanticError);
            } else if (op.arity == 1) {
                return Lists.of(type.eType);
            } else if (op.arity == 2) {
                return Lists.of(symTable.intType, type.eType);
            }
            logTooManyVariablesError(op);
            return Lists.of(symTable.semanticError);
        }

        @Override
        public List<BType> visit(BTableType type, Operation op) {
            if (op.arity == 0) {
                logNotEnoughVariablesError(op, 1);
                return Lists.of(symTable.semanticError);
            } else if (op.arity == 1) {
                return Lists.of(type.getConstraint());
            }
            logTooManyVariablesError(op);
            return Lists.of(symTable.semanticError);
        }

        @Override
        public List<BType> visit(BRecordType type, Operation op) {
            if (op.arity == 0) {
                logNotEnoughVariablesError(op, 1);
                return Lists.of(symTable.semanticError);
            } else if (op.arity == 1) {
                return Lists.of(types.inferRecordFieldType(type));
            } else if (op.arity == 2) {
                return Lists.of(symTable.stringType, types.inferRecordFieldType(type));
            }
            logTooManyVariablesError(op);
            return Lists.of(symTable.semanticError);
        }

        @Override
        public List<BType> visit(BIntermediateCollectionType collectionType, Operation op) {
            BTupleType type = collectionType.tupleType;
            if (type.tupleTypes.size() == op.arity) {
                return type.tupleTypes;
            } else if (type.tupleTypes.size() < op.arity) {
                logTooManyVariablesError(op);
                return Lists.of(symTable.semanticError);
            }
            logNotEnoughVariablesError(op, type.tupleTypes.size());
            return Lists.of(symTable.semanticError);
        }
    }

    /**
     * Type checker for checking output of a simple terminal operation.
     *
     * @since 0.961.0
     */
    private static class TerminalOutputTypeChecker extends BIterableTypeVisitor.TerminalOperationTypeChecker {

        TerminalOutputTypeChecker(BLangDiagnosticLog dlog, SymbolTable symTable, Types types) {
            super(dlog, symTable, types);
        }

        @Override
        public BType calculateType(Operation operation, BType type) {
            BType elementType = type;
            switch (operation.kind) {
                case MAX:
                case MIN:
                case SUM:
                    if (elementType.tag == TypeTags.INTERMEDIATE_COLLECTION) {
                        BIntermediateCollectionType collectionType = (BIntermediateCollectionType) elementType;
                        if (collectionType.tupleType.tupleTypes.size() == 1) {
                            elementType = collectionType.tupleType.tupleTypes.get(0);
                        }
                    }
                    if (elementType.tag == TypeTags.INT || elementType.tag == TypeTags.FLOAT) {
                        return elementType;
                    }
                    break;
                case AVERAGE:
                    if (elementType.tag == TypeTags.INTERMEDIATE_COLLECTION) {
                        BIntermediateCollectionType collectionType = (BIntermediateCollectionType) elementType;
                        if (collectionType.tupleType.tupleTypes.size() == 1) {
                            elementType = collectionType.tupleType.tupleTypes.get(0);
                        }
                    }
                    if (elementType.tag == TypeTags.INT || elementType.tag == TypeTags.FLOAT) {
                        return symTable.floatType;
                    }
                    break;
                case COUNT:
                    return symTable.intType;
                default:
                    break;
            }
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_OPERATION, operation.kind);
            return symTable.semanticError;
        }
    }

    /**
     * Type checker for checking input of a simple terminal operation.
     *
     * @since 0.961.0
     */
    private static class TerminalInputTypeChecker extends BIterableTypeVisitor.TerminalOperationTypeChecker {

        TerminalInputTypeChecker(BLangDiagnosticLog dlog, SymbolTable symTable, Types types) {
            super(dlog, symTable, types);
        }

        @Override
        public BType calculateType(Operation operation, BType type) {
            BType elementType = type;
            switch (operation.kind) {
                case MAX:
                case MIN:
                case SUM:
                    if (elementType.tag == TypeTags.INTERMEDIATE_COLLECTION) {
                        BIntermediateCollectionType collectionType = (BIntermediateCollectionType) elementType;
                        if (collectionType.tupleType.tupleTypes.size() == 1) {
                            elementType = collectionType.tupleType.tupleTypes.get(0);
                        }
                    }
                    if (elementType.tag == TypeTags.INT || elementType.tag == TypeTags.FLOAT) {
                        return elementType;
                    }
                    break;
                case AVERAGE:
                    if (elementType.tag == TypeTags.INTERMEDIATE_COLLECTION) {
                        BIntermediateCollectionType collectionType = (BIntermediateCollectionType) elementType;
                        if (collectionType.tupleType.tupleTypes.size() == 1) {
                            elementType = collectionType.tupleType.tupleTypes.get(0);
                        }
                    }
                    if (elementType.tag == TypeTags.INT || elementType.tag == TypeTags.FLOAT) {
                        return elementType;
                    }
                    break;
                case COUNT:
                    if (elementType.tag == TypeTags.INTERMEDIATE_COLLECTION) {
                        BIntermediateCollectionType collectionType = (BIntermediateCollectionType) elementType;
                        elementType = collectionType.tupleType.tupleTypes.get(0);
                    }
                    return elementType;
                default:
                    break;
            }
            return symTable.semanticError;
        }
    }

    public void validateIterableContext(IterableContext context) {
        final Operation lastOperation = context.operations.getLast();
        final BType expectedType = lastOperation.expectedType;
        final BType outputType = lastOperation.resultType;
        if (expectedType.tag == TypeTags.VOID && outputType.tag == TypeTags.VOID) {
            context.resultType = symTable.noType;
            return;
        }
        if (expectedType.tag == TypeTags.VOID) {
            // This error already logged.
            return;
        }
        if (expectedType == symTable.semanticError) {
            context.resultType = symTable.semanticError;
            return;
        }
        if (outputType.tag == TypeTags.VOID) {
            dlog.error(lastOperation.pos, DiagnosticCode.DOES_NOT_RETURN_VALUE, lastOperation.kind);
            context.resultType = symTable.semanticError;
            return;
        }
        // Calculate expected type, if this is an chained iterable operation.
        if (outputType.tag == TypeTags.INTERMEDIATE_COLLECTION) {
            BIntermediateCollectionType collectionType = (BIntermediateCollectionType) outputType;
            final BTupleType tupleType = collectionType.tupleType;
            if (expectedType.tag == TypeTags.ARRAY && tupleType.tupleTypes.size() == 1) {
                // Convert result into an array.
                context.resultType = types.checkType(lastOperation.pos, new BArrayType(tupleType.tupleTypes.get(0)),
                        expectedType, DiagnosticCode.INCOMPATIBLE_TYPES);
                return;
            } else if (expectedType.tag == TypeTags.MAP && tupleType.tupleTypes.size() == 2
                    && tupleType.tupleTypes.get(0).tag == TypeTags.STRING) {
                // Convert result into a map.
                context.resultType = types.checkType(lastOperation.pos, new BMapType(TypeTags.MAP,
                        tupleType.tupleTypes.get(1), null), expectedType, DiagnosticCode.INCOMPATIBLE_TYPES);
                return;
            } else if (expectedType.tag == TypeTags.TABLE) {
                // expectedTypes hold the types of expected return values (types of references) of the iterable
                // function call.
                // Here we validate,
                // 1. whether number of return values of the lambda function is 1.
                // 2. Whether it is a struct that is returned
                // 3. Whether the returned struct is compatible with the constraint struct of the expected type(table)
                if (tupleType.getTupleTypes().size() == 1 && (tupleType.getTupleTypes().get(0).tag == TypeTags.OBJECT
                        || tupleType.getTupleTypes().get(0).tag == TypeTags.RECORD)
                        && types.isAssignable(tupleType.getTupleTypes().get(0), ((BTableType) expectedType)
                        .constraint)) {
                    context.resultType = symTable.tableType;
                } else {
                    context.resultType = types.checkType(lastOperation.pos, outputType,
                                                         ((BTableType) expectedType).constraint,
                                                         DiagnosticCode.INCOMPATIBLE_TYPES);
                }
                return;
            } else if (expectedType.tag == TypeTags.TUPLE) {
                context.resultType = symTable.semanticError;
                return;
            } else if (expectedType.tag == TypeTags.ANY) {
                context.resultType = symTable.semanticError;
                dlog.error(lastOperation.pos, DiagnosticCode.ITERABLE_RETURN_TYPE_MISMATCH, lastOperation.kind);
                return;
            } else if (expectedType.tag == TypeTags.NONE) {
                context.resultType = symTable.noType;
                return;
            }
        }

        // Validate compatibility with calculated and expected type.
        context.resultType = types.checkType(lastOperation.pos, outputType, expectedType,
                DiagnosticCode.INCOMPATIBLE_TYPES);
    }
}
