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

import com.github.zafarkhaja.semver.ParseException;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.apache.commons.io.FileUtils;
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
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.central.client.CentralClientConstants.RESOLVED_REQUESTED_URI;

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

    /**
     * Create the bala in home repo.
     *
     * @param conn               http connection
     * @param pkgPathInBalaCache package path in bala cache, <user.home>.ballerina/bala_cache/<org-name>/<pkg-name>
     * @param pkgOrg             package org
     * @param pkgName            package name
     * @param isNightlyBuild     is nightly build
     * @param newUrl             new redirect url
     * @param contentDisposition content disposition header
     * @param outStream          Output print stream
     * @param logFormatter       log formatter
     */
    public static void createBalaInHomeRepo(HttpURLConnection conn, Path pkgPathInBalaCache, String pkgOrg,
                                            String pkgName, boolean isNightlyBuild, String newUrl,
                                            String contentDisposition, PrintStream outStream, LogFormatter logFormatter)
            throws CentralClientException {
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

        String validPkgVersion = validatePackageVersion(pkgVersion, logFormatter);
        String balaFile = getBalaFileName(contentDisposition, uriParts[uriParts.length - 1]);
        String platform = getPlatformFromBala(balaFile, pkgName, validPkgVersion);
        Path balaCacheWithPkgPath = pkgPathInBalaCache.resolve(validPkgVersion).resolve(platform);
        //<user.home>.ballerina/bala_cache/<org-name>/<pkg-name>/<pkg-version>

        try {
            if (Files.isDirectory(balaCacheWithPkgPath) && Files.list(balaCacheWithPkgPath).findAny().isPresent()) {
                throw new PackageAlreadyExistsException(
                        logFormatter.formatLog("package already exists in the home repository: " +
                                balaCacheWithPkgPath.toString()));
            }
        } catch (IOException e) {
            throw new PackageAlreadyExistsException(
                    logFormatter.formatLog("error accessing bala : " + balaCacheWithPkgPath.toString()));
        }

        createBalaFileDirectory(balaCacheWithPkgPath, logFormatter);
        writeBalaFile(conn, balaCacheWithPkgPath.resolve(balaFile), pkgOrg + "/" + pkgName + ":"
                        + validPkgVersion, responseContentLength, outStream, logFormatter);
        handleNightlyBuild(isNightlyBuild, balaCacheWithPkgPath, logFormatter);
    }

    /**
     * Validate package version with the regex.
     *
     * @param pkgVersion   package version
     * @param logFormatter log formatter
     * @return valid package version
     */
    static String validatePackageVersion(String pkgVersion, LogFormatter logFormatter) throws CentralClientException {
        try {
            Version version = Version.valueOf(pkgVersion);
            return version.toString();
        } catch (IllegalArgumentException e) {
            throw new CentralClientException(logFormatter.formatLog("Version cannot be empty"));
        } catch (UnexpectedCharacterException e) {
            throw new CentralClientException(
                    logFormatter.formatLog("Invalid version: '" + pkgVersion + "'. " + e.toString()));
        } catch (ParseException e) {
            throw new CentralClientException(
                    logFormatter.formatLog("Invalid version: '" + pkgVersion + "'. " + e.toString()));
        }
    }

    /**
     * Get bala file name from content disposition header if available.
     *
     * @param contentDisposition content disposition header value
     * @param balaFile           bala file name taken from RESOLVED_REQUESTED_URI
     * @return bala file name
     */
    private static String getBalaFileName(String contentDisposition, String balaFile) {
        if (contentDisposition != null && !contentDisposition.equals("")) {
            return contentDisposition.substring("attachment; filename=".length());
        } else {
            return balaFile;
        }
    }

    /**
     * Create bala file directory.
     *
     * @param fullPathToStoreBala full path to store the bala file
     *                            <user.home>.ballerina/bala_cache/<org-name>/<pkg-name>/<pkg-version>
     * @param logFormatter        log formatter
     */
    private static void createBalaFileDirectory(Path fullPathToStoreBala, LogFormatter logFormatter)
            throws CentralClientException {
        try {
            Files.createDirectories(fullPathToStoreBala);
        } catch (IOException e) {
            throw new CentralClientException(logFormatter.formatLog("error creating directory for bala file"));
        }
    }

    /**
     * Write bala file to the home repo.
     *
     * @param conn             http connection
     * @param balaPath         path of the bala file
     * @param fullPkgName      full package name, <org-name>/<pkg-name>:<pkg-version>
     * @param resContentLength response content length
     * @param outStream        Output print stream
     * @param logFormatter     log formatter
     */
    static void writeBalaFile(HttpURLConnection conn, Path balaPath, String fullPkgName, long resContentLength,
            PrintStream outStream, LogFormatter logFormatter) throws CentralClientException {
        try (InputStream inputStream = conn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(balaPath.toString())) {
            writeAndHandleProgress(inputStream, outputStream, resContentLength / 1024, fullPkgName, outStream,
                                   logFormatter);
        } catch (IOException e) {
            throw new CentralClientException(
                    logFormatter.formatLog("error occurred copying the bala file: " + e.getMessage()));
        }
        try {
            extractBala(balaPath, Optional.of(balaPath.getParent()).get());
            Files.delete(balaPath);
        } catch (IOException e) {
            throw new CentralClientException(
                    logFormatter.formatLog("error occurred extracting the bala file: " + e.getMessage()));
        }
    }

    /**
     * Handle nightly build.
     *
     * @param isNightlyBuild       is nightly build
     * @param balaCacheWithPkgPath bala cache with package path
     * @param logFormatter         log formatter
     */
    private static void handleNightlyBuild(boolean isNightlyBuild, Path balaCacheWithPkgPath,
            LogFormatter logFormatter) throws CentralClientException {
        if (isNightlyBuild) {
            // If its a nightly build tag the file as a module from nightly
            Path nightlyBuildMetaFile = Paths.get(balaCacheWithPkgPath.toString(), "nightly.build");
            if (!nightlyBuildMetaFile.toFile().exists()) {
                createNightlyBuildMetaFile(nightlyBuildMetaFile, logFormatter);
            }
        }
    }

    /**
     * Show progress of the writing the bala file.
     *
     * @param inputStream   response input stream
     * @param outputStream  home repo bala file output stream
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
        byte[] balaContent;
        try {
            balaContent = Files.readAllBytes(filePath);
            return balaContent.length / 1024;
        } catch (IOException e) {
            throw new CentralClientException("cannot read the bala content");
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

    private static String getPlatformFromBala(String balaName, String packageName, String version) {
        return balaName.split(packageName + "-")[1].split("-" + version)[0];
    }

    private static void extractBala(Path balaFilePath, Path balaFileDestPath) throws IOException {
        Files.createDirectories(balaFileDestPath);
        URI zipURI = URI.create("jar:" + balaFilePath.toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path packageRoot = zipFileSystem.getPath("/");
            List<Path> paths = Files.walk(packageRoot).filter(path -> path != packageRoot).collect(Collectors.toList());
            for (Path path : paths) {
                Path destPath = balaFileDestPath.resolve(packageRoot.relativize(path).toString());
                // Handle overwriting existing bala
                if (destPath.toFile().isDirectory()) {
                    FileUtils.deleteDirectory(destPath.toFile());
                }
                Files.copy(path, destPath, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}
