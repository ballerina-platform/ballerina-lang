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
 * @since 0.87
 */
public class Mnemonics {

    private static final String[] mnemonics = new String[InstructionCodes.INSTRUCTION_CODE_COUNT];

    static {
        mnemonics[InstructionCodes.NOP] = "nop";
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
        mnemonics[InstructionCodes.DCONST] = "dconst";

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

        mnemonics[InstructionCodes.CHNRECEIVE] = "chn_receive";
        mnemonics[InstructionCodes.CHNSEND] = "chn_send";

        mnemonics[InstructionCodes.MAPLOAD] = "map_load";
        mnemonics[InstructionCodes.JSONLOAD] = "json_load";

        mnemonics[InstructionCodes.COMPENSATE] = "compensate";

        mnemonics[InstructionCodes.BIASTORE] = "biastore";
        mnemonics[InstructionCodes.IASTORE] = "iastore";
        mnemonics[InstructionCodes.FASTORE] = "fastore";
        mnemonics[InstructionCodes.SASTORE] = "sastore";
        mnemonics[InstructionCodes.BASTORE] = "bastore";
        mnemonics[InstructionCodes.RASTORE] = "rastore";
        mnemonics[InstructionCodes.JSONASTORE] = "jsonastore";

        mnemonics[InstructionCodes.BIAND] = "biand";
        mnemonics[InstructionCodes.IAND] = "iand";
        mnemonics[InstructionCodes.BIOR] = "bior";
        mnemonics[InstructionCodes.IOR] = "ior";

        mnemonics[InstructionCodes.IGSTORE] = "igstore";
        mnemonics[InstructionCodes.FGSTORE] = "fgstore";
        mnemonics[InstructionCodes.SGSTORE] = "sgstore";
        mnemonics[InstructionCodes.BGSTORE] = "bgstore";
        mnemonics[InstructionCodes.RGSTORE] = "rgstore";

        mnemonics[InstructionCodes.IS_LIKE] = "is_like";

        mnemonics[InstructionCodes.STAMP] = "stamp";

        mnemonics[InstructionCodes.FREEZE] = "freeze";
        mnemonics[InstructionCodes.IS_FROZEN] = "is_frozen";

        mnemonics[InstructionCodes.ERROR] = "error";
        mnemonics[InstructionCodes.PANIC] = "panic";
        mnemonics[InstructionCodes.REASON] = "reason";
        mnemonics[InstructionCodes.DETAIL] = "detail";

        mnemonics[InstructionCodes.MAPSTORE] = "map_store";
        mnemonics[InstructionCodes.JSONSTORE] = "json_store";

        mnemonics[InstructionCodes.IADD] = "iadd";
        mnemonics[InstructionCodes.FADD] = "fadd";
        mnemonics[InstructionCodes.SADD] = "sadd";
        mnemonics[InstructionCodes.DADD] = "dadd";

        mnemonics[InstructionCodes.SCOPE_END] = "scope_end";
        mnemonics[InstructionCodes.LOOP_COMPENSATE] = "loop_compensate";

        mnemonics[InstructionCodes.XMLADD] = "radd";
        mnemonics[InstructionCodes.ISUB] = "isub";
        mnemonics[InstructionCodes.FSUB] = "fsub";
        mnemonics[InstructionCodes.DSUB] = "dsub";
        mnemonics[InstructionCodes.IMUL] = "imul";
        mnemonics[InstructionCodes.FMUL] = "fmul";
        mnemonics[InstructionCodes.DMUL] = "dmul";
        mnemonics[InstructionCodes.IDIV] = "idiv";
        mnemonics[InstructionCodes.FDIV] = "fdiv";
        mnemonics[InstructionCodes.DDIV] = "ddiv";
        mnemonics[InstructionCodes.IMOD] = "imod";
        mnemonics[InstructionCodes.FMOD] = "fmod";
        mnemonics[InstructionCodes.DMOD] = "dmod";
        mnemonics[InstructionCodes.INEG] = "ineg";
        mnemonics[InstructionCodes.FNEG] = "fneg";
        mnemonics[InstructionCodes.DNEG] = "dneg";
        mnemonics[InstructionCodes.BNOT] = "bnot";

        mnemonics[InstructionCodes.IEQ] = "ieq";
        mnemonics[InstructionCodes.FEQ] = "feq";
        mnemonics[InstructionCodes.SEQ] = "seq";
        mnemonics[InstructionCodes.BEQ] = "beq";
        mnemonics[InstructionCodes.DEQ] = "deq";
        mnemonics[InstructionCodes.REQ] = "req";
        mnemonics[InstructionCodes.REF_EQ] = "ref_eq";

        mnemonics[InstructionCodes.INE] = "ine";
        mnemonics[InstructionCodes.FNE] = "fne";
        mnemonics[InstructionCodes.SNE] = "sne";
        mnemonics[InstructionCodes.BNE] = "bne";
        mnemonics[InstructionCodes.DNE] = "dne";
        mnemonics[InstructionCodes.RNE] = "rne";
        mnemonics[InstructionCodes.REF_NEQ] = "ref_neq";

        mnemonics[InstructionCodes.IGT] = "igt";
        mnemonics[InstructionCodes.FGT] = "fgt";
        mnemonics[InstructionCodes.DGT] = "dgt";

        mnemonics[InstructionCodes.IGE] = "ige";
        mnemonics[InstructionCodes.FGE] = "fge";
        mnemonics[InstructionCodes.DGE] = "dge";

        mnemonics[InstructionCodes.ILT] = "ilt";
        mnemonics[InstructionCodes.FLT] = "flt";
        mnemonics[InstructionCodes.DLT] = "dlt";

        mnemonics[InstructionCodes.ILE] = "ile";
        mnemonics[InstructionCodes.FLE] = "fle";
        mnemonics[InstructionCodes.DLE] = "dle";

        mnemonics[InstructionCodes.REQ_NULL] = "reg_null";
        mnemonics[InstructionCodes.RNE_NULL] = "rne_null";

        mnemonics[InstructionCodes.BR_TRUE] = "br_true";
        mnemonics[InstructionCodes.BR_FALSE] = "br_false";

        mnemonics[InstructionCodes.GOTO] = "goto";
        mnemonics[InstructionCodes.HALT] = "halt";
        mnemonics[InstructionCodes.TR_RETRY] = "tr_retry";
        mnemonics[InstructionCodes.CALL] = "call";
        mnemonics[InstructionCodes.VCALL] = "v_call";
        mnemonics[InstructionCodes.FPCALL] = "fp_call";
        mnemonics[InstructionCodes.FPLOAD] = "fp_load";
        mnemonics[InstructionCodes.VFPLOAD] = "vfp_load";

        mnemonics[InstructionCodes.I2F] = "i2f";
        mnemonics[InstructionCodes.I2S] = "i2s";
        mnemonics[InstructionCodes.I2B] = "i2b";
        mnemonics[InstructionCodes.I2D] = "i2d";
        mnemonics[InstructionCodes.F2I] = "f2i";
        mnemonics[InstructionCodes.F2S] = "f2s";
        mnemonics[InstructionCodes.F2B] = "f2b";
        mnemonics[InstructionCodes.F2D] = "f2d";
        mnemonics[InstructionCodes.S2I] = "s2i";
        mnemonics[InstructionCodes.S2F] = "s2f";
        mnemonics[InstructionCodes.S2B] = "s2b";
        mnemonics[InstructionCodes.S2D] = "s2d";
        mnemonics[InstructionCodes.B2I] = "b2i";
        mnemonics[InstructionCodes.B2F] = "b2f";
        mnemonics[InstructionCodes.B2S] = "b2s";
        mnemonics[InstructionCodes.B2D] = "b2d";
        mnemonics[InstructionCodes.D2I] = "d2i";
        mnemonics[InstructionCodes.D2F] = "d2f";
        mnemonics[InstructionCodes.D2S] = "d2s";
        mnemonics[InstructionCodes.D2B] = "d2b";
        mnemonics[InstructionCodes.DT2JSON] = "dt2json";
        mnemonics[InstructionCodes.DT2XML] = "dt2xml";
        mnemonics[InstructionCodes.T2MAP] = "t2map";
        mnemonics[InstructionCodes.T2JSON] = "t2json";
        mnemonics[InstructionCodes.MAP2T] = "map2t";
        mnemonics[InstructionCodes.JSON2T] = "json2t";
        mnemonics[InstructionCodes.XML2S] = "xml2s";

        mnemonics[InstructionCodes.BILSHIFT] = "bilshift";
        mnemonics[InstructionCodes.BIRSHIFT] = "birshift";
        mnemonics[InstructionCodes.ILSHIFT] = "ilshift";
        mnemonics[InstructionCodes.IRSHIFT] = "irshift";

        mnemonics[InstructionCodes.I2ANY] = "i2any";
        mnemonics[InstructionCodes.F2ANY] = "f2any";
        mnemonics[InstructionCodes.S2ANY] = "s2any";
        mnemonics[InstructionCodes.B2ANY] = "b2any";

        mnemonics[InstructionCodes.TYPE_ASSERTION] = "type_assertion";

        // Type cast
        mnemonics[InstructionCodes.ANY2I] = "any2i";
        mnemonics[InstructionCodes.ANY2F] = "any2f";
        mnemonics[InstructionCodes.ANY2S] = "any2s";
        mnemonics[InstructionCodes.ANY2B] = "any2b";
        mnemonics[InstructionCodes.ANY2D] = "any2d";
        mnemonics[InstructionCodes.ANY2JSON] = "any2json";
        mnemonics[InstructionCodes.ANY2XML] = "any2xml";
        mnemonics[InstructionCodes.ANY2MAP] = "any2map";
        mnemonics[InstructionCodes.ANY2STM] = "any2stm";
        mnemonics[InstructionCodes.ANY2DT] = "any2dt";
        mnemonics[InstructionCodes.ANY2SCONV] = "any2s_conv";
        mnemonics[InstructionCodes.ANY2BI] = "any2bi";
        mnemonics[InstructionCodes.BI2ANY] = "bi2any";
        mnemonics[InstructionCodes.ANY2E] = "any2e";
        mnemonics[InstructionCodes.ANY2T] = "any2t";
        mnemonics[InstructionCodes.ANY2C] = "any2c";
        mnemonics[InstructionCodes.CHECKCAST] = "check_cast";

        mnemonics[InstructionCodes.ANY2TYPE] = "any2type";

        mnemonics[InstructionCodes.LOCK] = "lock";
        mnemonics[InstructionCodes.UNLOCK] = "unlock";

        // Transactions
        mnemonics[InstructionCodes.TR_BEGIN] = "tr_begin";
        mnemonics[InstructionCodes.TR_END] = "tr_end";

        mnemonics[InstructionCodes.WRKSEND] = "wrk_send";
        mnemonics[InstructionCodes.WRKRECEIVE] = "wrk_receive";

        mnemonics[InstructionCodes.WORKERSYNCSEND] = "worker_sync_send";
        mnemonics[InstructionCodes.WAIT] = "wait";

        mnemonics[InstructionCodes.MAP2JSON] = "map2json";
        mnemonics[InstructionCodes.JSON2MAP] = "json2map";

        mnemonics[InstructionCodes.IS_ASSIGNABLE] = "is_assignable";
        mnemonics[InstructionCodes.O2JSON] = "o2json";

        mnemonics[InstructionCodes.ARRAY2JSON] = "array2json";
        mnemonics[InstructionCodes.JSON2ARRAY] = "json2array";

        mnemonics[InstructionCodes.BINEWARRAY] = "binewarray";
        mnemonics[InstructionCodes.INEWARRAY] = "inewarray";
        mnemonics[InstructionCodes.FNEWARRAY] = "fnewarray";
        mnemonics[InstructionCodes.SNEWARRAY] = "snewarray";
        mnemonics[InstructionCodes.BNEWARRAY] = "bnewarray";
        mnemonics[InstructionCodes.RNEWARRAY] = "rnewarray";

        mnemonics[InstructionCodes.CLONE] = "clone";
        mnemonics[InstructionCodes.FLUSH] = "flush";
        mnemonics[InstructionCodes.LENGTH] = "length_of";
        mnemonics[InstructionCodes.WAITALL] = "wait_all";

        mnemonics[InstructionCodes.NEWSTRUCT] = "new_struct";
        mnemonics[InstructionCodes.NEWMAP] = "new_map";
        mnemonics[InstructionCodes.NEWTABLE] = "new_table";
        mnemonics[InstructionCodes.NEWSTREAM] = "new_stream";

        mnemonics[InstructionCodes.CONVERT] = "convert";

        mnemonics[InstructionCodes.ITR_NEW] = "itr_new";
        mnemonics[InstructionCodes.ITR_NEXT] = "itr_next";
        mnemonics[InstructionCodes.INT_RANGE] = "int_range";

        mnemonics[InstructionCodes.I2BI] = "i2bi";
        mnemonics[InstructionCodes.BI2I] = "bi2i";
        mnemonics[InstructionCodes.BIXOR] = "bior";
        mnemonics[InstructionCodes.IXOR] = "ior";
        mnemonics[InstructionCodes.BACONST] = "baconst";
        mnemonics[InstructionCodes.IURSHIFT] = "iurshift";

        mnemonics[InstructionCodes.IRET] = "iret";
        mnemonics[InstructionCodes.FRET] = "fret";
        mnemonics[InstructionCodes.SRET] = "sret";
        mnemonics[InstructionCodes.BRET] = "bret";
        mnemonics[InstructionCodes.DRET] = "dret";
        mnemonics[InstructionCodes.RRET] = "rret";
        mnemonics[InstructionCodes.RET] = "ret";

        mnemonics[InstructionCodes.XML2XMLATTRS] = "xml2xmlattrs";
        mnemonics[InstructionCodes.XMLATTRS2MAP] = "xmlattr2map";
        mnemonics[InstructionCodes.XMLATTRLOAD] = "xmlattrload";
        mnemonics[InstructionCodes.XMLATTRSTORE] = "xmlattrstore";
        mnemonics[InstructionCodes.S2QNAME] = "s2qname";
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

        mnemonics[InstructionCodes.TYPE_TEST] = "type_test";
        mnemonics[InstructionCodes.TYPELOAD] = "type_load";

        mnemonics[InstructionCodes.TEQ] = "teq";
        mnemonics[InstructionCodes.TNE] = "tne";
    }


    public static String getMnem(int opcode) {
        String mnem = mnemonics[opcode];
        if (mnem == null) {
            throw new IllegalStateException("opcode " + opcode + " is not added to mnemonics");
        }
        return mnem;
    }
}
