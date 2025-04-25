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

/**
 * Represent BddMomo type used for memoization.
 *
 * @since 2201.12.0
 */
public class BddMemo {

    protected MemoStatus isEmpty;

    public BddMemo() {
        this.isEmpty = MemoStatus.NULL;
    }

    public void setIsEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty ? MemoStatus.TRUE : MemoStatus.FALSE;
    }

    public boolean isEmpty() {
        return this.isEmpty == MemoStatus.TRUE;
    }
    /**
     * Represent if BddMemo is null or not.
     *
     * @since 3.0.0
     */
    public enum MemoStatus {
        LOOP, TRUE, FALSE, CYCLIC, PROVISIONAL, NULL;
    }
}
