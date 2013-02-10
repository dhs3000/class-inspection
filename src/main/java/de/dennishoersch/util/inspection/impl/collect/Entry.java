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
package de.dennishoersch.util.inspection.impl.collect;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

abstract class Entry {
    private final String _className;

    Entry(String className) {
        _className = className;
    }

    String getClassName() {
        return _className;
    }

    abstract byte[] getContent() throws IOException;

    static Entry newEntry(String className, File file) {
        return new FileContent(className, file);
    }

    static Entry newEntry(String className, JarFile jar, JarEntry entry) {
        return new JarEntryContent(className, jar, entry);
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