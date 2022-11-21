/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs.utils;

import org.ballerinalang.docgen.docs.BallerinaDocConstants;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Util methods used for doc generation.
 */
public class BallerinaDocUtils {

    private static final boolean debugEnabled = "true".equals(System.getProperty(
            BallerinaDocConstants.ENABLE_DEBUG_LOGS));
    private static final PrintStream out = System.out;

    public static void packageToZipFile(String sourceDirPath, String zipFilePath) throws IOException {
        Path p = Files.createFile(Paths.get(zipFilePath));
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(p))) {
            Path pp = Paths.get(sourceDirPath);
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            //Ignore
                        }
                    });
        }
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static String getSummary(String description) {
        Pattern pattern = Pattern.compile("^(#+.*\\n+(\\[?!.*\\n+)*)?(.*\\n?)");
        Matcher matcher = pattern.matcher(description);
        if (matcher.find()) {
            return matcher.group(3);
        } else {
            return "";
        }
    }

    public static String getPackageMdContent(String docPath) throws IOException {
        String packageMdContent = readFileAsString(docPath);
        String[] contentArr = packageMdContent.split("\n");
        String mdContent = "";
        for (String line : contentArr) {
            if (!line.startsWith("[//]:")) {
                mdContent = mdContent + "\n" + line;
            }
        }
        return mdContent;
    }

    public static String readFileAsString(String path) throws IOException {
        InputStream is = new FileInputStream(path);
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
            br = new BufferedReader(inputStreamReader);
            String content = br.readLine();
            if (content == null) {
                return sb.toString();
            }
            sb.append(content);

            while ((content = br.readLine()) != null) {
                sb.append('\n').append(content);
            }
            sb.append('\n');
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException ignore) {
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
        }
        return sb.toString();
    }
}
