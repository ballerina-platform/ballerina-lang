/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.query.api.definition;

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.exception.AttributeAlreadyExistException;
import org.wso2.siddhi.query.api.exception.AttributeNotExistException;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDefinition implements ExecutionPlan {

    protected String id;
    protected List<Attribute> attributeList = new ArrayList<Attribute>();



    protected void checkAttribute(String attributeName) {
        for (Attribute attribute : attributeList) {
            if (attribute.getName().equals(attributeName)) {
                throw new AttributeAlreadyExistException(attributeName + " is already defined for with type " + attribute.getType() + " for " + id);
            }
        }
    }

    public List<Attribute> getAttributeList() {
        return attributeList;
    }

    public Attribute.Type getAttributeType(String attributeName) {
        for (Attribute attribute : attributeList) {
            if (attribute.getName().equals(attributeName)) {
                return attribute.getType();
            }
        }
        throw new AttributeNotExistException("Cannot find attribute type as " + attributeName + " does not exist in "+id);
    }

    public int getAttributePosition(String attributeName) {
        for (int i = 0, attributeListSize = attributeList.size(); i < attributeListSize; i++) {
            Attribute attribute = attributeList.get(i);
            if (attribute.getName().equals(attributeName)) {
                return i;
            }
        }
        throw new AttributeNotExistException("Cannot get attribute position as " + attributeName + " does not exist in "+id);
    }


    public String[] getAttributeNameArray() {
        int attributeListSize = attributeList.size();
        String[] attributeNameArray = new String[attributeListSize];
        for (int i = 0; i < attributeListSize; i++) {
            attributeNameArray[i] = attributeList.get(i).getName();
        }
        return attributeNameArray;
    }

    public String getId() {
        return id;
    }
}
