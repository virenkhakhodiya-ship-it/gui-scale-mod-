package com.example.guiscale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiScaleScreen extends Screen {
    private double currentScale;
    private EditBox inputField;
    private ScaleSlider slider;

    public GuiScaleScreen() {
        super(Component.literal("Adjust GUI Scale"));
        this.currentScale = Minecraft.getInstance().options.guiScale().get() == 0 ? 1.0 : Minecraft.getInstance().options.guiScale().get() / 2.0;
        if (currentScale < 0.5) currentScale = 0.5;
        if (currentScale > 2.0) currentScale = 2.0;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.slider = new ScaleSlider(centerX - 100, centerY - 30, 200, 20, (this.currentScale - 0.5) / 1.5);
        this.addRenderableWidget(this.slider);

        this.inputField = new EditBox(this.font, centerX - 40, centerY + 5, 80, 20, Component.literal("Percentage"));
        this.inputField.setValue((int) (this.currentScale * 100) + "%");
        this.inputField.setResponder(text -> {
            try {
                String cleanText = text.replaceAll("[^0-9]", "");
                if (!cleanText.isEmpty()) {
                    int val = Integer.parseInt(cleanText);
                    val = Math.max(50, Math.min(200, val));
                    this.currentScale = val / 100.0;
                    this.slider.updateFromInput((this.currentScale - 0.5) / 1.5);
                    applyScale(this.currentScale);
                }
            } catch (NumberFormatException ignored) {}
        });
        this.addRenderableWidget(this.inputField);

        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> this.onClose())
                .bounds(centerX - 50, centerY + 40, 100, 20)
                .build());
    }

    private void applyScale(double scale) {
        Minecraft mc = Minecraft.getInstance();
        mc.options.guiScale().set((int) Math.round(scale * 2));
        mc.options.save();
        mc.resizeDisplay();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 60, 0xFFFFFF);
    }

    private class ScaleSlider extends AbstractSliderButton {
        public ScaleSlider(int x, int y, int width, int height, double value) {
            super(x, y, width, height, Component.empty(), value);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("GUI Scale: " + (int) Math.round((50 + this.value * 150)) + "%"));
        }

        @Override
        protected void applyValue() {
            currentScale = (50 + this.value * 150) / 100.0;
            if (inputField != null) inputField.setValue((int) (currentScale * 100) + "%");
            applyScale(currentScale);
        }

        public void updateFromInput(double sliderValue) {
            this.value = Math.max(0.0, Math.min(1.0, sliderValue));
            updateMessage();
        }
    }
}
