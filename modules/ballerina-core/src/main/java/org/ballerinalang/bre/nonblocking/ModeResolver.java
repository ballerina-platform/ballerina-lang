/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.bre.nonblocking;

import org.ballerinalang.runtime.Constants;


/**
 * Resolve which interpreter to run.
 */
public class ModeResolver {


    private static final ModeResolver instance = new ModeResolver();
    private boolean nonblockingEnabled = false;
    private boolean debugEnabled = false;

    private ModeResolver() {
        String property = System.getProperty(Constants.SYS_PROP_ENABLE_NONBLOCKING);
        if (property != null && property.equalsIgnoreCase("true")) {
            nonblockingEnabled = true;
        }
        String debug = System.getProperty(Constants.SYSTEM_PROP_BAL_DEBUG);
        if (debug != null && !debug.isEmpty()) {
            debugEnabled = true;
        }
    }

    public static ModeResolver getInstance() {
        return instance;
    }

    public boolean isNonblockingEnabled() {
        return nonblockingEnabled || debugEnabled;
    }

    public void setNonblockingEnabled(boolean enabled) {
        // Testing purpose only.
        this.nonblockingEnabled = enabled;
    }

    public boolean isDebugEnabled() {
        return this.debugEnabled;
    }

}
