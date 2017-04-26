/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.siddhi.extension.table.rdbms;

import org.wso2.siddhi.core.table.record.RecordIterator;
import org.wso2.siddhi.extension.table.rdbms.util.RDBMSTableUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RDBMSIterator implements RecordIterator<Object[]> {

    private ResultSet rs;
    private PreparedStatement stmt;
    private Connection conn;

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object[] next() {
        return new Object[0];
    }

    @Override
    public void remove() {

    }

    @Override
    public void close() throws IOException {
        RDBMSTableUtils.cleanupConnection(this.rs, this.stmt, this.conn);
        this.rs = null;
        this.stmt = null;
        this.conn = null;
    }

    @Override
    protected void finalize() throws Throwable {
            /* in the unlikely case, this iterator does not go to the end,
             * we have to make sure the connection is cleaned up */
        RDBMSTableUtils.cleanupConnection(this.rs, this.stmt, this.conn);
        super.finalize();
    }

}
