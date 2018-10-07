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
 * @since 0.87
 */
public class Mnemonics {
    private static final String[] mnemonics = new String[InstructionCodes.INSTRUCTION_CODE_COUNT];

    static {
        mnemonics[InstructionCodes.ICONST] = "iconst";
        mnemonics[InstructionCodes.FCONST] = "fconst";
        mnemonics[InstructionCodes.SCONST] = "sconst";
        mnemonics[InstructionCodes.ICONST_0] = "iconst_0";
        mnemonics[InstructionCodes.ICONST_1] = "iconst_1";
        mnemonics[InstructionCodes.ICONST_2] = "iconst_2";
        mnemonics[InstructionCodes.ICONST_3] = "iconst_3";
        mnemonics[InstructionCodes.ICONST_4] = "iconst_4";
        mnemonics[InstructionCodes.ICONST_5] = "iconst_5";
        mnemonics[InstructionCodes.FCONST_0] = "fconst_0";
        mnemonics[InstructionCodes.FCONST_1] = "fconst_1";
        mnemonics[InstructionCodes.FCONST_2] = "fconst_2";
        mnemonics[InstructionCodes.FCONST_3] = "fconst_3";
        mnemonics[InstructionCodes.FCONST_4] = "fconst_4";
        mnemonics[InstructionCodes.FCONST_5] = "fconst_5";
        mnemonics[InstructionCodes.BCONST_0] = "bconst_0";
        mnemonics[InstructionCodes.BCONST_1] = "bconst_1";
        mnemonics[InstructionCodes.RCONST_NULL] = "rconst_null";
        mnemonics[InstructionCodes.BICONST] = "biconst";

        mnemonics[InstructionCodes.IMOVE] = "imove";
        mnemonics[InstructionCodes.FMOVE] = "fmove";
        mnemonics[InstructionCodes.SMOVE] = "smove";
        mnemonics[InstructionCodes.BMOVE] = "bmove";
        mnemonics[InstructionCodes.RMOVE] = "rmove";
        mnemonics[InstructionCodes.BIALOAD] = "biaload";
        mnemonics[InstructionCodes.IALOAD] = "iaload";
        mnemonics[InstructionCodes.FALOAD] = "faload";
        mnemonics[InstructionCodes.SALOAD] = "saload";
        mnemonics[InstructionCodes.BALOAD] = "baload";
        mnemonics[InstructionCodes.RALOAD] = "raload";
        mnemonics[InstructionCodes.JSONALOAD] = "jsonaload";
        mnemonics[InstructionCodes.IGLOAD] = "igload";
        mnemonics[InstructionCodes.FGLOAD] = "fgload";
        mnemonics[InstructionCodes.SGLOAD] = "sgload";
        mnemonics[InstructionCodes.BGLOAD] = "bgload";
        mnemonics[InstructionCodes.RGLOAD] = "rgload";

        mnemonics[InstructionCodes.BIASTORE] = "biastore";
        mnemonics[InstructionCodes.IASTORE] = "iastore";
        mnemonics[InstructionCodes.FASTORE] = "fastore";
        mnemonics[InstructionCodes.SASTORE] = "sastore";
        mnemonics[InstructionCodes.BASTORE] = "bastore";
        mnemonics[InstructionCodes.RASTORE] = "rastore";
        mnemonics[InstructionCodes.JSONASTORE] = "jsonastore";
        mnemonics[InstructionCodes.IGSTORE] = "igstore";
        mnemonics[InstructionCodes.FGSTORE] = "fgstore";
        mnemonics[InstructionCodes.SGSTORE] = "sgstore";
        mnemonics[InstructionCodes.BGSTORE] = "bgstore";
        mnemonics[InstructionCodes.RGSTORE] = "rgstore";

        mnemonics[InstructionCodes.MAPLOAD] = "mapload";
        mnemonics[InstructionCodes.MAPSTORE] = "mapstore";
        mnemonics[InstructionCodes.JSONLOAD] = "jsonload";
        mnemonics[InstructionCodes.JSONSTORE] = "jsonstore";

        mnemonics[InstructionCodes.IADD] = "iadd";
        mnemonics[InstructionCodes.FADD] = "fadd";
        mnemonics[InstructionCodes.SADD] = "sadd";
        mnemonics[InstructionCodes.XMLADD] = "radd";
        mnemonics[InstructionCodes.IAND] = "iand";
        mnemonics[InstructionCodes.BIAND] = "biand";
        mnemonics[InstructionCodes.IOR] = "ior";
        mnemonics[InstructionCodes.BIOR] = "bior";
        mnemonics[InstructionCodes.IXOR] = "ior";
        mnemonics[InstructionCodes.BIXOR] = "bior";
        mnemonics[InstructionCodes.BILSHIFT] = "bilshift";
        mnemonics[InstructionCodes.BIRSHIFT] = "birshift";
        mnemonics[InstructionCodes.IRSHIFT] = "irshift";
        mnemonics[InstructionCodes.ILSHIFT] = "ilshift";
        mnemonics[InstructionCodes.IURSHIFT] = "iurshift";
        mnemonics[InstructionCodes.BACONST] = "baconst";

        mnemonics[InstructionCodes.ISUB] = "isub";
        mnemonics[InstructionCodes.FSUB] = "fsub";
        mnemonics[InstructionCodes.SADD] = "sadd";
        mnemonics[InstructionCodes.IMUL] = "imul";
        mnemonics[InstructionCodes.FMUL] = "fmul";
        mnemonics[InstructionCodes.IDIV] = "idiv";
        mnemonics[InstructionCodes.FDIV] = "fdiv";
        mnemonics[InstructionCodes.IMOD] = "imod";
        mnemonics[InstructionCodes.FMOD] = "fmod";
        mnemonics[InstructionCodes.INEG] = "ineg";
        mnemonics[InstructionCodes.FNEG] = "fneg";
        mnemonics[InstructionCodes.BNOT] = "bnot";

        mnemonics[InstructionCodes.IEQ] = "ieq";
        mnemonics[InstructionCodes.FEQ] = "feq";
        mnemonics[InstructionCodes.SEQ] = "seq";
        mnemonics[InstructionCodes.BEQ] = "beq";
        mnemonics[InstructionCodes.REQ] = "req";
        mnemonics[InstructionCodes.TEQ] = "teq";
        mnemonics[InstructionCodes.INE] = "ine";
        mnemonics[InstructionCodes.FNE] = "fne";
        mnemonics[InstructionCodes.SNE] = "sne";
        mnemonics[InstructionCodes.BNE] = "bne";
        mnemonics[InstructionCodes.RNE] = "rne";
        mnemonics[InstructionCodes.TNE] = "tne";

        mnemonics[InstructionCodes.IGT] = "igt";
        mnemonics[InstructionCodes.FGT] = "fgt";
        mnemonics[InstructionCodes.IGE] = "ige";
        mnemonics[InstructionCodes.FGE] = "fge";
        mnemonics[InstructionCodes.ILT] = "ilt";
        mnemonics[InstructionCodes.FLT] = "flt";
        mnemonics[InstructionCodes.ILE] = "ile";
        mnemonics[InstructionCodes.FLE] = "fle";

        mnemonics[InstructionCodes.REQ_NULL] = "reg_null";
        mnemonics[InstructionCodes.RNE_NULL] = "rne_null";
        mnemonics[InstructionCodes.BR_TRUE] = "br_true";
        mnemonics[InstructionCodes.BR_FALSE] = "br_false";

        mnemonics[InstructionCodes.GOTO] = "goto";
        mnemonics[InstructionCodes.HALT] = "halt";
        mnemonics[InstructionCodes.TR_RETRY] = "tr_retry";
        mnemonics[InstructionCodes.CALL] = "call";
        mnemonics[InstructionCodes.VCALL] = "ncall";
        mnemonics[InstructionCodes.THROW] = "throw";
        mnemonics[InstructionCodes.ERRSTORE] = "errstore";
        mnemonics[InstructionCodes.FPCALL] = "fp_call";
        mnemonics[InstructionCodes.FPLOAD] = "fp_load";

        mnemonics[InstructionCodes.I2F] = "i2f";
        mnemonics[InstructionCodes.I2S] = "i2s";
        mnemonics[InstructionCodes.I2B] = "i2b";
        mnemonics[InstructionCodes.I2ANY] = "i2any";
        mnemonics[InstructionCodes.I2BI] = "i2bi";
        mnemonics[InstructionCodes.BI2I] = "bi2i";
        mnemonics[InstructionCodes.BI2ANY] = "bi2any";
        mnemonics[InstructionCodes.F2I] = "f2i";
        mnemonics[InstructionCodes.F2S] = "f2s";
        mnemonics[InstructionCodes.F2B] = "f2b";
        mnemonics[InstructionCodes.F2ANY] = "f2any";
        mnemonics[InstructionCodes.S2I] = "s2i";
        mnemonics[InstructionCodes.S2F] = "s2f";
        mnemonics[InstructionCodes.S2B] = "s2b";
        mnemonics[InstructionCodes.S2ANY] = "s2any";
        mnemonics[InstructionCodes.B2I] = "b2i";
        mnemonics[InstructionCodes.B2F] = "b2f";
        mnemonics[InstructionCodes.B2S] = "b2s";
        mnemonics[InstructionCodes.B2ANY] = "b2any";
        mnemonics[InstructionCodes.ANY2SCONV] = "any2sconv";
        mnemonics[InstructionCodes.LENGTHOF] = "lengthof";

        mnemonics[InstructionCodes.TYPELOAD] = "typeload";

        // Type cast
        mnemonics[InstructionCodes.ANY2I] = "any2i";
        mnemonics[InstructionCodes.ANY2BI] = "any2bi";
        mnemonics[InstructionCodes.ANY2F] = "any2f";
        mnemonics[InstructionCodes.ANY2S] = "any2s";
        mnemonics[InstructionCodes.ANY2B] = "any2b";
        mnemonics[InstructionCodes.ANY2JSON] = "any2json";
        mnemonics[InstructionCodes.ANY2XML] = "any2xml";
        mnemonics[InstructionCodes.ANY2TYPE] = "any2type";
        mnemonics[InstructionCodes.ANY2T] = "any2t";
        mnemonics[InstructionCodes.ANY2MAP] = "any2map";
        mnemonics[InstructionCodes.CHECKCAST] = "checkcast";
        mnemonics[InstructionCodes.DT2JSON] = "dt2json";
        mnemonics[InstructionCodes.DT2XML] = "dt2xml";
        mnemonics[InstructionCodes.MAP2JSON] = "map2json";
        mnemonics[InstructionCodes.JSON2MAP] = "json2map";
        mnemonics[InstructionCodes.O2JSON] = "o2json";

        // Transactions
        mnemonics[InstructionCodes.TR_BEGIN] = "tr_begin";
        mnemonics[InstructionCodes.TR_END] = "tr_end";

        mnemonics[InstructionCodes.WRKSEND] = "wrksend";
        mnemonics[InstructionCodes.WRKRECEIVE] = "wrkreceive";
        mnemonics[InstructionCodes.FORKJOIN] = "forkjoin";
        
        mnemonics[InstructionCodes.AWAIT] = "await";

        mnemonics[InstructionCodes.BINEWARRAY] = "binewarray";
        mnemonics[InstructionCodes.INEWARRAY] = "inewarray";
        mnemonics[InstructionCodes.FNEWARRAY] = "fnewarray";
        mnemonics[InstructionCodes.SNEWARRAY] = "snewarray";
        mnemonics[InstructionCodes.BNEWARRAY] = "bnewarray";
        mnemonics[InstructionCodes.RNEWARRAY] = "rnewarray";

        mnemonics[InstructionCodes.NEWSTRUCT] = "newstruct";
        mnemonics[InstructionCodes.NEWMAP] = "newmap";
        mnemonics[InstructionCodes.NEWTABLE] = "newtable";
        mnemonics[InstructionCodes.NEWSTREAM] = "newstream";

        mnemonics[InstructionCodes.NEW_INT_RANGE] = "new_int_range";
        mnemonics[InstructionCodes.ITR_NEW] = "itr_new";
        mnemonics[InstructionCodes.ITR_HAS_NEXT] = "itr_has_next";
        mnemonics[InstructionCodes.ITR_NEXT] = "itr_next";
        mnemonics[InstructionCodes.INT_RANGE] = "int_range";

        mnemonics[InstructionCodes.IRET] = "iret";
        mnemonics[InstructionCodes.FRET] = "fret";
        mnemonics[InstructionCodes.SRET] = "sret";
        mnemonics[InstructionCodes.BRET] = "bret";
        mnemonics[InstructionCodes.RRET] = "rret";
        mnemonics[InstructionCodes.RET] = "ret";

        mnemonics[InstructionCodes.XML2XMLATTRS] = "xml2xmlattrs";
        mnemonics[InstructionCodes.XMLATTRLOAD] = "xmlattrload";
        mnemonics[InstructionCodes.XMLATTRSTORE] = "xmlattrstore";
        mnemonics[InstructionCodes.S2QNAME] = "s2qname";
        mnemonics[InstructionCodes.XMLATTRS2MAP] = "xmlattr2map";
        mnemonics[InstructionCodes.NEWQNAME] = "newqname";
        mnemonics[InstructionCodes.NEWXMLELEMENT] = "newqxmlelement";
        mnemonics[InstructionCodes.NEWXMLCOMMENT] = "newxmlcomment";
        mnemonics[InstructionCodes.NEWXMLTEXT] = "newxmltext";
        mnemonics[InstructionCodes.NEWXMLPI] = "newxmlpi";
        mnemonics[InstructionCodes.XMLSEQSTORE] = "xmlseqstore";
        mnemonics[InstructionCodes.XMLSEQLOAD] = "xmlseqload";
        mnemonics[InstructionCodes.XMLLOAD] = "xmlload";
        mnemonics[InstructionCodes.XMLLOADALL] = "xmlloadall";
        mnemonics[InstructionCodes.NEWXMLSEQ] = "newxmlseq";
        mnemonics[InstructionCodes.XML2S] = "xml2s";
        mnemonics[InstructionCodes.LOCK] = "lock";
        mnemonics[InstructionCodes.UNLOCK] = "unlock";

        mnemonics[InstructionCodes.SCOPE_END] = "scope_end";
        mnemonics[InstructionCodes.COMPENSATE] = "compensate";
        mnemonics[InstructionCodes.LOOP_COMPENSATE] = "loop_compensate";
    }

    public static String getMnem(int opcode) {
        String mnem = mnemonics[opcode];
        if (mnem == null) {
            throw new IllegalStateException("opcode " + opcode + " is not added to mnemonics");
        }
        return mnem;
    }
}
