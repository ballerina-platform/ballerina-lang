/*
 *  Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.transport.http.netty.contract.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Configuration model builder for netty transport configuration file.
 * <p>
 * This parses the netty transport configuration file and build the object model {@code TransportsConfiguration}
 *
 * @since 4.2.0
 */
public class ConfigurationBuilder {

    /* Environment variable which holds the location of the location of the transport configuration file */
    private static final String NETTY_TRANSPORT_CONF = "transports.netty.conf";

    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationBuilder.class);

    private static ConfigurationBuilder instance = new ConfigurationBuilder();

    public static ConfigurationBuilder getInstance() {
        return instance;
    }

    private ConfigurationBuilder() {
    }

    /**
     * Get the Environment {@code TransportsConfiguration}
     * <p>
     * Location of the configuration file should be defined in the environment variable 'transports.netty.conf'.
     * If environment variable is not specified, return the default configuration
     *
     * @return TransportsConfiguration defined in the environment
     */
    public TransportsConfiguration getConfiguration() {

        String nettyTransportsConfigFile = System.getProperty(NETTY_TRANSPORT_CONF,
                                                              "conf" + File.separator + "transports" + File.separator +
                                                              "netty-transports.yml");
        return getConfiguration(nettyTransportsConfigFile);
    }

    /**
     * Get the {@code TransportsConfiguration} represented by a particular configuration file.
     *
     * @param configFileLocation configuration file location
     * @return TransportsConfiguration represented by a particular configuration file
     */
    public TransportsConfiguration getConfiguration(String configFileLocation) {
        TransportsConfiguration transportsConfiguration;

        File file = new File(configFileLocation);
        if (file.exists()) {
            try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml(new CustomClassLoaderConstructor
                        (TransportsConfiguration.class, TransportsConfiguration.class.getClassLoader()));
                yaml.setBeanAccess(BeanAccess.FIELD);
                transportsConfiguration = yaml.loadAs(in, TransportsConfiguration.class);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Error while loading " + configFileLocation + " configuration file", e);
            }
        } else { // return a default config
            LOG.warn("Netty transport configuration file not found in: {} ,hence using default configuration",
                     configFileLocation);
            transportsConfiguration = new TransportsConfiguration();
        }

        return transportsConfiguration;
    }
}
