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
package io.ballerina.types;

import java.util.ArrayList;

/**
 * ListAtomicType node.
 *
 * @since 3.0.0
 */
public class ListAtomicType implements AtomicType {
    public final FixedLengthArray members;
    public final SemType rest;

    public static final ListAtomicType LIST_SUBTYPE_RO =
            new ListAtomicType(FixedLengthArray.from(new ArrayList<>(), 0), PredefinedType.READONLY);

    private ListAtomicType(FixedLengthArray members, SemType rest) {
        this.members = members;
        this.rest = rest;
    }

    public static ListAtomicType from(FixedLengthArray members, SemType rest) {
        return new ListAtomicType(members, rest);
    }
}
