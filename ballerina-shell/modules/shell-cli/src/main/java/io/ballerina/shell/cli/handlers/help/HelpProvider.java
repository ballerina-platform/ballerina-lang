/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package io.ballerina.shell.cli.handlers.help;

/**
 * Help provider interface for the providing classes.
 * Provides help for a given topic.
 *
 * @since 2.0.0
 */
public interface HelpProvider {
    /**
     * Get the content for a given topic.
     *
     * @param args   All the arguments given by requester.
     * @param output Builder to append output.
     * @throws HelpProviderException If fetching help failed.
     */
    void getTopic(String[] args, StringBuilder output) throws HelpProviderException;
}
