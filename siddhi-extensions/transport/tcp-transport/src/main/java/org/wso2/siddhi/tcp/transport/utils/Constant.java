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

public final class Constant {

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final int DEFAULT_RECEIVER_THREADS = 10;
    public static final int DEFAULT_WORKER_THREADS = 10;
    public static final int DEFAULT_QUEUE_SIZE_OF_TCP_TRANSPORT = 65525;
    public static final int DEFAULT_PORT = 9892;
    public static final String DEFAULT_HOST = "0.0.0.0";

    private Constant() {

    }
}
