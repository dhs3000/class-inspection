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
package de.dennishoersch.util.inspection.impl.inspect;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import de.dennishoersch.util.inspection.ClassInspector;
import de.dennishoersch.util.inspection.InspectionHelper;

/**
 * An inspector that checks if a class is annotated with a specific annotation and if so collects it.
 */
public class ClassesAnnotatedWith implements ClassInspector<Class<?>> {
    private static final Logger logger = Logger.getLogger(ClassesAnnotatedWith.class);

    private Class<? extends Annotation> annotation;

    private Set<Class<?>> _matches = Sets.newHashSet();

    /**
     * @param annotation
     */
    public ClassesAnnotatedWith(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }

    @Override
    public void inspect(ClassFile type, InspectionHelper helper) {
        logger.trace("Checking to see if class " + type.getName() + " matches criteria [" + toString() + "]");
        if (isAnnotationPresent(type, annotation)) {
            try {
                _matches.add(helper.loadClass(type));
            } catch (ClassNotFoundException t) {
                logger.warn("Could not load class '" + type.getName() + "'.", t);
            }
        }
    }

    private static boolean isAnnotationPresent(ClassFile type, Class<? extends Annotation> annotation) {
        AnnotationsAttribute visible = (AnnotationsAttribute) type.getAttribute(AnnotationsAttribute.visibleTag);
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
    public Collection<Class<?>> getElements() {
        return _matches;
    }

    @Override
    public String toString() {
        return "annotated with @" + annotation.getSimpleName();
    }
}
