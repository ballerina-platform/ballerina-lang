/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil.connector.models.connector.types;

import com.google.gson.annotations.Expose;
import org.ballerinalang.diagramutil.connector.models.connector.Type;

import java.util.Optional;

/**
 * Stream type model.
 */
public class StreamType extends Type {
    @Expose
    public Type leftTypeParam;
    @Expose
    public Type rightTypeParam;

    public StreamType(Optional<Type> leftTypeParam, Optional<Type> rightTypeParam) {
        this.typeName = "stream";
        if (leftTypeParam.isPresent()) {
            this.leftTypeParam = leftTypeParam.get();
        }
        if (rightTypeParam.isPresent()) {
            this.rightTypeParam = rightTypeParam.get();
        }
    }

    public StreamType(Type leftTypeParam, Type rightTypeParam) {
        this.typeName = "stream";
        this.leftTypeParam = leftTypeParam;
        this.rightTypeParam = rightTypeParam;
    }
}
