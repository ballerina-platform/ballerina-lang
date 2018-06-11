package org.ballerinalang.langserver.index;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class Constants {
    static final String DRIVER =
            "org.h2.Driver";

    static final String DEFAULT_CONNECTION_URL =
            "jdbc:h2:mem:test\\;INIT=RUNSCRIPT FROM 'classpath:lang-server-index.sql'";

            // INSERT Statements
    static final String INSERT_BLANG_PACKAGE =
                    "INSERT INTO bLangPackage (name, orgName, version) values (?, ?, ?)";

    static final String INSERT_BLANG_SERVICE =
            "INSERT INTO bLangService (packageId, name) values (?, ?)";

    static final String INSERT_BLANG_RESOURCE =
            "INSERT INTO bLangResource (serviceId, name) values (?, ?)";

    static final String INSERT_BLANG_FUNCTION =
            "INSERT INTO bLangFunction (packageId, objectId, name, completionItem) values (?, ?, ?, ?)";

    static final String INSERT_BLANG_RECORD =
            "INSERT INTO bLangRecord (packageId, name, fields) values (?, ?, ?)";

    static final String INSERT_BLANG_OBJECT = 
            "INSERT INTO bLangObject (packageId, name, fields, type) values (?, ?, ?, ?)";
    
            // GET Statements
    static final String GET_PACKAGE_BY_ORG =
            "SELECT * FROM bLangPackage WHERE orgName = ?";

    static final String SELECT_ALL_FUNCTIONS =
            "SELECT * FROM bLangFunction";

    static final String SELECT_ALL_OBJECTS_BY_TYPE =
            "SELECT * FROM bLangObject";
}
