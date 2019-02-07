/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.socket.tcp;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CallableUnitCallback;

import java.nio.ByteBuffer;

/**
 * Hello.
 */
public class ReadPendingCallback {

    private Context context;
    private CallableUnitCallback callback;
    private final int expectedLength;
    private int currentLength;
    private ByteBuffer buffer;

    public ReadPendingCallback(Context context, CallableUnitCallback callback, int expectedLength) {
        this.context = context;
        this.callback = callback;
        this.expectedLength = expectedLength;
    }

    public Context getContext() {
        return context;
    }

    public CallableUnitCallback getCallback() {
        return callback;
    }

    public int getExpectedLength() {
        return expectedLength;
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public void updateCurrentLength(int currentLength) {
        this.currentLength += currentLength;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}
