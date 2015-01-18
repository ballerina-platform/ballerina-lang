package org.wso2.siddhi.query.api.util;

import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.exception.DuplicateAnnotationException;

import java.util.List;

/**
 * Created by suho on 8/8/14.
 */
public class AnnotationHelper {


    public static Annotation getAnnotation(String annotationName, List<Annotation> annotationList) {
        Annotation annotation = null;
        for (Annotation aAnnotation : annotationList) {
            if (annotationName.equalsIgnoreCase(aAnnotation.getName())) {
                if (annotation == null) {
                    annotation = aAnnotation;
                } else {
                    throw new DuplicateAnnotationException("Annotation @" + annotationName + " is defined twice");
                }
            }
        }
        return annotation;
    }

    public static Element getAnnotationElement(String annotationName, String elementName, List<Annotation> annotationList) {
        Annotation annotation = getAnnotation(annotationName,annotationList);
        if (annotation != null) {
            Element element = null;
            for (Element aElement : annotation.getElements()) {
                if (elementName == null) {
                    if (aElement.getKey() == null) {

                        if (element == null) {
                            element = aElement;
                        } else {
                            throw new DuplicateAnnotationException("Annotation element @" + annotationName + "(...) is defined twice");
                        }
                    }
                } else {
                    if (elementName.equalsIgnoreCase(aElement.getKey())) {

                        if (element == null) {
                            element = aElement;
                        } else {
                            throw new DuplicateAnnotationException("Annotation element @" + annotationName + "(" + elementName + "=...) is defined twice");
                        }
                    }
                }

            }
            return element;
        }
        return null;
    }
}
