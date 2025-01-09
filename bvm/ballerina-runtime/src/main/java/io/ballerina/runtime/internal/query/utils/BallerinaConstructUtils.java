//package io.ballerina.runtime.internal.query.utils;
//
//import io.ballerina.runtime.api.values.*;
//import io.ballerina.runtime.internal.values.ArrayValueImpl;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
///**
// * Utility class for constructing Ballerina-specific outputs (list, map, table, stream, xml).
// */
//public class BallerinaConstructUtils {
//
//    public static Object construct(Class<?> completionType, Stream<Object> emittedValues) {
//        if (BArray.class.isAssignableFrom(completionType)) {
//            return constructList(emittedValues);
////        } else if (BMap.class.isAssignableFrom(completionType)) {
////            return constructMap(emittedValues);
////        } else if (BStream.class.isAssignableFrom(completionType)) {
////            return constructStream(emittedValues);
////        } else if (BXml.class.isAssignableFrom(completionType)) {
////            return constructXml(emittedValues);
//        } else {
//            throw new IllegalArgumentException("Unsupported completion type: " + completionType.getName());
//        }
//    }
//
//    private static BArray constructList(Stream<Object> emittedValues) {
//        // Collect emitted values into a list and return as a BArrayValue
//        List<Object> list = emittedValues.collect(Collectors.toList());
//
//        return new ArrayValueImpl(list, );
//    }
//
////    private static BMap constructMap(Stream<Object> emittedValues) {
////        // Construct a BMap from emitted values
////        BMap map = new BMap<>();
////        emittedValues.forEach(value -> {
////            if (value instanceof Map.Entry<?, ?> entry) {
////                map.put(entry.getKey().toString(), entry.getValue());
////            } else {
////                throw new IllegalArgumentException("Invalid value for map construction: " + value);
////            }
////        });
////        return map;
////    }
////
////    private static BStream constructStream(Stream<Object> emittedValues) {
////        // Convert emitted values into a BStream
////        return new BStream(emittedValues.iterator());
////    }
////
////    private static BXml constructXml(Stream<Object> emittedValues) {
////        // Concatenate emitted values to form an XML string
////        StringBuilder xmlBuilder = new StringBuilder();
////        emittedValues.forEach(value -> {
////            if (value instanceof String) {
////                xmlBuilder.append(value);
////            } else {
////                throw new IllegalArgumentException("Invalid value for XML construction: " + value);
////            }
////        });
////        return new BXml(xmlBuilder.toString());
////    }
//}
