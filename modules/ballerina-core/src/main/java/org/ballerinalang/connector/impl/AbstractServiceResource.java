package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.Annotation;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rajith on 9/4/17.
 */
public class AbstractServiceResource {

    //key - packagePath:annotationName, value - annotation
    //with below impl however only one annotation can be there with same name for a resource todo
    protected Map<String, Annotation> annotationMap = new HashMap<>();

    public void addAnnotation(String key, Annotation annotation) {
        //will replace if already exist
        annotationMap.put(key, annotation);
    }


}
