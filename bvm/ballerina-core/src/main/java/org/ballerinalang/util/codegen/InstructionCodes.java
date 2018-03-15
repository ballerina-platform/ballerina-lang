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
    int CCONST = 5;
    int BTCONST = 6;
    int ICONST_0 = 7;
    int ICONST_1 = 8;
    int ICONST_2 = 9;
    int ICONST_3 = 10;
    int ICONST_4 = 11;
    int ICONST_5 = 12;
    int FCONST_0 = 13;
    int FCONST_1 = 14;
    int FCONST_2 = 15;
    int FCONST_3 = 16;
    int FCONST_4 = 17;
    int FCONST_5 = 18;
    int BCONST_0 = 19;
    int BCONST_1 = 20;
    int RCONST_NULL = 21;

    int BMOVE = 22;
    int IMOVE = 23;
    int FMOVE = 24;
    int SMOVE = 25;
    int LMOVE = 26;
    int RMOVE = 27;

    int CALOAD = 28;
    int BTALOAD = 29;
    int BALOAD = 30;
    int IALOAD = 31;
    int FALOAD = 32;
    int SALOAD = 33;
    int LALOAD = 34;
    int RALOAD = 35;
    int JSONALOAD = 36;

    int BGLOAD = 37;
    int IGLOAD = 38;
    int FGLOAD = 39;
    int SGLOAD = 40;
    int LGLOAD = 41;
    int RGLOAD = 42;

    int BFIELDLOAD = 43;
    int IFIELDLOAD = 44;
    int FFIELDLOAD = 45;
    int SFIELDLOAD = 46;
    int LFIELDLOAD = 47;
    int RFIELDLOAD = 48;

    int MAPLOAD = 49;
    int JSONLOAD = 50;
    int ENUMERATORLOAD = 51;

    int CASTORE = 52;
    int BTASTORE = 53;
    int BASTORE = 54;
    int IASTORE = 55;
    int FASTORE = 56;
    int SASTORE = 57;
    int LASTORE = 58;
    int RASTORE = 59;
    int JSONASTORE = 60;


    int BGSTORE = 61;
    int IGSTORE = 62;
    int FGSTORE = 63;
    int SGSTORE = 64;
    int LGSTORE = 65;
    int RGSTORE = 66;


    int BFIELDSTORE = 67;
    int IFIELDSTORE = 68;
    int FFIELDSTORE = 69;
    int SFIELDSTORE = 70;
    int LFIELDSTORE = 71;
    int RFIELDSTORE = 72;

    int MAPSTORE = 73;
    int JSONSTORE = 74;

    int IADD = 75;
    int FADD = 76;
    int SADD = 77;
    int XMLADD = 78;
    int ISUB = 79;
    int FSUB = 80;
    int IMUL = 81;
    int FMUL = 82;
    int IDIV = 83;
    int FDIV = 84;
    int IMOD = 85;
    int FMOD = 86;
    int INEG = 87;
    int FNEG = 88;
    int BNOT = 89;

    int BEQ = 90;
    int IEQ = 91;
    int FEQ = 92;
    int SEQ = 93;
    int REQ = 94;

    int BNE = 95;
    int INE = 96;
    int FNE = 97;
    int SNE = 98;
    int RNE = 99;

    int IGT = 100;
    int FGT = 101;

    int IGE = 102;
    int FGE = 103;

    int ILT = 104;
    int FLT = 105;

    int ILE = 106;
    int FLE = 107;

    int REQ_NULL = 108;
    int RNE_NULL = 109;

    int BR_TRUE = 110;
    int BR_FALSE = 111;

    int GOTO = 112;
    int HALT = 113;
    int TR_RETRY = 114;
    int CALL = 115;
    int VCALL = 116;
    int ACALL = 117;
    int THROW = 118;
    int ERRSTORE = 119;
    int FPCALL = 120;
    int FPLOAD = 121;
    int TCALL = 122;

    int SEQ_NULL = 123;
    int SNE_NULL = 124;

    // Type Conversion related instructions
    int I2C = 125;
    int I2F = 126;
    int I2S = 127;
    int I2B = 128;
    int I2JSON = 129;
    int C2I = 130;
    int C2F = 131;
    int C2S = 132;
    int F2I = 133;
    int F2C = 134;
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
    int ANY2SCONV = 158;

    // Type cast
    int I2ANY = 159;
    int C2ANY = 160;
    int BT2ANY = 161;
    int F2ANY = 162;
    int S2ANY = 163;
    int B2ANY = 164;
    int L2ANY = 165;
    int ANY2I = 166;
    int ANY2C = 167;
    int ANY2BT = 168;
    int ANY2F = 169;
    int ANY2S = 170;
    int ANY2B = 171;
    int ANY2L = 172;
    int ANY2JSON = 173;
    int ANY2XML = 174;
    int ANY2MAP = 175;
    int ANY2DT = 176;

    int ANY2E = 177;
    int ANY2T = 178;
    int ANY2CN = 179;
    int CHECKCAST = 180;
    int NULL2JSON = 181;

    int ANY2TYPE = 182;
    int S2JSONX = 183;
    int NULL2S = 184;

    int LOCK = 185;
    int UNLOCK = 186;

    // Transactions
    int TR_BEGIN = 187;
    int TR_END = 188;

    int WRKSEND = 189;
    int WRKRECEIVE = 190;
    int FORKJOIN = 191;

    int CNEWARRAY = 200;
    int BTNEWARRAY = 201;
    int BNEWARRAY = 202;
    int INEWARRAY = 203;
    int FNEWARRAY = 204;
    int SNEWARRAY = 205;
    int LNEWARRAY = 206;
    int RNEWARRAY = 207;
    int JSONNEWARRAY = 208;
    int ARRAYLEN = 209;
    int LENGTHOF = 210;

    int NEWSTRUCT = 211;
    int NEWCONNECTOR = 212;
    int NEWMAP = 213;
    int NEWJSON = 214;
    int NEWTABLE = 215;

    int NEW_INT_RANGE = 216;
    int ITR_NEW = 217;
    int ITR_HAS_NEXT = 218;
    int ITR_NEXT = 219;

    int I2BT = 220;
    int BT2I = 221;


    int BRET = 222;
    int IRET = 223;
    int FRET = 224;
    int SRET = 225;
    int LRET = 226;
    int RRET = 227;
    int RET = 228;

    int XML2XMLATTRS = 238;
    int XMLATTRS2MAP = 239;
    int XMLATTRLOAD = 240;
    int XMLATTRSTORE = 241;
    int S2QNAME = 242;
    int NEWQNAME = 243;
    int NEWXMLELEMENT = 244;
    int NEWXMLCOMMENT = 245;
    int NEWXMLTEXT = 246;
    int NEWXMLPI = 247;
    int XMLSTORE = 248;
    int XMLLOAD = 249;
    
    int TYPEOF = 250;
    int TYPELOAD = 251;

    int TEQ = 252;
    int TNE = 253;

    int INSTRUCTION_CODE_COUNT = 254;
}
