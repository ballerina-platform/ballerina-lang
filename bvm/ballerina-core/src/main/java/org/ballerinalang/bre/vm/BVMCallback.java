/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.vm;

import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;

/**
 * This interface represents a callback to report back a VM related callback events.
 *
 * @since 0.985.0
 */
public interface BVMCallback {

    /**
     * Method to signal the callback once done.
     */
    void signal();

    /**
     * Method to set int return value.
     *
     * @param value to be returned
     */
    void setIntReturn(long value);

    /**
     * Method to set float return value.
     *
     * @param value to be returned
     */
    void setFloatReturn(double value);

    /**
     * Method to set string return value.
     *
     * @param value to be returned
     */
    void setStringReturn(String value);

    /**
     * Method to set boolean return value.
     *
     * @param value to be returned
     */
    void setBooleanReturn(int value);

    /**
     * Method to set byte return value.
     *
     * @param value to be returned
     */
    void setByteReturn(int value);

    /**
     * Method to set ref return value.
     *
     * @param value to be returned
     */
    void setRefReturn(BRefType<?> value);

    /**
     * Method to set error return value.
     *
     * @param error to be returned
     */
    void setError(BError error);

    /**
     * Method to get int return value.
     *
     * @return value
     */
    long getIntRetVal();

    /**
     * Method to get float return value.
     *
     * @return value
     */
    double getFloatRetVal();

    /**
     * Method to get string return value.
     *
     * @return value
     */
    String getStringRetVal();

    /**
     * Method to get boolean return value.
     *
     * @return value
     */
    int getBooleanRetVal();

    /**
     * Method to get byte return value.
     *
     * @return value
     */
    int getByteRetVal();

    /**
     * Method to get ref return value.
     *
     * @return value
     */
    BRefType<?> getRefRetVal();

    /**
     * Method to get error return value.
     *
     * @return value
     */
    BError getErrorVal();
}
