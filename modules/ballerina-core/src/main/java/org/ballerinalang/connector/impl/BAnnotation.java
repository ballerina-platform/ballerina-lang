package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by rajith on 9/4/17.
 */
public class BAnnotation implements Annotation {
    String name;
    String pkgPath;

    //key - attributeName, value - annotation value
    private Map<String, AnnAttrValue> attributeValueMap = new LinkedHashMap<>();

    public BAnnotation(String name, String pkgPath) {
        this.name = name;
        this.pkgPath = pkgPath;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return pkgPath + ":" + name;
    }

    @Override
    public AnnAttrValue getAnnAttrValue(String attributeName) {
        return attributeValueMap.get(attributeName);
    }

    public void addAnnotationValue(String attributeName, AnnAttrValue annotationValue) {
        attributeValueMap.put(attributeName, annotationValue);
    }
}
