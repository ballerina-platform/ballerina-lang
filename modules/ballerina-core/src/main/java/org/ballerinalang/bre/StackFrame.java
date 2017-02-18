/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre;

import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.values.BValue;

import java.util.AbstractMap;
import java.util.HashMap;

/**
 * {@code StackFrame} represents frame in a control stack.
 * Holds references to parameters, local variables and return values
 *
 * @since 0.8.0
 */
public class StackFrame {
    public BValue[] values;
    public BValue[] returnValues;
    private CallableUnitInfo callableUnitInfo;
    // Field for Non-Blocking Implementation.
    public BValue[] tempValues;
    // Use only in debugger. Added when variables are accessed.
    public HashMap<SymbolName, AbstractMap.SimpleEntry<Integer, String>> variables;

    public StackFrame(BValue[] values, BValue[] returnValues) {
        this.values = values;
        this.returnValues = returnValues;
    }
    
    /**
     * Create a Stack frame.
     * 
     * @param values         Parameter and local variable values
     * @param returnValues   Return values
     * @param callableUnitInfo         Meta info of the node.   
     */
    public StackFrame(BValue[] values, BValue[] returnValues, CallableUnitInfo callableUnitInfo) {
        this.values = values;
        this.returnValues = returnValues;
        this.callableUnitInfo = callableUnitInfo;
    }

    public StackFrame(BValue[] values, BValue[] returnValues, BValue[] tempValues, CallableUnitInfo callableUnitInfo) {
        this.values = values;
        this.returnValues = returnValues;
        this.tempValues = tempValues;
        this.callableUnitInfo = callableUnitInfo;
        variables = new HashMap<>();
    }

    /**
     * Get the meta info (see {@link CallableUnitInfo}) of this CallableUnit.
     * 
     * @return  Meta info (see {@link CallableUnitInfo}) of this CallableUnit.
     */
    public CallableUnitInfo getNodeInfo() {
        return this.callableUnitInfo;
    }
}
