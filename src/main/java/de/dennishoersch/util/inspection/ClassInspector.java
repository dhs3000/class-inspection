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

import javassist.bytecode.ClassFile;

/**
 * Interface of a class inspector. Inspects a class file and may collect elements that matches a specific criteria.
 * @param <T>
 */
public interface ClassInspector<T> {

    /**
     * Will be called repeatedly with candidate classes.
     * @param type
     * @param helper
     */
    void inspect(ClassFile type, InspectionHelper helper);

    /**
     *
     * @return the collected elements
     */
    Collection<T> getElements();
}