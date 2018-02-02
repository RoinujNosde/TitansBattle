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

import java.text.MessageFormat;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.events.NewKillerEvent;
import me.roinujnosde.titansbattle.events.ParticipantDeathEvent;
import me.roinujnosde.titansbattle.managers.ConfigManager;
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
    private final ConfigManager cm;
    private final Helper helper;

    public PlayerDeathListener() {
        gm = TitansBattle.getGameManager();
        cm = TitansBattle.getConfigManager();
        helper = TitansBattle.getHelper();
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = helper.getPlayerAttackerOrKiller(victim.getKiller());

        if (!gm.isHappening() && !gm.isStarting() && killer != null) {
            if (helper.isKiller(victim)) {
                NewKillerEvent nke = new NewKillerEvent(killer, victim);
                Bukkit.getPluginManager().callEvent(nke);
                gm.setKiller(helper.getGameFromWinnerOrKiller(victim), killer);
            }
        }

        if (gm.getParticipants().contains(victim.getUniqueId())) {
            ParticipantDeathEvent pde = new ParticipantDeathEvent(victim);
            Bukkit.getPluginManager().callEvent(pde);
            if (gm.isStarting()) {
                gm.removeParticipant(gm.getCurrentGame(), victim);
            }
            if (gm.isHappening()) {
                if (helper.isFun(gm.getCurrentGame())) {
                    event.setKeepInventory(true);
                    victim.getInventory().clear();
                    victim.getInventory().setArmorContents(null);
                }
                gm.getCasualties().add(victim);
                victim.sendMessage(TitansBattle.getLang("watch_to_the_end", gm.getCurrentGame()));
                if (killer != null) {
                    helper.increaseKillsCount(killer);
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("killed_by", gm.getCurrentGame()), victim.getName(), killer.getName()));
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("has_killed_times", gm.getCurrentGame()), killer.getName(), Integer.toString(gm.getPlayerKillsCount(killer))));
                } else {
                    Bukkit.broadcastMessage(MessageFormat.format(TitansBattle.getLang("died_by_himself", gm.getCurrentGame()), victim.getName()));
                }
                gm.removeParticipant(gm.getCurrentGame(), victim);
            }
        }

    }
}
