package fun.minerworld.minervisuals;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Небольшое анимированное меню MinerVisuals: 2 вкладки (Visuals, Misc). */
public class MinerVisualsScreen extends Screen {
    private static final String[] TABS = {"Visuals", "Misc"};

    private static final int PANEL_W = 250;
    private static final int PANEL_H = 140;
    private static final int TAB_W = 70;
    private static final int ROW_H = 22;

    private long openTime;
    private int tab = 0;
    private boolean hatSettings = false;
    private float tabUnderlineX = -1;

    public MinerVisualsScreen() {
        super(Component.literal("MinerVisuals"));
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------- helpers ----------

    private static float easeOutCubic(float t) {
        float f = 1f - t;
        return 1f - f * f * f;
    }

    private static int argb(int a, int r, int g, int b) {
        return (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255);
    }

    private static int lerp(int a, int b, float t) {
        return (int) (a + (b - a) * t);
    }

    private static int lerpColor(int c1, int c2, float t) {
        return argb(lerp(c1 >>> 24, c2 >>> 24, t), lerp((c1 >> 16) & 255, (c2 >> 16) & 255, t),
                lerp((c1 >> 8) & 255, (c2 >> 8) & 255, t), lerp(c1 & 255, c2 & 255, t));
    }

    private int px() { return (width - PANEL_W) / 2; }
    private int py() { return (height - PANEL_H) / 2; }
    private int contentY() { return py() + 36; }

    // Строка модуля China Hat
    private int rowX() { return px() + 10; }
    private int rowY() { return contentY() + 4; }
    private int rowW() { return PANEL_W - 20; }

    private boolean inRow(double mx, double my) {
        return mx >= rowX() && mx <= rowX() + rowW() && my >= rowY() && my <= rowY() + ROW_H;
    }

    // Квадраты выбора цвета
    private int swatchX(int i) { return px() + 30 + i * 70; }
    private int swatchY() { return contentY() + 22; }
    private static final int SWATCH = 32;

    // ---------- render ----------

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        float elapsed = (System.currentTimeMillis() - openTime) / 1000f;
        float appear = easeOutCubic(Math.min(1f, elapsed / 0.4f));

        g.fill(0, 0, width, height, argb((int) (130 * appear), 5, 5, 15));

        int w = (int) (PANEL_W * appear);
        int h = (int) (PANEL_H * appear);
        int x1 = (width - w) / 2;
        int y1 = (height - h) / 2;
        int x2 = x1 + w;
        int y2 = y1 + h;

        g.fill(x1, y1, x2, y2, argb((int) (235 * appear), 16, 16, 26));

        float pulse = (float) (Math.sin(elapsed * 2.5) * 0.5 + 0.5);
        int border = argb((int) (255 * appear), (int) (90 + 80 * pulse), (int) (60 + 40 * (1 - pulse)), 255);
        g.fill(x1, y1, x2, y1 + 2, border);
        g.fill(x1, y2 - 2, x2, y2, border);
        g.fill(x1, y1, x1 + 2, y2, border);
        g.fill(x2 - 2, y1, x2, y2, border);

        if (w > 20) {
            float run = (elapsed * 0.6f) % 1f;
            int barW = Math.max(20, w / 5);
            int bx = x1 + (int) ((w - barW) * run);
            g.fill(bx, y1, bx + barW, y1 + 2, argb(255, 255, 255, 255));
        }

        if (appear < 0.9f) {
            super.render(g, mouseX, mouseY, delta);
            return;
        }

        // Заголовок
        g.drawString(font, "MinerVisuals", px() + 10, py() + 8, 0xFFFFFFFF);

        // Вкладки
        int tabsX = px() + PANEL_W - 10 - TAB_W * TABS.length;
        int tabsY = py() + 6;
        for (int i = 0; i < TABS.length; i++) {
            int tx = tabsX + i * TAB_W;
            boolean hover = mouseX >= tx && mouseX < tx + TAB_W && mouseY >= tabsY && mouseY < tabsY + 20;
            int col = (i == tab) ? 0xFFFFFFFF : (hover ? 0xFFCCCCFF : 0xFF8888AA);
            g.drawCenteredString(font, TABS[i], tx + TAB_W / 2, tabsY + 5, col);
        }
        // Анимированное подчёркивание активной вкладки
        float targetX = tabsX + tab * TAB_W;
        if (tabUnderlineX < 0) tabUnderlineX = targetX;
        tabUnderlineX += (targetX - tabUnderlineX) * Math.min(1f, delta * 0.5f + 0.15f);
        g.fill((int) tabUnderlineX + 8, tabsY + 19, (int) tabUnderlineX + TAB_W - 8, tabsY + 21, border);

        // Разделитель
        g.fill(px() + 8, py() + 28, px() + PANEL_W - 8, py() + 29, argb(80, 255, 255, 255));

        if (tab == 0) {
            if (hatSettings) renderHatSettings(g, mouseX, mouseY);
            else renderVisuals(g, mouseX, mouseY);
        } else {
            g.drawCenteredString(font, "Empty", width / 2, contentY() + 30, 0xFF666680);
        }

        super.render(g, mouseX, mouseY, delta);
    }

    private void renderVisuals(GuiGraphics g, int mouseX, int mouseY) {
        float p = ChinaHat.progress();
        boolean hover = inRow(mouseX, mouseY);

        int off = argb(255, 30, 30, 44);
        int on = argb(255, 52, 40, 92);
        int bg = lerpColor(off, on, p);
        if (hover) bg = lerpColor(bg, argb(255, 255, 255, 255), 0.08f);
        g.fill(rowX(), rowY(), rowX() + rowW(), rowY() + ROW_H, bg);

        // акцентная полоска слева, выезжает при включении
        int accent = ChinaHat.color.rgb | 0xFF000000;
        g.fill(rowX(), rowY(), rowX() + (int) (3 * p), rowY() + ROW_H, accent);

        g.drawString(font, "China Hat", rowX() + 8, rowY() + 7, lerpColor(0xFFAAAAAA, 0xFFFFFFFF, p));

        // переключатель
        int tw = 24, th = 10;
        int tx = rowX() + rowW() - tw - 8;
        int ty = rowY() + (ROW_H - th) / 2;
        g.fill(tx, ty, tx + tw, ty + th, lerpColor(argb(255, 70, 70, 85), accent, p));
        int knobX = tx + 1 + (int) ((tw - th) * p);
        g.fill(knobX, ty + 1, knobX + th - 2, ty + th - 1, 0xFFFFFFFF);

        g.drawString(font, "LMB - toggle   RMB - settings", rowX(), rowY() + ROW_H + 8, 0xFF77778F);
    }

    private void renderHatSettings(GuiGraphics g, int mouseX, int mouseY) {
        g.drawString(font, "< Back   China Hat color", px() + 10, contentY() + 4, 0xFFFFFFFF);

        ChinaHat.HatColor[] colors = ChinaHat.HatColor.values();
        for (int i = 0; i < colors.length; i++) {
            int sx = swatchX(i);
            int sy = swatchY();
            boolean selected = ChinaHat.color == colors[i];
            boolean hover = mouseX >= sx && mouseX < sx + SWATCH && mouseY >= sy && mouseY < sy + SWATCH;
            int col = colors[i].rgb | 0xFF000000;

            if (selected || hover) {
                int o = selected ? 0xFFFFFFFF : 0x88FFFFFF;
                g.fill(sx - 2, sy - 2, sx + SWATCH + 2, sy + SWATCH + 2, o);
            }
            g.fill(sx, sy, sx + SWATCH, sy + SWATCH, col);
            g.drawCenteredString(font, colors[i].label, sx + SWATCH / 2, sy + SWATCH + 6, selected ? 0xFFFFFFFF : 0xFF8888AA);
        }
    }

    // ---------- input ----------

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        float elapsed = (System.currentTimeMillis() - openTime) / 1000f;
        if (elapsed < 0.3f) return true;

        // вкладки
        int tabsX = px() + PANEL_W - 10 - TAB_W * TABS.length;
        int tabsY = py() + 6;
        for (int i = 0; i < TABS.length; i++) {
            int tx = tabsX + i * TAB_W;
            if (mx >= tx && mx < tx + TAB_W && my >= tabsY && my < tabsY + 20) {
                tab = i;
                hatSettings = false;
                return true;
            }
        }

        if (tab == 0) {
            if (!hatSettings) {
                if (inRow(mx, my)) {
                    if (button == 0) ChinaHat.enabled = !ChinaHat.enabled; // ЛКМ: вкл/выкл
                    else if (button == 1) hatSettings = true;              // ПКМ: настройки
                    return true;
                }
            } else {
                // назад
                if (button == 1 || (my >= contentY() && my <= contentY() + 16 && mx >= px() + 10 && mx <= px() + 60)) {
                    hatSettings = false;
                    return true;
                }
                // выбор цвета
                ChinaHat.HatColor[] colors = ChinaHat.HatColor.values();
                for (int i = 0; i < colors.length; i++) {
                    int sx = swatchX(i);
                    int sy = swatchY();
                    if (mx >= sx && mx < sx + SWATCH && my >= sy && my < sy + SWATCH) {
                        ChinaHat.color = colors[i];
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Правый Shift закрывает меню (ESC тоже)
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
