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
import io.ballerina.projects.balo.BaloProject;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;
import org.ballerinalang.central.client.model.Error;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.central.client.util.BuildLogFormatter;
import org.ballerinalang.central.client.util.CommandException;
import org.ballerinalang.central.client.util.ErrorUtil;
import org.ballerinalang.central.client.util.LogFormatter;
import org.ballerinalang.central.client.util.NoPackageException;
import org.ballerinalang.central.client.util.Utils;
import org.ballerinalang.toml.model.Settings;
import org.wso2.ballerinalang.util.RepoUtils;

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
import java.util.stream.Collectors;

import static org.ballerinalang.central.client.util.Utils.authenticate;
import static org.ballerinalang.central.client.util.Utils.convertToUrl;
import static org.ballerinalang.central.client.util.Utils.createBaloInHomeRepo;
import static org.ballerinalang.central.client.util.Utils.getBallerinaCentralCliTokenUrl;
import static org.ballerinalang.central.client.util.Utils.getStatusCode;
import static org.ballerinalang.central.client.util.Utils.getTotalFileSizeInKB;
import static org.ballerinalang.central.client.util.Utils.initializeProxy;
import static org.ballerinalang.central.client.util.Utils.initializeSsl;
import static org.ballerinalang.central.client.util.Utils.readSettings;
import static org.ballerinalang.central.client.util.Utils.setRequestMethod;
import static org.ballerinalang.central.client.util.CentralClientConstants.ACCEPT_ENCODING;
import static org.ballerinalang.central.client.util.CentralClientConstants.APPLICATION_OCTET_STREAM;
import static org.ballerinalang.central.client.util.CentralClientConstants.AUTHORIZATION;
import static org.ballerinalang.central.client.util.CentralClientConstants.BALLERINA_PLATFORM;
import static org.ballerinalang.central.client.util.CentralClientConstants.BAL_LANG_SPEC_VERSION;
import static org.ballerinalang.central.client.util.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.util.CentralClientConstants.CONTENT_TYPE;
import static org.ballerinalang.central.client.util.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.util.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.util.CentralClientConstants.PUSH_ORGANIZATION;
import static org.ballerinalang.central.client.util.CentralClientConstants.USER_AGENT;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.SETTINGS_FILE_NAME;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.util.RepoUtils.getRemoteRepoURL;

/**
 * {@code CentralAPIClient} is a client for the Central API.
 *
 * @since 2.0.0
 */
public class CentralAPIClient {

    private String accessToken;
    private Proxy proxy;
    private String baseUrl;
    protected PrintStream errStream;
    protected PrintStream outStream;
    private static final String PACKAGES = "packages";
    private static final String ERR_CANNOT_CONNECT = "error: could not connect to remote repository to find package: ";
    private static final String ERR_CANNOT_PUSH = "error: failed to push the package: ";

    public CentralAPIClient() {
        String ballerinaCentralCliTokenUrl = getBallerinaCentralCliTokenUrl();
        Path ballerinaHomePath = RepoUtils.createAndGetHomeReposPath();
        Path settingsTomlFilePath = ballerinaHomePath.resolve(SETTINGS_FILE_NAME);
        Settings settings = readSettings();

        this.errStream = System.err;
        this.outStream = System.out;
        this.baseUrl = getRemoteRepoURL();
        this.accessToken = authenticate(errStream, ballerinaCentralCliTokenUrl, settings, settingsTomlFilePath);
        this.proxy = initializeProxy(settings.getProxy());
    }

    /**
     * Get package with version
     *
     * @param orgNamePath     The organization name of the package. (required)
     * @param packageNamePath The name of the package. (required)
     * @param version         The version or version range of the module. (required)
     * @return PackageJsonSchema
     */
    public Package getPackage(String orgNamePath, String packageNamePath, String version, String supportedPlatform) {
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
        conn.setRequestProperty("Ballerina-Platform", supportedPlatform);
        conn.setRequestProperty("Ballerina-Language-Specification-Version", IMPLEMENTATION_VERSION);

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
                    throw ErrorUtil.createCommandException(e.getMessage());
                }
            } else if (statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);
                    if (errorJsonSchema.getMessage().contains("package not found:")) {
                        throw new NoPackageException(errorJsonSchema.getMessage());
                    } else {
                        throw createLauncherException(
                                ERR_CANNOT_CONNECT + pkg + ". reason: " + errorJsonSchema.getMessage());
                    }
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(e.getMessage());
                }
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (BufferedReader errorStream = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(errorStream, Error.class);

                    if (errorJsonSchema.getMessage() != null && !"".equals(errorJsonSchema.getMessage())) {
                        throw new CommandException(errorJsonSchema.getMessage());
                    } else {
                        throw createLauncherException(ERR_CANNOT_CONNECT + pkg + ". reason:" + errorStream.lines()
                                .collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(e.getMessage());
                }
            } else {
                throw createLauncherException(ERR_CANNOT_CONNECT + pkg + ".");
            }
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    /**
     * Pushing a package to registry.
     */
    public void pushPackage(Path baloPath) {
        final int NO_OF_BYTES = 64;
        final int BUFFER_SIZE = 1024 * NO_OF_BYTES;

        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(PACKAGES);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.POST);

        // Load balo project
        BaloProject baloProject = BaloProject.loadProject(baloPath);
        String org = baloProject.currentPackage().packageDescriptor().org().toString();
        String name = baloProject.currentPackage().packageDescriptor().name().toString();
        String version = baloProject.currentPackage().packageDescriptor().version().toString();

        // Set headers
        conn.setRequestProperty(AUTHORIZATION, "Bearer " + this.accessToken);
        conn.setRequestProperty(PUSH_ORGANIZATION, org);
        conn.setRequestProperty(CONTENT_TYPE, APPLICATION_OCTET_STREAM);

        conn.setDoOutput(true);
        conn.setChunkedStreamingMode(BUFFER_SIZE);

        try (DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream())) {
            // Send balo content by 1 kb chunks
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            try (ProgressBar progressBar = new ProgressBar(
                    org + "/" + name + ":" + version + " [project repo -> central]", getTotalFileSizeInKB(baloPath),
                    1000, outStream, ProgressBarStyle.ASCII, " KB", 1);
                    FileInputStream fis = new FileInputStream(baloPath.toFile())) {
                while ((count = fis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, count);
                    outputStream.flush();
                    progressBar.stepBy((long) NO_OF_BYTES);
                }
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException("error occurred while uploading balo to central: " + e.getMessage());
        }

        try {
            int statusCode = getStatusCode(conn);
            // 200 - Module pushed successfully
            // Other - Error occurred, json returned with the error message
            if (statusCode == HttpURLConnection.HTTP_OK) {
                outStream.println(org + "/" + name + ":" + version + " pushed to central successfully");
            } else if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                errStream.println("unauthorized access token for organization: " + org);
            } else if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);

                    if (errorJsonSchema.getMessage() != null && !"".equals(errorJsonSchema.getMessage())) {
                        throw ErrorUtil.createCommandException(errorJsonSchema.getMessage());
                    } else {
                        throw ErrorUtil.createCommandException(
                                ERR_CANNOT_PUSH + "'" + org + "/" + name + ":" + version + "' reason:" + reader.lines()
                                        .collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(
                            ERR_CANNOT_PUSH + "'" + org + "/" + name + ":" + version + "' to the remote repository '"
                                    + conn.getURL() + "'");
                }
            } else {
                throw ErrorUtil.createCommandException(
                        ERR_CANNOT_PUSH + "'" + org + "/" + name + ":" + version + "' to the remote repository '"
                                + conn.getURL() + "'");
            }
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    public void pullPackage(String org, String name, String version, Path packagePathInBaloCache,
            String supportedPlatform, boolean isBuild) {
        LogFormatter logFormatter = new LogFormatter();
        if (isBuild) {
            logFormatter = new BuildLogFormatter();
        }

        String url = PACKAGES + "/" + org + "/" + name;
        // append version to url if available
        if (null != version && !version.isEmpty()) {
            url = url + "/" + version;
        }

        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(url);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

        // Set headers
        conn.setRequestProperty(BALLERINA_PLATFORM, supportedPlatform);
        conn.setRequestProperty(BAL_LANG_SPEC_VERSION, IMPLEMENTATION_VERSION);
        conn.setRequestProperty(ACCEPT_ENCODING, IDENTITY);
        conn.setRequestProperty(USER_AGENT, RepoUtils.getBallerinaVersion());

        try {
            // 302   - Package is found
            // Other - Error occurred, json returned with the error message
            if (getStatusCode(conn) == HttpURLConnection.HTTP_MOVED_TEMP) {
                // get redirect url from "location" header field
                String newUrl = conn.getHeaderField(LOCATION);
                String contentDisposition = conn.getHeaderField(CONTENT_DISPOSITION);

                conn = createHttpUrlConnection(newUrl);
                conn.setRequestProperty(CONTENT_DISPOSITION, contentDisposition);

                boolean isNightlyBuild = RepoUtils.getBallerinaVersion().contains("SNAPSHOT");
                createBaloInHomeRepo(conn, packagePathInBaloCache, org + "/" + name, isNightlyBuild, newUrl,
                        contentDisposition, outStream, logFormatter);
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);
                    throw ErrorUtil
                            .createCommandException(logFormatter.formatLog("error: " + errorJsonSchema.getMessage()));
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(logFormatter.formatLog(
                            "failed to pull the package '" + org + "/" + name + "' from the remote repository '" + url
                                    + "'"));
                }
            }
        } catch (Exception e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        } finally {
            conn.disconnect();
            Authenticator.setDefault(null);
        }
    }

    /**
     * Search packages in registry
     */
    public PackageSearchResult searchPackage(String query) {
        initializeSsl();
        HttpURLConnection conn = createHttpUrlConnection(PACKAGES + "/" + "?q=" + query);
        conn.setInstanceFollowRedirects(false);
        setRequestMethod(conn, Utils.RequestMethod.GET);

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
                    throw ErrorUtil.createCommandException(e.getMessage());
                }
            } else {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), Charset.defaultCharset()))) {
                    Error errorJsonSchema = new Gson().fromJson(reader, Error.class);

                    if (errorJsonSchema.getMessage() != null && !"".equals(errorJsonSchema.getMessage())) {
                        throw ErrorUtil.createCommandException(errorJsonSchema.getMessage());
                    } else {
                        throw ErrorUtil.createCommandException(reader.lines().collect(Collectors.joining("\n")));
                    }
                } catch (IOException e) {
                    throw ErrorUtil.createCommandException(e.getMessage());
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
    protected HttpURLConnection createHttpUrlConnection(String paths) {
        URL url = convertToUrl(this.baseUrl + "/" + paths);
        try {
            // set proxy if exists.
            if (this.proxy == null) {
                return (HttpURLConnection) url.openConnection();
            } else {
                return (HttpURLConnection) url.openConnection(this.proxy);
            }
        } catch (IOException e) {
            throw ErrorUtil.createCommandException(e.getMessage());
        }
    }
}
