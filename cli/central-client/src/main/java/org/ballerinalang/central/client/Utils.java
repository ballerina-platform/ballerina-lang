/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.central.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.ballerinalang.central.client.CentralClientConstants.RESOLVED_REQUESTED_URI;
import static org.ballerinalang.central.client.CentralClientConstants.SSL;
import static org.ballerinalang.central.client.CentralClientConstants.VERSION_REGEX;

/**
 * Utils class for this package.
 */
public class Utils {

    private Utils() {
    }

    /**
     * Request method types.
     */
    public enum RequestMethod {
        GET, POST
    }

    private static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            //No need to implement.
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            //No need to implement.
        }
    } };

    /**
     * Create the balo in home repo.
     *
     * @param conn               http connection
     * @param pkgPathInBaloCache package path in balo cache, <user.home>.ballerina/balo_cache/<org-name>/<pkg-name>
     * @param pkgNameWithOrg     package name with org, <org-name>/<pkg-name>
     * @param isNightlyBuild     is nightly build
     * @param newUrl             new redirect url
     * @param contentDisposition content disposition header
     * @param outStream          Output print stream
     * @param logFormatter       log formatter
     */
    public static void createBaloInHomeRepo(HttpURLConnection conn, Path pkgPathInBaloCache, String pkgNameWithOrg,
            boolean isNightlyBuild, String newUrl, String contentDisposition, PrintStream outStream,
            LogFormatter logFormatter) throws CentralClientException {
        long responseContentLength = conn.getContentLengthLong();
        if (responseContentLength <= 0) {
            throw new CentralClientException(
                    logFormatter.formatLog("invalid response from the server, please try again"));
        }
        String resolvedURI = conn.getHeaderField(RESOLVED_REQUESTED_URI);
        if (resolvedURI == null || resolvedURI.equals("")) {
            resolvedURI = newUrl;
        }
        String[] uriParts = resolvedURI.split("/");
        String pkgVersion = uriParts[uriParts.length - 2];

        validatePackageVersion(pkgVersion, logFormatter);
        String baloFile = getBaloFileName(contentDisposition, uriParts[uriParts.length - 1]);
        Path baloCacheWithPkgPath = pkgPathInBaloCache.resolve(pkgVersion);
        //<user.home>.ballerina/balo_cache/<org-name>/<pkg-name>/<pkg-version>

        Path baloPath = Paths.get(baloCacheWithPkgPath.toString(), baloFile);
        if (baloPath.toFile().exists()) {
            throw new PackageAlreadyExistsException(
                    logFormatter.formatLog("package already exists in the home repository: " + baloPath.toString()));
        }

        createBaloFileDirectory(baloCacheWithPkgPath, logFormatter);
        writeBaloFile(conn, baloPath, pkgNameWithOrg + ":" + pkgVersion, responseContentLength, outStream,
                      logFormatter);
        handleNightlyBuild(isNightlyBuild, baloCacheWithPkgPath, logFormatter);
    }

    /**
     * Validate package version with the regex.
     *
     * @param pkgVersion   package version
     * @param logFormatter log formatter
     */
    static void validatePackageVersion(String pkgVersion, LogFormatter logFormatter) throws CentralClientException {
        if (!pkgVersion.matches(VERSION_REGEX)) {
            throw new CentralClientException(logFormatter.formatLog("package version could not be detected"));
        }
    }

    /**
     * Get balo file name from content disposition header if available.
     *
     * @param contentDisposition content disposition header value
     * @param baloFile           balo file name taken from RESOLVED_REQUESTED_URI
     * @return balo file name
     */
    private static String getBaloFileName(String contentDisposition, String baloFile) {
        if (contentDisposition != null && !contentDisposition.equals("")) {
            return contentDisposition.substring("attachment; filename=".length());
        } else {
            return baloFile;
        }
    }

    /**
     * Create balo file directory.
     *
     * @param fullPathToStoreBalo full path to store the balo file
     *                            <user.home>.ballerina/balo_cache/<org-name>/<pkg-name>/<pkg-version>
     * @param logFormatter        log formatter
     */
    private static void createBaloFileDirectory(Path fullPathToStoreBalo, LogFormatter logFormatter)
            throws CentralClientException {
        try {
            Files.createDirectories(fullPathToStoreBalo);
        } catch (IOException e) {
            throw new CentralClientException(logFormatter.formatLog("error creating directory for balo file"));
        }
    }

    /**
     * Write balo file to the home repo.
     *
     * @param conn             http connection
     * @param baloPath         path of the balo file
     * @param fullPkgName      full package name, <org-name>/<pkg-name>:<pkg-version>
     * @param resContentLength response content length
     * @param outStream        Output print stream
     * @param logFormatter     log formatter
     */
    static void writeBaloFile(HttpURLConnection conn, Path baloPath, String fullPkgName, long resContentLength,
            PrintStream outStream, LogFormatter logFormatter) throws CentralClientException {
        try (InputStream inputStream = conn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(baloPath.toString())) {
            writeAndHandleProgress(inputStream, outputStream, resContentLength / 1024, fullPkgName, outStream,
                                   logFormatter);
        } catch (IOException e) {
            throw new CentralClientException(
                    logFormatter.formatLog("error occurred copying the balo file: " + e.getMessage()));
        }
    }

    /**
     * Handle nightly build.
     *
     * @param isNightlyBuild       is nightly build
     * @param baloCacheWithPkgPath balo cache with package path
     * @param logFormatter         log formatter
     */
    private static void handleNightlyBuild(boolean isNightlyBuild, Path baloCacheWithPkgPath,
            LogFormatter logFormatter) throws CentralClientException {
        if (isNightlyBuild) {
            // If its a nightly build tag the file as a module from nightly
            Path nightlyBuildMetaFile = Paths.get(baloCacheWithPkgPath.toString(), "nightly.build");
            if (!nightlyBuildMetaFile.toFile().exists()) {
                createNightlyBuildMetaFile(nightlyBuildMetaFile, logFormatter);
            }
        }
    }

    /**
     * Show progress of the writing the balo file.
     *
     * @param inputStream   response input stream
     * @param outputStream  home repo balo file output stream
     * @param totalSizeInKB response input stream size in kb
     * @param fullPkgName   full package name, <org-name>/<pkg-name>:<pkg-version>
     * @param outStream     Output print stream
     * @param logFormatter  log formatter
     */
    private static void writeAndHandleProgress(InputStream inputStream, FileOutputStream outputStream,
            long totalSizeInKB, String fullPkgName, PrintStream outStream, LogFormatter logFormatter) {
        int count;
        byte[] buffer = new byte[1024];

        try (ProgressBar progressBar = new ProgressBar(fullPkgName + " [central.ballerina.io -> home repo] ",
                                                       totalSizeInKB, 1000, outStream, ProgressBarStyle.ASCII, " KB",
                                                       1)) {
            while ((count = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
                progressBar.step();
            }
        } catch (IOException e) {
            outStream.println(logFormatter.formatLog(fullPkgName + "pulling the package from central failed"));
        } finally {
            outStream.println(logFormatter.formatLog(fullPkgName + " pulled from central successfully"));
        }
    }

    /**
     * Create nightly build meta file.
     *
     * @param nightlyBuildMetaFilePath nightly build meta file path
     * @param logFormatter             log formatter
     */
    private static void createNightlyBuildMetaFile(Path nightlyBuildMetaFilePath, LogFormatter logFormatter)
            throws CentralClientException {
        try {
            Files.createFile(nightlyBuildMetaFilePath);
        } catch (Exception e) {
            throw new CentralClientException(
                    logFormatter.formatLog("error occurred while creating nightly.build file."));
        }
    }

    /**
     * Convert string to URL.
     *
     * @param url string URL
     * @return URL
     */
    static URL convertToUrl(String url) throws ConnectionErrorException {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new ConnectionErrorException("malformed url:" + url, e);
        }
    }

    /**
     * Initialize SSL.
     */
    static void initializeSsl() throws CentralClientException {
        try {
            SSLContext sc = SSLContext.getInstance(SSL);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new CentralClientException("initializing SSL failed: " + e.getMessage());
        }
    }

    /**
     * Set request method of the http connection.
     *
     * @param conn   http connection
     * @param method request method
     */
    static void setRequestMethod(HttpURLConnection conn, RequestMethod method) throws CentralClientException {
        try {
            conn.setRequestMethod(method.name());
        } catch (ProtocolException e) {
            throw new CentralClientException(e.getMessage());
        }
    }

    /**
     * Get status code of http response.
     *
     * @param conn http connection
     * @return status code
     */
    static int getStatusCode(HttpURLConnection conn) throws ConnectionErrorException {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            throw new ConnectionErrorException("connection to the remote repository host failed: " + e.getMessage());
        }
    }

    /**
     * Get total file size in kb.
     *
     * @param filePath path to the file
     * @return size of the file in kb
     */
    static long getTotalFileSizeInKB(Path filePath) throws CentralClientException {
        byte[] baloContent;
        try {
            baloContent = Files.readAllBytes(filePath);
            return baloContent.length / 1024;
        } catch (IOException e) {
            throw new CentralClientException("cannot read the balo content");
        }
    }

    /**
     * Get array like string as a list of strings. eg: `"["a", "b", "c"]"`
     *
     * @param arrayString array like string
     * @return converted list of strings
     */
    static List<String> getAsList(String arrayString) {
        return new Gson().fromJson(arrayString, new TypeToken<List<String>>() { }.getType());
    }
}
