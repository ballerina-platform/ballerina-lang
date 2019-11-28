/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.programfile.Instruction.Operand;

/**
 * This class creates specific instructions for the given opcode/bytecode.
 *
 * @since 0.87
 */
@Deprecated
public class InstructionFactory {

    public static Instruction get(int opcode) {
        return new Instruction(opcode);
    }

    public static Instruction get(int opcode, Operand... operands) {
        return new Instruction(opcode, operands);
    }
}
