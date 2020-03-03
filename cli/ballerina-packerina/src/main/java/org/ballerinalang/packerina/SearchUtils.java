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

import org.ballerinalang.cli.module.Search;
import org.ballerinalang.cli.module.exeptions.CommandException;
import org.ballerinalang.toml.model.Proxy;
import org.wso2.ballerinalang.util.RepoUtils;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.PrintStream;

/**
 * This class provides util methods when searching for Ballerina modules in the central.
 *
 * @since 0.95.2
 */
public class SearchUtils {
    private static final PrintStream ERROR_STREAM = System.err;

    /**
     * Search for modules in central.
     *
     * @param query search keyword.
     */
    public static void searchInCentral(String query) {
        Proxy proxy = TomlParserUtils.readSettings().getProxy();
        String urlWithModulePath = RepoUtils.getRemoteRepoURL() + "/modules/" + "?q=" + query;

        try {
            Search.execute(urlWithModulePath, proxy.getHost(), proxy.getPort(), proxy.getUserName(),
                    proxy.getPassword(), RepoUtils.getTerminalWidth());
        } catch (CommandException e) {
            String errorMessage = e.getMessage();
            if (null != errorMessage && !"".equals(errorMessage.trim())) {
                // removing the error stack
                if (errorMessage.contains("\n\tat")) {
                    errorMessage = errorMessage.substring(0, errorMessage.indexOf("\n\tat"));
                }

                ERROR_STREAM.println(errorMessage);
            }
        }
    }
}
