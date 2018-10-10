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
    int DMOVE = 26;
    int RMOVE = 27;
    int BIALOAD = 28;
    int IALOAD = 29;
    int FALOAD = 30;
    int SALOAD = 31;
    int BALOAD = 32;
    int DALOAD = 33;
    int RALOAD = 34;
    int JSONALOAD = 35;

    int IGLOAD = 36;
    int FGLOAD = 37;
    int SGLOAD = 38;
    int BGLOAD = 39;
    int DGLOAD = 40;
    int RGLOAD = 41;

    int CHNRECEIVE = 42;
    int CHNSEND = 43;

    int MAPLOAD = 44;
    int JSONLOAD = 45;

    int COMPENSATE = 46;

    int BIASTORE = 47;
    int IASTORE = 48;
    int FASTORE = 49;
    int SASTORE = 50;
    int BASTORE = 51;
    int DASTORE = 52;
    int RASTORE = 53;
    int JSONASTORE = 54;

    int BIAND = 55;
    int IAND = 56;
    int BIOR = 57;
    int IOR = 58;

    int IGSTORE = 59;
    int FGSTORE = 60;
    int SGSTORE = 61;
    int BGSTORE = 62;
    int DGSTORE = 63;
    int RGSTORE = 64;

    int MAPSTORE = 65;
    int JSONSTORE = 66;

    int IADD = 67;
    int FADD = 68;
    int SADD = 69;
    int DADD = 70;

    int SCOPE_END = 71;
    int LOOP_COMPENSATE = 72;

    int XMLADD = 73;
    int ISUB = 74;
    int FSUB = 75;
    int DSUB = 76;
    int IMUL = 77;
    int FMUL = 78;
    int DMUL = 79;
    int IDIV = 80;
    int FDIV = 81;
    int DDIV = 82;
    int IMOD = 83;
    int FMOD = 84;
    int DMOD = 85;
    int INEG = 86;
    int FNEG = 87;
    int DNEG = 88;
    int BNOT = 89;

    int IEQ = 90;
    int FEQ = 91;
    int SEQ = 92;
    int BEQ = 93;
    int DEQ = 94;
    int REQ = 95;

    int INE = 96;
    int FNE = 97;
    int SNE = 98;
    int BNE = 99;
    int DNE = 100;
    int RNE = 101;

    int IGT = 102;
    int FGT = 103;
    int DGT = 104;

    int IGE = 105;
    int FGE = 106;
    int DGE = 107;

    int ILT = 108;
    int FLT = 109;
    int DLT = 110;

    int ILE = 111;
    int FLE = 112;
    int DLE = 113;

    int REQ_NULL = 114;
    int RNE_NULL = 115;

    int BR_TRUE = 116;
    int BR_FALSE = 117;

    int GOTO = 118;
    int HALT = 119;
    int TR_RETRY = 120;
    int CALL = 121;
    int VCALL = 122;
    int THROW = 123;
    int ERRSTORE = 124;
    int FPCALL = 125;
    int FPLOAD = 126;
    int VFPLOAD = 127;

    int SEQ_NULL = 128;
    int SNE_NULL = 129;

    // Type Conversion related instructions
    int I2F = 130;
    int I2S = 131;
    int I2B = 132;
    int I2D = 133;
    int F2I = 134;
    int F2S = 135;
    int F2B = 136;
    int F2D = 137;
    int S2I = 138;
    int S2F = 139;
    int S2B = 140;
    int S2D = 141;
    int B2I = 142;
    int B2F = 143;
    int B2S = 144;
    int B2D = 145;
    int D2I = 146;
    int D2F = 147;
    int D2S = 148;
    int D2B = 149;
    int DT2JSON = 150;
    int DT2XML = 151;
    int T2MAP = 152;
    int T2JSON = 153;
    int MAP2T = 154;
    int JSON2T = 155;
    int XML2S = 157;

    int BILSHIFT = 158;
    int BIRSHIFT = 159;
    int ILSHIFT = 160;
    int IRSHIFT = 161;

    // Type cast
    int I2ANY = 162;
    int F2ANY = 163;
    int S2ANY = 164;
    int B2ANY = 165;
    int D2ANY = 166;
    int ANY2I = 167;
    int ANY2F = 168;
    int ANY2S = 169;
    int ANY2B = 170;
    int ANY2D = 171;
    int ANY2JSON = 172;
    int ANY2XML = 173;
    int ANY2MAP = 174;
    int ANY2STM = 175;
    int ANY2DT = 176;
    int ANY2SCONV = 177;
    int ANY2BI = 178;
    int BI2ANY = 179;
    int ANY2E = 180;
    int ANY2T = 181;
    int ANY2C = 182;
    int CHECKCAST = 183;

    int ANY2TYPE = 184;

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
    int O2JSON = 197;
    
    int ARRAY2JSON = 198;
    int JSON2ARRAY = 199;

    int BINEWARRAY = 200;
    int INEWARRAY = 201;
    int FNEWARRAY = 202;
    int SNEWARRAY = 203;
    int BNEWARRAY = 204;
    int DNEWARRAY = 205;
    int RNEWARRAY = 206;
    int LENGTHOF = 208;

    int NEWSTRUCT = 210;
    int NEWMAP = 212;
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
    int IURSHIFT = 229;

    int IRET = 230;
    int FRET = 231;
    int SRET = 232;
    int BRET = 233;
    int DRET = 234;
    int RRET = 235;
    int RET = 236;

    int XML2XMLATTRS = 237;
    int XMLATTRS2MAP = 238;
    int XMLATTRLOAD = 239;
    int XMLATTRSTORE = 240;
    int S2QNAME = 241;
    int NEWQNAME = 242;
    int NEWXMLELEMENT = 243;
    int NEWXMLCOMMENT = 244;
    int NEWXMLTEXT = 245;
    int NEWXMLPI = 246;
    int XMLSEQSTORE = 247;
    int XMLSEQLOAD = 248;
    int XMLLOAD = 249;
    int XMLLOADALL = 250;
    int NEWXMLSEQ = 251;

    int TYPELOAD = 252;

    int TEQ = 253;
    int TNE = 254;

    int INSTRUCTION_CODE_COUNT = 255;
}
