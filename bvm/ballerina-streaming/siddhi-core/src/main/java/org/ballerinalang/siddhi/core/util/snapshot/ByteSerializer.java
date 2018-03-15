/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.siddhi.core.util.snapshot;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Serializer used by {@link SnapshotService} to do Object to Byte[] conversion and vise-versa.
 */
public class ByteSerializer {
    private static final Logger log = LoggerFactory.getLogger(ByteSerializer.class);

    private ByteSerializer() {
    }

    public static byte[] objectToByte(Object obj, SiddhiAppContext siddhiAppContext) {
        long start = System.currentTimeMillis();
        byte[] out = null;
        if (obj != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                out = baos.toByteArray();
            } catch (IOException e) {
                log.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Error when writing byte array.", e);
                return null;
            }
        }
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("For SiddhiApp '" + siddhiAppContext.getName() + "'. Encoded in :" + (end - start) + " msec");
        }
        return out;
    }

    public static Object byteToObject(byte[] bytes, SiddhiAppContext siddhiAppContext) {
        long start = System.currentTimeMillis();
        Object out = null;
        if (bytes != null) {
            try {
                ByteArrayInputStream bios = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bios);
                out = ois.readObject();
            } catch (IOException e) {
                log.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Error when writing to object.", e);
                return null;
            } catch (ClassNotFoundException e) {
                log.error(ExceptionUtil.getMessageWithContext(e, siddhiAppContext) +
                        " Error when writing to object.", e);
                return null;
            }
        }
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Decoded in :" + (end - start) + " msec");
        }
        return out;
    }
}
