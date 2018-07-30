/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.ballerinalang.stdlib.io.socket.server;

import java.util.HashSet;
import java.util.Set;

/**
 * This will maintain all the IO ready channel's hashcode IDs.
 */
public class SocketIOReadySet {

    private static Set<Integer> readReady = new HashSet<>();
    private static Set<Integer> writeReady = new HashSet<>();

    public static Set<Integer> getReadReady() {
        return readReady;
    }

    public static Set<Integer> getWriteReady() {
        return writeReady;
    }

    public static void addToReadReady(int readReadyChannelHash) {
        readReady.add(readReadyChannelHash);
    }

    public static void addToWriteReady(int writeReadyChannelHash) {
        writeReady.add(writeReadyChannelHash);
    }
}
