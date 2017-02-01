package org.wso2.ballerina.tooling.service.workspace.swagger;

import org.wso2.ballerina.core.nativeimpl.connectors.http.Constants;

/**
 * Hold constants required for mapper.
 */
public class MapperConstants {
    final static String httpVerbMatchingPattern = "(?i)|" + Constants.ANNOTATION_METHOD_GET + "|" +
    Constants.ANNOTATION_METHOD_PUT + "|" + Constants.ANNOTATION_METHOD_POST + "|" +
    Constants.ANNOTATION_METHOD_DELETE + "|" + Constants.ANNOTATION_METHOD_OPTIONS;
}
