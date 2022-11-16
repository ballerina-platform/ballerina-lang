/*
 *  Copyright (c) 2022, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;

import java.util.List;

/**
 * Java method invocation call modeled as BIR NonTerminator Instruction.
 *
 * @since 2201.4.0
 */
public class JMethodCallInstruction extends JInstruction {

    public List<BIROperand> args;
    public String jClassName;
    public String jMethodVMSig;
    public String name;
    public int invocationType;

    public JMethodCallInstruction(Location pos) {
        super(pos);
        jKind = JInsKind.CALL;
    }

    @Override
    public BIROperand[] getRhsOperands() {
        return args.toArray(new BIROperand[0]);
    }
}
