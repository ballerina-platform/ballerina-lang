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
package org.ballerinalang.util.codegen;

/**
 * @since 0.87
 */
public class Mnemonics {
    private static final String[] mnemonics = new String[InstructionCodes.InstructionCodeCount];

    static {
        mnemonics[InstructionCodes.iconst] = "iconst";
        mnemonics[InstructionCodes.fconst] = "fconst";
        mnemonics[InstructionCodes.sconst] = "sconst";
        mnemonics[InstructionCodes.iconst_0] = "iconst_0";
        mnemonics[InstructionCodes.iconst_1] = "iconst_1";
        mnemonics[InstructionCodes.iconst_2] = "iconst_2";
        mnemonics[InstructionCodes.iconst_3] = "iconst_3";
        mnemonics[InstructionCodes.iconst_4] = "iconst_4";
        mnemonics[InstructionCodes.iconst_5] = "iconst_5";
        mnemonics[InstructionCodes.fconst_0] = "fconst_0";
        mnemonics[InstructionCodes.fconst_1] = "fconst_1";
        mnemonics[InstructionCodes.fconst_2] = "fconst_2";
        mnemonics[InstructionCodes.fconst_3] = "fconst_3";
        mnemonics[InstructionCodes.fconst_4] = "fconst_4";
        mnemonics[InstructionCodes.fconst_5] = "fconst_5";
        mnemonics[InstructionCodes.bconst_0] = "bconst_0";
        mnemonics[InstructionCodes.bconst_1] = "bconst_1";

        mnemonics[InstructionCodes.iload] = "iload";
        mnemonics[InstructionCodes.fload] = "fload";
        mnemonics[InstructionCodes.sload] = "sload";
        mnemonics[InstructionCodes.bload] = "bload";
        mnemonics[InstructionCodes.rload] = "rload";
        mnemonics[InstructionCodes.iaload] = "iaload";
        mnemonics[InstructionCodes.faload] = "faload";
        mnemonics[InstructionCodes.saload] = "saload";
        mnemonics[InstructionCodes.baload] = "baload";
        mnemonics[InstructionCodes.raload] = "raload";

        mnemonics[InstructionCodes.istore] = "istore";
        mnemonics[InstructionCodes.fstore] = "fstore";
        mnemonics[InstructionCodes.sstore] = "sstore";
        mnemonics[InstructionCodes.bstore] = "bstore";
        mnemonics[InstructionCodes.rstore] = "rstore";
        mnemonics[InstructionCodes.iastore] = "iastore";
        mnemonics[InstructionCodes.fastore] = "fastore";
        mnemonics[InstructionCodes.sastore] = "sastore";
        mnemonics[InstructionCodes.bastore] = "bastore";
        mnemonics[InstructionCodes.rastore] = "rastore";

        mnemonics[InstructionCodes.iadd] = "iadd";
        mnemonics[InstructionCodes.fadd] = "fadd";
        mnemonics[InstructionCodes.sadd] = "sadd";
        mnemonics[InstructionCodes.isub] = "isub";
        mnemonics[InstructionCodes.fsub] = "fsub";
        mnemonics[InstructionCodes.sadd] = "sadd";
        mnemonics[InstructionCodes.imul] = "imul";
        mnemonics[InstructionCodes.fmul] = "fmul";
        mnemonics[InstructionCodes.idiv] = "idiv";
        mnemonics[InstructionCodes.fdiv] = "fdiv";
        mnemonics[InstructionCodes.imod] = "imod";
        mnemonics[InstructionCodes.fmod] = "fmod";
        
        mnemonics[InstructionCodes.icmp] = "icmp";
        mnemonics[InstructionCodes.fcmp] = "fcmp";
        mnemonics[InstructionCodes.scmp] = "scmp";
        mnemonics[InstructionCodes.bcmp] = "bcmp";
        mnemonics[InstructionCodes.ifeq] = "ifeq";
        mnemonics[InstructionCodes.ifne] = "ifne";
        mnemonics[InstructionCodes.iflt] = "iflt";
        mnemonics[InstructionCodes.ifge] = "ifge";
        mnemonics[InstructionCodes.ifgt] = "ifgt";
        mnemonics[InstructionCodes.ifle] = "ifle";

        mnemonics[InstructionCodes.goto_] = "goto";
        mnemonics[InstructionCodes.call] = "call";
        mnemonics[InstructionCodes.ret] = "ret";

        mnemonics[InstructionCodes.inewarray] = "inewarray";
        mnemonics[InstructionCodes.fnewarray] = "fnewarray";
        mnemonics[InstructionCodes.snewarray] = "snewarray";
        mnemonics[InstructionCodes.bnewarray] = "bnewarray";
        mnemonics[InstructionCodes.rnewarray] = "rnewarray";
    }

    public static String getMnem(int opcode) {
        return mnemonics[opcode];
    }
}
