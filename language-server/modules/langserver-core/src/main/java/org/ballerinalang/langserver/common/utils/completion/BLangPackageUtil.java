package org.ballerinalang.langserver.common.utils.completion;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;

/**
 * Created by nadeeshaan on 6/9/18.
 */
public class BLangPackageUtil {
    public static ObjectCategories getObjectCategories(BLangPackage bLangPackage) {
        ObjectCategories objectCategories = new ObjectCategories("SSSS");
//        List<BLangOb> objects = bLangPackage.get
//
//        for (BLangObject object : objects) {
//            // Filter the getCallerActions function from the objects function list
//            BLangFunction callerActionsFunction = object.functions.stream()
//                    .filter(bLangFunction -> GET_CALLER_ACTIONS.equals(bLangFunction.getName().getValue()))
//                    .findFirst().orElse(null);
//            if (callerActionsFunction != null) {
//                BType endpointActionsHolderType = callerActionsFunction.returnTypeNode.type;
//                BLangObject endpointActionsHolderObject = getObjectByType(objects, endpointActionsHolderType);
//                if (endpointActionsHolderObject != null) {
//                    // Remove the callerObject from the original list, since the callerObject visited
//                    objectCategories.addEndpoints(object);
//                    objectCategories.addEndpointActionHolder(endpointActionsHolderObject);
//                }
//            }
//        }
//
//        objects.removeAll(objectCategories.getEndpointActionHolders());
//        objects.removeAll(objectCategories.getEndpoints());
//
//        // Add the remaining objects to the objects list since those are neither Caller or Endpoint objects
//        objectCategories.getObjects().addAll(objects);
        
        return objectCategories;
    }

    /**
     * Object categories uses as a vessel to carry categorized objects in a blang package.
     -     * <p>
     +     * 
     * {endpoints}  holds the clients and listeners.
     -     * {callers}    holds the caller objects, which are holding the actions.
     +     * {endpointActionHolders}    holds the endpoint actions for the endpoints.
     * {objects}    holds all the other objects.
     +     * 
     +     * Note: number of endpoints EQUAL to number of endpointActionHolders.
     +     * Order of the endpoints EQUAL to the order of endpointActionHolders
     */
    public static class ObjectCategories {
        private String hello;

        public ObjectCategories(String hello) {
            this.hello = hello;
        }

        //        private List<BLangObject> endpoints = new ArrayList<>();
//        private List<BLangObject> endpointActionHolders = new ArrayList<>();
//        private List<BLangObject> objects = new ArrayList<>();
//
//        public List<BLangObject> getEndpointActionHolders() {
//            return endpointActionHolders;
//        }
//
//        public void addEndpointActionHolder(BLangObject caller) {
//            this.endpointActionHolders.add(caller);
//        }
//
//        public List<BLangObject> getObjects() {
//            return objects;
//        }
//        public void addObjects(List<BLangObject> objects) {
//            this.objects.addAll(objects);
//        }
    }
}
