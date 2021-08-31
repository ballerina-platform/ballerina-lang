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
package io.ballerina.semtype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Code common to implementation of multiple basic types.
 *
 * @since 2.0.0
 */
public class Common {

    public static boolean typeListIsReadOnly(List<SemType> list) {
        for (SemType t : list) {
            if (!Core.isReadOnly(t)) {
                return false;
            }
        }
        return true;
    }

    public static List<SemType> readOnlyTypeList(List<SemType> mt) {
        List<SemType> types = new ArrayList<>();
        for (SemType s : mt) {
            SemType t;
            if (Core.isReadOnly(s)) {
                t = s;
            } else {
                t = Core.intersect(s, PredefinedType.READONLY);
            }
            types.add(t);
        }
        return Collections.unmodifiableList(types);
    }

    /* [from nballerina] The goal of this is to ensure that mappingFormulaIsEmpty does
    not get an empty posList, because it will interpret that
    as `map<any|error>` rather than `map<readonly>`.
    Similarly, for listFormulaIsEmpty.
    We want to share BDDs between the RW and RO case so we cannot change how the BDD is interpreted.
    Instead, we transform the BDD to avoid cases that would give the wrong answer.
    Atom index 0 is LIST_SUBTYPE_RO and MAPPING_SUBTYPE_RO */
    public static Bdd bddFixReadOnly(Bdd b) {
        throw new AssertionError("Not Implemented");
    }

    public static boolean bddPosMaybeEmpty(Bdd b) {
        throw new AssertionError("Not Implemented");
    }

    public static Conjunction andIfPositive(Atom atom, Conjunction next) {
        throw new AssertionError("Not Implemented");
    }

    public static SubtypeData bddSubtypeUnion(SubtypeData t1, SubtypeData t2) {
        throw new AssertionError("Not Implemented");
    }

    public static SubtypeData bddSubtypeIntersect(SubtypeData t1, SubtypeData t2) {
        throw new AssertionError("Not Implemented");
    }

    public static SubtypeData bddSubtypeDiff(SubtypeData t1, SubtypeData t2) {
        throw new AssertionError("Not Implemented");
    }

    public static SubtypeData bddSubtypeComplement(SubtypeData t1, SubtypeData t2) {
        throw new AssertionError("Not Implemented");
    }
}
