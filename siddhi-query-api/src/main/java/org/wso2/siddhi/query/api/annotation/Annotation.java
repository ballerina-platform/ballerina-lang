/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by suho on 8/1/14.
 */
public class Annotation {
    private String name;
    private List<Element> elements = new ArrayList<Element>();

    public Annotation(String name) {
        this.name = name;
    }

    public Annotation element(String key, String value) {
        elements.add(new Element(key, value));
        return this;
    }

    public Annotation element(String value) {
        elements.add(new Element(null, value));
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Element> getElements() {
        return elements;
    }

    public void setElements(List<Element> elements) {
        this.elements = elements;
    }

    public Annotation element(Element element) {
        this.elements.add(element);
        return this;
    }

    public static Annotation annotation(String name) {
        return new Annotation(name);
    }

    public String getElement(String key){
        for(Element element : elements){
            if(element.getKey().equalsIgnoreCase(key)){
                return element.getValue();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "name='" + name + '\'' +
                ", elements=" + elements +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;

        Annotation that = (Annotation) o;

        if (elements != null ? !elements.equals(that.elements) : that.elements != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (elements != null ? elements.hashCode() : 0);
        return result;
    }
}
