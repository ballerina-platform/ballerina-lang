package io.ballerina.runtime.internal.testable;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.scheduling.Strand;

public class TestArguments {

    private static final int ARGUMENTS_NUMBER = 10;

    private final Class<?>[] argTypes;
    private final Object[] argValues;

    public TestArguments(String... args) {
        argTypes = new Class[ARGUMENTS_NUMBER + 1];
        argValues = new Object[ARGUMENTS_NUMBER + 1];

        argTypes[0] = Strand.class;
        argValues[0] = null;

        assignValues(0, args[0]);
        assignValues(1, args[1]);
        assignValues(2, args[2]);
        assignValues(3, args[3]);
        assignValues(4, args[4]);
        assignValues(5, args[5]);
        assignValues(6, args[6]);
        assignValues(7, args[7]);
        assignValues(8, args[8]);
        assignValues(9, args[9]);
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public Object[] getArgValues() {
        return argValues;
    }

    private void assignValues(int position, String argValue) {
        int index = position + 1;
        argTypes[index] = BString.class;
        argValues[index] = StringUtils.fromString(argValue);
    }
}
