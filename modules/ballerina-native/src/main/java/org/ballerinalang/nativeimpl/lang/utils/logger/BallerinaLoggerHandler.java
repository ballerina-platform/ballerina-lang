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

package org.ballerinalang.nativeimpl.lang.utils.logger;

import org.ballerinalang.bre.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Matcher;

/**
 * A class for retrieving the relevant Logger from LoggerFactory for a particular log function inovcation
 */

public class BallerinaLoggerHandler {
    private static final Logger ballerinaRootLogger = LoggerFactory.getLogger("ballerina");

    public static Logger getLogger(Context ctx) {
        String packageDirPath =
                ctx.getControlStack().getCurrentFrame().getNodeInfo().getNodeLocation().getPackageDirPath();

        if (packageDirPath == null) {
            return ballerinaRootLogger;
        } else {
            // TODO: Refactor this later
            packageDirPath = packageDirPath.replaceAll(Matcher.quoteReplacement(File.separator), ".");
        }

        return LoggerFactory.getLogger(ballerinaRootLogger.getName() + "." + packageDirPath);
    }
}
