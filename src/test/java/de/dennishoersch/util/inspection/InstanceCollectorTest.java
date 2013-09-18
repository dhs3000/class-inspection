/*
 * Copyright 2012-2013 Dennis Hörsch.
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

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import de.dennishoersch.util.inspection.find_instances.TestInterface;

/**
 * @author hoersch
 * 
 */
public class InstanceCollectorTest {

    /**
     * Test method for {@link InstanceCollector#instances()}.
     */
    @Test
    public void testInstances() {
        InstanceCollector<TestInterface> instanceCollector = new InstanceCollector<>(TestInterface.class, TestInterface.class.getPackage().getName());

        assertThat(instanceCollector.instances(), size(3));

        assertThat(Collections2.transform(instanceCollector.instances(), toName()), containsAll("EINS", "ZWEI", "TestClass"));
    }

    private static Function<TestInterface, String> toName() {
        return new Function<TestInterface, String>() {
            @Override
            public String apply(TestInterface input) {
                return input.name();
            }
        };
    }

    private static TypeSafeMatcher<Collection<?>> size(final int size) {
        return new TypeSafeMatcher<Collection<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Collection of size " + size + ".");
            }

            @Override
            protected boolean matchesSafely(Collection<?> item) {
                return item.size() == size;
            }
        };
    }

    private static TypeSafeMatcher<Collection<String>> containsAll(final String... strings) {
        return new TypeSafeMatcher<Collection<String>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Collection containing all strings (" + Arrays.toString(strings) + ")");

            }

            @Override
            protected boolean matchesSafely(Collection<String> item) {
                for (String s : strings) {
                    if (!item.contains(s)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
