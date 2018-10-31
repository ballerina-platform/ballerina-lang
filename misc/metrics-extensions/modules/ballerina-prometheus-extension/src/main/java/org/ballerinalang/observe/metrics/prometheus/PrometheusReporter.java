/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.ballerinalang.observe.metrics.prometheus;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.launcher.LauncherUtils;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.metrics.spi.MetricReporter;
import org.ballerinalang.util.observability.ObservabilityConstants;
import org.ballerinalang.util.tracer.exception.InvalidConfigurationException;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the reporter extension for the Prometheus.
 *
 * @since 0.980.0
 */
@JavaSPIService("org.ballerinalang.util.metrics.spi.MetricReporter")
public class PrometheusReporter implements MetricReporter {

    private static final PrintStream console = System.out;
    private static final String PROMETHEUS_PACKAGE = "prometheus";
    private static final String REPORTER_BALX_FILE_NAME = "reporter.balx";
    private static final String BALX_LIB_DIRECTORY = "lib";
    private static final String PROMETHEUS_HOST_CONFIG = ObservabilityConstants.CONFIG_TABLE_METRICS
            + "." + PROMETHEUS_PACKAGE + ".host";
    private static final String PROMETHEUS_PORT_CONFIG = ObservabilityConstants.CONFIG_TABLE_METRICS + "."
            + PROMETHEUS_PACKAGE + ".port";
    private static final String DEFAULT_PROMETHEUS_HOST = "0.0.0.0";
    private static final String DEFAULT_PROMETHEUS_PORT = "9797";

    @Override
    public void init() throws InvalidConfigurationException {
        String ballerinaHome = System.getProperty(BLangConstants.BALLERINA_HOME);
        Path reporterSourcePath = Paths.get(ballerinaHome, BALX_LIB_DIRECTORY, BLangConstants.BLANG_EXEC_FILE_EXT,
                PROMETHEUS_PACKAGE);
        Path repoterBalxPath = Paths.get(ballerinaHome, BALX_LIB_DIRECTORY, BLangConstants.BLANG_EXEC_FILE_EXT,
                PROMETHEUS_PACKAGE, REPORTER_BALX_FILE_NAME);
        LauncherUtils.runProgram(reporterSourcePath, repoterBalxPath, loadCurrentConfigs(), null, new String[0],
                                 true, false);
        String hostname = ConfigRegistry.getInstance().
                getConfigOrDefault(PROMETHEUS_HOST_CONFIG, DEFAULT_PROMETHEUS_HOST);
        String port = ConfigRegistry.getInstance().getConfigOrDefault(PROMETHEUS_PORT_CONFIG,
                DEFAULT_PROMETHEUS_PORT);
        console.println("ballerina: started Prometheus HTTP endpoint " + hostname + ":" + port);
    }

    private Map<String, String> loadCurrentConfigs() {
        Map<String, String> config = new HashMap<>();
        Iterator<String> configKeys = ConfigRegistry.getInstance().keySetIterator();
        while (configKeys.hasNext()) {
            String key = configKeys.next();
            config.put(key, ConfigRegistry.getInstance().getAsString(key));
        }
        return config;
    }

    @Override
    public String getName() {
        return PROMETHEUS_PACKAGE;
    }
}
