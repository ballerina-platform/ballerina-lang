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

    void signal();

    void setIntReturn(long value);

    void setFloatReturn(double value);

    void setStringReturn(String value);

    void setBooleanReturn(int value);

    void setByteReturn(int value);

    void setRefReturn(BRefType<?> value);

    void setError(BError error);

    long getIntRetVal();

    double getFloatRetVal();

    String getStringRetVal();

    int getBooleanRetVal();

    int getByteRetVal();

    BRefType<?> getRefRetVal();

    BError getErrorVal();
}
