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
    int igload = 32;
    int fgload = 33;
    int sgload = 34;
    int bgload = 35;
    int rgload = 36;

    int istore = 37;
    int fstore = 38;
    int sstore = 39;
    int bstore = 40;
    int rstore = 41;
    int iastore = 42;
    int fastore = 43;
    int sastore = 44;
    int bastore = 45;
    int rastore = 46;
    int igstore = 47;
    int fgstore = 48;
    int sgstore = 49;
    int bgstore = 50;
    int rgstore = 51;

    int ifieldload = 52;
    int ffieldload = 53;
    int sfieldload = 54;
    int bfieldload = 55;
    int rfieldload = 56;

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
    int acall = 121;
    int ret = 125;

    int inewarray = 200;
    int fnewarray = 201;
    int snewarray = 202;
    int bnewarray = 203;
    int rnewarray = 204;

    int newstruct = 210;
    int newconnector = 211;

    int InstructionCodeCount = 212;
}
