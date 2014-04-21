/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.siddhi.core.util;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.persistence.InMemoryPersistenceStore;
//import sun.misc.BASE64Decoder;
//import sun.misc.BASE64Encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ByteSerializer {
    private static final Logger log = Logger.getLogger(InMemoryPersistenceStore.class);
//    static private BASE64Encoder encode = new BASE64Encoder();
//    static private BASE64Decoder decode = new BASE64Decoder();

    static public byte[] OToB(Object obj) {
        long start = System.currentTimeMillis();
        byte[] out = null;
        if (obj != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                out = baos.toByteArray();
//                out = encode.encode(baos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("Encoded in :" + (end - start) + " msec");
        }
        return out;
    }

    static public Object BToO(byte[] bytes) {
        long start = System.currentTimeMillis();
        Object out = null;
        if (bytes != null) {
            try {

//                ByteArrayInputStream bios = new ByteArrayInputStream(decode.decodeBuffer(str));
                ByteArrayInputStream bios = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bios);
                out = ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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