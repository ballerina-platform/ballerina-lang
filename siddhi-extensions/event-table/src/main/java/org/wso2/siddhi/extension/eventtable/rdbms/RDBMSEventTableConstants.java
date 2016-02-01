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


public interface RDBMSEventTableConstants {

    public static final String EVENT_TABLE_RDBMS_ATTRIBUTE_TABLE_NAME = "$TABLE_NAME";
    public static final String EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_TYPES = "$COLUMN_TYPES";
    public static final String EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMNS = "$COLUMNS";
    public static final String EVENT_TABLE_RDBMS_ATTRIBUTE_VALUES = "$VALUES";
    public static final String EVENT_TABLE_RDBMS_ATTRIBUTE_COLUMN_VALUES = "$COLUMN_VALUES";
    public static final String EVENT_TABLE_RDBMS_ATTRIBUTE_CONDITION = "$CONDITION";
    public static final String EVENT_TABLE_RDBMS_COMMA = "comma";
    public static final String EVENT_TABLE_RDBMS_INTEGER = "integer";
    public static final String EVENT_TABLE_RDBMS_LONG = "long";
    public static final String EVENT_TABLE_RDBMS_FLOAT = "float";
    public static final String EVENT_TABLE_RDBMS_DOUBLE = "double";
    public static final String EVENT_TABLE_RDBMS_STRING = "string";
    public static final String EVENT_TABLE_RDBMS_BOOL = "bool";
    public static final String EVENT_TABLE_RDBMS_QUESTION_MARK = "questionMark";
    public static final String EVENT_TABLE_RDBMS_CREATE_TABLE = "createTable";
    public static final String EVENT_TABLE_RDBMS_INSERT_DATA = "insertDataToTable";
    public static final String EVENT_TABLE_RDBMS_TABLE_EXIST = "isTableExist";
    public static final String EVENT_TABLE_GENERIC_RDBMS_EQUAL = "equal";
    public static final String EVENT_TABLE_GENERIC_RDBMS_GREATER_THAN = "greaterThan";
    public static final String EVENT_TABLE_GENERIC_RDBMS_GREATER_THAN_EQUAL = "greaterThanEqual";
    public static final String EVENT_TABLE_GENERIC_RDBMS_LESS_THAN = "lessThan";
    public static final String EVENT_TABLE_GENERIC_RDBMS_LESS_THAN_EQUAL = "lessThanEqual";
    public static final String EVENT_TABLE_GENERIC_RDBMS_NOT_EQUAL = "notEqual";
    public static final String EVENT_TABLE_GENERIC_RDBMS_AND = "and";
    public static final String EVENT_TABLE_GENERIC_RDBMS_OR = "or";
    public static final String EVENT_TABLE_GENERIC_RDBMS_UPDATE_TABLE = "updateTableRow";
    public static final String EVENT_TABLE_GENERIC_RDBMS_DELETE_TABLE = "deleteTableRow";
    public static final String EVENT_TABLE_GENERIC_RDBMS_SELECT_TABLE = "selectTableRow";
    public static final String EVENT_TABLE_GENERIC_RDBMS_LIMIT_SELECT_TABLE = "limitSelectRow";
    public static final String EVENT_TABLE_GENERIC_RDBMS_TABLE_ROW_EXIST = "isTableRowExist";
    public static final String EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER = " ";

    public static final String ANNOTATION_ELEMENT_DATASOURCE_NAME = "datasource.name";
    public static final String ANNOTATION_ELEMENT_TABLE_NAME = "table.name";
    public static final String ANNOTATION_ELEMENT_CACHE = "cache";
    public static final String ANNOTATION_ELEMENT_CACHE_SIZE = "cache.size";
    public static final String ANNOTATION_ELEMENT_CACHE_LOADING = "cache.loading";
    public static final String ANNOTATION_ELEMENT_CACHE_VALIDITY_PERIOD = "cache.validity";
    public static final String ANNOTATION_ELEMENT_BLOOM_VALIDITY_PERIOD = "bloom.validity";
    public static final String EAGER_CACHE_LOADING_ELEMENT = "eager";

    public static final String ANNOTATION_ELEMENT_BLOOM_FILTERS = "bloom.filters";
    public static final String RDBMS_TABLE_CONFIG_FILE = "rdbms-table-config.xml";


    public static final String  EVENT_TABLE_RDBMS_TABLE_JDBC_URL = "jdbc.url";
    public static final String  EVENT_TABLE_RDBMS_TABLE_USERNAME = "username";
    public static final String  EVENT_TABLE_RDBMS_TABLE_PASSWORD = "password";
    public static final String  EVENT_TABLE_RDBMS_TABLE_DRIVER_NAME = "driver.name";

    public static final String ANNOTATION_ELEMENT_BLOOM_FILTERS_SIZE = "bloom.filters.size";
    public static final String ANNOTATION_ELEMENT_BLOOM_FILTERS_HASH = "bloom.filters.hash";

    public static final int BLOOM_FILTER_SIZE = 10000;
    public static final int BLOOM_FILTER_HASH_FUNCTIONS = 4;

    public static final String ANNOTATION_CONNECTION = "connection";


    public static final String BOOLEAN_LITERAL_TRUE = "TRUE";
    public static final String BOOLEAN_LITERAL_FALSE = "FALSE";
}
