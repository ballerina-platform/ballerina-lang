package io.ballerina.multiservice;

import org.ballerinalang.langserver.commons.registration.BallerinaServerCapability;

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
