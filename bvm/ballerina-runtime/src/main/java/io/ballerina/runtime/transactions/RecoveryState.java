package io.ballerina.runtime.transactions;

    public enum RecoveryState {
        // start record
        STARTING("STARTING"),
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
        IN_DOUBT("IN_DOUBT"),
        // done record
        TERMINATED("TERMINATED");

        // TODO: add heuristic states

        private final String state;

        RecoveryState(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }

        public static RecoveryState getRecoveryStatus(String state) {
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

