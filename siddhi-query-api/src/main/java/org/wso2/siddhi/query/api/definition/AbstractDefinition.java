/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;
import org.wso2.siddhi.query.api.exception.DuplicateAttributeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract definition used for Streams, Tables and other common artifacts
 */
public abstract class AbstractDefinition implements Serializable {

    protected String id;
    protected List<Attribute> attributeList = new ArrayList<Attribute>();
    protected List<Annotation> annotations = new ArrayList<Annotation>();

    protected AbstractDefinition() {

    }

    protected AbstractDefinition(String id) {
        this.id = id;
    }

    public static Annotation annotation(String name) {
        return new Annotation(name);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    protected void checkAttribute(String attributeName) {
        for (Attribute attribute : attributeList) {
            if (attribute.getName().equals(attributeName)) {
                throw new DuplicateAttributeException("'" + attributeName + "' is already defined for with type '" +
                        attribute.getType() + "' for '" + id + "'; " + this.toString());
            }
        }
    }

    public Attribute.Type getAttributeType(String attributeName) {
        for (Attribute attribute : attributeList) {
            if (attribute.getName().equals(attributeName)) {
                return attribute.getType();
            }
        }
        throw new AttributeNotExistException("Cannot find attribute type as '" + attributeName + "' does not exist in" +
                " '" + id + "'; " + this.toString());
    }

    public int getAttributePosition(String attributeName) {
        for (int i = 0, attributeListSize = attributeList.size(); i < attributeListSize; i++) {
            Attribute attribute = attributeList.get(i);
            if (attribute.getName().equals(attributeName)) {
                return i;
            }
        }
        throw new AttributeNotExistException("Cannot get attribute position as '" + attributeName + "' does not exist" +
                " in '" + id + "'; " + this.toString());
    }

    public String[] getAttributeNameArray() {
        int attributeListSize = attributeList.size();
        String[] attributeNameArray = new String[attributeListSize];
        for (int i = 0; i < attributeListSize; i++) {
            attributeNameArray[i] = attributeList.get(i).getName();
        }
        return attributeNameArray;
    }

    @Override
    public String toString() {
        return "AbstractDefinition{" +
                "id='" + id + '\'' +
                ", attributeList=" + attributeList +
                ", annotations=" + annotations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractDefinition)) {
            return false;
        }

        AbstractDefinition that = (AbstractDefinition) o;

        if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) {
            return false;
        }
        if (attributeList != null ? !attributeList.equals(that.attributeList) : that.attributeList != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (attributeList != null ? attributeList.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }


    public boolean equalsIgnoreAnnotations(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractDefinition)) {
            return false;
        }

        AbstractDefinition that = (AbstractDefinition) o;

        if (attributeList != null ? !attributeList.equals(that.attributeList) : that.attributeList != null) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }

        return true;
    }
}
