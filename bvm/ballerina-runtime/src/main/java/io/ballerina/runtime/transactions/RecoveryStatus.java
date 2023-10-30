package io.ballerina.runtime.transactions;
    public enum RecoveryStatus {
        PREPARING("PREPARING"),
        COMMITTING("COMMITTING"),
        ABORTING("ABORTING"),
        PREPARED("PREPARED"),
        IN_DOUBT("IN-DOUBT"),
        COMMITTED("COMMITTED"),
        ABORTED("ABORTED"),
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

