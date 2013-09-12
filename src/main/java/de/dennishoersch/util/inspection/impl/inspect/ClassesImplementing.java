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

import de.dennishoersch.util.inspection.InspectionHelper.ClassInfo;

/**
 * An inspector that checks if a class is implementing a given interface and
 * collects it.
 * 
 * @author hoersch
 * @param <T>
 */
public class ClassesImplementing<T> extends ClassesMatching<T> {

	private final Class<T> iface;

	/**
	 * @param iface
	 */
	public ClassesImplementing(Class<T> iface) {
		this.iface = iface;
	}

	@Override
	protected boolean isMatch(ClassInfo potentialMatch) {
		for (String implementedInterface : potentialMatch.getInterfaces()) {
			if (implementedInterface.equals(iface.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "classes implementing " + iface.getSimpleName();
	}
}
