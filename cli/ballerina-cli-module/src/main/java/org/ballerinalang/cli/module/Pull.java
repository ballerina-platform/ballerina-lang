// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.cli.module;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.cli.module.util.ErrorUtil;
import org.ballerinalang.cli.module.util.Utils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.core.HttpHeaders;

import static org.ballerinalang.cli.module.util.CliModuleConstants.BALLERINA_PLATFORM;
import static org.ballerinalang.cli.module.util.CliModuleConstants.BAL_LANG_SPEC_VERSION;
import static org.ballerinalang.cli.module.util.CliModuleConstants.IDENTITY;
import static org.ballerinalang.cli.module.util.CliModuleConstants.RESOLVED_REQUESTED_URI;
import static org.ballerinalang.cli.module.util.Utils.convertToUrl;
import static org.ballerinalang.cli.module.util.Utils.createHttpUrlConnection;
import static org.ballerinalang.cli.module.util.Utils.getStatusCode;
import static org.ballerinalang.cli.module.util.Utils.initializeSsl;
import static org.ballerinalang.cli.module.util.Utils.setRequestMethod;

/**
 * This class is pulling the modules from ballerina central.
 *
 * @since 1.2.0
 */
public class Pull {
    private static final String VERSION_REGEX = "(\\d+\\.)(\\d+\\.)(\\d+)";
    private static PrintStream outStream = System.out;
    private static DefaultLogFormatter logFormatter = new DefaultLogFormatter();

    private Pull() {
    }

    /**
     * Execute pull command.
     *
     * @param url                   remote uri of the central
     * @param modulePathInBaloCache module path in balo cache, user.home/.ballerina/balo_cache/org_name/module_name
     * @param moduleNameWithOrg     module name with org, org_name/module_name
     * @param proxyHost             proxy host
     * @param proxyPort             proxy port
     * @param proxyUsername         proxy username
     * @param proxyPassword         proxy password
     * @param supportedVersionRange supported version range
     * @param isBuild               pulling happens when building
     * @param isNightlyBuild        is nightly build
     * @param langSpecVersion       lang spec version
     * @param platform              supported version
     * @param clientId              client version
     */
    public static void execute(String url, String modulePathInBaloCache, String moduleNameWithOrg, String proxyHost,
            int proxyPort, String proxyUsername, String proxyPassword, String supportedVersionRange, boolean isBuild,
            boolean isNightlyBuild, String langSpecVersion, String platform, String clientId) {
        if (isBuild) {
            logFormatter = new BuildLogFormatter();
        }

        HttpURLConnection conn = null;
        try {
            initializeSsl();
            conn = createHttpUrlConnection(convertToUrl(url + supportedVersionRange), proxyHost, proxyPort,
                    proxyUsername, proxyPassword);

            conn.setInstanceFollowRedirects(false);
            setRequestMethod(conn, Utils.RequestMethod.GET);

            // Set headers
            conn.setRequestProperty(BALLERINA_PLATFORM, platform);
            conn.setRequestProperty(BAL_LANG_SPEC_VERSION, langSpecVersion);
            conn.setRequestProperty(HttpHeaders.ACCEPT_ENCODING, IDENTITY);
            conn.setRequestProperty(HttpHeaders.USER_AGENT, clientId);

            boolean redirect = false;
            // 302 - Module is found
            // Other - Error occurred, json returned with the error message
            if (getStatusCode(conn) == HttpURLConnection.HTTP_MOVED_TEMP) {
                redirect = true;
            } else {
                handleErrorResponse(conn, url, moduleNameWithOrg);
            }

            if (redirect) {
                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField(HttpHeaders.LOCATION);
                String contentDisposition = conn.getHeaderField(HttpHeaders.CONTENT_DISPOSITION);

                conn = createHttpUrlConnection(convertToUrl(newUrl), proxyHost, proxyPort, proxyUsername,
                        proxyPassword);
                conn.setRequestProperty(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

                createBaloInHomeRepo(conn, modulePathInBaloCache, moduleNameWithOrg, isNightlyBuild, newUrl,
                        contentDisposition);
            }
        } catch (Exception e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            Authenticator.setDefault(null);
        }
    }

    /**
     * Create the balo in home repo.
     *
     * @param conn                  http connection
     * @param modulePathInBaloCache module path in balo cache, <user.home>.ballerina/balo_cache/<org-name>/<module-name>
     * @param moduleNameWithOrg     module name with org, <org-name>/<module-name>
     * @param isNightlyBuild        is nightly build
     * @param newUrl                new redirect url
     * @param contentDisposition    content disposition header
     */
    private static void createBaloInHomeRepo(HttpURLConnection conn, String modulePathInBaloCache,
            String moduleNameWithOrg, boolean isNightlyBuild, String newUrl, String contentDisposition) {
        long responseContentLength = conn.getContentLengthLong();
        if (responseContentLength <= 0) {
            createError("invalid response from the server, please try again");
        }
        String resolvedURI = conn.getHeaderField(RESOLVED_REQUESTED_URI);
        if (resolvedURI == null || resolvedURI.equals("")) {
            resolvedURI = newUrl;
        }
        String[] uriParts = resolvedURI.split("/");
        String moduleVersion = uriParts[uriParts.length - 3];

        validateModuleVersion(moduleVersion);
        String baloFile = getBaloFileName(contentDisposition, uriParts[uriParts.length - 1]);
        Path baloCacheWithModulePath = Paths.get(modulePathInBaloCache, moduleVersion);
        //<user.home>.ballerina/balo_cache/<org-name>/<module-name>/<module-version>

        Path baloPath = Paths.get(baloCacheWithModulePath.toString(), baloFile);
        if (baloPath.toFile().exists()) {
            createError("module already exists in the home repository: " + baloPath.toString());
        }

        createBaloFileDirectory(baloCacheWithModulePath);
        writeBaloFile(conn, baloPath, moduleNameWithOrg + ":" + moduleVersion, responseContentLength);
        handleNightlyBuild(isNightlyBuild, baloCacheWithModulePath);
    }

    /**
     * Write balo file to the home repo.
     *
     * @param conn             http connection
     * @param baloPath         path of the balo file
     * @param fullModuleName   full module name, <org-name>/<module-name>:<module-version>
     * @param resContentLength response content length
     */
    private static void writeBaloFile(HttpURLConnection conn, Path baloPath, String fullModuleName,
            long resContentLength) {
        try (InputStream inputStream = conn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(baloPath.toString())) {
            writeAndHandleProgress(inputStream, outputStream, resContentLength / 1024, fullModuleName);
        } catch (IOException e) {
            createError("error occurred copying the balo file: " + e.getMessage());
        }
    }

    /**
     * Show progress of the writing the balo file.
     *
     * @param inputStream    response input stream
     * @param outputStream   home repo balo file output stream
     * @param totalSizeInKB  response input stream size in kb
     * @param fullModuleName full module name, <org-name>/<module-name>:<module-version>
     */
    private static void writeAndHandleProgress(InputStream inputStream, FileOutputStream outputStream,
            long totalSizeInKB, String fullModuleName) {
        int count;
        byte[] buffer = new byte[1024];

        try (ProgressBar progressBar = new ProgressBar(fullModuleName + " [central.ballerina.io -> home repo] ",
                totalSizeInKB, 1000, outStream, ProgressBarStyle.ASCII, " KB", 1)) {
            while ((count = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
                progressBar.step();
            }
        } catch (IOException e) {
            outStream.println(logFormatter.formatLog(fullModuleName + "pulling the module from central failed"));
        } finally {
            outStream.println(logFormatter.formatLog(fullModuleName + " pulled from central successfully"));
        }
    }

    /**
     * Handle nightly build.
     *
     * @param isNightlyBuild          is nightly build
     * @param baloCacheWithModulePath balo cache with module path
     */
    private static void handleNightlyBuild(boolean isNightlyBuild, Path baloCacheWithModulePath) {
        if (isNightlyBuild) {
            // If its a nightly build tag the file as a module from nightly
            Path nightlyBuildMetaFile = Paths.get(baloCacheWithModulePath.toString(), "nightly.build");
            if (!nightlyBuildMetaFile.toFile().exists()) {
                createNightlyBuildMetaFile(nightlyBuildMetaFile);
            }
        }
    }

    /**
     * Validate module version with the regex.
     *
     * @param moduleVersion module version
     */
    private static void validateModuleVersion(String moduleVersion) {
        if (!moduleVersion.matches(VERSION_REGEX)) {
            createError("module version could not be detected");
        }
    }

    /**
     * Create nightly build meta file.
     *
     * @param nightlyBuildMetaFilePath nightly build meta file path
     */
    private static void createNightlyBuildMetaFile(Path nightlyBuildMetaFilePath) {
        try {
            Files.createFile(nightlyBuildMetaFilePath);
        } catch (Exception e) {
            createError("error occurred while creating nightly.build file.");
        }
    }

    /**
     * Create balo file directory.
     *
     * @param fullPathToStoreBalo full path to store the balo file
     *                            <user.home>.ballerina/balo_cache/<org-name>/<module-name>/<module-version>
     */
    private static void createBaloFileDirectory(Path fullPathToStoreBalo) {
        try {
            Files.createDirectory(fullPathToStoreBalo);
        } catch (IOException e) {
            createError("error creating directory for balo file");
        }
    }

    /**
     * Handle error response.
     *
     * @param conn           http connection
     * @param url            remote repository url
     * @param moduleFullName module name with org and version
     */
    private static void handleErrorResponse(HttpURLConnection conn, String url, String moduleFullName) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            BMap payload = (BMap) JSONParser.parse(result.toString());
            createError("error: " + payload.getStringValue(StringUtils.fromString("message")));
        } catch (IOException e) {
            createError("failed to pull the module '" + moduleFullName + "' from the remote repository '" + url + "'");
        }
    }

    /**
     * Create error using log formatter.
     *
     * @param errMessage error message
     */
    private static void createError(String errMessage) {
        throw ErrorUtil.createCommandException(logFormatter.formatLog(errMessage));
    }

    /**
     * Get balo file name from content disposition header.
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
}

/**
 * Default log formatter class.
 */
class DefaultLogFormatter {
    String formatLog(String msg) {
        return msg;
    }
}

/**
 * Build log formatter class which used to log the message for build command.
 */
class BuildLogFormatter extends DefaultLogFormatter {
    @Override String formatLog(String msg) {
        return "\t" + msg;
    }
}
