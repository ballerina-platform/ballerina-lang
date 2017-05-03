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
    int rload = 26;
    int iaload = 27;
    int faload = 28;
    int saload = 29;
    int baload = 30;
    int raload = 31;

    int istore = 32;
    int fstore = 33;
    int sstore = 34;
    int bstore = 35;
    int rstore = 36;
    int iastore = 37;
    int fastore = 38;
    int sastore = 39;
    int bastore = 40;
    int rastore = 41;

    int ifieldload = 50;
    int ffieldload = 51;
    int sfieldload = 52;
    int bfieldload = 53;
    int rfieldload = 54;

    int ifieldstore = 60;
    int ffieldstore = 61;
    int sfieldstore = 62;
    int bfieldstore = 63;
    int rfieldstore = 64;

    int iadd = 70;
    int fadd = 71;
    int sadd = 72;
    int isub = 73;
    int fsub = 74;
    int imul = 75;
    int fmul = 76;
    int idiv = 77;
    int fdiv = 78;
    int imod = 79;
    int fmod = 80;

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

    int goto_ = 119;
    int call = 120;
    int ret = 121;

    int inewarray = 200;
    int fnewarray = 201;
    int snewarray = 202;
    int bnewarray = 203;
    int rnewarray = 204;

    int newstruct = 210;

    int InstructionCodeCount = 211;
}
