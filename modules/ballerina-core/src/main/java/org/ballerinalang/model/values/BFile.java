/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.model.values;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

/**
 * {@code BFile} represents a File in Ballerina.
 *
 * @since 0.9.0
 */
public class BFile implements BRefType {

    private String filePath;

    public BFile(String filePath) {
        this.filePath = filePath;
    }
    //TODO Add more properties


    @Override
    public String stringValue() {
        return filePath;
    }

    @Override
    public BType getType() {
        return BTypes.typeFile;
    }

    @Override public BValue copy() {
        return new BFile(filePath);
    }

    @Override
    public Object value() {
        return filePath;
    }
}
