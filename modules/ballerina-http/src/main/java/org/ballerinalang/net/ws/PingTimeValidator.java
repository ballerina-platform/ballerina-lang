/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.ws;

import java.util.Date;

/**
 * Time stamp to compare the time and validate the ping.
 */
public class PingTimeValidator {
    private final Date pingTime;
    private long timeoutInMillis;
    private boolean infiniteTimeout = false;

    /**
     * @param timeout timeout in seconds.
     */
    public PingTimeValidator(long timeout) {
        this.pingTime = new Date();
        if (timeout < 0) {
            infiniteTimeout = true;
        } else {
            this.timeoutInMillis = timeout * 1000;
        }
    }

    /**
     * Validate whether the time difference when validate is less than the expected timeout.
     *
     * @return true if time difference is less than the the expected timeout.
     */
    public boolean validate() {
        if (infiniteTimeout) {
            return true;
        }
        Date currentDate = new Date();
        long timeDiff = currentDate.getTime() - pingTime.getTime();
        return timeoutInMillis >= timeDiff;
    }
}
