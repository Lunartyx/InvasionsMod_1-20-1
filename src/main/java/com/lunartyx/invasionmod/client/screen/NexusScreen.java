package com.lunartyx.invasionmod.client.screen;

import com.lunartyx.invasionmod.InvasionMod;
import com.lunartyx.invasionmod.block.entity.NexusBlockEntity;
import com.lunartyx.invasionmod.screen.NexusScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NexusScreen extends HandledScreen<NexusScreenHandler> {
    private static final Identifier TEXTURE = new Identifier(InvasionMod.MOD_ID, "textures/gui/nexus.png");

    public NexusScreen(NexusScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = 8;
        this.titleY = 6;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int x = this.x;
        int y = this.y;
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int flux = this.handler.getFluxProgressScaled(26);
        if (flux > 0) {
            context.drawTexture(TEXTURE, x + 126, y + 28 + 26 - flux, 185, 26 - flux, 9, flux);
        }

        int cook = this.handler.getCookProgressScaled(18);
        if (cook > 0) {
            context.drawTexture(TEXTURE, x + 31, y + 51, 204, 0, cook, 2);
        }

        NexusBlockEntity.Mode mode = this.handler.getMode();
        if (mode == NexusBlockEntity.Mode.INVASION || mode == NexusBlockEntity.Mode.CONTINUOUS) {
            context.drawTexture(TEXTURE, x + 19, y + 29, 176, 0, 9, 31);
            context.drawTexture(TEXTURE, x + 19, y + 19, 194, 0, 9, 9);
        } else if (mode == NexusBlockEntity.Mode.ACTIVATING) {
            context.drawTexture(TEXTURE, x + 19, y + 29, 176, 31, 9, 31);
        }

        if (this.handler.isActivating()) {
            int activation = this.handler.getActivationProgressScaled(31);
            if (activation > 0) {
                context.drawTexture(TEXTURE, x + 19, y + 29 + 31 - activation, 176, 31 - activation, 9, activation);
            }
        } else if (this.handler.isContinuousActivation()) {
            int activation = this.handler.getActivationProgressScaled(31);
            if (activation > 0) {
                context.drawTexture(TEXTURE, x + 19, y + 29 + 31 - activation, 176, 62 - activation, 9, activation);
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        int color = 0x404040;
        context.drawText(this.textRenderer, Text.translatable("screen.invasionmod.nexus.level", this.handler.getNexusLevel()), 46, 6, color, false);
        MutableText modeName = Text.translatable("message.invasionmod.nexus.mode." + this.handler.getMode().getTranslationKey());
        context.drawText(this.textRenderer, Text.translatable("screen.invasionmod.nexus.mode", modeName), 13, 62, color, false);

        if (this.handler.getCurrentWave() > 0) {
            context.drawText(this.textRenderer, Text.translatable("screen.invasionmod.nexus.wave", this.handler.getCurrentWave()), 55, 37, color, false);
        }

        context.drawText(this.textRenderer,
                Text.translatable("screen.invasionmod.nexus.health", this.handler.getHealth(), NexusBlockEntity.MAX_HEALTH),
                96,
                60,
                color,
                false);

        context.drawText(this.textRenderer, Text.translatable("screen.invasionmod.nexus.radius", this.handler.getSpawnRadius()), 142, 72, color, false);

        if (this.handler.getMobsRemaining() > 0 || this.handler.getActiveMobs() > 0) {
            context.drawText(this.textRenderer,
                    Text.translatable("screen.invasionmod.nexus.mobs", this.handler.getMobsRemaining(), this.handler.getActiveMobs()),
                    96,
                    72,
                    color,
                    false);
        }

        if (this.handler.getWaveCooldown() > 0) {
            context.drawText(this.textRenderer,
                    Text.translatable("screen.invasionmod.nexus.cooldown", this.handler.getWaveCooldown()),
                    8,
                    72,
                    color,
                    false);
        }

        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.playerInventoryTitleY, color, false);
    }
}
