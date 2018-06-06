/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.siddhi.core;

import org.ballerinalang.siddhi.core.config.SiddhiContext;
import org.ballerinalang.siddhi.core.config.StatisticsConfiguration;
import org.ballerinalang.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.ballerinalang.siddhi.core.stream.input.source.SourceHandlerManager;
import org.ballerinalang.siddhi.core.stream.output.sink.SinkHandlerManager;
import org.ballerinalang.siddhi.core.table.record.RecordTableHandlerManager;
import org.ballerinalang.siddhi.core.util.SiddhiAppRuntimeBuilder;
import org.ballerinalang.siddhi.core.util.config.ConfigManager;
import org.ballerinalang.siddhi.core.util.parser.SiddhiAppParser;
import org.ballerinalang.siddhi.core.util.persistence.PersistenceStore;
import org.ballerinalang.siddhi.query.api.SiddhiApp;
import org.ballerinalang.siddhi.query.compiler.SiddhiCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.sql.DataSource;

/**
 * This is the main interface class of Siddhi where users will interact when using Siddhi as a library.
 */
public class SiddhiManager {

    private static final Logger log = LoggerFactory.getLogger(SiddhiManager.class);
    private SiddhiContext siddhiContext;
    private ConcurrentMap<String, SiddhiAppRuntime> siddhiAppRuntimeMap = new ConcurrentHashMap<String,
            SiddhiAppRuntime>();

    /**
     * Creates a Siddhi Manager instance with default {@link SiddhiContext}. This is the only method to create a new
     * Siddhi Manager instance which is the main interface when you use Siddhi as a library.
     */
    public SiddhiManager() {
        siddhiContext = new SiddhiContext();
    }

    public SiddhiAppRuntime createSiddhiAppRuntime(SiddhiApp siddhiApp) {
        return createSiddhiAppRuntime(siddhiApp, null);
    }

    private SiddhiAppRuntime createSiddhiAppRuntime(SiddhiApp siddhiApp, String siddhiAppString) {
        SiddhiAppRuntimeBuilder siddhiAppRuntimeBuilder = SiddhiAppParser.parse(siddhiApp, siddhiAppString,
                siddhiContext);
        siddhiAppRuntimeBuilder.setSiddhiAppRuntimeMap(siddhiAppRuntimeMap);
        SiddhiAppRuntime siddhiAppRuntime = siddhiAppRuntimeBuilder.build();
        siddhiAppRuntimeMap.put(siddhiAppRuntime.getName(), siddhiAppRuntime);
        return siddhiAppRuntime;
    }

    public SiddhiAppRuntime createSiddhiAppRuntime(String siddhiApp) {
        return createSiddhiAppRuntime(SiddhiCompiler.parse(siddhiApp), siddhiApp);
    }

    /**
     * Method to retrieve already submitted siddhi app by providing the name.
     *
     * @param siddhiAppName Name of the required Siddhi app
     * @return Siddhi app Runtime representing the provided name
     */
    public SiddhiAppRuntime getSiddhiAppRuntime(String siddhiAppName) {
        return siddhiAppRuntimeMap.get(siddhiAppName);
    }

    public void validateSiddhiApp(SiddhiApp siddhiApp) {
        validateSiddhiApp(siddhiApp, null);
    }

    private void validateSiddhiApp(SiddhiApp siddhiApp, String siddhiAppString) {
        final SiddhiAppRuntime siddhiAppRuntime = SiddhiAppParser.parse(siddhiApp, siddhiAppString,
                siddhiContext).build();
        siddhiAppRuntime.start();
        siddhiAppRuntime.shutdown();
    }

    public void validateSiddhiApp(String siddhiApp) {
        validateSiddhiApp(SiddhiCompiler.parse(siddhiApp), siddhiApp);
    }

    /**
     * Method to set persistence for the Siddhi Manager instance.
     * {@link org.ballerinalang.siddhi.core.util.persistence.InMemoryPersistenceStore} is the default persistence store
     * implementation users can utilize.
     *
     * @param persistenceStore Persistence Store implementation to be used.
     */
    public void setPersistenceStore(PersistenceStore persistenceStore) {
        this.siddhiContext.setPersistenceStore(persistenceStore);
    }

    /**
     * Method to set sink handler manager that would create sink handlers for each sink.
     *
     * @param sinkHandlerManager Sink Handler Manager Implementation to be used.
     */
    public void setSinkHandlerManager(SinkHandlerManager sinkHandlerManager) {
        this.siddhiContext.setSinkHandlerManager(sinkHandlerManager);
    }

    /**
     * Method to set source handler manager that would create source handlers for each source.
     *
     * @param sourceHandlerManager Source Handler Manager Implementation to be used.
     */
    public void setSourceHandlerManager(SourceHandlerManager sourceHandlerManager) {
        this.siddhiContext.setSourceHandlerManager(sourceHandlerManager);
    }

    /**
     * Method to set record table handler manager that would create record table handlers for each record table.
     *
     * @param recordTableHandlerManager Record Table Handler Manager Implementation to be used.
     */
    public void setRecordTableHandlerManager(RecordTableHandlerManager recordTableHandlerManager) {
        this.siddhiContext.setRecordTableHandlerManager(recordTableHandlerManager);
    }

    /**
     * Method to set configManager for the Siddhi Manager instance.
     *
     * @param configManager Config Manager implementation to be used.
     */
    public void setConfigManager(ConfigManager configManager) {
        this.siddhiContext.setConfigManager(configManager);
    }

    /**
     * Method used to register extensions to the Siddhi Manager. But extension classes should be present in classpath.
     *
     * @param name  Name of the extension as mentioned in the annotation.
     * @param clazz Class name of the implementation
     */
    public void setExtension(String name, Class clazz) {
        siddhiContext.getSiddhiExtensions().put(name, clazz);
    }

    /**
     * Method used to get the extensions registered in the siddhi manager.
     *
     * @return Extension name to class map
     */
    public Map<String, Class> getExtensions() {
        return siddhiContext.getSiddhiExtensions();
    }

    /**
     * Method used to remove the extensions registered in the siddhi manager.
     *
     * @param name Name of the extension as given in the annotation.
     */
    public void removeExtension(String name) {
        siddhiContext.getSiddhiExtensions().remove(name);
    }

    /**
     * Method used to add Carbon DataSources to Siddhi Manager to utilize them for event tables.
     *
     * @param dataSourceName Name of the DataSource
     * @param dataSource     Object representing DataSource
     */
    public void setDataSource(String dataSourceName, DataSource dataSource) {
        siddhiContext.addSiddhiDataSource(dataSourceName, dataSource);
    }

    /**
     * Method to integrate Carbon Metrics into Siddhi.
     *
     * @param statisticsConfiguration statistics configuration
     */
    public void setStatisticsConfiguration(StatisticsConfiguration statisticsConfiguration) {
        siddhiContext.setStatisticsConfiguration(statisticsConfiguration);
    }

    /**
     * Method used to get all SiddhiAppRuntimes.
     *
     * @return siddhiAppRuntimeMap
     */
    public ConcurrentMap<String, SiddhiAppRuntime> getSiddhiAppRuntimeMap() {
        return siddhiAppRuntimeMap;
    }

    /**
     * Method to shutdown Siddhi Manager.
     */
    public void shutdown() {
        List<String> siddhiAppNames = new ArrayList<String>(siddhiAppRuntimeMap.keySet());
        for (String siddhiAppName : siddhiAppNames) {
            siddhiAppRuntimeMap.get(siddhiAppName).shutdown();
        }
    }

    /**
     * Method used persist state of current Siddhi Manager instance. This will internally call all SiddhiAppRuntimes
     * within Siddhi Manager.
     */
    public void persist() {
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            siddhiAppRuntime.persist();
        }
    }

    /**
     * Method used to restore state of Current Siddhi Manager instance. This will internally call all
     * SiddhiAppRuntimes
     * within Siddhi Manager.
     */
    public void restoreLastState() {
        for (SiddhiAppRuntime siddhiAppRuntime : siddhiAppRuntimeMap.values()) {
            try {
                siddhiAppRuntime.restoreLastRevision();
            } catch (CannotRestoreSiddhiAppStateException e) {
                log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
            }
        }
    }
}
