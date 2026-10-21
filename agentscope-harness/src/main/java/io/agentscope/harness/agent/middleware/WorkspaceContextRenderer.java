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

/**
 * Renders the workspace system-prompt section appended by {@link WorkspaceContextMiddleware}.
 *
 * <p>The default section describes the host environment, workspace paths, file tools and memory
 * usage in English. Deployments that disable file tools, run a localized product, or manage memory
 * through their own pipeline can supply a renderer through
 * {@code HarnessAgent.Builder#workspaceContextRenderer} and keep only what their agent needs.
 * The renderer only changes prompt text; memory tools and hooks are configured separately.
 */
@FunctionalInterface
public interface WorkspaceContextRenderer {

    /**
     * @return the section appended to the system prompt; an empty string appends nothing
     */
    String render(WorkspaceContextView view);
}
