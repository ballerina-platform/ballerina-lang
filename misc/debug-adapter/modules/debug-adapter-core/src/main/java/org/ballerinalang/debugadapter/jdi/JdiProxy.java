/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.jdi;

/**
 * Base implementation of all JDI proxy implementation.
 */
public abstract class JdiProxy {
    protected final JdiTimer myTimer;
    private int myTimeStamp;

    public JdiProxy(JdiTimer timer) {
        myTimer = timer;
        myTimeStamp = myTimer.getCurrentTime();
    }

    protected void checkValid() {
        if (!isValid()) {
            myTimeStamp = myTimer.getCurrentTime();
            clearCaches();
        }
    }

    /**
     * @deprecated
     */
    @Deprecated
    public boolean isValid() {
        return myTimeStamp == myTimer.getCurrentTime();
    }

    protected abstract void clearCaches();
}
