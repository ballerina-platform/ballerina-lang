/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.ballerinalang.database.sql.statement;

import org.ballerinalang.database.sql.Constants;
import org.ballerinalang.database.sql.SQLDataIterator;
import org.ballerinalang.database.sql.SQLDatasource;
import org.ballerinalang.database.sql.exceptions.ApplicationException;
import org.ballerinalang.database.sql.exceptions.DatabaseException;
import org.ballerinalang.database.table.BCursorTable;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BField;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.DecimalValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static org.ballerinalang.database.sql.Constants.PARAMETER_DIRECTION_FIELD;
import static org.ballerinalang.database.sql.Constants.PARAMETER_SQL_TYPE_FIELD;
import static org.ballerinalang.database.sql.Constants.PARAMETER_VALUE_FIELD;

/**
 * Represent an abstract SQL statement. Contains the common functionality for any SQL statement.
 *
 * @since 1.0.0
 */
public abstract class AbstractSQLStatement implements SQLStatement {
    protected Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_UTC));
    private static final String POSTGRES_DOUBLE = "float8";
    private static final int ORACLE_CURSOR_TYPE = -10;

    protected ArrayValue constructParameters(ArrayValue parameters) throws ApplicationException {
        ArrayValue parametersNew = new ArrayValue();
        int paramCount = parameters.size();
        for (int i = 0; i < paramCount; ++i) {
            Object value = parameters.getValue(i);
            MapValue<String, Object> paramRecord;
            BType type = TypeChecker.getType(value);
            if (type.getTag() == TypeTags.OBJECT_TYPE_TAG
                    || type.getTag() == TypeTags.RECORD_TYPE_TAG) {
                paramRecord = (MapValue<String, Object>) value;
            } else {
                paramRecord = getSQLParameter();
                paramRecord.put(PARAMETER_VALUE_FIELD, value);
                paramRecord.put(PARAMETER_DIRECTION_FIELD, Constants.QueryParamDirection.DIR_IN);
                paramRecord.put(PARAMETER_SQL_TYPE_FIELD, getSQLType(value));
            }
            parametersNew.add(i, paramRecord);
        }
        return parametersNew;
    }

    /**
     * If there are any arrays of parameter for types other than sql array, the given query is expanded by adding "?" s
     * to match with the array size.
     *
     * @param query The query to be processed
     * @param parameters Array of parameters belonging to the query
     *
     * @return The string with "?" place holders for parameters
     */
    protected String createProcessedQueryString(String query, ArrayValue parameters) {
        String currentQuery = query;
        if (parameters != null) {
            int start = 0;
            Object[] vals;
            int count;
            int paramCount = parameters.size();
            for (int i = 0; i < paramCount; i++) {
                // types.bal Parameter
                MapValue<String, Object> paramValue = (MapValue<String, Object>) parameters.getRefValue(i);
                if (paramValue != null) {
                    String sqlType = getSQLType(paramValue);
                    Object value = paramValue.get(PARAMETER_VALUE_FIELD);
                    BType type = TypeChecker.getType(value);
                    if (value != null && (type.getTag() == TypeTags.ARRAY_TAG
                            && ((BArrayType) type).getElementType().getTag()
                            != TypeTags.BYTE_TAG)
                            && !Constants.SQLDataTypes.ARRAY.equalsIgnoreCase(sqlType)) {
                        count = ((ArrayValue) value).size();
                    } else {
                        count = 1;
                    }
                    vals = expandQuery(start, count, currentQuery);
                    start = (Integer) vals[0];
                    currentQuery = (String) vals[1];
                }
            }
        }
        return currentQuery;
    }

    protected TableValue constructTable(TableResourceManager rm, ResultSet rs, BStructureType structType,
            List<ColumnDefinition> columnDefinitions, String databaseProductName) {
        BStructureType tableConstraint = structType;
        if (structType == null) {
            tableConstraint = new BRecordType("$table$anon$constraint$",
                    new BPackage(Names.BUILTIN_ORG.getValue(), Names.BUILTIN_PACKAGE.getValue(),
                            Names.DEFAULT_VERSION.getValue()), 0, false);
            ((BRecordType) tableConstraint).restFieldType = BTypes.typeAnydata;
        }
        return new BCursorTable(
                new SQLDataIterator(rm, rs, utcCalendar, columnDefinitions, structType, databaseProductName),
                tableConstraint);
    }

    protected int getParameterDirection(MapValue<String, Object> parameter) {
        int direction = 0;
        String directionInput = parameter.getStringValue(PARAMETER_DIRECTION_FIELD);
        if (directionInput != null) {
            switch (directionInput) {
            case Constants.QueryParamDirection.DIR_OUT:
                direction = Constants.QueryParamDirection.OUT;
                break;
            case Constants.QueryParamDirection.DIR_INOUT:
                direction = Constants.QueryParamDirection.INOUT;
                break;
            default:
                direction = Constants.QueryParamDirection.IN;
                break;
            }
        }
        return direction;
    }

    protected List<ColumnDefinition> getColumnDefinitions(ResultSet rs) throws SQLException {
        List<ColumnDefinition> columnDefs = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int cols = rsMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String colName = rsMetaData.getColumnLabel(i);
            if (columnNames.contains(colName)) {
                String tableName = rsMetaData.getTableName(i).toUpperCase(Locale.getDefault());
                colName = tableName + "." + colName;
            }
            int colType = rsMetaData.getColumnType(i);
            int mappedType = getColumnType(colType);
            columnDefs.add(new SQLDataIterator.SQLColumnDefinition(colName, mappedType, colType));
            columnNames.add(colName);
        }
        return columnDefs;
    }

    /**
     * This method will return equal ballerina data type for SQL type.
     *
     * @param sqlType SQL type in column
     * @return TypeKind that represent respective ballerina type.
     */
    private int getColumnType(int sqlType) {
        switch (sqlType) {
            case Types.ARRAY:
                return TypeTags.ARRAY_TAG;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
            case Types.ROWID:
                return TypeTags.STRING_TAG;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return TypeTags.INT_TAG;
            case Types.BIT:
            case Types.BOOLEAN:
                return TypeTags.BOOLEAN_TAG;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return TypeTags.DECIMAL_TAG;
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return TypeTags.FLOAT_TAG;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return TypeTags.BYTE_ARRAY_TAG;
            case Types.STRUCT:
                return TypeTags.RECORD_TYPE_TAG;
            default:
                return TypeTags.NONE_TAG;
        }
    }

    private String getSQLType(Object value) throws ApplicationException {
        BType type = TypeChecker.getType(value);
        int tag = type.getTag();
        switch (tag) {
        case TypeTags.INT_TAG:
            return Constants.SQLDataTypes.BIGINT;
        case TypeTags.STRING_TAG:
            return Constants.SQLDataTypes.VARCHAR;
        case TypeTags.FLOAT_TAG:
            return Constants.SQLDataTypes.DOUBLE;
        case TypeTags.BOOLEAN_TAG:
            return Constants.SQLDataTypes.BOOLEAN;
        case TypeTags.DECIMAL_TAG:
            return Constants.SQLDataTypes.DECIMAL;
        case TypeTags.ARRAY_TAG:
            if (((BArrayType) type).getElementType().getTag() == TypeTags.BYTE_TAG) {
                return Constants.SQLDataTypes.BINARY;
            } else {
                throw new ApplicationException("Array data type as direct value is supported only " +
                        "with byte type elements, use sql:Parameter " + type.getName());
            }
        default:
            throw new ApplicationException(
                    "unsupported data type as direct value for sql operation, use sql:Parameter: " + type.getName());
        }
    }

    private MapValue<String, Object> getSQLParameter() {
        return BallerinaValues.createRecordValue(Constants.SQL_PACKAGE_PATH, Constants.SQL_PARAMETER);
    }

    protected String getSQLType(MapValue<String, Object> parameter) {
        String sqlType = "";
        Object sqlTypeValue = parameter.get(PARAMETER_SQL_TYPE_FIELD);
        if (sqlTypeValue != null) {
            sqlType = (String) sqlTypeValue;
        }
        return sqlType;
    }

   /* protected void checkAndObserveSQLAction(Context context, SQLDatasource datasource, String query) {
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);
        observerContext.ifPresent(ctx -> {
            ctx.addTag(TAG_KEY_PEER_ADDRESS, datasource.getPeerAddress());
            ctx.addTag(TAG_KEY_DB_INSTANCE, datasource.getDatabaseName());
            ctx.addTag(TAG_KEY_DB_STATEMENT, query);
            ctx.addTag(TAG_KEY_DB_TYPE, TAG_DB_TYPE_SQL);
        });
    }

    protected void checkAndObserveSQLError(Context context, String message) {
        Optional<ObserverContext> observerContext = ObserveUtils.getObserverContextOfCurrentFrame(context);
        observerContext.ifPresent(ctx -> {
            ctx.addProperty(ObservabilityConstants.PROPERTY_ERROR, Boolean.TRUE);
            ctx.addProperty(ObservabilityConstants.PROPERTY_ERROR_MESSAGE, message);
        });
    }
*/

    protected void createProcessedStatement(Connection conn, PreparedStatement stmt, ArrayValue params,
            String databaseProductName) throws ApplicationException, DatabaseException {
        if (params == null) {
            return;
        }
        int paramCount = params.size();
        int currentOrdinal = 0;
        for (int index = 0; index < paramCount; index++) {
            MapValue<String, Object> paramRecord = (MapValue<String, Object>) params.getRefValue(index);
            if (paramRecord != null) {
                String sqlType = getSQLType(paramRecord);
                Object value = paramRecord.get(PARAMETER_VALUE_FIELD);
                BType type = TypeChecker.getType(value);
                int direction = getParameterDirection(paramRecord);
                //If the parameter is an array and sql type is not "array" then treat it as an array of parameters
                if (value != null && (type.getTag() == TypeTags.ARRAY_TAG
                        && ((BArrayType) type).getElementType().getTag() != TypeTags.BYTE_TAG)
                        && !Constants.SQLDataTypes.ARRAY.equalsIgnoreCase(sqlType)) {
                    int arrayLength = ((ArrayValue) value).size();
                    int typeTagOfArrayElement = ((BArrayType) type).getElementType().getTag();
                    for (int i = 0; i < arrayLength; i++) {
                        Object paramValue;
                        switch (typeTagOfArrayElement) {
                        case TypeTags.INT_TAG:
                        case TypeTags.BYTE_TAG:
                        case TypeTags.FLOAT_TAG:
                        case TypeTags.STRING_TAG:
                        case TypeTags.DECIMAL_TAG:
                        case TypeTags.BOOLEAN_TAG:
                            paramValue = ((ArrayValue) value).getValue(i);
                            break;
                        // The value parameter of the struct is an array of arrays. Only possibility that should be
                        // supported is, this being an array of byte arrays (blob)
                        // eg: [blob1, blob2, blob3] == [byteArray1, byteArray2, byteArray3]
                        case TypeTags.ARRAY_TAG:
                            Object array = ((ArrayValue) value).getValue(i);
                            BType arrayElementType = TypeChecker.getType(array);
                            // array cannot be null because the type tag is not union
                            if (((BArrayType) arrayElementType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                                paramValue = array;
                                break;
                            } else {
                                throw new ApplicationException("unsupported array type for parameter index: " + index
                                        + ". Array element type being an array is supported only when the inner array"
                                        + " element type is BYTE");
                            }
                        default:
                            throw new ApplicationException("unsupported array type for parameter index " + index);
                        }
                        if (Constants.SQLDataTypes.REFCURSOR.equals(sqlType) || Constants.SQLDataTypes.BLOB
                                .equals(sqlType)) {
                            setParameter(conn, stmt, sqlType, paramValue, direction, currentOrdinal,
                                    databaseProductName);
                        } else {
                            setParameter(conn, stmt, sqlType, paramValue, direction, currentOrdinal);
                        }
                        currentOrdinal++;
                    }
                } else {
                    if (Constants.SQLDataTypes.REFCURSOR.equals(sqlType) || Constants.SQLDataTypes.ARRAY
                            .equals(sqlType) || Constants.SQLDataTypes.BLOB.equals(sqlType)) {
                        setParameter(conn, stmt, sqlType, value, direction, currentOrdinal, databaseProductName);
                    } else {
                        setParameter(conn, stmt, sqlType, value, direction, currentOrdinal);
                    }
                    currentOrdinal++;
                }
            } else {
                setNullObject(stmt, index);
                currentOrdinal++;
            }
        }
    }


    /**
     * This will close database connection and statement.
     *
     * @param stmt SQL statement
     * @param conn SQL connection
     * @param connectionClosable Whether the connection is closable or not. If the connection is not closable this
     * method will not release the connection. Therefore to avoid connection leaks it should have been taken care
     * of externally.
     */
    protected void cleanupResources(Statement stmt, Connection conn, boolean connectionClosable) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
            if (conn != null && !conn.isClosed() && connectionClosable) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new BallerinaException("error in cleaning sql resources: " + e.getMessage(), e);
        }
    }

    /**
     * This will close database connection, statement and the resultset.
     *
     * @param rs   SQL resultset
     * @param stmt SQL statement
     * @param conn SQL connection
     * @param connectionClosable Whether the connection is closable or not. If the connection is not closable this
     * method will not release the connection. Therefore to avoid connection leaks it should have been taken care
     * of externally.
     */
    protected void cleanupResources(ResultSet rs, Statement stmt, Connection conn, boolean connectionClosable) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            cleanupResources(stmt, conn, connectionClosable);
        } catch (SQLException e) {
            throw new BallerinaException("error in cleaning sql resources: " + e.getMessage(), e);
        }
    }

   /* protected void handleErrorOnTransaction(Context context) {
        TransactionLocalContext transactionLocalContext = context.getLocalTransactionInfo();
        if (transactionLocalContext == null) {
            return;
        }
        notifyTxMarkForAbort(context, transactionLocalContext);
    }
*/

    public Connection getDatabaseConnection(ObjectValue client, SQLDatasource datasource, boolean isSelectQuery)
            throws DatabaseException {
        //TODO: JBalMigration Commenting out transaction handling and observability
        //TODO: #16033
        /*Connection conn;
        boolean isInTransaction = context.isInTransaction();
        // Here when isSelectQuery condition is true i.e. in case of a select operation, we allow
        // it to use a normal database connection. This is because,
        // 1. In mysql (and possibly some other databases) another operation cannot be performed over a connection
        // which has an open result set on top of it
        // 2. But inside a transaction we use the same connection to perform all the db operation, so unless the
        // result set is fully iterated, it won't be possible to perform rest of the operations inside the transaction
        // 3. Therefore, we allow select operations to be performed on separate db connections inside transactions
        // (XA or general transactions)
        // 4. However for call operations, despite of the fact that they could output resultsets
        // (as OUT params or return values) we do not use a separate connection, because,
        // call operations can contain UPDATE actions as well inside the procedure which may require to happen in the
        // same scope as any other individual UPDATE actions
        if (!isInTransaction || isSelectQuery) {
            conn = datasource.getSQLConnection();
            return conn;
        } else {
            //This is when there is an infected transaction block. But this is not participated to the transaction
            //since the action call is outside of the transaction block.
            if (!context.getLocalTransactionInfo().hasTransactionBlock()) {
                conn = datasource.getSQLConnection();
                return conn;
            }
        }
        String connectorId = retrieveConnectorId(context);
        boolean isXAConnection = datasource.isXAConnection();
        TransactionLocalContext transactionLocalContext = context.getLocalTransactionInfo();
        String globalTxId = transactionLocalContext.getGlobalTransactionId();
        String currentTxBlockId = transactionLocalContext.getCurrentTransactionBlockId();
        BallerinaTransactionContext txContext = transactionLocalContext.getTransactionContext(connectorId);
        if (txContext == null) {
            if (isXAConnection) {
                XAConnection xaConn = datasource.getXADataSource().getXAConnection();
                XAResource xaResource = xaConn.getXAResource();
                TransactionResourceManager.getInstance().beginXATransaction(globalTxId, currentTxBlockId, xaResource);
                conn = xaConn.getConnection();
                txContext = new SQLTransactionContext(conn, xaResource);
            } else {
                conn = datasource.getSQLConnection();
                conn.setAutoCommit(false);
                txContext = new SQLTransactionContext(conn);
            }
            transactionLocalContext.registerTransactionContext(connectorId, txContext);
            TransactionResourceManager.getInstance().register(globalTxId, currentTxBlockId, txContext);
        } else {
            conn = ((SQLTransactionContext) txContext).getConnection();
        }
        return conn;*/
        Connection conn;
        try {
            conn = datasource.getSQLConnection();
        } catch (SQLException e) {
            throw new DatabaseException("error in get connection: " + Constants.CONNECTOR_NAME + ": ", e);
        }
        return conn;
    }

    //TODO: #16033
   /* private String retrieveConnectorId(Context context) {
        BMap<String, BValue> bConnector = (BMap<String, BValue>) context.getRefArgument(0);
        return (String) bConnector.getNativeData(Constants.CONNECTOR_ID_KEY);
    }*/

   /* private void notifyTxMarkForAbort(Context context, TransactionLocalContext transactionLocalContext) {
        String globalTransactionId = transactionLocalContext.getGlobalTransactionId();
        String transactionBlockId = transactionLocalContext.getCurrentTransactionBlockId();

        transactionLocalContext.markFailure();
        if (transactionLocalContext.isRetryPossible(context.getStrand(), transactionBlockId)) {
            return;
        }
        TransactionUtils.notifyTransactionAbort(context.getStrand(), globalTransactionId,
                transactionBlockId);
    }*/

    private void setParameter(Connection conn, PreparedStatement stmt, String sqlType, Object value, int direction,
            int index) throws DatabaseException, ApplicationException {
        setParameter(conn, stmt, sqlType, value, direction, index, null);
    }

    private void setParameter(Connection conn, PreparedStatement stmt, String sqlType, Object value, int direction,
            int index, String databaseProductName) throws ApplicationException, DatabaseException {
        if (sqlType == null || sqlType.isEmpty()) {
            setStringValue(stmt, value, index, direction, Types.VARCHAR);
        } else {
            String sqlDataType = sqlType.toUpperCase(Locale.getDefault());
            switch (sqlDataType) {
            case Constants.SQLDataTypes.SMALLINT:
                setSmallIntValue(stmt, value, index, direction, Types.SMALLINT);
                break;
            case Constants.SQLDataTypes.VARCHAR:
                setStringValue(stmt, value, index, direction, Types.VARCHAR);
                break;
            case Constants.SQLDataTypes.CHAR:
                setStringValue(stmt, value, index, direction, Types.CHAR);
                break;
            case Constants.SQLDataTypes.LONGVARCHAR:
                setStringValue(stmt, value, index, direction, Types.LONGVARCHAR);
                break;
            case Constants.SQLDataTypes.NCHAR:
                setNStringValue(stmt, value, index, direction, Types.NCHAR);
                break;
            case Constants.SQLDataTypes.NVARCHAR:
                setNStringValue(stmt, value, index, direction, Types.NVARCHAR);
                break;
            case Constants.SQLDataTypes.LONGNVARCHAR:
                setNStringValue(stmt, value, index, direction, Types.LONGNVARCHAR);
                break;
            case Constants.SQLDataTypes.DOUBLE:
                setDoubleValue(stmt, value, index, direction, Types.DOUBLE);
                break;
            case Constants.SQLDataTypes.NUMERIC:
                setNumericValue(stmt, value, index, direction, Types.NUMERIC);
                break;
            case Constants.SQLDataTypes.DECIMAL:
                setNumericValue(stmt, value, index, direction, Types.DECIMAL);
                break;
            case Constants.SQLDataTypes.BIT:
            case Constants.SQLDataTypes.BOOLEAN:
                setBooleanValue(stmt, value, index, direction, Types.BIT);
                break;
            case Constants.SQLDataTypes.TINYINT:
                setTinyIntValue(stmt, value, index, direction, Types.TINYINT);
                break;
            case Constants.SQLDataTypes.BIGINT:
                setBigIntValue(stmt, value, index, direction, Types.BIGINT);
                break;
            case Constants.SQLDataTypes.INTEGER:
                setIntValue(stmt, value, index, direction, Types.INTEGER);
                break;
            case Constants.SQLDataTypes.REAL:
                setRealValue(stmt, value, index, direction, Types.REAL);
                break;
            case Constants.SQLDataTypes.FLOAT:
                setRealValue(stmt, value, index, direction, Types.FLOAT);
                break;
            case Constants.SQLDataTypes.DATE:
                setDateValue(stmt, value, index, direction, Types.DATE);
                break;
            case Constants.SQLDataTypes.TIMESTAMP:
            case Constants.SQLDataTypes.DATETIME:
                setTimeStampValue(stmt, value, index, direction, Types.TIMESTAMP, utcCalendar);
                break;
            case Constants.SQLDataTypes.TIME:
                setTimeValue(stmt, value, index, direction, Types.TIME, utcCalendar);
                break;
            case Constants.SQLDataTypes.BINARY:
                setBinaryValue(stmt, value, index, direction, Types.BINARY);

                break;
            case Constants.SQLDataTypes.BLOB:
                setBlobValue(stmt, value, index, direction, Types.BLOB);
                break;
            case Constants.SQLDataTypes.LONGVARBINARY:
                setBlobValue(stmt, value, index, direction, Types.LONGVARBINARY);
                break;
            case Constants.SQLDataTypes.VARBINARY:
                setBinaryValue(stmt, value, index, direction, Types.VARBINARY);
                break;
            case Constants.SQLDataTypes.CLOB:
                setClobValue(stmt, value, index, direction, Types.CLOB);
                break;
            case Constants.SQLDataTypes.NCLOB:
                setNClobValue(stmt, value, index, direction, Types.NCLOB);
                break;
            case Constants.SQLDataTypes.ARRAY:
                setArrayValue(conn, stmt, value, index, direction, Types.ARRAY, databaseProductName);
                break;
            case Constants.SQLDataTypes.STRUCT:
                setUserDefinedValue(conn, stmt, value, index, direction, Types.STRUCT);
                break;
            case Constants.SQLDataTypes.REFCURSOR:
                setRefCursorValue(stmt, index, direction, databaseProductName);
                break;
            default:
                throw new ApplicationException("unsupported datatype as parameter: " + sqlType + " index:" + index);
            }
        }
    }

    private void setIntValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws DatabaseException, ApplicationException {
        Integer val = obtainIntegerValue(value);
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setInt(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setInt(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set integer to statement: ", e);
        }
    }

    private void setSmallIntValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Integer val = obtainIntegerValue(value);
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setShort(index + 1, val.shortValue());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setShort(index + 1, val.shortValue());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set integer to statement: ", e);
        }
    }

    private void setStringValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, (String) value);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setString(index + 1, (String) value);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set string to statement: ", e);
        }
    }

    private void setNStringValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNString(index + 1, (String) value);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (value == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNString(index + 1, (String) value);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set string to statement: ", e);
        }
    }

    private void setDoubleValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Double val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.FLOAT_TAG:
                    val = (Double) value;
                    break;
                case TypeTags.INT_TAG:
                case TypeTags.BYTE_TAG:
                    val = ((Long) value).doubleValue();
                    break;
                case TypeTags.DECIMAL_TAG:
                    val = ((DecimalValue) value).value().doubleValue();
                    break;
                case TypeTags.STRING_TAG:
                    val = Double.parseDouble((String) value);
                    break;
                default:
                    throw new ApplicationException("invalid value for double: " + value.toString());

            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDouble(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDouble(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set double to statement: ", e);
        }
    }

    private void setNumericValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        BigDecimal val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.DECIMAL_TAG:
                    val = ((DecimalValue) value).value();
                    break;
                case TypeTags.INT_TAG:
                    val = BigDecimal.valueOf((Long) value);
                    break;
                case TypeTags.FLOAT_TAG:
                    val = (BigDecimal.valueOf((Double) value));
                    break;
                case TypeTags.STRING_TAG:
                    val = new BigDecimal((String) value);
                    break;
                default:
                    throw new ApplicationException("invalid value for numeric: " + value.toString());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBigDecimal(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBigDecimal(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set numeric value to statement: ", e);
        }
    }

    private void setBooleanValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Boolean val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.BOOLEAN_TAG:
                    val = (Boolean) value;
                    break;
                case TypeTags.STRING_TAG:
                    val = Boolean.valueOf((String) value);
                     break;
                 default:
                     throw new ApplicationException("invalid value for boolean: " + value.toString());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBoolean(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBoolean(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set boolean value to statement: ", e);
        }
    }

    private void setTinyIntValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Byte val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.BYTE_TAG:
                case TypeTags.INT_TAG:
                    val = ((Long) value).byteValue();
                    break;
                case TypeTags.STRING_TAG:
                    val = Byte.parseByte((String) value);
                    break;
                default:
                    throw new ApplicationException("invalid value for byte: " + value.toString());

            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setByte(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setByte(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set tinyint value to statement: ", e);
        }
    }

    private void setBigIntValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Long val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
            case TypeTags.BYTE_TAG:
            case TypeTags.INT_TAG:
                val = (Long) value;
                break;
            case TypeTags.STRING_TAG:
                val = Long.parseLong((String) value);
                break;
            default:
                throw new ApplicationException("invalid value for bigint: " + value.toString());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setLong(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setLong(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set bigint value to statement: ", e);
        }
    }

    private void setRealValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Float val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.FLOAT_TAG:
                    val = ((Double) value).floatValue();
                    break;
                case TypeTags.BYTE_TAG:
                case TypeTags.INT_TAG:
                    val =  ((Long) value).floatValue();
                    break;
                case TypeTags.STRING_TAG:
                    val = Float.parseFloat((String) value);
                    break;
                default:
                    throw new ApplicationException("invalid value for float: " + value.toString());
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setFloat(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setFloat(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter, index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set float value to statement.", e);
        }
    }

    private void setDateValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        Date val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            if (value instanceof MapValue && type.getName().equals(Constants.STRUCT_TIME) && type
                    .getPackage().toString().equals(Constants.STRUCT_TIME_PACKAGE)) {
                long timeVal = ((MapValue<String, Object>) value).getIntValue(Constants.STRUCT_TIME_FIELD);
                val = new Date(timeVal);
            } else if (type.getTag() == TypeTags.INT_TAG) {
                val = new Date((Long) value);
            } else if (type.getTag() == TypeTags.STRING_TAG) {
                val = convertToDate((String) value);
            } else {
                throw new ApplicationException("invalid input type for date parameter with index: " + index);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDate(index + 1, val);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setDate(index + 1, val);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set date value to statement: ", e);
        }
    }

    private Date convertToDate(String source) throws ApplicationException {
        // the lexical form of the date is '-'? yyyy '-' mm '-' dd zzzzzz?
        if ((source == null) || source.trim().equals("")) {
            return null;
        }
        source = source.trim();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        if (source.startsWith("-")) {
            source = source.substring(1);
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }
        if (source.length() >= 10) {
            if ((source.charAt(4) != '-') || (source.charAt(7) != '-')) {
                throw new ApplicationException("invalid date format: " + source);
            }
            int year = Integer.parseInt(source.substring(0, 4));
            int month = Integer.parseInt(source.substring(5, 7));
            int day = Integer.parseInt(source.substring(8, 10));
            int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
            if (source.length() > 10) {
                String restpart = source.substring(10);
                timeZoneOffSet = getTimeZoneOffset(restpart);
                calendar.set(Calendar.DST_OFFSET, 0); //set the day light offset only if the time zone is present
            }
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
        } else {
            throw new ApplicationException("invalid date string to parse: " + source);
        }
        return new Date(calendar.getTime().getTime());
    }

    private void setTimeStampValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType,
            Calendar utcCalendar) throws ApplicationException, DatabaseException {
        Timestamp val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            if (value instanceof MapValue && type.getName().equals(Constants.STRUCT_TIME) && type
                    .getPackage().toString().equals(Constants.STRUCT_TIME_PACKAGE)) {
                Object timeVal = ((MapValue<String, Object>) value).get(Constants.STRUCT_TIME_FIELD);
                long time = (Long) timeVal;
                val = new Timestamp(time);
            } else if (value instanceof Long) {
                val = new Timestamp((Long) value);
            } else if (value instanceof String) {
                val = convertToTimeStamp((String) value);
            } else {
                throw new ApplicationException("invalid input type for timestamp parameter with index: " + index);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTimestamp(index + 1, val, utcCalendar);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTimestamp(index + 1, val, utcCalendar);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter, index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set timestamp value to statement: ", e);
        }
    }

    private Timestamp convertToTimeStamp(String source) throws ApplicationException {
        //lexical representation of the date time is '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        if ((source == null) || source.trim().equals("")) {
            return null;
        }
        source = source.trim();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        if (source.startsWith("-")) {
            source = source.substring(1);
            calendar.set(Calendar.ERA, GregorianCalendar.BC);
        }
        if (source.length() >= 19) {
            if ((source.charAt(4) != '-') || (source.charAt(7) != '-') || (source.charAt(10) != 'T') || (
                    source.charAt(13) != ':') || (source.charAt(16) != ':')) {
                throw new ApplicationException("invalid datetime format: " + source);
            }
            int year = Integer.parseInt(source.substring(0, 4));
            int month = Integer.parseInt(source.substring(5, 7));
            int day = Integer.parseInt(source.substring(8, 10));
            int hour = Integer.parseInt(source.substring(11, 13));
            int minite = Integer.parseInt(source.substring(14, 16));
            int second = Integer.parseInt(source.substring(17, 19));
            long miliSecond = 0;
            int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
            if (source.length() > 19) {
                String rest = source.substring(19);
                int[] offsetData = getTimeZoneWithMilliSeconds(rest);
                miliSecond = offsetData[0];
                int calculatedtimeZoneOffSet = offsetData[1];
                if (calculatedtimeZoneOffSet != -1) {
                    timeZoneOffSet = calculatedtimeZoneOffSet;
                }
                calendar.set(Calendar.DST_OFFSET, 0); //set the day light offset only if the time zone is present
            }
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minite);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, (int) miliSecond);
            calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
        } else {
            throw new ApplicationException("datetime string can not be less than 19 characters: " + source);
        }
        return new Timestamp(calendar.getTimeInMillis());
    }

    private int[] getTimeZoneWithMilliSeconds(String fractionStr) throws ApplicationException {
        int miliSecond = 0;
        int timeZoneOffSet = -1;
        if (fractionStr.startsWith(".")) {
            int milliSecondPartLength;
            if (fractionStr.endsWith("Z")) { //timezone is given as Z
                timeZoneOffSet = 0;
                String fractionPart = fractionStr.substring(1, fractionStr.lastIndexOf("Z"));
                miliSecond = Integer.parseInt(fractionPart);
                milliSecondPartLength = fractionPart.trim().length();
            } else {
                int lastIndexOfPlus = fractionStr.lastIndexOf("+");
                int lastIndexofMinus = fractionStr.lastIndexOf("-");
                if ((lastIndexOfPlus > 0) || (lastIndexofMinus > 0)) { //timezone +/-hh:mm
                    String timeOffSetStr;
                    if (lastIndexOfPlus > 0) {
                        timeOffSetStr = fractionStr.substring(lastIndexOfPlus + 1);
                        String fractionPart = fractionStr.substring(1, lastIndexOfPlus);
                        miliSecond = Integer.parseInt(fractionPart);
                        milliSecondPartLength = fractionPart.trim().length();
                        timeZoneOffSet = 1;
                    } else {
                        timeOffSetStr = fractionStr.substring(lastIndexofMinus + 1);
                        String fractionPart = fractionStr.substring(1, lastIndexofMinus);
                        miliSecond = Integer.parseInt(fractionPart);
                        milliSecondPartLength = fractionPart.trim().length();
                        timeZoneOffSet = -1;
                    }
                    if (timeOffSetStr.charAt(2) != ':') {
                        throw new ApplicationException("invalid time zone format: " + fractionStr);
                    }
                    int hours = Integer.parseInt(timeOffSetStr.substring(0, 2));
                    int minits = Integer.parseInt(timeOffSetStr.substring(3, 5));
                    timeZoneOffSet = ((hours * 60) + minits) * 60000 * timeZoneOffSet;

                } else { //no timezone
                    miliSecond = Integer.parseInt(fractionStr.substring(1));
                    milliSecondPartLength = fractionStr.substring(1).trim().length();
                }
            }
            if (milliSecondPartLength != 3) {
                // milisecond part represenst the fraction of the second so we have to
                // find the fraction and multiply it by 1000. So if milisecond part
                // has three digits nothing required
                miliSecond = miliSecond * 1000;
                for (int i = 0; i < milliSecondPartLength; i++) {
                    miliSecond = miliSecond / 10;
                }
            }
        } else {
            timeZoneOffSet = getTimeZoneOffset(fractionStr);
        }
        return new int[] { miliSecond, timeZoneOffSet };
    }

    private static int getTimeZoneOffset(String timezoneStr) throws ApplicationException {
        int timeZoneOffSet;
        if (timezoneStr.startsWith("Z")) { //GMT timezone
            timeZoneOffSet = 0;
        } else if (timezoneStr.startsWith("+") || timezoneStr.startsWith("-")) { //timezone with offset
            if (timezoneStr.charAt(3) != ':') {
                throw new ApplicationException("invalid time zone format:" + timezoneStr);
            }
            int hours = Integer.parseInt(timezoneStr.substring(1, 3));
            int minits = Integer.parseInt(timezoneStr.substring(4, 6));
            timeZoneOffSet = ((hours * 60) + minits) * 60000;
            if (timezoneStr.startsWith("-")) {
                timeZoneOffSet = timeZoneOffSet * -1;
            }
        } else {
            throw new ApplicationException("invalid prefix for timezone: " + timezoneStr);
        }
        return timeZoneOffSet;
    }

    private void setTimeValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType,
            Calendar utcCalendar) throws ApplicationException, DatabaseException {
        Time val = null;
        if (value != null) {
            BType type = TypeChecker.getType(value);
            if (value instanceof MapValue && type.getName().equals(Constants.STRUCT_TIME) && type
                    .getPackage().toString().equals(Constants.STRUCT_TIME_PACKAGE)) {
                Object timeVal = (((MapValue<String, Object>) value).get(Constants.STRUCT_TIME_FIELD));
                long time = (Long) timeVal;
                val = new Time(time);
            } else if (value instanceof Integer) {
                val = new Time((Integer) value);
            } else if (value instanceof String) {
                val = convertToTime((String) value);
            }
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTime(index + 1, val, utcCalendar);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setTime(index + 1, val, utcCalendar);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set timestamp value to statement: ", e);
        }
    }

    private Time convertToTime(String source) throws ApplicationException {
        //lexical representation of the time is hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
        if ((source == null) || source.trim().equals("")) {
            return null;
        }
        source = source.trim();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setLenient(false);
        if (source.length() >= 8) {
            if ((source.charAt(2) != ':') || (source.charAt(5) != ':')) {
                throw new ApplicationException("invalid time format: " + source);
            }
            int hour = Integer.parseInt(source.substring(0, 2));
            int minite = Integer.parseInt(source.substring(3, 5));
            int second = Integer.parseInt(source.substring(6, 8));
            int miliSecond = 0;
            int timeZoneOffSet = TimeZone.getDefault().getRawOffset();
            if (source.length() > 8) {
                String rest = source.substring(8);
                int[] offsetData = getTimeZoneWithMilliSeconds(rest);
                miliSecond = offsetData[0];
                timeZoneOffSet = offsetData[1];
                calendar.set(Calendar.DST_OFFSET, 0); //set the day light offset only if the time zone is present
            }
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minite);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, miliSecond);
            calendar.set(Calendar.ZONE_OFFSET, timeZoneOffSet);
        } else {
            throw new ApplicationException("time string can not be less than 8 characters: " + source);
        }
        return new Time(calendar.getTimeInMillis());
    }

    private void setBinaryValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        byte[] val = getByteArray(value);
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBinaryStream(index + 1, new ByteArrayInputStream(val), val.length);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBinaryStream(index + 1, new ByteArrayInputStream(val), val.length);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set binary value to statement: ", e);
        }
    }

    private void setBlobValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        byte[] val = getByteArray(value);
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBlob(index + 1, new ByteArrayInputStream(val), val.length);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setBlob(index + 1, new ByteArrayInputStream(val), val.length);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set binary value to statement: ", e);
        }
    }

    private static Integer obtainIntegerValue(Object value) throws ApplicationException {
        if (value != null) {
            BType type = TypeChecker.getType(value);
            switch (type.getTag()) {
                case TypeTags.BYTE_TAG:
                case TypeTags.INT_TAG:
                    return  ((Long) value).intValue();
                case TypeTags.STRING_TAG:
                    return Integer.parseInt((String) value);
                default:
                    throw new ApplicationException("invalid value for integer: " + value.toString());
            }
        }
        return null;
    }

    private byte[] getByteArray(Object value) throws ApplicationException {
        byte[] val = null;
        if (value instanceof ArrayValue) {
            val = ((ArrayValue) value).getBytes();
            val = Arrays.copyOfRange(val, 0, ((ArrayValue) value).size());
        } else if (value instanceof String) {
            val = getBytesFromBase64String((String) value);
        }
        return val;
    }

    private byte[] getBytesFromBase64String(String base64Str) throws ApplicationException {
        try {
            return Base64.getDecoder().decode(base64Str.getBytes(Charset.defaultCharset()));
        } catch (Exception e) {
            throw new ApplicationException("error in processing base64 string: ", e.getMessage());
        }
    }

    private void setClobValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        BufferedReader val = null;
        if (value != null) {
            val = new BufferedReader(new StringReader((String) value));
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setClob(index + 1, val, ((String) value).length());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setClob(index + 1, val, ((String) value).length());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set binary value to statement: ", e);
        }
    }

    private void setNClobValue(PreparedStatement stmt, Object value, int index, int direction, int sqlType)
            throws ApplicationException, DatabaseException {
        BufferedReader val = null;
        if (value != null) {
            val = new BufferedReader(new StringReader((String) value));
        }
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNClob(index + 1, val,  ((String) value).length());
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (val == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    stmt.setNClob(index + 1, val,  ((String) value).length());
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set binary value to statement: ", e);
        }
    }

    private void setRefCursorValue(PreparedStatement stmt, int index, int direction, String databaseProductName)
            throws ApplicationException, DatabaseException {
        try {
            if (Constants.QueryParamDirection.OUT == direction) {
                if (Constants.DatabaseNames.ORACLE.equals(databaseProductName)) {
                    // Since oracle does not support general java.sql.Types.REF_CURSOR in manipulating ref cursors it
                    // is required to use oracle.jdbc.OracleTypes.CURSOR here. In order to avoid oracle driver being
                    // a runtime dependency always, we have directly used the value(-10) of general oracle.jdbc
                    // .OracleTypes.CURSOR here.
                    ((CallableStatement) stmt).registerOutParameter(index + 1, ORACLE_CURSOR_TYPE);
                } else {
                    ((CallableStatement) stmt).registerOutParameter(index + 1, Types.REF_CURSOR);
                }
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in setting ref cursor value to statement: ", e);
        }
    }

    private void setArrayValue(Connection conn, PreparedStatement stmt, Object value, int index, int direction,
            int sqlType, String databaseProductName) throws ApplicationException, DatabaseException {
        Object[] arrayData = getArrayData(value);
        Object[] arrayValue = (Object[]) arrayData[0];
        String structuredSQLType = (String) arrayData[1];
        try {
            if (Constants.QueryParamDirection.IN == direction) {
                setArrayValue(arrayValue, conn, stmt, index, sqlType, databaseProductName, structuredSQLType);
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                setArrayValue(arrayValue, conn, stmt, index, sqlType, databaseProductName, structuredSQLType);
                registerArrayOutParameter(stmt, index, sqlType, structuredSQLType, databaseProductName);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                registerArrayOutParameter(stmt, index, sqlType, structuredSQLType, databaseProductName);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set array value to statement: ", e);
        }
    }

    private void registerArrayOutParameter(PreparedStatement stmt, int index, int sqlType,
            String structuredSQLType, String databaseProductName) throws SQLException {
        if (databaseProductName.equals(Constants.DatabaseNames.POSTGRESQL)) {
            ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType);
        } else {
            ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType, structuredSQLType);
        }
    }

    private void setArrayValue(Object[] arrayValue, Connection conn, PreparedStatement stmt, int index,
            int sqlType, String databaseProductName, String structuredSQLType) throws SQLException {
        if (arrayValue == null) {
            stmt.setNull(index + 1, sqlType);
        } else {
            // In POSTGRESQL, need to pass "float8" to indicate DOUBLE value.
            if (Constants.SQLDataTypes.DOUBLE.equals(structuredSQLType) && Constants.DatabaseNames.POSTGRESQL
                    .equals(databaseProductName)) {
                structuredSQLType = POSTGRES_DOUBLE;
            }
            Array array = conn.createArrayOf(structuredSQLType, arrayValue);
            stmt.setArray(index + 1, array);
        }
    }

    private void setNullObject(PreparedStatement stmt, int index) throws DatabaseException {
        try {
            stmt.setObject(index + 1, null);
        } catch (SQLException e) {
            throw new DatabaseException("error in set null to parameter with index: " + index, e);
        }
    }

    private static Object[] getArrayData(Object value) throws ApplicationException {
        BType type = TypeChecker.getType(value);
        if (value == null || type.getTag() != TypeTags.ARRAY_TAG) {
            return new Object[] { null, null };
        }
        BType elementType = ((BArrayType) type).getElementType();
        int typeTag = elementType.getTag();
        Object[] arrayData;
        int arrayLength;
        switch (typeTag) {
        case TypeTags.INT_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new Long[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getInt(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.BIGINT };
        case TypeTags.FLOAT_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new Double[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getFloat(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.DOUBLE };
        case TypeTags.DECIMAL_TAG:
            arrayLength = ((ArrayValue) value).size();
            arrayData = new BigDecimal[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getDecimal(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.DECIMAL };
        case TypeTags.STRING_TAG:
            arrayLength = (int) ((ArrayValue) value).size();
            arrayData = new String[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getString(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.VARCHAR };
        case TypeTags.BOOLEAN_TAG:
            arrayLength = (int) ((ArrayValue) value).size();
            arrayData = new Boolean[arrayLength];
            for (int i = 0; i < arrayLength; i++) {
                arrayData[i] = ((ArrayValue) value).getBoolean(i);
            }
            return new Object[] { arrayData, Constants.SQLDataTypes.BOOLEAN };
        case TypeTags.ARRAY_TAG:
            BType elementTypeOfArrayElement = ((BArrayType) elementType)
                    .getElementType();
            if (elementTypeOfArrayElement.getTag() == TypeTags.BYTE_TAG) {
                arrayLength = ((ArrayValue) value).size();
                arrayData = new Blob[arrayLength];
                for (int i = 0; i < arrayLength; i++) {
                    arrayData[i] = ((ArrayValue) value).getByte(i);
                }
                return new Object[] { arrayData, Constants.SQLDataTypes.BLOB };
            } else {
                throw new ApplicationException("unsupported data type for array parameter");
            }
        default:
            throw new ApplicationException("unsupported data type for array parameter");
        }
    }

    private void setUserDefinedValue(Connection conn, PreparedStatement stmt, Object value, int index,
            int direction, int sqlType) throws ApplicationException, DatabaseException {
        try {
            Object[] structData = getStructData(value, conn);
            Object[] dataArray = (Object[]) structData[0];
            String structuredSQLType = (String) structData[1];
            if (Constants.QueryParamDirection.IN == direction) {
                if (dataArray == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    Struct struct = conn.createStruct(structuredSQLType, dataArray);
                    stmt.setObject(index + 1, struct);
                }
            } else if (Constants.QueryParamDirection.INOUT == direction) {
                if (dataArray == null) {
                    stmt.setNull(index + 1, sqlType);
                } else {
                    Struct struct = conn.createStruct(structuredSQLType, dataArray);
                    stmt.setObject(index + 1, struct);
                }
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType, structuredSQLType);
            } else if (Constants.QueryParamDirection.OUT == direction) {
                ((CallableStatement) stmt).registerOutParameter(index + 1, sqlType, structuredSQLType);
            } else {
                throw new ApplicationException("invalid direction for the parameter with index: " + index);
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in set struct value to statement: ", e);
        }
    }

    private Object[] getStructData(Object value, Connection conn) throws SQLException, ApplicationException {
        BType type = TypeChecker.getType(value);
        if (value == null || (type.getTag() != TypeTags.OBJECT_TYPE_TAG
                && type.getTag() != TypeTags.RECORD_TYPE_TAG)) {
            return new Object[] { null, null };
        }
        String structuredSQLType = type.getName().toUpperCase(Locale.getDefault());
        Map<String, BField> structFields = ((BStructureType) type)
                .getFields();
        int fieldCount = structFields.size();
        Object[] structData = new Object[fieldCount];
        Iterator<BField> fieldIterator = structFields.values().iterator();
        for (int i = 0; i < fieldCount; ++i) {
            BField field = fieldIterator.next();
            Object bValue = ((MapValue) value).get(field.getFieldName());
            int typeTag = field.getFieldType().getTag();
            switch (typeTag) {
            case TypeTags.INT_TAG:
            case TypeTags.FLOAT_TAG:
            case TypeTags.STRING_TAG:
            case TypeTags.BOOLEAN_TAG:
            case TypeTags.DECIMAL_TAG:
                structData[i] = bValue;
                break;
            case TypeTags.ARRAY_TAG:
                BType elementType = ((BArrayType) field
                        .getFieldType()).getElementType();
                if (elementType.getTag() == TypeTags.BYTE_TAG) {
                    structData[i] = ((ArrayValue) bValue).getBytes();
                    break;
                } else {
                    throw new ApplicationException("unsupported data type for struct parameter: " + structuredSQLType);
                }
            case TypeTags.RECORD_TYPE_TAG:
                Object structValue = bValue;
                Object[] internalStructData = getStructData(structValue, conn);
                Object[] dataArray = (Object[]) internalStructData[0];
                String internalStructType = (String) internalStructData[1];
                structValue = conn.createStruct(internalStructType, dataArray);
                structData[i] = structValue;
                break;
            default:
                throw new ApplicationException("unsupported data type for struct parameter: " + structuredSQLType);
            }
        }
        return new Object[] { structData, structuredSQLType };
    }

    /**
     * Search for the first occurrence of "?" from the given starting point and replace it with given number of "?"'s.
     */
    private Object[] expandQuery(int start, int count, String query) {
        StringBuilder result = new StringBuilder();
        int n = query.length();
        boolean doubleQuoteExists = false;
        boolean singleQuoteExists = false;
        int end = n;
        for (int i = start; i < n; i++) {
            if (query.charAt(i) == '\'') {
                singleQuoteExists = !singleQuoteExists;
            } else if (query.charAt(i) == '\"') {
                doubleQuoteExists = !doubleQuoteExists;
            } else if (query.charAt(i) == '?' && !(doubleQuoteExists || singleQuoteExists)) {
                result.append(query, 0, i);
                result.append(generateQuestionMarks(count));
                end = result.length() + 1;
                if (i + 1 < n) {
                    result.append(query.substring(i + 1));
                }
                break;
            }
        }
        return new Object[] { end, result.toString() };
    }

    private String generateQuestionMarks(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append(Constants.QUESTION_MARK);
            if (i + 1 < n) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
