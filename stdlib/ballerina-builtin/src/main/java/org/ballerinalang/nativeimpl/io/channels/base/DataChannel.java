/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.nativeimpl.io.channels.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Represents a channel which will allow to perform data i/o
 */
public class DataChannel {
    /**
     * Source for reading bytes
     */
    private Channel channel;
    /**
     * Represents the byte order
     */
    private ByteOrder order;

    public DataChannel(Channel channel, ByteOrder order) {
        this.channel = channel;
        this.order = order;
    }

    private void readFull(ByteBuffer buffer, int requiredNumberOfBytes) throws IOException {
        int numberOfBytesRead;
        do {
            numberOfBytesRead = channel.read(buffer);
        } while (numberOfBytesRead < requiredNumberOfBytes && !channel.hasReachedEnd());
    }

    private int getInt(ByteBuffer buf){
        int value = 0;
        int msb = (buf.limit() - 1) * 8;
        do{
            value = value + (buf.get() >> msb);
            msb = msb -8;
        }while (buf.hasRemaining());
        return value;
    }

    private int getMergedByteValue(ByteBuffer buffer){
        //Arrange buffer for reading
        buffer.flip();
        if(buffer.order().equals(ByteOrder.nativeOrder())){
            return getInt(buffer);
        }else if(buffer.order().equals(ByteOrder.BIG_ENDIAN)){
            //TODO flip the bits here
            return getInt(buffer);
        }else {
            //TODO need to rearrange the bit order
            return getInt(buffer);
        }
    }

    public int readFixedInteger(Representation representation) throws IOException {
        int requiredNumberOfBytes = representation.getNumberOfBytes();
        ByteBuffer buffer = ByteBuffer.allocate(requiredNumberOfBytes);
        buffer.order(order);
        readFull(buffer,requiredNumberOfBytes);
        return getMergedByteValue(buffer);
    }
}
