/*
 *  Copyright (c) 2015, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.carbon.transport.http.netty.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Parses &amp; creates the object model for the Netty transport yaml configuration file.
 */
public class YAMLTransportConfigurationBuilder {

    private static final String NETTY_TRANSPORT_CONF = "transports.netty.conf";

    private static final Logger log = LoggerFactory.getLogger(YAMLTransportConfigurationBuilder.class);

    /**
     * Parses &amp; creates the object model for the Netty transport yaml configuration file.
     *
     * @return TransportsConfiguration
     */
    public static TransportsConfiguration build() {
        String nettyTransportsConfigFile = System.getProperty(NETTY_TRANSPORT_CONF,
                "conf" + File.separator + "transports" + File.separator + "netty-transports.yml");
        return build(nettyTransportsConfigFile);
    }

    public static TransportsConfiguration build(String nettyTransportsConfigFile) {
        TransportsConfiguration transportsConfiguration;
        File file = new File(nettyTransportsConfigFile);
        if (file.exists()) {
            try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                transportsConfiguration = yaml.loadAs(in, TransportsConfiguration.class);
            } catch (IOException e) {
                String msg = "Error while loading " + nettyTransportsConfigFile + " configuration file";
                throw new RuntimeException(msg, e);
            }
        } else { // return a default config
            log.warn("Netty transport configuration file not found in: " + nettyTransportsConfigFile);
            transportsConfiguration = TransportsConfiguration.getDefault();
        }

        return transportsConfiguration;
    }
}
