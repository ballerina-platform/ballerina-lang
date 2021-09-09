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
import io.ballerina.types.typeops.BddCommonOps;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests Bdd of Semtypes.
 *
 */
public class SemTypeBddTest {

    @Test
    public void bddTest() {
        Bdd b1 = BddCommonOps.bddAtom(RecAtom.createRecAtom(1));
        Bdd b2 = BddCommonOps.bddAtom(RecAtom.createRecAtom(2));
        Bdd b1and2 = BddCommonOps.bddIntersect(b1, b2);
        Bdd r = BddCommonOps.bddDiff(b1and2, b1);
        Assert.assertFalse(((BddAllOrNothing) r).isAll());
    }
}
