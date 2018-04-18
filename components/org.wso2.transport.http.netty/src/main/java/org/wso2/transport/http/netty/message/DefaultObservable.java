/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.netty.handler.codec.http.HttpContent;

/**
 * Default implementation of the message observer.
 */
public class DefaultObservable implements Observable {

    private Listener listener;

    @Override
    public void setListener(Listener listener) {
        System.out.println("setListener");
        this.listener = listener;
    }

    @Override
    public void removeListener() {
        this.listener = null;
    }

    @Override
    public void notifyAddListener(HttpContent httpContent) {
        System.out.println("onAdd---------------");

        if (listener != null) {
            System.out.println("onAdd within if");

            listener.onAdd(httpContent);
        }
    }

    @Override
    public void notifyGetListener(HttpContent httpContent) {
        System.out.println("onRemove---------------");

        if (listener != null) {
            System.out.println("onRemove within if");

            listener.onRemove(httpContent);
        }
    }
}
