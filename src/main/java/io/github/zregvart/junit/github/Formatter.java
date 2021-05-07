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

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;

import static io.github.zregvart.junit.github.ClassSourceLookup.classToPath;

/**
 * Generates @see <a href=
 * "https://docs.github.com/en/actions/reference/workflow-commands-for-github-actions#setting-a-warning-message">GitHub
 * command formatted</a> string from the given {@link TestSource} and
 * {@link TestExecutionResult}.
 */
final class Formatter {

    @FunctionalInterface
    interface CommandFormatter {
        String format(TestExecutionResult result);
    }

    static Integer determineLine(final Class<?> testClazz, final TestExecutionResult result) {
        return result.getThrowable().map(t -> determineLine(testClazz.getName(), t)).orElse(0);
    }

    static int determineLine(final String className, final Throwable t) {
        for (final StackTraceElement e : t.getStackTrace()) {
            if (className.equals(e.getClassName())) {
                return e.getLineNumber();
            }
        }

        return 0;
    }

    static String determineMessage(final TestExecutionResult result) {
        return result.getThrowable().map(t -> t.getMessage()).orElse(result.toString()).replaceAll("\\R", " ").trim();
    }

    static <T extends TestSource> CommandFormatter forSource(final T source) {
        if (source instanceof MethodSource) {
            return result -> format((MethodSource) source, result);
        }

        if (source instanceof ClassSource) {
            return result -> format((ClassSource) source, result);
        }

        throw new IllegalArgumentException("Unsupported source type: " + source.getClass().getName());
    }

    private static String format(final Class<?> testClazz, final TestExecutionResult result) {
        final int line = determineLine(testClazz, result);

        final String message = determineMessage(result);

        final Status status = result.getStatus();

        return String.format("::error file=%s,line=%d,col=0::%s - %s", classToPath(testClazz), line, status, message);
    }

    private static String format(final ClassSource source, final TestExecutionResult result) {
        return format(source.getJavaClass(), result);
    }

    private static String format(final MethodSource source, final TestExecutionResult result) {
        return format(source.getJavaClass(), result);
    }
}
