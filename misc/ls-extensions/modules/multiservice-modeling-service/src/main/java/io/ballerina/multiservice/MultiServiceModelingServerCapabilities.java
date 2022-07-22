package io.ballerina.multiservice;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

/**
 * Server capabilities for the solution architecture modeling service.
 */
public class MultiServiceModelingServerCapabilities extends BallerinaServerCapability {
    private boolean getMultiServiceModel;
    public MultiServiceModelingServerCapabilities() {
        super(MultiServiceModelingConstants.CAPABILITY_NAME);
    }
    public boolean isGetMultiServiceModel() {
        return getMultiServiceModel;
    }

    public void setGetMultiServiceModel(boolean getMultiServiceModel) {
        this.getMultiServiceModel = getMultiServiceModel;
    }
}
