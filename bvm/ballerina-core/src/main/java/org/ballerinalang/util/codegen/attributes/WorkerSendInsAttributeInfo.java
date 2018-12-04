/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.util.codegen.attributes;

/**
 * Keep track of the workers that may waiting on current worker.
 * @since 0.985.0
 */
public class WorkerSendInsAttributeInfo implements AttributeInfo {

    // Index to a UTF8CPEntry
    private int attributeNameIndex;
    public String[] sendIns;

    public WorkerSendInsAttributeInfo(int attributeNameIndex) {

        this.attributeNameIndex = attributeNameIndex;
    }

    public String[] getWorkerSendIns() {

        return sendIns;
    }

    @Override
    public Kind getKind() {

        return Kind.WORKER_SEND_INS;
    }

    @Override
    public int getAttributeNameIndex() {

        return attributeNameIndex;
    }
}
