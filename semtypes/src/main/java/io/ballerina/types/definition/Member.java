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

import io.ballerina.types.SemType;

import static io.ballerina.types.SemTypes.stringConst;
import static io.ballerina.types.SemTypes.union;

/**
 * Represent a member of an object type definition.
 *
 * @param name       member name
 * @param valueTy    member type
 * @param kind       is member a field or a method
 * @param visibility is member private or public
 * @param immutable  is member readonly. If this is set valueTy must be a subtype of readonly
 * @since 2201.12.0
 */
public record Member(String name, SemType valueTy, Kind kind, Visibility visibility, boolean immutable) {

    public Member {
        assert name != null && valueTy != null && kind != null && visibility != null;
    }

    // Various "tag" values associated with a member. Each of these tag values must be convertible to a Field in Map
    // type for the member
    @FunctionalInterface
    interface MemberTag {

        Field field();
    }

    public enum Kind implements MemberTag {
        Field,
        Method;

        // In nBallerina these are stings since they fit small strings. Maybe consider more efficient representation
        // for java
        private static final Field FIELD = new Field("kind", stringConst("field"), true, false);
        private static final Field METHOD = new Field("kind", stringConst("method"), true, false);

        public Field field() {
            return switch (this) {
                case Field -> FIELD;
                case Method -> METHOD;
            };
        }
    }

    public enum Visibility implements MemberTag {
        Public,
        Private;

        private static final SemType PUBLIC_TAG = stringConst("public");
        private static final Field PUBLIC = new Field("visibility", PUBLIC_TAG, true, false);
        private static final SemType PRIVATE_TAG = stringConst("private");
        private static final Field PRIVATE = new Field("visibility", PRIVATE_TAG, true, false);
        static final Field ALL = new Field("visibility", union(PRIVATE_TAG, PUBLIC_TAG), true, false);

        public Field field() {
            return switch (this) {
                case Public -> PUBLIC;
                case Private -> PRIVATE;
            };
        }
    }
}
