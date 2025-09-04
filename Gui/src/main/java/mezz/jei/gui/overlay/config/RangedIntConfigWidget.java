package mezz.jei.gui.overlay.config;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.runtime.config.IJeiConfigValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class RangedIntConfigWidget extends ConfigWidget {

    private final IJeiConfigValue<Integer> configValue;
    private final int min;
    private final int max;

    public RangedIntConfigWidget(IJeiConfigValue<Integer> configValue, int min, int max) {
        this.configValue = configValue;
        this.min = min;
        this.max = max;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY, float partialTicks) {

    }

    @Override
    protected void gatherTooltips(ITooltipBuilder tooltipBuilder) {

    }
}
