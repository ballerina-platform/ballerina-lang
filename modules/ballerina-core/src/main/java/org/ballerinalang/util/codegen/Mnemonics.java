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


import static org.ballerinalang.util.codegen.InstructionCodes.*;

/**
 * @since 0.87
 */
public class Mnemonics {
    private final static String[] mnemonics = new String[InstructionCodeCount];

    static {
        mnemonics[iconst] = "iconst";
        mnemonics[fconst] = "fconst";
        mnemonics[sconst] = "sconst";

        mnemonics[iconst_0] = "iconst_0";
        mnemonics[iconst_1] = "iconst_1";
        mnemonics[iconst_2] = "iconst_2";
        mnemonics[iconst_3] = "iconst_3";
        mnemonics[iconst_4] = "iconst_4";
        mnemonics[iconst_5] = "iconst_5";

        mnemonics[fconst_0] = "fconst_0";
        mnemonics[fconst_1] = "fconst_1";
        mnemonics[fconst_2] = "fconst_2";
        mnemonics[fconst_3] = "fconst_3";
        mnemonics[fconst_4] = "fconst_4";
        mnemonics[fconst_5] = "fconst_5";

        mnemonics[bconst_0] = "bconst_0";
        mnemonics[bconst_1] = "bconst_1";

        mnemonics[iload] = "iload";
        mnemonics[fload] = "fload";
        mnemonics[sload] = "fload";
        mnemonics[bload] = "bload";

        mnemonics[istore] = "istore";
        mnemonics[fstore] = "fstore";
        mnemonics[sstore] = "sstore";
        mnemonics[bstore] = "bstore";

        mnemonics[iadd] = "iadd";
        mnemonics[fadd] = "fadd";
        mnemonics[sadd] = "sadd";

        mnemonics[isub] = "isub";
        mnemonics[fsub] = "fsub";
        mnemonics[sadd] = "sadd";

        mnemonics[imul] = "imul";
        mnemonics[fmul] = "fmul";

        mnemonics[idiv] = "idiv";
        mnemonics[fdiv] = "fdiv";

        mnemonics[imod] = "imod";
        mnemonics[fmod] = "fmod";

        mnemonics[imove] = "imove";
        mnemonics[fmove] = "fmove";
        mnemonics[smove] = "smove";
        mnemonics[bmove] = "bmove";
        mnemonics[rmove] = "rmove";

        mnemonics[iret] = "iret";
        mnemonics[fret] = "fret";
        mnemonics[sret] = "sret";
        mnemonics[bret] = "bret";
        mnemonics[rret] = "rret";
        mnemonics[ret] = "ret";

        mnemonics[icmp] = "icmp";
        mnemonics[fcmp] = "fcmp";
        mnemonics[scmp] = "scmp";
        mnemonics[bcmp] = "bcmp";

        mnemonics[ifeq] = "ifeq";
        mnemonics[ifne] = "ifne";
        mnemonics[iflt] = "iflt";
        mnemonics[ifge] = "ifge";
        mnemonics[ifgt] = "ifgt";
        mnemonics[ifle] = "ifle";
        mnemonics[goto_] = "goto";

        mnemonics[call] = "call";
    }

    public static String getMnem(int opcode) {
        return mnemonics[opcode];
    }
}
