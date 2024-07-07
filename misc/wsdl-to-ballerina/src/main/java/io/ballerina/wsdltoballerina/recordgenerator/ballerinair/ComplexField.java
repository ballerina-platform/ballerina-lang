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

package io.ballerina.wsdltoballerina.recordgenerator.ballerinair;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Ballerina Complex Filed (ie: Record type),
 * <p>
 * This class supports below specs.
 * XSD
 *     - annotations?
 *     - (simpleContent|complexContent|((group|all|choice|sequence)?,((attribute|attributeGroup)*,anyAttribute?)))
 * XSD
 */
public class ComplexField extends Field {
    private String name;
    private String type;
    private boolean required;
    private boolean nullable;
    private boolean array;
    private List<FieldConstraint> constraints;
    private List<Field> fields;
    private String includedType;
    private boolean isCyclicDep;

    public ComplexField(String name, String type) {
        this.name = name;
        this.type = type;
        this.required = true;
        this.nullable = false;
        this.array = false;
        this.constraints = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.isCyclicDep = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public List<FieldConstraint> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<FieldConstraint> constraints) {
        this.constraints = constraints;
    }

    public void addConstraint(FieldConstraint constraint) {
        this.constraints.add(constraint);
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public String getIncludedType() {
        return includedType;
    }

    public void setIncludedType(String includedType) {
        this.includedType = includedType;
    }

    public boolean isCyclicDep() {
        return isCyclicDep;
    }

    public void setCyclicDep(boolean cyclicDep) {
        isCyclicDep = cyclicDep;
    }
}
