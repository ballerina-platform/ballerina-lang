/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.io;

/**
 * Holds the constants related to I/O
 */
public class IOConstants {

    /**
     * The value of the key the channel will be held in the struct
     *
     * @see java.nio.channels.ByteChannel
     */
    public static final String BYTE_CHANNEL_NAME = "channel";

    /**
     * The value of the character channel which will be derived from the byte channel
     */
    public static final String CHARACTER_CHANNEL_NAME = "char_channel";

    /**
     * The value of the text record channel which will be used for struct registration
     */
    public static final String TXT_RECORD_CHANNEL_NAME = "txt_record";
}
