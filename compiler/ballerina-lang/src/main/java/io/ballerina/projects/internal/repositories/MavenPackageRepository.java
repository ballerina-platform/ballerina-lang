package io.ballerina.projects.internal.repositories;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.Settings;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.internal.model.Proxy;
import io.ballerina.projects.internal.model.Repository;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.maven.bala.client.MavenResolverClient;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.ballerinalang.toml.exceptions.SettingsTomlException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static io.ballerina.projects.util.ProjectConstants.BALA_EXTENSION;

public class MavenPackageRepository extends CustomPackageRepository implements CustomRepositoryHelper {

    public static final String EMPTY = "";

    public static final String PLATFORM = "platform";
    private final MavenResolverClient client;
    private final String repoLocation;


    public MavenPackageRepository(Environment environment, Path cacheDirectory, String distributionVersion,
                                  String repoId, MavenResolverClient client) {
        super(environment, cacheDirectory, distributionVersion, repoId);
        this.client = client;
        this.repoLocation = cacheDirectory.resolve("bala").toAbsolutePath().toString();
    }

    public static MavenPackageRepository from(Environment environment, Path cacheDirectory, Repository repository){
        if (Files.notExists(cacheDirectory)) {
            throw new ProjectException("cache directory does not exists: " + cacheDirectory);
        }

        if (EMPTY.equals(repository.url())) {
            throw new ProjectException("repository url is not provided");
        }
        String ballerinaShortVersion = RepoUtils.getBallerinaShortVersion();
        MavenResolverClient mvnClient = new MavenResolverClient();
        if (!EMPTY.equals(repository.username()) && !EMPTY.equals(repository.password())) {
            mvnClient.addRepository(repository.id(), repository.url(), repository.username(), repository.password());
        } else {
            mvnClient.addRepository(repository.id(), repository.url());
        }

        Settings settings;
        try {
            settings = RepoUtils.readSettings();
        } catch (SettingsTomlException e) {
            settings = Settings.from();
        }
        Proxy proxy = settings.getProxy();
        mvnClient.setProxy(proxy.host(), proxy.port(), proxy.username(), proxy.password());

        return new MavenPackageRepository(environment, cacheDirectory, ballerinaShortVersion, repository.id(), mvnClient);
    }


    @Override
    public boolean getPackageFromRemoteRepo(String org,
                                               String name,
                                               String version) {
        try {
            this.client.pullPackage(org, name, version, this.repoLocation);
        } catch (MavenResolverClientException e) {
            System.out.println("Error while pulling package [" + org + "/" + name + ":" + version + "]: " +
                    e.getMessage());
            return false;
        }

        Path balaDownloadPath = Paths.get(this.repoLocation).resolve(org).resolve(name).resolve(version)
                .resolve(name + "-" + version + BALA_EXTENSION);
        Path balaHashPath = Paths.get(this.repoLocation).resolve(org).resolve(name).resolve(version)
                .resolve(name + "-" + version + BALA_EXTENSION + ".sha1");
        Path temporaryExtractionPath = Paths.get(this.repoLocation, org, name, version, PLATFORM);
        try {
            ProjectUtils.extractBala(balaDownloadPath, temporaryExtractionPath);
            Path packageJsonPath = temporaryExtractionPath.resolve("package.json");
            BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath, StandardCharsets.UTF_8);
            JsonObject resultObj = new Gson().fromJson(bufferedReader, JsonObject.class);
            String platform = resultObj.get(PLATFORM).getAsString();
            Path actualBalaPath = Paths.get(this.repoLocation, org, name, version, platform);
            temporaryExtractionPath.toFile().renameTo(actualBalaPath.toFile());
            Files.delete(balaDownloadPath);
            Files.delete(balaHashPath);
            Files.delete(balaDownloadPath.getParent().resolve("_remote.repositories"));
            if (Files.exists(temporaryExtractionPath)) {
                Files.walk(temporaryExtractionPath)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
        } catch (IOException e) {
            System.out.println("Unable to create bala for the package [" + org + "/" + name + ":" + version + "]: " +
                    e.getMessage());
            return false;
        }
        return true;
    }
}
