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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.Maps;

class ClassFilesCollector {

    private final ClassLoader _classloader;

    private Map<String, Entry> _entries;

    private final String _packageName;

    ClassFilesCollector(ClassLoader classloader, String packageName) {
        _classloader = classloader;
        _packageName = packageName;
    }

    Map<String, Entry> getEntries() {
        if (_entries == null) {
            _entries = Maps.newLinkedHashMap();
            collectClassFiles();
        }

        return _entries;
    }

    private void collectClassFiles() {
        try {
            String packageName = _packageName.replace('.', '/');
            // Collect classes
            Enumeration<URL> urls = _classloader.getResources(packageName);
            while (urls.hasMoreElements()) {
                String urlPath = urls.nextElement().getFile();
                urlPath = URLDecoder.decode(urlPath, "UTF-8");

                // If it's a file in a directory, trim the stupid file: spec
                if (urlPath.startsWith("file:")) {
                    urlPath = urlPath.substring(5);
                }

                // Else it's in a JAR, grab the path to the jar
                if (urlPath.indexOf('!') > 0) {
                    urlPath = urlPath.substring(0, urlPath.indexOf('!'));
                }

                ClassCollector.logger.debug("Scanning for classes in [" + urlPath + "]");
                File file = new File(urlPath);
                if (file.isDirectory()) {
                    collectClassesInDirectory(packageName, file);
                } else {
                    collectClassesInJar(packageName, file);
                }
            }
        } catch (IOException ioe) {
            ClassCollector.logger.warn("Could not read package: " + _packageName, ioe);
        }
    }

    private void collectClassesInDirectory(String parent, File location) {
        File[] files = location.listFiles();

        if (files == null) {
            ClassCollector.logger.warn("Could not list directory " + location.getAbsolutePath() + ".");
            return;
        }

        for (File file : files) {
            String packageOrClass = (parent == null ? file.getName() : parent + "/" + file.getName());

            if (file.isDirectory()) {
                collectClassesInDirectory(packageOrClass, file);
            } else if (file.getName().endsWith(".class")) {
                packageOrClass = packageOrClass.substring(0, packageOrClass.length() - 6).replace("/", ".");
                _entries.put(packageOrClass, Entry.newEntry(packageOrClass, file));
            }
        }
    }

    private void collectClassesInJar(String parent, File jarfile) {
        try {
            JarFile jar = new JarFile(jarfile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith(parent) && name.endsWith(".class")) {
                    name = name.substring(0, name.length() - 6).replace("/", ".");
                    _entries.put(name, Entry.newEntry(name, jar, entry));
                }
            }
        } catch (IOException ioe) {
            ClassCollector.logger.error("Could not search jar file '" + jarfile + "'.", ioe);
        }
    }
}