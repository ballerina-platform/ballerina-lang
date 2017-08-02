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
import org.wso2.siddhi.extension.table.rdbms.RDBMSCompiledExpression;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfiguration;
import org.wso2.siddhi.extension.table.rdbms.config.RDBMSQueryConfigurationEntry;
import org.wso2.siddhi.extension.table.rdbms.exception.RDBMSTableException;
import org.wso2.siddhi.query.api.annotation.Annotation;
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

import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.DATABASE_PRODUCT_NAME;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.PLACEHOLDER_CONDITION;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.RDBMS_QUERY_CONFIG_FILE;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.SQL_WHERE;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.VERSION;
import static org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableConstants.WHITESPACE;


/**
 * Class which holds the utility methods which are used by various units in the RDBMS Event Table implementation.
 */
public class RDBMSTableUtils {

    private static RDBMSConfigurationMapper mapper;

    private RDBMSTableUtils() {
        //preventing initialization
    }

    /**
     * Utility method which can be used to check if a given string instance is null or empty.
     *
     * @param field the string instance to be checked.
     * @return true if the field is null or empty.
     */
    public static boolean isEmpty(String field) {
        return (field == null || field.trim().length() == 0);
    }

    /**
     * Method which can be used to clear up and ephemeral SQL connectivity artifacts.
     *
     * @param rs   {@link ResultSet} instance (can be null)
     * @param stmt {@link PreparedStatement} instance (can be null)
     * @param conn {@link Connection} instance (can be null)
     */
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

    /**
     * Method which is used to roll back a DB connection (e.g. in case of any errors)
     *
     * @param conn the {@link Connection} instance to be rolled back (can be null).
     */
    public static void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }

    /**
     * Util method used throughout the RDBMS Event Table implementation which accepts a compiled condition (from
     * compile-time) and uses values from the runtime to populate the given {@link PreparedStatement}.
     *
     * @param stmt                  the {@link PreparedStatement} instance which has already been build with '?'
     *                              parameters to be filled.
     * @param compiledCondition     the compiled condition which was built during compile time and now is being provided
     *                              by the Siddhi runtime.
     * @param conditionParameterMap the map which contains the runtime value(s) for the condition.
     * @param seed                  the integer factor by which the seed count will be incremented when populating
     *                              the {@link PreparedStatement}.
     * @throws SQLException in the unlikely case where there are errors when setting values to the statement
     *                      (e.g. type mismatches)
     */
    public static void resolveCondition(PreparedStatement stmt, RDBMSCompiledExpression compiledCondition,
                                        Map<String, Object> conditionParameterMap, int seed) throws SQLException {
        SortedMap<Integer, Object> parameters = compiledCondition.getParameters();
        for (Map.Entry<Integer, Object> entry : parameters.entrySet()) {
            Object parameter = entry.getValue();
            if (parameter instanceof Constant) {
                Constant constant = (Constant) parameter;
                populateStatementWithSingleElement(stmt, entry.getKey() + seed, constant.getType(),
                        constant.getValue());
            } else {
                Attribute variable = (Attribute) parameter;
                populateStatementWithSingleElement(stmt, entry.getKey() + seed, variable.getType(),
                        conditionParameterMap.get(variable.getName()));
            }
        }
    }

    /**
     * Util method which is used to populate a {@link PreparedStatement} instance with a single element.
     *
     * @param stmt    the statement to which the element should be set.
     * @param ordinal the ordinal of the element in the statement (its place in a potential list of places).
     * @param type    the type of the element to be set, adheres to
     *                {@link org.wso2.siddhi.query.api.definition.Attribute.Type}.
     * @param value   the value of the element.
     * @throws SQLException if there are issues when the element is being set.
     */
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

    /**
     * Util method which validates the elements from an annotation to verify that they comply to the accepted standards.
     *
     * @param annotation the annotation to be validated.
     */
    public static void validateAnnotation(Annotation annotation) {
        if (annotation == null) {
            return;
        }
        List<Element> elements = annotation.getElements();
        for (Element element : elements) {
            if (isEmpty(element.getValue())) {
                throw new RDBMSTableException("Annotation '" + annotation.getName() + "' contains illegal value(s). " +
                        "Please check your query and try again.");
            }
        }
    }

    /**
     * Util method used to convert a list of elements in an annotation to a comma-separated string.
     *
     * @param elements the list of annotation elements.
     * @return a comma-separated string of all elements in the list.
     */
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

    /**
     * Read and return all string field lengths given in an RDBMS event table definition.
     *
     * @param fieldInfo the field length annotation from the "@Store" definition (can be empty).
     * @return a map of fields and their specified sizes.
     */
    public static Map<String, String> processFieldLengths(String fieldInfo) {
        Map<String, String> fieldLengths = new HashMap<>();
        List<String[]> processedLengths = processKeyValuePairs(fieldInfo);
        processedLengths.forEach(field -> fieldLengths.put(field[0], field[1]));
        return fieldLengths;
    }

    /**
     * Converts a flat string of key/value pairs (e.g. from an annotation) into a list of pairs.
     * Used String[] since Java does not offer tuples.
     *
     * @param annotationString the comma-separated string of key/value pairs.
     * @return a list processed and validated pairs.
     */
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

    /**
     * Method for replacing the placeholder for conditions with the SQL Where clause and the actual condition.
     *
     * @param query     the SQL query in string format, with the "{{CONDITION}}" placeholder present.
     * @param condition the actual condition (originating from the ConditionVisitor).
     * @return the formatted string.
     */
    public static String formatQueryWithCondition(String query, String condition) {
        return query.replace(PLACEHOLDER_CONDITION, SQL_WHERE + WHITESPACE + condition);
    }

    /**
     * Utility method used for looking up DB metadata information from a given datasource.
     *
     * @param ds the datasource from which the metadata needs to be looked up.
     * @return a list of DB metadata.
     */
    public static Map<String, Object> lookupDatabaseInfo(DataSource ds) {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData dmd = conn.getMetaData();
            Map<String, Object> result = new HashMap<>();
            result.put(DATABASE_PRODUCT_NAME, dmd.getDatabaseProductName());
            result.put(VERSION, Double.parseDouble(dmd.getDatabaseMajorVersion() + "."
                    + dmd.getDatabaseMinorVersion()));
            return result;
        } catch (SQLException e) {
            throw new RDBMSTableException("Error in looking up database type: " + e.getMessage(), e);
        } finally {
            cleanupConnection(null, null, conn);
        }
    }

    /**
     * Checks and returns an instance of the RDBMS query configuration mapper.
     *
     * @return an instance of {@link RDBMSConfigurationMapper}.
     * @throws CannotLoadConfigurationException if the configuration cannot be loaded.
     */
    private static RDBMSConfigurationMapper loadRDBMSConfigurationMapper() throws CannotLoadConfigurationException {
        if (mapper == null) {
            synchronized (RDBMSTableUtils.class) {
                if (mapper == null) {
                    RDBMSQueryConfiguration config = loadQueryConfiguration();
                    mapper = new RDBMSConfigurationMapper(config);
                }
            }
        }
        return mapper;
    }

    /**
     * Isolates a particular RDBMS query configuration entry which matches the retrieved DB metadata.
     *
     * @param ds the datasource against which the entry should be matched.
     * @return the matching RDBMS query configuration entry.
     * @throws CannotLoadConfigurationException if the configuration cannot be loaded.
     */
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

    /**
     * Utility method which loads the query configuration from file.
     *
     * @return the loaded query configuration.
     * @throws CannotLoadConfigurationException if the configuration cannot be loaded.
     */
    private static RDBMSQueryConfiguration loadQueryConfiguration() throws CannotLoadConfigurationException {
        return new RDBMSTableConfigLoader().loadConfiguration();
    }

    /**
     * Child class with a method for loading the JAXB configuration mappings
     */
    private static class RDBMSTableConfigLoader {

        /**
         * Method for loading the configuration mappings.
         *
         * @return an instance of {@link RDBMSQueryConfiguration}.
         * @throws CannotLoadConfigurationException if the config cannot me loaded.
         */
        private RDBMSQueryConfiguration loadConfiguration() throws CannotLoadConfigurationException {
            try {
                JAXBContext ctx = JAXBContext.newInstance(RDBMSQueryConfiguration.class);
                Unmarshaller unmarshaller = ctx.createUnmarshaller();
                ClassLoader classLoader = getClass().getClassLoader();
                InputStream inputStream = classLoader.getResourceAsStream(RDBMS_QUERY_CONFIG_FILE);
                if (inputStream == null) {
                    throw new CannotLoadConfigurationException(RDBMS_QUERY_CONFIG_FILE
                            + " is not found in the classpath");
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
            if (dbResults.isEmpty()) {
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
