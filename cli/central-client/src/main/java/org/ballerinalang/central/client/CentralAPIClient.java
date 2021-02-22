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
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.ConnectionErrorException;
import org.ballerinalang.central.client.exceptions.NoPackageException;
import org.ballerinalang.central.client.model.Error;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageSearchResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT_ENCODING;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_OCTET_STREAM;
import static org.ballerinalang.central.client.CentralClientConstants.AUTHORIZATION;
import static org.ballerinalang.central.client.CentralClientConstants.BALLERINA_PLATFORM;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_TYPE;
import static org.ballerinalang.central.client.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.CentralClientConstants.USER_AGENT;
import static org.ballerinalang.central.client.Utils.convertToUrl;
import static org.ballerinalang.central.client.Utils.createBalaInHomeRepo;
import static org.ballerinalang.central.client.Utils.getAsList;
import static org.ballerinalang.central.client.Utils.getStatusCode;
import static org.ballerinalang.central.client.Utils.getTotalFileSizeInKB;
import static org.ballerinalang.central.client.Utils.initializeSsl;
import static org.ballerinalang.central.client.Utils.setRequestMethod;

/**
 * {@code CentralAPIClient} is a client for the Central API.
 *
 * @since 2.0.0
 */
public class CentralAPIClient {

    private Proxy proxy;
    private String baseUrl;
    protected PrintStream outStream;
    private static final String PACKAGES = "packages";
    private static final String ERR_CANNOT_CONNECT = "error: could not connect to remote repository to find package: ";
    private static final String ERR_CANNOT_PUSH = "error: failed to push the package: ";

    public CentralAPIClient(String baseUrl, Proxy proxy) {
        this.outStream = System.out;
        this.baseUrl = baseUrl;
        this.proxy = proxy;
    }

    /**
     * Get package with version.
     *
     * @param orgNamePath     The organization name of the package. (required)
     * @param packageNamePath The name of the package. (required)
     * @param version         The version or version range of the module. (required)
     * @return PackageJsonSchema
     */
    public Package getPackage(String orgNamePath, String packageNamePath, String version, String supportedPlatform)
            throws CentralClientException {
        initializeSsl();
        String url = PACKAGES + "/" + orgNamePath + "/" + packageNamePath;
        // append version to url if available
        if (null != version && !version.isEmpty()) {
            url = url + "/" + version;
        }

        String pkg = orgNamePath + "/" + packageNamePath + ":" + version;
        HttpURLConnection conn = createHttpUrlConnection(url);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

        // set implementation version
        conn.setRequestProperty(BALLERINA_PLATFORM, supportedPlatform);

        // status code and meaning
        //// 302 - module found
        //// 404 - module not found
        //// 400 - bad request sent
        //// 500 - backend is broken
        try {
            int statusCode = getStatusCode(conn);
            if (statusCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    return new Gson().fromJson(reader, Package.class);
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);
                    if (errorJsonSchema.getMessage().contains("package not found for:")) {
                        throw new NoPackageException(errorJsonSchema.getMessage());
                    } else {
                        throw new CentralClientException(
                                ERR_CANNOT_CONNECT + pkg + ". reason: " + errorJsonSchema.getMessage());
                    }
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (BufferedReader errorStream = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(errorStream, Error.class);

                    if (errorJsonSchema.getMessage() != null && !"".equals(errorJsonSchema.getMessage())) {
                        throw new CentralClientException(errorJsonSchema.getMessage());
                    } else {
                        throw new CentralClientException(ERR_CANNOT_CONNECT + pkg + ". reason:" + errorStream.lines()
                                .collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            } else {
                throw new CentralClientException(ERR_CANNOT_CONNECT + pkg + ".");
            }
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    /**
     * Get the package versions.
     *
     * @param orgNamePath     The organization name of the package. (required)
     * @param packageNamePath The name of the package. (required)
     * @return PackageJsonSchema
     */
    public List<String> getPackageVersions(String orgNamePath, String packageNamePath, String supportedPlatform)
            throws CentralClientException {
        initializeSsl();
        String url = PACKAGES + "/" + orgNamePath + "/" + packageNamePath;

        HttpURLConnection conn = createHttpUrlConnection(url);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

        // Set headers
        conn.setRequestProperty(BALLERINA_PLATFORM, supportedPlatform);

        // status code and meaning
        //// 200 - list of versions
        //// 404 - package not found
        //// 500 - backend is broken
        try {
            int statusCode = getStatusCode(conn);
            if (statusCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    String collect = reader.lines().collect(Collectors.joining());
                    return getAsList(collect);
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);
                    if (errorJsonSchema.getMessage().contains("package not found")) {
                        // if package not found return empty list
                        return new ArrayList<>();
                    } else {
                        throw new CentralClientException(
                                "error: could not connect to remote repository to find versions for: " + orgNamePath
                                        + "/" + packageNamePath + ". reason: " + errorJsonSchema.getMessage());
                    }
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            } else {
                throw new CentralClientException(
                        "error: could not connect to remote repository to find versions for: " + orgNamePath + "/"
                                + packageNamePath + ".");
            }
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    /**
     * Pushing a package to registry.
     */
    public void pushPackage(Path balaPath, String org, String name, String version, String accessToken,
            String ballerinaVersion) throws CentralClientException {
        final int noOfBytes = 64;
        final int bufferSize = 1024 * noOfBytes;

        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(PACKAGES);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.POST);

        // Set headers
        conn.setRequestProperty(AUTHORIZATION, "Bearer " + accessToken);
        conn.setRequestProperty(CONTENT_TYPE, APPLICATION_OCTET_STREAM);
        conn.setRequestProperty(USER_AGENT, ballerinaVersion);

        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(bufferSize);

        try (DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream())) {
            // Send bala content by 1 kb chunks
            byte[] buffer = new byte[bufferSize];
            int count;
            try (ProgressBar progressBar = new ProgressBar(
                    org + "/" + name + ":" + version + " [project repo -> central]", getTotalFileSizeInKB(balaPath),
                    1000, outStream, ProgressBarStyle.ASCII, " KB", 1);
                    FileInputStream fis = new FileInputStream(balaPath.toFile())) {
                while ((count = fis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                    outputStream.flush();
                    progressBar.stepBy((long) noOfBytes);
                }
            }
        } catch (IOException e) {
            throw new CentralClientException("error occurred while uploading bala to central: " + e.getMessage());
        }

        try {
            int statusCode = getStatusCode(conn);
            // 200 - Module pushed successfully
            // 401 - Unauthorized access token for org
            // Other - Error occurred, json returned with the error message
            if (statusCode == HttpURLConnection.HTTP_NO_CONTENT) {
                outStream.println(org + "/" + name + ":" + version + " pushed to central successfully");
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);

                    if (errorJsonSchema.getMessage() != null && !"".equals(errorJsonSchema.getMessage())) {
                        throw new CentralClientException(errorJsonSchema.getMessage());
                    } else {
                        throw new CentralClientException(
                                ERR_CANNOT_PUSH + "'" + org + "/" + name + ":" + version + "' reason:" + reader.lines()
                                        .collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw new CentralClientException(
                            ERR_CANNOT_PUSH + "'" + org + "/" + name + ":" + version + "' to the remote repository '"
                                    + conn.getURL() + "'");
                }
            } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new CentralClientException("unauthorized access token for organization: " + org);
            } else {
                throw new CentralClientException(
                        ERR_CANNOT_PUSH + "'" + org + "/" + name + ":" + version + "' to the remote repository '" + conn
                                .getURL() + "'");
            }
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    public void pullPackage(String org, String name, String version, Path packagePathInBalaCache,
            String supportedPlatform, String ballerinaVersion, boolean isBuild) throws CentralClientException {
        LogFormatter logFormatter = new LogFormatter();
        if (isBuild) {
            logFormatter = new BuildLogFormatter();
        }

        String url = PACKAGES + "/" + org + "/" + name;
        // append version to url if available
        if (null != version && !version.isEmpty()) {
            url += "/" + version;
        } else {
            url += "/*";
        }

        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(url);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

        // Set headers
        conn.setRequestProperty(BALLERINA_PLATFORM, supportedPlatform);
        conn.setRequestProperty(ACCEPT_ENCODING, IDENTITY);
        conn.setRequestProperty(USER_AGENT, ballerinaVersion);
        conn.setRequestProperty(ACCEPT, APPLICATION_OCTET_STREAM);

        try {
            // 302   - Package is found
            // Other - Error occurred, json returned with the error message
            if (getStatusCode(conn) == HttpURLConnection.HTTP_MOVED_TEMP) {
                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField(LOCATION);
                String contentDisposition = conn.getHeaderField(CONTENT_DISPOSITION);

                // create connection
                if (this.proxy == null) {
                    conn = (HttpURLConnection) convertToUrl(newUrl).openConnection();
                } else {
                    conn = (HttpURLConnection) convertToUrl(newUrl).openConnection(this.proxy);
                }

                conn.setRequestProperty(CONTENT_DISPOSITION, contentDisposition);

                boolean isNightlyBuild = ballerinaVersion.contains("SNAPSHOT");
                createBalaInHomeRepo(conn, packagePathInBalaCache, org + "/" + name, isNightlyBuild, newUrl,
                                     contentDisposition, outStream, logFormatter);
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);
                    throw new CentralClientException(logFormatter.formatLog("error: " + errorJsonSchema.getMessage()));
                } catch (IOException e) {
                    throw new CentralClientException(logFormatter.formatLog(
                            "failed to pull the package '" + org + "/" + name + "' from the remote repository '" + url
                                    + "'"));
                }
            }
        } catch (IOException e) {
            throw new CentralClientException(e.getMessage());
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    /**
     * Search packages in registry.
     */
    public PackageSearchResult searchPackage(String query, String ballerinaVersion) throws CentralClientException {
        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(PACKAGES + "/?q=" + query);
        conn.setInstanceFollowRedirects(false);

        setRequestMethod(conn, Utils.RequestMethod.GET);
        conn.setRequestProperty(USER_AGENT, ballerinaVersion);

        // Handle response
        int statusCode = getStatusCode(conn);
        try {
            // 200 - modules found
            // Other - Error occurred, json returned with the error message
            if (statusCode == HttpURLConnection.HTTP_OK) {

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    return new Gson().fromJson(reader, PackageSearchResult.class);
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);

                    if (errorJsonSchema.getMessage() != null && !"".equals(errorJsonSchema.getMessage())) {
                        throw new CentralClientException(errorJsonSchema.getMessage());
                    } else {
                        throw new CentralClientException(reader.lines().collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw new CentralClientException(e.getMessage());
                }
            }
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    /**
     * Create http URL connection.
     *
     * @param paths resource paths
     * @return http URL connection
     */
    protected HttpURLConnection createHttpUrlConnection(String paths) throws ConnectionErrorException {
        URL url = convertToUrl(this.baseUrl + "/" + paths);
        try {
            // set proxy if exists.
            if (this.proxy == null) {
                return (HttpURLConnection) url.openConnection();
            } else {
                return (HttpURLConnection) url.openConnection(this.proxy);
            }
        } catch (IOException e) {
            throw new ConnectionErrorException("Creating connection to '" + url + "' failed:" + e.getMessage());
        }
    }
}
