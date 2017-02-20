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
 */
package org.ballerinalang.nativeimpl.connectors.data.sql;

import org.ballerinalang.model.DataIterator;
import org.ballerinalang.model.values.BLong;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.connectors.data.sql.client.SQLConnectorUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * This iterator mainly wrap java.sql.ResultSet. This will provide datatable operations
 * related to ballerina.data.connectors.sql connector.
 *
 * @since 0.8.0
 */
public class SQLDataIterator implements DataIterator {

    private Connection conn;
    private Statement stmt;
    private ResultSet rs;

    public SQLDataIterator(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        this.conn = conn;
        this.stmt = stmt;
        this.rs = rs;
    }

    @Override
    public void close() {
        SQLConnectorUtils.cleanupConnection(rs, stmt, conn);
        rs = null;
        stmt = null;
        conn = null;
    }

    @Override
    public boolean next() {
        try {
            return rs.next();
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public String getString(int index) {
        try {
            return rs.getString(index);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getString(String columnName) {
        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public long getLong(int index) {
        try {
            return rs.getLong(index);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public long getLong(String columnName) {
        try {
            return rs.getLong(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public int getInt(int index) {
        try {
            return rs.getInt(index);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public int getInt(String columnName) {
        try {
            return rs.getInt(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public float getFloat(int index) {
        try {
            return rs.getFloat(index);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public float getFloat(String columnName) {
        try {
            return rs.getFloat(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public double getDouble(int index) {
        try {
            return rs.getDouble(index);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public double getDouble(String columnName) {
        try {
            return rs.getDouble(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    public boolean getBoolean(int index) {
        try {
            return rs.getBoolean(index);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public boolean getBoolean(String columnName) {
        try {
            return rs.getBoolean(columnName);
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    @Override
    public String getObjectAsString(int columnIndex) {
        try {
            Object object = rs.getObject(columnIndex);
            if (object != null) {
                return getString(object);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    private String getString(Object object) throws SQLException {
        String value;
        if (object instanceof Blob) {
            value = getBString((Blob) object).stringValue();
        } else if (object instanceof Timestamp) {
            value = String.valueOf(((Timestamp) object).getTime());
        } else if (object instanceof Clob) {
            value = getBString((Clob) object).stringValue();
        } else if (object instanceof Date) {
            value = String.valueOf(((Date) object).getTime());
        } else if (object instanceof Time) {
            value = String.valueOf(((Time) object).getTime());
        } else if (object instanceof InputStream) {
            value = getBString((InputStream) object).stringValue();
        } else {
            value = String.valueOf(object);
        }
        return value;
    }

    @Override
    public String getObjectAsString(String columnName) {
        try {
            Object object = rs.getObject(columnName);
            if (object != null) {
                return getString(object);
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
    }

    // Below method doesn't support streaming.
    @Override
    public BValue get(int columnIndex, String type) {
        try {
            switch (type) {
            case "blob":
                return getBString(rs.getBlob(columnIndex));
            case "clob":
                return getBString(rs.getClob(columnIndex));
            case "nclob":
                return getBString(rs.getNClob(columnIndex));
            case "date":
                return new BLong(rs.getDate(columnIndex).getTime());
            case "time":
                return new BLong(rs.getTime(columnIndex).getTime());
            case "timestamp":
                return new BLong(rs.getTimestamp(columnIndex).getTime());
            case "binary":
                return getBString(rs.getBinaryStream(columnIndex));
            }
        } catch (SQLException e) {
            throw new BallerinaException("failed to get the value of " + type + ": " + e.getMessage(), e);
        }
        return null;
    }

    // Below method doesn't support streaming.
    @Override
    public BValue get(String columnName, String type) {
        try {
            switch (type) {
            case "blob":
                return getBString(rs.getBlob(columnName));
            case "clob":
                return getBString(rs.getClob(columnName));
            case "nclob":
                return getBString(rs.getNClob(columnName));
            case "date":
                return new BLong(rs.getDate(columnName).getTime());
            case "time":
                return new BLong(rs.getTime(columnName).getTime());
            case "timestamp":
                return new BLong(rs.getTimestamp(columnName).getTime());
            case "binary":
                return getBString(rs.getBinaryStream(columnName));
            }
        } catch (SQLException e) {
            throw new BallerinaException("failed to get the value of " + type + ": " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Map<String, Object> getArray(int columnIndex) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Array array = rs.getArray(columnIndex);
            if (!rs.wasNull()) {
                Object[] objArray = (Object[]) array.getArray();
                for (int i = 0; i < objArray.length; i++) {
                    resultMap.put(String.valueOf(i), objArray[i]);
                }
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> getArray(String columnName) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Array array = rs.getArray(columnName);
            if (!rs.wasNull()) {
                Object[] objArray = (Object[]) array.getArray();
                for (int i = 0; i < objArray.length; i++) {
                    resultMap.put(String.valueOf(i), objArray[i]);
                }
            }
        } catch (SQLException e) {
            throw new BallerinaException(e.getMessage(), e);
        }
        return resultMap;
    }

    private BValue getBString(Clob clob) throws SQLException {
        // Directly allocating full length arrays for decode byte arrays since anyway we are building
        // new String in memory.
        char[] arr = new char[8 * 1024];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        try (Reader characterStream = clob.getCharacterStream()) {
            while ((numCharsRead = characterStream.read(arr, 0, arr.length)) != -1) {
                buffer.append(arr, 0, numCharsRead);
            }
        } catch (IOException e) {
            throw new BallerinaException("failed to read from clob type: " + e.getMessage(), e);
        }
        return new BString(buffer.toString());
    }

    private BValue getBString(InputStream inputStream) throws SQLException {
        String value = getStringFromInputStream(inputStream);
        byte[] encode = getBase64Encode(value);
        return new BString(new String(encode, Charset.defaultCharset()));
    }

    private BValue getBString(Blob blob) throws SQLException {
        // Directly allocating full length arrays for decode byte arrays since anyway we are building
        // new String in memory.
        // Position of the getBytes has to be 1 instead of 0.
        // "pos - the ordinal position of the first byte in the BLOB value to be extracted;
        // the first byte is at position 1"
        // - https://docs.oracle.com/javase/7/docs/api/java/sql/Blob.html#getBytes(long,%20int)
        byte[] encode = getBase64Encode(new String(blob.getBytes(1L, (int) blob.length()), Charset.defaultCharset()));
        return new BString(new String(encode, Charset.defaultCharset()));
    }

    private static String getStringFromInputStream(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.defaultCharset()))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            throw new BallerinaException("failed to read binary as a string: " + e.getMessage(), e);
        }
        return sb.toString();
    }

    private byte[] getBase64Encode(String st) {
        return Base64.getEncoder().encode(st.getBytes(Charset.defaultCharset()));
    }
}
