/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nativeimpl.log;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.logging.BLogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

/**
 * Base class for the other log functions, containing a getter to retrieve the correct logger, given a package name.
 *
 * @since 0.95.0
 */
public abstract class AbstractLogFunction extends BlockingNativeCallableUnit {

    protected static final BLogManager LOG_MANAGER = (BLogManager) LogManager.getLogManager();

    private static final Logger ballerinaRootLogger = LoggerFactory.getLogger(BLogManager.BALLERINA_ROOT_LOGGER_NAME);

    protected Logger getLogger(String pkg) {
        if (".".equals(pkg) || pkg == null) {
            return ballerinaRootLogger;
        } else {
            // TODO: Refactor this later
            return LoggerFactory.getLogger(ballerinaRootLogger.getName() + "." + pkg);
        }
    }

    protected String getLogMessage(Context context, int index) {
        return String.valueOf(context.getStringArgument(index));
    }

    //TODO merge below and above methods(below one new bvm)
    protected String getPackagePath(Context ctx) {
        return ctx.getParentWorkerExecutionContext().callableUnitInfo.getPackageInfo().getPkgPath();
    }
}
