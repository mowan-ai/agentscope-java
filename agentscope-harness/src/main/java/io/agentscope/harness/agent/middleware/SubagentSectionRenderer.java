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

import java.util.List;

/**
 * Renders the {@code ## Subagents} system-prompt section injected by {@link SubagentsMiddleware}
 * and {@link DynamicSubagentsMiddleware} on every reasoning round.
 *
 * <p>Deployments that keep the subagent tools but need a different usage protocol (for example
 * background-only execution or a localized prompt) can supply a renderer through
 * {@code HarnessAgent.Builder#subagentSectionRenderer} instead of rewriting the default English
 * text after it has been assembled. The task summary and completion reminders are still appended
 * by the middleware.
 */
@FunctionalInterface
public interface SubagentSectionRenderer {

    /**
     * @param visibleEntries subagents the LLM may spawn; hidden and primary-only declarations are
     *     already filtered out, see {@link SubagentsMiddleware#visibleEntries(List)}
     * @param sessionMode whether the spawn/send tools are the {@code sessions_*} variants
     * @return the section text; it is prepended to the first system message as-is, so include
     *     any leading blank line the prompt needs. Return an empty string to omit the section.
     */
    String render(List<SubagentEntry> visibleEntries, boolean sessionMode);
}
