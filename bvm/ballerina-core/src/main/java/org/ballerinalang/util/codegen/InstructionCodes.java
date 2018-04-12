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
 * Bytecode instructions of a compiled Ballerina program.
 *
 * @since 0.87
 */
public interface InstructionCodes {

    int NOP = 0;
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
    int RCONST_NULL = 19;

    int IMOVE = 21;
    int FMOVE = 22;
    int SMOVE = 23;
    int BMOVE = 24;
    int LMOVE = 25;
    int RMOVE = 26;
    int IALOAD = 27;
    int FALOAD = 28;
    int SALOAD = 29;
    int BALOAD = 30;
    int LALOAD = 31;
    int RALOAD = 32;
    int JSONALOAD = 33;
    int IGLOAD = 34;
    int FGLOAD = 35;
    int SGLOAD = 36;
    int BGLOAD = 37;
    int LGLOAD = 38;
    int RGLOAD = 39;

    int IFIELDLOAD = 40;
    int FFIELDLOAD = 41;
    int SFIELDLOAD = 42;
    int BFIELDLOAD = 43;
    int LFIELDLOAD = 44;
    int RFIELDLOAD = 45;

    int MAPLOAD = 46;
    int JSONLOAD = 47;

    int IASTORE = 55;
    int FASTORE = 56;
    int SASTORE = 57;
    int BASTORE = 58;
    int LASTORE = 59;
    int RASTORE = 60;
    int JSONASTORE = 61;
    int IGSTORE = 62;
    int FGSTORE = 63;
    int SGSTORE = 64;
    int BGSTORE = 65;
    int LGSTORE = 66;
    int RGSTORE = 67;

    int IFIELDSTORE = 68;
    int FFIELDSTORE = 69;
    int SFIELDSTORE = 70;
    int BFIELDSTORE = 71;
    int LFIELDSTORE = 72;
    int RFIELDSTORE = 73;

    int MAPSTORE = 74;
    int JSONSTORE = 75;

    int IADD = 76;
    int FADD = 77;
    int SADD = 78;
    int XMLADD = 81;
    int ISUB = 82;
    int FSUB = 83;
    int IMUL = 84;
    int FMUL = 85;
    int IDIV = 86;
    int FDIV = 87;
    int IMOD = 88;
    int FMOD = 89;
    int INEG = 90;
    int FNEG = 91;
    int BNOT = 92;

    int IEQ = 93;
    int FEQ = 94;
    int SEQ = 95;
    int BEQ = 96;
    int REQ = 98;

    int INE = 99;
    int FNE = 100;
    int SNE = 101;
    int BNE = 102;
    int RNE = 104;

    int IGT = 105;
    int FGT = 106;

    int IGE = 107;
    int FGE = 108;

    int ILT = 109;
    int FLT = 110;

    int ILE = 111;
    int FLE = 112;

    int REQ_NULL = 113;
    int RNE_NULL = 114;

    int BR_TRUE = 115;
    int BR_FALSE = 116;

    int GOTO = 117;
    int HALT = 118;
    int TR_RETRY = 119;
    int CALL = 120;
    int VCALL = 121;
    int THROW = 123;
    int ERRSTORE = 124;
    int FPCALL = 125;
    int FPLOAD = 126;
    int TCALL = 127;

    int SEQ_NULL = 128;
    int SNE_NULL = 129;

    // Type Conversion related instructions
    int I2F = 130;
    int I2S = 131;
    int I2B = 132;
    int I2JSON = 133;
    int F2I = 134;
    int F2S = 135;
    int F2B = 136;
    int F2JSON = 137;
    int S2I = 138;
    int S2F = 139;
    int S2B = 140;
    int S2JSON = 141;
    int B2I = 142;
    int B2F = 143;
    int B2S = 144;
    int B2JSON = 145;
    int JSON2I = 146;
    int JSON2F = 147;
    int JSON2S = 148;
    int JSON2B = 149;
    int DT2JSON = 150;
    int DT2XML = 151;
    int T2MAP = 152;
    int T2JSON = 153;
    int MAP2T = 154;
    int JSON2T = 155;
    int XML2JSON = 156;
    int JSON2XML = 157;
    int S2XML = 158;
    int XML2S = 159;
    int ANY2SCONV = 175;

    // Type cast
    int I2ANY = 160;
    int F2ANY = 161;
    int S2ANY = 162;
    int B2ANY = 163;
    int L2ANY = 164;
    int ANY2I = 165;
    int ANY2F = 166;
    int ANY2S = 167;
    int ANY2B = 168;
    int ANY2L = 169;
    int ANY2JSON = 170;
    int ANY2XML = 171;
    int ANY2MAP = 172;
    int ANY2STM = 173;
    int ANY2DT = 174;

    int ANY2E = 177;
    int ANY2T = 178;
    int ANY2C = 179;
    int CHECKCAST = 180;
    int NULL2JSON = 181;

    int ANY2TYPE = 182;
    int S2JSONX = 183;
    int NULL2S = 184;
    int MAP2JSON = 194;
    int JSON2MAP = 195;

    int LOCK = 185;
    int UNLOCK = 186;

    // Transactions
    int TR_BEGIN = 188;
    int TR_END = 189;

    int WRKSEND = 190;
    int WRKRECEIVE = 191;
    int FORKJOIN = 192;
    
    int AWAIT = 193;

    int IS_ASSIGNABLE = 196;
    int CHECK_CONVERSION = 197;
    
    int ARRAY2JSON = 198;
    int JSON2ARRAY = 199;

    int INEWARRAY = 200;
    int FNEWARRAY = 201;
    int SNEWARRAY = 202;
    int BNEWARRAY = 203;
    int LNEWARRAY = 204;
    int RNEWARRAY = 205;
    int JSONNEWARRAY = 206;
    int ARRAYLEN = 207;
    int LENGTHOF = 208;

    int NEWSTRUCT = 210;
    int NEWMAP = 212;
    int NEWJSON = 213;
    int NEWTABLE = 215;
    int NEWSTREAM = 217;

    int NEW_INT_RANGE = 219;
    int ITR_NEW = 220;
    int ITR_HAS_NEXT = 221;
    int ITR_NEXT = 222;

    int IRET = 229;
    int FRET = 230;
    int SRET = 231;
    int BRET = 232;
    int LRET = 233;
    int RRET = 234;
    int RET = 235;

    int XML2XMLATTRS = 236;
    int XMLATTRS2MAP = 237;
    int XMLATTRLOAD = 238;
    int XMLATTRSTORE = 239;
    int S2QNAME = 240;
    int NEWQNAME = 241;
    int NEWXMLELEMENT = 242;
    int NEWXMLCOMMENT = 243;
    int NEWXMLTEXT = 244;
    int NEWXMLPI = 245;
    int XMLSEQSTORE = 246;
    int XMLSEQLOAD = 247;
    int XMLLOAD = 248;
    int XMLLOADALL = 249;
    int NEWXMLSEQ = 250;

    int TYPEOF = 251;
    int TYPELOAD = 252;

    int TEQ = 253;
    int TNE = 254;

    int INSTRUCTION_CODE_COUNT = 255;
}
