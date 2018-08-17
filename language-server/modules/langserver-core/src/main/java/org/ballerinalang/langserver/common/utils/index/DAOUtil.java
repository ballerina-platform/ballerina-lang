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
package org.ballerinalang.langserver.common.utils.index;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.index.dao.ObjectDAO;
import org.ballerinalang.langserver.index.dao.OtherTypeDAO;
import org.ballerinalang.langserver.index.dao.PackageFunctionDAO;
import org.ballerinalang.langserver.index.dao.RecordDAO;
import org.eclipse.lsp4j.CompletionItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for DAO manipulation.
 */
public class DAOUtil {
    
    private static final Gson gson = new Gson();
    
    private static final JsonParser parser = new JsonParser();
    
    private static final Logger logger = LoggerFactory.getLogger(DAOUtil.class);

    /**
     * Generate the PackageFunctionDAO list from the result set.
     * @param resultSet         Result set for manipulation
     * @return {@link List}     List of DAOs
     */
    public static List<PackageFunctionDAO> getPackageFunctionDAO(ResultSet resultSet) {
        List<PackageFunctionDAO> packageFunctionDAOs = new ArrayList<>();

        try {
            while (resultSet.next()) {
                String pkgName = resultSet.getString(1);
                String pkgOrgName = resultSet.getString(2);
                String funcName = resultSet.getString(4);
                String completionItem = resultSet.getString(3);
                boolean isPrivate = resultSet.getBoolean(5);
                boolean isAttached = resultSet.getBoolean(6);
                PackageFunctionDAO packageFunctionDAO = new PackageFunctionDAO(pkgName, pkgOrgName, funcName,
                        jsonToCompletionItem(completionItem), isPrivate, isAttached);
                packageFunctionDAOs.add(packageFunctionDAO);
            }
        } catch (SQLException e) {
            logger.error("Error Processing Package Functions Result Set");
        }
        return packageFunctionDAOs;
    }

    /**
     * Generate the RecordDAO list from the result set.
     * @param resultSet         Result set for manipulation
     * @return {@link List}     List of DAOs
     */
    public static List<RecordDAO> getRecordDAO(ResultSet resultSet) {
        List<RecordDAO> recordDAOs = new ArrayList<>();

        try {
            while (resultSet.next()) {
                String pkgName = resultSet.getString(1);
                String pkgOrgName = resultSet.getString(2);
                String recordName = resultSet.getString(4);
                String completionItem = resultSet.getString(3);
                boolean isPrivate = resultSet.getBoolean(5);
                RecordDAO recordDAO =
                        new RecordDAO(pkgName, pkgOrgName, recordName, isPrivate, jsonToCompletionItem(completionItem));
                recordDAOs.add(recordDAO);
            }
        } catch (SQLException e) {
            logger.error("Error Processing Records Result Set");
        }
        return recordDAOs;
    }

    /**
     * Generate the OtherTypeDAO list from the result set.
     * @param resultSet         Result set for manipulation
     * @return {@link List}     List of DAOs
     */
    public static List<OtherTypeDAO> getOtherTypeDAO(ResultSet resultSet) {
        List<OtherTypeDAO> otherTypeDAOs = new ArrayList<>();

        try {
            while (resultSet.next()) {
                String pkgName = resultSet.getString(1);
                String pkgOrgName = resultSet.getString(2);
                String recordName = resultSet.getString(4);
                String completionItem = resultSet.getString(3);
                OtherTypeDAO otherTypeDAO =
                        new OtherTypeDAO(pkgName, pkgOrgName, recordName, jsonToCompletionItem(completionItem));
                otherTypeDAOs.add(otherTypeDAO);
            }
        } catch (SQLException e) {
            logger.error("Error Processing Other Types Result Set");
        }
        return otherTypeDAOs;
    }

    /**
     * Generate the ObjectDAO list from the result set.
     * @param resultSet         Result set for manipulation
     * @return {@link List}     List of DAOs
     */
    public static List<ObjectDAO> getObjectDAO(ResultSet resultSet) {
        List<ObjectDAO> objectDAOs = new ArrayList<>();

        try {
            while (resultSet.next()) {
                String pkgName = resultSet.getString(1);
                String pkgOrgName = resultSet.getString(2);
                String recordName = resultSet.getString(4);
                boolean isPrivate = resultSet.getBoolean(5);
                String completionItem = resultSet.getString(3);
                ObjectDAO recordDAO =
                        new ObjectDAO(pkgName, pkgOrgName, recordName, isPrivate, jsonToCompletionItem(completionItem));
                objectDAOs.add(recordDAO);
            }
        } catch (SQLException e) {
            logger.error("Error Objects Result Set");
        }
        return objectDAOs;
    }

    // Private Methods

    private static CompletionItem jsonToCompletionItem(String completionItemJson) {
        return gson.fromJson(parser.parse(completionItemJson).getAsJsonObject(), CompletionItem.class);
    }
}
