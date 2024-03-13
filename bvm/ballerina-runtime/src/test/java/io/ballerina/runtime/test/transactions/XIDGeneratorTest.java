/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org).
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

package io.ballerina.runtime.test.transactions;

import io.ballerina.runtime.transactions.XIDGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.transaction.xa.Xid;

public class XIDGeneratorTest {

    @Test (description = "Test createXID method in XIDGenerator class")
    public void testCreateXID()
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String combinedId = "00000000-0000-0000-0000-000000000000:0";
        Class<?> xidGeneratorClass = Class.forName("io.ballerina.runtime.transactions.XIDGenerator");
        Method createXIDMethod = xidGeneratorClass.getDeclaredMethod("createXID", String.class);
        createXIDMethod.setAccessible(true);
        Xid xid = (Xid) createXIDMethod.invoke(null, combinedId);
        Assert.assertNotNull(xid);
        Assert.assertEquals(xid.getFormatId(), XIDGenerator.getDefaultFormat());
        Assert.assertEquals(new String(xid.getGlobalTransactionId()), "00000000-0000-0000-0000-000000000000");
        Assert.assertEquals(new String(xid.getBranchQualifier()), "0");
    }
}
