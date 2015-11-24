/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.transport.http.netty.common;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Interface for Message Sender to the BE.
 */
public interface TransportSender {
    /**
     * Should include the logic for handover messages to BE.
     * @param msg Mediated Request.
     * @param callback Carbon callback created by engine.
     * @return void
     * @throws GWException Gateway exception to singal any failures to upper layers.
     */
    public boolean send(CarbonMessage msg, CarbonCallback callback) throws GWException;

}
