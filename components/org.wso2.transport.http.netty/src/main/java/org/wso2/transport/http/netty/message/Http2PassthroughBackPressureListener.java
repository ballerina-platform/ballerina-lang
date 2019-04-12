/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.transport.http.netty.message;

/**
 * HTTP/2 back pressure listener for passthrough scenario.
 */
public class Http2PassthroughBackPressureListener implements BackPressureListener {

    private Http2InboundContentListener inboundListenerHook;

    public Http2PassthroughBackPressureListener(Http2InboundContentListener inboundListenerHook) {
        this.inboundListenerHook = inboundListenerHook;
    }

    @Override
    public void onUnWritable() {
        //Stop consuming bytes which will eventually stop the window updates to the peer
        inboundListenerHook.stopByteConsumption();
    }

    @Override
    public void onWritable() {
        //Immediately consume outstanding unconsumed bytes to resume window updates to the peer
        inboundListenerHook.resumeByteConsumption();
    }
}
