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

import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.ParticipantDeathEvent;
import me.roinujnosde.titansbattle.managers.GameManager;
import org.bukkit.Bukkit;
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
    private final Helper helper;
    private final TitansBattle plugin;

    public PlayerDeathListener() {
        plugin = TitansBattle.getInstance();
        gm = plugin.getGameManager();
        helper = plugin.getHelper();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = helper.getPlayerAttackerOrKiller(victim.getKiller());

        if (!gm.isHappening() && !gm.isStarting() && killer != null) {
            if (helper.isKiller(victim)) {
                gm.setKiller(helper.getGameFromWinnerOrKiller(victim), killer, victim);
                plugin.getDatabaseManager().saveAll();
            }
        }

        if (gm.getParticipants().contains(victim.getUniqueId())) {
            Bukkit.getPluginManager().callEvent(new ParticipantDeathEvent(victim));
            if (gm.isStarting()) {
                gm.removeParticipant(gm.getCurrentGame(), victim);
            }
            if (gm.isHappening()) {
                if (helper.isFun(gm.getCurrentGame())) {
                    event.setKeepInventory(true);
                    victim.getInventory().clear();
                    victim.getInventory().setArmorContents(null);
                }
                gm.addCasualty(victim, killer);
            }
        }
    }
}
