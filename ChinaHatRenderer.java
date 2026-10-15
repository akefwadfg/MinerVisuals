package fun.minerworld.minervisuals;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Рисует шапку-конус над головой игрока (видна от 3-го лица). */
public final class ChinaHatRenderer {
    private static final int SEGMENTS = 40;

    private ChinaHatRenderer() {}

    public static void render(WorldRenderContext ctx) {
        ChinaHat.tick();
        float p = ChinaHat.progress();
        if (p <= 0.005f) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        Camera cam = ctx.camera();
        if (!cam.isDetached()) return; // от первого лица шапку не видно

        PoseStack ps = ctx.matrixStack();
        if (ps == null) return;

        float pt = ctx.tickCounter().getGameTimeDeltaPartialTick(false);
        Vec3 pos = player.getPosition(pt);
        Vec3 c = cam.getPosition();

        float radius = 0.55f * p;
        float height = 0.30f * p;
        float alpha = 0.80f * p;
        double baseY = pos.y + player.getBbHeight() + 0.02;

        int rgb = ChinaHat.color.rgb;
        float r = ((rgb >> 16) & 255) / 255f;
        float g = ((rgb >> 8) & 255) / 255f;
        float b = (rgb & 255) / 255f;

        ps.pushPose();
        ps.translate(pos.x - c.x, baseY - c.y, pos.z - c.z);
        Matrix4f m = ps.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // Конус: светлая вершина, насыщенный край
        BufferBuilder cone = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        cone.addVertex(m, 0f, height, 0f).setColor(Math.min(1f, r + 0.35f), Math.min(1f, g + 0.35f), Math.min(1f, b + 0.35f), alpha);
        for (int i = 0; i <= SEGMENTS; i++) {
            double a = Math.PI * 2 * i / SEGMENTS;
            cone.addVertex(m, (float) Math.cos(a) * radius, 0f, (float) Math.sin(a) * radius).setColor(r, g, b, alpha);
        }
        BufferUploader.drawWithShader(cone.buildOrThrow());

        // Основание (темнее)
        BufferBuilder base = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        base.addVertex(m, 0f, 0f, 0f).setColor(r * 0.5f, g * 0.5f, b * 0.5f, alpha);
        for (int i = 0; i <= SEGMENTS; i++) {
            double a = Math.PI * 2 * i / SEGMENTS;
            base.addVertex(m, (float) Math.cos(a) * radius, 0f, (float) Math.sin(a) * radius).setColor(r * 0.7f, g * 0.7f, b * 0.7f, alpha);
        }
        BufferUploader.drawWithShader(base.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        ps.popPose();
    }
}
