/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.siddhi.extension.table.rdbms.util;

public class RDBMSTableConstants {

    public static final String VERSION = "Version";
    public static final String DATABASE_PRODUCT_NAME = "Database Product Name";

    public static final String RDBMS_QUERY_CONFIG_FILE = "rdbms-config.xml";
    public static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";
    public static final String COLUMNS_PLACEHOLDER = "{{COLUMNS, PRIMARY_KEYS}}";
    public static final String CONDITION_PLACEHOLDER = "{{CONDITION}}";
    public static final String WHITESPACE = " ";
    public static final String COMMA = ",";
    public static final String EQUALS = "=";
    public static final String OPEN_PARENTHESIS = "(";
    public static final String CLOSE_PARENTHESIS = ")";
    public static final String PRIMARY_KEY_DEF = "PRIMARY KEY";
    public static final String SQL_WHERE = "WHERE";
    //Annotation field names
    public static String ANNOTATION_ELEMENT_URL = "jdbc.url";
    public static String ANNOTATION_ELEMENT_USERNAME = "username";
    public static String ANNOTATION_ELEMENT_PASSWORD = "password";
    public static String ANNOTATION_ELEMENT_TABLE_NAME = "table.name";
    public static String ANNOTATION_ELEMENT_FIELD_LENGTHS = "field.length";
    public static String ANNOTATION_ELEMENT_POOL_PROPERTIES = "pool.properties";
    public static String ANNOTATION_ELEMENT_JNDI_RESOURCE = "jndi.resource";

    private RDBMSTableConstants() {

    }

}
