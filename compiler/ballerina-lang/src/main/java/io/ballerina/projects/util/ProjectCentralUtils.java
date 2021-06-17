package io.ballerina.projects.util;

import com.google.gson.Gson;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.TomlDocument;
import io.ballerina.projects.internal.SettingsBuilder;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.ballerinalang.central.client.LogFormatter;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.Error;

import javax.net.ssl.HttpsURLConnection;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.io.IOException;
import java.io.PrintStream;

import static io.ballerina.projects.util.ProjectUtils.createAndGetHomeReposPath;
import static org.ballerinalang.central.client.Utils.createBalaInHomeRepo;


/**
 * Utilities related to Central functionality.
 *
 * @since 2.0.0
 */
public class ProjectCentralUtils {

    static final String BALLERINA_PLATFORM = "Ballerina-Platform";
    static final String IDENTITY = "identity";
    static final String AUTHORIZATION = "Authorization";
    static final String ACCEPT_ENCODING = "Accept-Encoding";
    static final String USER_AGENT = "User-Agent";
    static final String LOCATION = "Location";
    static final String ACCEPT = "Accept";
    static final String CONTENT_DISPOSITION = "Content-Disposition";
    static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    static final String APPLICATION_JSON = "application/json";
    static final PrintStream OUTSTREAM = System.out;
    private static final String PACKAGES = "packages";
    private static final String SUPPORTED_PLATFORM = "java11";
    private static final String ERR_CANNOT_PULL_PACKAGE = "error: failed to pull the package: ";

    public static boolean pullPackage(String orgName, String packageName, String version) throws ProjectException {
        // Initialize proxy and access Token
        Settings settings = readSettings();
        Proxy proxy = ProjectUtils.initializeProxy(settings.getProxy());
        String accessToken = ProjectUtils.getAccessTokenOfCLI(settings);

        // Resolve url and pacakge signature
        String packageSignature = orgName + "/" + packageName;
        String url = RepoUtils.getRemoteRepoURL() + "/" + PACKAGES + "/" + orgName + "/" + packageName;

        if (version != null && !version.isEmpty()) {
            url += "/" + version;
            packageSignature += ":" + version;
        } else {
            url += "/*";
            packageSignature += ":*";
        }

        Optional<ResponseBody> body = Optional.empty();
        OkHttpClient client = getOkHttpClient(proxy);

        try {
            LogFormatter logFormatter = new LogFormatter();

            Request packagePullReq =
                    getNewRequest(SUPPORTED_PLATFORM, ProjectUtils.getBallerinaVersion(), accessToken)
                            .get()
                            .url(url)
                            .addHeader(ACCEPT_ENCODING, IDENTITY)
                            .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                            .build();

            Call packagePullReqCall = client.newCall(packagePullReq);
            Response packagePullResponse = packagePullReqCall.execute();

            // 302 - Package is found
            if (packagePullResponse.code() == HttpsURLConnection.HTTP_MOVED_TEMP) {
                // get redirect url from "location" header field
                Optional<String> balaUrl = Optional.ofNullable(packagePullResponse.header(LOCATION));
                Optional<String> balaFileName = Optional.ofNullable(packagePullResponse.header(CONTENT_DISPOSITION));

                if (balaUrl.isPresent() && balaFileName.isPresent()) {
                    Request downloadBalaRequest =
                            getNewRequest(SUPPORTED_PLATFORM, ProjectUtils.getBallerinaVersion(), accessToken)
                                    .get()
                                    .url(balaUrl.get())
                                    .header(ACCEPT_ENCODING, IDENTITY)
                                    .addHeader(CONTENT_DISPOSITION, balaFileName.get())
                                    .build();

                    Call downloadBalaRequestCall = client.newCall(downloadBalaRequest);
                    Response balaDownloadResponse = downloadBalaRequestCall.execute();
                    boolean isNightlyBuild = ProjectUtils.getBallerinaVersion().contains("SNAPSHOT");

                    Path packagePathInBalaCache =
                            ProjectUtils.createAndGetHomeReposPath()
                                    .resolve(ProjectConstants.REPOSITORIES_DIR)
                                    .resolve(ProjectConstants.CENTRAL_REPOSITORY_CACHE_NAME)
                                    .resolve(ProjectConstants.BALA_DIR_NAME)
                                    .resolve(orgName)
                                    .resolve(packageName);

                    createBalaInHomeRepo(balaDownloadResponse, packagePathInBalaCache, orgName, packageName,
                            isNightlyBuild, balaUrl.get(), balaFileName.get(), OUTSTREAM, logFormatter);
                    return true;
                } else {
                    String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                            "' from the remote repository '" + url + "'. reason: bala file location is missing.");
                    throw new ProjectException(errorMsg);
                }
            }

            body = Optional.ofNullable(packagePullResponse.body());

            if (body.isPresent()) {
                Optional<MediaType> contentType = Optional.ofNullable(body.get().contentType());
                if (contentType.isPresent() && isApplicationJsonContentType(contentType.get().toString())) {
                    // If request sent is invalid or when package is not found
                    if (packagePullResponse.code() == HttpsURLConnection.HTTP_BAD_REQUEST ||
                            packagePullResponse.code() == HttpsURLConnection.HTTP_NOT_FOUND) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            throw new ProjectException("error: " + error.getMessage());
                        }
                    }

                    //  When error occurred at remote repository
                    if (packagePullResponse.code() == HttpsURLConnection.HTTP_INTERNAL_ERROR) {
                        Error error = new Gson().fromJson(body.get().string(), Error.class);
                        if (error.getMessage() != null && !"".equals(error.getMessage())) {
                            String errorMsg =
                                    logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature + "' from" +
                                            " the remote repository '" + url +
                                            "'. reason: " + error.getMessage());
                            throw new ProjectException(errorMsg);
                        }
                    }
                }
            }

            String errorMsg = logFormatter.formatLog(ERR_CANNOT_PULL_PACKAGE + "'" + packageSignature +
                    "' from the remote repository '" + url + "'.");
            throw new ProjectException(errorMsg);
        } catch (IOException | ProjectException | CentralClientException e) {
            throw new ProjectException(e.getMessage());
        } finally {
            body.ifPresent(ResponseBody::close);
            try {
                closeClient(client);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static OkHttpClient getOkHttpClient(Proxy proxy) {
        return new OkHttpClient.Builder()
                .followRedirects(false)
                .proxy(proxy)
                .build();
    }

    private static Request.Builder getNewRequest(String supportedPlatform, String ballerinaVersion,
                                                 String accessToken) {
        if (accessToken.isEmpty()) {
            return new Request.Builder()
                    .addHeader(BALLERINA_PLATFORM, supportedPlatform)
                    .addHeader(USER_AGENT, ballerinaVersion);
        } else {
            return new Request.Builder()
                    .addHeader(BALLERINA_PLATFORM, supportedPlatform)
                    .addHeader(USER_AGENT, ballerinaVersion)
                    .addHeader(AUTHORIZATION, getBearerToken(accessToken));
        }
    }

    private static String getBearerToken(String accessToken) {
        return "Bearer " + accessToken;
    }

    /**
     * Closes the http client.
     *
     * @param client the client
     * @throws IOException when cache of the client cannot be closed
     */
    private static void closeClient(OkHttpClient client) throws IOException {
        client.dispatcher().executorService().shutdown();
        client.connectionPool().evictAll();
        Optional<Cache> clientCache = Optional.ofNullable(client.cache());
        if (clientCache.isPresent()) {
            clientCache.get().close();
        }
    }

    static boolean isApplicationJsonContentType(String contentType) {
        return contentType.startsWith(APPLICATION_JSON);
    }

    /**
     * Read Settings.toml to populate the configurations.
     *
     * @return {@link Settings} settings object
     */
    private static Settings readSettings() {
        Path settingsFilePath = createAndGetHomeReposPath().resolve(ProjectConstants.SETTINGS_FILE_NAME);
        try {
            TomlDocument settingsTomlDocument = TomlDocument
                    .from(String.valueOf(settingsFilePath.getFileName()), Files.readString(settingsFilePath));
            SettingsBuilder settingsBuilder = SettingsBuilder.from(settingsTomlDocument);
            return settingsBuilder.settings();
        } catch (IOException e) {
            // If Settings.toml not exists return empty Settings object
            return Settings.from();
        }
    }

}
