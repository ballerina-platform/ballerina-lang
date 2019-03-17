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

/**
 * This class holds common constants used scenario tests and framework.
 */
public class ScenarioConstants {

    /**
     * This regular expression matches numbers with exponents.
     */
    public static final String REGEX_EXPONENT = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";

    public static final String SOURCE_FILES = "sources";

    public static final String K8S_CLUSTER_URL = "ClusterUrl";
    public static final String LB_INGRESS_HOST = "LB_INGRESS_HOST";
    public static final String INFRA_B7A_STACK_NAME = "B7AStackName";

    public static final String TEST_RUN_UUID = "invocation.uuid";
}
