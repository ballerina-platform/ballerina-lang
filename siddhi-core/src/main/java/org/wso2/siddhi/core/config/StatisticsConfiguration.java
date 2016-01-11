package org.wso2.siddhi.core.config;

import org.wso2.siddhi.core.util.statistics.StatisticsTrackerFactory;

public class StatisticsConfiguration {

    private String matricPrefix= "org.wso2.siddhi";
    private StatisticsTrackerFactory factory;

    public StatisticsConfiguration(StatisticsTrackerFactory factory){
        this.factory = factory;
    }

    public StatisticsTrackerFactory getFactory(){
        return factory;
    }

    public String getMatricPrefix() {
        return matricPrefix;
    }

    public void setMatricPrefix(String matricPrefix) {
        this.matricPrefix = matricPrefix;
    }
}


