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
package org.ballerinalang.sample;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.packerina.BuilderUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This is Main class used to generate sample balx files.
 *
 * @since 0.95.2
 */
public class SampleGenerator {
    private static final Logger log = LoggerFactory.getLogger(SampleGenerator.class);

    public static void main(String[] args) {
        validateArgs(args);
        String[] filePathParts = args[1].split("/");
        String sampleName = filePathParts[filePathParts.length - 1];
        log.info("generating balx file for sample - " + sampleName);

        // Get source root path.
        Path userDir = Paths.get(args[0]);
        String filePath = BCompileUtil.concatFileName(args[1], userDir);
        Path sourceRootPath = Paths.get(filePath);
        Path packagePath = Paths.get(args[2]);
        String targetFilePath = BCompileUtil.concatFileName(args[3], userDir);
        Path targetPath = Paths.get(targetFilePath);

        BuilderUtils.compileAndWrite(sourceRootPath, packagePath, targetPath);
        log.info("balx generated successfully for sample - " + sampleName);
    }

    private static void validateArgs(String[] args) {
        if (args == null || args.length == 0) {
            throw new BallerinaException("Invalid number of arguments, require 4");
        }
        if (args.length > 4) {
            throw new BallerinaException("too many arguments given, " + args.length);
        }
    }
}
