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
package org.wso2.siddhi.query.api.query;

import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.condition.Condition;
import org.wso2.siddhi.query.api.query.input.AnonymousStream;
import org.wso2.siddhi.query.api.query.input.BasicStream;
import org.wso2.siddhi.query.api.query.input.Stream;
import org.wso2.siddhi.query.api.query.output.OutputRate;
import org.wso2.siddhi.query.api.query.output.stream.DeleteStream;
import org.wso2.siddhi.query.api.query.output.stream.InsertIntoStream;
import org.wso2.siddhi.query.api.query.output.stream.OutStream;
import org.wso2.siddhi.query.api.query.output.stream.UpdateStream;
import org.wso2.siddhi.query.api.query.selection.Selector;

public class Query implements ExecutionPlan {

    private Stream inputStream;
    private Selector selector = new Selector();
    private OutStream outStream;
    private String partitionId;
    private OutputRate outputRate;

    public Query from(Stream stream) {
        this.inputStream = stream;
        return this;
    }

    public Query select(Selector selector) {
        this.selector = selector;
        return this;
    }

    public Query outStream(OutStream outStream) {
        this.outStream = outStream;
        return this;
    }

    public Query insertInto(String outputStreamId, OutStream.OutputEventsFor outputEventsFor) {
        this.outStream = new InsertIntoStream(outputStreamId, outputEventsFor);
        return this;
    }


    public Query insertInto(String outputStreamId) {
        this.outStream = new InsertIntoStream(outputStreamId);
        return this;
    }
    
    public void partitionBy(String partitionId) {
    	this.partitionId = partitionId;
    }

    public BasicStream returnStream() {
        return new AnonymousStream(this);
    }

    public void deleteBy(String outputTableId, Condition deletingCondition) {
        this.outStream = new DeleteStream(outputTableId, deletingCondition);
    }

    public void deleteBy(String outputTableId, OutStream.OutputEventsFor outputEventsFor, Condition deletingCondition) {
        this.outStream = new DeleteStream(outputTableId, outputEventsFor, deletingCondition);
    }

    public void updateBy(String outputTableId, Condition deletingCondition) {
        this.outStream = new UpdateStream(outputTableId, deletingCondition);
    }

    public void updateBy(String outputTableId, OutStream.OutputEventsFor outputEventsFor, Condition updatingCondition) {
        this.outStream = new UpdateStream(outputTableId, outputEventsFor, updatingCondition);
    }

    public void output(OutputRate outputRate) {
        this.outputRate = outputRate;
    }

    public Stream getInputStream() {
        return inputStream;
    }

    public OutStream getOutputStream() {
        return outStream;
    }

    public Selector getSelector() {
        return selector;
    }

	public String getPartitionId() {
		return partitionId;
	}

    public OutputRate getOutputRate() {
        return outputRate;
    }
}
