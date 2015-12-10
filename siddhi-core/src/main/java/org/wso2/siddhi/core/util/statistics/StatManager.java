package org.wso2.siddhi.core.util.statistics;

/**
 * Created by sajith on 12/3/15.
 */
public class StatManager {
    public enum RunningMode{
        SIDDHI_ONLY, CEP, DISTRIBUTED
    }

    StatisticsTrackerFactory factory;
    Class<?> reporterType;
    RunningMode runningMode;

    public StatManager(StatisticsTrackerFactory factory, Class<?> reporterType, RunningMode runningMode){
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
}


