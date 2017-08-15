/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.core.table.holder;

/**
 * PrimaryKeyReferenceHolder for indexed Event Table
 */
public class PrimaryKeyReferenceHolder {
    private String primaryKeyAttribute = null;
    private int primaryKeyPosition = -1;

    public PrimaryKeyReferenceHolder(String primaryKeyAttribute, int primaryKeyPosition) {

        this.primaryKeyAttribute = primaryKeyAttribute;
        this.primaryKeyPosition = primaryKeyPosition;
    }

    public String getPrimaryKeyAttribute() {
        return primaryKeyAttribute;
    }

    public int getPrimaryKeyPosition() {
        return primaryKeyPosition;
    }
}
