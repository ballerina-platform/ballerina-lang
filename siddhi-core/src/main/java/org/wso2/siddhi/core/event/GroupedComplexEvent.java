package org.wso2.siddhi.core.event;

public class GroupedComplexEvent implements ComplexEvent {

    private String groupKey;
    private final ComplexEvent complexEvent;
    private ComplexEvent next;

    public GroupedComplexEvent(String groupKey, ComplexEvent complexEvent) {
        this.groupKey = groupKey;
        this.complexEvent = complexEvent;
    }

    @Override
    public ComplexEvent getNext() {
        return next;
    }

    @Override
    public void setNext(ComplexEvent events) {
        next = events;
    }

    @Override
    public Object[] getOutputData() {
        return complexEvent.getOutputData();
    }

    @Override
    public void setOutputData(Object object, int index) {
        complexEvent.setOutputData(object, index);
    }

    @Override
    public long getTimestamp() {
        return complexEvent.getTimestamp();
    }

    @Override
    public Object getAttribute(int[] position) {
        return complexEvent.getAttribute(position);
    }

    @Override
    public Type getType() {
        return complexEvent.getType();
    }

    @Override
    public void setType(Type type) {
        complexEvent.setType(type);
    }

    public ComplexEvent getComplexEvent() {
        return complexEvent;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }
}
