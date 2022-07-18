//package io.ballerina.multiservice;
//
//import java.util.List;
//
//public class ComponentModel {
//    private final String name;
//    private final String org;
//    private final String version;
//    private final List<Service> services;
//
//    public ComponentModel(String name, String org, String version, List<Service> services) {
//        this.name = name;
//        this.org = org;
//        this.version = version;
//        this.services = services;
//    }
//
//    public static class Service {
//        private final String path;
//        private final String serviceId;
//        private final List<Resource> resources;
//
//        public Service(String path, String serviceId, List<Resource> resources) {
//            this.path = path;
//            this.serviceId = serviceId;
//            this.resources = resources;
//        }
//    }
//
//    public static class Resource {
//        private final String path;
//        private final String method;
//        private final List<Parameter> parameters;
//        private final List<String> returns;
//        private final List<Interaction> interactions;
//
//        public Resource(String name, String accessor, List<Parameter> parameters, List<String> returns, List<Interaction> interactions) {
//            this.path = name;
//            this.method = accessor;
//            this.parameters = parameters;
//            this.returns = returns;
//            this.interactions = interactions;
//        }
//    }
//
//    public static class Parameter {
//        private final String type;
//        private final String name;
//        private final String in;
//
//        public Parameter(String type, String name, String in) {
//            this.type = type;
//            this.name = name;
//            this.in = in;
//        }
//    }
//
//    public static class Interaction {
//        private final String serviceId;
//
//        // add recourceID object
//        private final String path;
//        private final String method;
//
//        public Interaction(String serviceId, String name, String accessor) {
//            this.serviceId = serviceId;
//            this.path = name;
//            this.method = accessor;
//        }
//    }
//
//}
