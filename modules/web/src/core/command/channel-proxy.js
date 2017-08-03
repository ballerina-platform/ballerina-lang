/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
let channel;

/**
 * Command Channel Proxy
 * Exposes a limited functionality to outside
 * Eg: Prevent others calling off, on on event bus.
 * Just let them use dispatch
 */
class CommandChannelProxy {
    /**
     * Register handlers for logging purposes
     * @param {CommandChannel} commandChannel command channel instance
     */
    constructor(commandChannel) {
        channel = commandChannel;
    }

    /**
     * Dispatch a command
     *
     * @param {any[]} args
     */
    dispatch(...args) {
        if (channel) {
            channel.dispatch(...args);
        }
    }
}

export default CommandChannelProxy;
