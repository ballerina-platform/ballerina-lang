package io.ballerina.multiservice;

import org.ballerinalang.langserver.commons.registration.BallerinaClientCapability;

/**
 * Client capabilities for the solution architecture modeling service.
 */
public class MultiServiceModelingClientCapabilities extends BallerinaClientCapability {
    private boolean getMultiServiceModel;

    public MultiServiceModelingClientCapabilities() {
        super(MultiServiceModelingConstants.CAPABILITY_NAME);
    }

    public boolean isGetMultiServiceModel() {
        return getMultiServiceModel;
    }

    public void setGetMultiServiceModel(boolean getMultiServiceModel) {
        this.getMultiServiceModel = getMultiServiceModel;
    }
}
