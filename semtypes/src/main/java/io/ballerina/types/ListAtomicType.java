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
package io.ballerina.types;

import java.util.Objects;

/**
 * ListAtomicType node.
 *
 * @since 2201.12.0
 */
public final class ListAtomicType implements AtomicType {

    private final FixedLengthArray members;
    private final CellSemType rest;
    public int temperature = 0;

    public ListAtomicType(FixedLengthArray members, CellSemType rest) {
        assert members != null;
        assert rest != null;
        this.members = members;
        this.rest = rest;
    }

    public static ListAtomicType from(FixedLengthArray members, CellSemType rest) {
        return new ListAtomicType(members, rest);
    }

    @Override
    public Atom.Kind atomKind() {
        return Atom.Kind.LIST_ATOM;
    }

    public FixedLengthArray members() {
        return members;
    }

    public CellSemType rest() {
        return rest;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ListAtomicType) obj;
        return Objects.equals(this.members, that.members) &&
                Objects.equals(this.rest, that.rest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(members, rest);
    }

    @Override
    public String toString() {
        return "ListAtomicType[" +
                "members=" + members + ", " +
                "rest=" + rest + ']';
    }

}
