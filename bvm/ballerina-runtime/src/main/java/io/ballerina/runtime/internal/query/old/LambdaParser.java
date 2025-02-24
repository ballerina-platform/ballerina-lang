package io.ballerina.runtime.internal.query.old;

import io.ballerina.runtime.api.values.BFunctionPointer;

import java.util.HashMap;
import java.util.Map;

public class LambdaParser {
    public static void parseLambda(BFunctionPointer func) {
        // Logging can be handled using a proper logging framework or removed entirely
        int a = 5;
        Map<Object , Object> map = new HashMap<>();
        func.copy(map);
        a = 9;
    }
}
