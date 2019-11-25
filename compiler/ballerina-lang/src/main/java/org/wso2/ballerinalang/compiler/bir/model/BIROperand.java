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

/**
 * {@code BIROperand} represents an operand in an instruction.
 *
 * @since 0.980.0
 */
public class BIROperand extends BIRNode {

    public BIRVariableDcl variableDcl;

    public BIROperand(BIRVariableDcl variableDcl) {
        super(null);
        this.variableDcl = variableDcl;
    }

    @Override
    public void accept(BIRVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof BIROperand)) {
            return false;
        }

        return this.variableDcl.equals(((BIROperand) other).variableDcl);
    }

    @Override
    public String toString() {
        return variableDcl.toString();
    }

    @Override
    public int hashCode() {
        return this.variableDcl.hashCode();
    }
}
