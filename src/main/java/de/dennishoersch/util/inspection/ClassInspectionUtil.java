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
import java.util.Collection;

import de.dennishoersch.util.inspection.AnnotatedElementsAnnotatedWith.ClassMetadata;

/**
 *
 * @author hoersch
 */
public class ClassInspectionUtil {

    /**
     * Attempts to discover elements that are matched with the given inspector.
     * @param inspector
     * @param packageName package name to scan (including subpackages) for classes
     * @return matched classes
     */
    public static <T> Collection<T> findElements(ClassInspector<T> inspector, String packageName) {
        return new ClassEnumerator<T>(inspector, packageName).findAndLetInspect().getElements();
    }


    /**
     * Attempts to discover classes that are assignable from the given class.
     *
     * @param clazz the class that matching classes should be assignable from
     * @param packageName package name to scan (including subpackages) for classes
     * @return matched classes
     */
    public static <T> Collection<Class<? extends T>> findClassesAssignableFrom(Class<T> clazz, String packageName) {
        return findElements(new ClassesAssignableFrom<T>(clazz), packageName);
    }

    /**
     * Attempts to discover classes that are annotated with to the annotation.
     *
     * @param annotation the annotation that should be present on matching classes
     * @param packageName package name to scan (including subpackages) for classes
     * @return matched classes
     */
    public static Collection<Class<?>> findAnnotatedClasses(Class<? extends Annotation> annotation, String packageName) {
        return findElements(new ClassesAnnotatedWith(annotation), packageName);
    }

    /**
     * Attempts to discover elements that are annotated with to the annotation.
     *
     * @param annotation the annotation that should be present on matching classes
     * @param packageName package name to scan (including subpackages) for classes
     * @return matched classes
     */
    public static Collection<ClassMetadata> findAnnotatedElements(Class<? extends Annotation> annotation, String packageName) {
        return findElements(new AnnotatedElementsAnnotatedWith(annotation), packageName);
    }
}
