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

/**
 * Constants used in ballerina configuration.
 */
public interface ConfigConstants {

    String DEFAULT_CONF_FILE_NAME = "deployment.yaml";
    String DEFAULT_CONF_DIR_BRE = "bre";
    String DEFAULT_CONF_DIR_CONF = "conf";

    /* System properties.  */
    String SYS_PROP_BALLERINA_CONF = "ballerina.conf";
    String SYS_PROP_BALLERINA_HOME = "ballerina.home";
}
