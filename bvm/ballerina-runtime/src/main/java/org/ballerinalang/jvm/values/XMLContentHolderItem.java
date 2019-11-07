package org.ballerinalang.jvm.values;

import org.apache.axiom.om.OMNode;
import org.ballerinalang.jvm.XMLNodeType;

import java.util.Map;

public class XMLContentHolderItem extends XMLValue<OMNode> {

    private String data;
    private String target; // only applicable for PI nodes
    private XMLNodeType type;

    public XMLContentHolderItem(String data, XMLNodeType contentType) {
        this.data = data;
        this.type = contentType;
    }

    public XMLContentHolderItem(String data, String target) {
        this.data = data;
        this.target = target;
        this.type = XMLNodeType.PI;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public String getItemType() {
        return null;
    }

    @Override
    public String getElementName() {
        return null;
    }

    @Override
    public String getTextValue() {
        return null;
    }

    @Override
    public String getAttribute(String localName, String namespace) {
        return null;
    }

    @Override
    public String getAttribute(String localName, String namespace, String prefix) {
        return null;
    }

    @Override
    public void setAttribute(String localName, String namespace, String prefix, String value) {

    }

    @Override
    public MapValue<String, ?> getAttributesMap() {
        return null;
    }

    @Override
    public void setAttributes(MapValue<String, ?> attributes) {

    }

    @Override
    public XMLValue<?> elements() {
        return null;
    }

    @Override
    public XMLValue<?> elements(String qname) {
        return null;
    }

    @Override
    public XMLValue<?> children() {
        return null;
    }

    @Override
    public XMLValue<?> children(String qname) {
        return null;
    }

    @Override
    public void setChildren(XMLValue<?> seq) {

    }

    @Override
    public void addChildren(XMLValue<?> seq) {

    }

    @Override
    public XMLValue<?> strip() {
        return null;
    }

    @Override
    public XMLNodeType getNodeType() {
        return null;
    }

    @Override
    public XMLValue<?> slice(long startIndex, long endIndex) {
        return null;
    }

    @Override
    public XMLValue<?> descendants(String qname) {
        return null;
    }

    @Override
    public XMLValue<?> getItem(int index) {
        return null;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public Object frozenCopy(Map<Object, Object> refs) {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void build() {

    }

    @Override
    public void removeAttribute(String qname) {

    }

    @Override
    public void removeChildren(String qname) {

    }

    @Override
    public OMNode value() {
        return null;
    }

    @Override
    public IteratorValue getIterator() {
        return null;
    }

    public enum ContentType {
        COMMENT, CDATA, PI;
    }
}
