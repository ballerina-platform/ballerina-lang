/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.tool.util;

import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.values.MapValue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Ballerina tool utilities.
 */
public class ToolUtil {
    //private static final String STAGING_URL = "https://api.staging-central.ballerina.io/update-tool";
    private static final String BALLERINA_CONFIG = "ballerina-version";
    private static final String BALLERINA_TOOLS_CONFIG = "ballerina-tool-version";

    private static final String STAGING_URL = "http://localhost:3030/update-tool";
//    public static void main(String [] args) {
//        listSDKs(System.out, false);
//    //  remove(System.out, "ballerina-2.0.0-alpha");
//
//
////        try {
////            getDistribution(System.out, "1.0.0-alpha");
////        } catch(Exception e) {
////
////        }
//
//
//    }

    public static void listSDKs(PrintStream outStream, boolean isRemote) {
        try {
            String currentBallerinaVersion = getCurrentBallerinaVersion();
            if (isRemote) {
                MapValue distributions = getDistributions();
                for (int i = 0; i < distributions.getArrayValue("list").size(); i++) {
                    MapValue dist = (MapValue) distributions.getArrayValue("list").get(i);
                    outStream.println(markVersion(currentBallerinaVersion,
                            dist.getStringValue("type") + "-" + dist.getStringValue("version")));
                }
            } else {
                File folder = new File(OSUtils.getToolPath());
                File[] listOfFiles;
                listOfFiles = folder.listFiles();
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isDirectory()) {
                        outStream.println(markVersion(currentBallerinaVersion, listOfFiles[i].getName()));
                    }
                }
            }
        } catch (IOException e) {
            outStream.println("Ballerina Update service is not available");
        }
    }

    /**
     * Provides used Ballerina version.
     * @return Used Ballerina version
     */
    private static String getCurrentBallerinaVersion() throws IOException {
        return getVersion(OSUtils.getToolPath() + File.separator + BALLERINA_CONFIG);
    }

    private static void setCurrentBallerinaVersion(String version) throws IOException {
        setVersion(OSUtils.getToolPath() + File.separator + BALLERINA_CONFIG, version);
    }

    /**
     * Provides used Ballerina tools version.
     * @return Used Ballerina tools version.
     */
    private static String getCurrentToolsVersion() throws IOException {
        return getVersion(OSUtils.getToolPath() + File.separator + BALLERINA_TOOLS_CONFIG);
    }

    private static void setCurrentToolsVersion(String version) throws IOException {
        setVersion(OSUtils.getToolPath() + File.separator + BALLERINA_TOOLS_CONFIG, version);
    }


    private static String getVersion(String path) throws IOException {
        String fileName = OSUtils.getToolPath() + File.separator + BALLERINA_CONFIG;
        BufferedReader br = Files.newBufferedReader(Paths.get(fileName));
        List<String> list = br.lines().collect(Collectors.toList());
        return list.get(0);
    }

    private static void setVersion(String path, String version) throws IOException {
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.println(version);
        writer.close();
    }

    /**
     * Checks used Ballerina version and mark the output.
     * @param used Used Ballerina version
     * @param current Version needs to be checked
     * @return Marked output
     */
    private static String markVersion(String used, String current) {
        if (used.equals(current)) {
            return "* " + current;
        } else {
            return "  " + current;
        }
    }

    public static void install(PrintStream printStream, String distribution) {
        try {
            File installFile = new File(OSUtils.getToolPath() + File.separator + distribution);
            if (installFile.exists()) {
                if (distribution.equals(getCurrentBallerinaVersion())) {
                    printStream.println(distribution + " is already in use ");
                } else {
                    setCurrentBallerinaVersion(distribution);
                    printStream.println(distribution + " is installed ");
                }
            } else {
                String distributionType = distribution.split("-")[0];
                String distributionVersion = distribution.replace(distributionType + "-", "");
                URL url = new URL(STAGING_URL + "/distributions/" + distributionVersion);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("user-agent",
                        OSUtils.getUserAgent(getCurrentBallerinaVersion(), getCurrentToolsVersion(), distributionType));
                conn.setRequestProperty("Accept", "application/json");
                if (conn.getResponseCode() == 302) {
                    String newUrl = conn.getHeaderField("Location");
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setRequestProperty("content-type", "binary/data");
                    printStream.print("Downloading " + distribution);
                    InputStream in = conn.getInputStream();
                    String zipFileLocation = OSUtils.getToolPath() + distribution + ".zip";
                    FileOutputStream out = new FileOutputStream(zipFileLocation);
                    byte[] b = new byte[1024];
                    int count;
                    int progress = 0;
                    while ((count = in.read(b)) > 0) {
                        out.write(b, 0, count);
                        progress++;
                        if (progress % 1024 == 0) {
                            printStream.print(".");
                        }
                    }
                    printStream.println();
                    File zipFile = new File(zipFileLocation);
                    unzip(zipFile, OSUtils.getToolPath());
                    zipFile.delete();
                    setCurrentBallerinaVersion(distribution);

                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + conn.getResponseCode());
                    }
                    conn.disconnect();
                    printStream.println(distribution + " is installed ");
                } else {
                    printStream.println(distribution + " is not found ");
                }
            }
        } catch (IOException e) {
            printStream.println("Cannot connect to the central server");
        }
    }

    public static void update(PrintStream printStream, String version) {
        install(printStream, version);
    }

    public static void remove(PrintStream outStream, String version) {
        try {
            if (version.equals(getCurrentBallerinaVersion())) {
                outStream.println("You cannot remove default Ballerina version");
            } else {
                File directory = new File(OSUtils.getToolPath() + File.separator + version);
                if (directory.exists()) {
                    removeDirectory(directory);
                    outStream.println(version + " deleted successfully");
                } else {
                    outStream.println(version + " does not exist");
                }
            }
        } catch (IOException e) {
            outStream.println("Error occurred while removing");
        }
    }

    private static void removeDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                if (!Files.isSymbolicLink(f.toPath())) {
                    removeDirectory(f);
                }
            }
        }
        file.delete();
    }

    private static MapValue getDistributions() throws IOException {
        MapValue distributions;
        URL url = new URL(STAGING_URL + "/distributions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("user-agent",
                OSUtils.getUserAgent(getCurrentBallerinaVersion(), getCurrentToolsVersion(), "jballerina"));
        conn.setRequestProperty("Accept", "application/json");
        if (conn.getResponseCode() != 200) {
            conn.disconnect();
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        } else {
            JSONParser parser = new JSONParser();

            distributions = (MapValue) parser.parse(new InputStreamReader((conn.getInputStream())));
        }
        conn.disconnect();
        return distributions;
    }

    public static void unzip(File source, String out) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source))) {

            ZipEntry entry = zis.getNextEntry();

            while (entry != null) {

                File file = new File(out, entry.getName());

                if (entry.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();

                    if (!parent.exists()) {
                        parent.mkdirs();
                    }

                    try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {

                        byte[] buffer = new byte[Math.toIntExact(entry.getSize())];

                        int location;

                        while ((location = zis.read(buffer)) != -1) {
                            bos.write(buffer, 0, location);
                        }
                    }
                }
                entry = zis.getNextEntry();
            }
        }
    }

}

