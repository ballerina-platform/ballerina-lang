/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.ballerinalang.runtime.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility methods for loading Ballerina Configuration.
 *
 * @since 0.89
 */
public class BLangConfigurationManager {

    private static final Logger breLog = LoggerFactory.getLogger(BLangConfigurationManager.class);

    private static BLangConfigurationManager instance = new BLangConfigurationManager();
    private BallerinaConfiguration configuration;
    private ConfigProvider configProvider;

    private BLangConfigurationManager() {
    }

    public static BLangConfigurationManager getInstance() {
        return instance;
    }

    /**
     * Returns BallerinaConfiguration instance.
     *
     * @return BallerinaConfiguration instance.
     */
    public BallerinaConfiguration getConfiguration() {
        if (configuration != null) {
            return configuration;
        }
        try {
            configProvider = getBallerinaConfigProvider();
            configuration = configProvider.getConfigurationObject(BallerinaConfiguration.class);
        } catch (ConfigurationException e) {
            breLog.warn("unable to load ballerina configuration, using default configuration.", e);
            configuration = new BallerinaConfiguration();
        }
        return configuration;
    }

    /**
     * Returns Config Provider for ballerina runtime.
     *
     * @return ConfigProvider instance.
     * @throws ConfigurationException when error occurred while loading configuration.
     */
    public ConfigProvider getBallerinaConfigProvider() throws ConfigurationException {
        if (configProvider != null) {
            return configProvider;
        }
        Path balConfPath = null;
        String balConfString = System.getProperty(ConfigConstants.SYS_PROP_BALLERINA_CONF);
        if (balConfString != null) {
            balConfPath = Paths.get(new File(balConfString).toURI());
        }
        if (balConfPath == null) {
            balConfPath = Paths.get(System.getProperty(ConfigConstants.SYS_PROP_BALLERINA_HOME),
                    ConfigConstants.DEFAULT_CONF_DIR_BRE, ConfigConstants.DEFAULT_CONF_DIR_CONF,
                    ConfigConstants.DEFAULT_CONF_FILE_NAME);
        }
        configProvider = ConfigProviderFactory.getConfigProvider(balConfPath);
        return configProvider;
    }

    public void clear() {
        configuration = null;
        configProvider = null;
    }
}
