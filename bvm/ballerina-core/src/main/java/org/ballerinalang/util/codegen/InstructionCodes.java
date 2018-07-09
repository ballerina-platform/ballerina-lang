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
    int BICONST = 20;

    int IMOVE = 21;
    int FMOVE = 22;
    int SMOVE = 23;
    int BMOVE = 24;
    int RMOVE = 25;
    int BIALOAD = 26;
    int IALOAD = 27;
    int FALOAD = 28;
    int SALOAD = 29;
    int BALOAD = 30;
    int RALOAD = 31;
    int JSONALOAD = 32;

    int IGLOAD = 33;
    int FGLOAD = 34;
    int SGLOAD = 35;
    int BGLOAD = 36;
    int RGLOAD = 37;

    int MAPLOAD = 47;
    int JSONLOAD = 48;

    int COMPENSATE = 49;

    int BIASTORE = 50;
    int IASTORE = 51;
    int FASTORE = 52;
    int SASTORE = 53;
    int BASTORE = 54;
    int RASTORE = 55;
    int JSONASTORE = 56;

    int BIAND = 57;
    int IAND = 58;
    int BIOR = 59;
    int IOR = 60;

    int IGSTORE = 61;
    int FGSTORE = 62;
    int SGSTORE = 63;
    int BGSTORE = 64;
    int RGSTORE = 65;

    int MAPSTORE = 74;
    int JSONSTORE = 75;

    int IADD = 76;
    int FADD = 77;
    int SADD = 78;

    int SCOPE_END = 79;
    int LOOP_COMPENSATE = 80;

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

    int S2XML = 156;
    int XML2S = 157;

    int BISHL = 158;
    int BISHR = 159;
    int BIUSHR = 160;

    // Type cast
    int I2ANY = 162;
    int F2ANY = 163;
    int S2ANY = 164;
    int B2ANY = 165;
    int ANY2I = 166;
    int ANY2F = 167;
    int ANY2S = 168;
    int ANY2B = 169;
    int ANY2JSON = 170;
    int ANY2XML = 171;
    int ANY2MAP = 172;
    int ANY2STM = 173;
    int ANY2DT = 174;
    int ANY2SCONV = 175;
    int ANY2BI = 176;
    int BI2ANY = 177;
    int ANY2E = 178;
    int ANY2T = 179;
    int ANY2C = 180;
    int CHECKCAST = 181;
    int NULL2JSON = 182;

    int ANY2TYPE = 183;
    int NULL2S = 185;

    int LOCK = 186;
    int UNLOCK = 187;

    // Transactions
    int TR_BEGIN = 188;
    int TR_END = 189;

    int WRKSEND = 190;
    int WRKRECEIVE = 191;
    int FORKJOIN = 192;
    
    int AWAIT = 193;

    int MAP2JSON = 194;
    int JSON2MAP = 195;

    int IS_ASSIGNABLE = 196;
    int CHECK_CONVERSION = 197;
    
    int ARRAY2JSON = 198;
    int JSON2ARRAY = 199;

    int BINEWARRAY = 200;
    int INEWARRAY = 201;
    int FNEWARRAY = 202;
    int SNEWARRAY = 203;
    int BNEWARRAY = 204;
    int RNEWARRAY = 205;
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
    int INT_RANGE = 223;

    int I2BI = 224;
    int BI2I = 225;
    int BIXOR = 226;
    int IXOR = 227;
    int BACONST = 228;

    int IRET = 230;
    int FRET = 231;
    int SRET = 232;
    int BRET = 233;
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
