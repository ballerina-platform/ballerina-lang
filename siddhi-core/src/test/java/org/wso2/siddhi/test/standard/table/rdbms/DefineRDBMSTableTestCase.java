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
package org.wso2.siddhi.test.standard.table.rdbms;

import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.DifferentDefinitionAlreadyExistException;
import org.wso2.siddhi.core.exception.QueryCreationException;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import javax.sql.DataSource;

public class DefineRDBMSTableTestCase {

    private static String dataSourceName = "cepDataSource";
    private DataSource dataSource = new BasicDataSource();


    @Test
    public void testSingleDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);
        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name("cseEventTable").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT);
        tableDefinition.from("datasource.name", "cepDataSource").from("table.name", "testTable");
        siddhiManager.defineTable(tableDefinition);
    }

    @Test
    public void testSingleDefinitionQuery() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);
        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) from ('datasource.name'= 'cepDataSource', 'table.name'='epTestTable', 'create.query'='CREATE TABLE cepdb.epTestTable (name VARCHAR(40), address VARCHAR(200))')");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition1() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) from ('datasource.name'='cepDataSource','table.name'='cepEventTable0')");
        siddhiManager.defineTable("define table cseEventTable(symbols string, price int, volume float) from ('datasource.name'='cepDataSource','table.name'='cepEventTable0')");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, volume float) from ('datasource.name'='cepDataSource','table.name'='cepEventTableDiff')");
        siddhiManager.defineTable("define table cseEventTable(symbols string, price int, volume float) from ('datasource.name'='cepDataSource','table.name'='cepEventTableDiff')");
    }

    @Test
    public void testAddingTwoSameDefinition3() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) ");
        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) ");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition4() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventTable(symbol string, price int, volume float) ");
        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float) from ('datasource.name'='cepDataSource','table.name'='cepEventTableTwoDiff') ");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testAddingTwoSameDefinition5() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table cseEventTable(symbol string, price int, volume float)  from ('datasource.name'='cepDataSource','table.name'='cepEventTableTwoDiff')");
        siddhiManager.defineStream("define stream cseEventTable(symbol string, price int, volume float)");
    }

    @Test(expected = DifferentDefinitionAlreadyExistException.class)
    public void testOutputDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "select * " +
                "insert into OutputStream ;");


        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from ('datasource.name'='cepDataSource','table.name'='cepEventTable1')");
    }

    @Test
    public void testOutputDefinition2() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from ('datasource.name'='cepDataSource','table.name'='cepEventTable2')");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "select * " +
                "insert into OutputStream ;");

    }

    @Test(expected = QueryCreationException.class)
    public void testOutputDefinition3() {
        String ts = Float.toString(Float.MIN_VALUE);
        System.out.println(ts.length());
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from ('datasource.name'='cepDataSource','table.name'='cepEventTable') ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long, time long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "select * " +
                "insert into OutputStream ;");
    }

    @Test(expected = QueryCreationException.class)
    public void testOutputDefinition4() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from ('datasource.name'='cepDataSource','table.name'='cepEventTable4') ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume int) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "select * " +
                "insert into OutputStream ;");

    }

    @Test
    public void testOutputDefinition5() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineTable("define table OutputStream (symbol string, price float, volume long) from ('datasource.name'='cepDataSource','table.name'='cepEventTable5') ");
        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, test long) ");
        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "select * " +
                "insert into OutputStream ;");

    }

    @Test
    public void testSingleerDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name("cseEventTable").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT).from("datasource.name", "cepDataSource");
        tableDefinition.from("datasource.name", "cepDataSource").from("table.name", "testEventTable");

        siddhiManager.defineTable(tableDefinition);
    }


}
