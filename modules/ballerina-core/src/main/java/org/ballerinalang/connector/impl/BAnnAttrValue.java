package org.ballerinalang.connector.impl;

import org.ballerinalang.connector.api.AnnAttrValue;
import org.ballerinalang.connector.api.Annotation;
import org.ballerinalang.connector.api.AnnotationValueType;

/**
 * Created by rajith on 9/4/17.
 */
public class BAnnAttrValue implements AnnAttrValue {
    private AnnotationValueType type;

    private long intValue;
    private double floatValue;
    private String stringValue;
    private boolean booleanValue;

    private Annotation annotation;

    private AnnAttrValue[] annotationValueArray;


    public BAnnAttrValue(AnnotationValueType type) {
        this.type = type;
    }

    @Override
    public AnnotationValueType getType() {
        return type;
    }

    @Override
    public long getIntValue() {
        return intValue;
    }

    @Override
    public double getFloatValue() {
        return floatValue;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public boolean getBooleanValue() {
        return booleanValue;
    }

    @Override
    public Annotation getAnnotation() {
        return annotation;
    }

    @Override
    public AnnAttrValue[] getAnnAttrValueArray() {
        return annotationValueArray;
    }

    public void setIntValue(long intValue) {
        this.intValue = intValue;
    }

    public void setFloatValue(double floatValue) {
        this.floatValue = floatValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public void setAnnotationValueArray(AnnAttrValue[] annotationValueArray) {
        this.annotationValueArray = annotationValueArray;
    }


}
