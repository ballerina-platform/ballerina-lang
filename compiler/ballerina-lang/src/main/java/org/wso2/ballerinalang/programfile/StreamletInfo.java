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

package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.compiler.semantics.model.types.BConnectorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.Objects;

/**
 * {@code StreamletInfo} contains metadata of a Ballerina streamlet entry in the program file.
 */
public class StreamletInfo extends StructureTypeInfo {

    // Streamlet constructor signature
    public int signatureCPIndex;

    public BType[] paramTypes;

    public BConnectorType connectorType;

    public int siddhiQueryCPIndex;

    public int streamIdsAsStringCPIndex;

    private String siddhiQuery;

    private String streamIdsAsString;

    public String getSiddhiQuery() {
        return siddhiQuery;
    }

    public void setSiddhiQuery(String siddhiQuery) {
        this.siddhiQuery = siddhiQuery;
    }

    public String getStreamIdsAsString() {
        return streamIdsAsString;
    }

    public void setStreamIdsAsString(String streamIdsAsString) {
        this.streamIdsAsString = streamIdsAsString;
    }

    public StreamletInfo(int pkgPathCPIndex, int nameCPIndex, int flags) {
        super(pkgPathCPIndex, nameCPIndex, flags);
    }

    public StreamletInfo(int pkgPathCPIndex, int nameCPIndex, int siddhiQueryCPIndex, int streamIdsAsStringCPIndex, int flags) {
        super(pkgPathCPIndex, nameCPIndex, flags);
        this.siddhiQueryCPIndex = siddhiQueryCPIndex;
        this.streamIdsAsStringCPIndex = streamIdsAsStringCPIndex;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkgNameCPIndex, nameCPIndex);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StreamletInfo && pkgNameCPIndex == (((StreamletInfo) obj).pkgNameCPIndex)
                && nameCPIndex == (((StreamletInfo) obj).nameCPIndex);
    }
}
