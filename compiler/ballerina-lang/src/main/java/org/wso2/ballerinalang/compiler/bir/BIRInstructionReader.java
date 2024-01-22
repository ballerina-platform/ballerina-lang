/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import io.ballerina.tools.diagnostics.Location;
import io.netty.buffer.ByteBuf;
import org.wso2.ballerinalang.compiler.bir.model.BIRInstruction;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.model.BIRNonTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BIROperand;
import org.wso2.ballerinalang.compiler.bir.model.BIRTerminator;
import org.wso2.ballerinalang.compiler.bir.model.BirScope;
import org.wso2.ballerinalang.compiler.bir.model.InstructionKind;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLocation;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BIRInstructionReader {

    private DataInputStream dataInputStream;
    private ByteBuf scopeBuf;
    private Map<String, BIRNode.BIRBasicBlock> basicBlockMap = new HashMap<>();
    private CPEntry[] constantPool;

    public BIRInstructionReader(DataInputStream dataInputStream, ByteBuf scopeBuf,
                                HashMap<String, BIRNode.BIRBasicBlock> basicBlockMap, CPEntry[] constantPool) {

        this.dataInputStream = dataInputStream;
        this.scopeBuf = scopeBuf;
        this.basicBlockMap = basicBlockMap;
        this.constantPool = constantPool;
    }

    public void readBBs(BIRNode.BIRFunction birFunction) throws IOException {
        for (int i = 0; i < dataInputStream.readInt(); i++) {
            BIRNode.BIRBasicBlock basicBlock = getBB(getStringCPEntryValue());
            readInstructions(basicBlock);
        }
    }

    private void readInstructions(BIRNode.BIRBasicBlock basicBlock) throws IOException {
        for (int i = 0; i < dataInputStream.readInt(); i++) {
            readInstruction();

        }
    }

    private BIRInstruction readInstruction() throws IOException {
        Location pos = readPosition();
        BirScope birScope = readScope();
        // TODO Find a proper way to get the InstructionKind
        InstructionKind kind = InstructionKind.values()[dataInputStream.readByte() - 1];
        switch (kind) {
            case GOTO:
            case CALL:;
            case BRANCH:;
            case RETURN:;
            case ASYNC_CALL:;
            case WAIT:;
            case FP_CALL:;
            case WK_RECEIVE:;
            case WK_SEND:;
            case FLUSH:;
            case LOCK:;
            case FIELD_LOCK:;
            case UNLOCK:;
            case WAIT_ALL:;
            case MOVE:
//                return new BIRNonTerminator.Move()
            case CONST_LOAD:;
            case NEW_STRUCTURE:;
            case MAP_STORE:;
            case MAP_LOAD:;
            case NEW_ARRAY:;
            case ARRAY_STORE:;
            case ARRAY_LOAD:;
            case NEW_ERROR:;
            case TYPE_CAST:;
            case IS_LIKE:;
            case TYPE_TEST:;
            case NEW_INSTANCE:;
            case OBJECT_STORE:;
            case OBJECT_LOAD:;
            case PANIC:;
            case FP_LOAD:;
            case STRING_LOAD:;
            case NEW_XML_ELEMENT:;
            case NEW_XML_TEXT:;
            case NEW_XML_COMMENT:;
            case NEW_XML_PI:;
            case NEW_XML_SEQUENCE:;
            case NEW_XML_QNAME:;
            case NEW_STRING_XML_QNAME:;
            case XML_SEQ_STORE:;
            case XML_SEQ_LOAD:;
            case XML_LOAD:;
            case XML_LOAD_ALL:;
            case XML_ATTRIBUTE_LOAD:;
            case XML_ATTRIBUTE_STORE:;
            case NEW_TABLE:;
            case NEW_TYPEDESC:;
            case NEW_STREAM:;
            case TABLE_STORE:;
            case TABLE_LOAD:;

        }
        return null;
    }

//    private BIROperand readOperand() throws IOException {
//        if (dataInputStream.readBoolean()) {
//            BType variableDeclType = readBType(dataInputStream);
//        }
//        return new BIROperand()
//    }


    private BirScope readScope() {
        // TODO Find a way to write the scopes
        return null;
    }

    private BIRNode.BIRBasicBlock getBB(String bbId) {
        if (basicBlockMap.containsKey(bbId)) {
            return basicBlockMap.get(bbId);
        }

        BIRNode.BIRBasicBlock bb = new BIRNode.BIRBasicBlock(bbId);
        basicBlockMap.put(bbId, bb);
        return bb;
    }

    private Location readPosition() throws IOException {
        String cUnitName = getStringCPEntryValue();
        int sLine = dataInputStream.readInt();
        int sCol = dataInputStream.readInt();
        int eLine = dataInputStream.readInt();
        int eCol = dataInputStream.readInt();
        return new BLangDiagnosticLocation(cUnitName, sLine, eLine, sCol, eCol);
    }

    // private utility methods
    // TODO Same methods are present in BIRNodeEnter. Find a way to merge both into a utility class maybe?
    private String getStringCPEntryValue() throws IOException {
        int pkgNameCPIndex = dataInputStream.readInt();
        CPEntry.StringCPEntry stringCPEntry = (CPEntry.StringCPEntry) constantPool[pkgNameCPIndex];
        return stringCPEntry.value;
    }

    private String getStringCPEntryValue(int cpIndex) throws IOException {
        CPEntry.StringCPEntry stringCPEntry = (CPEntry.StringCPEntry) constantPool[cpIndex];
        return stringCPEntry.value;
    }

    private long getIntCPEntryValue() throws IOException {
        int pkgNameCPIndex = dataInputStream.readInt();
        CPEntry.IntegerCPEntry intCPEntry = (CPEntry.IntegerCPEntry) constantPool[pkgNameCPIndex];
        return intCPEntry.value;
    }

    private int getByteCPEntryValue() throws IOException {
        int byteCpIndex = dataInputStream.readInt();
        CPEntry.ByteCPEntry byteCPEntry = (CPEntry.ByteCPEntry) constantPool[byteCpIndex];
        return byteCPEntry.value;
    }

    private String getFloatCPEntryValue() throws IOException {
        int floatCpIndex = dataInputStream.readInt();
        CPEntry.FloatCPEntry floatCPEntry = (CPEntry.FloatCPEntry) constantPool[floatCpIndex];
        return Double.toString(floatCPEntry.value);
    }

//    private BType readBType() throws IOException {
//        int typeCpIndex = dataInputStream.readInt();
//        CPEntry cpEntry = constantPool[typeCpIndex];
//        BType type = null;
//        if (cpEntry != null) {
//            type = ((CPEntry.ShapeCPEntry) cpEntry).shape;
//            if (type.tag != TypeTags.INVOKABLE) {
//                return type;
//            }
//        }
//        if (type == null) {
//            byte[] e = env.unparsedBTypeCPs.get(typeCpIndex);
//            type = new BIRNodeEnter.BIRTypeReader(new DataInputStream(new ByteArrayInputStream(e))).readType(typeCpIndex);
//            addShapeCP(type, typeCpIndex);
//        }
//        return type;
//    }
}
