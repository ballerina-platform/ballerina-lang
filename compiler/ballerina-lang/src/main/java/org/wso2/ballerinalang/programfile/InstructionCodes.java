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
    int DCONST = 21;

    int IMOVE = 22;
    int FMOVE = 23;
    int SMOVE = 24;
    int BMOVE = 25;
    int RMOVE = 26;
    int DMOVE = 27;
    int BIALOAD = 28;
    int IALOAD = 29;
    int FALOAD = 30;
    int SALOAD = 31;
    int BALOAD = 32;
    int RALOAD = 33;
    int DALOAD = 34;
    int JSONALOAD = 35;

    int IGLOAD = 36;
    int FGLOAD = 37;
    int SGLOAD = 38;
    int BGLOAD = 39;
    int RGLOAD = 40;
    int DGLOAD = 41;

    int CHNRECEIVE = 42;
    int CHNSEND = 43;

    int MAPLOAD = 47;
    int JSONLOAD = 48;

    int COMPENSATE = 49;

    int BIASTORE = 50;
    int IASTORE = 51;
    int FASTORE = 52;
    int SASTORE = 53;
    int BASTORE = 54;
    int RASTORE = 55;
    int DASTORE = 56;
    int JSONASTORE = 57;

    int BIAND = 58;
    int IAND = 59;
    int BIOR = 60;
    int IOR = 61;

    int IGSTORE = 62;
    int FGSTORE = 63;
    int SGSTORE = 64;
    int BGSTORE = 65;
    int RGSTORE = 66;
    int DGSTORE = 67;

    int MAPSTORE = 74;
    int JSONSTORE = 75;

    int IADD = 76;
    int FADD = 77;
    int SADD = 78;
    int DADD = 79;

    int SCOPE_END = 80;
    int LOOP_COMPENSATE = 81;

    int XMLADD = 82;
    int ISUB = 83;
    int FSUB = 84;
    int DSUB = 85;
    int IMUL = 86;
    int FMUL = 87;
    int DMUL = 88;
    int IDIV = 89;
    int FDIV = 90;
    int DDIV = 91;
    int IMOD = 92;
    int FMOD = 93;
    int DMOD = 94;
    int INEG = 95;
    int FNEG = 96;
    int DNEG = 97;
    int BNOT = 98;

    int IEQ = 99;
    int FEQ = 100;
    int SEQ = 101;
    int BEQ = 102;
    int REQ = 103;
    int DEQ = 104;

    int INE = 105;
    int FNE = 106;
    int SNE = 107;
    int BNE = 108;
    int RNE = 109;
    int DNE = 110;

    int IGT = 111;
    int FGT = 112;
    int DGT = 113;

    int IGE = 114;
    int FGE = 115;
    int DGE = 116;

    int ILT = 117;
    int FLT = 118;
    int DLT = 119;

    int ILE = 120;
    int FLE = 121;
    int DLE = 122;

    int REQ_NULL = 123;
    int RNE_NULL = 124;

    int BR_TRUE = 125;
    int BR_FALSE = 126;

    int GOTO = 127;
    int HALT = 128;
    int TR_RETRY = 129;
    int CALL = 130;
    int VCALL = 131;
    int THROW = 133;
    int ERRSTORE = 134;
    int FPCALL = 135;
    int FPLOAD = 136;
    int VFPLOAD = 137;

    int SEQ_NULL = 138;
    int SNE_NULL = 139;

    // Type Conversion related instructions
    int I2F = 140;
    int I2S = 141;
    int I2B = 142;
    int I2D = 143;
    int F2I = 144;
    int F2S = 145;
    int F2B = 146;
    int F2D = 147;
    int S2I = 148;
    int S2F = 149;
    int S2B = 150;
    int S2D = 151;
    int B2I = 152;
    int B2F = 153;
    int B2S = 154;
    int B2D = 155;
    int D2I = 156;
    int D2F = 157;
    int D2S = 158;
    int D2B = 159;
    int DT2JSON = 160;
    int DT2XML = 161;
    int T2MAP = 162;
    int T2JSON = 163;
    int MAP2T = 164;
    int JSON2T = 165;
    int XML2S = 167;

    int BILSHIFT = 168;
    int BIRSHIFT = 169;
    int ILSHIFT = 170;
    int IRSHIFT = 171;

    // Type cast
    int I2ANY = 172;
    int F2ANY = 173;
    int S2ANY = 174;
    int B2ANY = 175;
    int D2ANY = 176;
    int ANY2I = 177;
    int ANY2F = 178;
    int ANY2S = 179;
    int ANY2B = 180;
    int ANY2D = 181;
    int ANY2JSON = 182;
    int ANY2XML = 183;
    int ANY2MAP = 184;
    int ANY2STM = 185;
    int ANY2DT = 186;
    int ANY2SCONV = 187;
    int ANY2BI = 188;
    int BI2ANY = 189;
    int ANY2E = 190;
    int ANY2T = 191;
    int ANY2C = 192;
    int CHECKCAST = 193;

    int ANY2TYPE = 194;

    int LOCK = 196;
    int UNLOCK = 197;

    // Transactions
    int TR_BEGIN = 198;
    int TR_END = 199;

    int WRKSEND = 200;
    int WRKRECEIVE = 201;
    int FORKJOIN = 202;
    
    int AWAIT = 203;

    int MAP2JSON = 204;
    int JSON2MAP = 205;

    int IS_ASSIGNABLE = 206;
    int O2JSON = 207;
    
    int ARRAY2JSON = 208;
    int JSON2ARRAY = 209;

    int BINEWARRAY = 210;
    int INEWARRAY = 211;
    int FNEWARRAY = 212;
    int SNEWARRAY = 213;
    int BNEWARRAY = 214;
    int RNEWARRAY = 215;
    int DNEWARRAY = 216;
    int LENGTHOF = 218;

    int NEWSTRUCT = 220;
    int NEWMAP = 222;
    int NEWTABLE = 225;
    int NEWSTREAM = 227;

    int NEW_INT_RANGE = 229;
    int ITR_NEW = 230;
    int ITR_HAS_NEXT = 231;
    int ITR_NEXT = 232;
    int INT_RANGE = 233;

    int I2BI = 234;
    int BI2I = 235;
    int BIXOR = 236;
    int IXOR = 237;
    int BACONST = 238;
    int IURSHIFT = 239;

    int IRET = 240;
    int FRET = 241;
    int DRET = 242;
    int SRET = 243;
    int BRET = 244;
    int RRET = 245;
    int RET = 246;

    int XML2XMLATTRS = 247;
    int XMLATTRS2MAP = 248;
    int XMLATTRLOAD = 249;
    int XMLATTRSTORE = 250;
    int S2QNAME = 251;
    int NEWQNAME = 252;
    int NEWXMLELEMENT = 253;
    int NEWXMLCOMMENT = 254;
    int NEWXMLTEXT = 255;
    int NEWXMLPI = 256;
    int XMLSEQSTORE = 257;
    int XMLSEQLOAD = 258;
    int XMLLOAD = 259;
    int XMLLOADALL = 260;
    int NEWXMLSEQ = 261;

    int TYPELOAD = 262;

    int TEQ = 263;
    int TNE = 264;

    int INSTRUCTION_CODE_COUNT = 265;
}
