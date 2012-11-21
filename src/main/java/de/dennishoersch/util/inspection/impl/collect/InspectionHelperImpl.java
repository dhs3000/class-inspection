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

import java.io.IOException;
import java.util.Map;

import javassist.bytecode.ClassFile;

import com.google.common.collect.Maps;

import de.dennishoersch.util.inspection.InspectionHelper;

final class InspectionHelperImpl implements InspectionHelper {
    private Map<String, ClassInfo> _classFiles = Maps.newHashMap();

    private final ClassLoader _classloader;

    private final Map<String, Entry> _entries = Maps.newLinkedHashMap();

    InspectionHelperImpl(ClassLoader classloader, Map<String, Entry> entries) {
        _classloader = classloader;
        _entries.putAll(entries);
    }

    @Override
    public Class<?> loadClass(ClassFile type) throws ClassNotFoundException {
        return _classloader.loadClass(type.getName());
    }

    @Override
    public ClassInfo getClassInfo(String name) throws IOException, ClassNotFoundException {
        ClassInfo classFile = _classFiles.get(name);
        if (classFile == null) {
            if (name.startsWith("java") || name.startsWith("com.sun")) {
                // java-*-Packages, welche z.b. in rt.jar liegen, können nicht ausgelesen werden, sollten aber auch nicht den Heap zumüllen (?)
                classFile = new NativeClassInfo(_classloader.loadClass(name));
            } else {
                Entry entry = _entries.get(name);
                if (entry == null) {
                    int lastDot = name.lastIndexOf(".");
                    if (lastDot >= 0) {
                        // Klasse ist nicht im gerade betrachteten Package, dieses Package nachladen (aber ausdrücklich nicht inspizieren)
                        String packageName = name.substring(0, lastDot);

                        Map<String, Entry> newEntries = new ClassFilesCollector(_classloader, packageName).getEntries();
                        _entries.putAll(newEntries);
                        entry = _entries.get(name);
                    }
                    if (entry == null) {
                        throw new ClassNotFoundException(name);
                    }
                }
                classFile = new ClassFileClassInfo(ClassCollector.toClassFile(entry.getContent()));
            }
            _classFiles.put(name, classFile);
        }
        return classFile;
    }

    private static class NativeClassInfo implements ClassInfo {
        private final Class<?> _clazz;

        NativeClassInfo(Class<?> clazz) {
            this._clazz = clazz;
        }

        @Override
        public String getName() {
            return _clazz.getName();
        }

        @Override
        public String getSuperclass() {
            return _clazz.getSuperclass().getName();
        }
    }

    private static class ClassFileClassInfo implements ClassInfo {
        private final ClassFile _classFile;

        ClassFileClassInfo(ClassFile classFile) {
            this._classFile = classFile;
        }

        @Override
        public String getName() {
            return _classFile.getName();
        }

        @Override
        public String getSuperclass() {
            return _classFile.getSuperclass();
        }
    }

    @Override
    public ClassInfo toClassInfo(ClassFile type) {
        return new ClassFileClassInfo(type);
    }

}