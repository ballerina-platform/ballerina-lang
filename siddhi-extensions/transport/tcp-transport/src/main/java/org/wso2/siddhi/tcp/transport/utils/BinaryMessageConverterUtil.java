/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.siddhi.tcp.transport.utils;

import io.netty.buffer.ByteBuf;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BinaryMessageConverterUtil {

    public static byte[] loadData(InputStream in, byte[] dataArray) throws IOException {

        int start = 0;
        while (true) {
            int readCount = in.read(dataArray, start, dataArray.length - start);
            if (readCount != -1) {
                start += readCount;
                if (start == dataArray.length) {
                    return dataArray;
                }
            } else {
                throw new EOFException("Connection closed from remote end.");
            }
        }
    }

    public static String getString(ByteBuffer byteBuffer, int size) {

        byte[] bytes = new byte[size];
        byteBuffer.get(bytes);
        return new String(bytes);
    }

    public static String getString(ByteBuf byteBuffer, int size) {
        byte[] bytes = new byte[size];
        byteBuffer.readBytes(bytes);
        return new String(bytes);
    }

    public static int getSize(Object data) {
        if (data instanceof String) {
            return 4 + ((String) data).length();
        } else if (data instanceof Integer) {
            return 4;
        } else if (data instanceof Long) {
            return 8;
        } else if (data instanceof Float) {
            return 4;
        } else if (data instanceof Double) {
            return 8;
        } else if (data instanceof Boolean) {
            return 1;
        } else {
            return 4;
        }
    }


    public static void assignData(Object data, ByteBuf eventDataBuffer) throws IOException {
        if (data instanceof String) {
            eventDataBuffer.writeInt(((String) data).length());
            eventDataBuffer.writeBytes((((String) data).getBytes(BinaryMessageConstants.DEFAULT_CHARSET)));
        } else if (data instanceof Integer) {
            eventDataBuffer.writeInt((Integer) data);
        } else if (data instanceof Long) {
            eventDataBuffer.writeLong((Long) data);
        } else if (data instanceof Float) {
            eventDataBuffer.writeFloat((Float) data);
        } else if (data instanceof Double) {
            eventDataBuffer.writeDouble((Double) data);
        } else if (data instanceof Boolean) {
            eventDataBuffer.writeByte((byte) (((Boolean) data) ? 1 : 0));
        } else {
            eventDataBuffer.writeInt(0);
        }

    }
}
