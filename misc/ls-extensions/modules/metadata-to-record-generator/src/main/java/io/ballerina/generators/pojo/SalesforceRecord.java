package io.ballerina.generators.pojo;

import java.util.List;

/**
 * A POJO class representing the extracted data for generating Ballerina record type.
 */
public class SalesforceRecord {
    String recordName;
    List<SalesforceFieldItem> fields;

    public SalesforceRecord() {

    }

    public SalesforceRecord(String recordName, List<SalesforceFieldItem> fields) {
        this.recordName = recordName;
        this.fields = fields;
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    public List<SalesforceFieldItem> getFields() {
        return fields;
    }

    public void setFields(List<SalesforceFieldItem> fields) {
        this.fields = fields;
    }
}
