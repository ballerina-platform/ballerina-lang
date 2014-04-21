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

import org.apache.log4j.Logger;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.QueryFactory;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;

import javax.sql.DataSource;

public class RDBMSDeleteTestCase {
    static final Logger log = Logger.getLogger(RDBMSDeleteTestCase.class);

    private static String dataSourceName = "cepDataSource";
    private DataSource dataSource = new BasicDataSource();

    @Test
    public void testQuery1() throws InterruptedException {
        log.info("DeleteFromTableTestCase testQuery1 OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");

        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) " + createFromClause("cepDataSource", "cepEventTable", null));

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol=='GOOG';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"GOOG", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }


    @Test
    public void testQuery1LFU() throws InterruptedException {
        log.info("DeleteFromTableTestCase testQuery1LFU OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");

        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) " + createFromClause("cepDataSource", "cepEventTable", null, "lfu"));

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol=='GOOG';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"GOOG", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    public void testQuery1LRU() throws InterruptedException {
        log.info("DeleteFromTableTestCase testQuery1LRU OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");

        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) " + createFromClause("cepDataSource", "cepEventTable", null, "lru"));

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol=='GOOG';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"GOOG", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }


    @Test
    public void testQuery2() throws InterruptedException {
        log.info("DeleteFromTableTestCase test5 OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long)  " + createFromClause("cepDataSource", "cepEventTable", null));

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol!='GOOG';");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"GOOG", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }

    @Test
    /*
     delete with AND condition. condition uses same parameter twice.
     */
    public void testQuery3() throws InterruptedException {
        log.info("DeleteFromTableTestCase test5 OUT size 2");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        siddhiManager.defineStream("define stream cseEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineStream("define stream cseDeleteEventStream (symbol string, price float, volume long) ");
        siddhiManager.defineTable("define table cseEventTable (symbol string, price float, volume long) " + createFromClause("cepDataSource", "cepEventTable", null));

        String queryReference = siddhiManager.addQuery("from cseEventStream " +
                "insert into cseEventTable;");

        siddhiManager.addQuery("from cseDeleteEventStream " +
                "delete cseEventTable " +
                "    on cseEventTable.symbol != 'WSO2' ;");
        InputHandler cseEventStream = siddhiManager.getInputHandler("cseEventStream");
        InputHandler cseDeleteEventStream = siddhiManager.getInputHandler("cseDeleteEventStream");
        cseEventStream.send(new Object[]{"WSO2", 55.6f, 100l});
        cseEventStream.send(new Object[]{"GOOG", 75.6f, 100l});
        cseEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"GOOG", 57.6f, 100l});
        cseDeleteEventStream.send(new Object[]{"WSO2", 57.6f, 100l});
        Thread.sleep(500);
        siddhiManager.shutdown();

    }


    @Test
    public void testSingleerDefinition() {
        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.getSiddhiContext().addDataSource(dataSourceName, dataSource);

        TableDefinition tableDefinition = QueryFactory.createTableDefinition();
        tableDefinition.name("cseEventTable").attribute("symbol", Attribute.Type.STRING).attribute("price", Attribute.Type.INT).attribute("volume", Attribute.Type.FLOAT).from("datasource.name", "cepDataSource").from("table.name", "cepEventTable");
        siddhiManager.defineTable(tableDefinition);
    }

    private String createFromClause(String dataSourceName, String tableName, String createQuery) {
        return createFromClause(dataSourceName, tableName, createQuery, null);
    }

    private String createFromClause(String dataSourceName, String tableName, String createQuery, String algorithm) {
        String query = "from ( 'datasource.name'='" + dataSourceName +  "','table.name'='" + tableName + "'";
        if (createQuery != null) {
            query = query + ",'create.query'='" + createQuery + "'";
        }
        if (algorithm != null) {
            query = query + ", 'caching.algorithm'='" + algorithm + "'";
        }
        query = query + ")";
        return query;
    }

}
