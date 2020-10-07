 /*
  *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 package org.ballerinalang.test.jvm;

 import org.ballerinalang.core.model.values.BValue;
 import org.ballerinalang.test.util.BCompileUtil;
 import org.ballerinalang.test.util.BRunUtil;
 import org.ballerinalang.test.util.CompileResult;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;

 /**
  * Test cases to cover anonymous functions related tests on JBallerina.
  *
  * @since 0.995.0
  */

 public class AnonymousFunctionsTest {

     private CompileResult compileResult;

     @BeforeClass
     public void setup() {
         compileResult = BCompileUtil.compile("test-src/jvm/anon-funcs.bal");
     }

     @Test(description = "Test anon call")
     public void testAnonCall() {
         BValue[] result = BRunUtil.invoke(compileResult, "testAnonFunc");
         Assert.assertEquals(result[0].stringValue(), "Hello World.!!!");
     }

     @Test(description = "Test fp passing between methods")
     public void testFPPassing() {
         BValue[] result = BRunUtil.invoke(compileResult, "testFPPassing");
         Assert.assertEquals(result[0].stringValue(), "200");
     }

     @Test(description = "Test basic closures")
     public void testBasicClosure() {
         BValue[] result = BRunUtil.invoke(compileResult, "testBasicClosure");
         Assert.assertEquals(result[0].stringValue(), "242");
     }

     @Test(description = "Test multilevel closures")
     public void testMultilevelClosures() {
         BValue[] result = BRunUtil.invoke(compileResult, "testMultilevelClosure");
         Assert.assertEquals(result[0].stringValue(), "72");
     }

     @Test(description = "Test basic worker")
     public void testWorkerBasic() {
         BValue[] result = BRunUtil.invoke(compileResult, "basicWorkerTest");
         Assert.assertEquals(result[0].stringValue(), "120");
     }
 }
