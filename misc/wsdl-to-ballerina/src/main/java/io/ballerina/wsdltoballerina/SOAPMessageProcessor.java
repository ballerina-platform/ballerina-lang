///*
// *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
// *
// *  WSO2 LLC. licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except
// *  in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// */
//
//package io.ballerina.wsdltoballerina;
//
//import io.ballerina.compiler.syntax.tree.AbstractNodeFactory;
//import io.ballerina.compiler.syntax.tree.AnnotationNode;
//import io.ballerina.compiler.syntax.tree.Node;
//import io.ballerina.compiler.syntax.tree.NodeFactory;
//import io.ballerina.compiler.syntax.tree.NodeList;
//import io.ballerina.compiler.syntax.tree.NonTerminalNode;
//import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
//import io.ballerina.compiler.syntax.tree.SyntaxKind;
//import io.ballerina.compiler.syntax.tree.Token;
//import org.apache.ws.commons.schema.XmlSchema;
//import org.apache.ws.commons.schema.XmlSchemaAll;
//import org.apache.ws.commons.schema.XmlSchemaChoice;
//import org.apache.ws.commons.schema.XmlSchemaComplexType;
//import org.apache.ws.commons.schema.XmlSchemaElement;
//import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
//import org.apache.ws.commons.schema.XmlSchemaParticle;
//import org.apache.ws.commons.schema.XmlSchemaSequence;
//import org.apache.ws.commons.schema.XmlSchemaSimpleType;
//import org.apache.ws.commons.schema.XmlSchemaType;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import javax.wsdl.Message;
//import javax.wsdl.Part;
//import javax.xml.namespace.QName;
//
//public class SOAPMessageProcessor {
//
////    private final Message message;
////    private final Map<String, XmlSchema> targetNSToSchema;
////    private Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
////
////    protected SOAPMessageProcessor(Message message, Map<String, XmlSchema> targetNSToSchema) {
////        this.message = message;
////        this.targetNSToSchema = targetNSToSchema;
////    }
////
////    public RecordTypeDescriptorNode generateRecordsForMessage() {
////        Token recordKeyWord = AbstractNodeFactory.createToken(SyntaxKind.RECORD_KEYWORD);
////        Token bodyStartDelimiter = AbstractNodeFactory.createToken(SyntaxKind.OPEN_BRACE_TOKEN);
////        String recordName = message.getQName().getLocalPart();
////        Collection<Part> messageParts = message.getParts().values();
////        List<Node> recordFields = new ArrayList<>();
////        for (Part part : messageParts) {
//////            Node recordField = getPartRecordField(part);
////            getPartRecordField(part);
////        }
////
////        NodeList<Node> fieldNodes = AbstractNodeFactory.createNodeList(recordFields);
////        Token bodyEndDelimiter = AbstractNodeFactory.createToken(SyntaxKind.CLOSE_BRACE_TOKEN);
////        RecordTypeDescriptorNode baseRecordTypeDescNode =
////                NodeFactory.createRecordTypeDescriptorNode(recordKeyWord, bodyStartDelimiter, fieldNodes,
////                        null, bodyEndDelimiter);
////        return baseRecordTypeDescNode;
////    }
////
////    private void getPartRecordField(Part part) {
////        QName elementName = part.getElementName();
////
////        XmlSchema schema = targetNSToSchema.get(elementName.getNamespaceURI());
////        XmlSchemaElement xmlSchemaElement = schema.getElementByName(elementName);
////
////        Map<String, NonTerminalNode> recordToTypeDescNodes = new LinkedHashMap<>();
////        Map<String, AnnotationNode> recordToAnnotationNodes = new LinkedHashMap<>();
////        Map<String, XmlSchemaElement> recordToElementNodes = new LinkedHashMap<>();
////        XMLSchemaToRecordConverter converter = new XMLSchemaToRecordConverter();
////        converter.generateRecords(xmlSchemaElement, false, recordToTypeDescNodes, recordToAnnotationNodes, recordToElementNodes, "", false);
////
//////        if (schemaType instanceof XmlSchemaComplexType) {
//////            processComplexType((XmlSchemaComplexType) schemaType, part.getName());
//////        } else if (schemaType instanceof XmlSchemaSimpleType) {
////////            return processSimpleType((XmlSchemaSimpleType) param, name, isOptional(schema.getElementByName(elementName)));
//////        }
////    }
////
////    private String processComplexType(XmlSchemaComplexType elemType, String name) {
////        XmlSchemaParticle particle = elemType.getParticle();
////        String ret = "";
////
////        if (particle == null) { // WSDL4J fails parsing some advanced definitions
////            if (elemType.getUnhandledAttributes() != null) {
////                return "Unprocessed";
//////                return CodeWriter.keyVal(name, "NIL", "Unprocessed complex type - Please refer to documentation");
////            } else { // aka. no such element in the WSDL definition
////                return "";
////            }
////        }
////
////        XmlSchemaObjectCollection elementList = null;
////        if (particle instanceof XmlSchemaSequence) {
////            elementList = ((XmlSchemaSequence) particle).getItems();
////        } else if (particle instanceof XmlSchemaAll) {
////            elementList = ((XmlSchemaAll) particle).getItems();
////        } else if (particle instanceof XmlSchemaChoice) {
////            elementList = ((XmlSchemaChoice) particle).getItems();
////        }
////
////        for (Iterator it = elementList.getIterator(); it.hasNext(); ) {
////            Object member = it.next();
////            if (member instanceof XmlSchemaElement) {
////                XmlSchemaElement element = ((XmlSchemaElement) member);
////                XmlSchemaType elementType = element.getSchemaType();
////
////                if (elementType instanceof XmlSchemaSimpleType) {
//////                    ret += processSimpleType((XmlSchemaSimpleType) elementType, element.getName(), isOptional(element));
////                } else if (elementType instanceof XmlSchemaComplexType) {
//////                    ret += "    @\"" + element.getName() + "\" : @{" + (isOptional(element) ? "    // {Optional}\n" : "\n");
////                    ret += processComplexType((XmlSchemaComplexType) elementType, element.getName());
//////                    ret += "    },\n";
////                }
////
////
////            }
////        }
////        return ret;
////    }
////
//////    private void generateRecords(Message message) {
//////
//////
//////        String recordName = message.getQName().getLocalPart();
//////
//////    }
//}
