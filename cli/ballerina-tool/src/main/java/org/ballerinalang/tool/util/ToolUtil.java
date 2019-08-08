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

import com.moandjiezana.toml.Toml;
import org.ballerinalang.jvm.JSONParser;
import org.ballerinalang.jvm.values.MapValue;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Ballerina tool utilities.
 */
public class ToolUtil {
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/update-tool";
    private static final String USER_AGENT = "jBallerina/0.992.0 (win-64) Updater/1.0";
    private static final String CONFIG_FILE_NAME = "ballerina-tool.toml";

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
                File folder = new File(OSUtils.getConfigPath());
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
    private static String getCurrentBallerinaVersion() {
        Toml toml = new Toml().read(new File(OSUtils.getConfigPath() + File.separator + CONFIG_FILE_NAME));
        return toml.getString("ballerina-version");
    }

    /**
     * Provides used Ballerina tools version.
     * @return Used Ballerina tools version.
     */
    private static String getCurrentToolsVersion() {
        Toml toml = new Toml().read(new File(OSUtils.getConfigPath() + File.separator + CONFIG_FILE_NAME));
        return toml.getString("tools-version");
    }

    /**
     * Checks used Ballerina version and mark the output
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

    public static void install(PrintStream outStream, String version) {
        outStream.println("Ballerina Update service is not available");
        //TODO : Need to implement
//        File folder = new File("../../");
//        File[] listOfFiles = folder.listFiles();
//        boolean isInstalled = false;
//
//        for (int i = 0; i < listOfFiles.length; i++) {
//            if (listOfFiles[i].isDirectory()) {
//                if (listOfFiles[i].getName().replace("jballerina-tools-", "").equals(version)) {
//                    isInstalled = true;
//                    break;
//                }
//            }
//        }
//
//        if (isInstalled) {
//            outStream.println("jballerina-tools-" + version + " is already installed");
//        } else {
//            try {
//                MapValue distributions = getDistribution(version);
//            } catch (MalformedURLException e) {
//                outStream.print(e);
//            } catch (IOException e) {
//                outStream.print(e);
//            }
//
//        }
    }

    public static void update(PrintStream outStream, String currentVersion) {
        outStream.println("Ballerina Update service is not available");
        //TODO : Need to implement
//        File folder = new File("../../");
//        File[] listOfFiles = folder.listFiles();
//        for (int i = 0; i < listOfFiles.length; i++) {
//            if (listOfFiles[i].isDirectory()) {
//                outStream.println(listOfFiles[i].getName().replace("jballerina-tools-", ""));
//            }
//        }
    }

    public static void remove(PrintStream outStream, String version) {
        if (version.equals(getCurrentBallerinaVersion())) {
            outStream.println("You cannot remove default Ballerina version");
        } else {
            File folder = new File(OSUtils.getConfigPath() + File.separator + version);

            //TODO : Check permissions
            folder.setWritable(true);
            if (folder.delete()) {
                outStream.println(version + " deleted successfully");
            } else {
                outStream.println("Error occurred while deleting " + version);
            }
        }
    }

    private static MapValue getDistributions() throws IOException {
        MapValue distributions;
        URL url = new URL(STAGING_URL + "/distributions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("user-agent", USER_AGENT);
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

    private static MapValue getDistribution(String version) throws IOException {
        MapValue distributions;
        URL url = new URL(STAGING_URL + "/distributions/" + version);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("user-agent", USER_AGENT);
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
}

