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
import static org.mockito.Mockito.mock;

import io.agentscope.core.agent.Agent;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.middleware.ReasoningInput;
import io.agentscope.harness.agent.subagent.SubagentFactory;
import io.agentscope.harness.agent.subagent.task.TaskRepository;
import io.agentscope.harness.agent.workspace.WorkspaceManager;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

class SubagentSectionRendererTest {

    private static SubagentEntry entry(String name) {
        SubagentFactory factory = rc -> (Agent) null;
        return new SubagentEntry(name, name + " description", factory);
    }

    @Test
    void defaultTemplateIsUnchangedWithoutRenderer() {
        List<SubagentEntry> entries = List.of(entry("worker"));
        String text = SubagentsMiddleware.renderSubagentSection(entries, false);
        assertEquals(text, SubagentsMiddleware.renderSubagentSection(entries, false, null));
        assertTrue(text.contains("## Subagents"));
        assertTrue(text.contains("- `worker`: worker description"));
    }

    @Test
    void customRendererReplacesSectionWithVisibleEntries() {
        AtomicReference<Boolean> mode = new AtomicReference<>();
        SubagentSectionRenderer renderer =
                (visible, sessionMode) -> {
                    mode.set(sessionMode);
                    return "\n## 子代理\n"
                            + String.join(",", visible.stream().map(SubagentEntry::name).toList());
                };
        String text =
                SubagentsMiddleware.renderSubagentSection(
                        List.of(entry("a"), entry("b")), true, renderer);
        assertEquals("\n## 子代理\na,b", text);
        assertTrue(mode.get());
    }

    @Test
    void nullRenderResultOmitsSection() {
        assertEquals(
                "",
                SubagentsMiddleware.renderSubagentSection(
                        List.of(entry("a")), false, (visible, sessionMode) -> null));
    }

    @Test
    void middlewareInjectsCustomSectionIntoSystemMessage() {
        TaskRepository repo = mock(TaskRepository.class);
        SubagentsMiddleware middleware =
                new SubagentsMiddleware(List.of(entry("worker")), repo, (WorkspaceManager) null)
                        .setSectionRenderer((visible, sessionMode) -> "## 子代理\n自定义协议\n");
        AtomicReference<ReasoningInput> forwarded = new AtomicReference<>();
        Msg system = Msg.builder().role(MsgRole.SYSTEM).textContent("base").build();
        middleware
                .onReasoning(
                        null,
                        null,
                        new ReasoningInput(List.of(system), List.of(), null),
                        input -> {
                            forwarded.set(input);
                            return Flux.<AgentEvent>empty();
                        })
                .blockLast();
        String prompt = forwarded.get().messages().get(0).getTextContent();
        assertTrue(prompt.contains("自定义协议"));
        assertFalse(prompt.contains("You have access to subagent tools"));
    }
}
