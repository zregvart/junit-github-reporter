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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Tries to locate the file where a provided {@link Class} resides on the
 * filesystem.
 */
final class ClassSourceLookup {
    /**
     * Uses {@link CodeSource} to get the path to the {@link Class} file and
     * then works its way up the filesystem tree to locate one of the candidate
     * file paths. In case none is found, a fallback results in
     * {@code package/Class.java} like path.
     */
    static Path classToPath(final Class<?> clazz) {
        final ProtectionDomain protectionDomain = clazz.getProtectionDomain();
        final CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            return fallbackClassToPath(clazz);
        }

        final URL location = codeSource.getLocation();
        Path path;
        try {
            path = Paths.get(location.toURI());
        } catch (final URISyntaxException e) {
            throw new IllegalStateException(e);
        }

        final List<Path> candidates = pathCandidatesFor(clazz);

        while (path != null && !Files.exists(path.resolve(".git"))) {
            for (final Path candidate : candidates) {
                final Path mightBePath = path.resolve(candidate);
                if (Files.exists(mightBePath)) {
                    return mightBePath.normalize();
                }
            }

            path = path.getParent();
        }

        return fallbackClassToPath(clazz);
    }

    private static Path fallbackClassToPath(final Class<?> clazz) {
        return Paths.get(clazz.getName().replace('.', File.separatorChar) + ".java");
    }

    private static List<Path> pathCandidatesFor(final Class<?> clazz) {
        final Class<?> clz = toplevelClassOf(clazz);

        final String sourceFile = clz.getSimpleName() + ".java";

        final List<Path> candidates = new ArrayList<>();
        final Path sourceFilePath = Paths.get(sourceFile);
        candidates.add(sourceFilePath);

        final Package pkg = clazz.getPackage();
        final String[] packages = pkg.getName().split("\\.");
        Path packagePath = Paths.get(".");
        for (final String p : packages) {
            packagePath = packagePath.resolve(Paths.get(p));
        }
        final Path packageAndSourcePath = packagePath.resolve(sourceFilePath);
        candidates.add(packageAndSourcePath);
        candidates.add(Paths.get("src", "test", "java").resolve(packageAndSourcePath));
        candidates.add(Paths.get("src", "it", "java").resolve(packageAndSourcePath));

        return candidates;
    }

    private static Class<?> toplevelClassOf(final Class<?> clazz) {
        Class<?> clz = clazz;
        while (clz.getEnclosingClass() != null) {
            clz = clz.getEnclosingClass();
        }

        return clz;
    }
}
