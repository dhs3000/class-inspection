/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.dennishoersch.util.inspection;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * An inspector that checks if a class or filed within or a method within is annotated with a specific annotation and if so collects it.
 */
public class AnnotatedElementsAnnotatedWith implements ClassInspector<AnnotatedElementsAnnotatedWith.ClassMetadata> {
    private static final Logger logger = Logger.getLogger(AnnotatedElementsAnnotatedWith.class);

    /**
     * Simple metadata of a processed class. Check {@link #isClassAnnotated} to see if the class itself is annotated.
     */
    public static class ClassMetadata {
        /** The class.  */
        public final Class<?> clazz;

        /** Is the class itself annotated?  */
        public final boolean isClassAnnotated;

        /** Annotated fields in this class. */
        public final Set<Field> annotatedFields = Sets.newHashSet();

        /** Annotated methods in this class. */
        public final Set<Method> annotatedMethods = Sets.newHashSet();

        ClassMetadata(Class<?> clazz, boolean isClassAnnotated) {
            this.clazz = clazz;
            this.isClassAnnotated = isClassAnnotated;
        }

        @Override
        public String toString() {
            return "ClassMetadata [clazz=" + clazz + ", isClassAnnotated=" + isClassAnnotated + ", annotatedFields=" + annotatedFields + ", annotatedMethods=" + annotatedMethods + "]";
        }
    }

    private Class<? extends Annotation> annotation;

    private Map<String, ClassMetadata> _matches = Maps.newHashMap();

    /**
     * @param annotation
     */
    public AnnotatedElementsAnnotatedWith(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void inspect(ClassFile type, InspectionHelper helper) {
        logger.trace("Checking to see if class " + type.getName() + " matches criteria [" + toString() + "]");
        {
            ClassMetadata classMetadata = _matches.get(type.getName());
            if (classMetadata != null) {
                logger.warn("Class " + type.getName() + " was already preocessed!");
                return;
            }
        }
        try {
            if (isAllowedOn(annotation, ElementType.TYPE)) {
                if (isAnnotationPresent(type, annotation)) {
                    storeAndGetClassMetadata(helper, type, true);
                }
            }
            if (isAllowedOn(annotation, ElementType.FIELD)) {
                for (FieldInfo field : (List<FieldInfo>) type.getFields()) {
                    if (isAnnotationPresent(field, annotation)) {
                        ClassMetadata classMetadata = storeAndGetClassMetadata(helper, type, false);
                        classMetadata.annotatedFields.add(classMetadata.clazz.getField(field.getName()));
                    }
                }
            }
            if (isAllowedOn(annotation, ElementType.METHOD)) {
                for (MethodInfo method : (List<MethodInfo>) type.getMethods()) {
                    if (isAnnotationPresent(method, annotation)) {
                        ClassMetadata classMetadata = storeAndGetClassMetadata(helper, type, false);
                        for (Method m : classMetadata.clazz.getMethods()) {
                            if (m.getName().equals(method.getName()) && m.isAnnotationPresent(annotation)) {
                                classMetadata.annotatedMethods.add(m);
                            }
                        }
                    }
                }
            }
        } catch (ClassNotFoundException t) {
            logger.warn("Could not load class '" + type.getName() + "'.", t);

        } catch (NoSuchFieldException t) {
            logger.warn("Could not load field of class '" + type.getName() + "'.", t);
        }
    }

    private boolean isAllowedOn(Class<? extends Annotation> a, ElementType type) {
        Target target = a.getAnnotation(Target.class);
        if (target == null || Arrays.binarySearch(target.value(), type) >= 0) {
            return true;
        }
        return false;
    }

    private ClassMetadata storeAndGetClassMetadata(InspectionHelper helper, ClassFile type, boolean isClassAnnotated) throws ClassNotFoundException {
        ClassMetadata classMetadata = _matches.get(type.getName());
        if (classMetadata == null) {
            Class<?> clazz = helper.loadClass(type);
            classMetadata = new ClassMetadata(clazz, isClassAnnotated);
            _matches.put(type.getName(), classMetadata);
        }
        return classMetadata;
    }

    private static boolean isAnnotationPresent(ClassFile type, Class<? extends Annotation> annotation) {
        AnnotationsAttribute visible = (AnnotationsAttribute) type.getAttribute(AnnotationsAttribute.visibleTag);
        return isAnnotationPresent(annotation, visible);
    }

    private static boolean isAnnotationPresent(FieldInfo type, Class<? extends Annotation> annotation) {
        AnnotationsAttribute visible = (AnnotationsAttribute) type.getAttribute(AnnotationsAttribute.visibleTag);
        return isAnnotationPresent(annotation, visible);
    }

    private static boolean isAnnotationPresent(MethodInfo type, Class<? extends Annotation> annotation) {
        AnnotationsAttribute visible = (AnnotationsAttribute) type.getAttribute(AnnotationsAttribute.visibleTag);
        return isAnnotationPresent(annotation, visible);
    }

    private static boolean isAnnotationPresent(Class<? extends Annotation> annotation, AnnotationsAttribute visible) {
        if (visible == null) {
            return false;
        }
        for (javassist.bytecode.annotation.Annotation ann : visible.getAnnotations()) {
            //System.out.println("@" + ann.getTypeName());
            if (ann.getTypeName().equals(annotation.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<ClassMetadata> getElements() {
        return _matches.values();
    }

    @Override
    public String toString() {
        return "elements (classes, fields and methods annotated with @" + annotation.getSimpleName();
    }
}
