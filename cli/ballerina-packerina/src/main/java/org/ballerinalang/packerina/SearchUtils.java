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

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.util.ExecutorUtils;

import java.net.URI;

/**
 * This class provides util methods when searching for Ballerina packages in the central.
 *
 * @since 0.95.2
 */
public class SearchUtils {
    private static final String BALLERINA_STAGING_URL = "https://staging.central.ballerina.io:9090/";

    /**
     * Search for packages in central.
     * @param searchType query type searched for i.e. from text, org-name or package
     * @param argument arguments passed
     */
    public static void searchInCentral(String searchType, String argument) {
        URI balxPath = URI.create(String.valueOf(SearchUtils.class.getClassLoader().getResource
                ("ballerina.search.balx")));
        String url = "";
        switch (searchType) {
            case "all":
                break;
            case "org":
                url = "?org=" + argument;
                break;
            case "package":
                url = "?package=" + argument;
                break;
            case "text":
                url = "?query=" + argument;
                break;
            default:
                throw new BLangCompilerException("invalid search query");
        }
        ExecutorUtils.execute(balxPath, BALLERINA_STAGING_URL, url);
    }
}
