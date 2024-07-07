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

import io.ballerina.wsdltoballerina.diagnostic.DiagnosticMessage;
import io.ballerina.wsdltoballerina.diagnostic.DiagnosticUtils;
import io.ballerina.wsdltoballerina.wsdlmodel.SOAPVersion;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLMessage;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLOperation;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLPart;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLPayload;
import io.ballerina.wsdltoballerina.wsdlmodel.WSDLService;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.ballerinalang.formatter.core.FormatterException;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLElement;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Header;
import javax.wsdl.extensions.soap12.SOAP12Operation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

public class WSDLToBallerina {
    private Port soapPort;
    private SOAPVersion soapVersion;

    public WSDLToBallerinaResponse generateFromWSDL(String wsdlText, List<String> filteredWSDLOperations) {
        List<DiagnosticMessage> diagnosticMessages = new ArrayList<>();
        WSDLToBallerinaResponse response = new WSDLToBallerinaResponse();
        try {
            WSDLReader reader = WSDLFactory.newInstance().newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setFeature("javax.wsdl.importDocuments", true);

            InputStream wsdlStream = new ByteArrayInputStream(wsdlText.getBytes(Charset.defaultCharset()));
            Definition wsdlDefinition = reader.readWSDL(null, new InputSource(wsdlStream));

            WSDLService wsdlService = getSOAPService(wsdlDefinition);
            if (wsdlService == null) {
                return response;
            }

            soapVersion = wsdlService.getSoapVersion();
            initializeSchemas(wsdlDefinition);

            List<WSDLOperation> wsdlOperations = getWSDLOperations();
            wsdlService.setWSDLOperations(wsdlOperations);

            TypeGenerator typeGenerator = new TypeGenerator(wsdlService);

            try {
                String typeModulePart = typeGenerator.generate(filteredWSDLOperations);
                GeneratedSourceFile typesSrc = new GeneratedSourceFile("types.bal", typeModulePart);
                response.setTypesSource(typesSrc);
            } catch (FormatterException e) {
                DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaErr101(null);
                diagnosticMessages.add(message);
            }
        } catch (WSDLException e) {
            DiagnosticMessage message = DiagnosticMessage.wsdlToBallerinaErr100(null);
            diagnosticMessages.add(message);
        }
        return DiagnosticUtils.getDiagnosticResponse(diagnosticMessages, response);
    }

    private void initializeSchemas(Definition wsdlDefinition) {
        Map<String, XmlSchema> targetNSToSchema = new HashMap<>();
        Types types = wsdlDefinition.getTypes();
        List<?> extensions = new ArrayList<>();
        if (types != null) {
            extensions = wsdlDefinition.getTypes().getExtensibilityElements();
        }
        for (Object extension : extensions) {
            if (extension instanceof Schema) {
                Element schElement = ((Schema) extension).getElement();
                String targetNamespace = schElement.getAttribute("targetNamespace");
                if (!targetNamespace.isEmpty()) {
                    targetNSToSchema.put(targetNamespace, new XmlSchemaCollection().read(schElement));
                }
            }
        }
        SchemaManager.getInstance().initializeSchemas(targetNSToSchema);
    }

    private WSDLService getSOAPService(Definition wsdlDefinition) {
        @SuppressWarnings("unchecked")
        Collection<Service> services = wsdlDefinition.getAllServices().values();
        for (Service service : services) {
            @SuppressWarnings("unchecked")
            Collection<Port> ports = service.getPorts().values();
            for (Port port : ports) {
                List<?> extensions = port.getExtensibilityElements();
                for (Object extension : extensions) {
                    if (extension instanceof SOAPAddress || extension instanceof SOAP12Address) {
                        this.soapPort = port;
                        SOAPVersion version = extension instanceof SOAPAddress ?
                                SOAPVersion.SOAP11 : SOAPVersion.SOAP12;
                        String serviceUrl = extension instanceof SOAPAddress ?
                                ((SOAPAddress) extension).getLocationURI() :
                                ((SOAP12Address) extension).getLocationURI();
                        WSDLService wsdlService = new WSDLService();
                        wsdlService.setSoapVersion(version);
                        wsdlService.setSoapServiceUrl(serviceUrl);
                        return wsdlService;
                    }
                }
            }
        }
        return null;
    }

    private List<WSDLOperation> getWSDLOperations() {
        List<WSDLOperation> wsdlOperations = new ArrayList<>();
        for (Object op : soapPort.getBinding().getBindingOperations()) {
            BindingOperation bindingOperation = (BindingOperation) op;
            String operationName = bindingOperation.getName();
            String operationAction = null;
            for (Object element : bindingOperation.getExtensibilityElements()) {
                if (soapVersion == SOAPVersion.SOAP11 && element instanceof SOAPOperation soapOperation) {
                    operationAction = soapOperation.getSoapActionURI();
                } else if (soapVersion == SOAPVersion.SOAP12 && element instanceof SOAP12Operation soapOperation) {
                    operationAction = soapOperation.getSoapActionURI();
                }
            }

            WSDLOperation wsdlOperation = new WSDLOperation(operationName, operationAction);
            WSDLPayload inputPayload = getWSDLPayload(bindingOperation, bindingOperation.getOperation().getInput());
            WSDLPayload outputPayload = getWSDLPayload(bindingOperation, bindingOperation.getOperation().getOutput());

            wsdlOperation.setOperationInput(inputPayload);
            wsdlOperation.setOperationOutput(outputPayload);

            wsdlOperations.add(wsdlOperation);
        }
        return wsdlOperations;
    }

    private WSDLPayload getWSDLPayload(BindingOperation bindingOperation, WSDLElement operationPayload) {
        WSDLPayload wsdlPayload = new WSDLPayload();
        List<?> extensions = new ArrayList<>();
        Message message = null;
        if (operationPayload instanceof Input) {
            wsdlPayload.setName(bindingOperation.getOperation().getInput().getName());
            extensions.addAll(bindingOperation.getBindingInput().getExtensibilityElements());
            message = bindingOperation.getOperation().getInput().getMessage();
        } else if (operationPayload instanceof Output) {
            wsdlPayload.setName(bindingOperation.getOperation().getOutput().getName());
            extensions.addAll(bindingOperation.getBindingOutput().getExtensibilityElements());
            message = bindingOperation.getOperation().getOutput().getMessage();
        }
        for (Object element : extensions) {
            if (soapVersion == SOAPVersion.SOAP11 && element instanceof SOAPHeader) {
                // TODO: Implement later.
            } else if (soapVersion == SOAPVersion.SOAP12 && element instanceof SOAP12Header) {
                // TODO: Implement later.
            }
        }
        if (message != null) {
            // TODO: Handle when the messages doesn't have tns:Types and rather have parts with basic types.
            WSDLMessage wsdlMessage = new WSDLMessage(message.getQName().getLocalPart());
            for (Iterator it = message.getParts().values().iterator(); it.hasNext(); ) {
                Object partObject = it.next();
                if (partObject instanceof Part part) {
                    QName partElementName = part.getElementName();
                    WSDLPart wsdlPart =
                            new WSDLPart(partElementName.getLocalPart(), partElementName.getNamespaceURI());
                    wsdlMessage.addPart(wsdlPart);

                }
            }
            wsdlPayload.setMessage(wsdlMessage);
        }
        return wsdlPayload;
    }
}
