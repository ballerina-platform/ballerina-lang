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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;

/**
 * Default VM callback implementation.
 *
 * @since 0.985.0
 */
public class StrandCallback implements BVMCallback {

    //Return value holders
    private long longVal;
    private double doubleVal;
    private String stringVal;
    private int intVal;
    private BRefType<?> refVal;
    private BError error;

    protected BType retType; //TODO may be this is wrong, we should take the type in wait expression -check this

    StrandCallback(BType retType) {
        this.retType = retType;
    }

    @Override
    public void signal() {
        //TODO
    }

    @Override
    public void setIntReturn(long value) {
        this.longVal = value;
    }

    @Override
    public void setFloatReturn(double value) {
        this.doubleVal = value;
    }

    @Override
    public void setStringReturn(String value) {
        this.stringVal = value;
    }

    @Override
    public void setBooleanReturn(int value) {
        this.intVal = value;
    }

    @Override
    public void setByteReturn(int value) {
        this.intVal = value;
    }

    @Override
    public void setRefReturn(BRefType<?> value) {
        this.refVal = value;
    }

    @Override
    public void setError(BError error) {
        this.error = error;
    }

    @Override
    public long getIntRetVal() {
        return longVal;
    }

    @Override
    public double getFloatRetVal() {
        return doubleVal;
    }

    @Override
    public String getStringRetVal() {
        return stringVal;
    }

    @Override
    public int getBooleanRetVal() {
        return intVal;
    }

    @Override
    public int getByteRetVal() {
        return intVal;
    }

    @Override
    public BRefType<?> getRefRetVal() {
        return refVal;
    }

    @Override
    public BError getErrorVal() {
        return error;
    }

}
