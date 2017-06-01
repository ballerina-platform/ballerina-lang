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

import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry;
import org.ballerinalang.util.codegen.cpentries.ConstantPoolEntry.EntryType;
import org.ballerinalang.util.codegen.cpentries.IntegerCPEntry;
import org.ballerinalang.util.codegen.cpentries.PackageRefCPEntry;
import org.ballerinalang.util.codegen.cpentries.StringCPEntry;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Writes a Ballerina program to a file.
 *
 * @since 0.87
 */
public class ProgramFileWriter {

    public void writeProgram(ProgramFile programFile) {
        DataOutputStream outputStream;
        try {
            FileOutputStream fos = new FileOutputStream("first-prog");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            outputStream = new DataOutputStream(bos);

            outputStream.writeInt(programFile.getMagicValue());
            outputStream.writeShort(programFile.getVersion());

            writeCP(outputStream, programFile.getConstPool());
//            writeInstructions(outputStream, programFile.getInstructionList());
//            writeFunctions(outputStream, programFile.getFuncInfoList());

            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
    }

    private void writeCP(DataOutputStream outputStream, List<ConstantPoolEntry> constPool) throws IOException {
        outputStream.writeInt(constPool.size());

        for (ConstantPoolEntry cpEntry : constPool) {
            switch (cpEntry.getEntryType()) {
                case CP_ENTRY_UTF8:
                    outputStream.writeByte(EntryType.CP_ENTRY_UTF8.getValue());
                    String stringVal = ((UTF8CPEntry) cpEntry).getValue();
                    byte[] bytes = toUTF(stringVal);
                    outputStream.writeChar(bytes.length);
                    outputStream.write(bytes);
                    break;
                case CP_ENTRY_PACKAGE:
                    outputStream.writeByte(EntryType.CP_ENTRY_PACKAGE.getValue());
                    int nameIndex = ((PackageRefCPEntry) cpEntry).getNameCPIndex();
                    outputStream.writeInt(nameIndex);
                    break;
                case CP_ENTRY_INTEGER:
                    outputStream.writeByte(EntryType.CP_ENTRY_INTEGER.getValue());
                    long longVal = ((IntegerCPEntry) cpEntry).getValue();
                    outputStream.writeLong(longVal);
                    break;
                case CP_ENTRY_FLOAT:
                    outputStream.writeByte(EntryType.CP_ENTRY_INTEGER.getValue());
                    double doubleVal = ((IntegerCPEntry) cpEntry).getValue();
                    outputStream.writeDouble(doubleVal);
                    break;
                case CP_ENTRY_STRING:
                    outputStream.writeByte(EntryType.CP_ENTRY_STRING.getValue());
                    nameIndex = ((StringCPEntry) cpEntry).getStringCPIndex();
                    outputStream.writeInt(nameIndex);
//                case CP_ENTRY_NAME_AND_TYPE:
//                    outputStream.writeByte(EntryType.CP_ENTRY_NAME_AND_TYPE.getValue());
//                    NameAndTypeCPEntry nameAndTypeCPEntry = (NameAndTypeCPEntry) cpEntry;
////                    nameAndTypeCPEntry.
//                case CP_ENTRY_FUNCTION_REF:
//                    outputStream.writeByte(EntryType.CP_ENTRY_FUNCTION_REF.getValue());
//                default:

            }
        }
    }

    private void writeFunctions(DataOutputStream outputStream,
                                List<FunctionInfo> functionInfoList) throws IOException {
        outputStream.writeShort(functionInfoList.size());
        for (FunctionInfo functionInfo : functionInfoList) {
            outputStream.writeInt(functionInfo.getPackageCPIndex());
//            outputStream.writeInt(functionInfo.getNameAndTypeIndex());

            CodeAttributeInfo codeAttribute = functionInfo.codeAttributeInfo;

//            outputStream.writeShort(functionInfo.getAttributeInfoList().size());
            outputStream.writeInt(codeAttribute.getAttributeNameIndex());
            outputStream.writeInt(codeAttribute.getMaxIntRegs());
            outputStream.writeInt(codeAttribute.getMaxLongRegs());
            outputStream.writeInt(codeAttribute.getMaxStringRegs());
            outputStream.writeInt(codeAttribute.getMaxRefRegs());
            outputStream.writeInt(codeAttribute.getCodeAddrs());
        }
    }

    private void writeAttributes(DataOutputStream outputStream,
                                 List<AttributeInfo> attributeInfoList) throws IOException {
        outputStream.writeShort(attributeInfoList.size());

    }

    private void writeInstructions(DataOutputStream outputStream,
                                   List<Instruction> instructionList) throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteAOS);
        for (Instruction instruction : instructionList) {
            dos.writeByte(instruction.opcode);
            int[] operands = instruction.operands;
            for (int operand : operands) {
                dos.writeInt(operand);
            }
        }
        byte[] code = byteAOS.toByteArray();
        outputStream.writeInt(code.length);
        outputStream.write(code);
    }

    private byte[] toUTF(String value) throws IOException {
        ByteArrayOutputStream byteAOS = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(byteAOS);
        dos.writeUTF(value);
        return byteAOS.toByteArray();
    }
}
