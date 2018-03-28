/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.List;

/**
 * Used in taint analysis to maintains tainted status of return parameters or any taint checking related errors.
 *
 * @since 0.965.0
 */
public class TaintRecord {
    public List<Boolean> retParamTaintedStatus;
    public List<TaintError> taintError;

    public TaintRecord(List<Boolean> retParamTaintedStatus, List<TaintError> taintError) {
        this.retParamTaintedStatus = retParamTaintedStatus;
        this.taintError = taintError;
    }

    /**
     * Used to propagate taint checking related error information through taint-table.
     */
    public static class TaintError {
        public DiagnosticPos pos;
        public String paramName;
        public DiagnosticCode diagnosticCode;

        public TaintError(DiagnosticPos pos, String paramName, DiagnosticCode diagnosticCode) {
            this.pos = pos;
            this.paramName = paramName;
            this.diagnosticCode = diagnosticCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            TaintError that = (TaintError) o;

            if (!pos.toString().equals(that.pos.toString())) {
                return false;
            }
            if (!paramName.equals(that.paramName)) {
                return false;
            }
            return diagnosticCode == that.diagnosticCode;
        }

        @Override
        public int hashCode() {
            int result = pos.toString().hashCode();
            result = 31 * result + paramName.hashCode();
            result = 31 * result + diagnosticCode.hashCode();
            return result;
        }
    }
}
