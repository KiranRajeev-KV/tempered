package com.auco.tempered.client.guide;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.config.aspect.ReinforcedSettings;
import com.auco.tempered.config.equipment.EquipmentUpgradeSettings;
import com.auco.tempered.presentation.GuideTopic;
import guideme.compiler.PageCompiler;
import guideme.compiler.ParsedGuidePage;
import guideme.compiler.TagCompiler;
import guideme.Guide;
import guideme.indices.ItemIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Verifies packaged GuideME pages/shortcuts and recompilation after a server balance change. */
@ExtendWith(EphemeralTestServerProvider.class)
class GuideIntegrationTest {
    @Test
    void pagesHaveValidNavigationAndItemTargetsAndTagsRecompileActiveSettings(MinecraftServer server) throws Exception {
        server.submit(() -> {
            List<ParsedGuidePage> pages = new ArrayList<>();
            for (GuideTopic topic : GuideTopic.values()) {
                String source = pageSource(topic.pageId().getPath());
                var parsed = PageCompiler.parse("tempered", "en_us", topic.pageId(), source);
                assertNotNull(parsed.getFrontmatter().navigationEntry(), "Missing navigation for " + topic);
                if (topic != GuideTopic.OVERVIEW) {
                    assertEquals(GuideTopic.OVERVIEW.pageId(), parsed.getFrontmatter().navigationEntry().parent());
                }
                var links = Pattern.compile("\\]\\(([^)]+\\.md)\\)").matcher(source);
                while (links.find()) assertNotNull(pageSource(links.group(1)), "Broken link in " + topic);
                var settingsTags = Pattern.compile("<tempered:Settings topic=\"([^\"]+)\"\\s*/>").matcher(source);
                while (settingsTags.find()) {
                    String id = settingsTags.group(1);
                    assertTrue(java.util.Arrays.stream(GuideTopic.values())
                            .anyMatch(candidate -> candidate.hasSettings() && candidate.id().equals(id)),
                            "Invalid configured topic in " + topic + ": " + id);
                }
                // Parse errors are rendered as an error heading by GuideME instead of thrown.
                var heading = parsed.getAstRoot().children().stream()
                        .filter(guideme.libs.mdast.model.MdAstHeading.class::isInstance)
                        .map(guideme.libs.mdast.model.MdAstHeading.class::cast).findFirst().orElseThrow();
                assertEquals(1, heading.depth);
                assertNotEquals("PARSING ERROR",
                        assertInstanceOf(guideme.libs.mdast.model.MdAstText.class, heading.children().getFirst()).value);
                pages.add(parsed);
            }
            var items = new ItemIndex();
            items.rebuild(pages);
            assertEquals(GuideTopic.REINFORCED.pageId(), items.get(ResourceLocation.parse("tempered:reinforced_aspect")).pageId());
            assertEquals(GuideTopic.SWIFT.pageId(), items.get(ResourceLocation.parse("tempered:swift_aspect")).pageId());

            var guide = Guide.builder(ResourceLocation.parse("tempered:test_guide")).register(false)
                    .disableDefaultExtensions().extension(TagCompiler.EXTENSION_POINT, new SettingsTagCompiler()).build();
            var section = PageCompiler.parse("tempered", "en_us", ResourceLocation.parse("tempered:test.md"),
                    "<tempered:Settings topic=\"reinforced\" />");
            var before = TemperedConfig.active();
            try {
                var custom = new GameplaySettings(new ReinforcedSettings(true,
                        IntStream.rangeClosed(1, 100).mapToObj(i -> (double) i).toList()), before.swift(), before.executioner());
                TemperedConfig.activate(custom);
                var first = PageCompiler.compile(guide, guide.getExtensions(), section).document();
                assertEquals(101, first.getBlocks().getFirst().getChildren().size());
                TemperedConfig.activate(GameplaySettings.DEFAULT);
                var revisited = PageCompiler.compile(guide, guide.getExtensions(), section).document();
                assertEquals(GameplaySettings.DEFAULT.reinforced().maxLevel() + 1,
                        revisited.getBlocks().getFirst().getChildren().size());
                assertEquals(101, first.getBlocks().getFirst().getChildren().size(), "Captured page must not mutate");
                var wearSection = PageCompiler.parse("tempered", "en_us", section.getId(),
                        "<tempered:Settings topic=\"maintenance\" />");
                var fractionalWear = PageCompiler.compile(guide, guide.getExtensions(), wearSection).document();
                String originalWear = fractionalWear.getTextContent();
                var defaults = GameplaySettings.DEFAULT;
                TemperedConfig.activate(new GameplaySettings(defaults.reinforced(), defaults.swift(), defaults.executioner(),
                        defaults.acquisition(), new EquipmentUpgradeSettings(EquipmentUpgradeSettings.DamagePolicy.DAMAGE_POINTS)));
                var pointsWear = PageCompiler.compile(guide, guide.getExtensions(), wearSection).document();
                assertNotEquals(originalWear, pointsWear.getTextContent(), "Revisited maintenance page must reflect the server policy");
                assertEquals(originalWear, fractionalWear.getTextContent(), "An already compiled page keeps its snapshot");
                var invalid = PageCompiler.parse("tempered", "en_us", section.getId(), "<tempered:Settings topic=\"unknown\" />");
                assertTrue(PageCompiler.compile(guide, guide.getExtensions(), invalid).document().getTextContent()
                        .contains("topic must be reinforced"));
            } finally {
                TemperedConfig.activate(before);
            }
        }).get();
    }

    private static String pageSource(String name) {
        try (var input = GuideIntegrationTest.class.getResourceAsStream("/assets/tempered/guides/tempered/guide/" + name)) {
            assertNotNull(input, "Missing guide page: " + name);
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
    }
}
