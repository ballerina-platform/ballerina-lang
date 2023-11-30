package io.ballerina.runtime.transactions;

    public enum RecoveryStatus {
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

        RecoveryStatus(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }

        public static RecoveryStatus getRecoveryStatus(String state) {
            for (RecoveryStatus recoveryStatus : RecoveryStatus.values()) {
                if (recoveryStatus.getState().equals(state)) {
                    return recoveryStatus;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return state;
        }
    }

