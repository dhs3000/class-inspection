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

import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import de.dennishoersch.util.inspection.AnnotatedElementsAnnotatedWith.ClassMetadata;
import de.dennishoersch.util.inspection.testpackage.PackagedAnnotatedAndMethodAnnotatedTestClass;
import de.dennishoersch.util.inspection.testpackage.PackagedAnnotatedTestClass;
import de.dennishoersch.util.inspection.testpackage.PackagedNotAnnotatedTestClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.BaseClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.BaseClassExtendingClass1;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.BaseClassExtendingClass2;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.ClassWithInnerClassExtendingFromSuperBaseClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.SuperBaseClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.SuperBaseClassExtendingClass;


/**
 *
 * @author hoersch
 */
public class ClassInspectorTest extends TestCase {


    // TODO tests mit Methoden aus basisklasse und überschrieben

    /**
     *
     */
    @Test
    public void testFindClassesAssignableFromObject() {
        Collection<Class<?>> classes = ClassInspectorUtil.findClassesAssignableFrom(Object.class, this.getClass().getPackage().getName() + ".testpackage");

        System.out.println(classes);
        System.out.println(classes.size());

        assertEquals("Number of found classes", 3, classes.size());
        assertTrue("Contains PackagedAnnotatedAndMethodAnnotatedTestClass.class", classes.contains(PackagedAnnotatedAndMethodAnnotatedTestClass.class));
        assertTrue("Contains PackagedAnnotatedTestClass.class", classes.contains(PackagedAnnotatedTestClass.class));
        assertTrue("Contains PackagedNotAnnotatedTestClass.class", classes.contains(PackagedNotAnnotatedTestClass.class));
    }

    /**
     *
     */
    @Test
    public void testFindClassesAssignableFromBaseClass() {
        Collection<Class<? extends BaseClass>> classes = ClassInspectorUtil.findClassesAssignableFrom(BaseClass.class, this.getClass().getPackage().getName() + ".testpackage_with_classhierarchy");
        //       Collection<Class<?>> classes = ClassInspectorUtil.findClassesAssignableFrom(BaseClass.class, "de.his");

        System.out.println(classes);
        System.out.println(classes.size());

        assertEquals("Number of found classes", 3, classes.size());
        assertTrue("Contains BaseClass.class", classes.contains(BaseClass.class));
        assertTrue("Contains BaseClassExtendingClass1.class", classes.contains(BaseClassExtendingClass1.class));
        assertTrue("Contains BaseClassExtendingClass2.class", classes.contains(BaseClassExtendingClass2.class));
    }

    /**
     *
     */
    @Test
    public void testFindClassesAssignableFromSuperBaseClass() {
//        Collection<Class<?>> classes = ClassInspectorUtil.findClassesAssignableFrom(SuperBaseClass.class, this.getClass().getPackage().getName() + ".testpackage_with_classhierarchy");
              Collection<Class<? extends SuperBaseClass>> classes = ClassInspectorUtil.findClassesAssignableFrom(SuperBaseClass.class, "de.dennishoersch.util");

        System.out.println(classes);
        System.out.println(classes.size());

        assertEquals("Number of found classes", 7, classes.size());
        assertTrue("Contains SuperBaseClass.class", classes.contains(SuperBaseClass.class));
        assertTrue("Contains BaseClass.class", classes.contains(BaseClass.class));
        assertTrue("Contains BaseClassExtendingClass1.class", classes.contains(BaseClassExtendingClass1.class));
        assertTrue("Contains BaseClassExtendingClass2.class", classes.contains(BaseClassExtendingClass2.class));
        assertTrue("Contains SuperBaseClassExtendingClass.class", classes.contains(SuperBaseClassExtendingClass.class));
        assertTrue("Contains ClassWithInnerClassExtendingFromSuperBaseClass.InnerClass.class", classes.contains(ClassWithInnerClassExtendingFromSuperBaseClass.InnerClass.class));
        assertTrue("Contains ClassWithInnerClassExtendingFromSuperBaseClass$PrivateInnerClass.class", Iterables.contains(Iterables.transform(classes, ToClassName.INSTANCE), "de.dennishoersch.util.inspection.testpackage_with_classhierarchy.ClassWithInnerClassExtendingFromSuperBaseClass$PrivateInnerClass"));
    }

    private enum ToClassName implements Function<Class<?>, String> {
        INSTANCE;
        @Override
        public String apply(Class<?> input) {
            return input.getName();
        }
    }

    /**
     *
     */
    @Test
    public void testFindAnnotated() {
        Collection<Class<?>> classes = ClassInspectorUtil.findAnnotatedClasses(ClassInspectorTestAnnotationOnlyOnType.class, this.getClass().getPackage().getName());
        //        Collection<Class<?>> classes = ClassInspectorUtil.findAnnotatedClasses(ClassInspectorTestAnnotationOnlyOnType.class, "de.his");


        System.out.println(classes);
        assertEquals("Number of found classes", 2, classes.size());

        assertTrue("Contains AnnotatedTestClass.class", classes.contains(AnnotatedTestClass.class));
        assertTrue("Contains PackagedAnnotatedTestClass.class", classes.contains(PackagedAnnotatedTestClass.class));
    }

    /**
     *
     */
    @Test
    public void testFindAnnotatedElementsOnClassAndMethod() {
        Collection<ClassMetadata> classes = ClassInspectorUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnTypeAndMethod.class, this.getClass().getPackage().getName());
        //        Collection<ClassMetadata> classes = ClassInspectorUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnTypeAndMethod.class, "de.his");


        System.out.println(classes);
        assertEquals("Number of found classes", 1, classes.size());
        ClassMetadata classMetadata = classes.iterator().next();
        assertTrue("Class is annotated", classMetadata.isClassAnnotated);
        assertTrue("Class is PackagedAnnotatedAndMethodAnnotatedTestClass.class", classMetadata.clazz.equals(PackagedAnnotatedAndMethodAnnotatedTestClass.class));
        assertEquals("Number of annotated methods", 1, classMetadata.annotatedMethods.size());
        assertEquals("Number of annotated fields", 0, classMetadata.annotatedFields.size());

    }

    /**
     *
     */
    @Test
    public void testFindAnnotatedElementsOnClass() {
        Collection<ClassMetadata> classes = ClassInspectorUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnType.class, this.getClass().getPackage().getName());
        //        Collection<ClassMetadata> classes = ClassInspectorUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnType.class, "de.his");


        System.out.println(classes);
        assertEquals("Number of found classes", 2, classes.size());
        for (ClassMetadata classMetadata : classes) {
            assertEquals("Number of annotated methods", 0, classMetadata.annotatedMethods.size());
            assertEquals("Number of annotated fields", 0, classMetadata.annotatedFields.size());
        }
    }
}
