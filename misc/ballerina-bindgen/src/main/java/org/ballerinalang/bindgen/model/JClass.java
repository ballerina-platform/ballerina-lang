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

import org.ballerinalang.bindgen.utils.BindgenEnv;
import org.ballerinalang.bindgen.utils.BindgenUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.EXCEPTION_CLASS_PREFIX;
import static org.ballerinalang.bindgen.utils.BindgenUtils.getAlias;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isFinalField;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicClass;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicConstructor;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicField;
import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicMethod;

/**
 * Class for storing details pertaining to a specific Java class used for Ballerina bridge code generation.
 *
 * @since 1.2.0
 */
public class JClass {

    private BindgenEnv env;
    private String prefix;
    private String packageName;
    private String shortClassName;
    private Class currentClass;

    private boolean modulesFlag;
    private boolean importJavaArraysModule = false;

    private Map<String, String> superClassPackage = new HashMap<>();
    private Set<String> importedPackages = new HashSet<>();
    private List<JField> fieldList = new ArrayList<>();
    private List<JMethod> methodList = new ArrayList<>();
    private List<JConstructor> constructorList = new ArrayList<>();
    private Map<String, Integer> overloadedMethods = new HashMap<>();

    public JClass(Class c, BindgenEnv env) {
        this.env = env;
        currentClass = c;
        prefix = c.getName().replace(".", "_").replace("$", "_");
        shortClassName = getAlias(c, env.getAliases());
        packageName = c.getPackage().getName();
        shortClassName = getExceptionName(c, shortClassName);
        modulesFlag = env.getModulesFlag();

        Class sClass = c.getSuperclass();
        // Iterate until a public super class is found.
        while (sClass != null && !isPublicClass(sClass)) {
            sClass = sClass.getSuperclass();
        }
        if (sClass != null) {
            env.setClassListForLooping(sClass.getName());
            String simpleClassName = getAlias(sClass, env.getAliases()).replace("$", "");
            simpleClassName = getExceptionName(sClass, simpleClassName);
            superClassPackage.put(simpleClassName, sClass.getPackageName().replace(".", ""));
        }

        if (env.isDirectJavaClass()) {
            populateConstructors(c.getConstructors());
            populateMethods(c);
            populateFields(c.getFields());
        }

        if (modulesFlag) {
            importedPackages.remove(c.getPackageName());
        }
    }

    private String getExceptionName(Class exception, String name) {
        try {
            // Append the exception class prefix in front of bindings generated for Java exceptions.
            if (this.getClass().getClassLoader().loadClass(Exception.class.getCanonicalName())
                    .isAssignableFrom(exception)) {
                String shortClassName = EXCEPTION_CLASS_PREFIX + name;
                env.setAlias(shortClassName, exception.getName());
                return shortClassName;
            }
        } catch (ClassNotFoundException ignore) {
            // Silently ignore if the exception class cannot be found.
        }
        return name;
    }

    private List<Method> getMethodsAsList(Class classObject) {
        Method[] declaredMethods = classObject.getMethods();
        List<Method> classMethods = new LinkedList<>();
        for (Method m : declaredMethods) {
            if (!m.isSynthetic() && (!m.getName().equals("toString")) && isPublicMethod(m)) {
                classMethods.add(m);
            }
        }
        return classMethods;
    }

    private void populateConstructors(Constructor[] constructors) {
        int i = 1;
        List<JConstructor> tempList = new ArrayList<>();
        for (Constructor constructor : constructors) {
            if (isPublicConstructor(constructor)) {
                tempList.add(new JConstructor(constructor, env, this, null));
            }
        }
        tempList.sort(Comparator.comparing(JConstructor::getParamTypes));
        for (JConstructor constructor:tempList) {
            JConstructor jConstructor = new JConstructor(constructor.getConstructor(), env,
                    this, "new" + shortClassName + i);
            if (modulesFlag) {
                importedPackages.addAll(jConstructor.getImportedPackages());
            }
            constructorList.add(jConstructor);
            if (jConstructor.requireJavaArrays()) {
                importJavaArraysModule = true;
            }
            i++;
        }
    }

    private void populateMethods(Class c) {
        List<JMethod> tempList = new ArrayList<>();
        for (Method method : getMethodsAsList(c)) {
            tempList.add(new JMethod(method, env, prefix, currentClass, 0));
        }
        tempList.sort(Comparator.comparing(JMethod::getParamTypes));
        for (JMethod method : tempList) {
            setMethodCount(method.getJavaMethodName());
            JMethod jMethod = new JMethod(method.getMethod(), env, prefix, currentClass,
                    isOverloaded(method.getMethod()) ? getMethodCount(method.getJavaMethodName()) : 0);
            if (jMethod.requireJavaArrays()) {
                importJavaArraysModule = true;
            }
            if (modulesFlag) {
                importedPackages.addAll(jMethod.getImportedPackages());
            }
            methodList.add(jMethod);
        }
        methodList.sort(Comparator.comparing(JMethod::getMethodName));
    }

    private void populateFields(Field[] fields) {
        boolean addField = true;
        for (Field field : fields) {
            // To prevent the duplication of fields resulting from super classes.
            for (JField jField : fieldList) {
                if (jField.getFieldName().equals(field.getName()) || !isPublicField(field)) {
                    addField = false;
                }
            }
            if (addField) {
                JField jFieldGetter = new JField(field, BFunction.BFunctionKind.FIELD_GET, env, this);
                fieldList.add(jFieldGetter);
                if (modulesFlag) {
                    BindgenUtils.addImportedPackage(field.getType(), importedPackages);
                }
                if (jFieldGetter.requireJavaArrays()) {
                    importJavaArraysModule = true;
                }
                if (!isFinalField(field) && isPublicField(field)) {
                    fieldList.add(new JField(field, BFunction.BFunctionKind.FIELD_SET, env, this));
                }
            }
        }
    }

    private boolean isOverloaded(Method method) {
        boolean overloaded = false;
        int count = 0;
        for (Method m : getMethodsAsList(currentClass)) {
            if (m.getName().equals(method.getName())) {
                count++;
            }
        }
        if (count > 1) {
            overloaded = true;
        }
        return overloaded;
    }

    public String getShortClassName() {
        return shortClassName;
    }

    public String getPackageName() {
        return packageName;
    }

    private void setMethodCount(String methodName) {
        Integer methodCount = overloadedMethods.get(methodName);
        if (methodCount == null) {
            overloadedMethods.put(methodName, 1);
        } else {
            overloadedMethods.replace(methodName, methodCount + 1);
        }
    }

    private Integer getMethodCount(String methodName) {
        return overloadedMethods.get(methodName);
    }

    public Class getCurrentClass() {
        return currentClass;
    }

    public boolean isImportJavaArraysModule() {
        return importJavaArraysModule;
    }

    public Set<String> getImportedPackages() {
        return importedPackages;
    }

    public List<JField> getFieldList() {
        return fieldList;
    }

    public List<JMethod> getMethodList() {
        return methodList;
    }

    public List<JConstructor> getConstructorList() {
        return constructorList;
    }

    public Map<String, String> getSuperClassPackage() {
        return superClassPackage;
    }
}
