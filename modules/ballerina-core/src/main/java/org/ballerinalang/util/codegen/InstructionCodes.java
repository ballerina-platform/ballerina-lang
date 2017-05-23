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
    
    int ICONST = 2;
    int FCONST = 3;
    int SCONST = 4;
    int ICONST_0 = 5;
    int ICONST_1 = 6;
    int ICONST_2 = 7;
    int ICONST_3 = 8;
    int ICONST_4 = 9;
    int ICONST_5 = 10;
    int FCONST_0 = 11;
    int FCONST_1 = 12;
    int FCONST_2 = 13;
    int FCONST_3 = 14;
    int FCONST_4 = 15;
    int FCONST_5 = 16;
    int BCONST_0 = 17;
    int BCONST_1 = 18;

    int ILOAD = 22;
    int FLOAD = 23;
    int SLOAD = 24;
    int BLOAD = 25;
    int RLOAD = 26;
    int IALOAD = 27;
    int FALOAD = 28;
    int SALOAD = 29;
    int BALOAD = 30;
    int RALOAD = 31;
    int IGLOAD = 32;
    int FGLOAD = 33;
    int SGLOAD = 34;
    int BGLOAD = 35;
    int RGLOAD = 36;

    int ISTORE = 37;
    int FSTORE = 38;
    int SSTORE = 39;
    int BSTORE = 40;
    int RSTORE = 41;
    int IASTORE = 42;
    int FASTORE = 43;
    int SASTORE = 44;
    int BASTORE = 45;
    int RASTORE = 46;
    int IGSTORE = 47;
    int FGSTORE = 48;
    int SGSTORE = 49;
    int BGSTORE = 50;
    int RGSTORE = 51;

    int IFIELDLOAD = 52;
    int FFIELDLOAD = 53;
    int SFIELDLOAD = 54;
    int BFIELDLOAD = 55;
    int RFIELDLOAD = 56;

    int IFIELDSTORE = 60;
    int FFIELDSTORE = 61;
    int SFIELDSTORE = 62;
    int BFIELDSTORE = 63;
    int RFIELDSTORE = 64;

    int IADD = 70;
    int FADD = 71;
    int SADD = 72;
    int ISUB = 73;
    int FSUB = 74;
    int IMUL = 75;
    int FMUL = 76;
    int IDIV = 77;
    int FDIV = 78;
    int IMOD = 79;
    int FMOD = 80;

    int ICMP = 100;
    int FCMP = 101;
    int SCMP = 102;
    int BCMP = 103;
    int IFEQ = 110;
    int IFNE = 111;
    int IFLT = 112;
    int IFGE = 113;
    int IFGT = 114;
    int IFLE = 115;

    int GOTO = 119;
    int CALL = 120;
    int ACALL = 121;
    int RET = 125;

    int INEWARRAY = 200;
    int FNEWARRAY = 201;
    int SNEWARRAY = 202;
    int BNEWARRAY = 203;
    int RNEWARRAY = 204;

    int NEWSTRUCT = 210;
    int NEWCONNECTOR = 211;

    int INSTRUCTION_CODE_COUNT = 212;
}
