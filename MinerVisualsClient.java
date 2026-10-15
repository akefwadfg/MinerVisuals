package fun.minerworld.minervisuals;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class MinerVisualsClient implements ClientModInitializer {
    public static KeyMapping OPEN_MENU;

    @Override
    public void onInitializeClient() {
        // Открытие меню: ПРАВЫЙ SHIFT
        OPEN_MENU = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.minervisuals.open_menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.minervisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MENU.consumeClick()) {
                if (client.screen == null && client.player != null) {
                    client.setScreen(new MinerVisualsScreen());
                }
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(ChinaHatRenderer::render);
    }
}
