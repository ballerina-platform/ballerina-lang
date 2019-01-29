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
package org.ballerinalang.langserver.index.dao;

import java.sql.Connection;

/**
 * DAO factory to produce Various DAOs.
 * 
 * @since 0.983.0
 */
public class DAOFactory {

    private Connection connection;

    public DAOFactory(Connection connection) {
        this.connection = connection;
    }

    public AbstractDAO get(DAOType type) {
        switch (type) {
            case FUNCTION_SYMBOL:
                return new BFunctionSymbolDAO(this.connection);
            case OBJECT_TYPE:
                return new BObjectTypeSymbolDAO(this.connection);
            case OTHER_TYPE_SYMBOL:
                return new BOtherTypeSymbolDAO(this.connection);
            case PACKAGE_SYMBOL:
                return new BPackageSymbolDAO(this.connection);
            case RECORD_TYPE_SYMBOL:
                return new BRecordTypeSymbolDAO(this.connection);
            default:
                // Should not come to this point and if so, there is a bug.
                return null;
        }
    }
}
