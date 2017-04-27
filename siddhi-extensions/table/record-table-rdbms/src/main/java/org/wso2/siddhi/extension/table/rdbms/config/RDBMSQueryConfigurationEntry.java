/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.siddhi.extension.table.rdbms.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains all the Siddhi RDBMS query configuration mappings.
 */
@XmlRootElement(name = "database")
public class RDBMSQueryConfigurationEntry {

    private String databaseName;
    private String category;
    private double minVersion;
    private double maxVersion;
    private String tableCheckQuery;
    private String tableCreateQuery;
    private String indexCreateQuery;
    private String recordSelectQuery;
    private String recordExistsQuery;
    private String recordInsertQuery;
    private String recordUpdateQuery;
    private String recordMergeQuery;
    private String recordDeleteQuery;
    private String quoteMark = "";
    private boolean keyExplicitNotNull = false;
    private String stringSize;
    private RDBMSTypeMapping RDBMSTypeMapping;

    @XmlAttribute(name = "name", required = true)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @XmlAttribute(name = "minVersion")
    public double getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(double minVersion) {
        this.minVersion = minVersion;
    }

    @XmlAttribute(name = "maxVersion")
    public double getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(double maxVersion) {
        this.maxVersion = maxVersion;
    }

    @XmlAttribute(name = "category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @XmlAttribute(required = true)
    public String getTableCheckQuery() {
        return tableCheckQuery;
    }

    public void setTableCheckQuery(String tableCheckQuery) {
        this.tableCheckQuery = tableCheckQuery;
    }

    @XmlAttribute(required = true)
    public String getTableCreateQuery() {
        return tableCreateQuery;
    }

    public void setTableCreateQuery(String tableCreateQuery) {
        this.tableCreateQuery = tableCreateQuery;
    }

    @XmlAttribute(required = true)
    public String getIndexCreateQuery() {
        return indexCreateQuery;
    }

    public void setIndexCreateQuery(String indexCreateQuery) {
        this.indexCreateQuery = indexCreateQuery;
    }

    @XmlAttribute(required = true)
    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }

    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
    }

    @XmlAttribute(required = true)
    public String getRecordUpdateQuery() {
        return recordUpdateQuery;
    }

    public void setRecordUpdateQuery(String recordUpdateQuery) {
        this.recordUpdateQuery = recordUpdateQuery;
    }

    public String getRecordMergeQuery() {
        return recordMergeQuery;
    }

    public void setRecordMergeQuery(String recordMergeQuery) {
        this.recordMergeQuery = recordMergeQuery;
    }

    @XmlAttribute(required = true)
    public String getRecordSelectQuery() {
        return recordSelectQuery;
    }

    public void setRecordSelectQuery(String recordSelectQuery) {
        this.recordSelectQuery = recordSelectQuery;
    }

    @XmlAttribute(required = true)
    public String getRecordExistsQuery() {
        return recordExistsQuery;
    }

    public void setRecordExistsQuery(String recordExistsQuery) {
        this.recordExistsQuery = recordExistsQuery;
    }

    @XmlAttribute(required = true)
    public String getRecordDeleteQuery() {
        return recordDeleteQuery;
    }

    public void setRecordDeleteQuery(String recordDeleteQuery) {
        this.recordDeleteQuery = recordDeleteQuery;
    }

    public String getQuoteMark() {
        return quoteMark;
    }

    public void setQuoteMark(String quoteMark) {
        this.quoteMark = quoteMark;
    }

    public boolean isKeyExplicitNotNull() {
        return keyExplicitNotNull;
    }

    public void setKeyExplicitNotNull(boolean keyExplicitNotNull) {
        this.keyExplicitNotNull = keyExplicitNotNull;
    }

    public String getStringSize() {
        return stringSize;
    }

    public void setStringSize(String stringSize) {
        this.stringSize = stringSize;
    }

    @XmlElement(name = "typeMapping", required = true)
    public RDBMSTypeMapping getRDBMSTypeMapping() {
        return RDBMSTypeMapping;
    }

    public void setRDBMSTypeMapping(RDBMSTypeMapping RDBMSTypeMapping) {
        this.RDBMSTypeMapping = RDBMSTypeMapping;
    }

}
