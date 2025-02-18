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

import java.util.Objects;
import java.util.Optional;

/**
 * Represents the memoization emptiness of a BDD used in Context.
 *
 * @since 2201.12.0
 */
public final class BddMemo {

    public Status isEmpty;

    public BddMemo() {
        this.isEmpty = Status.NULL;
    }

    public enum Status {
        // We know where this BDD is empty or not
        TRUE,
        FALSE,
        // There is some recursive part in this type
        LOOP,
        CYCLIC,
        // We are in the process of determining if this BDD is empty or not
        PROVISIONAL,
        // We just initialized the node, treated to be same as not having a memo
        NULL
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BddMemo bddMemo)) {
            return false;
        }
        return isEmpty == bddMemo.isEmpty;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isEmpty);
    }

    public Optional<Boolean> isEmpty() {
        return switch (isEmpty) {
            // Cyclic types are empty because we define types inductively
            case TRUE, CYCLIC -> Optional.of(true);
            case FALSE -> Optional.of(false);
            case LOOP, PROVISIONAL -> {
                // If it was provisional we came from a back edge
                // Again we treat the loop part as empty
                isEmpty = Status.LOOP;
                yield Optional.of(true);
            }
            case NULL -> Optional.empty();
        };
    }
}
