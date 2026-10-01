package com.auco.tempered.client.guide;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import com.auco.tempered.config.GameplaySettings;
import com.auco.tempered.config.TemperedConfig;
import com.auco.tempered.presentation.GuideSettingsContent;
import com.auco.tempered.presentation.GuideTopic;
import guideme.compiler.PageCompiler;
import guideme.compiler.tags.BlockTagCompiler;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytParagraph;
import guideme.document.block.LytVBox;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;

/** Adapts typed, translated balance explanations to GuideME's layout API. */
public final class SettingsTagCompiler extends BlockTagCompiler {
    private static final PageCompiler.State<GameplaySettings> SETTINGS =
            new PageCompiler.State<>("tempered:settings", GameplaySettings.class, null);

    @Override
    public Set<String> getTagNames() { return Set.of("tempered:Settings"); }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields element) {
        String id = element.getAttributeString("topic", "");
        var topic = Arrays.stream(GuideTopic.values()).filter(value -> value.id().equals(id)
                && value.hasSettings()).findFirst().orElse(null);
        if (topic == null) {
            String supported = Arrays.stream(GuideTopic.values()).filter(GuideTopic::hasSettings)
                    .map(GuideTopic::id).collect(Collectors.joining(", "));
            parent.appendError(compiler, "topic must be " + supported, element);
            return;
        }
        // One snapshot for every settings tag in a page; no compiled-value cache across page visits.
        var settings = compiler.getCompilerState(SETTINGS);
        if (settings == null) {
            settings = TemperedConfig.active();
            compiler.setCompilerState(SETTINGS, settings);
        }
        var block = new LytVBox();
        block.setGap(5);
        for (var line : GuideSettingsContent.build(topic, settings)) {
            var paragraph = new LytParagraph();
            paragraph.appendComponent(line);
            block.append(paragraph);
        }
        parent.append(block);
    }
}
