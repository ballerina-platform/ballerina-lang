/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.bindgen.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.command.BindingsGenerator.isDirectJavaClass;
import static org.ballerinalang.bindgen.command.BindingsGenerator.setAllClasses;
import static org.ballerinalang.bindgen.utils.BindgenConstants.ACCESS_FIELD;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MUTATE_FIELD;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getAlias;
import static org.ballerinalang.bindgen.utils.BindgenUtils.handleOverloadedMethods;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isAbstractClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isFinalField;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicField;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicMethod;

/**
 * Class for storing details pertaining to a specific Java class used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JClass {

    private String prefix;
    private String className;
    private String packageName;
    private String accessModifier;
    private String shortClassName;

    private boolean isInterface = false;
    private boolean isDirectClass = false;
    private boolean isAbstract = false;
    private boolean importJavaArraysModule = false;

    private Set<String> superClasses = new HashSet<>();
    private Set<String> superClassNames = new LinkedHashSet<>();
    private List<JField> fieldList = new ArrayList<>();
    private List<JMethod> methodList = new ArrayList<>();
    private List<JConstructor> constructorList = new ArrayList<>();
    private List<JConstructor> initFunctionList = new ArrayList<>();
    private Map<String, Integer> overloadedMethods = new HashMap<>();

    public JClass(Class c) {
        className = c.getName();
        prefix = className.replace(".", "_").replace("$", "_");
        shortClassName = getAlias(c);
        packageName = c.getPackage().getName();
        shortClassName = getExceptionName(c, shortClassName);
        superClassNames.add(c.getName());

        setAllClasses(shortClassName);
        if (c.isInterface()) {
            isInterface = true;
            setAllClasses(getAlias(Object.class));
            superClassNames.add(Object.class.getName());
            superClasses.add(getAlias(Object.class));
        }
        populateImplementedInterfaces(c.getInterfaces());

        Class sClass = c.getSuperclass();
        while (sClass != null) {
            populateImplementedInterfaces(sClass.getInterfaces());
            String simpleClassName = getAlias(sClass).replace("$", "");
            superClassNames.add(sClass.getName());
            superClasses.add(simpleClassName);
            setAllClasses(simpleClassName);
            sClass = sClass.getSuperclass();
        }

        if (isAbstractClass(c)) {
            isAbstract = true;
        }
        if (isDirectJavaClass()) {
            isDirectClass = true;
            populateConstructors(c.getConstructors());
            populateInitFunctions();
            populateMethodsInOrder(c);
            populateFields(c.getFields());
        }
    }

    private void populateMethodsInOrder(Class c) {
        Map<Method, String> methodClassMap = getMethodsAsMap(c);
        LinkedList<String> list = new LinkedList<>(superClassNames);
        Iterator<String> iterator = list.descendingIterator();
        while (iterator.hasNext()) {
            String superclass = iterator.next();
            if (methodClassMap.containsValue(superclass)) {
                List<JMethod> jMethods = new ArrayList<>();
                List<Method> methods = new ArrayList<>();
                for (Map.Entry<Method, String> mapValue : methodClassMap.entrySet()) {
                    if (mapValue.getValue().equals(superclass)) {
                        jMethods.add(new JMethod(mapValue.getKey()));
                        methods.add(mapValue.getKey());
                    }
                }
                populateMethods(methods);
                methodList.sort(Comparator.comparing(JMethod::getParamTypes));
                jMethods.sort(Comparator.comparing(JMethod::getParamTypes));
                handleOverloadedMethods(methodList, jMethods, this);
                methodList.sort(Comparator.comparing(JMethod::getMethodName));
            }
        }
    }

    private String getExceptionName(Class exception, String name) {
        try {
            // Append the prefix "J" in front of bindings generated for Java exceptions.
            if (this.getClass().getClassLoader().loadClass(Exception.class.getCanonicalName())
                    .isAssignableFrom(exception)) {
                return "J" + name;
            }
        } catch (ClassNotFoundException ignore) {
            // Silently ignore if the exception class cannot be found.
        }
        return name;
    }

    private Map<Method, String> getMethodsAsMap(Class classObject) {
        Method[] declaredMethods = classObject.getMethods();
        Map<Method, String> classMethods = new HashMap<>();
        for (Method m : declaredMethods) {
            if (!m.isSynthetic() && (!m.getName().equals("toString"))) {
                classMethods.put(m, m.getDeclaringClass().getName());
            }
        }
        return classMethods;
    }

    private void populateConstructors(Constructor[] constructors) {
        int i = 1;
        for (Constructor constructor : constructors) {
            JConstructor jConstructor = new JConstructor(constructor);
            constructorList.add(jConstructor);
            if (jConstructor.requireJavaArrays()) {
                importJavaArraysModule = true;
            }
        }
        constructorList.sort(Comparator.comparing(JConstructor::getParamTypes));
        for (JConstructor jConstructor:constructorList) {
            jConstructor.setConstructorName("new" + shortClassName + i);
            jConstructor.setShortClassName(shortClassName);
            i++;
        }
    }

    private void populateInitFunctions() {
        int j = 1;
        for (JConstructor constructor : constructorList) {
            JConstructor newCons = null;
            try {
                newCons = (JConstructor) constructor.clone();
            } catch (CloneNotSupportedException ignore) {

            }
            if (newCons != null) {
                newCons.setExternalFunctionName(constructor.getConstructorName());
                newCons.setConstructorName("" + j);
                initFunctionList.add(newCons);
            }
            j++;
        }
    }

    private void populateMethods(List<Method> declaredMethods) {
        for (Method method : declaredMethods) {
            if (isPublicMethod(method)) {
                JMethod jMethod = new JMethod(method);
                jMethod.setShortClassName(shortClassName);
                if (jMethod.requireJavaArrays()) {
                    importJavaArraysModule = true;
                }
                methodList.add(jMethod);
            }
        }
    }

    private void populateFields(Field[] fields) {
        boolean addField = true;
        for (Field field : fields) {
            // To prevent the duplication of fields resulting from super classes.
            for (JField jField : fieldList) {
                if (jField.getFieldName().equals(field.getName())) {
                    addField = false;
                }
            }
            if (addField) {
                JField jFieldGetter = new JField(field, ACCESS_FIELD);
                fieldList.add(jFieldGetter);
                if (jFieldGetter.requireJavaArrays()) {
                    importJavaArraysModule = true;
                }
                if (!isFinalField(field) && isPublicField(field)) {
                    fieldList.add(new JField(field, MUTATE_FIELD));
                }
            }
        }
    }

    private void populateImplementedInterfaces(Class[] interfaces) {
        for (Class interfaceClass : interfaces) {
            setAllClasses(getAlias(interfaceClass));
            superClasses.add(getAlias(interfaceClass));
            superClassNames.add(interfaceClass.getName());
            if (interfaceClass.getInterfaces() != null) {
                populateImplementedInterfaces(interfaceClass.getInterfaces());
            }
        }
    }

    public String getShortClassName() {
        return shortClassName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setAccessModifier(String accessModifier) {
        this.accessModifier = accessModifier;
    }

    public void setMethodCount(String methodName) {
        Integer methodCount = overloadedMethods.get(methodName);
        if (methodCount == null) {
            overloadedMethods.put(methodName, 1);
        } else {
            overloadedMethods.replace(methodName, methodCount + 1);
        }
    }

    public Integer getMethodCount(String methodName) {
        return overloadedMethods.get(methodName);
    }
}
