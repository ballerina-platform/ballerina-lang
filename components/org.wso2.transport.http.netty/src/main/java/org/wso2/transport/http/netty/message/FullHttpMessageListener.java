/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.transport.http.netty.message;

/**
 * Gets notified upon complete content accumulation.
 */
public interface FullHttpMessageListener {

    /**
     * Get notified when all the content are added to the {@link HttpCarbonMessage}.
     *
     * @param httpCarbonMessage {@link HttpCarbonMessage} with complete content

     */
    void onComplete(HttpCarbonMessage httpCarbonMessage);

    /**
     * Get notified when an error occurred during the accumulation.
     *
     * @param error of the message
     */
    void onError(Exception error);
}
