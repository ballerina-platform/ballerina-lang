package org.ballerinalang.net.http.nativeimpl.connection;

import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.DataContext;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

/**
 * Created by rukshani on 8/21/18.
 */
public class PipelinedResponse implements Comparable<PipelinedResponse> {

    private final HttpCarbonMessage outboundResponseMsg;
    HttpCarbonMessage inboundRequestMsg;
    DataContext dataContext;
    BMap<String, BValue> outboundResponseStruct;
    private final int sequenceId;

    public PipelinedResponse(int sequenceId, HttpCarbonMessage inboundRequestMsg, HttpCarbonMessage outboundResponseMsg,
                             DataContext dataContext, BMap<String, BValue> outboundResponseStruct) {
        this.inboundRequestMsg = inboundRequestMsg;
        this.outboundResponseMsg = outboundResponseMsg;
        this.dataContext = dataContext;
        this.outboundResponseStruct = outboundResponseStruct;
        this.sequenceId = sequenceId;
    }

    public PipelinedResponse(HttpCarbonMessage response, int sequenceId) {
        this.outboundResponseMsg = response;
        this.sequenceId = sequenceId;
    }

    public int getSequenceId() {
        return sequenceId;
    }


    public HttpCarbonMessage getOutboundResponseMsg() {
        return outboundResponseMsg;
    }

    public HttpCarbonMessage getInboundRequestMsg() {
        return inboundRequestMsg;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public BMap<String, BValue> getOutboundResponseStruct() {
        return outboundResponseStruct;
    }

    @Override
    public int compareTo(PipelinedResponse other) {
        return this.sequenceId - other.getSequenceId();
    }
}
