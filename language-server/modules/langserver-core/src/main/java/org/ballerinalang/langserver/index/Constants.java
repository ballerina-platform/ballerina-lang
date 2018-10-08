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
    
    // GET Statements

    static final String GET_FILTERED_FUNCTIONS_FROM_PACKAGE = "SELECT p.name, p.orgName, f.completionItem, f.name, " +
            "f.private, f.attached FROM (SELECT id, name, orgName FROM bLangPackage WHERE name = ? AND orgName = ?)" +
            "AS p INNER JOIN bLangFunction AS f WHERE p.id=f.packageId AND f.objectId=-1 AND f.private=? " +
            "AND f.attached=? AND f.name NOT LIKE '%<init>%' AND " + "f.name NOT LIKE '%<start>%' " +
            "AND f.name NOT LIKE '%<stop>%'";
    
    

    static final String GET_ALL_PACKAGES = "SELECT * FROM bLangPackage";

    static final String GET_ALL_ENDPOINTS = "SELECT * FROM bLangObject WHERE type = 1";

    static final String GET_ALL_ACTIONS = "SELECT p.ID, o.ID, f.NAME  " +
            "FROM BLANGOBJECT as o " +
            "JOIN BLANGPACKAGE as p ON p.ID = o.PACKAGEID " +
            "JOIN BLANGFUNCTION as f ON f.OBJECTID   = o.ACTIONHOLDERID  " +
            "WHERE o.TYPE  = 1 AND p.NAME LIKE ? AND o.NAME LIKE ?";
}
