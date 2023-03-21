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
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRVisitor;

import java.util.List;

/**
 * Java method invocation call modeled as BIR terminator.
 *
 * @since 1.2.0
 */
public class JIMethodCall extends JTerminator {

    public BIROperand receiver;
    public List<BIROperand> args;
    public List<BIROperand> resourcePathArgs;
    public boolean varArgExist;
    public JType varArgType;
    public String jClassName;
    public String jMethodVMSig;
    public String name;
    public int invocationType;

    public boolean isInternal = false;

    public JIMethodCall(Location pos) {

        super(pos);
        this.jTermKind = JTermKind.JI_METHOD_CALL;
    }

    @Override
    public void accept(BIRVisitor visitor) {
        // Do nothing
    }

    @Override
    public BIROperand[] getRhsOperands() {
        return args.toArray(new BIROperand[0]);
    }

    @Override
    public BIRBasicBlock[] getNextBasicBlocks() {
        return new BIRBasicBlock[0];
    }
}
