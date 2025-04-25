/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.types.definition;

import io.ballerina.types.CellAtomicType;
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;

import java.util.List;

import static io.ballerina.types.SemTypes.booleanConst;
import static io.ballerina.types.SemTypes.stringConst;
import static io.ballerina.types.SemTypes.union;
import static io.ballerina.types.subtypedata.CellSubtype.cellContaining;

/**
 * Represent {@code object-type-quals} in the spec.
 *
 * @param isolated         is object isolated
 * @param readonly         represent {@code class readonly}. Note this is used to determining "rest" part of the object
 *                         only {@code Member} types must be correctly set as intersection with readonly where
 *                         applicable even with this set to true
 * @param networkQualifier is object client, service or none
 * @since 2201.12.0
 */
public record ObjectQualifiers(boolean isolated, boolean readonly, NetworkQualifier networkQualifier) {

    private static final ObjectQualifiers DEFAULT = new ObjectQualifiers(false, false, NetworkQualifier.None);

    public static ObjectQualifiers defaultQualifiers() {
        return DEFAULT;
    }

    public static ObjectQualifiers from(boolean isolated, boolean readonly, NetworkQualifier networkQualifier) {
        if (networkQualifier == NetworkQualifier.None && !isolated) {
            return defaultQualifiers();
        }
        return new ObjectQualifiers(isolated, readonly, networkQualifier);
    }

    public enum NetworkQualifier {
        Client,
        Service,
        None;

        private static final SemType CLIENT_TAG = stringConst("client");
        private static final Field CLIENT = new Field("network", CLIENT_TAG, true, false);

        private static final SemType SERVICE_TAG = stringConst("service");
        private static final Field SERVICE = new Field("network", SERVICE_TAG, true, false);

        // Object can't be both client and service, which is enforced by the enum. We are using a union here so that
        // if this is none it matches both
        private static final Field NONE = new Field("network", union(CLIENT_TAG, SERVICE_TAG), true, false);

        private Field field() {
            return switch (this) {
                case Client -> CLIENT;
                case Service -> SERVICE;
                case None -> NONE;
            };
        }
    }

    public CellField field(Env env) {
        MappingDefinition md = new MappingDefinition();
        Field isolatedField =
                new Field("isolated", isolated ? booleanConst(true) : PredefinedType.BOOLEAN, true, false);
        Field networkField = networkQualifier.field();
        SemType ty = md.defineMappingTypeWrapped(env, List.of(isolatedField, networkField), PredefinedType.NEVER,
                CellAtomicType.CellMutability.CELL_MUT_NONE);

        return CellField.from("$qualifiers", cellContaining(env, ty));
    }
}
