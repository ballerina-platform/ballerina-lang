/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package io.ballerina.runtime.api.types.semtype;

import java.util.Objects;
import java.util.Optional;

// TODO: consider moving this to inner as well
public final class BddMemo {

    public Status isEmpty;

    public BddMemo() {
        this.isEmpty = Status.NULL;
    }

    public enum Status {
        TRUE,
        FALSE,
        LOOP,
        CYCLIC,
        PROVISIONAL,
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
            case TRUE, CYCLIC -> Optional.of(true);
            case FALSE -> Optional.of(false);
            case LOOP, PROVISIONAL -> {
                isEmpty = Status.LOOP;
                yield Optional.of(true);
            }
            case NULL -> Optional.empty();
        };
    }
}
