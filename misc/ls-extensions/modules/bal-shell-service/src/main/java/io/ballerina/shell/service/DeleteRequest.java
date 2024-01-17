/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.shell.service;

/**
 * Request format for delete definition or declaration
 * from BalShell endpoint.
 *
 * @since 2201.1.1
 */
public class DeleteRequest {
    private final String varToDelete;

    public DeleteRequest(String varToDelete) {
        this.varToDelete = varToDelete;
    }

    /**
     * Returns value needs to be deleted.
     *
     * @return value to delete.
     */
    public String getVarToDelete() {
        return varToDelete;
    }
}
