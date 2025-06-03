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

import io.ballerina.tools.text.LSPTextEdit;
import io.ballerina.tools.text.TextEdit;

import java.util.List;
import java.util.Map;

/**
 * Represents the response of a command execution in the Ballerina CLI service.
 *
 * @param status       The status of the command execution.
 * @param resultTypes  A list of result types produced by the command.
 * @param textEdits    A map of text edits to be applied as a result of the command execution.
 * @param errors       A list of errors encountered during command execution.
 *
 * @since 2201.13.0
 */
public record CommandResponse(Status status, List<ResultType> resultTypes, Map<String, List<LSPTextEdit>> textEdits,
                              List<Error> errors) {

    /**
     * Builder class for constructing a CommandResponse.
     */
    public static class Builder {
        private Status status;
        private List<ResultType> resultTypes;
        private Map<String, List<LSPTextEdit>> textEdits;
        private List<Error> errors;

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public Builder resultTypes(List<ResultType> resultTypes) {
            this.resultTypes = resultTypes;
            return this;
        }

        public Builder textEdits(Map<String, List<LSPTextEdit>> textEdits) {
            this.textEdits = textEdits;
            return this;
        }

        public Builder errors(List<Error> errors) {
            this.errors = errors;
            return this;
        }

        public CommandResponse build() {
            return new CommandResponse(status, resultTypes, textEdits, errors);
        }
    }
}
