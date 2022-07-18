package io.ballerina.multiservice;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class MultiServiceModelingServerCapabilitySetter extends BallerinaServerCapabilitySetter<MultiServiceModelingServerCapabilities> {

    @Override
    public Optional<MultiServiceModelingServerCapabilities> build() {
        MultiServiceModelingServerCapabilities capabilities = new MultiServiceModelingServerCapabilities();
        capabilities.setGetMultiServiceModel(true);
        return Optional.of(capabilities);
    }

    @Override
    public String getCapabilityName() {
        return MultiServiceModelingConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<MultiServiceModelingServerCapabilities> getCapability() {
        return MultiServiceModelingServerCapabilities.class;
    }
}
