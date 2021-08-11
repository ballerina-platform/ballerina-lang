/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * Loads the properties file.
 *
 * @since 2.0.0
 */
public class PropertiesLoader {
    public static final String APP_NAME = "app.name";
    public static final String REPL_PROMPT = "repl.prompt";
    public static final String HEADER_FILE = "resource.header.file";

    public static final String KEYWORDS_FILE = "resource.keywords.file";
    public static final String COMMANDS_FILE = "resource.commands.file";
    public static final String HELP_FILE = "resource.help.file";

    public static final String COMMAND_PREFIX = "commands.prefix";
    public static final String COMMAND_EXIT = "commands.exit";
    public static final String COMMAND_HELP = "commands.help";
    public static final String COMMAND_RESET = "commands.reset";
    public static final String COMMAND_DEBUG = "commands.debug";
    public static final String COMMAND_VARS = "commands.vars";
    public static final String COMMAND_IMPORTS = "commands.imports";
    public static final String COMMAND_DCLNS = "commands.dclns";
    public static final String COMMAND_DELETE = "commands.delete";
    public static final String COMMAND_FILE = "commands.file";

    public static final String HELP_DESCRIPTION_POSTFIX = "commands.help.ps.description";
    public static final String HELP_EXAMPLE_POSTFIX = "commands.help.ps.example";

    public static final String DESCRIPTION_URL_TEMPLATE = "help.description.url";
    public static final String EXAMPLE_URL_TEMPLATE = "help.example.url";

    private static final String PROPERTIES_FILE = "shell.properties";
    private static PropertiesLoader propertiesLoader;
    private final Properties properties;

    private PropertiesLoader() throws IOException {
        this.properties = load();
    }

    /**
     * Get the instance of the loader.
     *
     * @return Property loader instance.
     */
    public static synchronized PropertiesLoader getInstance() {
        try {
            if (propertiesLoader == null) {
                propertiesLoader = new PropertiesLoader();
            }
            return propertiesLoader;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get value associated with a key in props file.
     *
     * @param key Key to find.
     * @return Value of the key.
     */
    public static String getProperty(String key) {
        return getInstance().get(key);
    }

    /**
     * Load properties file from resources.
     *
     * @return Loaded properties file.
     * @throws IOException File reading failed.
     */
    private Properties load() throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
        Objects.requireNonNull(inputStream, "Properties file open failed.");
        properties.load(inputStream);
        return properties;
    }

    /**
     * Get value associated with a key in props file.
     *
     * @param key Key to find.
     * @return Value of the key.
     */
    public String get(String key) {
        return properties.getProperty(key);
    }
}
