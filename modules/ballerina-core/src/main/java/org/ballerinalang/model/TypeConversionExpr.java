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
package org.ballerinalang.model;

/**
 * {@code TypeConversionExpr} interface makes an {@link org.ballerinalang.model.expressions.Expression}.
 * which converts ballerina values to another.
 *
 * @since 0.88
 */
public interface TypeConversionExpr {

    /**
     * Set that this expression contains multiple returns.
     * 
     * @param Flag indicating whether this expression contains multiple returns.
     */
    void setMultiReturnAvailable(boolean multiReturnsAvailable);

}
