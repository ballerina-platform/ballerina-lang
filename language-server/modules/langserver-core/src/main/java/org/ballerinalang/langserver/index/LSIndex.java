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

import java.nio.file.Path;

/**
 * Index Interface for Ballerina Language Server.
 */
public interface LSIndex {

    /**
     * Load the index database schema from the disk.
     *
     * @return {@link Boolean}  Whether the index schema loading is successful or not
     */
    boolean loadIndexSchema();

    /**
     * Re-Index the Language server index.
     *
     * @return {@link Boolean}  Whether the re-indexing process is success or not
     */
    boolean reIndex();

    /**
     * Carryout the Language Server Index.
     *
     * @return {@link Boolean}  Whether the indexing process is success or not
     */
    boolean doIndex();

    /**
     * Save the current in-memory index to a given file location.
     *
     * @param path                  File path to save the index dump
     * @return {@link Boolean}      Whether the save process is success or not      
     */
    boolean saveIndexDump(Path path);

    /**
     * Close the Language Server Index Connection.
     * 
     * @return  Whether the operation is success or not
     */
    boolean closeConnection();
}
