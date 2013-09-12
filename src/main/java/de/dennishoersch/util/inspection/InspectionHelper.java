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

import java.io.IOException;

import javassist.bytecode.ClassFile;

/**
 * Helper that is passed to class inspectors when called to inspect a ClassFile.
 * @author hoersch
 */
public interface InspectionHelper {

	/**
	 * Loads a class.
	 *
	 * @param type
	 * @return the class
	 * @throws ClassNotFoundException
	 */
	Class<?> loadClass(ClassFile type) throws ClassNotFoundException;

	/**
	 * Gets a ClassInfo of a named class.
	 *
	 * @param name
	 * @return class info
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	ClassInfo getClassInfo(String name) throws IOException, ClassNotFoundException;

	/**
	 * Converts the given ClassFile to a ClassInfo object.
	 *
	 * @param type
	 * @return a class info object
	 */
	ClassInfo toClassInfo(ClassFile type);

	/**
	 * Simple wrapper of classes, may be backed by real classes or just
	 * {@link ClassFile}.
	 */
	public interface ClassInfo {
		/**
		 * @return name of the class
		 */
		public String getName();

		/**
		 * @return name of the super class
		 */
		public String getSuperclass();

		/**
		 * @return all interfaces of this class
		 */
		public String[] getInterfaces();
	}

}
