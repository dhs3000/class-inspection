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
package de.dennishoersch.util.inspection.impl.collect;

import static de.dennishoersch.util.inspection.impl.collect.InspectionHelperImpl.toClassFile;

import java.io.IOException;
import java.util.Map;

import javassist.bytecode.ClassFile;

import org.apache.log4j.Logger;

import de.dennishoersch.util.inspection.ClassInspector;
import de.dennishoersch.util.inspection.InspectionHelper;

/**
 *
 * @author hoersch
 * @param <T>
 */
public class ClassCollector<T, CI extends ClassInspector<T>> {
    static final Logger logger = Logger.getLogger(ClassCollector.class);

    private ClassLoader _classloader = Thread.currentThread().getContextClassLoader();

    private final CI _inspector;

    private final String _packageName;

    /**
     * @param inspector an instance of {@link ClassInspector} that will be used to inspect classes
     * @param packageName the name of the package from which to start scanning for classes
     */
    public ClassCollector(CI inspector, String packageName) {
        _inspector = inspector;
        _packageName = packageName;

    }

    /**
     * Scans for classes recursively starting at the given package and let the inspector inspect each one.
     *
     * @return the inspector
     */
    public CI findAndLetInspect() {

        Map<String, Entry> entries = new ClassFilesCollector(_classloader, _packageName).getEntries();

        // Inspect collected classes
        InspectionHelper helper = new InspectionHelperImpl(_classloader, entries);
        for (Entry entry : entries.values()) {
            try {
                letInspect(helper, entry.getClassName(), entry.getContent());
            } catch (IOException e) {
                logger.error("Could not read class '" + entry.getClassName() + "'!", e);
            }
        }

        return _inspector;
    }

    private void letInspect(InspectionHelper helper, String className, byte[] classContent) {
        try {

            ClassFile type = toClassFile(classContent);

            logger.trace("Checking to see if class " + className + " matches criteria [" + _inspector + "]");

            _inspector.inspect(type, helper);
        } catch (Throwable t) {
            logger.warn("Could not examine class '" + className + "'" + " due to a " + t.getClass().getName() + " with message: " + t.getMessage());
        }
    }

    /**
     * Sets an ClassLoader to be used for class loading. The default is the context ClassLoader.
     *
     * @param classloader
     */
    public void setClassLoader(ClassLoader classloader) {
        _classloader = classloader;
    }
}
