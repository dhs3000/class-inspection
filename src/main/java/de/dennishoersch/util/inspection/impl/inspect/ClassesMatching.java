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
package de.dennishoersch.util.inspection.impl.inspect;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javassist.bytecode.ClassFile;

import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

import de.dennishoersch.util.inspection.ClassInspector;
import de.dennishoersch.util.inspection.InspectionHelper;
import de.dennishoersch.util.inspection.InspectionHelper.ClassInfo;

/**
 * An inspector that checks if a class is matching a condition and if so collects it.
 * @author hoersch
 * @param <T>
 */
abstract class ClassesMatching<T> implements ClassInspector<Class<? extends T>> {
	private static final Logger logger = Logger.getLogger(ClassesMatching.class);

	private final Set<Class<? extends T>> _matches = Sets.newHashSet();

	/**
	 * @param clazz
	 */
	protected ClassesMatching() {
	}

	@Override
	public final void inspect(ClassFile type, InspectionHelper helper) {
		logger.trace("Checking to see if class " + type.getName() + " matches criteria [" + toString() + "]");
		try {
			ClassInfo classInfo = helper.toClassInfo(type);
			for (;;) {
				if (isMatch(classInfo)) {
					@SuppressWarnings("unchecked")
					Class<T> loadedClass = (Class<T>)helper.loadClass(type);
					_matches.add(loadedClass);
					return;
				}

				if (classInfo.getSuperclass().equals(Object.class.getName())) {
					return;
				}
				classInfo = helper.getClassInfo(classInfo.getSuperclass());
			}
		} catch (ClassNotFoundException e) {
			logger.warn("Could not load class '" + type.getName() + "'.", e);
		} catch (IOException e) {
			logger.warn("Could not load class file.", e);
		}
	}

	protected abstract boolean isMatch(ClassInfo potentialMatch);

	@Override
	public final Collection<Class<? extends T>> getElements() {
		return _matches;
	}

	@Override
	public abstract String toString();
}
