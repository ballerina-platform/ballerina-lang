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

import io.ballerina.projects.util.ProjectConstants;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.parser.SettingsProcessor;
import org.ballerinalang.tool.LauncherUtils;
import org.wso2.ballerinalang.compiler.packaging.converters.URIDryConverter;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.ballerinalang.central.client.CentralClientConstants.RESOLVED_REQUESTED_URI;
import static org.ballerinalang.central.client.CentralClientConstants.SSL;
import static org.ballerinalang.central.client.CentralClientConstants.VERSION_REGEX;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_DEV_CENTRAL;
import static org.wso2.ballerinalang.util.RepoUtils.SET_BALLERINA_STAGE_CENTRAL;

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
     * Checks if the access token is available in Settings.toml or not.
     *
     * @return access token if its present
     */
    static String authenticate(PrintStream errStream, String ballerinaCentralCliTokenUrl, Settings settings,
            Path settingsTomlFilePath) {
        String accessToken = getAccessTokenOfCLI(settings);

        if (accessToken.isEmpty()) {
            try {
                errStream.println(
                        "Opening the web browser to " + ballerinaCentralCliTokenUrl + " for auto token update ...");

                BrowserLauncher.startInDefaultBrowser(ballerinaCentralCliTokenUrl);
            } catch (IOException e) {
                throw LauncherUtils.createLauncherException(
                        "Access token is missing in " + settingsTomlFilePath.toString()
                                + "\nAuto update failed. Please visit https://central.ballerina.io");
            }
            long modifiedTimeOfFileAtStart = getLastModifiedTimeOfFile(settingsTomlFilePath);
            TokenUpdater.execute(settingsTomlFilePath.toString());

            boolean waitForToken = true;
            while (waitForToken) {
                pause();
                long modifiedTimeOfFileAfter = getLastModifiedTimeOfFile(settingsTomlFilePath);
                if (modifiedTimeOfFileAtStart != modifiedTimeOfFileAfter) {
                    accessToken = getAccessTokenOfCLI(settings);
                    if (accessToken.isEmpty()) {
                        throw createLauncherException(
                                "Access token is missing in " + settingsTomlFilePath.toString() + "\nPlease "
                                        + "visit https://central.ballerina.io");
                    } else {
                        waitForToken = false;
                    }
                }
            }
        }
        return accessToken;
    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    public static Settings readSettings() {
        Path settingsFilePath = RepoUtils.createAndGetHomeReposPath().resolve(ProjectConstants.SETTINGS_FILE_NAME);
        try {
            return SettingsProcessor.parseTomlContentFromFile(settingsFilePath);
        } catch (IOException e) {
            return new Settings();
        }
    }

    /**
     * Initialize proxy if proxy is available in settings.toml.
     *
     * @param proxy toml model proxy
     * @return proxy
     */
    public static Proxy initializeProxy(org.ballerinalang.toml.model.Proxy proxy) {
        if (!"".equals(proxy.getHost())) {
            InetSocketAddress proxyInet = new InetSocketAddress(proxy.getHost(), proxy.getPort());
            if (!"".equals(proxy.getUserName()) && "".equals(proxy.getPassword())) {
                Authenticator authenticator = new URIDryConverter.RemoteAuthenticator();
                Authenticator.setDefault(authenticator);
            }
            return new Proxy(Proxy.Type.HTTP, proxyInet);
        }

        return null;
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    static String getAccessTokenOfCLI(Settings settings) {
        // The access token can be specified as an environment variable or in 'Settings.toml'. First we would check if
        // the access token was specified as an environment variable. If not we would read it from 'Settings.toml'
        String tokenAsEnvVar = System.getenv(ProjectDirConstants.BALLERINA_CENTRAL_ACCESS_TOKEN);
        if (tokenAsEnvVar != null) {
            return tokenAsEnvVar;
        }
        if (settings.getCentral() != null) {
            return settings.getCentral().getAccessToken();
        }
        return "";
    }

    /**
     * Pause for 3s to check if the access token is received.
     */
    private static void pause() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            throw createLauncherException("Error occurred while retrieving the access token");
        }
    }

    /**
     * Get last modified time of file.
     *
     * @param path file path
     * @return last modified time in milliseconds
     */
    static long getLastModifiedTimeOfFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return -1;
        }
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw createLauncherException("Error occurred when reading file for token " + path.toString());
        }
    }

    static String getBallerinaCentralCliTokenUrl() {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return "https://staging-central.ballerina.io/cli-token";
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return "https://dev-central.ballerina.io/cli-token";
        } else {
            return "https://central.ballerina.io/cli-token";
        }
    }

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
            LogFormatter logFormatter) {
        long responseContentLength = conn.getContentLengthLong();
        if (responseContentLength <= 0) {
            throw ErrorUtil.createCentralClientException(
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
            throw ErrorUtil.createCentralClientException(
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
    static void validatePackageVersion(String pkgVersion, LogFormatter logFormatter) {
        if (!pkgVersion.matches(VERSION_REGEX)) {
            throw ErrorUtil
                    .createCentralClientException(logFormatter.formatLog("package version could not be detected"));
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
    private static void createBaloFileDirectory(Path fullPathToStoreBalo, LogFormatter logFormatter) {
        try {
            Files.createDirectories(fullPathToStoreBalo);
        } catch (IOException e) {
            throw ErrorUtil
                    .createCentralClientException(logFormatter.formatLog("error creating directory for balo file"));
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
             PrintStream outStream, LogFormatter logFormatter) {
        try (InputStream inputStream = conn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(baloPath.toString())) {
            writeAndHandleProgress(inputStream, outputStream, resContentLength / 1024, fullPkgName, outStream,
                    logFormatter);
        } catch (IOException e) {
            throw ErrorUtil.createCentralClientException(
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
            LogFormatter logFormatter) {
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
                totalSizeInKB, 1000, outStream, ProgressBarStyle.ASCII, " KB", 1)) {
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
    private static void createNightlyBuildMetaFile(Path nightlyBuildMetaFilePath, LogFormatter logFormatter) {
        try {
            Files.createFile(nightlyBuildMetaFilePath);
        } catch (Exception e) {
            throw ErrorUtil.createCentralClientException(
                    logFormatter.formatLog("error occurred while creating nightly.build file."));
        }
    }

    /**
     * Convert string to URL.
     *
     * @param url string URL
     * @return URL
     */
    static URL convertToUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw ErrorUtil.createCentralClientException(e.getMessage());
        }
    }

    /**
     * Initialize SSL.
     */
    static void initializeSsl() {
        try {
            SSLContext sc = SSLContext.getInstance(SSL);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw ErrorUtil.createCentralClientException("initializing SSL failed: " + e.getMessage());
        }
    }

    /**
     * Set request method of the http connection.
     *
     * @param conn   http connection
     * @param method request method
     */
    static void setRequestMethod(HttpURLConnection conn, RequestMethod method) {
        try {
            conn.setRequestMethod(method.name());
        } catch (ProtocolException e) {
            throw ErrorUtil.createCentralClientException(e.getMessage());
        }
    }

    /**
     * Get status code of http response.
     *
     * @param conn http connection
     * @return status code
     */
    static int getStatusCode(HttpURLConnection conn) {
        try {
            return conn.getResponseCode();
        } catch (IOException e) {
            throw ErrorUtil
                    .createCentralClientException("connection to the remote repository host failed: " + e.getMessage());
        }
    }

    /**
     * Get total file size in kb.
     *
     * @param filePath path to the file
     * @return size of the file in kb
     */
    static long getTotalFileSizeInKB(Path filePath) {
        byte[] baloContent;
        try {
            baloContent = Files.readAllBytes(filePath);
            return baloContent.length / 1024;
        } catch (IOException e) {
            throw ErrorUtil.createCentralClientException("cannot read the balo content");
        }
    }
}
