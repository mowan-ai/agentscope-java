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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.skill.SkillFilter;
import io.agentscope.core.tool.Toolkit;
import io.agentscope.harness.agent.filesystem.local.LocalFilesystem;
import io.agentscope.harness.agent.skill.WorkspaceSkillRepository;
import io.agentscope.harness.agent.skill.runtime.SkillPromptBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HarnessSkillMiddlewarePromptCustomizationTest {

    @TempDir Path workspace;

    @Test
    void customHeaderAndCatalogFilterKeepHiddenSkillsLoadable() throws IOException {
        writeSkill("weather", "查询天气");
        writeSkill("story-workflow", "由服务端注入的领域说明");
        WorkspaceSkillRepository repository =
                new WorkspaceSkillRepository(
                        new LocalFilesystem(workspace), "skills", "workspace", false);
        Toolkit toolkit = new Toolkit();
        HarnessSkillMiddleware middleware =
                new HarnessSkillMiddleware(List.of(repository), toolkit)
                        .promptBuilder(
                                new SkillPromptBuilder(
                                        "## 可用技能\n<available_skills>\n", null, false))
                        .catalogFilter(SkillFilter.except("story-workflow"));
        RuntimeContext context = RuntimeContext.builder().sessionId("s-1").build();

        String prompt = middleware.onSystemPrompt(null, context, "").block();

        assertTrue(prompt.startsWith("## 可用技能"));
        assertTrue(prompt.contains("查询天气"));
        assertFalse(prompt.contains("story-workflow"));
        assertFalse(prompt.contains("Skills provide specialized capabilities"));
        assertTrue(toolkit.getToolNames().contains("load_skill_through_path"));
        assertTrue(
                middleware.runtime().currentCatalog(context).ids().stream()
                        .anyMatch(id -> id.startsWith("story-workflow_")),
                "hidden skill must stay loadable");
    }

    private void writeSkill(String name, String description) throws IOException {
        Path directory = workspace.resolve("skills").resolve(name);
        Files.createDirectories(directory);
        Files.writeString(
                directory.resolve("SKILL.md"),
                "---\nname: " + name + "\ndescription: " + description + "\n---\n# " + name + "\n");
    }
}
