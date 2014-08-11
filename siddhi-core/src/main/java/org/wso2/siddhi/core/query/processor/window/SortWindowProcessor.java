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
package org.wso2.siddhi.core.query.processor.window;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.StreamEvent;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.remove.RemoveEvent;
import org.wso2.siddhi.core.query.QueryPostProcessingElement;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/*
* Sample Query:
* from inputStream#window.sort(5, attribute1, "asc", attribute2, "desc")
* select attribute1, attribute2
* insert into outputStream;
*
* Description:
* In the example query given, 5 is the size of the window.
* The arguements following the size of the window are optional.
* If neither "asc" nor "desc" is given for a certain attribute, order defaults to "asc"
* */
public class SortWindowProcessor extends WindowProcessor {

    private int lengthToKeep;
    private ArrayList<Event> sortedWindow;
    private ArrayList<int[]> attributeList;
    private EventComparator eventComparator;

    private static final String ASC = "asc";
    private static final String DESC = "desc";

    private class EventComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            int comparisonResult;
            for (int i=0 ; i<attributeList.size(); i++){
                int[] listItem = attributeList.get(i);
                int attributePosition = listItem[0];
                Comparable comparableVariable1 = (Comparable)e1.getData(attributePosition);
                Comparable comparableVariable2 = (Comparable)e2.getData(attributePosition);
                comparisonResult = comparableVariable1.compareTo(comparableVariable2);
                if(comparisonResult != 0){
                    return listItem[1]*comparisonResult;
                }
            }
            return 0;
        }
    }

    @Override
    protected void processEvent(InEvent event) {
        acquireLock();
        try {
            sortedWindow.add(new RemoveEvent(event, Long.MAX_VALUE));
            if (sortedWindow.size() > lengthToKeep) {
                Collections.sort(sortedWindow, eventComparator);
                nextProcessor.process(new RemoveEvent(sortedWindow.remove(sortedWindow.size()-1), System.currentTimeMillis()));
            }
            nextProcessor.process(event);
        } finally {
            releaseLock();
        }

    }

    @Override
    protected void processEvent(InListEvent listEvent) {
        acquireLock();
        try {
            int windowVacancySize = (lengthToKeep - sortedWindow.size());
            if (listEvent.getActiveEvents() > windowVacancySize) {
                InEvent[] newEvents = new InEvent[windowVacancySize];
                int index = 0;
                for (int i = 0; i < listEvent.getActiveEvents(); i++) {
                    InEvent inEvent = (InEvent) listEvent.getEvent(i);
                    if (index < windowVacancySize - 1) {
                        newEvents[index] = inEvent;
                        sortedWindow.add(new RemoveEvent(inEvent, Long.MAX_VALUE));
                        index++;
                    } else if (index == windowVacancySize - 1) {
                        newEvents[index] = inEvent;
                        sortedWindow.add(new RemoveEvent(inEvent, Long.MAX_VALUE));
                        index++;
                        nextProcessor.process(new InListEvent(newEvents));
                    } else {
                        sortedWindow.add(new RemoveEvent(inEvent, Long.MAX_VALUE));
                        Collections.sort(sortedWindow, eventComparator);
                        RemoveEvent removeEvent = (RemoveEvent) sortedWindow.remove(sortedWindow.size()-1);
                        removeEvent.setExpiryTime(System.currentTimeMillis());
                        nextProcessor.process(removeEvent);
                        nextProcessor.process(inEvent);
                    }
                }
            } else {
                for (int i = 0; i < listEvent.getActiveEvents(); i++) {
                    sortedWindow.add(new RemoveEvent(listEvent.getEvent(i), Long.MAX_VALUE));
                }
                nextProcessor.process(listEvent);
            }
        } finally {
            releaseLock();
        }

    }

    @Override
    public Iterator<StreamEvent> iterator() {
        return new ArrayList<StreamEvent>(sortedWindow).iterator();
    }

    @Override
    public Iterator<StreamEvent> iterator(String predicate) {
        //TODO: Handle isDistributedProcessingEnabled case
        /* if (siddhiContext.isDistributedProcessingEnabled()) {

        } else {
            return this.iterator();
        }*/
        return this.iterator(); //adding this return statement, until the distributed case is handled.
    }

    @Override
    protected Object[] currentState() {
        Object[] currentState = new Object[]{sortedWindow};
        return currentState;
    }

    @Override
    protected void restoreState(Object[] data) {
        sortedWindow = (ArrayList<Event>)data[0];
    }

    @Override
    protected void init(Expression[] parameters, QueryPostProcessingElement nextProcessor, AbstractDefinition streamDefinition, String elementId, boolean async, SiddhiContext siddhiContext) {
        if (parameters[0] instanceof IntConstant){
            lengthToKeep = ((IntConstant) parameters[0]).getValue();
        } else {
            throw new UnsupportedOperationException("The first parameter should be an integer");
        }
        attributeList = new ArrayList<int[]>();
        eventComparator = new EventComparator();
        for (int i = 1, parametersLength = parameters.length  ; i < parametersLength; i++) {
            if (parameters[i] instanceof StringConstant){
                throw new UnsupportedOperationException("Required a variable, but found a string parameter");
            } else{
                String attributeName = ((Variable) parameters[i]).getAttributeName();
                int position = definition.getAttributePosition(attributeName);
                int order;
                String nextParameter;
                if(i+1 < parametersLength && parameters[i+1] instanceof StringConstant){
                    nextParameter = ((StringConstant) parameters[i+1]).getValue();
                    if (nextParameter.equalsIgnoreCase(DESC)){
                        order = -1;
                        i++;
                    } else if (nextParameter.equalsIgnoreCase(ASC)){
                        order = 1;
                        i++;
                    } else {
                        throw new UnsupportedOperationException("Parameter string literals should only be \"asc\" or \"desc\"");
                    }
                } else {
                    order = 1; //assigning the default order: "asc"
                }
                attributeList.add(new int[]{position,order});
            }
        }

        if (this.siddhiContext.isDistributedProcessingEnabled()) {
            //TODO: Handle isDistributedProcessingEnabled case
        } else {
            sortedWindow = new ArrayList<Event>();
        }

    }

    @Override
    public void destroy(){
    }
}
