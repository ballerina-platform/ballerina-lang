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

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableContext;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.Operation;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIterableTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
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
    private DiagnosticLog dlog;

    private final BIterableTypeVisitor foreachTypeChecker, mapTypeChecker, filterTypeChecker;

    private IterableAnalyzer(CompilerContext context) {
        context.put(ITERABLE_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = DiagnosticLog.getInstance(context);
        this.typeChecker = TypeChecker.getInstance(context);

        this.foreachTypeChecker = new ForeachTypeChecker(dlog, symTable);
        this.mapTypeChecker = new MapTypeChecker(dlog, symTable);
        this.filterTypeChecker = new FilterTypeChecker(dlog, symTable);
    }

    public static IterableAnalyzer getInstance(CompilerContext context) {
        IterableAnalyzer iterableAnalyzer = context.get(ITERABLE_ANALYZER_KEY);
        if (iterableAnalyzer == null) {
            iterableAnalyzer = new IterableAnalyzer(context);
        }
        return iterableAnalyzer;
    }

    public void handlerIterableOperation(BLangInvocation iExpr, List<BType> expTypes, SymbolEnv env) {
        final IterableContext context;

        if (iExpr.expr.type.tag != TypeTags.TUPLE_COLLECTION) {
            context = new IterableContext(iExpr.expr);   // This is a new iteration chain.
            env.enclPkg.iterableContexts.add(context);
        } else {
            context = ((BLangInvocation) iExpr.expr).iContext; // Get context from previous invocation.
        }
        iExpr.iContext = context;

        final IterableKind iterableKind = IterableKind.getFromString(iExpr.name.value);

        final Operation iOperation = new Operation(iterableKind, iExpr, expTypes, env);
        iExpr.iContext.addOperation(iOperation);

        if (iterableKind.isLambdaRequired()) {
            handleLambdaBasedIterableOperation(iOperation);
        } else {
            handleSimpleOperations(iOperation);
        }
    }

    private void handleSimpleOperations(Operation operation) {
        if (operation.iExpr.argExprs.size() > 0) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_NO_ARGS_REQUIRED, operation.kind);
            return;
        }
        // Handle new Iterable Operations here.
        switch (operation.kind) {
            case MAX:
            case MIN:
            case SUM:
            case AVERAGE:
            case COUNT:
                break;
            default:
                // TODO: Log Error. Unsupported Operation.
        }
    }

    private void handleLambdaBasedIterableOperation(Operation operation) {
        if (operation.iExpr.argExprs.size() == 0 || operation.iExpr.argExprs.size() > 1) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_LAMBDA_REQUIRED);
            return;
        }

        operation.lambda = operation.iExpr.argExprs.get(0);
        final List<BType> bTypes = typeChecker.checkExpr(operation.lambda, operation.env);
        if (bTypes.size() != 1 || bTypes.get(0).tag != TypeTags.INVOKABLE) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_LAMBDA_REQUIRED);
            return;
        }

        operation.lambdaType = (BInvokableType) bTypes.get(0);
        operation.arity = operation.lambdaType.getParameterTypes().size();
        final List<BType> supportedArgTypes, supportedRetTypes, givenRetTypes;
        // Define new iterable operation here.
        givenRetTypes = operation.lambdaType.getReturnTypes();
        switch (operation.kind) {
            case FOREACH:
                supportedArgTypes = operation.collectionType.accept(foreachTypeChecker, operation);
                supportedRetTypes = Collections.emptyList();
                break;
            case MAP:
                supportedArgTypes = operation.collectionType.accept(mapTypeChecker, operation);
                supportedRetTypes = givenRetTypes;
                break;
            case FILTER:
                supportedArgTypes = operation.collectionType.accept(filterTypeChecker, operation);
                supportedRetTypes = Lists.of(symTable.booleanType);
                break;
            default:
                return;
        }
        validateLambdaArgs(operation, supportedArgTypes);
        validateLambdaReturnArgs(operation, supportedRetTypes, givenRetTypes);
        assignInvocationType(operation, supportedArgTypes, supportedRetTypes);
    }

    private void validateLambdaArgs(Operation operation, List<BType> supportedTypes) {
        final List<BType> givenTypes = operation.lambdaType.getParameterTypes();
        if (givenTypes.size() < supportedTypes.size()) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_NOT_ENOUGH_VARIABLES, operation.collectionType,
                    supportedTypes.size());
            return;
        } else if (givenTypes.size() > supportedTypes.size()) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_TOO_MANY_VARIABLES, operation.collectionType);
            return;
        }
        for (int i = 0; i < givenTypes.size(); i++) {
            if (supportedTypes.get(i).tag == TypeTags.ERROR || givenTypes.get(i).tag == TypeTags.ERROR) {
                return;
            }
            types.checkType(operation.lambda, givenTypes.get(i), supportedTypes.get(i),
                    DiagnosticCode.ITERABLE_LAMBDA_INCOMPATIBLE_TYPES);
        }
    }

    private void validateLambdaReturnArgs(Operation operation, List<BType> supportedTypes, List<BType> givenTypes) {
        if (supportedTypes == givenTypes) {
            // Ignore this validation.
            return;
        }
        if (givenTypes.size() < supportedTypes.size()) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_TOO_MANY_RETURN_VARIABLES, operation.kind);
            return;
        } else if (givenTypes.size() > supportedTypes.size()) {
            dlog.error(operation.pos, DiagnosticCode.ITERABLE_NOT_ENOUGH_RETURN_VARIABLES, operation.kind);
            return;
        }
        for (int i = 0; i < givenTypes.size(); i++) {
            if (supportedTypes.get(i).tag == TypeTags.ERROR || givenTypes.get(i).tag == TypeTags.ERROR) {
                return;
            }
            types.checkType(operation.lambda, givenTypes.get(i), supportedTypes.get(i),
                    DiagnosticCode.ITERABLE_LAMBDA_INCOMPATIBLE_TYPES);
        }
    }

    private void assignInvocationType(Operation operation, List<BType> argTypes, List<BType> supportedRetTypes) {
        operation.argTypes = argTypes;
        operation.retArgTypes = supportedRetTypes;
        if (operation.kind.isTerminal()) {
            operation.resultTypes = supportedRetTypes;
            return;
        }
        if (supportedRetTypes.size() == 0) {
            operation.resultTypes = Collections.emptyList();
            return;
        }
        if (operation.kind == IterableKind.FILTER) {
            operation.resultTypes = Lists.of(new BTupleCollectionType(argTypes));
            return;
        }
        operation.resultTypes = Lists.of(new BTupleCollectionType(supportedRetTypes));
    }

    /* Iterable Operation type checkers */

    /**
     * Type checker for Foreach Operation.
     *
     * @since 0.961.0
     */
    private static class ForeachTypeChecker extends BIterableTypeVisitor {

        ForeachTypeChecker(DiagnosticLog dlog, SymbolTable symTable) {
            super(dlog, symTable);
        }

        @Override
        public List<BType> visit(BMapType type, Operation op) {
            if (op.arity == 1) {
                return Lists.of(type.constraint);
            } else if (op.arity == 2) {
                return Lists.of(symTable.stringType, type.constraint);
            }
            logTooMayVariablesError(op);
            return getErrorTypeList(op.arity, symTable.stringType, type.constraint);
        }

        @Override
        public List<BType> visit(BXMLType type, Operation op) {
            if (op.arity == 1) {
                return Lists.of(symTable.xmlType);
            } else if (op.arity == 2) {
                return Lists.of(symTable.intType, symTable.xmlType);
            }
            logTooMayVariablesError(op);
            return getErrorTypeList(op.arity, symTable.intType, symTable.xmlType);
        }

        @Override
        public List<BType> visit(BJSONType type, Operation op) {
            if (op.arity == 1) {
                return Lists.of(symTable.jsonType);
            }
            logTooMayVariablesError(op);
            return getErrorTypeList(op.arity, symTable.jsonType);
        }

        @Override
        public List<BType> visit(BArrayType type, Operation op) {
            if (op.arity == 1) {
                return Lists.of(type.eType);
            } else if (op.arity == 2) {
                return Lists.of(symTable.intType, type.eType);
            }
            logTooMayVariablesError(op);
            return getErrorTypeList(op.arity, symTable.intType, type.eType);
        }

        @Override
        public List<BType> visit(BTupleCollectionType type, Operation op) {
            if (type.tupleTypes.size() == op.arity) {
                return type.tupleTypes;
            } else if (type.tupleTypes.size() < op.arity) {
                logTooMayVariablesError(op);
                return getErrorTypeList(op.arity, type.tupleTypes.toArray(new BType[0]));
            }
            dlog.error(op.pos, DiagnosticCode.ITERABLE_NOT_ENOUGH_VARIABLES, op.collectionType, type.tupleTypes.size());
            return Collections.nCopies(op.arity, symTable.errType);
        }
    }

    /**
     * Type checker for Map Operation.
     *
     * @since 0.961.0
     */
    private static class MapTypeChecker extends ForeachTypeChecker {

        MapTypeChecker(DiagnosticLog dlog, SymbolTable symTable) {
            super(dlog, symTable);
        }

        /* override methods to extend Map functionality. Otherwise it will have same behaviour as foreach */
    }

    /**
     * Type checker for Filter Operation.
     *
     * @since 0.961.0
     */
    private static class FilterTypeChecker extends ForeachTypeChecker {

        FilterTypeChecker(DiagnosticLog dlog, SymbolTable symTable) {
            super(dlog, symTable);
        }

        /* override methods to extend filter functionality. Otherwise it will have same behaviour as foreach */
    }
}
