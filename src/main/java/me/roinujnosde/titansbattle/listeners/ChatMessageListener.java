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

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import me.roinujnosde.titansbattle.Helper;
import me.roinujnosde.titansbattle.TitansBattle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 *
 * @author RoinujNosde
 */
public class ChatMessageListener implements Listener {

    private final Helper helper;

    public ChatMessageListener() {
        helper = TitansBattle.getHelper();
    }

    @EventHandler
    public void onChat(ChatMessageEvent event) {
        if (helper.getKillerPrefix(event.getSender()) != null) {
            if (event.getTags().contains(helper.getKillerPrefixPlaceholder(event.getSender()))) {
                event.setTagValue(helper.getKillerPrefixPlaceholder(event.getSender()), helper.getKillerPrefix(event.getSender()));
            }
        }
        if (helper.getWinnerPrefix(event.getSender()) != null) {
            if (event.getTags().contains(helper.getWinnerPrefixPlaceholder(event.getSender()))) {
                event.setTagValue(helper.getWinnerPrefixPlaceholder(event.getSender()), helper.getWinnerPrefix(event.getSender()));
            }
        }
    }
}
