/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.configurable;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.ballerinalang.config.cipher.AESCipherTool;
import org.ballerinalang.config.cipher.AESCipherToolException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlConstants.SECRET_FILE_NAME;

/**
 * Singleton class that holds security functions related to configurable values.
 *
 * @since 2.0.0
 */
public class ConfigSecurityUtils {

    private static final Pattern encryptedFieldPattern = Pattern.compile("^@encrypted:\\{(.*)\\}$");

    private static AESCipherTool cipherTool = null;

    private static final PrintStream stderr = System.err;

    private ConfigSecurityUtils() {
    }

    public static String decryptString(String value) {
        Matcher encryptedStringMatcher = encryptedFieldPattern.matcher(value);
        if (encryptedStringMatcher.find()) {
            try {
                return cipherTool.decrypt(encryptedStringMatcher.group(1));
            } catch (IllegalArgumentException | AESCipherToolException e) {
                throw ErrorCreator.createError(StringUtils.fromString("failed to retrieve encrypted value: " +
                                                                              e.getMessage()));
            }
        }
        return value;
    }

    public static void handleEncryptedValues(String variableName, String value) {
        Matcher encryptedStringMatcher = encryptedFieldPattern.matcher(value);
        if (!encryptedStringMatcher.find()) {
            return;
        }
        if (cipherTool == null) {
            Path userSecretFile = getSecretPath();
            try {
                if (Files.exists(userSecretFile)) {
                    cipherTool = new AESCipherTool(userSecretFile);
                } else {
                    // Prompting when secret file is explicitly not set by the user
                    stderr.println("ballerina: enter secret for config value decryption:");
                    cipherTool = new AESCipherTool(new String(System.console().readPassword()));
                }
            } catch (IOException | AESCipherToolException e) {
                String msg = "failed to initialize the cipher tool: " + e.getMessage();
                throw ErrorCreator.createError(StringUtils.fromString(msg));
            } catch (IndexOutOfBoundsException e) {
                throw ErrorCreator.createError(StringUtils.fromString("failed to initialize the cipher tool due to " +
                                                                              "empty secret text"));
            }
        }
        // Validate the encrypted values.
        try {
            cipherTool.decrypt(encryptedStringMatcher.group(1));
        } catch (IllegalArgumentException | AESCipherToolException e) {
            throw ErrorCreator
                    .createError(StringUtils.fromString("failed to retrieve the encrypted value for variable: '" +
                                                                variableName + "' : " + e.getMessage()));
        }
    }

    private static Path getSecretPath() {
        Map<String, String> envVariables = System.getenv();
        return Paths.get(envVariables.getOrDefault(ConfigTomlConstants.CONFIG_SECRET_ENV_VARIABLE,
                                                   Paths.get(RuntimeUtils.USER_DIR, SECRET_FILE_NAME).toString()));
    }
}
