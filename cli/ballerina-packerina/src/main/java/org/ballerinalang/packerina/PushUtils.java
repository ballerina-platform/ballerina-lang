/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.packerina;

import org.ballerinalang.cli.module.Push;
import org.ballerinalang.cli.module.TokenUpdater;
import org.ballerinalang.cli.module.exeptions.CommandException;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.DependencyMetadata;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Proxy;
import org.ballerinalang.toml.model.Settings;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.IMPLEMENTATION_VERSION;
import static org.wso2.ballerinalang.programfile.ProgramFileConstants.SUPPORTED_PLATFORMS;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_DEV_STAGE_CENTRAL;
import static org.wso2.ballerinalang.util.RepoUtils.getRemoteRepoURL;

/**
 * This class provides util methods when pushing Ballerina modules to central and home repository.
 *
 * @since 0.95.2
 */
public class PushUtils {

    private static final String BALLERINA_CENTRAL_CLI_TOKEN = BALLERINA_DEV_STAGE_CENTRAL ?
                                                              "https://staging-central.ballerina.io/cli-token" :
                                                              "https://central.ballerina.io/cli-token";
    private static final Path BALLERINA_HOME_PATH = RepoUtils.createAndGetHomeReposPath();
    private static final Path SETTINGS_TOML_FILE_PATH = BALLERINA_HOME_PATH.resolve(
            ProjectDirConstants.SETTINGS_FILE_NAME);

    private static Settings settings;

    private static final PrintStream SYS_ERR = System.err;
    private static List<String> supportedPlatforms = Arrays.stream(SUPPORTED_PLATFORMS).collect(Collectors.toList());
    private static java.net.Proxy proxy;
    
    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    //No need to implement.
                }
            }
    };
    
    static {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            supportedPlatforms.add("any");
            proxy = getProxy();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // ignore errors
        }
    }
    
    /**
     * Push/Uploads modules to the remote repository.
     *
     * @param moduleNames List of modules to push to remote
     * @param sourceRootPath Path to the directory containing the Ballerina.toml
     */
    public static void pushModules(List<String> moduleNames, Path sourceRootPath) {
        try {
            Map<Path, List<Dependency>> balosWithDependencies = validateBalos(moduleNames, sourceRootPath);
            recursivelyPushBalos(balosWithDependencies);
        } catch (IOException e) {
            throw createLauncherException("unexpected error occurred when trying to push to remote repository: " +
                                          getRemoteRepoURL());
        }
    }
    
    /**
     * Push a balo file to remote repository.
     *
     * @param baloPath Path to the balo file.
     */
    private static void pushBaloToRemote(Path baloPath) {
        Path baloFileName = baloPath.getFileName();
        if (null != baloFileName) {
            // get the manifest from balo file
            Manifest manifest = RepoUtils.getManifestFromBalo(baloPath.toAbsolutePath());
            String moduleName = baloFileName.toString().split("-")[0];
            String orgName = manifest.getProject().getOrgName();
            String version = manifest.getProject().getVersion();
        
            // Get access token
            String accessToken = checkAccessToken();
            Proxy proxy = settings.getProxy();
            // Push module to central
            String urlWithModulePath = RepoUtils.getRemoteRepoURL() + "/modules/";

            try {
                Push.execute(urlWithModulePath, proxy.getHost(), proxy.getPort(), proxy.getUserName(),
                        proxy.getPassword(), accessToken, orgName, moduleName, version, baloPath.toAbsolutePath());
            } catch (CommandException e) {
                String errorMessage = e.getMessage();
                if (null != errorMessage && !"".equals(errorMessage.trim())) {
                    // removing the error stack
                    if (errorMessage.contains("\n\tat")) {
                        errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                    }

                    errorMessage = errorMessage.replaceAll("error: ", "");

                    throw createLauncherException(
                            "unexpected error occurred while pushing module '" + moduleName + "' to remote repository("
                                    + getRemoteRepoURL() + "): " + errorMessage);
                }
            }
        }
    }
    
    /**
     * Perform validations on the balo files to be pushed.
     *
     * @param moduleNames    The list of modules to push.
     * @param sourceRootPath The path to the project root.
     * @return A map of balo files along with their unresolved dependencies.
     * @throws IOException When trying to access remote repository
     */
    private static Map<Path, List<Dependency>> validateBalos(List<String> moduleNames, Path sourceRootPath)
            throws IOException {
        Map<Path, List<Dependency>> balos = new HashMap<>();
        
        for (String moduleName : moduleNames) {
            // Get balo output path
            Path baloOutputDir = Paths.get(sourceRootPath.toString(), ProjectDirConstants.TARGET_DIR_NAME,
                    ProjectDirConstants.TARGET_BALO_DIRECTORY);
    
            if (Files.notExists(baloOutputDir)) {
                throw createLauncherException("cannot find balo file for the module: " + moduleName + ". Run " +
                                              "'ballerina build -c <module_name>' to compile and generate the balo.");
            }
    
            Optional<Path> moduleBaloFile;
            try (Stream<Path> baloFilesStream = Files.list(baloOutputDir)) {
                moduleBaloFile = baloFilesStream.filter(baloFile -> null != baloFile.getFileName() &&
                        baloFile.getFileName().toString().startsWith(moduleName + "-" +
                                IMPLEMENTATION_VERSION))
                        .findFirst();
            }

            if (!moduleBaloFile.isPresent()) {
                throw createLauncherException("cannot find balo file for the module: " + moduleName + ". Run " +
                                              "'ballerina build -c <module_name>' to compile and generate the balo.");
            }
    
            // get the manifest from balo file
            Path baloFilePath = moduleBaloFile.get();
            Manifest manifest = RepoUtils.getManifestFromBalo(baloFilePath.toAbsolutePath());
            
            // Validate the org-name
            if (!RepoUtils.validateOrg(manifest.getProject().getOrgName())) {
                throw createLauncherException("invalid organization name provided \'" +
                                              manifest.getProject().getOrgName() + "\'. Only lowercase alphanumerics " +
                                              "and underscores are allowed in an organization name and the maximum " +
                                              "length is 256 characters");
            }
    
            // Validate the module-name
            if (!RepoUtils.validatePkg(moduleName)) {
                throw createLauncherException("invalid module name provided \'" + moduleName + "\'. Only " +
                                              "alphanumerics, underscores and periods are allowed in a module name " +
                                              "and the maximum length is 256 characters");
            }
    
            if (!RepoUtils.validatePkg(moduleName)) {
                throw createLauncherException("invalid module name provided \'" + moduleName + "\'. Only " +
                                              "alphanumerics, underscores and periods are allowed in a module name " +
                                              "and the maximum length is 256 characters");
            }
    
            // check if there are any dependencies with balo path
            List<String> dependenciesWithBaloPath =
                    manifest.getDependencies().stream()
                            .filter(dep -> dep.getMetadata().getPath() != null)
                            .map(Dependency::getModuleID)
                            .collect(Collectors.toList());
    
            if (dependenciesWithBaloPath.size() > 0) {
                throw createLauncherException("dependencies cannot be given by path when pushing module(s) to " +
                                              "remote. check dependencies in Ballerina.toml: [" +
                                              String.join(", ", dependenciesWithBaloPath) + "]");
            }
            
            // check if the module is already there in remote repository
            Dependency moduleAsDependency = new Dependency();
            moduleAsDependency.setModuleID(manifest.getProject().getOrgName() + "/" + moduleName);
            DependencyMetadata depMetadata = new DependencyMetadata();
            depMetadata.setVersion(manifest.getProject().getVersion());
            moduleAsDependency.setMetadata(depMetadata);
            if (isDependencyAvailableInRemote(moduleAsDependency)) {
                throw createLauncherException("module '" + moduleAsDependency.toString() + "' already exists in " +
                                              "remote repository(" + getRemoteRepoURL() + "). build and push after " +
                                              "update the version in the Ballerina.toml.");
            }
    
            balos.put(baloFilePath, manifest.getDependencies());
        }
    
        // check if non project dependencies are available in remote repository
        for (Map.Entry<Path, List<Dependency>> baloPath : balos.entrySet()) {
            Path baloFileName = baloPath.getKey().getFileName();
            if (null != baloFileName) {
                String moduleName = baloFileName.toString().split("-")[0];
                Manifest manifest = RepoUtils.getManifestFromBalo(baloPath.getKey().toAbsolutePath());
                Iterator<Dependency> dependencies = baloPath.getValue().iterator();
                while (dependencies.hasNext()) {
                    Dependency dependency = dependencies.next();
                    // Throw errors in the following scenarios if they are not available in central.
                    // 1. If dependency is from a different org.
                    // 2. If dependency has same org as manifest but the dependency name is not from the project.
                    if (!"ballerina".equals(dependency.getOrgName()) &&
                        !"ballerinax".equals(dependency.getOrgName()) &&
                        !dependency.getOrgName().equals(manifest.getProject().getOrgName()) ||
                        (dependency.getOrgName().equals(manifest.getProject().getOrgName()) &&
                         !ProjectDirs.isModuleExist(sourceRootPath, dependency.getModuleName()))) {
                        if (!isDependencyAvailableInRemote(dependency)) {
                            throw createLauncherException("'" + dependency.toString() + "' which is a dependency of " +
                                                          "module '" + moduleName + "' cannot be found in remote " +
                                                          "repository(" + RepoUtils.getRemoteRepoURL() + ")");
                        } else {
                            // remove from list if deps are available in central.
                            dependencies.remove();
                        }
                    }
                }
            }
        }
    
        return balos;
    }
    
    /**
     * Push balos to remote repository in the order of there dependencies are resolved.
     *
     * @param balos The remaining balos to be pushed.
     * @throws IOException When trying to access remote repository
     */
    private static void recursivelyPushBalos(Map<Path, List<Dependency>> balos) throws IOException {
        // if there are no more balos to push.
        if (balos.size() == 0) {
            return;
        }
    
        // go through the dependencies of balos and see if they are available in remote repository. if they are
        // available remove them from the list.
        for (List<Dependency> deps : balos.values()) {
            Iterator<Dependency> depsIterator = deps.iterator();
            while (depsIterator.hasNext()) {
                Dependency dep = depsIterator.next();
                if (isDependencyAvailableInRemote(dep)) {
                    depsIterator.remove();
                }
                
                if ("ballerina".equals(dep.getOrgName()) || "ballerinax".equals(dep.getOrgName())) {
                    depsIterator.remove();
                }
            }
        }
        
        // check if there are balos where their dependencies are already available in remote repository
        Optional<List<Dependency>> baloWithAllDependenciesAvailableInCentral = balos.values().stream()
                .filter(depList -> depList.size() == 0)
                .findAny();
    
        // if there isn't any balos where dependencies are resolved, then throw an error.
        if (!baloWithAllDependenciesAvailableInCentral.isPresent()) {
            Set<String> unresolvedDependencies = balos.values().stream()
                    .flatMap(List::stream)
                    .map(Dependency::toString)
                    .collect(Collectors.toSet());
            throw createLauncherException("unable to find dependencies in remote repository: [" +
                                          String.join(", ", unresolvedDependencies) + "]");
        }
        
        // push all the modules where dependencies are available in remote repository and remove them from the map.
        Iterator<Map.Entry<Path, List<Dependency>>> iterator = balos.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Path, List<Dependency>> baloDeps = iterator.next();
            if (baloDeps.getValue().size() == 0) {
                pushBaloToRemote(baloDeps.getKey());
                iterator.remove();
            }
        }
    
        recursivelyPushBalos(balos);
    }
    
    private static boolean isDependencyAvailableInRemote(Dependency dep) throws IOException {
        URI baseURI = URI.create(RepoUtils.getRemoteRepoURL() + "/modules/");
        String moduleUrl = baseURI.toString() + dep.getOrgName() + "/" + dep.getModuleName();
        
        // append version to url if available
        if (null != dep.getMetadata() && null != dep.getMetadata().getVersion() &&
            !dep.getMetadata().getVersion().isEmpty()) {
            moduleUrl = moduleUrl + "/" + dep.getMetadata().getVersion();
        }
        
        for (String supportedPlatform : supportedPlatforms) {
            HttpURLConnection conn;
            // set proxy if exists.
            if (null == proxy) {
                conn = (HttpURLConnection) URI.create(moduleUrl).toURL().openConnection();
            } else {
                conn = (HttpURLConnection) URI.create(moduleUrl).toURL().openConnection(proxy);
            }
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            // set implementation version
            conn.setRequestProperty("Ballerina-Platform", supportedPlatform);
            conn.setRequestProperty("Ballerina-Language-Specification-Version", IMPLEMENTATION_VERSION);
        
            // status code and meaning
            //// 302 - module found
            //// 404 - module not found
            //// 400 - bad request sent
            //// 500 - backend is broken
            int statusCode = conn.getResponseCode();
            if (statusCode == 302) {
                return true;
            } else if (statusCode == 400) {
                try (BufferedReader errorStream = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), Charset.defaultCharset()))) {
                    String errorContent = errorStream.lines().collect(Collectors.joining("\n"));
                    throw createLauncherException("error: could not connect to remote repository to find module: " +
                                                  dep.toString() + ". reason: " + errorContent);
                }
            } else if (statusCode == 500) {
                throw createLauncherException("error: could not connect to remote repository to find module: " +
                                              dep.toString() + ".");
            }
            conn.disconnect();
            Authenticator.setDefault(null);
        }
        
        return false;
    }
    
    /**
     * Checks if the access token is available in Settings.toml or not.
     *
     * @return access token if its present
     */
    private static String checkAccessToken() {
        String accessToken = getAccessTokenOfCLI();

        if (accessToken.isEmpty()) {
            try {
                SYS_ERR.println("Opening the web browser to " + BALLERINA_CENTRAL_CLI_TOKEN +
                                        " for auto token update ...");

                BrowserLauncher.startInDefaultBrowser(BALLERINA_CENTRAL_CLI_TOKEN);
            } catch (IOException e) {
                throw createLauncherException("Access token is missing in " + SETTINGS_TOML_FILE_PATH.toString() +
                                                 "\nAuto update failed. Please visit https://central.ballerina.io");
            }
            long modifiedTimeOfFileAtStart = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
            TokenUpdater.execute(SETTINGS_TOML_FILE_PATH.toString());

            boolean waitForToken = true;
            while (waitForToken) {
                pause();
                long modifiedTimeOfFileAfter = getLastModifiedTimeOfFile(SETTINGS_TOML_FILE_PATH);
                if (modifiedTimeOfFileAtStart != modifiedTimeOfFileAfter) {
                    accessToken = getAccessTokenOfCLI();
                    if (accessToken.isEmpty()) {
                        throw createLauncherException("Access token is missing in " +
                                                                 SETTINGS_TOML_FILE_PATH.toString() + "\nPlease " +
                                                                 "visit https://central.ballerina.io");
                    } else {
                        waitForToken = false;
                    }
                }
            }
        }
        return accessToken;
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
    private static long getLastModifiedTimeOfFile(Path path) {
        if (!Files.isRegularFile(path)) {
            return -1;
        }
        try {
            return Files.getLastModifiedTime(path).toMillis();
        } catch (IOException ex) {
            throw createLauncherException("Error occurred when reading file for token " +
                                             SETTINGS_TOML_FILE_PATH.toString());
        }
    }

    /**
     * Read the access token generated for the CLI.
     *
     * @return access token for generated for the CLI
     */
    private static String getAccessTokenOfCLI() {
        settings = TomlParserUtils.readSettings();
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
     * Push all modules to central.
     *
     * @param sourceRootPath source root or project root
     */
    public static void pushAllModules(Path sourceRootPath) {
        try {
            List<String> fileList;
            try (Stream<Path> moduleDirsStream = Files.list(sourceRootPath
                    .resolve(ProjectDirConstants.SOURCE_DIR_NAME))) {
                fileList = moduleDirsStream.filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
                        .map(ProjectDirs::getLastComp)
                        .filter(dirName -> !isSpecialDirectory(dirName))
                        .map(Path::toString).collect(Collectors.toList());
            }

            if (fileList.size() == 0) {
                throw createLauncherException("no modules found to push in " + sourceRootPath.toString());
            }
            
            pushModules(fileList, sourceRootPath);
        } catch (IOException ex) {
            throw createLauncherException("error occurred while pushing modules from " + sourceRootPath.toString()
                                                     + " " + ex.getMessage());
        }
    }

    /**
     * Checks if the directory is a special directory that is not a module.
     *
     * @param dirName directory name
     * @return if the directory is a special directory or not
     */
    private static boolean isSpecialDirectory(Path dirName) {
        List<String> ignoreDirs = Arrays.asList(ProjectDirConstants.TARGET_DIR_NAME,
                                                ProjectDirConstants.RESOURCE_DIR_NAME);
        String dirNameStr = dirName.toString();
        return dirNameStr.startsWith(".") || dirName.toFile().isHidden() || ignoreDirs.contains(dirNameStr);
    }
    
    private static java.net.Proxy getProxy() {
        org.ballerinalang.toml.model.Proxy proxy = TomlParserUtils.readSettings().getProxy();
        if (!"".equals(proxy.getHost())) {
            InetSocketAddress proxyInet = new InetSocketAddress(proxy.getHost(), proxy.getPort());
            if (!"".equals(proxy.getUserName()) && "".equals(proxy.getPassword())) {
                Authenticator authenticator = new RemoteAuthenticator();
                Authenticator.setDefault(authenticator);
            }
            return new java.net.Proxy(java.net.Proxy.Type.HTTP, proxyInet);
        }
        
        return null;
    }
    
    static class RemoteAuthenticator extends Authenticator {
        org.ballerinalang.toml.model.Proxy proxy;
        public RemoteAuthenticator() {
            proxy = TomlParserUtils.readSettings().getProxy();
        }
        
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return (new PasswordAuthentication(this.proxy.getUserName(), this.proxy.getPassword().toCharArray()));
        }
    }
}
