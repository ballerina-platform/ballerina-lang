/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org).
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

package io.ballerina.runtime.transactions;

/**
 * {@code RecoveryState} Defines the recovery states of a transaction.
 *
 * @since 2201.9.0
 */
public enum RecoveryState {
    // prepare record
    PREPARING("PREPARING"),
    // decision records
    COMMITTING("COMMITTING"),
    ABORTING("ABORTING"),
    // outcome records
    COMMITTED("COMMITTED"),
    ABORTED("ABORTED"),
    MIXED("MIXED"),
    HAZARD("HAZARD"),
    // done record
    TERMINATED("TERMINATED");

    private final String state;

    RecoveryState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    /**
     * Get the recovery state for the given state.
     *
     * @param state the state as a string
     * @return the recovery state
     */
    public static RecoveryState getRecoveryState(String state) {
        for (RecoveryState recoveryState : RecoveryState.values()) {
            if (recoveryState.getState().equals(state)) {
                return recoveryState;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return state;
    }
}
