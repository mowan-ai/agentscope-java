/*
 * Copyright 2024-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.agentscope.harness.agent.middleware;

import java.time.LocalDate;
import java.util.List;

/**
 * Inputs available to a {@link WorkspaceContextRenderer}. Contents are already read for the current
 * call; {@code memoryContent} is already cut to the configured token budget.
 *
 * @param agentName agent name used by the default session header
 * @param today current date on the server clock
 * @param sessionId current session id, or null
 * @param agentsContent workspace {@code AGENTS.md}, empty when absent
 * @param memoryContent {@code MEMORY.md}, empty when absent or not injected
 * @param memoryTruncated whether {@code memoryContent} was cut to fit the token budget
 * @param memoryContextIncluded whether {@code MEMORY.md} injection is enabled for this agent
 * @param knowledgeContent {@code knowledge/KNOWLEDGE.md}, empty when absent
 * @param knowledgeFiles knowledge file paths relative to the workspace
 * @param memoryToolsEnabled whether the memory_* tools are registered
 * @param memoryHooksEnabled whether automatic memory flush/consolidation hooks are enabled
 */
public record WorkspaceContextView(
        String agentName,
        LocalDate today,
        String sessionId,
        String agentsContent,
        String memoryContent,
        boolean memoryTruncated,
        boolean memoryContextIncluded,
        String knowledgeContent,
        List<String> knowledgeFiles,
        boolean memoryToolsEnabled,
        boolean memoryHooksEnabled) {

    public WorkspaceContextView {
        agentsContent = agentsContent != null ? agentsContent : "";
        memoryContent = memoryContent != null ? memoryContent : "";
        knowledgeContent = knowledgeContent != null ? knowledgeContent : "";
        knowledgeFiles = knowledgeFiles != null ? List.copyOf(knowledgeFiles) : List.of();
    }
}
