package mezz.jei.gui.overlay.config;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.gui.input.IUserInputHandler;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class CheckboxCategoryConfigWidget extends CheckboxWidget {

    protected final List<ConfigWidget> subConfigs;
    private boolean expand = false;

    public CheckboxCategoryConfigWidget(Component name, IJeiConfigValue<Boolean> configValue, List<ConfigWidget> subConfigs) {
        super(name, configValue);
        this.subConfigs = new ArrayList<>(subConfigs);
    }

    @Override
    public IUserInputHandler createInputHandler() {
        return super.createInputHandler();
    }
}
