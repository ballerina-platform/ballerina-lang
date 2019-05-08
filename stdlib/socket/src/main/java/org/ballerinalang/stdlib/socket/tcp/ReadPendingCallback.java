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
import org.ballerinalang.model.values.BError;

import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This will hold information related to the pending read actions.
 *
 * @since 0.995.0
 */
public class ReadPendingCallback {

    private Context context;
    private CallableUnitCallback callback;
    private final int expectedLength;
    private int currentLength;
    private ByteBuffer buffer;
    private int socketHash;
    private Timer timer;
    private long timeout;

    public ReadPendingCallback(Context context, CallableUnitCallback callback, int expectedLength, int socketHash,
            long timeout) {
        this.context = context;
        this.callback = callback;
        this.expectedLength = expectedLength;
        this.socketHash = socketHash;
        this.timeout = timeout;
        scheduleTimeout(timeout);
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

    private void scheduleTimeout(long timeout) {
        timer = getTimer();
        timer.schedule(getTimerTask(), timeout);
    }

    public void resetTimeout() {
        timer.cancel();
        timer = getTimer();
        timer.schedule(getTimerTask(), this.timeout);
    }

    public void cancelTimeout() {
        timer.cancel();
    }

    private Timer getTimer() {
        return new Timer("B7aSocketTimeoutTimer");
    }

    private TimerTask getTimerTask() {
        return new TimerTask() {
            public void run() {
                ReadPendingSocketMap.getInstance().remove(socketHash);
                final BError timeoutError = SocketUtils.createSocketError(context, "Read timed out");
                context.setReturnValues(timeoutError);
                callback.notifySuccess();
                cancel();
            }
        };
    }
}
