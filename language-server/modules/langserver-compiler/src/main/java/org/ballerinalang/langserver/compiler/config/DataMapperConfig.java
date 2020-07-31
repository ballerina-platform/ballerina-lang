/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.com). All Rights Reserved.
 *
 * This software is the property of WSO2 Inc. and its suppliers, if any.
 * Dissemination of any information or reproduction of any material contained
 * herein is strictly forbidden, unless permitted by WSO2 in accordance with
 * the WSO2 Commercial License available at http://wso2.com/licenses.
 * For specific language governing the permissions and limitations under
 * this license, please see the license as well as any agreement you’ve
 * entered into with WSO2 governing the purchase of this software and any
 */
package org.ballerinalang.langserver.compiler.config;

/**
 * Ballerina Data Mapper code action Configuration.
 */
public class DataMapperConfig {
    private final boolean enabled;
    private final String url;

    DataMapperConfig() {
        this.enabled = false;
        this.url = "";
    }

    /**
     * Returns True if data mapper is enabled, False otherwise.
     *
     * @return True or False
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the URL set for the data mapper back end service.
     *
     * @return string URL
     */
    public String getUrl() {
        return url;
    }
}