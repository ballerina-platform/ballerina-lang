/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.messaging.kafka.utils;

import org.ballerinalang.compiler.plugins.SupportedResourceParamTypes;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.FunctionNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.model.tree.SimpleVariableNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.util.AbstractTransportCompilerPlugin;

import java.util.List;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_RESOURCE_ON_MESSAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PARAMETER_CONSUMER_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PARAMETER_PARTITION_OFFSET_ARRAY_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PARAMETER_RECORD_ARRAY_NAME;
import static org.ballerinalang.util.diagnostic.Diagnostic.Kind.ERROR;

/**
 * Compiler plugin for validating Kafka Service.
 */

@SupportedResourceParamTypes(
        expectedListenerType = @SupportedResourceParamTypes.Type(
                packageName = KafkaConstants.FULL_PACKAGE_NAME,
                name = CONSUMER_STRUCT_NAME
        ),
        paramTypes = {
                @SupportedResourceParamTypes.Type(packageName = KAFKA_PACKAGE_NAME, name = CONSUMER_STRUCT_NAME)
        }
)
public class KafkaServiceCompilerPlugin extends AbstractTransportCompilerPlugin {

    private DiagnosticLog diagnosticLog = null;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.diagnosticLog = diagnosticLog;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        validateService(serviceNode);
    }

    private void validateService(ServiceNode serviceNode) {
        List<? extends FunctionNode> resources = serviceNode.getResources();
        // This is currently disabled as resources without parameters does not hit here.
        /*if (resources.size() == 0) {
            String message = "No resources found to handle the Kafka records in " + serviceNode.getName().getValue();
            logError(message, serviceNode.getPosition());
            return;
        }*/
        if (resources.size() > 1) {
            String message = "More than one resources found in Kafka service "
                    + serviceNode.getName().getValue()
                    + ". Kafka Service should only have one resource";
            logError(message, serviceNode.getPosition());
            return;
        }
        validateResource(resources.get(0));
    }

    private void validateResource(FunctionNode resource) {
        String resourceName = resource.getName().getValue();
        Diagnostic.DiagnosticPosition position = resource.getPosition();
        if (!KAFKA_RESOURCE_ON_MESSAGE.equals(resourceName)) {
            String message = "Kafka service has invalid resource: " + resourceName
                    + ". Valid resource name:" + KAFKA_RESOURCE_ON_MESSAGE;
            logError(message, position);
            return;
        }
        List<? extends SimpleVariableNode> inputParameters = resource.getParameters();
        if (inputParameters.size() == 2) {
            validateResourceWithTwoInputParameters(inputParameters, position);
        } else if (inputParameters.size() == 4) {
            validateResourceWithFourInputParameters(inputParameters, position);
        } else {
            logError("Invalid number of input parameters found in resource " + resourceName, position);
            return;
        }
        boolean isReturnTypeValid = isResourceReturnsErrorOrNil(resource);
        if (!isReturnTypeValid) {
            String message = "Invalid return type for the resource function:"
                    + "Expected error? or subset of error? but found: "
                    + ((BLangFunction) resource).returnTypeNode;
            logError(message, position);
        }
    }

    private void validateResourceWithTwoInputParameters(
            List<? extends SimpleVariableNode> parameters, Diagnostic.DiagnosticPosition position) {
        checkParameter(getParameterTypeName(parameters.get(0)), PARAMETER_CONSUMER_NAME, position);
        checkParameter(getParameterTypeName(parameters.get(1)), PARAMETER_RECORD_ARRAY_NAME, position);
    }

    private void validateResourceWithFourInputParameters(
            List<? extends SimpleVariableNode> parameters, Diagnostic.DiagnosticPosition position) {
        checkParameter(getParameterTypeName(parameters.get(0)), PARAMETER_CONSUMER_NAME, position);
        checkParameter(getParameterTypeName(parameters.get(1)), PARAMETER_RECORD_ARRAY_NAME, position);
        checkParameter(getParameterTypeName(parameters.get(2)), PARAMETER_PARTITION_OFFSET_ARRAY_NAME, position);
        checkParameter(getParameterTypeName(parameters.get(3)), BTypes.typeString.getName(), position);
    }

    private void checkParameter(String parameterName, String expectedValue, Diagnostic.DiagnosticPosition position) {
        String parameter = clearParameter(parameterName);
        if (!parameter.equals(expectedValue)) {
            String message = "Resource parameter " + parameterName + " is invalid. Expected: " + expectedValue + ".";
            logError(message, position);
        }
    }

    private String clearParameter(String parameter) {
        String clearedParameter = parameter.replace("wso2/", "").replace(":0.0.0", "");
        return clearedParameter;
    }

    private String getParameterTypeName (SimpleVariableNode parameter) {
        return ((BLangSimpleVariable) parameter).getTypeNode().type.toString();
    }

    private void logError(String message, Diagnostic.DiagnosticPosition position) {
        diagnosticLog.logDiagnostic(ERROR, position, message);
    }
}
