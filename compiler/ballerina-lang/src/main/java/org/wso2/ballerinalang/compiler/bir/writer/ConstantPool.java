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


import org.ballerinalang.compiler.BLangCompilerException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A pool of constant values in the binary BIR file.
 *
 * @since 0.980.0
 */
public class ConstantPool {

    // Size to be written to tag a null value
    private static final int NULL_VALUE_FIELD_SIZE_TAG = -1;

    private final List<CPEntry> cpEntries = new ArrayList<>();

    public int addCPEntry(CPEntry cpEntry) {
        if (cpEntries.contains(cpEntry)) {
            return cpEntries.indexOf(cpEntry);
        }

        cpEntries.add(cpEntry);
        return cpEntries.size() - 1;
    }

    public byte[] serialize()  {
        ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
        try (DataOutputStream dataStream = new DataOutputStream(byteArrayStream)) {
            writeToStream(dataStream);
            return byteArrayStream.toByteArray();
        } catch (IOException e) {
            throw new BLangCompilerException("failed to create bir consent pool", e);
        }
    }

    private void writeToStream(DataOutputStream stream) throws IOException {
        stream.writeInt(cpEntries.size());
        for (CPEntry cpEntry : cpEntries) {
            stream.writeByte(cpEntry.entryType.value);
            switch (cpEntry.entryType) {
                case CP_ENTRY_INTEGER:
                    stream.writeLong(((CPEntry.IntegerCPEntry) cpEntry).value);
                    break;
                case CP_ENTRY_BYTE:
                    stream.writeByte(((CPEntry.ByteCPEntry) cpEntry).value);
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
                default:
                    throw new IllegalStateException("unsupported constant pool entry type: " +
                                                    cpEntry.entryType.name());
            }
        }
    }
}
