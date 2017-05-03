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

import com.google.common.collect.Maps;
import org.wso2.siddhi.core.exception.CannotLoadConfigurationException;
import org.wso2.siddhi.extension.table.rdbms.RDBMSCompiledCondition;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfiguration;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.*;

public class RDBMSTableUtils {

    private static RDBMSConfigurationMapper mapper;

    private RDBMSTableUtils() {
    }

    public static Map<String, Object> lookupDatabaseInfo(DataSource ds) throws CannotLoadConfigurationException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData dmd = conn.getMetaData();
            Map<String, Object> result = new HashMap<>();
            result.put(DATABASE_PRODUCT_NAME, dmd.getDatabaseProductName());
            result.put(VERSION, Double.parseDouble(dmd.getDatabaseMajorVersion() + "." + dmd.getDatabaseMinorVersion()));
            return result;
        } catch (SQLException e) {
            throw new CannotLoadConfigurationException("Error in looking up database type: " + e.getMessage(), e);
        } finally {
            cleanupConnection(null, null, conn);
        }
    }

    private static RDBMSConfigurationMapper loadRDBMSConfigurationMapper() throws CannotLoadConfigurationException {
        if (mapper == null) {
            RDBMSQueryConfiguration config = loadQueryConfiguration();
            mapper = new RDBMSConfigurationMapper(config);
        }
        return mapper;
    }

    public static RDBMSQueryConfigurationEntry lookupCurrentQueryConfigurationEntry(
            DataSource ds) throws CannotLoadConfigurationException {
        Map<String, Object> dbInfo = lookupDatabaseInfo(ds);
        RDBMSConfigurationMapper mapper = loadRDBMSConfigurationMapper();
        RDBMSQueryConfigurationEntry entry = mapper.lookupEntry((String) dbInfo.get(DATABASE_PRODUCT_NAME),
                (double) dbInfo.get(VERSION));
        if (entry != null) {
            return entry;
        } else {
            throw new CannotLoadConfigurationException("Cannot find a database section in the RDBMS "
                    + "configuration for the database: " + dbInfo);
        }
    }

    private static RDBMSQueryConfiguration loadQueryConfiguration() throws CannotLoadConfigurationException {
        return new RDBMSTableConfigLoader().loadConfiguration();
    }

    public static boolean isEmpty(String field) {
        return (field == null || field.trim().length() == 0);
    }

    public static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }

    public static void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }

    public static void resolveCondition(PreparedStatement stmt, RDBMSCompiledCondition compiledCondition,
                                        Map<String, Object> parameterMap, int seed) throws SQLException {
        SortedMap<Integer, Object> parameters = compiledCondition.getParameters();
        for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
            Object parameter = entry.getValue();
            if (parameter instanceof Constant) {
                Constant constant = (Constant) parameter;
                populateStatementWithSingleElement(stmt, seed + entry.getKey(), constant.getType(),
                        constant.getValue());
            } else {
                Attribute variable = (Attribute) parameter;
                populateStatementWithSingleElement(stmt, seed + entry.getKey(), variable.getType(),
                        parameterMap.get(variable.getName()));
            }
        }
    }

    public static void populateStatementWithSingleElement(PreparedStatement stmt, int ordinal, Attribute.Type type,
                                                          Object value) throws SQLException {
        switch (type) {
            case BOOL:
                stmt.setBoolean(ordinal, (Boolean) value);
                break;
            case DOUBLE:
                stmt.setDouble(ordinal, (Double) value);
                break;
            case FLOAT:
                stmt.setFloat(ordinal, (Float) value);
                break;
            case INT:
                stmt.setInt(ordinal, (Integer) value);
                break;
            case LONG:
                stmt.setLong(ordinal, (Long) value);
                break;
            case OBJECT:
                stmt.setObject(ordinal, value);
                break;
            case STRING:
                stmt.setString(ordinal, (String) value);
                break;
        }
    }

    public static String flattenAnnotatedElements(List<Element> elements) {
        StringBuilder sb = new StringBuilder();
        elements.forEach(elem -> {
            sb.append(elem.getValue());
            if (elements.indexOf(elem) != elements.size() - 1) {
                sb.append(RDBMSTableConstants.SEPARATOR);
            }
        });
        return sb.toString();
    }

    public static Map<String, String> processFieldLengths(String fieldInfo) {
        Map<String, String> fieldLengths = new HashMap<>();
        List<String[]> processedLengths = processKeyValuePairs(fieldInfo);
        processedLengths.forEach(field -> fieldLengths.put(field[0], field[1]));
        return fieldLengths;
    }

    public static List<String[]> processKeyValuePairs(String annotationString) {
        List<String[]> keyValuePairs = new ArrayList<>();
        if (!isEmpty(annotationString)) {
            String[] pairs = annotationString.split(",");
            for (String element : pairs) {
                if (!element.contains(":")) {
                    throw new RDBMSTableException("Property '" + element + "' does not adhere to the expected " +
                            "format: a property must be a key-value pair separated by a colon (:)");
                }
                String[] pair = element.split(":");
                if (pair.length != 2) {
                    throw new RDBMSTableException("Property '" + pair[0] + "' does not adhere to the expected " +
                            "format: a property must be a key-value pair separated by a colon (:)");
                } else {
                    keyValuePairs.add(pair);
                }
            }
        }
        return keyValuePairs;
    }

    public static String formatQueryWithCondition(String query, String condition) {
        return query.replace(PLACEHOLDER_CONDITION, SQL_WHERE + WHITESPACE + condition);
    }

    private static class RDBMSTableConfigLoader {

        private RDBMSQueryConfiguration loadConfiguration() throws CannotLoadConfigurationException {
            try {
                JAXBContext ctx = JAXBContext.newInstance(RDBMSQueryConfiguration.class);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(RDBMS_QUERY_CONFIG_FILE);
                if (inputStream == null) {
                    throw new CannotLoadConfigurationException(RDBMS_QUERY_CONFIG_FILE + " is not found in the classpath");
                }
                return (RDBMSQueryConfiguration) unmarshaller.unmarshal(inputStream);
            } catch (JAXBException e) {
                throw new CannotLoadConfigurationException(
                        "Error in processing RDBMS query configuration: " + e.getMessage(), e);
            }
        }

    }

    /**
     * RDBMS configuration mapping class to be used to lookup matching configuration entry with a data source.
     */
    private static class RDBMSConfigurationMapper {

        private List<Map.Entry<Pattern, RDBMSQueryConfigurationEntry>> entries = new ArrayList<>();

        public RDBMSConfigurationMapper(RDBMSQueryConfiguration config) {
            for (RDBMSQueryConfigurationEntry entry : config.getDatabases()) {
                this.entries.add(Maps.immutableEntry(Pattern.compile(entry.getDatabaseName().toLowerCase()), entry));
            }
        }

        private boolean checkVersion(RDBMSQueryConfigurationEntry entry, double version) {
            double minVersion = entry.getMinVersion();
            double maxVersion = entry.getMaxVersion();
            //Keeping things readable
            if (minVersion != 0 && version < minVersion) {
                return false;
            }
            if (maxVersion != 0 && version > maxVersion) {
                return false;
            }
            return true;
        }

        private List<RDBMSQueryConfigurationEntry> extractMatchingConfigEntries(String dbName) {
            List<RDBMSQueryConfigurationEntry> result = new ArrayList<>();
            this.entries.forEach(entry -> {
                if (entry.getKey().matcher(dbName).find()) {
                    result.add(entry.getValue());
                }
            });
            return result;
        }

        public RDBMSQueryConfigurationEntry lookupEntry(String dbName, double version) {
            List<RDBMSQueryConfigurationEntry> dbResults = this.extractMatchingConfigEntries(dbName.toLowerCase());
            if (dbResults == null || dbResults.isEmpty()) {
                return null;
            }
            List<RDBMSQueryConfigurationEntry> versionResults = new ArrayList<>();
            for (RDBMSQueryConfigurationEntry entry : dbResults) {
                if (this.checkVersion(entry, version)) {
                    versionResults.add(entry);
                }
            }
            if (versionResults.isEmpty()) {
                return null;
            }
            return versionResults.get(0);
        }
    }
}
