/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.scenario.test.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

/**
 * This is the base class of scenario test classes.
 */
public class ScenarioTestBase {

    private static final String INPUTS_LOCATION = System.getProperty("data.bucket.location");
    private static final String INFRASTRUCTURE_PROPERTIES = "infrastructure.properties";
    private static final String DEPLOYMENT_PROPERTIES = "deployment.properties";
    private static final String JOB_PROPERTIES = "testplan-props.properties";

    public static final Logger LOG = LoggerFactory.getLogger(ScenarioTestBase.class);

    private static final String BALLERINA_VERSION = "BallerinaVersion";
    private static Properties infraProperties;

    private static String serviceURL;
    private static String deploymentStackName;
    private static String runningBallerinaVersion;
    private static String testRunUUID;


    /**
     * Initialize testcase.
     *
     * @throws Exception if the logging in fails
     */
    public void init() throws Exception {
        LOG.info("Started Executing Scenario TestBase ");

        configureUrls();
    }

    /**
     * Validates if the test case is one that should be executed on the running product version.
     * <p>
     * All test methods of the test case will be skipped if it is not compatible with the running version. This is
     * introduced to cater tests introduced for fixes done as patches for released product versions as they may only
     * be valid for the product version for which the fix was done for.
     *
     * @param incompatibleVersions product versions that the test is not compatible with
     */
    protected void skipTestsForIncompatibleProductVersions(String... incompatibleVersions) {
        //running product version can be null if the property is not in deployment properties
        if (null != runningBallerinaVersion && Arrays.asList(incompatibleVersions).contains(runningBallerinaVersion)) {
            String errorMessage =
                    "Skipping test: " + this.getClass().getName() + " due to version mismatch. Running "
                    + "Ballerina version: " + runningBallerinaVersion + ", Non allowed versions: "
                    + Arrays.toString(incompatibleVersions);
            LOG.warn(errorMessage);
            throw new SkipException(errorMessage);
        }
    }

    /**
     * Perform cleanup.
     *
     * @throws Exception if an error occurs while performing clean up task
     */
    public void cleanup() throws Exception {
    }

    /**
     * This is a utility method to load the deployment details.
     * The deployment details are available as key-value pairs in {@link #INFRASTRUCTURE_PROPERTIES},
     * {@link #DEPLOYMENT_PROPERTIES}, and {@link #JOB_PROPERTIES} under the
     * {@link #INPUTS_LOCATION}.
     *
     * This method loads these files into one single properties, and return it.
     *
     * @return properties the deployment properties
     */
    public static Properties getDeploymentProperties() {
        Path infraPropsFile = Paths.get(INPUTS_LOCATION + File.separator + INFRASTRUCTURE_PROPERTIES);
        Path deployPropsFile = Paths.get(INPUTS_LOCATION + File.separator + DEPLOYMENT_PROPERTIES);
        Path jobPropsFile = Paths.get(INPUTS_LOCATION + File.separator + JOB_PROPERTIES);

        Properties props = new Properties();
        loadProperties(infraPropsFile, props);
        loadProperties(deployPropsFile, props);
        loadProperties(jobPropsFile, props);
        return props;
    }

    public String getInfrastructureProperty(String propertyName) {
        return infraProperties.getProperty(propertyName);
    }

    private static void loadProperties(Path propsFile, Properties props) {
        String msg = "Deployment property file not found: ";
        if (!Files.exists(propsFile)) {
            LOG.warn(msg + propsFile);
            return;
        }

        try (InputStream propsIS = Files.newInputStream(propsFile)) {
            props.load(propsIS);
        } catch (IOException ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    public static synchronized void configureUrls() {
        if (null == infraProperties) {
            infraProperties = getDeploymentProperties();
            serviceURL = infraProperties.getProperty(ScenarioConstants.LB_INGRESS_HOST)
                         + (infraProperties.getProperty(ScenarioConstants.LB_INGRESS_HOST).endsWith("/") ? "" : "/");
            runningBallerinaVersion = infraProperties.getProperty(BALLERINA_VERSION);

            deploymentStackName = infraProperties.getProperty(ScenarioConstants.INFRA_B7A_STACK_NAME);
        }
    }

    protected String getServiceURL(String path) {
        return "https://" + serviceURL + (path.startsWith("/") ? "" : "/") + path;
    }

    public static String getServiceURL() {
        return serviceURL;
    }

    public static void setServiceURL(String serviceURL) {
        ScenarioTestBase.serviceURL = serviceURL;
    }

    public static String getDeploymentStackName() {
        return deploymentStackName;
    }

    public static void setDeploymentStackName(String deploymentStackName) {
        ScenarioTestBase.deploymentStackName = deploymentStackName;
    }

    public static String getTestRunUUID() {
        return testRunUUID;
    }

    public static void setTestRunUUID(String testRunUUID) {
        ScenarioTestBase.testRunUUID = testRunUUID;
    }
}

