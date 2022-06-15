package io.ballerina.generators;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the performance analyzer service.
 */
public class MetadataToRecordServerCapabilities extends BallerinaServerCapability {
    private boolean getResults;
    private boolean getRemoteFunctionCalls;
    public MetadataToRecordServerCapabilities() {
        super(MetadataToRecordConstants.CAPABILITY_NAME);
    }

    public boolean isGetResults() {
        return getResults;
    }

    public void setGetResults(boolean getResults) {
        this.getResults = getResults;
    }

    public boolean isGetRemoteFunctionCalls() {
        return getRemoteFunctionCalls;
    }

    public void setGetRemoteFunctionCalls(boolean getRemoteFunctionCalls) {
        this.getRemoteFunctionCalls = getRemoteFunctionCalls;
    }
}
