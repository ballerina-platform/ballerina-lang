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
    private String queryBatchSize;
    private String tableCheckQuery;
    private String tableCreateQuery;
    private String tableTruncateQuery;
    private String indexCreateQuery;
    private String recordSelectQuery;
    private String recordExistsQuery;
    private String recordInsertQuery;
    private String recordUpdateQuery;
    private String recordMergeQuery;
    private String recordDeleteQuery;
    private String quoteMark = "";
    private boolean keyExplicitNotNull = false;
    private RDBMSTypeMapping RDBMSTypeMapping;

    @XmlAttribute(name = "name", required = true)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @XmlAttribute(name = "minVersion", required = false)
    public double getMinVersion() {
        return minVersion;
    }

    public void setMaxVersion(double maxVersion) {
        this.maxVersion = maxVersion;
    }

    @XmlAttribute(name = "maxVersion", required = false)
    public double getMaxVersion() {
        return maxVersion;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @XmlAttribute(name = "category", required = false)
    public String getCategory() {
        return category;
    }

    public void setQueryBatchSize(String queryBatchSize) {
        this.queryBatchSize = queryBatchSize;
    }

    @XmlElement(name = "query-batch-size")
    public String getQueryBatchSize() {
        return queryBatchSize;
    }

    public String getTableCheckQuery() {
        return tableCheckQuery;
    }

    public void setTableCheckQuery(String tableCheckQuery) {
        this.tableCheckQuery = tableCheckQuery;
    }

    public String getTableCreateQuery() {
        return tableCreateQuery;
    }

    public void setTableCreateQuery(String tableCreateQuery) {
        this.tableCreateQuery = tableCreateQuery;
    }

    public String getTableTruncateQuery() {
        return tableTruncateQuery;
    }

    public void setTableTruncateQuery(String tableTruncateQuery) {
        this.tableTruncateQuery = tableTruncateQuery;
    }

    public String getIndexCreateQuery() {
        return indexCreateQuery;
    }

    public void setIndexCreateQuery(String indexCreateQuery) {
        this.indexCreateQuery = indexCreateQuery;
    }

    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }

    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
    }

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

    public void setRecordSelectQuery(String recordSelectQuery) {
        this.recordSelectQuery = recordSelectQuery;
    }

    public String getRecordSelectQuery() {
        return recordSelectQuery;
    }

    public void setRecordExistsQuery(String recordExistsQuery) {
        this.recordExistsQuery = recordExistsQuery;
    }

    public String getRecordExistsQuery() {
        return recordExistsQuery;
    }

    public void setRecordDeleteQuery(String recordDeleteQuery) {
        this.recordDeleteQuery = recordDeleteQuery;
    }

    public String getRecordDeleteQuery() {
        return recordDeleteQuery;
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

    @XmlElement(name = "typeMapping")
    public RDBMSTypeMapping getRDBMSTypeMapping() {
        return RDBMSTypeMapping;
    }

    public void setRDBMSTypeMapping(RDBMSTypeMapping RDBMSTypeMapping) {
        this.RDBMSTypeMapping = RDBMSTypeMapping;
    }

}
