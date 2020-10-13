/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.net.uri.parser;


import io.ballerina.runtime.util.exceptions.BallerinaException;

/**
 * This class is use to set and return data in the template tree.
 * @param <DataType> Type of data which should be set and returned.
 */
public class DataReturnAgent<DataType> {

    private DataType data;
    private BallerinaException ballerinaException;

    /**
     * Set data.
     * @param data data which should get returned.
     */
    public void setData(DataType data) {
        this.data = data;
    }

    /**
     * Get data.
     * @return data stored in the Agent.
     */
    public DataType getData() {
        return data;
    }

    /**
     * Set Error.
     * @param ballerinaException the error to be set.
     */
    public void setError(BallerinaException ballerinaException) {
        this.ballerinaException = ballerinaException;
    }

    /**
     * Get Error.
     * @return the Throwable which caused the error.
     */
    public BallerinaException getError() {
        return ballerinaException;
    }
}
