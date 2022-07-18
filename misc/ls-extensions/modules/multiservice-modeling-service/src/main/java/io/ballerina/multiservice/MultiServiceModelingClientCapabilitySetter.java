package io.ballerina.multiservice;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter")
public class MultiServiceModelingClientCapabilitySetter extends BallerinaClientCapabilitySetter<MultiServiceModelingClientCapabilities> {
    @Override
    public String getCapabilityName() {
        return MultiServiceModelingConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<MultiServiceModelingClientCapabilities> getCapability() {
        return MultiServiceModelingClientCapabilities.class;
    }
}
