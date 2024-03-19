/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.bir.codegen.interop;

import io.ballerina.tools.diagnostics.Location;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;

import java.util.List;

/**
 * Java method invocation call, modelled as BIR terminator, for internal CLI argument population.
 *
 * @since 2201.9.0
 */
public class JIMethodCLICall extends JTerminator {

    public List<BIROperand> lhsArgs;
    public String jClassName;
    public String jMethodVMSig;
    public String name;
    public List<BIROperand> defaultFunctionArgs;

    public JIMethodCLICall(Location pos) {
        super(pos);
        this.jTermKind = JTermKind.JI_METHOD_CLI_CALL;
    }

    @Override
    public BIROperand[] getRhsOperands() {
        return lhsArgs.toArray(new BIROperand[0]);
    }

    @Override
    public void setRhsOperands(BIROperand[] operands) {
        this.lhsArgs = List.of(operands);
    }

}
