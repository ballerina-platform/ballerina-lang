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
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;
import org.apache.commons.io.FileUtils;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_JSON;
import static org.ballerinalang.central.client.CentralClientConstants.BALLERINA_DEV_CENTRAL;
import static org.ballerinalang.central.client.CentralClientConstants.BALLERINA_STAGE_CENTRAL;
import static org.ballerinalang.central.client.CentralClientConstants.BYTES_FOR_KB;
import static org.ballerinalang.central.client.CentralClientConstants.DEV_REPO;
import static org.ballerinalang.central.client.CentralClientConstants.PRODUCTION_REPO;
import static org.ballerinalang.central.client.CentralClientConstants.PROGRESS_BAR_BYTE_THRESHOLD;
import static org.ballerinalang.central.client.CentralClientConstants.RESOLVED_REQUESTED_URI;
import static org.ballerinalang.central.client.CentralClientConstants.SHA256;
import static org.ballerinalang.central.client.CentralClientConstants.SHA256_ALGORITHM;
import static org.ballerinalang.central.client.CentralClientConstants.STAGING_REPO;
import static org.ballerinalang.central.client.CentralClientConstants.UPDATE_INTERVAL_MILLIS;

/**
 * Utils class for this package.
 */
public class Utils {

    public static final String DEPRECATED_META_FILE_NAME = "deprecated.txt";
    public static final boolean SET_BALLERINA_STAGE_CENTRAL = Boolean.parseBoolean(
            System.getenv(BALLERINA_STAGE_CENTRAL));
    public static final boolean SET_BALLERINA_DEV_CENTRAL = Boolean.parseBoolean(
            System.getenv(BALLERINA_DEV_CENTRAL));

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
     * @param balaDownloadResponse http response for downloading the bala file
     * @param pkgPathInBalaCache   package path in bala cache,
     *                             <user.home>.ballerina/bala_cache/<org-name>/<pkg-name>
     * @param pkgOrg               package org
     * @param pkgName              package name
     * @param isNightlyBuild       is nightly build
     * @param deprecationMsg       deprecation message for deprecated packages
     * @param newUrl               new redirect url
     * @param contentDisposition   content disposition header
     * @param outStream            Output print stream
     * @param logFormatter         log formatter
     */
    public static void createBalaInHomeRepo(Response balaDownloadResponse, Path pkgPathInBalaCache, String pkgOrg,
            String pkgName, boolean isNightlyBuild, String deprecationMsg,
            String newUrl, String contentDisposition, PrintStream outStream,
            LogFormatter logFormatter, String trueDigest)
            throws CentralClientException {

        long responseContentLength = 0;
        Optional<ResponseBody> downloadBody = Optional.ofNullable(balaDownloadResponse.body());
        if (downloadBody.isPresent()) {
            long contentLength = downloadBody.get().contentLength();
            if (contentLength <= 0) {
                downloadBody.get().close();
                throw new CentralClientException(
                        logFormatter.formatLog("invalid response from the server, please try again"));
            } else {
                responseContentLength = contentLength;
            }
        }

        String resolvedURI = balaDownloadResponse.header(RESOLVED_REQUESTED_URI);
        if (resolvedURI == null || resolvedURI.isEmpty()) {
            resolvedURI = newUrl;
        }
        String[] uriParts = resolvedURI.split("/");
        String pkgVersion = uriParts[uriParts.length - 2];

        String validPkgVersion = validatePackageVersion(pkgVersion, logFormatter);
        String balaFile = getBalaFileName(contentDisposition, uriParts[uriParts.length - 1]);
        String platform = getPlatformFromBala(balaFile, pkgName, validPkgVersion);
        Path balaCacheWithPkgPath = pkgPathInBalaCache.resolve(validPkgVersion).resolve(platform);
        // <user.home>.ballerina/bala_cache/<org-name>/<pkg-name>/<pkg-version>

        try {
            if (Files.isDirectory(balaCacheWithPkgPath) && Files.list(balaCacheWithPkgPath).findAny().isPresent()) {
                // update the existing deprecation details
                Path deprecatedFilePath = balaCacheWithPkgPath.resolve(DEPRECATED_META_FILE_NAME);
                if (deprecatedFilePath.toFile().exists() && deprecationMsg == null) {
                    // delete deprecated file if it exists
                    Files.delete(deprecatedFilePath);
                } else if (deprecationMsg != null) {
                    // write deprecation details to the file
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(deprecatedFilePath.toFile(),
                            Charset.defaultCharset()))) {
                        writer.write(deprecationMsg);
                    }
                }

                downloadBody.ifPresent(ResponseBody::close);
                throw new PackageAlreadyExistsException(
                        logFormatter.formatLog("package already exists in the home repository: " +
                                balaCacheWithPkgPath.toString()));
            }
        } catch (IOException e) {
            downloadBody.ifPresent(ResponseBody::close);
            throw new PackageAlreadyExistsException(
                    logFormatter.formatLog("error accessing bala : " + balaCacheWithPkgPath.toString()));
        }

        // Create the following temp path
        // bala/<org-name>/<pkg-name>/<pkg-version_temp/<platform>
        Path tempPath = pkgPathInBalaCache.resolve(validPkgVersion + "_temp").resolve(platform);
        createBalaFileDirectory(tempPath, logFormatter);

        // Write balaFiles to tempPath
        writeBalaFile(balaDownloadResponse, tempPath.resolve(balaFile),
                pkgOrg + "/" + pkgName + ":" + validPkgVersion, responseContentLength,
                outStream, logFormatter, pkgPathInBalaCache.resolve(validPkgVersion), trueDigest);

        // Once files are written to temp path, rename temp path with platform name
        try {
            File tempDir = tempPath.getParent().toFile();
            File platformDir = balaCacheWithPkgPath.getParent().toFile();

            if (!tempDir.renameTo(platformDir)) {
                throw new CentralClientException(logFormatter.formatLog("error creating directory for bala file"));
            }
        } catch (NullPointerException e) {
            throw new CentralClientException(logFormatter.formatLog("error creating directory for bala file :"
                    + e.getMessage()));
        }

        handleNightlyBuild(isNightlyBuild, balaCacheWithPkgPath, logFormatter);
        handlePackageDeprecation(deprecationMsg, balaCacheWithPkgPath, logFormatter);
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
     * @param balaDownloadResponse http bala file download response
     * @param balaPath             path of the bala file
     * @param fullPkgName          full package name,
     *                             <org-name>/<pkg-name>:<pkg-version>
     * @param resContentLength     response content length
     * @param outStream            Output print stream
     * @param logFormatter         log formatter
     * @param homeRepo             path of the repo bala file is saved to
     */
    static void writeBalaFile(Response balaDownloadResponse, Path balaPath, String fullPkgName, long resContentLength,
            PrintStream outStream, LogFormatter logFormatter, Path homeRepo, String trueDigest)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.ofNullable(balaDownloadResponse.body());
        if (body.isPresent()) {
            try {
                try (InputStream inputStream = body.get().byteStream();
                        FileOutputStream outputStream = new FileOutputStream(balaPath.toString())) {
                    if (outStream == null) {
                        writeAndHandleProgressQuietly(inputStream, outputStream);
                    } else {
                        writeAndHandleProgress(inputStream, outputStream, resContentLength / 1024, fullPkgName,
                                outStream, logFormatter, homeRepo);
                    }
                } catch (IOException e) {
                    throw new CentralClientException(
                            logFormatter.formatLog("error occurred copying the bala file: " + e.getMessage()));
                }
                try {
                    extractBala(balaPath, Optional.of(balaPath.getParent()).get(), trueDigest, fullPkgName, outStream);
                    Files.delete(balaPath);
                } catch (IOException | CentralClientException e) {
                    throw new CentralClientException(
                            logFormatter.formatLog("error occurred extracting the bala file: " + e.getMessage()));
                }
            } finally {
                body.get().close();
            }
        } else {
            throw new CentralClientException(
                    logFormatter.formatLog("error occurred extracting bytes of bala file: " + fullPkgName));
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
                createMetaFile(nightlyBuildMetaFile, logFormatter, "error occurred while creating nightly.build file.");
            }
        }
    }

    /**
     * Handle package deprecation.
     *
     * @param deprecateMsg         deprecated message
     * @param balaCacheWithPkgPath bala cache with package path
     * @param logFormatter         log formatter
     */
    private static void handlePackageDeprecation(String deprecateMsg, Path balaCacheWithPkgPath,
            LogFormatter logFormatter) throws CentralClientException {
        if (deprecateMsg != null) {
            // If its a deprecated package tag a file to denote as deprecated
            Path deprecateMsgFile = Paths.get(balaCacheWithPkgPath.toString(), DEPRECATED_META_FILE_NAME);
            if (!deprecateMsgFile.toFile().exists()) {
                createMetaFile(deprecateMsgFile, logFormatter,
                        "error occurred while creating the file '" + DEPRECATED_META_FILE_NAME + "'");
            }
            writeDeprecatedMsg(deprecateMsgFile, logFormatter, deprecateMsg);
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
     * @param homeRepo      path of the repo bala file is saved to
     */
    private static void writeAndHandleProgress(InputStream inputStream, FileOutputStream outputStream,
            long totalSizeInKB, String fullPkgName, PrintStream outStream, LogFormatter logFormatter,
            Path homeRepo) throws IOException {
        int count;
        byte[] buffer = new byte[1024];
        String remoteRepo = getRemoteRepo();
        String progressBarTask = fullPkgName + " [" + remoteRepo + " ->" + homeRepo + "] ";
        try (ProgressBar progressBar = new ProgressBar(progressBarTask, totalSizeInKB, 1000,
                outStream, ProgressBarStyle.ASCII, " KB", 1)) {
            while ((count = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, count);
                progressBar.step();
            }
        } finally {
            outStream.println(logFormatter.formatLog(fullPkgName + " pulled from central successfully"));
        }
    }

    private static void writeAndHandleProgressQuietly(InputStream inputStream, FileOutputStream outputStream)
            throws IOException {
        int count;
        byte[] buffer = new byte[1024];

        while ((count = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, count);
        }
    }

    /**
     * Create meta file in given path.
     *
     * @param metaFilePath meta file path
     * @param logFormatter log formatter
     */
    private static void createMetaFile(Path metaFilePath, LogFormatter logFormatter, String message)
            throws CentralClientException {
        try {
            Files.createFile(metaFilePath);
        } catch (Exception e) {
            throw new CentralClientException(
                    logFormatter.formatLog(message));
        }
    }

    /**
     * Write deprecated message to meta file.
     *
     * @param metaFilePath deprecated message meta file path
     * @param logFormatter log formatter
     */
    private static void writeDeprecatedMsg(Path metaFilePath, LogFormatter logFormatter, String message)
            throws CentralClientException {
        if (metaFilePath.toFile().exists()) {
            try (FileWriter fileWriter = new FileWriter(metaFilePath.toAbsolutePath().toString(),
                    Charset.defaultCharset());
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
                bufferedWriter.write(message);
            } catch (IOException e) {
                throw new CentralClientException(
                        logFormatter.formatLog("error occurred while writing deprecation message to the file '"
                                + DEPRECATED_META_FILE_NAME + "'"));
            }
        }
    }

    /**
     * Get array like string as a list of strings. eg: `"["a", "b", "c"]"`
     *
     * @param arrayString array like string
     * @return converted list of strings
     */
    static List<String> getAsList(String arrayString) {
        return new Gson().fromJson(arrayString, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * Check if content type is json.
     *
     * @param contentType the content type
     * @return true if its json, else false
     */
    static boolean isApplicationJsonContentType(String contentType) {
        return contentType.startsWith(APPLICATION_JSON);
    }

    private static String getPlatformFromBala(String balaName, String packageName, String version) {
        return balaName.split(packageName + "-")[1].split("-" + version)[0];
    }

    private static void extractBala(Path balaFilePath, Path balaFileDestPath, String trueDigest, String packageName,
            PrintStream outStream)
            throws IOException, CentralClientException {
        Files.createDirectories(balaFileDestPath);
        URI zipURI = URI.create("jar:" + balaFilePath.toUri().toString());

        // If the hash value is not matching , throw an exception.
        if (!trueDigest.equals(SHA256 + checkHash(balaFilePath.toString(), SHA256_ALGORITHM))) {
            StringBuilder warning = new StringBuilder(
                    String.format("*************************************************************%n" +
        "* WARNING: Certain packages may have originated from sources other than the official distributors. *%n" +
        "*************************************************************%n%n" +
        "* Verification failed: The hash value of the following package could not be confirmed. %n" +
        packageName +
        "%n"));
            if (outStream != null) {
                outStream.println(warning.toString());
            }
        }

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

    public static String checkHash(String filePath, String algorithm) throws CentralClientException {
        try {
            byte[] data = Files.readAllBytes(Paths.get(filePath));
            byte[] hash = MessageDigest.getInstance(algorithm).digest(data);
            return new BigInteger(1, hash).toString(16);
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new CentralClientException("Unable to calculate the hash value of the file: " + filePath);
        }
    }

    static String getBearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }

    /**
     * Custom request body implementation that indicate the number of bytes written
     * using a progress bar.
     */
    public static class ProgressRequestBody extends RequestBody {
        private final RequestBody reqBody;
        private final String task;
        private final PrintStream out;

        public ProgressRequestBody(RequestBody reqBody, String task, PrintStream out) {
            this.reqBody = reqBody;
            this.task = task;
            this.out = out;
        }

        @Override
        public MediaType contentType() {
            return this.reqBody.contentType();
        }

        @Override
        public long contentLength() throws IOException {
            return this.reqBody.contentLength();
        }

        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            final long totalBytes = contentLength();
            long byteConverter;
            String unitName;

            if (totalBytes < BYTES_FOR_KB * PROGRESS_BAR_BYTE_THRESHOLD) {
                // use bytes for progress bar if payload is less than 5 KB
                byteConverter = 1;
                unitName = " B";
            } else if (totalBytes < BYTES_FOR_KB * BYTES_FOR_KB * PROGRESS_BAR_BYTE_THRESHOLD) {
                // use kilobytes for progress bar if payload is less than 5 MB
                byteConverter = BYTES_FOR_KB;
                unitName = " KB";
            } else { // else use megabytes for progress bar.
                byteConverter = BYTES_FOR_KB * BYTES_FOR_KB;
                unitName = " MB";
            }

            ProgressBar progressBar = new ProgressBar(task, contentLength(), UPDATE_INTERVAL_MILLIS, out,
                    ProgressBarStyle.ASCII, unitName, byteConverter);
            CountingSink countingSink = new CountingSink(sink, progressBar);
            BufferedSink progressSink = Okio.buffer(countingSink);
            this.reqBody.writeTo(progressSink);
            progressSink.flush();
            progressBar.close();
        }
    }

    private static class CountingSink extends ForwardingSink {
        private long bytesWritten = 0;
        private final ProgressBar progressBar;

        public CountingSink(Sink delegate, ProgressBar progressBar) {
            super(delegate);
            this.progressBar = progressBar;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            this.bytesWritten += byteCount;
            this.progressBar.stepTo(bytesWritten);
        }
    }

    /**
     * A listener interface that allows tracking byte writing.
     */
    public interface ProgressListener {
        void onRequestProgress(long bytesWritten, long contentLength) throws IOException;
    }

    /**
     * Get the remote repo URL.
     *
     * @return URL of the remote repository
     */
    public static String getRemoteRepo() {
        if (SET_BALLERINA_STAGE_CENTRAL) {
            return STAGING_REPO;
        } else if (SET_BALLERINA_DEV_CENTRAL) {
            return DEV_REPO;
        }
        return PRODUCTION_REPO;
    }
}
