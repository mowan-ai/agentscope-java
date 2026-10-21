/*
 * Copyright 2024-2026 the original author or authors.
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
package io.agentscope.harness.agent.middleware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.harness.agent.workspace.WorkspaceManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class WorkspaceContextRendererTest {

    @TempDir Path workspace;

    @Test
    void rendererReplacesDefaultSectionAndReceivesWorkspaceFiles() throws Exception {
        Files.writeString(workspace.resolve("MEMORY.md"), "# 记忆\n- 喜欢恐龙");
        try (WorkspaceManager manager = new WorkspaceManager(workspace)) {
            WorkspaceContextMiddleware middleware =
                    new WorkspaceContextMiddleware(manager, "main", null, 8000, false, true);
            AtomicReference<WorkspaceContextView> seen = new AtomicReference<>();
            middleware.setRenderer(
                    view -> {
                        seen.set(view);
                        return "## 会话\n今天是 " + view.today() + "\n" + view.memoryContent();
                    });

            String prompt =
                    middleware
                            .onSystemPrompt(
                                    null,
                                    RuntimeContext.builder().sessionId("s-1").build(),
                                    "BASE\n")
                            .block();

            assertTrue(prompt.startsWith("BASE\n## 会话\n今天是 " + LocalDate.now()));
            assertTrue(prompt.contains("喜欢恐龙"));
            assertFalse(prompt.contains("AgentStateStore Context"));
            assertFalse(prompt.contains("Memory Recall"));
            assertFalse(prompt.contains("read_file"));
            assertEquals("s-1", seen.get().sessionId());
            assertTrue(seen.get().memoryContextIncluded());
            assertTrue(seen.get().memoryToolsEnabled());
            assertFalse(seen.get().memoryHooksEnabled());
            assertFalse(seen.get().memoryTruncated());
        }
    }

    @Test
    void rendererSeesMemoryCutToTokenBudget() throws Exception {
        Files.writeString(workspace.resolve("MEMORY.md"), "x".repeat(1000));
        try (WorkspaceManager manager = new WorkspaceManager(workspace)) {
            WorkspaceContextMiddleware middleware =
                    new WorkspaceContextMiddleware(manager, "main", null, 50, false, false);
            AtomicReference<WorkspaceContextView> seen = new AtomicReference<>();
            middleware.setRenderer(
                    view -> {
                        seen.set(view);
                        return "";
                    });

            assertEquals("BASE", middleware.onSystemPrompt(null, null, "BASE").block());
            assertTrue(seen.get().memoryTruncated());
            assertEquals(200, seen.get().memoryContent().length());
        }
    }

    @Test
    void nullRendererKeepsDefaultSection() throws Exception {
        try (WorkspaceManager manager = new WorkspaceManager(workspace)) {
            WorkspaceContextMiddleware middleware = new WorkspaceContextMiddleware(manager);
            middleware.setRenderer(null);
            String prompt = middleware.onSystemPrompt(null, null, "BASE\n").block();
            assertTrue(prompt.contains("## AgentStateStore Context"));
        }
    }
}
