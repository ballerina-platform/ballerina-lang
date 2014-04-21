package org.wso2.siddhi.core.util.parser;

import org.wso2.siddhi.core.partition.executor.PartitionExecutor;
import org.wso2.siddhi.query.api.definition.partition.PartitionDefinition;
import org.wso2.siddhi.query.api.definition.partition.PartitionType;
import org.wso2.siddhi.query.api.definition.partition.VariablePartitionType;

import java.util.List;

public class PartitionParser {
    public static List<PartitionExecutor> parsePartition(PartitionDefinition partitionDefinition) {
        for(PartitionType partitionType:partitionDefinition.getPartitionTypeList()){
            if(partitionType instanceof VariablePartitionType){
//                partitionType
            }

        }
        return null;
    }


}