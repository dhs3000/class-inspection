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

import java.lang.annotation.Annotation;
import java.util.Collection;

import com.google.common.base.Function;

import de.dennishoersch.util.inspection.impl.collect.ClassCollector;
import de.dennishoersch.util.inspection.impl.inspect.AnnotatedElementsAnnotatedWith;
import de.dennishoersch.util.inspection.impl.inspect.ClassesAnnotatedWith;
import de.dennishoersch.util.inspection.impl.inspect.ClassesAssignableFrom;
import de.dennishoersch.util.inspection.impl.inspect.ClassesImplementing;

/**
 * Utilities to inspect and discover classes (elements) that match a specific
 * criteria.
 * 
 * @author hoersch
 */
public class ClassInspectionUtil {

	/**
	 * Collects elements that are matched by the given inspector.
	 * 
	 * @param inspector
	 * @param packageName
	 *            package name to scan recursively
	 * @return matched classes
	 */
	public static <T, CI extends ClassInspector<T>> Collection<T> findElements(CI inspector, String packageName) {
		return new ClassCollector<T, CI>(inspector, packageName).findAndLetInspect().getElements();
	}

	/**
	 * Collects classes that are assignable from the given class.
	 * 
	 * @param clazz
	 *            the class that matching classes should be assignable from
	 * @param packageName
	 *            package name to scan recursively
	 * @return matched classes
	 */
	public static <T> Collection<Class<? extends T>> findClassesAssignableFrom(Class<T> clazz, String packageName) {
		return findElements(new ClassesAssignableFrom<T>(clazz), packageName);
	}

	public static <T> Collection<Class<? extends T>> findClassesImplementing(Class<T> iface, String packageName) {
		if (!iface.isInterface()) {
			throw new IllegalArgumentException("'" + iface + "' is no Interface!");
		}
		return findElements(new ClassesImplementing<T>(iface), packageName);
	}

	/**
	 * Collects classes that are annotated with the annotation.
	 * 
	 * @param annotation
	 * @param packageName
	 *            package name to scan recursively
	 * @return matched classes
	 */
	public static Collection<Class<?>> findAnnotatedClasses(Class<? extends Annotation> annotation, String packageName) {
		return findElements(new ClassesAnnotatedWith(annotation), packageName);
	}

	/**
	 * Collects class metadata of classes and members are annotated with the
	 * annotation.
	 * 
	 * @param annotation
	 * @param packageName
	 *            package name to scan recursively
	 * @return matched classes
	 */
	public static Collection<ClassAnnotationMetadata> findAnnotatedElements(Class<? extends Annotation> annotation, String packageName) {
		return findElements(new AnnotatedElementsAnnotatedWith(annotation), packageName);
	}

	/**
	 * @return function class to name
	 */
	public static Function<Class<?>, String> classToName() {
		return ToClassName.INSTANCE;
	}

	private enum ToClassName implements Function<Class<?>, String> {
		INSTANCE;
		@Override
		public String apply(Class<?> input) {
			return input.getName();
		}
	}

}
