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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Tries to locate the file where a provided {@link Class} resides on the
 * filesystem.
 */
final class ClassSourceLookup {
    static final Supplier<Path> GITHUB_WORKSPACE = new Supplier<Path>() {

        private Path githubWorkspace;

        @Override
        public Path get() {
            if (githubWorkspace == null) {
                // not really a problem if we write from multiple threads the
                // value will be the same
                final String githubWorkspaceFromEnvironment = System.getenv("GITHUB_WORKSPACE");
                if (githubWorkspaceFromEnvironment == null || githubWorkspaceFromEnvironment.trim().isEmpty()) {
                    return Paths.get(".");
                }

                githubWorkspace = Paths.get(githubWorkspaceFromEnvironment);
            }
            return githubWorkspace;
        }
    };

    /**
     * Searches within {@code GITHUB_WORKSPACE} for file named
     * {@code package/TestClass.java}.
     */
    static Path sourcePathFor(final Class<?> clazz) {
        final Path githubWorkspace = GITHUB_WORKSPACE.get();

        final Path sourceFileEndPath = sourceFilePathByConvention(clazz);
        try {
            final Optional<Path> maybeSourceFilePath = Files
                .find(githubWorkspace, 42, (path, attrs) -> attrs.isRegularFile() && path.endsWith(sourceFileEndPath))
                .parallel()
                .findFirst();
            if (!maybeSourceFilePath.isPresent()) {
                return fallbackSourceFilePath(sourceFileEndPath);
            }
            return githubWorkspace.relativize(maybeSourceFilePath.get()).normalize();
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static Path fallbackSourceFilePath(final Path sourceFilePath) {
        return Paths.get("src", "test", "java").resolve(sourceFilePath);
    }

    private static Path sourceFilePathByConvention(final Class<?> clazz) {
        return Paths.get(topLevelClassOf(clazz).getName().replace('.', File.separatorChar) + ".java");
    }

    private static Class<?> topLevelClassOf(final Class<?> clazz) {
        Class<?> clz = clazz;
        while (clz.getEnclosingClass() != null) {
            clz = clz.getEnclosingClass();
        }

        return clz;
    }
}
