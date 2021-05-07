/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.zregvart.junit.github;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassSourceLookupTest {
    Path testClassPath = Paths.get("src", "test", "java", "io", "github", "zregvart", "junit", "github", "TestClass.java");

    @Test
    public void shouldFindPathToNestedTestClass() {
        assertThat(ClassSourceLookup.classToPath(TestClass.NestedTest.class)).endsWith(testClassPath);
    }

    @Test
    public void shouldFindPathToTestClass() {
        assertThat(ClassSourceLookup.classToPath(TestClass.class)).endsWith(testClassPath);
    }

    @Test
    public void shouldProvideAFallback() {
        assertThat(ClassSourceLookup.classToPath(Object.class)).extracting(Path::toString).isEqualTo(Paths.get("java", "lang", "Object.java").toString());
    }
}
