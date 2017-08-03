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
package org.wso2.siddhi.query.api.annotation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Annotation for siddhi functions and extensions
 */
public class Annotation implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private ArrayList<Element> elements = new ArrayList<Element>();
    private ArrayList<Annotation> annotations = new ArrayList<Annotation>();

    public Annotation(String name) {
        this.name = name;
    }

    public static Annotation annotation(String name) {
        return new Annotation(name);
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
        this.elements.clear();
        this.elements.addAll(elements);
    }

    public String getElement(String key) {
        for (Element element : elements) {
            if (element.getKey() != null && element.getKey().equalsIgnoreCase(key)) {
                return element.getValue();
            }
        }
        return null;
    }

    public Annotation element(String key, String value) {
        elements.add(new Element(key, value));
        return this;
    }

    public Annotation element(String value) {
        elements.add(new Element(null, value));
        return this;
    }

    public Annotation element(Element element) {
        this.elements.add(element);
        return this;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations.clear();
        this.annotations.addAll(annotations);
    }

    public List<Annotation> getAnnotations(String name) {
        return annotations
                .stream()
                .filter(annotation -> annotation.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
    }

    public Annotation annotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "name='" + name + '\'' +
                ", elements=" + elements +
                ", annotations=" + annotations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Annotation that = (Annotation) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (elements != null ? !elements.equals(that.elements) : that.elements != null) {
            return false;
        }
        return annotations != null ? annotations.equals(that.annotations) : that.annotations == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (elements != null ? elements.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }
}
