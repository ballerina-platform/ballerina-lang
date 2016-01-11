package org.wso2.siddhi.core.util.statistics;

public class StatisticsConfiguration {
    public enum RunningMode{
        SIDDHI_ONLY, CEP, DISTRIBUTED
    }

    String matricPrefix= "org.wso2.siddhi";
    StatisticsTrackerFactory factory;
    Class<?> reporterType;
    RunningMode runningMode;

    public StatisticsConfiguration(StatisticsTrackerFactory factory, Class<?> reporterType, RunningMode runningMode){
        this.factory = factory;
        this.reporterType = reporterType;
        this.runningMode = runningMode;
    }

    public StatisticsTrackerFactory getFactory(){
        return factory;
    }

    public Class<?> getReporterType(){
        return reporterType;
    }

    public RunningMode getRunningMode(){
        return this.runningMode;
    }

    public String getMatricPrefix() {
        return matricPrefix;
    }

    public void setMatricPrefix(String matricPrefix) {
        this.matricPrefix = matricPrefix;
    }
}


