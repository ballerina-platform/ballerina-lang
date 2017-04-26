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
 * Bytecode instructions of a Ballerina compiled program.
 *
 * @since 0.87
 */
public interface InstructionCodes {

    // TODO Add const* load* and store* instructions
    // TODO const* load a constant from the CP
    // TODO load* bring value from local variable to a register
    // TODO store* stores a value in a register to a local variable

    int iconst = 2;
    int fconst = 3;
    int sconst = 4;

    int iconst_0 = 5;
    int iconst_1 = 6;
    int iconst_2 = 7;
    int iconst_3 = 8;
    int iconst_4 = 9;
    int iconst_5 = 10;

    int fconst_0 = 11;
    int fconst_1 = 12;
    int fconst_2 = 13;
    int fconst_3 = 14;
    int fconst_4 = 15;
    int fconst_5 = 16;

    int bconst_0 = 17;
    int bconst_1 = 18;

    int iload = 22;
    int fload = 23;
    int sload = 24;
    int bload = 25;

    int istore = 30;
    int fstore = 31;
    int sstore = 32;
    int bstore = 33;

    int iadd = 50;
    int fadd = 51;
    int sadd = 52;

    int isub = 53;
    int fsub = 54;

    int imul = 55;
    int fmul = 56;

    int idiv = 57;
    int fdiv = 58;

    int imod = 59;
    int fmod = 60;

    int imove = 70;
    int fmove = 71;
    int smove = 72;
    int bmove = 73;
    int rmove = 74;

    int iret = 90;
    int fret = 91;
    int sret = 92;
    int bret = 93;
    int rret = 94;
    int ret = 95;


    int icmp = 100;
    int fcmp = 101;
    int scmp = 102;
    int bcmp = 103;

    int ifeq = 110;
    int ifne = 111;
    int iflt = 112;
    int ifge = 113;
    int ifgt = 114;
    int ifle = 115;
    int goto_ = 116;

    int call = 120;
    int InstructionCodeCount = 121;

}
