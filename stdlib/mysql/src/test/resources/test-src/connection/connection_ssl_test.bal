// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied. See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerinax/mysql;
import ballerinax/sql;

function testWithVerifyCert(string hostname, int port, string user, string pw, string db) returns boolean {
    mysql:Options opt = {
        ssl: {
            mode: "VERIFY_CERT",
            clientCertKeystore : {
                path: "/Users/sinthuja/test/mysql/ssl-2/client-keystore.p12",
                password: "changeit"
            },
            trustCertKeystore: {
                 path: "/Users/sinthuja/test/mysql/ssl/trust-keystore.p12",
                 password: "changeit"
            }
        }
    };
    mysql:Client testDB = new (
            host= hostname,
            port= port,
            username=user,
            password= pw,
            database=db,
            options = opt);

    var dt = testDB->select("SELECT * FROM Customers", ());
    boolean success = false;
    if (dt is table<record {}>) {
        success = true;
    }
    checkpanic testDB.close();
    return success;
}

function testWithPool(string hostname, int port, string user, string pw, string db) returns boolean {
    sql:PoolOptions pool = {};
    mysql:Client testDB = new ();

    var dt = testDB->select("SELECT * FROM Customers", ());
    boolean success = false;
    if (dt is table<record {}>) {
        success = true;
    }
    checkpanic testDB.close();
    return success;
}

function testWithPreferredSSL(string hostname, int port, string user, string pw, string db) returns boolean {
    mysql:Options opt = {
        ssl: {
            mode: "PREFERRED"
        },
        readWriteTimeoutInSeconds: 60
    };
    mysql:Client testDB = new (
            host= hostname,
            port= port,
            username=user,
            password=pw,
            database=db,
            options=opt
    );
    var dt = testDB->select("SELECT * FROM Customers", ());
    boolean success = false;
    if (dt is table<record {}>) {
        success = true;
    }
    checkpanic testDB.close();
    return success;
}

function test2(string hostname, int port, string user, string pw, string db) returns boolean {
    mysql:Options opt = {
        ssl: {
            mode: "PREFERRED"
        },
        readWriteTimeoutInSeconds: 60
    };
    mysql:Client testDB = new (
            host= hostname
    );
    var dt = testDB->select("SELECT * FROM Customers", ());
    boolean success = false;
    if (dt is table<record {}>) {
        success = true;
    }
    checkpanic testDB.close();
    return success;
}


function testWithRequiredSSL(string hostname, int port, string user, string pw, string db) returns boolean {
    mysql:Options opt = {
        ssl: {
            mode: "REQUIRED"
        }
    };
    mysql:Client testDB = new (
            host= hostname,
            port=port,
            username= user,
            password=pw,
            database= db,
            options=opt
    );
    var dt = testDB->select("SELECT * FROM Customers", ());
    boolean success = false;
    if (dt is table<record {}>) {
        success = true;
    }
    checkpanic testDB.close();
    return success;
}