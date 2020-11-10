/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.serializer.json;

import org.ballerinalang.core.model.util.serializer.TypeInstanceProvider;
import org.ballerinalang.core.model.util.serializer.TypeInstanceProviderFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TypeInstanceProviderFactory test cases.
 * <p>
 * {@link TypeInstanceProviderFactory} dynamically create TypeInstanceProvider implementations.
 */
public class TypeInstanceProviderFactoryTest {
    @Test
    public void testNoParamConstructorObject() {
        TypeInstanceProvider provider = new TypeInstanceProviderFactory().from(Foo.class.getName());
        Object o = provider.newInstance();
        Assert.assertTrue(o instanceof Foo);
        Assert.assertEquals(42, ((Foo) o).secretNum);
    }

    /**
     * Simple class with a single int field used to testing.
     */
    static class Foo {
        final int secretNum = 42;
    }
}
