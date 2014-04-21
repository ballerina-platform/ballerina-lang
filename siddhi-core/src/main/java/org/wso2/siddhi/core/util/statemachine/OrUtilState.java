/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.util.statemachine;

import org.wso2.siddhi.query.api.query.input.TransformedStream;

public class OrUtilState extends UtilState implements LogicUtilState {

    private int partner = -1;


    public OrUtilState(int stateNumber, TransformedStream transformedStream, int partnerNum) {
        super(stateNumber, transformedStream);
        this.partner=partnerNum;
    }

    public int getPartnerNumber() {
        return partner;
    }

    public void setPartner(int partner) {
        this.partner = partner;
    }
}
