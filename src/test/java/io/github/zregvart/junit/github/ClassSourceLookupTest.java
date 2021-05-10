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

import net.bytebuddy.ByteBuddy;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassSourceLookupTest {
    @SetEnvironmentVariable(key = "GITHUB_WORKSPACE", value = ".")
    static class BasicCases {
        @Test
        public void shouldFindPathToNestedTestClass() {
            assertThat(ClassSourceLookup.sourcePathFor(TestClass.NestedTest.class)).isEqualTo(TestClass.SOURCE_PATH);
        }

        @Test
        public void shouldFindPathToTestClass() {
            assertThat(ClassSourceLookup.sourcePathFor(TestClass.class)).isEqualTo(TestClass.SOURCE_PATH);
        }

        @Test
        public void shouldProvideAFallback() {
            assertThat(ClassSourceLookup.sourcePathFor(Object.class)).isEqualTo(Paths.get("src", "test", "java", "java", "lang", "Object.java"));
        }
    }

    @SetEnvironmentVariable(key = "GITHUB_WORKSPACE", value = "src/test/resources")
    static class LookupWithinModule {
        @Test
        public void shouldFindSourceFilePathInModule() {
            final Path sourcePathFor = ClassSourceLookup.sourcePathFor(createClass("io.github.zregvart.junit.github.module.TestInModule"));
            final Path expected = Paths.get("module", "src", "test", "java", "io", "github", "zregvart", "junit", "github", "module", "TestInModule.java");

            assertThat(sourcePathFor).isEqualTo(expected);
        }

        @Test
        public void shouldFindSourceFilePathInSubModule() {
            final Path sourcePathFor = ClassSourceLookup.sourcePathFor(createClass("io.github.zregvart.junit.github.submodule.TestInSubmodule"));
            final Path expected = Paths.get("module", "submodule", "src", "test", "java", "io", "github", "zregvart", "junit", "github", "submodule",
                "TestInSubmodule.java");

            assertThat(sourcePathFor).isEqualTo(expected);
        }

        @Test
        public void shouldFindSourceFilePathInTopLevelModule() {
            final Path sourcePathFor = ClassSourceLookup.sourcePathFor(createClass("io.github.zregvart.junit.github.tlp.TestInTopProject"));
            final Path expected = Paths.get("src", "test", "java", "io", "github", "zregvart", "junit", "github", "tlp",
                "TestInTopProject.java");

            assertThat(sourcePathFor).isEqualTo(expected);
        }
    }

    static Class<?> createClass(final String fullyQualified) {
        return new ByteBuddy()
            .subclass(Object.class)
            .name(fullyQualified)
            .make()
            .load(ClassSourceLookupTest.class.getClassLoader())
            .getLoaded();
    }
}
