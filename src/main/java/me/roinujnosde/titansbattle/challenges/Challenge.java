package me.roinujnosde.titansbattle.challenges;

import me.roinujnosde.titansbattle.BaseGame;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Challenge extends BaseGame {

    private final ArenaConfiguration config;

    // TODO Remove from ChallengeManager#challenges on finish

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
}