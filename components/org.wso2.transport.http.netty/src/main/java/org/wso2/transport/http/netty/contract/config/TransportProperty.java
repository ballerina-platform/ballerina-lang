package org.wso2.transport.http.netty.contract.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * represents transport property.
 */
@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransportProperty {

    @XmlAttribute
    protected String name;

    @XmlValue
    protected Object value;

    /**
     * @deprecated
     * @return the default transport property.
     */
    @Deprecated
    public static TransportProperty getDefault() {
        return new TransportProperty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
