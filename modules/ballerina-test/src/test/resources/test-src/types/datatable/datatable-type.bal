import ballerina.lang.datatables;
import ballerina.data.sql;
import ballerina.lang.xmls;
import ballerina.lang.jsons;
import ballerina.lang.blobs;

struct ResultPrimitive {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
}

struct ResultSetTestAlias {
    int INT_TYPE;
    int LONG_TYPE;
    float FLOAT_TYPE;
    float DOUBLE_TYPE;
    boolean BOOLEAN_TYPE;
    string STRING_TYPE;
    int DT2INT_TYPE;
}

struct ResultObject {
    blob BLOB_TYPE;
    string CLOB_TYPE;
    blob BINARY_TYPE;
}

struct ResultMap {
    map INT_ARRAY;
    map LONG_ARRAY;
    map FLOAT_ARRAY;
    map BOOLEAN_ARRAY;
    map STRING_ARRAY;
}

struct ResultBlob {
    blob BLOB_TYPE;
}

struct ResultDates {
    string DATE_TYPE;
    string TIME_TYPE;
    string TIMESTAMP_TYPE;
    string DATETIME_TYPE;
}

struct ResultPrimitiveInt {
    int INT_TYPE;
}

struct ResultCount {
    int COUNTVAL;
}



function testToJson () (json) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
        json result;
        result, _ = <json>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testToXml () (xml) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 1", parameters);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function toXmlComplex () (xml) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    sql:Parameter[] parameters = [];

    try {
        datatable dt = testDB.select("SELECT int_type, int_array, long_type, long_array, float_type,
                    float_array, double_type, boolean_type, string_type, double_array, boolean_array, string_array
                    from MixTypes where row_id =1", parameters);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testJsonWithNull () (json) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});

    sql:Parameter[] parameters = [];
    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                  boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
        json result;
        result, _ = <json>dt;
        return result;
    }  finally {
        testDB.close();
    }
    return null;
}

function testXmlWithNull () (xml) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});

    sql:Parameter[] parameters = [];
    try {
        datatable dt = testDB.select("SELECT int_type, long_type, float_type, double_type,
                   boolean_type, string_type from DataTable WHERE row_id = 2", parameters);
        xml result;
        result, _ = <xml>dt;
        return result;
    } finally {
        testDB.close();
    }
    return null;
}

function testToXmlWithinTransaction () (string, int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable dt = testDB.select("SELECT int_type, long_type from DataTable WHERE row_id = 1", parameters);
            xml xmlResult;
            xmlResult, _ = <xml>dt;
            result = xmls:toString(xmlResult);
        } aborted {
        returnValue = -1;
    }
    return result, returnValue;
    } finally {
        testDB.close();
    }
    return "", -1;
}

function testToJsonWithinTransaction () (string, int) {
    sql:ClientConnector testDB = create sql:ClientConnector(sql:HSQLDB_FILE, "./target/tempdb/",
                                                            0, "TEST_DATA_TABLE_DB", "SA", "", {maximumPoolSize:1});
    int returnValue = 0;
    string result;
    try {
        transaction {
            sql:Parameter[] parameters = [];
            datatable dt = testDB.select("SELECT int_type, long_type from DataTable WHERE row_id = 1", parameters);
            json jsonResult;
            jsonResult, _ = <json>dt;
            result = jsons:toString(jsonResult);
        } aborted {
        returnValue = -1;
    }
    return result, returnValue;
    } finally {
        testDB.close();
    }
    return "", -2;
}
