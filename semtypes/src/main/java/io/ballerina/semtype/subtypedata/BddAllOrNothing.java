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
package io.ballerina.semtype.subtypedata;

import io.ballerina.semtype.Bdd;

/**
 * Represent boolean subtype of Bdd type.
 *
 * @since 2.0.0
 */
public class BddAllOrNothing implements Bdd {
    private final boolean isAll;

    private static final BddAllOrNothing all = new BddAllOrNothing(true);
    private static final BddAllOrNothing nothing = new BddAllOrNothing(false);

    private BddAllOrNothing(boolean isAll) {
        this.isAll = isAll;
    }

    public static BddAllOrNothing bddAll() {
        return all;
    }

    public static BddAllOrNothing bddNothing() {
        return nothing;
    }

    public boolean isAll() {
        return this.isAll;
    }

    public BddAllOrNothing complement() {
        if (isAll) {
            return nothing;
        }
        return all;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof BddAllOrNothing) {
            return this.isAll == ((BddAllOrNothing) obj).isAll;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 0xa11084 + (isAll ? 1 : 0);
    }
}
