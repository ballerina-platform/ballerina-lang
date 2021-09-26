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
import okhttp3.Cache;
import okhttp3.Call;
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
import org.ballerinalang.central.client.model.Error;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageNameResolutionRequest;
import org.ballerinalang.central.client.model.PackageNameResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionRequest;
import org.ballerinalang.central.client.model.PackageResolutionResponse;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.central.client.model.connector.BalConnector;
import org.ballerinalang.central.client.model.connector.BalConnectorSearchResult;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static java.net.HttpURLConnection.HTTP_UNAVAILABLE;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT_ENCODING;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_JSON;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_OCTET_STREAM;
import static org.ballerinalang.central.client.CentralClientConstants.AUTHORIZATION;
import static org.ballerinalang.central.client.CentralClientConstants.BALLERINA_PLATFORM;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.CentralClientConstants.USER_AGENT;
import static org.ballerinalang.central.client.Utils.ProgressRequestBody;
import static org.ballerinalang.central.client.Utils.createBalaInHomeRepo;
import static org.ballerinalang.central.client.Utils.getAsList;
import static org.ballerinalang.central.client.Utils.getBearerToken;
import static org.ballerinalang.central.client.Utils.isApplicationJsonContentType;

/**
 * {@code CentralAPIClient} is a client for the Central API.
 *
 * @since 2.0.0
 */
public class CentralAPIClient {

    private static final String PACKAGES = "packages";
    private static final String RESOLVE_DEPENDENCIES = "resolve-dependencies";
    private static final String RESOLVE_MODULES = "resolve-modules";
    private static final String CONNECTORS = "connectors";
    private static final String ERR_CANNOT_FIND_PACKAGE = "error: could not connect to remote repository to find " +
            "package: ";
    private static final String ERR_CANNOT_FIND_VERSIONS = "error: could not connect to remote repository to find " +
            "versions for: ";
    private static final String ERR_CANNOT_PUSH = "error: failed to push the package: ";
    private static final String ERR_CANNOT_PULL_PACKAGE = "error: failed to pull the package: ";
    private static final String ERR_CANNOT_SEARCH = "error: failed to search packages: ";
    private static final String ERR_PACKAGE_RESOLUTION = "error: while connecting to central: ";
    private static final String ERR_CANNOT_GET_CONNECTOR = "error: failed to find connector: ";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final String baseUrl;
    private final Proxy proxy;
    private String accessToken;
    protected PrintStream outStream;

    public CentralAPIClient(String baseUrl, Proxy proxy, String accessToken) {
        this.outStream = System.out;
        this.baseUrl = baseUrl;
        this.proxy = proxy;
        this.accessToken = accessToken;
    }

    /**
     * Get package with version.
     *
     * @param orgNamePath       The organization name of the package. (required)
     * @param packageNamePath   The name of the package. (required)
     * @param version           The version or version range of the module. (required)
     * @param supportedPlatform The ballerina platform. (required)
     * @param ballerinaVersion  The ballerina version. (required)
     * @return PackageJsonSchema
     */
    public Package getPackage(String orgNamePath, String packageNamePath, String version, String supportedPlatform,
            String ballerinaVersion) throws CentralClientException {
        String packageSignature = orgNamePath + "/" + packageNamePath + ":" + version;
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            String url = this.baseUrl + "/" + PACKAGES + "/" + orgNamePath + "/" + packageNamePath;
            // append version to url if available
            if (null != version && !version.isEmpty()) {
                url = url + "/" + version;
            }

            Request getPackageReq = getNewRequest(supportedPlatform, ballerinaVersion).get().url(url).build();
            Call getPackageReqCall = client.newCall(getPackageReq);
            Response getPackageResponse = getPackageReqCall.execute();

            body = Optional.ofNullable(getPackageResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // Package is found
                    if (getPackageResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(body.get().string(), Package.class);
                    }

                    // Package is not found
                    if (getPackageResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage().contains("package not found for:")) {
                            throw new NoPackageException(error.getMessage());
                        } else {
                            throw new CentralClientException(ERR_CANNOT_FIND_PACKAGE + packageSignature + ". reason: "
                                                                     + error.getMessage());
                        }
                    }

                    // If request sent is wrong or error occurred at remote repository
                    if (getPackageResponse.code() == HTTP_BAD_REQUEST ||
                        getPackageResponse.code() == HTTP_INTERNAL_ERROR ||
                        getPackageResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
        String packageSignature = orgNamePath + "/" + packageNamePath;
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            String url = this.baseUrl + "/" + PACKAGES + "/" + orgNamePath + "/" + packageNamePath;
            Request getVersionsReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(url)
                    .build();
            Call getVersionsReqCall = client.newCall(getVersionsReq);
            Response getVersionsResponse = getVersionsReqCall.execute();

            body = Optional.ofNullable(getVersionsResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // Package versions found
                    if (getVersionsResponse.code() == HTTP_OK) {
                        return getAsList(body.get().string());
                    }
    
                    // Package is not found
                    if (getVersionsResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
     * @param balaPath              The path to the bala file.
     * @param org                   The organization of the package.
     * @param name                  The name of the package.
     * @param version               The version of the package.
     * @param supportedPlatform     The supported platform.
     * @param ballerinaVersion      The ballerina version.
     */
    public void pushPackage(Path balaPath, String org, String name, String version, String supportedPlatform,
                            String ballerinaVersion) throws CentralClientException {
        boolean enableOutputStream =
                Boolean.parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
        String packageSignature = org + "/" + name + ":" + version;
        String url = this.baseUrl + "/" + PACKAGES;
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

            ProgressRequestBody balaFileReqBodyWithProgressBar = new ProgressRequestBody(balaFileReqBody,
                    packageSignature + " [project repo -> central]", this.outStream);

            // If OutStream is disabled, then pass `balaFileReqBody` only
            Request pushRequest = getNewRequest(supportedPlatform, ballerinaVersion)
                    .post(enableOutputStream ? balaFileReqBodyWithProgressBar : balaFileReqBody)
                    .url(url)
                    .build();

            Call pushRequestCall = client.newCall(pushRequest);
            Response packagePushResponse = pushRequestCall.execute();

            // Successfully pushed
            if (packagePushResponse.code() == HTTP_NO_CONTENT) {
                if (enableOutputStream) {
                    this.outStream.println(packageSignature + " pushed to central successfully");
                }
                return;
            }

            body = Optional.ofNullable(packagePushResponse.body());
            // Invalid access token to push
            if (packagePushResponse.code() == HTTP_UNAUTHORIZED) {
                if (body.isPresent()) {
                    Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                    if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        throw new CentralClientException("unauthorized access token for organization: '" + org +
                                                         "'. reason: " + error.getMessage() +
                                                         ". check access token set in 'Settings.toml' file.");
                    } else {
                        throw new CentralClientException("unauthorized access token for organization: '" + org +
                                                         "'. check access token set in 'Settings.toml' file.");
                    }
                }
            }
    
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                    // When request sent is invalid
                    if (packagePushResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            // Currently this error is returned from central when token is unauthorized. This will later
                            // be removed with https://github.com/wso2-enterprise/ballerina-registry/issues/745
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
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
     * @param org                       The organization of the package.
     * @param name                      The name of the package.
     * @param version                   The version of the package.
     * @param packagePathInBalaCache    The package path in Bala cache.
     * @param supportedPlatform         The supported platform.
     * @param ballerinaVersion          The ballerina version.
     * @param isBuild                   If build option is enabled or not.
     * @throws CentralClientException   Central Client exception.
     */
    public void pullPackage(String org, String name, String version, Path packagePathInBalaCache,
                            String supportedPlatform, String ballerinaVersion, boolean isBuild)
            throws CentralClientException {
        boolean enableOutputStream =
                Boolean.parseBoolean(System.getProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM));
        String packageSignature =  org + "/" + name;
        String url = this.baseUrl + "/" + PACKAGES + "/" + org + "/" + name;
        // append version to url if available
        if (null != version && !version.isEmpty()) {
            url += "/" + version;
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

            Call packagePullReqCall = client.newCall(packagePullReq);
            Response packagePullResponse = packagePullReqCall.execute();

            // 302   - Package is found
            if (packagePullResponse.code() == HTTP_MOVED_TEMP) {
                // get redirect url from "location" header field
                Optional<String> balaUrl = Optional.ofNullable(packagePullResponse.header(LOCATION));
                Optional<String> balaFileName = Optional.ofNullable(packagePullResponse.header(CONTENT_DISPOSITION));

                if (balaUrl.isPresent() && balaFileName.isPresent()) {
                    Request downloadBalaRequest = getNewRequest(supportedPlatform, ballerinaVersion)
                            .get()
                            .url(balaUrl.get())
                            .header(ACCEPT_ENCODING, IDENTITY)
                            .addHeader(CONTENT_DISPOSITION, balaFileName.get())
                            .build();

                    Call downloadBalaRequestCall = client.newCall(downloadBalaRequest);
                    Response balaDownloadResponse = downloadBalaRequestCall.execute();
                    boolean isNightlyBuild = ballerinaVersion.contains("SNAPSHOT");
                    createBalaInHomeRepo(balaDownloadResponse, packagePathInBalaCache, org, name, isNightlyBuild,
                            balaUrl.get(), balaFileName.get(), enableOutputStream ? outStream : null, logFormatter);
                    return;
                } else {
                    String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                            "' from the remote repository '" + url + "'. reason: bala file location is missing.");
                    throw new CentralClientException(errorMsg);
                }
            }
    
            body = Optional.ofNullable(packagePullResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If request sent is invalid or when package is not found
                    if (packagePullResponse.code() == HTTP_BAD_REQUEST ||
                        packagePullResponse.code() == HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException("error: " + error.getMessage());
                        }
                    }
    
                    //  When error occurred at remote repository
                    if (packagePullResponse.code() == HTTP_INTERNAL_ERROR ||
                        packagePullResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            String errorMsg =
                                    logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
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
     * Resolve Package Names of modules.
     *
     * @throws CentralClientException   Central Client exception.
     * @return
     */
    public PackageNameResolutionResponse resolvePackageNames(PackageNameResolutionRequest request,
                                                             String supportedPlatform, String ballerinaVersion,
                                                             boolean isBuild)
            throws CentralClientException {

        String url = this.baseUrl + "/" + PACKAGES + "/" + RESOLVE_MODULES;

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

            Call resolutionReqCall = client.newCall(resolutionReq);
            Response packageResolutionResponse = resolutionReqCall.execute();

            body = Optional.ofNullable(packageResolutionResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (packageResolutionResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(body.get().string(), PackageNameResolutionResponse.class);
                    }

                    // If search request was sent wrongly
                    if (packageResolutionResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (packageResolutionResponse.code() == HTTP_INTERNAL_ERROR ||
                            packageResolutionResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
     * @throws CentralClientException   Central Client exception.
     */
    public PackageResolutionResponse resolveDependencies(PackageResolutionRequest request, String supportedPlatform,
                                                         String ballerinaVersion, boolean isBuild)
            throws CentralClientException {

        String url = this.baseUrl + "/" + PACKAGES + "/" + RESOLVE_DEPENDENCIES;

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

            Call packageResolutionReqCall = client.newCall(packageResolutionReq);
            Response packageResolutionResponse = packageResolutionReqCall.execute();

            body = Optional.ofNullable(packageResolutionResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (packageResolutionResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(body.get().string(), PackageResolutionResponse.class);
                    }

                    // If search request was sent wrongly
                    if (packageResolutionResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ConnectionErrorException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (packageResolutionResponse.code() == HTTP_INTERNAL_ERROR ||
                            packageResolutionResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
                    .url(this.baseUrl + "/" + PACKAGES + "/?q=" + query)
                    .build();

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();

            body = Optional.ofNullable(searchResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (searchResponse.code() == HTTP_OK) {
                        return new Gson().fromJson(body.get().string(), PackageSearchResult.class);
                    }
        
                    // If search request was sent wrongly
                    if (searchResponse.code() == HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }
        
                    // If error occurred at remote repository
                    if (searchResponse.code() == HTTP_INTERNAL_ERROR ||
                        searchResponse.code() == HTTP_UNAVAILABLE) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
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
     * Get connectors with search filters
     */
    public BalConnectorSearchResult getConnectors(String query, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + "/" + CONNECTORS + (query.equals("") ? "" : "/?package=" + query))
                    .build();

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();

            body = Optional.ofNullable(searchResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                        BalConnectorSearchResult searchResult = new Gson().fromJson(body.get().string(),
                                BalConnectorSearchResult.class);
                        return searchResult;
                    }

                    // If search request was sent wrongly
                    if (searchResponse.code() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (searchResponse.code() == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + query + "' reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + query + "'.");
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + query + "'. reason: " + e.getMessage());
        }
        finally {
            body.ifPresent(ResponseBody::close);
            try {
                this.closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    /**
     * Get connector by id
     */
    public BalConnector getConnector(String id, String supportedPlatform, String ballerinaVersion)
            throws CentralClientException {
        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = this.getClient();
        try {
            Request searchReq = getNewRequest(supportedPlatform, ballerinaVersion)
                    .get()
                    .url(this.baseUrl + "/" + CONNECTORS + "/" + id)
                    .build();

            Call httpRequestCall = client.newCall(searchReq);
            Response searchResponse = httpRequestCall.execute();

            body = Optional.ofNullable(searchResponse.body());
            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent()  && isApplicationJsonContentType(contentType.get().toString())) {
                    // If searching was successful
                    if (searchResponse.code() == HttpsURLConnection.HTTP_OK) {
                        BalConnector connectorResult = new Gson().fromJson(body.get().string(),
                                BalConnector.class);
                        return connectorResult;
                    }

                    // If search request was sent wrongly
                    if (searchResponse.code() == HttpsURLConnection.HTTP_BAD_REQUEST) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(error.getMessage());
                        }
                    }

                    // If error occurred at remote repository
                    if (searchResponse.code() == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + id + "' reason:" +
                                    error.getMessage());
                        }
                    }
                }
            }

            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + id + "'.");
        } catch (IOException e) {
            throw new CentralClientException(ERR_CANNOT_GET_CONNECTOR + "'" + id + "'. reason: " + e.getMessage());
        }
        finally {
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
        return new OkHttpClient.Builder()
                .followRedirects(false)
                .proxy(this.proxy)
                .build();
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
}
