/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm.persistency.reftypes.impl;

import org.ballerinalang.bre.bvm.persistency.SerializableState;
import org.ballerinalang.bre.bvm.persistency.reftypes.RefTypeHandler;
import org.ballerinalang.bre.bvm.persistency.reftypes.SerializableBStruct;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.ProgramFile;

public class BStructHandler implements RefTypeHandler<SerializableBStruct, BStruct> {
    @Override
    public SerializableBStruct serialize(BStruct bStruct, SerializableState state) {
//        SerializableBStruct serializableBStruct = new SerializableBStruct(bStruct, state);
//        return serializableBStruct;
        return null;
    }

    @Override
    public BStruct deserialize(SerializableBStruct serializableBStruct, SerializableState state, ProgramFile programFile) {
//        BStruct bStruct = serializableBStruct.getBSturct(programFile, state);
//        return bStruct;
        return null;
    }
}
