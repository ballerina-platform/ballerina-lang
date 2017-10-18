/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.nativeimpl.utils.logger;

import org.ballerinalang.bre.Context;
import org.ballerinalang.logging.BLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for selecting the appropriate logger for a particular log statement.
 *
 * @since 0.89
 */
public class BallerinaLogHandler {
    private static final Logger ballerinaRootLogger = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);

    public static Logger getLogger(Context ctx) {
        String packageDirPath =
                ctx.getControlStackNew().getStack()[ctx.getControlStackNew().fp - 1].getCallableUnitInfo()
                        .getPackageInfo().getPkgPath();

        if (".".equals(packageDirPath) || packageDirPath == null) {
            return ballerinaRootLogger;
        } else {
            // TODO: Refactor this later
            return LoggerFactory.getLogger(ballerinaRootLogger.getName() + "." + packageDirPath);
        }
    }
}
