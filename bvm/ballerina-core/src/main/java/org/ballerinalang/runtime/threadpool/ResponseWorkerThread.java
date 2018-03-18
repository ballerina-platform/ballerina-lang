/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.ballerinalang.runtime.threadpool;

import org.ballerinalang.bre.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Worker Thread which is responsible for response processing.
 */
public class ResponseWorkerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ResponseWorkerThread.class);
    private Context context;

    public ResponseWorkerThread(Context context) {
        this.context = context;
    }

    public void run() {
//        TODO
//        BLangVM bLangVM = new BLangVM(context.getProgramFile());
//        try {
//            bLangVM.run(context);
//        } catch (Exception e) {
//            logger.error("unhandled exception ", e);
//        }
    }
}
