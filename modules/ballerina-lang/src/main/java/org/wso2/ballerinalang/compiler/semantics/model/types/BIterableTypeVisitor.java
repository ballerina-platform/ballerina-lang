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
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticLog;
import org.wso2.ballerinalang.util.Lists;

import java.util.Collections;
import java.util.List;

/**
 * TODO: Add.
 *
 * @since 0.96.0
 */
public abstract class BIterableTypeVisitor implements BTypeVisitor<Operation, List<BType>> {

    protected final DiagnosticLog dlog;
    protected SymbolTable symTable;

    public BIterableTypeVisitor(DiagnosticLog dlog, SymbolTable symTable) {
        this.dlog = dlog;
        this.symTable = symTable;
    }

    @Override
    public List<BType> visit(BType type, Operation op) {
        dlog.error(op.pos, DiagnosticCode.ITERABLE_NOT_SUPPORTED_COLLECTION, op.collectionType);
        return getErrorTypeList(op.arity);
    }

    @Override
    public List<BType> visit(BErrorType type, Operation op) {
        return Collections.nCopies(op.arity, type);
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


    /* Util functions */

    protected void logTooMayVariablesError(Operation op) {
        dlog.error(op.pos, DiagnosticCode.ITERABLE_TOO_MANY_VARIABLES, op.collectionType);
    }

    protected List<BType> getErrorTypeList(int size, BType... initialData) {
        List<BType> errorTypes = Lists.of(initialData);
        errorTypes.addAll(Collections.nCopies(size - initialData.length, symTable.errType));
        return errorTypes;
    }


}
