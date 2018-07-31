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
package org.wso2.ballerinalang.compiler.bir.model;

import org.wso2.ballerinalang.compiler.bir.model.BIRNode.BIRVariableDcl;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * {@code BIROperand} represents an operand in an instruction.
 * <p>
 * There can only be two kinds of operands: variable references and constant values.
 *
 * @since 0.980.0
 */
public abstract class BIROperand {

    public BType type;
    public Kind kind;

    public BIROperand(BType type, Kind kind) {
        this.type = type;
        this.kind = kind;
    }

    /**
     * A variable reference operand.
     *
     * @since 0.980.0
     */
    public static class BIRVarRef extends BIROperand {

        public BIRVariableDcl variableDcl;

        public BIRVarRef(BIRVariableDcl variableDcl) {
            super(variableDcl.type, Kind.VAR_REF);
            this.variableDcl = variableDcl;
        }
    }

    /**
     * A constant value operand.
     *
     * @since 0.980.0
     */
    public static class BIRConstant extends BIROperand {

        public Object value;

        public BIRConstant(BType type, Object value) {
            super(type, Kind.CONST);
            this.value = value;
        }
    }

    /**
     * The kind of the operand.
     */
    public enum Kind {
        VAR_REF,
        CONST
    }
}
