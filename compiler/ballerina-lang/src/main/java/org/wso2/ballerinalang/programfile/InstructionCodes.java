/*
 *  Copyright (c) DADD + 1, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
@Deprecated
public interface InstructionCodes {

    int NOP = 0;
    int ICONST = NOP + 1;
    int FCONST = ICONST + 1;
    int SCONST = FCONST + 1;
    int ICONST_0 = SCONST + 1;
    int ICONST_1 = ICONST_0 + 1;
    int ICONST_2 = ICONST_1 + 1;
    int ICONST_3 = ICONST_2 + 1;
    int ICONST_4 = ICONST_3 + 1;
    int ICONST_5 = ICONST_4 + 1;
    int FCONST_0 = ICONST_5 + 1;
    int FCONST_1 = FCONST_0 + 1;
    int FCONST_2 = FCONST_1 + 1;
    int FCONST_3 = FCONST_2 + 1;
    int FCONST_4 = FCONST_3 + 1;
    int FCONST_5 = FCONST_4 + 1;
    int BCONST_0 = FCONST_5 + 1;
    int BCONST_1 = BCONST_0 + 1;
    int RCONST_NULL = BCONST_1 + 1;
    int BICONST = RCONST_NULL + 1;
    int DCONST = BICONST + 1;
    int MCONST = DCONST + 1;
    int IMOVE = MCONST + 1;
    int FMOVE = IMOVE + 1;
    int SMOVE = FMOVE + 1;
    int BMOVE = SMOVE + 1;
    int RMOVE = BMOVE + 1;
    int BIALOAD = RMOVE + 1;
    int IALOAD = BIALOAD + 1;
    int FALOAD = IALOAD + 1;
    int SALOAD = FALOAD + 1;
    int BALOAD = SALOAD + 1;
    int RALOAD = BALOAD + 1;
    int JSONALOAD = RALOAD + 1;

    int IGLOAD = JSONALOAD + 1;
    int FGLOAD = IGLOAD + 1;
    int SGLOAD = FGLOAD + 1;
    int BGLOAD = SGLOAD + 1;
    int RGLOAD = BGLOAD + 1;

    int CHNRECEIVE = RGLOAD + 1;
    int CHNSEND = CHNRECEIVE + 1;

    int MAPLOAD = CHNSEND + 1;
    int JSONLOAD = MAPLOAD + 1;

    int COMPENSATE = JSONLOAD + 1;

    int BIASTORE = COMPENSATE + 1;
    int IASTORE = BIASTORE + 1;
    int FASTORE = IASTORE + 1;
    int SASTORE = FASTORE + 1;
    int BASTORE = SASTORE + 1;
    int RASTORE = BASTORE + 1;
    int JSONASTORE = RASTORE + 1;

    int IAND = JSONASTORE + 1;
    int IOR = IAND + 1;

    int IGSTORE = IOR + 1;
    int FGSTORE = IGSTORE + 1;
    int SGSTORE = FGSTORE + 1;
    int BGSTORE = SGSTORE + 1;
    int RGSTORE = BGSTORE + 1;

    int IS_LIKE = RGSTORE + 1;

    int ERROR = IS_LIKE + 1;
    int PANIC = ERROR + 1;

    int MAPSTORE = PANIC + 1;
    int JSONSTORE = MAPSTORE + 1;

    int IADD = JSONSTORE + 1;
    int FADD = IADD + 1;
    int SADD = FADD + 1;
    int DADD = SADD + 1;

    int SCOPE_END = DADD + 1;
    int LOOP_COMPENSATE = SCOPE_END + 1;

    int XMLADD = LOOP_COMPENSATE + 1;
    int ISUB = XMLADD + 1;
    int FSUB = ISUB + 1;
    int DSUB = FSUB + 1;
    int IMUL = DSUB + 1;
    int FMUL = IMUL + 1;
    int DMUL = FMUL + 1;
    int IDIV = DMUL + 1;
    int FDIV = IDIV + 1;
    int DDIV = FDIV + 1;
    int IMOD = DDIV + 1;
    int FMOD = IMOD + 1;
    int DMOD = FMOD + 1;
    int INEG = DMOD + 1;
    int FNEG = INEG + 1;
    int DNEG = FNEG + 1;
    int BNOT = DNEG + 1;

    int IEQ = BNOT + 1;
    int FEQ = IEQ + 1;
    int SEQ = FEQ + 1;
    int BEQ = SEQ + 1;
    int DEQ = BEQ + 1;
    int REQ = DEQ + 1;
    int REF_EQ = REQ + 1;

    int INE = REF_EQ + 1;
    int FNE = INE + 1;
    int SNE = FNE + 1;
    int BNE = SNE + 1;
    int DNE = BNE + 1;
    int RNE = DNE + 1;
    int REF_NEQ = RNE + 1;

    int IGT = REF_NEQ + 1;
    int FGT = IGT + 1;
    int DGT = FGT + 1;

    int IGE = DGT + 1;
    int FGE = IGE + 1;
    int DGE = FGE + 1;

    int ILT = DGE + 1;
    int FLT = ILT + 1;
    int DLT = FLT + 1;

    int ILE = DLT + 1;
    int FLE = ILE + 1;
    int DLE = FLE + 1;

    int REQ_NULL = DLE + 1;
    int RNE_NULL = REQ_NULL + 1;

    int BR_TRUE = RNE_NULL + 1;
    int BR_FALSE = BR_TRUE + 1;

    int GOTO = BR_FALSE + 1;
    int HALT = GOTO + 1;
    int TR_RETRY = HALT + 1;
    int CALL = TR_RETRY + 1;
    int VCALL = CALL + 1;
    int FPCALL = VCALL + 1;
    int FPLOAD = FPCALL + 1;
    int VFPLOAD = FPLOAD + 1;

    // Type Conversion related instructions
    int I2F = VFPLOAD + 1;
    int I2S = I2F + 1;
    int I2B = I2S + 1;
    int I2D = I2B + 1;
    int F2I = I2D + 1;
    int F2S = F2I + 1;
    int F2B = F2S + 1;
    int F2D = F2B + 1;
    int S2I = F2D + 1;
    int S2F = S2I + 1;
    int S2B = S2F + 1;
    int S2D = S2B + 1;
    int B2I = S2D + 1;
    int B2F = B2I + 1;
    int B2S = B2F + 1;
    int B2D = B2S + 1;
    int D2I = B2D + 1;
    int D2F = D2I + 1;
    int D2S = D2F + 1;
    int D2B = D2S + 1;
    int DT2JSON = D2B + 1;
    int DT2XML = DT2JSON + 1;
    int T2MAP = DT2XML + 1;
    int T2JSON = T2MAP + 1;
    int MAP2T = T2JSON + 1;
    int JSON2T = MAP2T + 1;
    int XML2S = JSON2T + 1;

    int BILSHIFT = XML2S + 1;
    int BIRSHIFT = BILSHIFT + 1;
    int ILSHIFT = BIRSHIFT + 1;
    int IRSHIFT = ILSHIFT + 1;

    // Type cast
    int I2ANY = IRSHIFT + 1;
    int F2ANY = I2ANY + 1;
    int S2ANY = F2ANY + 1;
    int B2ANY = S2ANY + 1;

    int TYPE_CAST = B2ANY + 1;

    int ANY2I = TYPE_CAST + 1;
    int ANY2F = ANY2I + 1;
    int ANY2S = ANY2F + 1;
    int ANY2B = ANY2S + 1;
    int ANY2D = ANY2B + 1;
    int ANY2JSON = ANY2D + 1;
    int ANY2XML = ANY2JSON + 1;
    int ANY2MAP = ANY2XML + 1;
    int ANY2STM = ANY2MAP + 1;
    int ANY2DT = ANY2STM + 1;
    int ANY2SCONV = ANY2DT + 1;
    int ANY2BI = ANY2SCONV + 1;
    int BI2ANY = ANY2BI + 1;
    int ANY2E = BI2ANY + 1;
    int ANY2T = ANY2E + 1;
    int ANY2C = ANY2T + 1;
    int CHECKCAST = ANY2C + 1;

    int ANY2TYPE = CHECKCAST + 1;

    int LOCK = ANY2TYPE + 1;
    int UNLOCK = LOCK + 1;

    // Transactions
    int TR_BEGIN = UNLOCK + 1;
    int TR_END = TR_BEGIN + 1;

    int WRKSEND = TR_END + 1;
    int WRKRECEIVE = WRKSEND + 1;

    int WORKERSYNCSEND = WRKRECEIVE + 1;
    int WAIT = WORKERSYNCSEND + 1;

    int MAP2JSON = WAIT + 1;
    int JSON2MAP = MAP2JSON + 1;

    int IS_ASSIGNABLE = JSON2MAP + 1;
    int O2JSON = IS_ASSIGNABLE + 1;

    int ARRAY2JSON = O2JSON + 1;
    int JSON2ARRAY = ARRAY2JSON + 1;

    int BINEWARRAY = JSON2ARRAY + 1;
    int INEWARRAY = BINEWARRAY + 1;
    int FNEWARRAY = INEWARRAY + 1;
    int SNEWARRAY = FNEWARRAY + 1;
    int BNEWARRAY = SNEWARRAY + 1;
    int RNEWARRAY = BNEWARRAY + 1;
    
    int FLUSH = RNEWARRAY + 1;

    int WAITALL = FLUSH + 1;

    int NEWSTRUCT = WAITALL + 1;
    int NEWMAP = NEWSTRUCT + 1;
    int NEWTABLE = NEWMAP + 1;
    int NEWSTREAM = NEWTABLE + 1;
    
    int ITR_NEW = NEWSTREAM + 1;
    int ITR_NEXT = ITR_NEW + 1;
    int INT_RANGE = ITR_NEXT + 1;

    int I2BI = INT_RANGE + 1;
    int F2BI = I2BI + 1;
    int D2BI = F2BI + 1;
    int IXOR = D2BI + 1;
    int BACONST = IXOR + 1;
    int IURSHIFT = BACONST + 1;

    int IRET = IURSHIFT + 1;
    int FRET = IRET + 1;
    int SRET = FRET + 1;
    int BRET = SRET + 1;
    int DRET = BRET + 1;
    int RRET = DRET + 1;
    int RET = RRET + 1;

    int XML2XMLATTRS = RET + 1;
    int XMLATTRS2MAP = XML2XMLATTRS + 1;
    int XMLATTRLOAD = XMLATTRS2MAP + 1;
    int XMLATTRSTORE = XMLATTRLOAD + 1;
    int S2QNAME = XMLATTRSTORE + 1;
    int NEWQNAME = S2QNAME + 1;
    int NEWXMLELEMENT = NEWQNAME + 1;
    int NEWXMLCOMMENT = NEWXMLELEMENT + 1;
    int NEWXMLTEXT = NEWXMLCOMMENT + 1;
    int NEWXMLPI = NEWXMLTEXT + 1;
    int XMLSEQSTORE = NEWXMLPI + 1;
    int XMLSEQLOAD = XMLSEQSTORE + 1;
    int XMLLOAD = XMLSEQLOAD + 1;
    int XMLLOADALL = XMLLOAD + 1;
    int NEWXMLSEQ = XMLLOADALL + 1;

    int TYPE_TEST = NEWXMLSEQ + 1;
    int TYPELOAD = TYPE_TEST + 1;

    int TEQ = TYPELOAD + 1;
    int TNE = TEQ + 1;

    int ANNOT_ACCESS = TNE + 1;

    int INSTRUCTION_CODE_COUNT = 255;
}
