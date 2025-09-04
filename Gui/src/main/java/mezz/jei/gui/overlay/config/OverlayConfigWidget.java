package mezz.jei.gui.overlay.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class OverlayConfigWidget {


    public static OverlayConfigWidget create(int width, double mouseX, double mouseY) {
        List<ConfigWidget> categories = new ArrayList<>();;

        categories.add(
                new CheckboxCategoryConfigWidget(
                        Component.translatable("")
                )
        );

        int height = categories.size() * ConfigWidget.HEIGHT;
        return new OverlayConfigWidget(width, height, mouseX, mouseY, categories);
    }

    private final List<ConfigWidget> categories = new ArrayList<>();
    private final int posX;
    private final int posY;
    private int width;
    private int height;

    private OverlayConfigWidget(int width, int height, double mouseX, double mouseY, List<ConfigWidget> categories) {
        this.width = width;
        this.height = height;
        this.categories.addAll(categories);
        this.posX = (int) mouseX - width;
        this.posY = (int) mouseY - height;
    }

    public void render(GuiGraphics guiGraphics, double mouseX, double mouseY, float partialTicks) {

    }


}
