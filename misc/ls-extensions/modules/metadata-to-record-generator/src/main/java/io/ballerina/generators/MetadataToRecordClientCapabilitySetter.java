package io.ballerina.generators;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter;

/**
 * Client Capability setter for the {@link MetadataToRecordService}.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaClientCapabilitySetter")
public class MetadataToRecordClientCapabilitySetter extends
        BallerinaClientCapabilitySetter<MetadataToRecordClientCapabilities> {
    @Override
    public String getCapabilityName() {
        return MetadataToRecordConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<MetadataToRecordClientCapabilities> getCapability() {
        return MetadataToRecordClientCapabilities.class;
    }
}
