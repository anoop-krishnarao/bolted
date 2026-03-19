package dev.anoopkrishnarao.bolted.keybind;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public class BoltedKeybinds {

    public static KeyMapping LOCK_KEY;

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("bolted", "general")
        );

        LOCK_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.bolted.lock",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_L,
                category
        ));
    }
}