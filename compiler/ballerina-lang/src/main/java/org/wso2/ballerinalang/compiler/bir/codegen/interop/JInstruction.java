/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;

/**
 * Java specific instruction definitions.
 *
 * @since 1.2.0
 */
public class JInstruction extends BIRNonTerminator {

    public JInsKind jKind;

    JInstruction(Location pos) {

        super(pos, InstructionKind.PLATFORM);
    }

    @Override
    public void accept(BIRVisitor visitor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BIROperand[] getRhsOperands() {
        return new BIROperand[0];
    }

    @Override
    public void setRhsOperands(BIROperand[] operands) {
        // do nothing
    }
}
