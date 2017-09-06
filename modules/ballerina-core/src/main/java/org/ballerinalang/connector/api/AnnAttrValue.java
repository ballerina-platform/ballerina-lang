package org.ballerinalang.connector.api;

/**
 * Created by rajith on 9/4/17.
 */
public interface AnnAttrValue {

    AnnotationValueType getType();

    long getIntValue();

    double getFloatValue();

    String getStringValue();

    boolean getBooleanValue();

    Annotation getAnnotation();

    AnnAttrValue[] getAnnAttrValueArray();

}
