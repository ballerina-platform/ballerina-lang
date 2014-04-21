package org.wso2.siddhi.core.query.processor.window;


import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.event.remove.RemoveListEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.core.util.collection.queue.ISiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.SiddhiQueue;
import org.wso2.siddhi.core.util.collection.queue.SiddhiQueueGrid;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LengthBatchWindowProcessor extends WindowProcessor {

    private int lengthToKeep;
    private List<InEvent> newEventList;
    private List<RemoveEvent> oldEventList;

    private ISiddhiQueue<StreamEvent> window;

    @Override
    protected void processEvent(InEvent event) {
        acquireLock();
        try {
            processLengthBatchWindow(event);
        } finally {
            releaseLock();
        }
    }

    @Override
    protected void processEvent(InListEvent listEvent) {
        acquireLock();
        try {
            Event[] events = listEvent.getEvents();
            for (int i = 0, events1Length = listEvent.getActiveEvents(); i < events1Length; i++) {
                processLengthBatchWindow((InEvent) events[i]);
            }
        } finally {
            releaseLock();
        }
    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return window.iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String predicate) {
        if (siddhiContext.isDistributedProcessingEnabled()) {
            return ((SiddhiQueueGrid<StreamEvent>) window).iterator(predicate);
        } else {
            return window.iterator();
        }
    }

    private void processLengthBatchWindow(InEvent event) {
        newEventList.add(event);
        if (log.isDebugEnabled()) {
            log.debug("newEventList size " + newEventList.size() + " with event " + event);
        }
        if (newEventList.size() == lengthToKeep) {

            //sending old events
            oldEventList.clear();
            while (true) {
                RemoveEvent oldEvent = (RemoveEvent) window.poll();
                if (oldEvent == null) {
                    break;
                } else {
                    oldEvent.setExpiryTime(System.currentTimeMillis());
                    oldEventList.add(oldEvent);
                }
            }
            if (oldEventList.size() > 0) {
                nextProcessor.process(new RemoveListEvent(oldEventList.toArray(new RemoveEvent[oldEventList.size()])));
                oldEventList.clear();
            }

            InEvent[] inEvents = newEventList.toArray(new InEvent[newEventList.size()]);
            for (InEvent inEvent : inEvents) {
                window.put(new RemoveEvent(inEvent, -1));
            }
            nextProcessor.process(new InListEvent(inEvents));

            newEventList.clear();
        }
    }


    @Override
    protected Object[] currentState() {
        return new Object[]{window.currentState(), oldEventList, newEventList};
    }

    @Override
    protected void restoreState(Object[] data) {
        window.restoreState((Object[]) data[0]);
        oldEventList = ((ArrayList<RemoveEvent>) data[1]);
        newEventList = ((ArrayList<InEvent>) data[2]);
    }

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        lengthToKeep = ((IntConstant) parameters[0]).getValue();

        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            window = new SiddhiQueueGrid<StreamEvent>(elementId, this.siddhiContext, this.async);
        } else {
            window = new SiddhiQueue<StreamEvent>();
        }
        oldEventList = new ArrayList<RemoveEvent>();
        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            newEventList = this.siddhiContext.getHazelcastInstance().getList(elementId + "-newEventList");
        } else {
            newEventList = new ArrayList<InEvent>();
        }

    }

    @Override
    public void destroy(){

    }

}
