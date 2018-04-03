/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.ballerinalang.siddhi.annotation.classindex;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Class filter responsible for filtering classes based on various criteria.
 */
public final class ClassFilter {
    private ClassFilter() {
    }

    /**
     * Returns a builder for a filter which satisfies all selected predicates.
     * @return Builder object
     */
    public static UnionBuilder only() {
        return new Builder();
    }

    /**
     * Returns a filter which satisfies any of the selected predicates.
     *
     * @param alternatives alternative predicates
     * @return filter which satisfies any of the provided predicates
     */
    public static FilterBuilder any(final Predicate... alternatives) {
        return new CommonFilterBuilder() {
            @Override
            public boolean matches(Class<?> klass) {
                for (Predicate alternative : alternatives) {
                    if (alternative.matches(klass)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Class from.
     */
    public interface Predicate {
        /**
         * Returns true if the class should be included in the result.
         * @param klass input class
         * @return true or false based on match
         */
        boolean matches(Class<?> klass);
    }

    /**
     * Interface for filter builder.
     */
    public interface FilterBuilder extends Predicate {

        /**
         * Filters given classes.
         *
         * @param classes input classes
         * @param <T> Output class type
         * @return filtered classes
         */
        <T> Iterable<Class<? extends T>> from(Iterable<Class<? extends T>> classes);
    }

    /**
     * Interface for union builder.
     */
    public interface UnionBuilder extends FilterBuilder {
        /**
         * Satisfies given predicate.
         *
         * @param predicate predicate to satisfy
         * @return union builder
         */
        UnionBuilder satisfying(Predicate predicate);

        /**
         * Returns top level classes.
         * @return union builder
         */
        UnionBuilder topLevel();

        /**
         * Returns top level or static nested classes.
         * @return union builder
         */
        UnionBuilder topLevelOrStaticNested();

        /**
         * Returns classes nested (directly or indirectly) in given class.
         * @param enclosing classes to be nested
         * @return union builder
         */
        UnionBuilder enclosedIn(final Class<?> enclosing);

        /**
         * Returns classes nested directly in given class.
         * @param enclosing input class
         * @return union builder
         */
        UnionBuilder enclosedDirectlyIn(final Class<?> enclosing);

        /**
         * Returns classes annotated with given annotation.
         * <p>
         * As opposed to {@link ClassIndex#getAnnotated(Class)} this method only works if annotation
         * is itself annotation with {@link Retention} set to {@link RetentionPolicy#RUNTIME}.
         * </p>
         * @param annotation class annotation
         * @return union builder
         * @throws IllegalStateException if annotation retention policy is not set equal to
         *                               {@link RetentionPolicy#RUNTIME}.
         */
        UnionBuilder annotatedWith(final Class<? extends Annotation> annotation);

        /**
         * Returns classes marked with given modifiers.
         *
         * @param modifiers modifiers to expect, see {@link Modifier}
         * @return union builder
         */
        UnionBuilder withModifiers(int modifiers);

        /**
         * Returns classes not marked with given modifiers.
         *
         * @param modifiers modifiers to expect, see {@link Modifier}
         * @return union builder
         */
        UnionBuilder withoutModifiers(int modifiers);

        /**
         * Returns classes which have public default constructor.
         * <p>
         * Default constructor is a constructor without any parameters.
         * Note that (non-static) inner classes never have the default constructor
         * (see: "http://thecodersbreakfast.net/index.php?post/2011/09/26/
         * Inner-classes-and-the-myth-of-the-default-constructor" Inner classes and the
         * myth of the default constructor)
         * </p>
         *
         * @return union builder
         */
        UnionBuilder withPublicDefaultConstructor();

        /**
         * Returns only interfaces.
         * @return union builder
         */
        UnionBuilder interfaces();

        /**
         * Returns only classes - filters out any interfaces.
         * @return union builder
         */
        UnionBuilder classes();
    }

    private abstract static class CommonFilterBuilder implements FilterBuilder {
        @Override
        public <T> Iterable<Class<? extends T>> from(Iterable<Class<? extends T>> classes) {
            List<Class<? extends T>> result = new ArrayList<>();
            for (Class<? extends T> klass : classes) {
                if (matches(klass)) {
                    result.add(klass);
                }
            }

            return result;
        }
    }

    private static class Builder extends CommonFilterBuilder implements UnionBuilder {
        private final List<Predicate> predicates = new ArrayList<>();

        @Override
        public Builder satisfying(Predicate predicate) {
            predicates.add(predicate);
            return this;
        }

        @Override
        public Builder topLevel() {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return klass.getEnclosingClass() == null;
                }
            });
        }

        @Override
        public UnionBuilder topLevelOrStaticNested() {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return klass.getEnclosingClass() == null || (klass.getModifiers() & Modifier.STATIC) != 0;
                }
            });
        }

        @Override
        public Builder enclosedIn(final Class<?> enclosing) {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    while (true) {
                        klass = klass.getEnclosingClass();
                        if (klass == null) {
                            return false;
                        }
                        if (enclosing.equals(klass)) {
                            return true;
                        }
                    }
                }
            });
        }

        @Override
        public UnionBuilder enclosedDirectlyIn(final Class<?> enclosing) {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    klass = klass.getEnclosingClass();
                    if (klass == null) {
                        return false;
                    }
                    return klass == enclosing;
                }
            });
        }

        @Override
        public Builder annotatedWith(final Class<? extends Annotation> annotation) {
            Retention retention = annotation.getAnnotation(Retention.class);
            if (retention == null || retention.value() != RetentionPolicy.RUNTIME) {
                throw new IllegalStateException("Cannot filter annotated with annotation without retention policy"
                        + " set to RUNTIME: " + annotation.getName());
            }
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return klass.isAnnotationPresent(annotation);
                }
            });
        }

        @Override
        public UnionBuilder withModifiers(final int modifiers) {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return (klass.getModifiers() & modifiers) != 0;
                }
            });
        }

        @Override
        public UnionBuilder withoutModifiers(final int modifiers) {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return (klass.getModifiers() & modifiers) == 0;
                }
            });
        }

        @Override
        public UnionBuilder withPublicDefaultConstructor() {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    try {
                        Constructor<?> constructor = klass.getConstructor();
                        return (constructor.getModifiers() & Modifier.PUBLIC) != 0;
                    } catch (NoSuchMethodException | SecurityException e) {
                        return false;
                    }
                }
            });
        }

        @Override
        public UnionBuilder interfaces() {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return klass.isInterface();
                }
            });
        }

        @Override
        public UnionBuilder classes() {
            return satisfying(new Predicate() {
                @Override
                public boolean matches(Class<?> klass) {
                    return !klass.isInterface();
                }
            });
        }

        @Override
        public boolean matches(Class<?> klass) {
            for (Predicate predicate : predicates) {
                if (!predicate.matches(klass)) {
                    return false;
                }
            }
            return true;
        }
    }
}
