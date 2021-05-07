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
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

import static org.assertj.core.api.Assertions.assertThat;

public class GitHubCommandListenerTest {

    GitHubCommandListener listener = new GitHubCommandListener();

    @Test
    @StdIo
    public void shouldReportViaGitHubWorkflowCommands(final StdOut out) throws NoSuchMethodException {
        final TestSource methodSource = MethodSource.from(TestClass.class, TestClass.class.getDeclaredMethod("failedTest"));
        final TestDescriptor descriptor = new AbstractTestDescriptor(UniqueId.root("test", "value"), "test", methodSource) {
            @Override
            public Type getType() {
                return Type.TEST;
            }
        };

        final TestIdentifier testIdentifier = TestIdentifier.from(descriptor);

        final Throwable throwable = TestClass.from(TestClass::failedTest);

        listener.executionFinished(testIdentifier, TestExecutionResult.failed(throwable));

        final String[] capturedLines = out.capturedLines();
        assertThat(capturedLines).hasSize(1);
        assertThat(capturedLines[0])
            .startsWith("::warning file=")
            .endsWith("TestClass.java,line=27,col=0::FAILED - Expecting value to be false but was true");
    }
}
