/* Copyright 2005-2006 Tim Fennell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * File modified by Marc Weyland
 */
package de.dennishoersch.util.inspection.impl.collect;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.bytecode.ClassFile;

import org.apache.log4j.Logger;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

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
     * Scans for classes starting at the package provided and descending into subpackages.
     * Each class is offered up to the inspector as it is discovered.
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

    static class ClassFilesCollector {

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

                    logger.debug("Scanning for classes in [" + urlPath + "]");
                    File file = new File(urlPath);
                    if (file.isDirectory()) {
                        collectClassesInDirectory(packageName, file);
                    } else {
                        collectClassesInJar(packageName, file);
                    }
                }
            } catch (IOException ioe) {
                logger.warn("Could not read package: " + _packageName, ioe);
            }
        }

        /**
         * Finds matches in a physical directory on a filesystem.  Examines all
         * files within a directory - if the File object is not a directory, and ends with <i>.class</i>
         * the file is loaded and tested to see if it is acceptable according to the Test.  Operates
         * recursively to find classes within a folder structure matching the package structure.
         *
         * @param parent the package name up to this directory in the package hierarchy.  E.g. if
         *        /classes is in the classpath and we wish to examine files in /classes/org/apache then
         *        the values of <i>parent</i> would be <i>org/apache</i>
         * @param location a File object representing a directory
         */
        private void collectClassesInDirectory(String parent, File location) {
            File[] files = location.listFiles();

            // File.listFiles() can return null when an IO error occurs!
            if (files == null) {
                logger.warn("Could not list directory " + location.getAbsolutePath() + ".");
                return;
            }

            for (File file : files) {
                String packageOrClass = (parent == null ? file.getName() : parent + "/" + file.getName());

                if (file.isDirectory()) {
                    collectClassesInDirectory(packageOrClass, file);
                } else if (file.getName().endsWith(".class")) {
                    packageOrClass = packageOrClass.substring(0, packageOrClass.length() - 6).replace("/", ".");
                    _entries.put(packageOrClass, new FileContent(packageOrClass, file));
                }
            }
        }

        /**
         * Finds matching classes within a jar files that contains a folder structure
         * matching the package structure.  If the File is not a JarFile or does not exist a warning
         * will be logged, but no error will be raised.
         *
         * @param parent the parent package under which classes must be in order to be considered
         * @param jarfile the jar file to be examined for classes
         */
        private void collectClassesInJar(String parent, File jarfile) {
            try {
                JarFile jar = new JarFile(jarfile);
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (!entry.isDirectory() && name.startsWith(parent) && name.endsWith(".class")) {
                        name = name.substring(0, name.length() - 6).replace("/", ".");
                        _entries.put(name, new JarEntryContent(name, jar, entry));
                    }
                }
            } catch (IOException ioe) {
                logger.error("Could not search jar file '" + jarfile + "'.", ioe);
            }
        }
    }

    /**
     * Add the class designated by the fully qualified class name provided to the set of
     * resolved classes if and only if it is approved by the Test supplied.
     *
     * @param inspector the test used to determine if the class matches
     * @param fqn the fully qualified name of a class
     */
    private void letInspect(InspectionHelper helper, String className, byte[] classContent) {
        try {

            ClassFile type = toClassFile(classContent);

            logger.trace("Checking to see if class " + className + " matches criteria [" + _inspector + "]");

            _inspector.inspect(type, helper);
        } catch (Throwable t) {
            logger.warn("Could not examine class '" + className + "'" + " due to a " + t.getClass().getName() + " with message: " + t.getMessage());
        }
    }

    static ClassFile toClassFile(byte[] classContent) throws IOException {
        DataInputStream dstream = new DataInputStream(new ByteArrayInputStream(classContent));
        ClassFile type = new ClassFile(dstream);
        return type;
    }

    /**
     * Sets an ClassLoader to be used for class loading. The default is the context ClassLoader.
     *
     * @param classloader
     */
    public void setClassLoader(ClassLoader classloader) {
        _classloader = classloader;
    }

    static final class InspectionHelperImpl implements InspectionHelper {
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
                    classFile = new ClassFileClassInfo(toClassFile(entry.getContent()));
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

    private static abstract class Entry {
        private final String _className;

        Entry(String className) {
            _className = className;
        }

        String getClassName() {
            return _className;
        }

        abstract byte[] getContent() throws IOException;
    }

    private static class FileContent extends Entry {
        private final File _file;

        FileContent(String className, File file) {
            super(className);
            _file = file;
        }

        @Override
        public byte[] getContent() throws IOException {
            return Files.toByteArray(_file);
        }
    }

    private static class JarEntryContent extends Entry {
        private final JarEntry _entry;

        private final JarFile _jar;

        JarEntryContent(String className, JarFile jar, JarEntry entry) {
            super(className);
            _jar = jar;
            _entry = entry;
        }

        @Override
        public byte[] getContent() throws IOException {
            return ByteStreams.toByteArray(_jar.getInputStream(_entry));
        }
    }
}
