/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.wsdltoballerina;

import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.Field;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLOperation;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLPart;

import java.util.ArrayList;
import java.util.List;

public class OperationProcessor {
    private final WSDLOperation wsdlOperation;

    OperationProcessor(WSDLOperation wsdlOperation) {
        this.wsdlOperation = wsdlOperation;
    }

    List<Field> generateFields() {
        List<Field> fields = new ArrayList<>();
        PartProcessor partProcessor = new PartProcessor();
        List<WSDLPart> inputParts = wsdlOperation.getOperationInput().getMessage().getParts();
        for (WSDLPart inputPart : inputParts) {
            List<Field> inputFields = partProcessor.generateFields(inputPart);
            fields.addAll(inputFields);
        }
        List<WSDLPart> outputParts = wsdlOperation.getOperationOutput().getMessage().getParts();
        for (WSDLPart onputPart : outputParts) {
            List<Field> outputFields = partProcessor.generateFields(onputPart);
            fields.addAll(outputFields);
        }
        return fields;
    }
}
