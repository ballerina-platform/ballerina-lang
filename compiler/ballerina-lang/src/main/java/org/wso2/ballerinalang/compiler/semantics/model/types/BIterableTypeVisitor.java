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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.Operation;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.Lists;

import java.util.List;

/**
 * Type visitor used for validating iterable collections.
 *
 * @since 0.961.0
 */
public abstract class BIterableTypeVisitor implements BTypeVisitor<Operation, List<BType>> {

    protected final BLangDiagnosticLog dlog;
    protected SymbolTable symTable;

    public BIterableTypeVisitor(BLangDiagnosticLog dlog, SymbolTable symTable) {
        this.dlog = dlog;
        this.symTable = symTable;
    }

    @Override
    public List<BType> visit(BType type, Operation op) {
        dlog.error(op.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_COLLECTION, op.collectionType);
        return Lists.of(symTable.errType);
    }

    @Override
    public List<BType> visit(BErrorType type, Operation op) {
        return Lists.of(symTable.errType);
    }

    /* Following  types are not iterable and will be handler at BType visitor */

    @Override
    public List<BType> visit(BBuiltInRefType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BAnyType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BStructType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BTableType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BTupleType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BStreamType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BConnectorType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BEnumType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BInvokableType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BUnionType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BFutureType type, Operation op) {
        return visit((BType) type, op);
    }

    @Override
    public List<BType> visit(BFiniteType type, Operation op) {
        return visit((BType) type, op);
    }

    /* Util functions */

    protected void logTooMayVariablesError(Operation op) {
        dlog.error(op.pos, DiagnosticCode.ITERABLE_TOO_MANY_VARIABLES, op.collectionType);
    }

    protected void logNotEnoughVariablesError(Operation op, int count) {
        dlog.error(op.pos, DiagnosticCode.ITERABLE_NOT_ENOUGH_VARIABLES, op.collectionType, count);
    }

    /**
     * Type checker for Simple terminal operations.
     *
     * @since 0.970.0
     */
    public abstract static class TerminalOperationTypeChecker extends BIterableTypeVisitor {

        public TerminalOperationTypeChecker(BLangDiagnosticLog dlog, SymbolTable symTable) {
            super(dlog, symTable);
        }

        @Override
        public List<BType> visit(BMapType t, Operation operation) {
            return Lists.of(calculateType(operation, t.constraint));
        }

        @Override
        public List<BType> visit(BXMLType t, Operation operation) {
            return Lists.of(calculateType(operation, t));
        }

        @Override
        public List<BType> visit(BJSONType t, Operation operation) {
            return Lists.of(calculateType(operation, t));
        }

        @Override
        public List<BType> visit(BArrayType t, Operation operation) {
            return Lists.of(calculateType(operation, t.eType));
        }

        @Override
        public List<BType> visit(BIntermediateCollectionType t, Operation operation) {
            return Lists.of(calculateType(operation, t));
        }

        @Override
        public List<BType> visit(BTableType t, Operation operation) {
            return Lists.of(calculateType(operation, t));
        }

        public abstract BType calculateType(Operation operation, BType type);
    }

}
