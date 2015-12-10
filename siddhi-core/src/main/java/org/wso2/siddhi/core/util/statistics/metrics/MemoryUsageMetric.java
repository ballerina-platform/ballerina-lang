package org.wso2.siddhi.core.util.statistics.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Multiset;
import org.omg.CORBA.Object;
import org.wso2.siddhi.core.util.statistics.MemoryUsageTracker;
import org.wso2.siddhi.core.util.statistics.memory.measurer.ObjectGraphMeasurer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by sajith on 11/26/15.
 */
public class MemoryUsageMetric implements MemoryUsageTracker {
    public static final int WORD_LENGTH = Integer.parseInt(System.getProperty("sun.arch.data.model")) / 8;

    private ConcurrentMap<Object, ObjectMetric> registeredObjects = new ConcurrentHashMap<Object, ObjectMetric>();
    private String execPlanName;

    public MemoryUsageMetric(String execPlanName){
        this.execPlanName = execPlanName;
    }

    static long FootprintSizeEstimate(ObjectGraphMeasurer.Footprint footprint){
        long referencesMemory = WORD_LENGTH * footprint.getReferences();
        long primitivesMemory = 0;

        for (Multiset.Entry entry: footprint.getPrimitives().entrySet()){
            String dataType = entry.getElement().toString();
            if (dataType.equalsIgnoreCase("double") || dataType.equalsIgnoreCase("long")) {
                primitivesMemory += 8 * entry.getCount();
            } else if (dataType.equalsIgnoreCase("int") || dataType.equalsIgnoreCase("float")) {
                primitivesMemory += 4 * entry.getCount();
            } else if (dataType.equalsIgnoreCase("short") || dataType.equalsIgnoreCase("char")){
                primitivesMemory += 2 * entry.getCount();
            } else if (dataType.equalsIgnoreCase("byte") || dataType.equalsIgnoreCase("boolean")){
                primitivesMemory += 1 * entry.getCount();
            }
        }

        return (referencesMemory + primitivesMemory);
    }

    /**
     * Register the object that needs to be measured the memory usage
     *
     * @param object Object
     * @param name   An unique value to identify the object.
     */
    @Override
    public void registerObject(final Object object, String name) {
        if (registeredObjects.get(object) == null) {
            registeredObjects.put(object, new ObjectMetric(object, name));
        }
    }

    /**
     * @return Name of the memory usage tracker
     */
    @Override
    public String getName() {
        return null;
    }

    class ObjectMetric {
        private final MetricRegistry metricRegistry = MetricRegistryHolder.getMetricRegistry();
        private final Object object;
        private String name;

        public ObjectMetric(final Object object, String name){
            this.object = object;
            this.name = name;
            initMetric();
        }

        private void initMetric(){
            metricRegistry.register(MetricRegistry.name(MemoryUsageMetric.class, execPlanName, name),
                    new Gauge<Long>() {
                        @Override
                        public Long getValue() {
                            ObjectGraphMeasurer.Footprint footprint = ObjectGraphMeasurer.measure(object);
                            return FootprintSizeEstimate(footprint);
                        }
                    });
        }
    }
    
}
