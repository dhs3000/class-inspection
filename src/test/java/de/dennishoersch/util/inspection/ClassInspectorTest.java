/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dennishoersch.util.inspection;

import java.util.Collection;

import junit.framework.TestCase;

import org.junit.Test;

import com.google.common.collect.Iterables;

import de.dennishoersch.util.inspection.testpackage.PackagedAnnotatedAndMethodAnnotatedTestClass;
import de.dennishoersch.util.inspection.testpackage.PackagedAnnotatedTestClass;
import de.dennishoersch.util.inspection.testpackage.PackagedNotAnnotatedTestClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.BaseClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.BaseClassExtendingClass1;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.BaseClassExtendingClass2;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.ClassWithInnerClassExtendingFromSuperBaseClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.SuperBaseClass;
import de.dennishoersch.util.inspection.testpackage_with_classhierarchy.SuperBaseClassExtendingClass;
import de.dennishoersch.util.inspection.testpackage_with_interface.TestInterface;
import de.dennishoersch.util.inspection.testpackage_with_interface.sub.SubOfTestClass2;
import de.dennishoersch.util.inspection.testpackage_with_interface.sub.TestClass1;
import de.dennishoersch.util.inspection.testpackage_with_interface.sub.TestClass2;
import de.dennishoersch.util.inspection.testpackage_with_interface.sub.TestEnum;

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
		Collection<Class<?>> classes = ClassInspectionUtil.findClassesAssignableFrom(Object.class, this.getClass().getPackage().getName() + ".testpackage");

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
		Collection<Class<? extends BaseClass>> classes = ClassInspectionUtil.findClassesAssignableFrom(BaseClass.class, this.getClass().getPackage().getName() + ".testpackage_with_classhierarchy");
		// Collection<Class<?>> classes =
		// ClassInspectionUtil.findClassesAssignableFrom(BaseClass.class,
		// "de.his");

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
		// Collection<Class<?>> classes =
		// ClassInspectionUtil.findClassesAssignableFrom(SuperBaseClass.class,
		// this.getClass().getPackage().getName() +
		// ".testpackage_with_classhierarchy");
		Collection<Class<? extends SuperBaseClass>> classes = ClassInspectionUtil.findClassesAssignableFrom(SuperBaseClass.class, "de.dennishoersch.util");

		System.out.println(classes);
		System.out.println(classes.size());

		assertEquals("Number of found classes", 7, classes.size());
		assertTrue("Contains SuperBaseClass.class", classes.contains(SuperBaseClass.class));
		assertTrue("Contains BaseClass.class", classes.contains(BaseClass.class));
		assertTrue("Contains BaseClassExtendingClass1.class", classes.contains(BaseClassExtendingClass1.class));
		assertTrue("Contains BaseClassExtendingClass2.class", classes.contains(BaseClassExtendingClass2.class));
		assertTrue("Contains SuperBaseClassExtendingClass.class", classes.contains(SuperBaseClassExtendingClass.class));
		assertTrue("Contains ClassWithInnerClassExtendingFromSuperBaseClass.InnerClass.class", classes.contains(ClassWithInnerClassExtendingFromSuperBaseClass.InnerClass.class));
		assertTrue("Contains ClassWithInnerClassExtendingFromSuperBaseClass$PrivateInnerClass.class", Iterables.contains(Iterables.transform(classes, ClassInspectionUtil.classToName()),
				"de.dennishoersch.util.inspection.testpackage_with_classhierarchy.ClassWithInnerClassExtendingFromSuperBaseClass$PrivateInnerClass"));
	}

	/**
	 *
	 */
	@Test
	public void testFindClassesAssignableFromTestInterface() {
		Collection<Class<? extends TestInterface>> classes = ClassInspectionUtil.findClassesImplementing(TestInterface.class, TestInterface.class.getPackage().getName());

		System.out.println(classes);
		System.out.println(classes.size());

		assertEquals("Number of found classes", 4, classes.size());
		assertTrue("Contains TestClass1.class", classes.contains(TestClass1.class));
		assertTrue("Contains TestClass2.class", classes.contains(TestClass2.class));
		assertTrue("Contains SubOfTestClass2.class", classes.contains(SubOfTestClass2.class));
		assertTrue("Contains TestEnum.class", classes.contains(TestEnum.class));
	}

	/**
	 *
	 */
	@Test
	public void testFindAnnotated() {
		Collection<Class<?>> classes = ClassInspectionUtil.findAnnotatedClasses(ClassInspectorTestAnnotationOnlyOnType.class, this.getClass().getPackage().getName());
		// Collection<Class<?>> classes =
		// ClassInspectionUtil.findAnnotatedClasses(ClassInspectorTestAnnotationOnlyOnType.class,
		// "de.his");

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
		Collection<ClassAnnotationMetadata> classes = ClassInspectionUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnTypeAndMethod.class, this.getClass().getPackage().getName());
		// Collection<ClassMetadata> classes =
		// ClassInspectionUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnTypeAndMethod.class,
		// "de.his");

		System.out.println(classes);
		assertEquals("Number of found classes", 1, classes.size());
		ClassAnnotationMetadata classMetadata = classes.iterator().next();
		assertTrue("Class is annotated", classMetadata.isRelateedClassAnnotated());
		assertTrue("Class is PackagedAnnotatedAndMethodAnnotatedTestClass.class", classMetadata.getRelatedClass().equals(PackagedAnnotatedAndMethodAnnotatedTestClass.class));
		assertEquals("Number of annotated methods", 1, classMetadata.getAnnotatedMethods().size());
		assertEquals("Number of annotated fields", 0, classMetadata.getAnnotatedFields().size());

	}

	/**
	 *
	 */
	@Test
	public void testFindAnnotatedElementsOnClass() {
		Collection<ClassAnnotationMetadata> classes = ClassInspectionUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnType.class, this.getClass().getPackage().getName());
		// Collection<ClassMetadata> classes =
		// ClassInspectionUtil.findAnnotatedElements(ClassInspectorTestAnnotationOnlyOnType.class,
		// "de.his");

		System.out.println(classes);
		assertEquals("Number of found classes", 2, classes.size());
		for (ClassAnnotationMetadata classMetadata : classes) {
			assertEquals("Number of annotated methods", 0, classMetadata.getAnnotatedMethods().size());
			assertEquals("Number of annotated fields", 0, classMetadata.getAnnotatedMethods().size());
		}
	}
}
