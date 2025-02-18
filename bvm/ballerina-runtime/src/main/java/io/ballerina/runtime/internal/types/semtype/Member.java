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

package io.ballerina.runtime.internal.types.semtype;

import io.ballerina.runtime.api.types.semtype.Core;
import io.ballerina.runtime.api.types.semtype.SemType;

import static io.ballerina.runtime.api.types.semtype.Builder.getStringConst;

/**
 * Represents a member of an object type.
 *
 * @param name       the name of the member. For methods, this is the method name, and for fields, this is the field
 *                   name.
 * @param valueTy    the type of the member
 * @param kind       the kind of the member (either {@link Kind#Field} or {@link Kind#Method})
 * @param visibility the visibility of the member (either {@link Visibility#Public} or {@link Visibility#Private})
 * @param immutable  whether the member is immutable
 * @since 2201.12.0
 */
public record Member(String name, SemType valueTy, Kind kind, Visibility visibility, boolean immutable) {

    public enum Kind {
        Field,
        Method;

        private static final MappingDefinition.Field FIELD =
                new MappingDefinition.Field("kind", getStringConst("field"), true, false);
        private static final MappingDefinition.Field METHOD =
                new MappingDefinition.Field("kind", getStringConst("method"), true, false);

        public MappingDefinition.Field field() {
            return switch (this) {
                case Field -> FIELD;
                case Method -> METHOD;
            };
        }
    }

    public enum Visibility {
        Public,
        Private;

        private static final SemType PUBLIC_TAG = getStringConst("public");
        private static final MappingDefinition.Field PUBLIC =
                new MappingDefinition.Field("visibility", PUBLIC_TAG, true, false);
        private static final SemType PRIVATE_TAG = getStringConst("private");
        private static final MappingDefinition.Field PRIVATE =
                new MappingDefinition.Field("visibility", PRIVATE_TAG, true, false);
        static final MappingDefinition.Field ALL =
                new MappingDefinition.Field("visibility", Core.union(PRIVATE_TAG, PUBLIC_TAG), true, false);

        public MappingDefinition.Field field() {
            return switch (this) {
                case Public -> PUBLIC;
                case Private -> PRIVATE;
            };
        }
    }
}
