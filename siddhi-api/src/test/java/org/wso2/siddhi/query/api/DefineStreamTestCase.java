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
package org.wso2.siddhi.query.api;

import org.junit.Test;
import org.wso2.siddhi.query.api.exception.AttributeAlreadyExistException;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

public class DefineStreamTestCase {

    //define stream cseEventStream (symbol string, price int, volume float );

    @Test
    public void testCreatingStreamDefinition() {
        StreamDefinition streamDefinition = QueryFactory.createStreamDefinition();
        streamDefinition.name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);

    }

    @Test(expected = AttributeAlreadyExistException.class)
    public void testCreatingStreamWithDuplicateAttribute() {
        StreamDefinition streamDefinition = QueryFactory.createStreamDefinition();
        streamDefinition.name("cseEventStream").attribute("symbol", Attribute.Type.STRING).attribute("symbol", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);

    }


}
