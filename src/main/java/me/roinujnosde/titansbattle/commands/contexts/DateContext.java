package me.roinujnosde.titansbattle.commands.contexts;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateContext extends AbstractContextResolver<Date> {

    public DateContext(@NotNull TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull Class<Date> getType() {
        return Date.class;
    }

    @Override
    public Date getContext(BukkitCommandExecutionContext context) throws InvalidCommandArgument {
        try {
            return new SimpleDateFormat(getConfigManager().getDateFormat()).parse(context.popFirstArg());
        } catch (ParseException ex) {
            context.getSender().sendMessage(plugin.getLang("invalid-date"));
            throw new InvalidCommandArgument();
        }
    }
}
