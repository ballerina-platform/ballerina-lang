/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.persistence.serializable.serializer.providers.instance;

import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.persistence.serializable.SerializableContext;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.serializer.TypeInstanceProvider;
import org.ballerinalang.util.codegen.ProgramFile;

/**
 * Provide object instance for serializing {@lin SerializableContext}.
 */
public class SerializableContextInstanceProvider implements TypeInstanceProvider {
    @Override
    public String getTypeName() {
        return SerializableContext.class.getName();
    }

    @Override
    public Object newInstance() {
        WorkerExecutionContext ctx = new WorkerExecutionContext(new ProgramFile());
        return new SerializableContext(null, ctx, new SerializableState(ctx));
    }

    @Override
    public Class getTypeClass() {
        return SerializableContext.class;
    }
}
