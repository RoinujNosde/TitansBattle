package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.types.Kit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Challenge extends BaseGame {

    private final ArenaConfiguration config;

    public Challenge(@NotNull TitansBattle plugin, @NotNull ArenaConfiguration config) {
        super(plugin);
        this.config = config;
    }

    @Override
    protected @Nullable String getLang(@NotNull String key) {
        String lang = null;
        if (!key.startsWith("challenge_")) {
            lang = super.getLang("challenge_" + key);
        }
        if (lang == null) {
            lang = super.getLang(key);
        }
        return lang;
    }

    @NotNull
    @Override
    public ArenaConfiguration getConfig() {
        return config;
    }

    @Override
    public void finish(boolean cancelled) {
        // TODO Implement finish logic
        super.finish(cancelled);
        plugin.getChallengeManager().remove(this);
    }
}