package mezz.jei.gui.overlay.config;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class CheckboxWidget extends ConfigWidget {

    private static final int CHECKBOX_SIZE = 4;
    private static final int PADDING = 3;

    private final Component name;
    private final StateProvider stateProvider;

    public CheckboxWidget(Component name, StateProvider stateProvider) {
        this.name = name.copy();
        this.stateProvider = stateProvider;

        this.tooltipProvider = tooltipBuilder -> {
            tooltipBuilder.add(name.copy());
        };
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, Font font, double mouseX, double mouseY, float partialTicks) {
        int boxColor = stateProvider.get() ? 0xFF00FF00 : 0xFFFF0000;
        //draw checkbox
        guiGraphics.fill(width - PADDING - CHECKBOX_SIZE, PADDING, width - PADDING, CHECKBOX_SIZE + PADDING, boxColor);
        //draw name
        guiGraphics.drawString(font, name, PADDING, PADDING, 0XFFFFFFFF);
    }

    public boolean isOverCheckbox(double mouseX, double mouseY) {
        return mouseX <= x + width - PADDING &&
                mouseX >= x + width - PADDING - CHECKBOX_SIZE &&
                mouseY >= y + PADDING &&
                mouseY <= y + PADDING + CHECKBOX_SIZE;
    }

    public interface StateProvider {
        boolean get();

        void next();
    }

    @Override
    public IUserInputHandler createInputHandler() {
        return new UserInputHandler();
    }

    protected class UserInputHandler implements IUserInputHandler {
        @Override
        public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
            if (input.getKey().getType() == InputConstants.Type.MOUSE) {
                int button = input.getKey().getValue();
                boolean leftOrRight = button == InputConstants.MOUSE_BUTTON_LEFT || button == InputConstants.MOUSE_BUTTON_RIGHT;
                if (!leftOrRight) {
                    stateProvider.next();
                    return Optional.of(this);
                }
            }
            return Optional.empty();
        }
    }
}
