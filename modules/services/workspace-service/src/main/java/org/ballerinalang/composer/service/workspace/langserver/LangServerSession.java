/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.langserver;

import io.netty.channel.Channel;

/**
 * LangServer session which holds the client information.
 */
public class LangServerSession {

    private Channel channel = null;

    /**
     * Constructor
     * @param channel netty channel
     */
    public LangServerSession(Channel channel) {
        this.channel = channel;
    }

    /**
     * Get the channel
     * @return channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Set the channel
     * @param channel netty channel
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
