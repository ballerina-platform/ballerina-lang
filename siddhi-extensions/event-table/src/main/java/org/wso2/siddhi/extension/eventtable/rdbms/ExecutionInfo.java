/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.rdbms;

import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.List;

/**
 * Contain all the configuration details to execute db actions
 */
public class ExecutionInfo {

    private List<Attribute> insertQueryColumnOrder;
    private List<Attribute> updateQueryColumnOrder;
    private List<Attribute> deleteQueryColumnOrder;
    private List<Attribute> conditionQueryColumnOrder;
    private String preparedInsertStatement;
    private String preparedDeleteStatement;
    private String preparedUpdateStatement;
    private String preparedCreateTableStatement;
    private String preparedSelectTableStatement;
    private String preparedTableExistenceCheckStatement;
    private String preparedTableRowExistenceCheckStatement;
    private boolean isBloomFilterCompatible;

    public List<Attribute> getInsertQueryColumnOrder() {
        return insertQueryColumnOrder;
    }

    public void setInsertQueryColumnOrder(List<Attribute> insertQueryColumnOrder) {
        this.insertQueryColumnOrder = insertQueryColumnOrder;
    }

    public String getPreparedInsertStatement() {
        return preparedInsertStatement;
    }

    public void setPreparedInsertStatement(String insertStatementPrefix) {
        this.preparedInsertStatement = insertStatementPrefix;
    }

    public String getPreparedUpdateStatement() {
        return preparedUpdateStatement;
    }

    public void setPreparedUpdateStatement(String preparedUpdateStatement) {
        this.preparedUpdateStatement = preparedUpdateStatement;
    }

    public String getPreparedCreateTableStatement() {
        return preparedCreateTableStatement;
    }

    public void setPreparedCreateTableStatement(String preparedCreateTableStatement) {
        this.preparedCreateTableStatement = preparedCreateTableStatement;
    }

    public List<Attribute> getUpdateQueryColumnOrder() {
        return updateQueryColumnOrder;
    }

    public void setUpdateQueryColumnOrder(List<Attribute> updateQueryColumnOrder) {
        this.updateQueryColumnOrder = updateQueryColumnOrder;
    }

    public String getPreparedTableExistenceCheckStatement() {
        return preparedTableExistenceCheckStatement;
    }

    public void setPreparedTableExistenceCheckStatement(String preparedTableExistenceCheckStatement) {
        this.preparedTableExistenceCheckStatement = preparedTableExistenceCheckStatement;
    }

    public String getPreparedDeleteStatement() {
        return preparedDeleteStatement;
    }

    public void setPreparedDeleteStatement(String preparedDeleteStatement) {
        this.preparedDeleteStatement = preparedDeleteStatement;
    }

    public List<Attribute> getDeleteQueryColumnOrder() {
        return deleteQueryColumnOrder;
    }

    public void setDeleteQueryColumnOrder(List<Attribute> deleteQueryColumnOrder) {
        this.deleteQueryColumnOrder = deleteQueryColumnOrder;
    }

    public String getPreparedSelectTableStatement() {
        return preparedSelectTableStatement;
    }

    public void setPreparedSelectTableStatement(String preparedSelectTableStatement) {
        this.preparedSelectTableStatement = preparedSelectTableStatement;
    }

    public List<Attribute> getConditionQueryColumnOrder() {
        return conditionQueryColumnOrder;
    }

    public void setConditionQueryColumnOrder(List<Attribute> conditionQueryColumnOrder) {
        this.conditionQueryColumnOrder = conditionQueryColumnOrder;
    }

    public String getPreparedTableRowExistenceCheckStatement() {
        return preparedTableRowExistenceCheckStatement;
    }

    public void setPreparedTableRowExistenceCheckStatement(String preparedTableRowExistenceCheckStatement) {
        this.preparedTableRowExistenceCheckStatement = preparedTableRowExistenceCheckStatement;
    }

    public void setIsBloomFilterCompatible(boolean isBloomFilterCompatible) {
        this.isBloomFilterCompatible = isBloomFilterCompatible;
    }

    public boolean isBloomFilterCompatible() {
        return isBloomFilterCompatible;
    }
}
