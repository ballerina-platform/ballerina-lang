package org.ballerinalang.test.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * Test cases for reading encrypted fields from a configuration file through the config API.
 */
public class EncryptedConfigsTest {

    private static final ConfigRegistry registry = ConfigRegistry.getInstance();

    private CompileResult compileResult;
    private String resourceRoot;
    private Path sourceRoot;
    private Path confRoot;
    private String key;

    @BeforeClass
    public void setup() throws IOException {
        resourceRoot = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        sourceRoot = Paths.get(resourceRoot, "test-src", "config");
        confRoot = Paths.get(resourceRoot, "datafiles", "config");
        System.setProperty("ballerina.source.root", sourceRoot.toString());
        compileResult = BCompileUtil.compile(sourceRoot.resolve("encrypted_configs.bal").toString());
        key = "user.password";
    }

    @Test(description = "Test reading an encrypted value from a config file, using the secret file in source root")
    public void testReadEncryptedField() throws IOException {
        copySecret("secret.txt");
        registry.initRegistry(null, confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString(key)};
        BValue[] returns = BRunUtil.invoke(compileResult, "getDecryptedValue", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "password");
    }

    @Test(description =
                  "Test reading an encrypted value from a config file, using an explicitly provided secret file path")
    public void testReadEncryptedFieldUsingExplicitlyProvidedSecretFile() throws IOException {
        copySecret("secret.txt", "secret-copy.txt");
        registry.initRegistry(Collections.singletonMap("ballerina.config.secret",
                                                       Paths.get(resourceRoot, "datafiles", "config", "secret-copy.txt")
                                                               .toString()),
                              confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString(key)};
        BValue[] returns = BRunUtil.invoke(compileResult, "getDecryptedValue", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "password");
        Files.deleteIfExists(Paths.get(resourceRoot, "datafiles", "config", "secret-copy.txt"));
    }

    @Test(description = "Test reading an encrypted value from a config file, with a non-existent secret key file",
          expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*file not found:.*")
    public void testReadEncryptedFieldWithMissingSecretFile() throws IOException {
        registry.initRegistry(Collections.singletonMap("ballerina.config.secret",
                                                       Paths.get(resourceRoot, "datafiles", "config", "secret-copy.txt")
                                                               .toString()),
                              confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString(key)};
        BRunUtil.invoke(compileResult, "getDecryptedValue", args);
    }

    @Test(description = "Test for invalid secrets", expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = ".*failed to retrieve encrypted value.*")
    public void testReadEncryptedFieldWithInvalidSecret() throws IOException {
        copySecret("invalid-secret.txt", "invalid-secret-copy.txt");
        registry.initRegistry(Collections.singletonMap("ballerina.config.secret",
                                                       Paths.get(resourceRoot, "datafiles", "config",
                                                                 "invalid-secret-copy.txt").toString()),
                              confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString(key)};
        BRunUtil.invoke(compileResult, "getDecryptedValue", args);

        Files.deleteIfExists(Paths.get(resourceRoot, "datafiles", "config", "invalid-secret-copy.txt"));
    }

    private void copySecret(String file) throws IOException {
        Files.deleteIfExists(sourceRoot.resolve(file));
        Files.copy(Paths.get(resourceRoot, "datafiles", "config", file), sourceRoot.resolve(file));
    }

    private void copySecret(String from, String to) throws IOException {
        Files.copy(Paths.get(resourceRoot, "datafiles", "config", from),
                   Paths.get(resourceRoot, "datafiles", "config", to));
    }

    @AfterClass
    public void cleanup() {
        System.clearProperty("ballerina.source.root");
    }
}
