package io.ballerina.generators.platform;

import io.ballerina.generators.MetadataToRecordSchemaResponse;
import io.ballerina.generators.exceptions.MetadataToRecordGeneratorException;

/**
 * Interface that defines a metadata converter.
 */
public interface MetadataConverter {
    public MetadataToRecordSchemaResponse convert(String metadata) throws MetadataToRecordGeneratorException;
}
