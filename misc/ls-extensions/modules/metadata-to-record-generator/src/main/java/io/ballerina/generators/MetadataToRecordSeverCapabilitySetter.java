package io.ballerina.generators;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

/**
 * Capability setter for the {@link MetadataToRecordService}.
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class MetadataToRecordSeverCapabilitySetter extends
        BallerinaServerCapabilitySetter<MetadataToRecordServerCapabilities> {

    @Override
    public Optional<MetadataToRecordServerCapabilities> build() {
        MetadataToRecordServerCapabilities capabilities = new MetadataToRecordServerCapabilities();
        capabilities.setGetResults(true);
        capabilities.setGetRemoteFunctionCalls(true);
        return Optional.of(capabilities);
    }

    @Override
    public String getCapabilityName() {
        return MetadataToRecordConstants.CAPABILITY_NAME;
    }

    @Override
    public Class<MetadataToRecordServerCapabilities> getCapability() {
        return MetadataToRecordServerCapabilities.class;
    }
}
