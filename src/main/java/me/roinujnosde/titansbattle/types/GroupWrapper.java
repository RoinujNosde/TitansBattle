/*
 * The MIT License
 *
 * Copyright 2018 Edson Passos - edsonpassosjr@outlook.com.
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
package me.roinujnosde.titansbattle.types;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import java.util.UUID;
import me.roinujnosde.titansbattle.TitansBattle;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import org.bukkit.Bukkit;

/**
 *
 * @author Edson Passos <edsonpassosjr@outlook.com>
 */
public class GroupWrapper {

    private Object base;
    private TitansBattle plugin;

    public GroupWrapper(Object base) {
        if (base == null) {
            throw new IllegalArgumentException("base may not be null");
        }
        plugin = TitansBattle.getInstance();
        if (plugin.isFactions()) {
            if (!(base instanceof Faction)) {
                throw new IllegalArgumentException("base must be a instance of Faction");
            }
        }
        if (plugin.isSimpleClans()) {
            if (!(base instanceof Clan)) {
                throw new IllegalArgumentException("base must be a instance of Clan");
            }
        }
        this.base = base;
    }

    public Object getBase() {
        return base;
    }

    public String getId() {
        String id = "";
        if (plugin.isFactions()) {
            id = ((Faction) base).getId();
        }
        if (plugin.isSimpleClans()) {
            id = ((Clan) base).getTag();
        }
        return id;
    }

    public void disband() {
        if (plugin.isFactions()) {
            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "f disband " + ((Faction) base).getName());
        }
        if (plugin.isSimpleClans()) {
            ((Clan) base).disband();
        }
    }

    public boolean isMember(UUID uuid) {
        if (uuid != null) {
            if (plugin.isFactions()) {
                return MPlayer.get(uuid).getFaction().equals(base);
            }
            if (plugin.isSimpleClans()) {
                return ((Clan) base).isMember(uuid);
            }
        }
        return false;
    }

    public boolean isLeaderOrOfficer(UUID uuid) {
        if (uuid != null) {
            if (plugin.isSimpleClans()) {
                return plugin.getClanManager().getClanPlayer(uuid).isLeader();
            }
            if (plugin.isFactions()) {
                return MPlayer.get(uuid).getRole() == Rel.LEADER || MPlayer.get(uuid).getRole() == Rel.OFFICER;
            }
        }
        return false;
    }

    public String getName() {
        if (plugin.isSimpleClans()) {
            return ((Clan) base).getColorTag();
        }
        if (plugin.isFactions()) {
            return ((Faction) base).getName();
        }
        return null;
    }
}
