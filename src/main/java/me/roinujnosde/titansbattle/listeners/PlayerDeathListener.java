/*
 * The MIT License
 *
 * Copyright 2017 Edson Passos - edsonpassosjr@outlook.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.roinujnosde.titansbattle.listeners;

import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.games.Game;
import me.roinujnosde.titansbattle.managers.DatabaseManager;
import me.roinujnosde.titansbattle.managers.GameManager;
import me.roinujnosde.titansbattle.types.GameConfiguration;
import me.roinujnosde.titansbattle.types.Warrior;
import me.roinujnosde.titansbattle.utils.Helper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 *
 * @author RoinujNosde
 */
public class PlayerDeathListener implements Listener {

    private final GameManager gm;
    private final DatabaseManager databaseManager;

    public PlayerDeathListener() {
        TitansBattle plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        databaseManager = plugin.getDatabaseManager();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = Helper.getPlayerAttackerOrKiller(victim.getKiller());

        Game game = gm.getCurrentGame().orElse(null);
        if (game == null) {
            if (killer != null && Helper.isKiller(victim)) {
                GameConfiguration gameConfig = Helper.getGameConfigurationFromWinnerOrKiller(victim);
                if (gameConfig == null) {
                    return;
                }
                gm.setKiller(gameConfig, killer, victim);
                databaseManager.saveAll();
            }
            return;
        }
        Warrior warrior = databaseManager.getWarrior(victim.getUniqueId());
        game.onDeath(warrior, killer != null ?
                databaseManager.getWarrior(killer.getUniqueId()) : null);
        if (game.shouldKeepInventoryOnDeath(warrior)) {
            event.setKeepInventory(true);
        }
        if (game.shouldClearDropsOnDeath(warrior)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }
}
