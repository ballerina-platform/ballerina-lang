/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.channels.base.data;

/**
 * Represents a long read though the dataExpr channel.
 */
public class LongResult {
    /**
     * Value of the long which is read.
     */
    private long value;
    /**
     * Number of bytes read.
     */
    private int byteCount;

    public LongResult(long value) {
        this.value = value;
    }

    public LongResult(long value, int byteCount) {
        this.value = value;
        this.byteCount = byteCount;
    }

    public long getValue() {
        return value;
    }

    public int getByteCount() {
        return byteCount;
    }
}
