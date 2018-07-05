/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.langserver.index;

/**
 * Index Query Constants.
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
            "INSERT INTO bLangFunction (packageId, objectId, name, completionItem, private, attached)" +
                    " values (?, ?, ?, ?, ?, ?)";

    static final String INSERT_BLANG_RECORD =
            "INSERT INTO bLangRecord (packageId, name, fields, private, completionItem) values (?, ?, ?, ?, ?)";

    static final String INSERT_OTHER_TYPE =
            "INSERT INTO bLangType (packageId, name, fields, completionItem) values (?, ?, ?, ?)";

    static final String INSERT_BLANG_OBJECT = 
            "INSERT INTO bLangObject (packageId, name, fields, type, private, completionItem)" +
                    "values (?, ?, ?, ?, ?, ?)";
    
    // UPDATE Statements
    static final String UPDATE_ENDPOINT_ACTION_HOLDER_ID = 
            "UPDATE bLangObject SET actionHolderId = ? WHERE id = ?";
    
    // GET Statements
    static final String GET_ALL_FUNCTIONS_FROM_PACKAGE = "SELECT p.name, p.orgName, f.completionItem, f.name, " +
            "f.private, f.attached FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?)" +
            "AS p INNER JOIN bLangFunction AS f WHERE p.id=f.packageId AND f.objectId=-1 AND f.name " +
            "NOT LIKE '%<init>%' AND " + "f.name NOT LIKE '%<start>%' AND f.name NOT LIKE '%<stop>%'";

    static final String GET_FILTERED_FUNCTIONS_FROM_PACKAGE = "SELECT p.name, p.orgName, f.completionItem, f.name, " +
            "f.private, f.attached FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?)" +
            "AS p INNER JOIN bLangFunction AS f WHERE p.id=f.packageId AND f.objectId=-1 AND f.private=? " +
            "AND f.attached=? AND f.name NOT LIKE '%<init>%' AND " + "f.name NOT LIKE '%<start>%' " +
            "AND f.name NOT LIKE '%<stop>%'";
    
    static final String GET_ALL_RECORDS_FROM_PACKAGE = "SELECT p.name, p.orgName, r.completionItem, r.name, " +
            "r.private FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?) AS p " +
            "INNER JOIN bLangRecord AS r WHERE p.id = r.packageId";
    
    static final String GET_RECORDS_ON_ACCESS_TYPE_FROM_PACKAGE = "SELECT p.name, p.orgName, r.completionItem, " +
            "r.name, r.private FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?) AS p " +
            "INNER JOIN bLangRecord AS r WHERE p.id = r.packageId AND r.private=?";
    
    static final String GET_OTHER_TYPES_FROM_PACKAGE = "SELECT p.name, p.orgName, t.completionItem, t.name " +
            "FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?) AS p " +
            "INNER JOIN bLangType AS t WHERE p.id = t.packageId";
    
    static final String GET_OBJECTS_ON_ACCESS_TYPE_FROM_PACKAGE = "SELECT p.name, p.orgName, o.completionItem," +
            " o.name, o.private FROM (select id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?) " +
            "AS p INNER JOIN bLangObject AS o WHERE p.id = o.packageId AND o.type = 3 AND o.private=?";
    
    static final String GET_ALL_OBJECT_FROM_PACKAGE = "SELECT p.name, p.orgName, o.completionItem, o.name, o.private " +
            "FROM (select id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?) AS p " +
            "INNER JOIN bLangObject AS o WHERE p.id = o.packageId AND o.type = 3";

    static final String GET_ALL_PACKAGES = "SELECT * FROM bLangPackage";

    static final String GET_ALL_ENDPOINTS = "SELECT * FROM bLangObject WHERE type = 1";
}
