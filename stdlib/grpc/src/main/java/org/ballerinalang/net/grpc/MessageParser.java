/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.net.grpc;

import com.google.protobuf.CodedInputStream;

import java.io.IOException;

/**
 * Proto Message Parser.
 *
 * @since 1.0.0
 */
public class MessageParser {

    private final String messageName;

    MessageParser(String messageName) {
        this.messageName = messageName;
    }

    public Message parseFrom(CodedInputStream input) throws
            IOException {
        return new Message(messageName, input);
    }
}
