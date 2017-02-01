/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.model.values;

import org.wso2.ballerina.core.exception.BallerinaException;

/**
 * The {@code BNull} represents a null value in Ballerina.
 *
 * @since 0.8.0
 */
public final class BNull extends BValueType {

    public BNull() {
    }

    @Override
    public String stringValue() {
        return "null";
    }

    @Override
    public int intValue() {
        throw new BallerinaException("null cannot be converted to integer");
    }

    @Override
    public long longValue() {
        throw new BallerinaException("null cannot be converted to long");
    }

    @Override
    public float floatValue() {
        throw new BallerinaException("null cannot be converted to float");
    }

    @Override
    public double doubleValue() {
        throw new BallerinaException("null cannot be converted to double");
    }

    @Override
    public boolean booleanValue() {
        throw new BallerinaException("null cannot be converted to boolean");
    }
}
