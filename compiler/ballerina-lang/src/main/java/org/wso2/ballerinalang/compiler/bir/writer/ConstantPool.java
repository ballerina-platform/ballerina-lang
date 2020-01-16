/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.bir.writer;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A pool of constant values in the binary BIR file.
 *
 * @since 0.980.0
 */
public class ConstantPool {

    // Size to be written to tag a null value
    private static final int NULL_VALUE_FIELD_SIZE_TAG = -1;

    private final Map<CPEntry, Integer> cpEntriesMap = new HashMap<>();
    private final List<CPEntry> cpEntries = new ArrayList<>();

    public int addCPEntry(CPEntry cpEntry) {
        int size = cpEntries.size();
        Integer position = cpEntriesMap.get(cpEntry);
        if (position == null) {
            cpEntries.add(cpEntry);
            cpEntriesMap.put(cpEntry, size);
            return size;
        }
        return position;
    }

    public int addShapeCPEntry(BType shape) {
        CPEntry.ShapeCPEntry shapeCPEntry = new CPEntry.ShapeCPEntry(shape);
        return addCPEntry(shapeCPEntry);
    }

    public byte[] serialize()  {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (DataOutputStream dataStream = new DataOutputStream(byteArrayStream)) {
            writeToStream(dataStream);
            dataStream.flush();
            byte[] bytes = byteArrayStream.toByteArray();
            overwriteSize(bytes);
            return bytes;
        } catch (IOException e) {
            throw new BLangCompilerException("failed to create bir consent pool", e);
        }
    }

    private void overwriteSize(byte[] bytes) {
        int v = cpEntries.size();
        bytes[0] = (byte) ((v >>> 24) & 0xFF);
        bytes[1] = (byte) ((v >>> 16) & 0xFF);
        bytes[2] = (byte) ((v >>> 8) & 0xFF);
        bytes[3] = (byte) ((v >>> 0) & 0xFF);
    }

    private void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(-1);
        for (int i = 0; i < cpEntries.size(); i++) {
            CPEntry cpEntry = cpEntries.get(i);
            stream.writeByte(cpEntry.entryType.value);
            switch (cpEntry.entryType) {
                case CP_ENTRY_INTEGER:
                    stream.writeLong(((CPEntry.IntegerCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_BYTE:
                    stream.writeInt(((CPEntry.ByteCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_FLOAT:
                    stream.writeDouble(((CPEntry.FloatCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_BOOLEAN:
                    stream.writeBoolean(((CPEntry.BooleanCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_STRING:
                    CPEntry.StringCPEntry stringCPEntry = (CPEntry.StringCPEntry) cpEntry;
                    if (stringCPEntry.value != null) {
                        byte[] strBytes = stringCPEntry.value.getBytes(StandardCharsets.UTF_8);
                        stream.writeInt(strBytes.length);
                        stream.write(strBytes);
                    } else {
                        // If the string value is null, we write the size as -1.
                        // This marks that the value followed by -1 size is a null value.
                        stream.writeShort(NULL_VALUE_FIELD_SIZE_TAG);
                    }
                    break;
                case CP_ENTRY_PACKAGE:
                    CPEntry.PackageCPEntry pkgCPEntry = (CPEntry.PackageCPEntry) cpEntry;
                    stream.writeInt(pkgCPEntry.orgNameCPIndex);
                    stream.writeInt(pkgCPEntry.pkgNameCPIndex);
                    stream.writeInt(pkgCPEntry.versionCPIndex);
                    break;
                case CP_ENTRY_SHAPE:
                    CPEntry.ShapeCPEntry shapeCPEntry = (CPEntry.ShapeCPEntry) cpEntry;

                    ByteBuf typeBuf = Unpooled.buffer();
                    BIRTypeWriter birTypeWriter = new BIRTypeWriter(typeBuf, this);
                    birTypeWriter.visitType(shapeCPEntry.shape);
                    byte[] bytes = Arrays.copyOfRange(typeBuf.array(), 0, typeBuf.writerIndex());

                    stream.writeInt(bytes.length);
                    stream.write(bytes);
                    break;
                default:
                    throw new IllegalStateException("unsupported constant pool entry type: " +
                                                    cpEntry.entryType.name());
            }
        }
    }
}
