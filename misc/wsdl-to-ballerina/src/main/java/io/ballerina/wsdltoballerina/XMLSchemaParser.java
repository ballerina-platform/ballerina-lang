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

import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.BasicField;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.ComplexField;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.EnumConstraint;
import io.ballerina.wsdltoballerina.recordgenerator.ballerinair.Field;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAnyAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaEnumerationFacet;
import org.apache.ws.commons.schema.XmlSchemaFacet;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeContent;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeList;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeRestriction;
import org.apache.ws.commons.schema.XmlSchemaSimpleTypeUnion;
import org.apache.ws.commons.schema.XmlSchemaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class XMLSchemaParser {
    private final XmlSchemaElement rootElement;
    private final List<Field> fields;
    private final Map<XmlSchemaType, Field> visitedTypes = new HashMap<>();

    protected XMLSchemaParser(XmlSchemaElement rootElement) {
        this.rootElement = rootElement;
        fields = new ArrayList<>();
    }

    protected List<Field> parseRootElement() {
        XmlSchemaType elementType = rootElement.getSchemaType();
        if (elementType instanceof XmlSchemaSimpleType) {
            fields.add(processSimpleField(rootElement, (XmlSchemaSimpleType) elementType));
        } else if (elementType instanceof XmlSchemaComplexType) {
            fields.add(processComplexField(rootElement, (XmlSchemaComplexType) elementType));
        }
        return fields;
    }

    private BasicField processSimpleField(XmlSchemaElement element, XmlSchemaSimpleType elementType) {
        String name = element.getName();
        XmlSchemaSimpleTypeContent typeContent = elementType.getContent();
        if (typeContent == null) {
            String type = element.getSchemaTypeName().getLocalPart();
            BasicField elementBasicField = new BasicField(name, type);
            elementBasicField.setNullable(element.isNillable());
            elementBasicField.setRequired(element.getMinOccurs() > 0);
            elementBasicField.setDefaultValue(element.getDefaultValue());
            elementBasicField.setArray(element.getMaxOccurs() > 1);
            return elementBasicField;
        }
        if (typeContent instanceof XmlSchemaSimpleTypeList) {
            // TODO: Implement this later
        } else if (typeContent instanceof XmlSchemaSimpleTypeRestriction) {
            XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) typeContent;
            String type = restriction.getBaseTypeName().getLocalPart();
            BasicField elementBasicField = new BasicField(name, type);
            elementBasicField.setNullable(element.isNillable());
            elementBasicField.setRequired(element.getMinOccurs() > 0);
            elementBasicField.setDefaultValue(element.getDefaultValue());
            elementBasicField.setArray(element.getMaxOccurs() > 1);
            processRestriction(restriction, elementBasicField);
            return elementBasicField;
        } else if (typeContent instanceof XmlSchemaSimpleTypeUnion) {
            // TODO: Implement this later
        }
        return null;
    }

    private ComplexField processComplexField(XmlSchemaElement element, XmlSchemaComplexType elementType) {
        String name = element.getName();
        ComplexField complexField = new ComplexField(name, elementType.getName());
        ComplexField processedComplexField = processComplexField(complexField, elementType);
        processedComplexField.setNullable(element.isNillable());
        processedComplexField.setRequired(element.getMinOccurs() > 0);
        processedComplexField.setArray(element.getMaxOccurs() > 1);
        return processedComplexField;
    }

    private ComplexField processComplexField(XmlSchemaComplexType elementType) {
        ComplexField complexField = new ComplexField(null, elementType.getName());
        return processComplexField(complexField, elementType);
    }

    private ComplexField processComplexField(ComplexField complexField, XmlSchemaComplexType elementType) {
        visitedTypes.put(elementType, complexField);

        // Process Content Models
        XmlSchemaContentModel contentModel = elementType.getContentModel();
        if (contentModel != null) {
            // In this case `particle` should be null;
            if (contentModel instanceof XmlSchemaSimpleContent) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaSimpleContentExtension simpleContent) {
                    String baseType = simpleContent.getBaseTypeName().getLocalPart();
                    String baseTypeNSURI = simpleContent.getBaseTypeName().getNamespaceURI();
                    if (!XMLSchemaParserUtils.isXSDDataType(baseType)) {
                        complexField.setIncludedType(baseType);
                        processIncludedType(baseType, baseTypeNSURI);
                    }
                    XmlSchemaObjectCollection attributes = ((XmlSchemaSimpleContentExtension) content).getAttributes();
                    if (attributes != null) {
                        for (Iterator it = attributes.getIterator(); it.hasNext(); ) {
                            Object attributeMember = it.next();
                            if (attributeMember instanceof XmlSchemaAttribute attribute) {
                                BasicField attributeBasicField = processAttributeType(attribute);
                                complexField.addField(attributeBasicField);
                            }
                        }
                    }
                } else if (content instanceof XmlSchemaSimpleContentRestriction) {
                    // TODO: Implement this later
                }
            } else if (contentModel instanceof XmlSchemaComplexContent) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaComplexContentExtension complexContent) {
                    String baseType = complexContent.getBaseTypeName().getLocalPart();
                    String baseTypeNSURI = complexContent.getBaseTypeName().getNamespaceURI();
                    if (!XMLSchemaParserUtils.isXSDDataType(baseType)) {
                        complexField.setIncludedType(baseType);
                        processIncludedType(baseType, baseTypeNSURI);
                    }
                    XmlSchemaParticle particle = complexContent.getParticle();
                    if (particle != null) {
                        processParticle(particle, complexField);
                    }
                    XmlSchemaObjectCollection attributes = complexContent.getAttributes();
                    if (attributes != null) {
                        processAttributes(attributes, complexField);
                    }
                } else if (content instanceof XmlSchemaComplexContentRestriction) {
                    // TODO: Implement this later
                }
            }
        }
        // Process Particles
        XmlSchemaParticle particle = elementType.getParticle();
        if (particle != null) {
            // In this case `contentModel` should be null;
            processParticle(particle, complexField);
        }
        // Process Attributes
        XmlSchemaObjectCollection attributes = elementType.getAttributes();
        if (attributes != null) {
            processAttributes(attributes, complexField);
        }
        XmlSchemaAnyAttribute anyAttribute = elementType.getAnyAttribute();
        if (anyAttribute != null) {
            // TODO: Implement this later
        }
        return complexField;
    }

    private void processParticle(XmlSchemaParticle particle, ComplexField complexField) {
        if (particle instanceof XmlSchemaAll) {
            // TODO: Implement this later
        } else if (particle instanceof XmlSchemaChoice) {
            // TODO: Implement this later
        } else if (particle instanceof XmlSchemaSequence) {
            XmlSchemaObjectCollection childElements = ((XmlSchemaSequence) particle).getItems();
            for (Iterator it = childElements.getIterator(); it.hasNext(); ) {
                Object childElementMember = it.next();
                if (childElementMember instanceof XmlSchemaElement childElement) {
                    XmlSchemaType childElementType = childElement.getSchemaType();
                    if (visitedTypes.containsKey(childElementType)) {
                        ComplexField nestedField =
                                new ComplexField(childElement.getName(), childElement.getSchemaType().getName());
                        nestedField.setNullable(childElement.isNillable());
                        nestedField.setRequired(childElement.getMinOccurs() > 0);
                        nestedField.setArray(childElement.getMaxOccurs() > 1);
                        nestedField.setCyclicDep(true);
                        complexField.addField(nestedField);
                        continue;
                    }
                    if (childElementType instanceof XmlSchemaSimpleType) {
                        BasicField childElementBasicField =
                                processSimpleField(childElement, (XmlSchemaSimpleType) childElementType);
                        complexField.addField(childElementBasicField);
                    } else if (childElementType instanceof XmlSchemaComplexType) {
                        ComplexField childElementComplexField =
                                processComplexField(childElement, (XmlSchemaComplexType) childElementType);
                        complexField.addField(childElementComplexField);
                    }
                }
            }
        }
    }

    private void processAttributes(XmlSchemaObjectCollection attributes, ComplexField complexField) {
        for (Iterator it = attributes.getIterator(); it.hasNext(); ) {
            Object attributeMember = it.next();
            if (attributeMember instanceof XmlSchemaAttribute attribute) {
                BasicField attributeBasicField = processAttributeType(attribute);
                complexField.addField(attributeBasicField);
            }
        }
    }

    private void processRestriction(XmlSchemaSimpleTypeRestriction restriction, BasicField basicField) {
        XmlSchemaObjectCollection facets = restriction.getFacets();
        EnumConstraint enumConstraint = new EnumConstraint();
        for (Iterator it = facets.getIterator(); it.hasNext(); ) {
            XmlSchemaFacet facet = (XmlSchemaFacet) it.next();
            if (facet instanceof XmlSchemaEnumerationFacet) {
                XmlSchemaEnumerationFacet enumFacet = (XmlSchemaEnumerationFacet) facet;
                enumConstraint.addEnumValue(enumFacet.getValue().toString());
            } else {
                // TODO: There are so many facets have to handle.
            }
        }
        basicField.addConstraint(enumConstraint);
    }

    private BasicField processAttributeType(XmlSchemaAttribute attribute) {
        String attributeName = attribute.getName();
        String attributeType = attribute.getSchemaTypeName().getLocalPart();
        XmlSchema schema = SchemaManager.getInstance().getSchema(attribute.getSchemaTypeName().getNamespaceURI());
        if (schema != null) {
            XmlSchemaType schemaType = schema.getTypeByName(attribute.getSchemaTypeName());
            if (schemaType instanceof XmlSchemaSimpleType) {
                BasicField dependentBasicField =
                        processDependentBasicFieldForAttribute(attribute, (XmlSchemaSimpleType) schemaType);
                fields.add(dependentBasicField);
            }
        }
        BasicField attributeBasicField = new BasicField(attributeName, attributeType);
        attributeBasicField.setNullable(false);
        attributeBasicField.setRequired(attribute.getUse().getValue().equals(GeneratorConstants.USE_REQUIRED));
        attributeBasicField.setDefaultValue(attribute.getDefaultValue());
        attributeBasicField.setArray(false);
        attributeBasicField.setAttributeField(true);
        return attributeBasicField;
    }

    private BasicField processDependentBasicFieldForAttribute(XmlSchemaAttribute attribute,
                                                              XmlSchemaSimpleType elementType) {
        String name = elementType.getName();
        XmlSchemaSimpleTypeContent typeContent = elementType.getContent();
        if (typeContent == null) {
            String type = attribute.getSchemaTypeName().getLocalPart();
            BasicField basicField = new BasicField(name, type);
            basicField.setNullable(false);
            basicField.setRequired(attribute.getUse().getValue().equals(GeneratorConstants.USE_REQUIRED));
            basicField.setDefaultValue(attribute.getDefaultValue());
            basicField.setArray(false);
            return basicField;
        }
        if (typeContent instanceof XmlSchemaSimpleTypeList) {
            // TODO: Implement this later
        } else if (typeContent instanceof XmlSchemaSimpleTypeRestriction) {
            XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) typeContent;
            String type = restriction.getBaseTypeName().getLocalPart();
            BasicField basicField = new BasicField(name, type);
            basicField.setNullable(false);
            basicField.setRequired(attribute.getUse().getValue().equals(GeneratorConstants.USE_REQUIRED));
            basicField.setDefaultValue(attribute.getDefaultValue());
            basicField.setArray(false);
            processRestriction(restriction, basicField);
            return basicField;
        } else if (typeContent instanceof XmlSchemaSimpleTypeUnion) {
            // TODO: Implement this later
        }
        return null;
    }

    private void processIncludedType(String includedType, String baseTypeNSURI) {
        XmlSchema schema = SchemaManager.getInstance().getSchema(baseTypeNSURI);
        if (schema != null) {
            XmlSchemaType schemaType = schema.getTypeByName(includedType);
            if (schemaType instanceof XmlSchemaComplexType) {
                if (!visitedTypes.containsKey(schemaType)) {
                    ComplexField includedTypeField = processComplexField((XmlSchemaComplexType) schemaType);
                    fields.add(includedTypeField);
                }
            }
        }
    }
}
