/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
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
    public static final String EVENT_TABLE_RDBMS_BOOLEAN = "boolean";
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
    public static final String EVENT_TABLE_GENERIC_RDBMS_TABLE_ROW_EXIST = "isTableRowExist";
    public static final String EVENT_TABLE_CONDITION_WHITE_SPACE_CHARACTER = " ";


}
