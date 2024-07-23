/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org).
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
 *
 */

package io.ballerina.runtime.api.types.semtype;

/**
 * Represent a recursive type atom.
 *
 * @since 2201.10.0
 */
public final class RecAtom implements Atom {

    private final int index;
    private static final int BDD_REC_ATOM_READONLY = 0;
    private static final RecAtom ZERO = new RecAtom(BDD_REC_ATOM_READONLY);

    private RecAtom(int index) {
        this.index = index;
    }

    public static RecAtom createRecAtom(int index) {
        if (index == BDD_REC_ATOM_READONLY) {
            return ZERO;
        }
        return new RecAtom(index);
    }

    public static RecAtom createDistinctRecAtom(int index) {
        return new RecAtom(index);
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof RecAtom recAtom) {
            return recAtom.index == this.index;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
