/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerinalang.compiler.bir.writer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.ballerinalang.model.elements.MarkdownDocAttachment;

/**
 * Writes markdown document attachment in binary format to the byte buffer.
 *
 * @since 1.3.0
 */
class DocAttachmentWriter {

    static void writeMarkdownDocAttachment(ByteBuf buf, MarkdownDocAttachment markdownDocAttachment, ConstantPool cp) {

        ByteBuf birBuf = Unpooled.buffer();
        if (markdownDocAttachment == null) {
            birBuf.writeBoolean(false);
        } else {
            birBuf.writeBoolean(true);

            birBuf.writeInt(markdownDocAttachment.description == null ? -1
                    : addStringCPEntry(markdownDocAttachment.description, cp));
            birBuf.writeInt(markdownDocAttachment.returnValueDescription == null ? -1
                    : addStringCPEntry(markdownDocAttachment.returnValueDescription, cp));
            birBuf.writeInt(markdownDocAttachment.parameters.size());
            for (MarkdownDocAttachment.Parameter parameter : markdownDocAttachment.parameters) {
                birBuf.writeInt(parameter.name == null ? -1
                        : addStringCPEntry(parameter.name, cp));
                birBuf.writeInt(parameter.description == null ? -1
                        : addStringCPEntry(parameter.description, cp));
            }
        }
        int length = birBuf.nioBuffer().limit();
        buf.writeInt(length);
        buf.writeBytes(birBuf.nioBuffer().array(), 0, length);
    }

    private static int addStringCPEntry(String value, ConstantPool cp) {

        return cp.addCPEntry(new CPEntry.StringCPEntry(value));
    }
}
