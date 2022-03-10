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

import io.ballerina.types.subtypedata.BddAllOrNothing;
import io.ballerina.types.subtypedata.BddNode;
import io.ballerina.types.typeops.BddCommonOps;

import java.util.ArrayList;
import java.util.List;

/**
 *  Represents path from root to leaf (ending with true).
 *  bdd gets the Bdd for this path
 *
 * @since 3.0.0
 */
public class BddPath {
    Bdd bdd;
    List<Atom> pos;
    List<Atom> neg;

    private BddPath(BddPath bddPath) {
        this.bdd = bddPath.bdd;
        this.pos = new ArrayList<>(bddPath.pos); // pos: path.pos.clone()
        this.neg = new ArrayList<>(bddPath.neg); // pos: path.pos.clone()
    }

    public BddPath() {
        this.bdd = BddAllOrNothing.bddAll();
        this.pos = new ArrayList<>();
        this.neg = new ArrayList<>();
    }

    public static void bddPaths(Bdd b, List<BddPath> paths, BddPath accum) {
        if (b instanceof BddAllOrNothing) {
            if (((BddAllOrNothing) b).isAll()) {
                paths.add(accum);
            }
        } else {
            BddPath left = bddPathClone(accum);
            BddPath right = bddPathClone(accum);
            BddNode bn = (BddNode) b;
            left.pos.add(bn.atom);
            left.bdd = BddCommonOps.bddIntersect(left.bdd, BddCommonOps.bddAtom(bn.atom));
            bddPaths(bn.left, paths, left);
            bddPaths(bn.middle, paths, accum);
            right.neg.add(bn.atom);
            right.bdd = BddCommonOps.bddDiff(right.bdd, BddCommonOps.bddAtom(bn.atom));
            bddPaths(bn.right, paths, right);
        }
    }

    private static BddPath bddPathClone(BddPath path) {
        return new BddPath(path);
    }

    public static BddPath from() {
        return new BddPath();
    }

}
