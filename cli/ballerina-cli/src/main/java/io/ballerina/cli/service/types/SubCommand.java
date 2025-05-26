/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.cli.service.types;

import java.util.List;

/**
 * Represents a sub-command in the Ballerina CLI service.
 *
 * @param command The main command to which this sub-command belongs.
 * @param subCommand The name of the sub-command.
 * @param arguments A list of arguments associated with the sub-command.
 * @param resultType The expected result type of executing this sub-command.
 *
 * @since 2201.13.0
 */
public record SubCommand(String command, String subCommand, List<Argument> arguments, ResultType resultType) {
}
