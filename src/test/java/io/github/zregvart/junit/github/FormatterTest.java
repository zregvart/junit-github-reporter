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

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;

import static org.assertj.core.api.Assertions.assertThat;

public class FormatterTest {

    @Test
    public void shouldDetermineLine() {
        final Throwable throwable = TestClass.from(TestClass::failedTest);

        assertThat(Formatter.determineLine(TestClass.class, TestExecutionResult.failed(throwable))).isEqualTo(37);
    }

    @Test
    public void shouldDetermineLineOfNestedTest() {
        final Throwable throwable = TestClass.from(TestClass.NestedTest::failedTest);

        assertThat(Formatter.determineLine(TestClass.NestedTest.class, TestExecutionResult.failed(throwable))).isEqualTo(28);
    }

    @Test
    public void shouldDetermineTheMessage() {
        final Throwable throwable = TestClass.from(TestClass::failedTest);

        assertThat(Formatter.determineMessage(TestExecutionResult.failed(throwable))).isEqualTo("Expecting value to be false but was true");
    }

    @Test
    public void shouldFormatMessagesWithErrorSeverity() {
        final Throwable failed = TestClass.from(TestClass::failedTest);

        assertThat(Formatter.format(TestClass.class, TestExecutionResult.failed(failed))).isEqualTo(
            "::error file=src/test/java/io/github/zregvart/junit/github/TestClass.java,line=37,col=0::FAILED - Expecting value to be false but was true");
    }

    @Test
    public void shouldFormatMessagesWithWarningSeverity() {
        final Throwable aborted = TestClass.from(TestClass::abortedTest);

        assertThat(Formatter.format(TestClass.class, TestExecutionResult.aborted(aborted))).isEqualTo(
            "::warning file=src/test/java/io/github/zregvart/junit/github/TestClass.java,line=33,col=0::ABORTED - assumption was not met due to:  Expecting value to be false but was true");
    }
}
