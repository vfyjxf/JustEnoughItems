package mezz.jei.gui.overlay.config;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.handlers.NullInputHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public abstract class ConfigWidget {

    protected static final int HEIGHT = 20;

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean focused;
    protected TooltipProvider tooltipProvider;

    protected abstract void renderWidget(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY, float partialTicks);

    public final void render(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY, float partialTicks) {
        this.focused = mouseX > x && mouseX < x + width &&
                mouseY > y && mouseY < y + height;

        guiGraphics.pose().pushPose();
        {
            guiGraphics.pose().translate(x, y, 0);
            this.renderWidget(guiGraphics, font, mouseX, mouseY, partialTicks);
        }
        guiGraphics.pose().pushPose();
    }

    public void layout() {}

    protected void gatherTooltips(ITooltipBuilder tooltipBuilder) {
        if (tooltipProvider != null) {
            tooltipProvider.build(tooltipBuilder);
        }
    }

    public IUserInputHandler createInputHandler() {
        return NullInputHandler.INSTANCE;
    }

    @FunctionalInterface
    public interface TooltipProvider {
        void build(ITooltipBuilder tooltipBuilder);
    }
}
