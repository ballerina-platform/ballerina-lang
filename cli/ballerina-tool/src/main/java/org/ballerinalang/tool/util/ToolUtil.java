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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Ballerina tool utilities.
 */
public class ToolUtil {
    private static final String STAGING_URL = "https://api.staging-central.ballerina.io/update-tool";
    private static final String BALLERINA_CONFIG = "ballerina-version";
    private static final String BALLERINA_TOOLS_CONFIG = "ballerina-tools-version";

    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };

    /**
     * List distributions in the local and remote.
     * @param outStream stream outputs need to be printed
     * @param isRemote option to list distributions in the central
     */
    public static void listDistributions(PrintStream outStream, boolean isRemote) {
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
                File folder = new File(OSUtils.getDistributionsPath());
                File[] listOfFiles;
                listOfFiles = folder.listFiles();
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].isDirectory()) {
                        outStream.println(markVersion(currentBallerinaVersion, listOfFiles[i].getName()));
                    }
                }
            }
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            outStream.println("Ballerina Update service is not available");
        }
    }


    /**
     * Provide the path of currently configured Ballerina Distribution.
     * @return Used Ballerina distribution path
     */
    public static String getCurrentDist() throws IOException {
        String currentBallerinaVersion = getCurrentBallerinaVersion();
        return OSUtils.getDistributionsPath() + File.separator + currentBallerinaVersion;
//      return System.getProperty("ballerina.home");
    }

    /**
     * Provides used Ballerina version.
     * @return Used Ballerina version
     */
    private static String getCurrentBallerinaVersion() throws IOException {
        return getVersion(OSUtils.getDistributionsPath() + File.separator + BALLERINA_CONFIG);
    }

    private static void setCurrentBallerinaVersion(String version) throws IOException {
        setVersion(OSUtils.getDistributionsPath() + File.separator + BALLERINA_CONFIG, version);
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
        BufferedReader br = Files.newBufferedReader(Paths.get(path));
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

    public static boolean use(PrintStream printStream, String distribution) {
        try {
            File installFile = new File(OSUtils.getDistributionsPath() + File.separator + distribution);
            if (installFile.exists()) {
                if (distribution.equals(getCurrentBallerinaVersion())) {
                    printStream.println(distribution + " is already in use ");
                    return true;
                } else {
                    setCurrentBallerinaVersion(distribution);
                    printStream.println("Using " + distribution);
                    return true;
                }
            }
        } catch (IOException e) {
            printStream.println("Cannot use " + distribution);
        }

        return false;
    }

    public static void install(PrintStream printStream, String distribution) {
        try {
            if (!use(printStream, distribution)) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

                String distributionType = distribution.split("-")[0];
                String distributionVersion = distribution.replace(distributionType + "-", "");
                URL url = new URL(STAGING_URL + "/distributions/" + distributionVersion);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("user-agent",
                        OSUtils.getUserAgent(distributionVersion, getCurrentToolsVersion(), distributionType));
                conn.setRequestProperty("Accept", "application/json");
                if (conn.getResponseCode() == 302) {
                    String newUrl = conn.getHeaderField("Location");
                    conn = (HttpURLConnection) new URL(newUrl).openConnection();
                    conn.setRequestProperty("content-type", "binary/data");
                    download(printStream, conn, distribution);
                } else if (conn.getResponseCode() == 200) {
                    download(printStream, conn, distribution);
                } else {
                    printStream.println(distribution + " is not found ");
                }
            }
        } catch (IOException | KeyManagementException | NoSuchAlgorithmException e) {
            printStream.println("Cannot connect to the central server");
        }
    }

    public static void download(PrintStream printStream, HttpURLConnection conn,
                                String distribution) throws IOException {
        printStream.print("Downloading " + distribution);
        InputStream in = conn.getInputStream();
        String zipFileLocation = OSUtils.getDistributionsPath() + File.separator + distribution + ".zip";
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
        unzip(zipFileLocation, OSUtils.getDistributionsPath());
        setCurrentBallerinaVersion(distribution);

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }
        conn.disconnect();
        printStream.println(distribution + " is installed ");
    }

    public static void update(PrintStream printStream, String version) {
        //TODO : Get available versions, find latest patch and install that version
        install(printStream, version);
    }

    public static void remove(PrintStream outStream, String version) {
        try {
            if (version.equals(getCurrentBallerinaVersion())) {
                outStream.println("You cannot remove default Ballerina version");
            } else {
                File directory = new File(OSUtils.getDistributionsPath() + File.separator + version);
                if (directory.exists()) {
                    deleteFiles(directory.toPath(), outStream, version);
                    outStream.println(version + " deleted successfully");
                } else {
                    outStream.println(version + " does not exist");
                }
            }
        } catch (IOException e) {
            outStream.println("Error occurred while removing");
        }
    }

    /**
     * Delete files inside directories.
     *
     * @param dirPath directory path
     * @param outStream output stream
*      @param version deleting version
     * @throws IOException throw an exception if an issue occurs
     */
    public static void deleteFiles(Path dirPath, PrintStream outStream, String version) throws IOException {
        if (dirPath == null) {
            return;
        }
        Files.walk(dirPath)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        outStream.println(version + " cannot remove");
                    }
                });
    }

    private static MapValue getDistributions() throws IOException, KeyManagementException, NoSuchAlgorithmException {

        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

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

    public static void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                byte[] bytesIn = new byte[1024];
                int read = 0;
                while ((read = zipIn.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();
            } else {
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        new File(zipFilePath).delete();
    }
}

