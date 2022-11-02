// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.ballerinalang.testerina.natives.io;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangExceptionHelper;
import io.ballerina.runtime.internal.util.exceptions.RuntimeErrors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * File utility functions for the Testerina module.
 *
 * @since 2201.3.0
 */
public class FileUtils {

    public static void writeContent(BString targetPath, BString content) throws Exception {

        // Escape the control characters of the JSON string
        Pattern pattern = Pattern.compile("[\n\t]");
        Matcher matcher = pattern.matcher(content.getValue());
        StringBuilder stringBuilder = new StringBuilder();
        while (matcher.find()) {
            switch (matcher.group()) {
                case "\n":
                    matcher.appendReplacement(stringBuilder, "\\\\n");
                    break;
                case "\t":
                    matcher.appendReplacement(stringBuilder, "\\\\t");
                    break;
            }
        }
        matcher.appendTail(stringBuilder);


        File jsonFile = new File(Paths.get(targetPath.getValue()).toString());
        jsonFile.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(jsonFile);
        try (Writer writer = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
            writer.write(new String(stringBuilder.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            writer.flush();
        }
    }

    public static BString readContent(BString targetPath) {
        if (fileExists(targetPath)) {
            try {
                String readString = Files.readString(Paths.get(targetPath.getValue()));
                return StringUtils.fromString(readString);
            } catch (IOException e) {
                throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR);
            }
        }
        throw BLangExceptionHelper.getRuntimeException(RuntimeErrors.OPERATION_NOT_SUPPORTED_ERROR);
    }

    public static boolean fileExists(BString filePath) {
        return Files.exists(Paths.get(filePath.getValue()));
    }
}
