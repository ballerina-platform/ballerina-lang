package org.ballerinalang.stdlib.config;

import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
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
    public void setup() {
        resourceRoot = Paths.get("src", "test", "resources").toAbsolutePath().toString();
        sourceRoot = Paths.get(resourceRoot, "test-src");
        confRoot = Paths.get(resourceRoot, "datafiles");
        System.setProperty("ballerina.source.root", sourceRoot.toString());
        compileResult = BCompileUtil.compile(sourceRoot.resolve("encrypted_configs.bal").toString());
        key = "\"user.password\"";
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
        registry.initRegistry(Collections.singletonMap("b7a.config.secret",
                                                       Paths.get(resourceRoot, "datafiles", "secret-copy.txt")
                                                               .toString()),
                              confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString(key)};
        BValue[] returns = BRunUtil.invoke(compileResult, "getDecryptedValue", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(returns[0].stringValue(), "password");
        Files.deleteIfExists(Paths.get(resourceRoot, "datafiles", "secret-copy.txt"));
    }

    @Test(description = "Test reading an encrypted value from a config file, with a non-existent secret key file",
          expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*file not found:.*")
    public void testReadEncryptedFieldWithMissingSecretFile() throws IOException {
        registry.initRegistry(Collections.singletonMap("b7a.config.secret",
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
        registry.initRegistry(Collections.singletonMap("b7a.config.secret",
                                                       Paths.get(resourceRoot, "datafiles",
                                                                 "invalid-secret-copy.txt").toString()),
                              confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString(key)};
        BRunUtil.invoke(compileResult, "getDecryptedValue", args);

        Files.deleteIfExists(Paths.get(resourceRoot, "datafiles", "invalid-secret-copy.txt"));
    }

    @Test(description = "Test reading an encrypted int value from a config file")
    public void testIntEncryptedField() throws IOException {
        copySecret("secret.txt");
        registry.initRegistry(null, confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString("\"int.value\"")};
        BValue[] returns = BRunUtil.invoke(compileResult, "getDecryptedInt", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 100);
    }

    @Test(description = "Test reading an encrypted float value from a config file")
    public void testFloatEncryptedField() throws IOException {
        copySecret("secret.txt");
        registry.initRegistry(null, confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString("\"float.value\"")};
        BValue[] returns = BRunUtil.invoke(compileResult, "getDecryptedFloat", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 123.45);
    }

    @Test(description = "Test reading an encrypted boolean value from a config file")
    public void testBooleanEncryptedField() throws IOException {
        copySecret("secret.txt");
        registry.initRegistry(null, confRoot.resolve("encrypted-configs.conf").toString(), null);

        BValue[] args = {new BString("\"boolean.value\"")};
        BValue[] returns = BRunUtil.invoke(compileResult, "getDecryptedBoolean", args);

        Assert.assertNotNull(returns);
        Assert.assertEquals(((BBoolean) returns[0]).booleanValue(), true);
    }

    private void copySecret(String file) throws IOException {
        Files.deleteIfExists(sourceRoot.resolve(file));
        Files.copy(Paths.get(resourceRoot, "datafiles", file), sourceRoot.resolve(file));
    }

    private void copySecret(String from, String to) throws IOException {
        Files.deleteIfExists(Paths.get(resourceRoot, "datafiles", to));
        Files.copy(Paths.get(resourceRoot, "datafiles", from),
                   Paths.get(resourceRoot, "datafiles", to));
    }

    @AfterClass
    public void cleanup() {
        System.clearProperty("ballerina.source.root");
    }
}
