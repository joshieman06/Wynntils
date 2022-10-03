/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.gui.render.FontRenderer;
import com.wynntils.gui.render.HorizontalAlignment;
import com.wynntils.gui.render.RenderUtils;
import com.wynntils.gui.screens.SearchableScreen;
import com.wynntils.mc.objects.CommonColors;
import com.wynntils.mc.utils.McUtils;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

// FIXME: Add selection support to this class to be a fully working text box
public class TextInputBoxWidget extends AbstractWidget {
    private static final char DEFAULT_CURSOR_CHAR = '_';
    private final Consumer<String> onUpdateConsumer;
    protected String textBoxInput = "";
    private int cursorPosition = 0;
    private long lastCursorSwitch = 0;
    private boolean renderCursor = true;

    protected final SearchableScreen searchableScreen;

    protected TextInputBoxWidget(
            int x,
            int y,
            int width,
            int height,
            Component boxTitle,
            Consumer<String> onUpdateConsumer,
            SearchableScreen searchableScreen) {
        super(x, y, width, height, boxTitle);
        this.onUpdateConsumer = onUpdateConsumer;
        this.searchableScreen = searchableScreen;
    }

    public TextInputBoxWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, SearchableScreen searchableScreen) {
        super(x, y, width, height, TextComponent.EMPTY);
        this.onUpdateConsumer = onUpdateConsumer;
        this.searchableScreen = searchableScreen;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        poseStack.pushPose();

        poseStack.translate(this.x, this.y, 0);

        this.renderBg(poseStack, McUtils.mc(), mouseX, mouseY);

        String renderedText = getRenderedText(this.width - 4);

        FontRenderer.getInstance()
                .renderAlignedTextInBox(
                        poseStack,
                        renderedText,
                        2,
                        this.width,
                        2,
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.Left,
                        FontRenderer.TextShadow.NORMAL);

        poseStack.popPose();
    }

    protected String getRenderedText(float maxTextWidth) {
        Font font = FontRenderer.getInstance().getFont();

        String cursorChar = getRenderCursorChar();

        final float cursorWidth = font.width(String.valueOf(this.getCursorChar()));

        String renderedText;
        if (font.width(textBoxInput + this.getCursorChar()) < maxTextWidth) {
            renderedText =
                    (textBoxInput.substring(0, cursorPosition) + cursorChar + textBoxInput.substring(cursorPosition));
        } else {
            // This case, the input is too long, only render text that fits, and is closest to cursor
            StringBuilder builder = new StringBuilder(cursorChar);

            int stringPosition = Math.min(textBoxInput.length() - 1, cursorPosition);

            while (font.width(builder.toString()) < maxTextWidth - cursorWidth && stringPosition >= 0) {
                builder.append(textBoxInput.charAt(stringPosition));

                stringPosition--;
            }

            builder.reverse();

            stringPosition = cursorPosition + 1;

            while (font.width(builder.toString()) < maxTextWidth - cursorWidth
                    && stringPosition < this.textBoxInput.length()) {
                builder.append(textBoxInput.charAt(stringPosition));

                stringPosition++;
            }

            renderedText = builder.toString();
        }
        return renderedText;
    }

    @Override
    protected void renderBg(PoseStack poseStack, Minecraft minecraft, int mouseX, int mouseY) {
        RenderUtils.drawRect(poseStack, CommonColors.BLACK, 0, 0, 0, this.width, this.height);
        RenderUtils.drawRectBorders(poseStack, CommonColors.WHITE, 0, 0, this.width, this.height, 0, 2);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        McUtils.soundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        searchableScreen.setFocusedTextInput(this);

        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textBoxInput == null) {
            textBoxInput = "";
        }

        textBoxInput = textBoxInput.substring(0, cursorPosition) + codePoint + textBoxInput.substring(cursorPosition);
        cursorPosition = Math.min(textBoxInput.length(), cursorPosition + 1);
        this.onUpdateConsumer.accept(this.getTextBoxInput());
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            removeFocus();
            return true;
        }

        if (Screen.isCopy(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getTextBoxInput());
            return true;
        } else if (Screen.isPaste(keyCode)) {
            this.setTextBoxInput((textBoxInput.substring(0, cursorPosition)
                    + Minecraft.getInstance().keyboardHandler.getClipboard()
                    + textBoxInput.substring(cursorPosition)));
            return true;
        } else if (Screen.isCut(keyCode)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(getTextBoxInput());
            setTextBoxInput("");

            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (textBoxInput.isEmpty()) {
                return false;
            }

            if (Screen.hasControlDown()) {
                setTextBoxInput("");
                return true;
            }

            textBoxInput =
                    textBoxInput.substring(0, Math.max(0, cursorPosition - 1)) + textBoxInput.substring(cursorPosition);
            cursorPosition = Math.max(0, cursorPosition - 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (textBoxInput.isEmpty()) {
                return false;
            }

            if (Screen.hasControlDown()) {
                setTextBoxInput(textBoxInput.substring(0, cursorPosition));
                return true;
            }

            textBoxInput = textBoxInput.substring(0, cursorPosition)
                    + textBoxInput.substring(Math.min(textBoxInput.length(), cursorPosition + 1));
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (Screen.hasControlDown()) {
                cursorPosition = 0;
                return true;
            }

            cursorPosition = Math.max(0, cursorPosition - 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (Screen.hasControlDown()) {
                cursorPosition = textBoxInput.length();
                return true;
            }

            cursorPosition = Math.min(textBoxInput.length(), cursorPosition + 1);
            this.onUpdateConsumer.accept(this.getTextBoxInput());
            return true;
        }

        return false;
    }

    @Override
    public boolean isFocused() {
        return searchableScreen.getFocusedTextInput() == this;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {}

    private String getRenderCursorChar() {
        String cursorChar;
        if (System.currentTimeMillis() - lastCursorSwitch > 350) {
            renderCursor = !renderCursor;
            lastCursorSwitch = System.currentTimeMillis();
        }

        cursorChar = isFocused() && renderCursor ? String.valueOf(this.getCursorChar()) : "";
        return cursorChar;
    }

    protected void removeFocus() {
        searchableScreen.setFocusedTextInput(null);
    }

    public void setTextBoxInput(String textBoxInput) {
        this.textBoxInput = textBoxInput;
        this.cursorPosition = this.textBoxInput.length();
        this.onUpdateConsumer.accept(this.textBoxInput);
    }

    private char getCursorChar() {
        return this.DEFAULT_CURSOR_CHAR;
    }

    public String getTextBoxInput() {
        return textBoxInput;
    }
}