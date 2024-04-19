/*
 *  Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.types;

import static io.ballerina.types.PredefinedType.BDD_REC_ATOM_READONLY;

/**
 * Represent a recursive type atom.
 *
 * @since 2201.8.0
 */
public class RecAtom implements Atom {
    public final int index;
    private TargetKind targetKind = null;
    public static final RecAtom ZERO = new RecAtom(BDD_REC_ATOM_READONLY);

    private RecAtom(int index) {
        this.index = index;
    }

    public static RecAtom createRecAtom(int index) {
        if (index == BDD_REC_ATOM_READONLY) {
            return ZERO;
        }
        return new RecAtom(index);
    }

    public TargetKind getTargetKind() {
        if (targetKind == null) {
            throw new IllegalStateException("Target kind is not set for the recursive type atom");
        }
        return targetKind;
    }

    public void setTargetKind(TargetKind targetKind) {
        this.targetKind = targetKind;
    }

    @Override
    public int index() {
        return index;
    }

    public enum TargetKind {
        LIST_ATOM,
        FUNCTION_ATOM,
        MAPPING_ATOM
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecAtom recAtom) {
            return recAtom.index == this.index;
        }
        return false;
    }

    @Override
    public String toString() {
        return "RecAtom{ index=" + index + ", targetKind=" + targetKind + '}';
    }
}
