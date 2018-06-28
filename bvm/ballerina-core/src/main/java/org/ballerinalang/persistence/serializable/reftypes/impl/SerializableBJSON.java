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
package org.ballerinalang.persistence.serializable.reftypes.impl;

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.persistence.serializable.SerializableState;
import org.ballerinalang.persistence.serializable.reftypes.SerializableRefType;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.Base64;

/**
 * Implementation of @{@link SerializableRefType} to serialize and deserialize {@link BJSON} objects.
 *
 * @since 0.976.0
 */
public class SerializableBJSON implements SerializableRefType {

    private String jsonContent;

    public SerializableBJSON(BJSON bjson) {
        String jsonString = bjson.stringValue();
        jsonContent = Base64.getEncoder().encodeToString(jsonString.getBytes());
    }

    @Override
    public BRefType getBRefType(ProgramFile programFile, SerializableState state) {
        if (jsonContent == null) {
            throw new BallerinaException("JSON content is null.");
        }

        String jsonString = new String(Base64.getDecoder().decode(jsonContent));
        if (jsonString.contains("{")) {
            jsonString = jsonString.replaceAll("\"", "'");
        } else {
            jsonString = "'" + jsonString + "'";
        }
        return new BJSON(jsonString);
    }
}
