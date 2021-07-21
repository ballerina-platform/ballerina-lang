/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package toml.parser.test.api.object;

import io.ballerina.toml.api.Toml;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Contains the tests to check object mapper functionality.
 *
 * @since 2.0.0
 */
public class ToObjectTest {

    @Test
    public void testToMap() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("object/complex.toml");

        Toml read = Toml.read(inputStream);
        Map<String, Object> map = read.toMap();
        String simplekv = (String) map.get("simplekv");
        Assert.assertEquals(simplekv, "simplekv value");
        String simplekv1 = (String) map.get("simplekv1");
        Assert.assertEquals(simplekv1, "simplekv1 value");
        long simpleint = (Long) map.get("simpleint");
        Assert.assertEquals(simpleint, 11);
        boolean simplebool = (Boolean) map.get("simplebool");
        Assert.assertFalse(simplebool);
        List<Long> simpleArr = (List<Long>) map.get("simpleArr");
        Assert.assertEquals(simpleArr.get(0), Long.valueOf(1));
        Assert.assertEquals(simpleArr.get(1), Long.valueOf(5));
        Assert.assertEquals(simpleArr.get(2), Long.valueOf(7));

        Map<String, Object> table = (Map<String, Object>) map.get("table");
        String tableKv = (String) table.get("tableKv");
        Assert.assertEquals(tableKv, "table kv");
        String tableKv1 = (String) table.get("tableKv1");
        Assert.assertEquals(tableKv1, "table kv1");

        Map<String, Object> childTable = (Map<String, Object>) table.get("child");
        String childTableKv = (String) childTable.get("tableKvChild");
        Assert.assertEquals(childTableKv, "table kv child");
        String childTableKv1 = (String) childTable.get("tableKv1Child");
        Assert.assertEquals(childTableKv1, "table kv1 child");

        Map<String, Object> grandChildTable = (Map<String, Object>) childTable.get("grandchild");
        String grandChildTableKv = (String) grandChildTable.get("tableKvGrandChild");
        Assert.assertEquals(grandChildTableKv, "table kv grandchild");
        String grandChildTableKv1 = (String) grandChildTable.get("tableKv1GrandChild");
        Assert.assertEquals(grandChildTableKv1, "table kv1 grandchild");

        List<Map<String, Object>> tableArr = (List<Map<String, Object>>) map.get("tableArr");
        
        Map<String, Object> firstTable = tableArr.get(0);
        String firstTableStr = (String) firstTable.get("tableKv");
        Assert.assertEquals(firstTableStr, "tableArr kv first");
        String firstTableStr1 = (String) firstTable.get("tableKv1");
        Assert.assertEquals(firstTableStr1, "tableArr kv1 first");

        Map<String, Object> secondTable = tableArr.get(1);
        String secondTableStr = (String) secondTable.get("tableKv");
        Assert.assertEquals(secondTableStr, "tableArr kv2 second");

        Map<String, Object> thirdTable = tableArr.get(2);
        String thirdTableStr = (String) thirdTable.get("tableKv");
        Assert.assertEquals(thirdTableStr, "tableArr kv third");
        String thirdTableStr1 = (String) thirdTable.get("tableKv1");
        Assert.assertEquals(thirdTableStr1, "tableArr kv1 third");
    }

    @Test
    public void testToObject() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("object/complex.toml");

        Toml read = Toml.read(inputStream);
        NewObject newObject = read.to(NewObject.class);
        String simplekv = newObject.getSimplekv();
        Assert.assertEquals(simplekv, "simplekv value");
        String simplekv1 = newObject.getSimplekv1();
        Assert.assertEquals(simplekv1, "simplekv1 value");
        long simpleint = newObject.getSimpleint();
        Assert.assertEquals(simpleint, 11);
        boolean simplebool = newObject.isSimplebool();
        Assert.assertFalse(simplebool);
        List<Long> simpleArr = newObject.getSimpleArr();
        Assert.assertEquals(simpleArr.get(0), Long.valueOf(1));
        Assert.assertEquals(simpleArr.get(1), Long.valueOf(5));
        Assert.assertEquals(simpleArr.get(2), Long.valueOf(7));

        Table table = newObject.getTable();
        String tableKv = table.getTableKv();
        Assert.assertEquals(tableKv, "table kv");
        String tableKv1 = table.getTableKv1();
        Assert.assertEquals(tableKv1, "table kv1");

        Child childTable = table.getChild();
        String childTableKv = childTable.getTableKvChild();
        Assert.assertEquals(childTableKv, "table kv child");
        String childTableKv1 = childTable.getTableKv1Child();
        Assert.assertEquals(childTableKv1, "table kv1 child");

        GrandChild grandChildTable = childTable.getGrandChild();
        String grandChildTableKv = grandChildTable.getTableKvGrandChild();
        Assert.assertEquals(grandChildTableKv, "table kv grandchild");
        String grandChildTableKv1 = grandChildTable.getTableKv1GrandChild();
        Assert.assertEquals(grandChildTableKv1, "table kv1 grandchild");

        List<TableArr> tableArr = newObject.getTableArr();
        
        TableArr firstTable = tableArr.get(0);
        String firstTableStr = firstTable.getTableKv();
        Assert.assertEquals(firstTableStr, "tableArr kv first");
        String firstTableStr1 = firstTable.getTableKv1();
        Assert.assertEquals(firstTableStr1, "tableArr kv1 first");

        TableArr secondTable = tableArr.get(1);
        String secondTableStr = secondTable.getTableKv();
        Assert.assertEquals(secondTableStr, "tableArr kv2 second");

        TableArr thirdTable = tableArr.get(2);
        String thirdTableStr = thirdTable.getTableKv();
        Assert.assertEquals(thirdTableStr, "tableArr kv third");
        String thirdTableStr1 = thirdTable.getTableKv1();
        Assert.assertEquals(thirdTableStr1, "tableArr kv1 third");
    }
    
    class NewObject { 
        private String simplekv;
        private String simplekv1;
        private int simpleint;
        private double simplefloat;
        private boolean simplebool;
        private List<Long> simpleArr;
        private Table table;
        private List<TableArr> tableArr;

        public String getSimplekv() {
            return simplekv;
        }

        public String getSimplekv1() {
            return simplekv1;
        }

        public int getSimpleint() {
            return simpleint;
        }

        public double getSimplefloat() {
            return simplefloat;
        }

        public boolean isSimplebool() {
            return simplebool;
        }

        public List<Long> getSimpleArr() {
            return simpleArr;
        }

        public Table getTable() {
            return table;
        }

        public List<TableArr> getTableArr() {
            return tableArr;
        }
    }
    
    class Table {
        private String tableKv;
        private String tableKv1;
        private Child child;

        public String getTableKv() {
            return tableKv;
        }

        public String getTableKv1() {
            return tableKv1;
        }

        public Child getChild() {
            return child;
        }
    }

    class Child {
        private String tableKvChild;
        private String tableKv1Child;
        private GrandChild grandchild;

        public String getTableKvChild() {
            return tableKvChild;
        }

        public String getTableKv1Child() {
            return tableKv1Child;
        }

        public GrandChild getGrandChild() {
            return grandchild;
        }
    }

    class GrandChild {
        private String tableKvGrandChild;
        private String tableKv1GrandChild;

        public String getTableKvGrandChild() {
            return tableKvGrandChild;
        }

        public String getTableKv1GrandChild() {
            return tableKv1GrandChild;
        }
    }
    
    class TableArr {
        private String tableKv;
        private String tableKv1;

        public String getTableKv() {
            return tableKv;
        }

        public String getTableKv1() {
            return tableKv1;
        }
    }
}
