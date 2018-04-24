/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.cli.utils;

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class for packaging both pull and push.
 *
 * @since 0.964
 */
public class ExecutorUtils {

    /**
     * Run balx that lives within jars.
     *
     * @param balxResource URI of the balx resource
     * @param isFunction   if a function or service is to be invoked
     * @param args         arguments passed to the function
     */
    public static void execute(URI balxResource, boolean isFunction, String... args) {
        initFileSystem(balxResource);
        Path baloFilePath = Paths.get(balxResource);
        ProgramFile programFile = readExecutableProgram(baloFilePath);

        if (isFunction) {
            BLangProgramRunner.runMain(programFile, args);
        } else {
            BLangProgramRunner.runService(programFile);
        }
    }

    /**
     * Init file system from jar.
     *
     * @param uri URI of the file
     */

    private static void initFileSystem(URI uri) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            FileSystems.newFileSystem(uri, env);
        } catch (Exception ignore) {
        }
    }

    /**
     * Get program file after reading the executable program i.e. balo file.
     *
     * @param baloFilePath path of the balo file
     * @return program file
     */
    private static ProgramFile readExecutableProgram(Path baloFilePath) {
        ByteArrayInputStream byteIS = null;
        try {
            byteIS = new ByteArrayInputStream(Files.readAllBytes(baloFilePath));
            ProgramFileReader reader = new ProgramFileReader();
            return reader.readProgram(byteIS);
        } catch (IOException ignore) {
        } finally {
            if (byteIS != null) {
                try {
                    byteIS.close();
                } catch (IOException ignore) {
                }
            }
        }
        return null;
    }
}
