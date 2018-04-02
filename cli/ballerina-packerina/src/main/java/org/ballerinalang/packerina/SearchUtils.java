/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.packerina;

import org.ballerinalang.spi.EmbeddedExecutor;
import org.ballerinalang.util.EmbeddedExecutorProvider;

/**
 * This class provides util methods when searching for Ballerina packages in the central.
 *
 * @since 0.95.2
 */
public class SearchUtils {
    private static final String BALLERINA_STAGING_URL = "https://staging.central.ballerina.io:9090/";

    /**
     * Search for packages in central.
     *
     * @param argument arguments passed
     */
    public static void searchInCentral(String argument) {
        String query = "?keyword=" + argument;
        EmbeddedExecutor executor = EmbeddedExecutorProvider.getInstance().getExecutor();
        executor.execute("packaging.search/ballerina.search.balx", BALLERINA_STAGING_URL, query);
    }
}
