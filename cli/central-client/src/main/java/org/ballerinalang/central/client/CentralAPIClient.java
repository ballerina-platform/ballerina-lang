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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.exceptions.NoPackageException;
import org.ballerinalang.central.client.exceptions.PackageAlreadyExistsException;
import org.ballerinalang.central.client.model.ConnectorInfo;
import org.ballerinalang.central.client.model.Error;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageNameResolutionRequest;
import org.ballerinalang.central.client.model.PackageNameResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionRequest;
import org.ballerinalang.central.client.model.PackageResolutionResponse;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.central.client.model.ToolSearchResult;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static java.net.HttpURLConnection.HTTP_BAD_GATEWAY;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT_ENCODING;
import static org.ballerinalang.central.client.CentralClientConstants.ANY_PLATFORM;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_JSON;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_OCTET_STREAM;
import static org.ballerinalang.central.client.CentralClientConstants.AUTHORIZATION;
import static org.ballerinalang.central.client.CentralClientConstants.BALA_URL;
import static org.ballerinalang.central.client.CentralClientConstants.BALLERINA_PLATFORM;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_TYPE;
import static org.ballerinalang.central.client.CentralClientConstants.DEPRECATE_MESSAGE;
import static org.ballerinalang.central.client.CentralClientConstants.DIGEST;
import static org.ballerinalang.central.client.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.CentralClientConstants.IS_DEPRECATED;
import static org.ballerinalang.central.client.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.CentralClientConstants.ORGANIZATION;
import static org.ballerinalang.central.client.CentralClientConstants.PKG_NAME;
import static org.ballerinalang.central.client.CentralClientConstants.PLATFORM;
import static org.ballerinalang.central.client.CentralClientConstants.SHA256;
import static org.ballerinalang.central.client.CentralClientConstants.SHA256_ALGORITHM;
import static org.ballerinalang.central.client.CentralClientConstants.USER_AGENT;
import static org.ballerinalang.central.client.CentralClientConstants.VERSION;
import static org.ballerinalang.central.client.Utils.ProgressRequestBody;
import static org.ballerinalang.central.client.Utils.checkHash;
import static org.ballerinalang.central.client.Utils.createBalaInHomeRepo;
import static org.ballerinalang.central.client.Utils.getAsList;
import static org.ballerinalang.central.client.Utils.getBearerToken;
import static org.ballerinalang.central.client.Utils.getRemoteRepo;
import static org.ballerinalang.central.client.Utils.isApplicationJsonContentType;
/**
 * {@code CentralAPIClient} is a client for the Central API.
 *
 * @since 2.0.0
 */
public class CentralAPIClient {

    private static final String PACKAGES = "packages";
    private static final String TOOLS = "tools";
    private static final String CONNECTORS = "connectors";
    private static final String TRIGGERS = "triggers";
    private static final String SEARCH_QUERY = "?q=";
    private static final String SEPARATOR = "/";
    private static final String RESOLVE_DEPENDENCIES = "resolve-dependencies";
    private static final String RESOLVE_MODULES = "resolve-modules";
    private static final String DEPRECATE = "deprecate";
    private static final String UN_DEPRECATE = "undeprecate";
    private static final String PACKAGE_PATH_PREFIX = SEPARATOR + PACKAGES + SEPARATOR;
    private static final String TOOL_PATH_PREFIX = SEPARATOR + TOOLS + SEPARATOR;
    private static final String CONNECTOR_PATH_PREFIX = SEPARATOR + CONNECTORS + SEPARATOR;
    private static final String TRIGGER_PATH_PREFIX = SEPARATOR + TRIGGERS + SEPARATOR;
    private static final String ERR_CANNOT_FIND_PACKAGE = "error: could not connect to remote repository to find " +
            "package: ";
    private static final String ERR_CANNOT_FIND_VERSIONS = "error: could not connect to remote repository to find " +
            "versions for: ";
    private static final String ERR_CANNOT_PUSH = "error: failed to push the package: ";
    private static final String ERR_CANNOT_PULL_PACKAGE = "error: failed to pull the package: ";
    private static final String ERR_CANNOT_SEARCH = "error: failed to search packages: ";
    private static final String ERR_CANNOT_GET_CONNECTOR = "error: failed to find connector: ";
    private static final String ERR_CANNOT_GET_TRIGGERS = "error: failed to find triggers: ";
    private static final String ERR_CANNOT_GET_TRIGGER = "error: failed to find the trigger: ";
    private static final String ERR_PACKAGE_DEPRECATE = "error: failed to deprecate the package: ";
    private static final String ERR_PACKAGE_UN_DEPRECATE = "error: failed to undo deprecation of the package: ";
    private static final String ERR_PACKAGE_RESOLUTION = "error: while connecting to central: ";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    // System property name for enabling central verbose
    public static final String SYS_PROP_CENTRAL_VERBOSE_ENABLED = "CENTRAL_VERBOSE_ENABLED";
    private static final int DEFAULT_CONNECT_TIMEOUT = 60;
    private static final int DEFAULT_READ_TIMEOUT = 60;
    private static final int DEFAULT_WRITE_TIMEOUT = 60;
    private static final int DEFAULT_CALL_TIMEOUT = 0;

    private final String baseUrl;
    private final Proxy proxy;
    private String accessToken;
    protected PrintStream outStream;
    private final boolean verboseEnabled;
    private final String proxyUsername;
    private final String proxyPassword;
    private final int connectTimeout;
    private final int readTimeout;
    private final int writeTimeout;
    private final int callTimeout;

    public CentralAPIClient(String baseUrl, Proxy proxy, String accessToken) {
        this.outStream = System.out;
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        this.accessToken = accessToken;
        this.verboseEnabled = Boolean.parseBoolean(System.getenv(SYS_PROP_CENTRAL_VERBOSE_ENABLED));
        this.proxyUsername = "";
        this.proxyPassword = "";
        this.connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        this.readTimeout = DEFAULT_READ_TIMEOUT;
        this.writeTimeout = DEFAULT_WRITE_TIMEOUT;
        this.callTimeout = DEFAULT_CALL_TIMEOUT;
    }

    public CentralAPIClient(String baseUrl, Proxy proxy, String proxyUsername, String proxyPassword,
            String accessToken, int connectionTimeout, int readTimeout, int writeTimeout,
            int callTimeout) {
        this.outStream = System.out;
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        this.accessToken = accessToken;
        this.verboseEnabled = Boolean.parseBoolean(System.getenv(SYS_PROP_CENTRAL_VERBOSE_ENABLED));
        this.proxyUsername = proxyUsername;
        this.proxyPassword = proxyPassword;
        this.connectTimeout = connectionTimeout;
        this.readTimeout = readTimeout;
        this.writeTimeout = writeTimeout;
        this.callTimeout = callTimeout;
    }

    /**
     * Get package with version.
     *
     * @param orgNamePath       The organization name of the package. (required)
     * @param packageNamePath   The name of the package. (required)
     * @param version           The version or version range of the module.
     *                          (required)
     * @param supportedPlatform The ballerina platform. (required)
     * @param ballerinaVersion  The ballerina version. (required)
     * @return PackageJsonSchema
     */
    public Package getPackage(String orgNamePath, String packageNamePath, String version, String supportedPlatform,
            String ballerinaVersion) throws CentralClientException {
        String packageSignature = orgNamePath + SEPARATOR + packageNamePath + ":" + version;
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            String resourceUrl = PACKAGE_PATH_PREFIX + orgNamePath + SEPARATOR + packageNamePath;
            String url = this.baseUrl + resourceUrl;
            // append version to url if available
            if (null != version && !version.isEmpty()) {
                url = url + SEPARATOR + version;
            }

            Request getPackageReq = getNewRequest(supportedPlatform, ballerinaVersion).get().url(url).build();
            logRequestInitVerbose(getPackageReq);

            Call getPackageReqCall = client.newCall(getPackageReq);
            Response getPackageResponse = getPackageReqCall.execute();
            logRequestConnectVerbose(getPackageReq, resourceUrl);

            body = Optional.ofNullable(getPackageResponse.body());
            String responseBodyContent = null;
            if (body.isPresent()) {
                responseBodyContent = body.get().string();
            }
            logResponseVerbose(getPackageResponse, responseBodyContent);

            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // Package is found
                    if (getPackageResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(responseBodyContent, Package.class);
                    }

                    // Package is not found
                    if (getPackageResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(responseBodyContent, Error.class);
                        if (error.getMessage().contains("package not found for:")) {
                            throw new NoPackageException(error.getMessage());
                        } else {
                            throw new CentralClientException(ERR_CANNOT_FIND_PACKAGE + packageSignature + ". reason: "
                                    + error.getMessage());
                        }
                    }

                    // Unauthorized access token
                    if (getPackageResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(orgNamePath, body, responseBodyContent);
                    }

                    // If request sent is wrong or error occurred at remote repository
                    if (getPackageResponse.code() == HTTP_BAD_REQUEST ||
                            getPackageResponse.code() == HTTP_INTERNAL_ERROR ||
                            getPackageResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(responseBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_FIND_PACKAGE + packageSignature);
        } catch (SocketTimeoutException e) {
            throw new ConnectionErrorException(ERR_CANNOT_FIND_PACKAGE + packageSignature + ". reason: " +
                    e.getMessage());
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_FIND_PACKAGE + packageSignature + ". reason: " +
                    e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get the package versions.
     *
     * @param orgNamePath       The organization name of the package. (required)
     * @param packageNamePath   The name of the package. (required)
     * @param supportedPlatform The ballerina platform. (required)
     * @param ballerinaVersion  The ballerina version. (required)
     * @return PackageJsonSchema
     */
    public List<String> getPackageVersions(String orgNamePath, String packageNamePath, String supportedPlatform,
            String ballerinaVersion) throws CentralClientException {
        String packageSignature = orgNamePath + SEPARATOR + packageNamePath;
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            String resourceUrl = PACKAGE_PATH_PREFIX + orgNamePath + SEPARATOR + packageNamePath;
            String url = this.baseUrl + resourceUrl;
            Request getVersionsReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(url)
                    .build();
            logRequestInitVerbose(getVersionsReq);
            Call getVersionsReqCall = client.newCall(getVersionsReq);
            Response getVersionsResponse = getVersionsReqCall.execute();
            logRequestConnectVerbose(getVersionsReq, resourceUrl);

            body = Optional.ofNullable(getVersionsResponse.body());
            String responseBodyContent = null;
            if (body.isPresent()) {
                responseBodyContent = body.get().string();
            }
            logResponseVerbose(getVersionsResponse, responseBodyContent);

            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // Package versions found
                    if (getVersionsResponse.code() == HTTP_OK) {
                        return getAsList(responseBodyContent);
                    }

                    // Unauthorized access token
                    if (getVersionsResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(orgNamePath, body, responseBodyContent);
                    }

                    // Package is not found
                    if (getVersionsResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(responseBodyContent, Error.class);
                        if (error.getMessage().contains("package not found")) {
                            // if package not found return empty list
                            return new ArrayList<>();
                        } else {
                            throw new CentralClientException(ERR_CANNOT_FIND_VERSIONS + packageSignature +
                                    ". reason: " + error.getMessage());
                        }
                    }

                    // If request sent is wrong or error occurred at remote repository
                    if (getVersionsResponse.code() == HTTP_BAD_REQUEST ||
                            getVersionsResponse.code() == HTTP_INTERNAL_ERROR ||
                            getVersionsResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(responseBodyContent, Error.class);
                        throw new CentralClientException(ERR_CANNOT_FIND_VERSIONS + packageSignature +
                                ". reason: " + error.getMessage());
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_FIND_VERSIONS + packageSignature + ".");
        } catch (SocketTimeoutException | UnknownHostException e) {
            throw new ConnectionErrorException(ERR_CANNOT_FIND_VERSIONS + packageSignature + ". reason: " +
                    e.getMessage());
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_FIND_VERSIONS + packageSignature + ". reason: " +
                    e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Push a package to registry.
     *
     * @param balaPath          The path to the bala file.
     * @param org               The organization of the package.
     * @param name              The name of the package.
     * @param version           The version of the package.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  The ballerina version.
     */
    public void pushPackage(Path balaPath, String org, String name, String version, String supportedPlatform,
            String ballerinaVersion) throws CentralClientException {
        boolean enableOutputStream = Boolean
                .parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
        String packageSignature = org + SEPARATOR + name + ":" + version;
        String url = this.baseUrl + SEPARATOR + PACKAGES;
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            String fileName = org + "-" + name + "-" + version + ".bala";
            Path fileNamePath = balaPath.getFileName();
            if (fileNamePath != null) {
                fileName = fileNamePath.toString();
            }

            RequestBody balaFileReqBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("bala-file", fileName,
                            RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), balaPath.toFile()))
                    .build();
            String remoteRepo = getRemoteRepo();
            String projectRepo = balaPath.toString().split(name)[0] + name;
            ProgressRequestBody balaFileReqBodyWithProgressBar = new ProgressRequestBody(balaFileReqBody,
                    packageSignature + " [" + projectRepo + " -> " + remoteRepo + "]", this.outStream);

            String digestVal = SHA256 + checkHash(balaPath.toString(), SHA256_ALGORITHM);
            // If OutStream is disabled, then pass `balaFileReqBody` only
            Request pushRequest = getNewRequest(supportedPlatform, ballerinaVersion)
                    .addHeader(DIGEST, digestVal)
                    .post(enableOutputStream ? balaFileReqBodyWithProgressBar : balaFileReqBody)
                    .url(url)
                    .build();
            logRequestInitVerbose(pushRequest);

            Call pushRequestCall = client.newCall(pushRequest);
            Response packagePushResponse = pushRequestCall.execute();
            logRequestConnectVerbose(pushRequest, SEPARATOR + PACKAGES);

            body = Optional.ofNullable(packagePushResponse.body());
            String responseBodyContent = null;
            if (body.isPresent()) {
                responseBodyContent = body.get().string();
            }
            logResponseVerbose(packagePushResponse, responseBodyContent);

            // Successfully pushed
            if (packagePushResponse.code() == HTTP_NO_CONTENT) {
                if (enableOutputStream) {
                    this.outStream.println(packageSignature + " pushed to central successfully");
                }
                return;
            }

            // Invalid access token to push
            if (packagePushResponse.code() == HTTP_UNAUTHORIZED) {
                handleUnauthorizedResponse(org, body, responseBodyContent);
            }

            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // When request sent is invalid
                    if (packagePushResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(responseBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            // Currently this error is returned from central when token is unauthorized.
                            // This will later
                            // be removed with
                            // https://github.com/wso2-enterprise/ballerina-registry/issues/745
                            if (error.getMessage().contains("subject claims missing in the user info repsonse")) {
                                error.setMessage("unauthorized access token for organization: '" + org + "'. check " +
                                        "access token set in 'Settings.toml' file.");
                            }
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // When error occurred at remote repository
                    if (packagePushResponse.code() == HTTP_INTERNAL_ERROR ||
                            packagePushResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(responseBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(ERR_CANNOT_PUSH + "'" + packageSignature +
                                    "' reason:" + error.getMessage());
                        }
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_PUSH + "'" + packageSignature + "' to the remote repository '" +
                    url + "'.");
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_PUSH + "'" + packageSignature + "' to the remote repository '" +
                    url + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Pull a package from central.
     *
     * @param org                    The organization of the package.
     * @param name                   The name of the package.
     * @param version                The version of the package.
     * @param packagePathInBalaCache The package path in Bala cache.
     * @param supportedPlatform      The supported platform.
     * @param ballerinaVersion       The ballerina version.
     * @param isBuild                If build option is enabled or not.
     * @throws CentralClientException Central Client exception.
     */
    public void pullPackage(String org, String name, String version, Path packagePathInBalaCache,
            String supportedPlatform, String ballerinaVersion, boolean isBuild)
            throws CentralClientException {
        String resourceUrl = PACKAGE_PATH_PREFIX + org + SEPARATOR + name;
        boolean enableOutputStream = Boolean
                .parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
        String packageSignature = org + SEPARATOR + name;
        String url = this.baseUrl + resourceUrl;
        // append version to url if available
        if (null != version && !version.isEmpty()) {
            url += SEPARATOR + version;
            packageSignature += ":" + version;
        } else {
            url += "/*";
            packageSignature += ":*";
        }

        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            LogFormatter logFormatter = new LogFormatter();
            if (isBuild) {
                logFormatter = new BuildLogFormatter();
            }

            Request packagePullReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(url)
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                    .build();
            logRequestInitVerbose(packagePullReq);
            Call packagePullReqCall = client.newCall(packagePullReq);
            Response packagePullResponse = packagePullReqCall.execute();
            logRequestConnectVerbose(packagePullReq, resourceUrl);

            body = Optional.ofNullable(packagePullResponse.body());
            String pkgPullResBodyContent = null;
            if (body.isPresent()) {
                pkgPullResBodyContent = body.get().string();
            }
            logResponseVerbose(packagePullResponse, pkgPullResBodyContent);

            // 302 - Package is found
            if (packagePullResponse.code() == HTTP_MOVED_TEMP) {
                // get redirect url from "location" header field
                Optional<String> balaUrl = Optional.ofNullable(packagePullResponse.header(LOCATION));
                Optional<String> balaFileName = Optional.ofNullable(packagePullResponse.header(CONTENT_DISPOSITION));
                Optional<String> deprecationFlag = Optional.ofNullable(packagePullResponse.header(IS_DEPRECATED));
                Optional<String> deprecationMsg = Optional.ofNullable(packagePullResponse.header(DEPRECATE_MESSAGE));
                Optional<String> digest = Optional.ofNullable(packagePullResponse.header(DIGEST));

                String digestVal = digest.orElse("");
                boolean isDeprecated = deprecationFlag.isPresent() && Boolean.parseBoolean(deprecationFlag.get());
                String deprecationMessage = deprecationMsg.orElse("");

                if (!isBuild && isDeprecated) {
                    outStream.println("WARNING [" + name + "] " + packageSignature + " is deprecated: "
                            + deprecationMessage);
                }

                if (balaUrl.isPresent() && balaFileName.isPresent()) {
                    Request downloadBalaRequest = getNewRequest(supportedPlatform, ballerinaVersion)
                            .get()
                            .url(balaUrl.get())
                            .header(ACCEPT_ENCODING, IDENTITY)
                            .addHeader(CONTENT_DISPOSITION, balaFileName.get())
                            .build();
                    logRequestInitVerbose(downloadBalaRequest);
                    Call downloadBalaRequestCall = client.newCall(downloadBalaRequest);
                    Response balaDownloadResponse = downloadBalaRequestCall.execute();
                    logRequestConnectVerbose(downloadBalaRequest, balaUrl.get());
                    logResponseVerbose(balaDownloadResponse, null);

                    if (balaDownloadResponse.code() == HTTP_OK) {
                        boolean isNightlyBuild = ballerinaVersion.contains("SNAPSHOT");
                        createBalaInHomeRepo(balaDownloadResponse, packagePathInBalaCache, org, name, isNightlyBuild,
                                isDeprecated ? deprecationMessage : null,
                                balaUrl.get(), balaFileName.get(), enableOutputStream ? outStream : null, logFormatter,
                                digestVal);
                        return;
                    } else {
                        String errorMessage = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                                "'. BALA content download from '" + balaUrl.get() + "' failed.");
                        handleResponseErrors(balaDownloadResponse, errorMessage);
                    }
                } else {
                    String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                            "' from the remote repository '" + url + "'. reason: bala file location is missing.");
                    throw new CentralClientException(errorMsg);
                }
            }

            // Unauthorized access token
            if (packagePullResponse.code() == HTTP_UNAUTHORIZED) {
                handleUnauthorizedResponse(org, body, pkgPullResBodyContent);
            }

            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If request sent is invalid or when package is not found
                    if (packagePullResponse.code() == HTTP_BAD_REQUEST ||
                            packagePullResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(pkgPullResBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException("error: " + error.getMessage());
                        }
                    }

                    // When error occurred at remote repository
                    if (packagePullResponse.code() == HTTP_INTERNAL_ERROR ||
                            packagePullResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(pkgPullResBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                                    "' from" +
                                    " the remote repository '" + url +
                                    "'. reason: " + error.getMessage());
                            throw new CentralClientException(errorMsg);
                        }
                    }
                }
            }

            String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                    "' from the remote repository '" + url + "'.");
            throw new CentralClientException(errorMsg);
        } catch (IOException e) {
            throw new CentralClientException(e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Pull a tool from central.
     *
     * @param toolId            The id of the tool.
     * @param version           The version of the package.
     * @param balaCacheDirPath  The package path in Bala cache.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  The ballerina version.
     * @param isBuild           If build option is enabled or not.
     * @return An array containing isPulled, organization, package name and version.
     * @throws CentralClientException Central Client exception.
     */
    public String[] pullTool(String toolId, String version, Path balaCacheDirPath, String supportedPlatform,
            String ballerinaVersion, boolean isBuild) throws CentralClientException {
        String resourceUrl = TOOL_PATH_PREFIX + toolId;
        boolean enableOutputStream = Boolean
                .parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
        String toolSignature = toolId;

        String url = this.baseUrl + resourceUrl;
        // append version to url if available
        if (null != version && !version.isEmpty()) {
            url += SEPARATOR + version;
            toolSignature += ":" + version;
        }

        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            LogFormatter logFormatter = new LogFormatter();
            if (isBuild) {
                logFormatter = new BuildLogFormatter();
            }

            Request packagePullReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(url)
                    .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                    .build();
            logRequestInitVerbose(packagePullReq);
            Call toolPullReqCall = client.newCall(packagePullReq);
            Response packagePullResponse = toolPullReqCall.execute();
            logRequestConnectVerbose(packagePullReq, resourceUrl);

            body = Optional.ofNullable(packagePullResponse.body());
            String pkgPullResBodyContent = null;
            if (body.isPresent()) {
                pkgPullResBodyContent = body.get().string();
            }
            logResponseVerbose(packagePullResponse, pkgPullResBodyContent);

            // 200 - Package is found
            if (packagePullResponse.code() == HTTP_OK) {
                Optional<String> org = Optional.empty();
                Optional<String> pkgName = Optional.empty();
                Optional<String> latestVersion = Optional.empty();
                Optional<String> balaUrl = Optional.empty();
                Optional<String> platform = Optional.empty();
                if (body.isPresent()) {
                    Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                    if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                        JsonObject jsonContent = new Gson().fromJson(pkgPullResBodyContent, JsonObject.class);
                        org = Optional.ofNullable(jsonContent.get(ORGANIZATION).getAsString());
                        pkgName = Optional.ofNullable(jsonContent.get(PKG_NAME).getAsString());
                        latestVersion = Optional.ofNullable(jsonContent.get(VERSION).getAsString());
                        balaUrl = Optional.ofNullable(jsonContent.get(BALA_URL).getAsString());
                        platform = Optional.of(jsonContent.get(PLATFORM).getAsString())
                                .or(() -> Optional.of(ANY_PLATFORM));
                    }
                }

                if (balaUrl.isPresent() && org.isPresent() && latestVersion.isPresent() && pkgName.isPresent()) {
                    String balaFileName = "attachment; filename=" + org.get() + "-" + pkgName.get()
                            + "-" + platform.get() + "-" + latestVersion.get() + ".bala";
                    Request downloadBalaRequest = getNewRequest(supportedPlatform, ballerinaVersion)
                            .get()
                            .url(balaUrl.get())
                            .header(ACCEPT_ENCODING, IDENTITY)
                            .addHeader(CONTENT_DISPOSITION, balaFileName)
                            .build();
                    logRequestInitVerbose(downloadBalaRequest);
                    Call downloadBalaRequestCall = client.newCall(downloadBalaRequest);
                    Response balaDownloadResponse = downloadBalaRequestCall.execute();
                    logRequestConnectVerbose(downloadBalaRequest, balaUrl.get());
                    logResponseVerbose(balaDownloadResponse, null);

                    Path packagePathInBalaCache = balaCacheDirPath.resolve(org.get()).resolve(pkgName.get());

                    if (balaDownloadResponse.code() == HTTP_OK) {
                        boolean isNightlyBuild = ballerinaVersion.contains("SNAPSHOT");
                        try {
                            createBalaInHomeRepo(balaDownloadResponse, packagePathInBalaCache, org.get(), pkgName.get(),
                                    isNightlyBuild, null, balaUrl.get(), balaFileName,
                                    enableOutputStream ? outStream : null, logFormatter, "");
                            return new String[] { String.valueOf(true), org.get(), pkgName.get(), latestVersion.get() };
                        } catch (PackageAlreadyExistsException ignore) {
                            // package already exists. setting org, name and version fields is enough
                            return new String[] { String.valueOf(false), org.get(), pkgName.get(),
                                    latestVersion.get() };
                        }
                    } else {
                        String errorMessage = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + toolSignature +
                                "'. BALA content download from '" + balaUrl.get() + "' failed.");
                        handleResponseErrors(balaDownloadResponse, errorMessage);
                    }
                } else {
                    String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + toolSignature +
                            "' from the remote repository '" + url + "'. reason: bala file location is missing.");
                    throw new CentralClientException(errorMsg);
                }
            }

            // Unauthorized access token
            if (packagePullResponse.code() == HTTP_UNAUTHORIZED) {
                handleUnauthorizedResponse(body, pkgPullResBodyContent);
            }

            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If request sent is invalid or when tool is not found
                    if (packagePullResponse.code() == HTTP_BAD_REQUEST ||
                            packagePullResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(pkgPullResBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException("error: " + error.getMessage());
                        }
                    }

                    // When error occurred at remote repository
                    if (packagePullResponse.code() == HTTP_INTERNAL_ERROR ||
                            packagePullResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(pkgPullResBodyContent, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + toolSignature +
                                    "' from" +
                                    " the remote repository '" + url +
                                    "'. reason: " + error.getMessage());
                            throw new CentralClientException(errorMsg);
                        }
                    }
                }
            }

            String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + toolSignature +
                    "' from the remote repository '" + url + "'.");
            throw new CentralClientException(errorMsg);
        } catch (IOException e) {
            throw new CentralClientException(e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Resolve Package Names of modules.
     *
     * @return Package name resolution response
     * @throws CentralClientException Central Client exception.
     */
    public PackageNameResolutionResponse resolvePackageNames(PackageNameResolutionRequest request,
            String supportedPlatform, String ballerinaVersion) throws CentralClientException {

        String url = this.baseUrl + PACKAGE_PATH_PREFIX + RESOLVE_MODULES;

        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            RequestBody requestBody = RequestBody.create(JSON, new Gson().toJson(request));
            Request resolutionReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .post(requestBody)
                    .url(url)
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_JSON)
                    .build();
            logRequestInitVerbose(resolutionReq);

            Call resolutionReqCall = client.newCall(resolutionReq);
            Response packageResolutionResponse = resolutionReqCall.execute();
            logRequestConnectVerbose(resolutionReq, PACKAGE_PATH_PREFIX + RESOLVE_MODULES);

            body = Optional.ofNullable(packageResolutionResponse.body());
            String resolvePackageNamesBody = null;
            if (body.isPresent()) {
                resolvePackageNamesBody = body.get().string();
            }
            logResponseVerbose(packageResolutionResponse, resolvePackageNamesBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (packageResolutionResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(resolvePackageNamesBody, PackageNameResolutionResponse.class);
                    }

                    // Unauthorized access token
                    if (packageResolutionResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(contentType.get(), resolvePackageNamesBody);
                    }

                    // If search request was sent wrongly
                    if (packageResolutionResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(resolvePackageNamesBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (packageResolutionResponse.code() == HTTP_INTERNAL_ERROR ||
                            packageResolutionResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(resolvePackageNamesBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION + " reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }
            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION);
        } catch (IOException e) {
            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION + ". reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Resolve Dependencies from central.
     *
     * @throws CentralClientException Central Client exception.
     */
    public PackageResolutionResponse resolveDependencies(PackageResolutionRequest request, String supportedPlatform,
            String ballerinaVersion)
            throws CentralClientException {

        String url = this.baseUrl + PACKAGE_PATH_PREFIX + RESOLVE_DEPENDENCIES;

        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            RequestBody requestBody = RequestBody.create(JSON, new Gson().toJson(request));
            Request packageResolutionReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .post(requestBody)
                    .url(url)
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_JSON)
                    .build();
            logRequestInitVerbose(packageResolutionReq);
            Call packageResolutionReqCall = client.newCall(packageResolutionReq);
            Response packageResolutionResponse = packageResolutionReqCall.execute();
            logRequestConnectVerbose(packageResolutionReq, PACKAGE_PATH_PREFIX + RESOLVE_DEPENDENCIES);

            body = Optional.ofNullable(packageResolutionResponse.body());
            String packageResolutionResponseBody = null;
            if (body.isPresent()) {
                packageResolutionResponseBody = body.get().string();
            }
            logResponseVerbose(packageResolutionResponse, packageResolutionResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (packageResolutionResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(packageResolutionResponseBody, PackageResolutionResponse.class);
                    }

                    // Unauthorized access token
                    if (packageResolutionResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(contentType.get(), packageResolutionResponseBody);
                    }

                    // If search request was sent wrongly
                    if (packageResolutionResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(packageResolutionResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (packageResolutionResponse.code() == HTTP_INTERNAL_ERROR ||
                            packageResolutionResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(packageResolutionResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION + " reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }
            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION);
        } catch (IOException e) {
            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION + ". reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Resolve Tools from central.
     *
     * @param request            The tool resolution request.
     * @param supportedPlatform  The supported jBallerina backend platform.
     * @param ballerinaVersion   The ballerina distribution version.
     *
     * @throws CentralClientException Central Client exception.
     */
    public ToolResolutionCentralResponse resolveToolDependencies(
            ToolResolutionCentralRequest request, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        String url = this.baseUrl + TOOL_PATH_PREFIX + RESOLVE_DEPENDENCIES;
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            RequestBody requestBody = RequestBody.create(JSON, new Gson().toJson(request));
            Request toolResolutionReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .post(requestBody)
                    .url(url)
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_JSON)
                    .build();
            logRequestInitVerbose(toolResolutionReq);
            Call toolResolutionReqCall = client.newCall(toolResolutionReq);
            Response toolResolutionResponse = toolResolutionReqCall.execute();
            logRequestConnectVerbose(toolResolutionReq, TOOL_PATH_PREFIX + RESOLVE_DEPENDENCIES);
            body = Optional.ofNullable(toolResolutionResponse.body());
            String toolResolutionResponseBody = null;
            if (body.isPresent()) {
                toolResolutionResponseBody = body.get().string();
            }
            logResponseVerbose(toolResolutionResponse, toolResolutionResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If resolution was successful
                    if (toolResolutionResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(toolResolutionResponseBody, ToolResolutionCentralResponse.class);
                    }
                    // Unauthorized access token
                    if (toolResolutionResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(contentType.get(), toolResolutionResponseBody);
                    }
                    // If search request was sent wrongly
                    if (toolResolutionResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(toolResolutionResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(error.getMessage());
                        }
                    }
                    // If error occurred at remote repository
                    if (toolResolutionResponse.code() == HTTP_INTERNAL_ERROR ||
                            toolResolutionResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(toolResolutionResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION + " reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }
            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION);
        } catch (IOException e) {
            throw new ConnectionErrorException(ERR_PACKAGE_RESOLUTION + ". reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Search packages in registry.
     */
    public PackageSearchResult searchPackage(String query, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + SEPARATOR + PACKAGES + "/?q=" + query)
                    .build();
            logRequestInitVerbose(searchReq);
            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, SEPARATOR + PACKAGES + "/?q=" + query);

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (searchResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(searchResponseBody, PackageSearchResult.class);
                    }

                    // Unauthorized access token
                    if (searchResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(contentType.get(), searchResponseBody);
                    }

                    // If search request was sent wrongly
                    if (searchResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(searchResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (searchResponse.code() == HTTP_INTERNAL_ERROR ||
                            searchResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(searchResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(ERR_CANNOT_SEARCH + "'" + query + "' reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_SEARCH + "'" + query + "'.");
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_SEARCH + "'" + query + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Search tools in central.
     */
    public ToolSearchResult searchTool(String keyword, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + SEPARATOR + TOOLS + SEARCH_QUERY + keyword)
                    .build();

            logRequestInitVerbose(searchReq);
            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, TOOL_PATH_PREFIX + SEARCH_QUERY + SEPARATOR + keyword);

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (searchResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(searchResponseBody, ToolSearchResult.class);
                    }

                    // Unauthorized access token
                    if (searchResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(contentType.get(), searchResponseBody);
                    }

                    // If search request was sent wrongly
                    if (searchResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(searchResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (searchResponse.code() == HTTP_INTERNAL_ERROR ||
                            searchResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(searchResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(ERR_CANNOT_SEARCH + "'" + keyword + "' reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_SEARCH + "'" + keyword + "'.");
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_SEARCH + "'" + keyword + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Deprecate a package in registry.
     */
    public void deprecatePackage(String packageInfo, String deprecationMsg, String supportedPlatform,
            String ballerinaVersion, Boolean isUndo) throws CentralClientException {
        // Get existing package details
        // PackageInfo is already validated to support the format
        // org-name/package-name:version
        Package existingPackage = getPackage(
                packageInfo.split(SEPARATOR)[0], packageInfo.split(SEPARATOR)[1].split(":")[0],
                packageInfo.split(SEPARATOR)[1].split(":")[1], supportedPlatform, ballerinaVersion);

        String packageValue = packageInfo.endsWith(":*") ? packageInfo.substring(0, packageInfo.length() - 2)
                : packageInfo;
        if (isUndo && !existingPackage.getDeprecated()) {
            this.outStream.println("package " + packageValue + " is not marked as deprecated in central");
            return;
        }

        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            RequestBody requestBody;
            String requestURL;
            if (isUndo) {
                requestURL = this.baseUrl + PACKAGE_PATH_PREFIX + UN_DEPRECATE + SEPARATOR +
                        packageInfo.replace(":", SEPARATOR);
                requestBody = RequestBody.create(JSON, "{}");
            } else {
                requestBody = RequestBody.create(JSON, "{\"message\": \"" + deprecationMsg + "\"}");
                requestURL = this.baseUrl + PACKAGE_PATH_PREFIX + DEPRECATE + SEPARATOR +
                        packageInfo.replace(":", SEPARATOR);
            }

            Request deprecationReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .put(requestBody)
                    .url(requestURL)
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_JSON)
                    .build();
            logRequestInitVerbose(deprecationReq);

            Call httpRequestCall = client.newCall(deprecationReq);
            Response deprecationResponse = httpRequestCall.execute();
            logRequestConnectVerbose(deprecationReq, requestURL.replace(this.baseUrl, ""));

            body = Optional.ofNullable(deprecationResponse.body());
            String deprecationResponseBody = null;
            if (body.isPresent()) {
                deprecationResponseBody = body.get().string();
            }
            logResponseVerbose(deprecationResponse, deprecationResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If deprecation was successful
                    if (deprecationResponse.code() == HTTP_OK) {
                        Package packageResponse = new Gson().fromJson(deprecationResponseBody, Package.class);
                        if (packageResponse.getDeprecated()) {
                            if (existingPackage.getDeprecated()) {
                                this.outStream.println("deprecation message is successfully updated for the package "
                                        + packageValue + " in central");
                            } else {
                                this.outStream.println("package " + packageValue
                                        + " marked as deprecated in central successfully");
                            }
                        } else {
                            this.outStream.println("deprecation of the package " + packageValue +
                                    " is successfully undone in central");
                        }
                    }

                    // Unauthorized access token
                    if (deprecationResponse.code() == HTTP_UNAUTHORIZED) {
                        handleUnauthorizedResponse(contentType.get(), deprecationResponseBody);
                    }

                    // If deprecation request was sent wrongly
                    if (deprecationResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(deprecationResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // If deprecation request was sent wrongly
                    if (deprecationResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(deprecationResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (deprecationResponse.code() == HTTP_INTERNAL_ERROR ||
                            deprecationResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(deprecationResponseBody, Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            String errorMsg = isUndo ? ERR_PACKAGE_UN_DEPRECATE : ERR_PACKAGE_DEPRECATE;
                            throw new CentralClientException(errorMsg + "'" + packageValue +
                                    "' reason:" + error.getMessage());
                        }
                    }
                }
            }
        } catch (IOException e) {
            String errorMsg = isUndo ? ERR_PACKAGE_UN_DEPRECATE : ERR_PACKAGE_DEPRECATE;
            throw new CentralClientException(errorMsg + "'" + packageValue + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get packages from central.
     *
     * @param params            Search query param map.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  The ballerina version.
     * @return Package list
     * @throws CentralClientException Central client exception.
     */
    public JsonElement getPackages(Map<String, String> params, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .callTimeout(callTimeout, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .build();

        try {
            HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(this.baseUrl))
                    .newBuilder().addPathSegment(PACKAGES);
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }

            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(httpBuilder.build())
                    .build();

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();

            ResponseBody responseBody = searchResponse.body();
            body = responseBody != null ? Optional.of(responseBody) : Optional.empty();
            if (body.isPresent()) {
                MediaType contentType = body.get().contentType();
                if (contentType != null && isApplicationJsonContentType(contentType.toString()) &&
                        searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                    return new Gson().toJsonTree(body.get().string());
                }
            }
            handleResponseErrors(searchResponse, ERR_CANNOT_SEARCH);
            return new JsonArray();
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_SEARCH + "'. Reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get connectors with search filters.
     *
     * @param params            Search query param map.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  The ballerina version.
     * @return Connector list.
     * @throws CentralClientException Central Client exception.
     */
    public JsonElement getConnectors(Map<String, String> params, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        // TODO: update this client initiation with default timeouts after fixing
        // central/connectors API.
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .callTimeout(callTimeout, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .build();

        try {
            HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(this.baseUrl))
                    .newBuilder().addPathSegment(CONNECTORS);
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }

            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(httpBuilder.build())
                    .build();
            logRequestInitVerbose(searchReq);

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, SEPARATOR + CONNECTORS);

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString()) &&
                        searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                    return new Gson().toJsonTree(searchResponseBody);
                }
            }
            handleResponseErrors(searchResponse, ERR_CANNOT_GET_CONNECTOR);
            return new JsonArray();
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get connector by id.
     *
     * @param id                Connector id.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  String supportedPlatform.
     * @return Connector.
     * @throws CentralClientException Central Client exception.
     */
    public JsonObject getConnector(String id, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + CONNECTOR_PATH_PREFIX + id)
                    .build();
            logRequestInitVerbose(searchReq);

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, CONNECTOR_PATH_PREFIX + id);

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString()) &&
                        searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                    return new Gson().fromJson(searchResponseBody, JsonObject.class);
                }
            }
            handleResponseErrors(searchResponse, ERR_CANNOT_GET_CONNECTOR + " id:" + id);
            return new JsonObject();
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + id + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get connector by connector FQN.
     *
     * @param connector         Connector information.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  String supportedPlatform.
     * @return Connector.
     * @throws CentralClientException Central Client exception.
     */
    public JsonObject getConnector(ConnectorInfo connector, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + CONNECTOR_PATH_PREFIX + connector.getOrgName() +
                            SEPARATOR + connector.getPackageName() + SEPARATOR + connector.getVersion() +
                            SEPARATOR + connector.getModuleName() + SEPARATOR + connector.getName())
                    .build();
            logRequestInitVerbose(searchReq);

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, CONNECTOR_PATH_PREFIX + connector.getOrgName() +
                    SEPARATOR + connector.getPackageName() + SEPARATOR + connector.getVersion() +
                    SEPARATOR + connector.getModuleName() + SEPARATOR + connector.getName());

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString()) &&
                        searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                    return new Gson().fromJson(searchResponseBody, JsonObject.class);
                }
            }
            handleResponseErrors(searchResponse, ERR_CANNOT_GET_CONNECTOR + " " + connector.getPackageName());
            return null;
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + connector.getPackageName() +
                    "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Gets an new http client.
     *
     * @return the client
     */
    protected OkHttpClient getClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .callTimeout(callTimeout, TimeUnit.SECONDS)
                .followRedirects(false)
                .retryOnConnectionFailure(true)
                .proxy(this.proxy)
                .build();

        if ((!(this.proxyUsername).isEmpty() && !(this.proxyPassword).isEmpty())) {
            Authenticator proxyAuthenticator = (route, response) -> {
                if (response.request().header("Proxy-Authorization") != null) {
                    return null; // Give up, we've already attempted to authenticate.
                }
                String credential = Credentials.basic(this.proxyUsername, this.proxyPassword);
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            };
            okHttpClient = okHttpClient.newBuilder()
                    .proxyAuthenticator(proxyAuthenticator)
                    .build();
        }
        return okHttpClient;
    }

    /**
     * Handle response errors.
     *
     * @param response The response.
     * @param msg      Custom error message.
     * @throws CentralClientException Central Client exception.
     * @throws IOException            throws when handling http response.
     */
    protected void handleResponseErrors(Response response, String msg) throws CentralClientException, IOException {
        Optional<ResponseBody> body = Optional.ofNullable(response.body());
        if (body.isPresent()) {
            // If search request was sent wrongly
            String responseBody = body.get().string();
            MediaType contentType = body.get().contentType();
            if (response.code() == HTTP_BAD_REQUEST || response.code() == HTTP_NOT_FOUND) {
                Error error = new Gson().fromJson(responseBody, Error.class);
                if (error.getMessage() != null && !"".equals(error.getMessage())) {
                    throw new CentralClientException(error.getMessage());
                }
            }

            // Unauthorized access token
            if (response.code() == HTTP_UNAUTHORIZED) {
                handleUnauthorizedResponse(contentType, responseBody);
            }

            // If error occurred at remote repository or invalid/no response received at the
            // gateway server
            if (response.code() == HTTP_INTERNAL_ERROR || response.code() == HTTP_UNAVAILABLE ||
                    response.code() == HTTP_BAD_GATEWAY || response.code() == HTTP_GATEWAY_TIMEOUT) {
                Error error = new Gson().fromJson(responseBody, Error.class);
                if (error.getMessage() != null && !"".equals(error.getMessage())) {
                    throw new CentralClientException(msg + " reason:" + error.getMessage());
                }
            }
        }

        throw new CentralClientException(msg);
    }

    /**
     * Closes the http client.
     *
     * @param client the client
     * @throws IOException when cache of the client cannot be closed
     */
    protected void closeClient(OkHttpClient client) throws IOException {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        Optional<Cache> clientCache = Optional.ofNullable(client.cache());
        if (clientCache.isPresent()) {
            clientCache.get().close();
        }
    }

    /**
     * Creates a new http request builder.
     *
     * @param supportedPlatform supported platform
     * @param ballerinaVersion  ballerina version
     * @return Http request builder
     */
    protected Request.Builder getNewRequest(String supportedPlatform, String ballerinaVersion) {
        if (this.accessToken.isEmpty()) {
            return new Request.Builder()
                    .addHeader(BALLERINA_PLATFORM, supportedPlatform)
                    .addHeader(USER_AGENT, ballerinaVersion);
        } else {
            return new Request.Builder()
                    .addHeader(BALLERINA_PLATFORM, supportedPlatform)
                    .addHeader(USER_AGENT, ballerinaVersion)
                    .addHeader(AUTHORIZATION, getBearerToken(this.accessToken));
        }
    }

    public String accessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Get triggers with search filters.
     *
     * @param params            Search query param map.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  The ballerina version.
     * @return Trigger list.
     * @throws CentralClientException Central Client exception.
     */
    public JsonElement getTriggers(Map<String, String> params, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        // TODO: update this client initiation with default timeouts after fixing
        // central/triggers API.
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .callTimeout(callTimeout, TimeUnit.SECONDS)
                .proxy(this.proxy)
                .build();

        try {
            HttpUrl.Builder httpBuilder = Objects.requireNonNull(HttpUrl.parse(this.baseUrl))
                    .newBuilder().addPathSegment(TRIGGERS);
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }

            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(httpBuilder.build())
                    .build();
            logRequestInitVerbose(searchReq);

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, SEPARATOR + TRIGGERS);

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString()) &&
                        searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                    return new Gson().toJsonTree(searchResponseBody);
                }
            }
            handleResponseErrors(searchResponse, ERR_CANNOT_GET_TRIGGERS);
            return new JsonArray();
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_TRIGGERS + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get trigger by id.
     *
     * @param id                Trigger id.
     * @param supportedPlatform The supported platform.
     * @param ballerinaVersion  String supportedPlatform.
     * @return Trigger.
     * @throws CentralClientException Central Client exception.
     */
    public JsonObject getTrigger(String id, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + TRIGGER_PATH_PREFIX + id)
                    .build();
            logRequestInitVerbose(searchReq);

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();
            logRequestConnectVerbose(searchReq, TRIGGER_PATH_PREFIX + id);

            body = Optional.ofNullable(searchResponse.body());
            String searchResponseBody = null;
            if (body.isPresent()) {
                searchResponseBody = body.get().string();
            }
            logResponseVerbose(searchResponse, searchResponseBody);
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString()) &&
                        searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                    return new Gson().fromJson(searchResponseBody, JsonObject.class);
                }
            }
            handleResponseErrors(searchResponse, ERR_CANNOT_GET_TRIGGER + " id:" + id);
            return new JsonObject();
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_TRIGGER + "'" + id + "'. reason: " + e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Handle unauthorized response.
     *
     * @param org          org name
     * @param body         response body
     * @param responseBody error message
     * @throws IOException            when accessing response body
     * @throws CentralClientException with unauthorized error message
     */
    private void handleUnauthorizedResponse(String org, Optional<ResponseBody> body, String responseBody)
            throws IOException, CentralClientException {
        if (body.isPresent()) {
            Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
            if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                Error error = new Gson().fromJson(responseBody, Error.class);
                throw new CentralClientException("unauthorized access token for organization: '" + org +
                        "'. check access token set in 'Settings.toml' file. reason: " + error.getMessage());
            } else {
                throw new CentralClientException("unauthorized access token for organization: '" + org +
                        "'. check access token set in 'Settings.toml' file.");
            }
        }
    }

    /**
     * Handle unauthorized response.
     *
     * @param body         response body
     * @param responseBody error message
     * @throws IOException            when accessing response body
     * @throws CentralClientException with unauthorized error message
     */
    private void handleUnauthorizedResponse(Optional<ResponseBody> body, String responseBody)
            throws IOException, CentralClientException {
        if (body.isPresent()) {
            Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
            if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                Error error = new Gson().fromJson(responseBody, Error.class);
                throw new CentralClientException("unauthorized access token. " +
                        "check access token set in 'Settings.toml' file. reason: " + error.getMessage());
            } else {
                throw new CentralClientException("unauthorized access token. " +
                        "check access token set in 'Settings.toml' file.");
            }
        }
    }

    /**
     * Handle unauthorized response.
     *
     * @param mediaType mediaType
     * @param responseBody response body
     * @throws IOException            when accessing response body
     * @throws CentralClientException with unauthorized error message
     */
    private void handleUnauthorizedResponse(MediaType mediaType, String responseBody)
            throws IOException, CentralClientException {
        Optional<MediaType> contentType = Optional.ofNullable(mediaType);
        StringBuilder message = new StringBuilder("unauthorized access token. " +
                    "check access token set in 'Settings.toml' file.");
        if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
            Error error = new Gson().fromJson(responseBody, Error.class);
            message.append("reason: ").append(error.getMessage());
        }
        throw new CentralClientException(message.toString());
    }

    private void logResponseVerbose(Response response, String bodyContent) {
        if (this.verboseEnabled) {
            Optional<ResponseBody> body = Optional.ofNullable(response.body());
            this.outStream.println("< HTTP " + response.code() + " " + response.message());

            if (body.isPresent()) {
                for (String headerName : response.headers().names()) {
                    this.outStream.println("> " + headerName + ": " + response.header(headerName));
                }
                this.outStream.println("< ");
                if (bodyContent != null && !bodyContent.isEmpty()) {
                    this.outStream.println(bodyContent);
                }
                this.outStream.println("* Connection to host " + this.baseUrl + " left intact \n");
            }
        }
    }

    private void logRequestInitVerbose(Request request) {
        if (this.verboseEnabled) {
            this.outStream.println("* Trying " + request.url());

        }
    }

    private void logRequestConnectVerbose(Request request, String resourceUrl) {
        if (this.verboseEnabled) {
            this.outStream.println("* Connected to " + this.baseUrl);
            this.outStream.println("> " + request.method() + " " + resourceUrl + " HTTP");
            this.outStream.println("> Host: " + this.baseUrl);
            for (String headerName : request.headers().names()) {
                if (headerName.equals(AUTHORIZATION)) {
                    this.outStream.println("> " + headerName + ": Bearer ************************************");
                } else {
                    this.outStream.println("> " + headerName + ": " + request.header(headerName));
                }
            }
            this.outStream.println(">");
        }
    }
}
