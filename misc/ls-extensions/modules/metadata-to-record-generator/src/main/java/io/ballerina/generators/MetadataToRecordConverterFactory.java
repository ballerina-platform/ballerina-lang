package io.ballerina.generators;

import io.ballerina.generators.exceptions.MetadataToRecordGeneratorException;
import io.ballerina.generators.platform.MetadataConverter;
import io.ballerina.generators.platform.SalesforceMetadataConverter;

import static io.ballerina.generators.MetadataToRecordConstants.SALESFORCE;
import static io.ballerina.generators.MetadataToRecordConstants.UNSUPPORTED_METADATA_SOURCE;

/**
 * Factory class for the creation of metadata converter.
 */
public class MetadataToRecordConverterFactory {
    public MetadataConverter getConvert(String schemaSource) throws MetadataToRecordGeneratorException {
        if (SALESFORCE.equals(schemaSource)) {
            return new SalesforceMetadataConverter();
        } else {
            throw new MetadataToRecordGeneratorException(UNSUPPORTED_METADATA_SOURCE);
        }
    }
}
