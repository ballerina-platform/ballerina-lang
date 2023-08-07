package org.ballerinalang.maven.bala.client;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class MavenResolverClientTest extends MavenResolverClient{

    public static final String BALA_EXTENSION = ".bala";
    public static final String REMOTE_REPO = "repo-push-pull";
    public static final String POM_EXTENSION = ".pom";
    String pushPullRepo = Paths.get("build") + File.separator + REMOTE_REPO;
    Path balaPath = Paths.get("src", "test", "resources", "luheerathan-pact1-any-0.1.0.bala");

    public MavenResolverClientTest() {
        super();
    }

//    @Test (description = "Push a package to a remote repository")
//    public void testPushPackage() throws MavenResolverClientException {
//        String org = "luheerathan";
//        String packageName = "pact1";
//        String version = "0.1.0";
//        MavenResolverClientTest mvnClient = new MavenResolverClientTest();
//        mvnClient.addRepository(REMOTE_REPO, "file:" + pushPullRepo);
//        mvnClient.pushPackage(balaPath, org, packageName, version, Paths.get(pushPullRepo));
//        String artifact = packageName + "-" + version + BALA_EXTENSION;
//        String pomFile = packageName + "-" + version + POM_EXTENSION;
//        for (String ext : new String[]{".sha1", ".md5", ""}) {
//            Assert.assertTrue(Paths.get(pushPullRepo, org, packageName, version, artifact + ext).toFile().exists());
//            Assert.assertTrue(Paths.get(pushPullRepo, org, packageName, version, pomFile + ext).toFile().exists());
//        }
//    }
//
//    @Test (description = "Pull a package from a remote repository", dependsOnMethods = {"testPushPackage"})
//    public void testPullPackage() {
//
//    }
}
