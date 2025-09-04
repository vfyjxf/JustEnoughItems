package mezz.jei.gui.overlay.config;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CategoryConfigWidget extends ConfigWidget {

    private final Component name;
    private final List<ConfigWidget> subConfigs;

    private boolean expand;

    public CategoryConfigWidget(Component name, List<ConfigWidget> subConfigs) {
        this.name = name;
        this.subConfigs = new ArrayList<>(subConfigs);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY, float partialTicks) {

    }

}
