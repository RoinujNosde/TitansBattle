package me.roinujnosde.titansbattle.commands.completions;

import co.aikar.commands.BukkitCommandCompletionContext;
import co.aikar.commands.InvalidCommandArgument;
import me.roinujnosde.titansbattle.TitansBattle;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class PagesCompletion extends AbstractCompletion {

    public PagesCompletion(TitansBattle plugin) {
        super(plugin);
    }

    @Override
    public @NotNull String getId() {
        return "pages";
    }

    @Override
    public Collection<String> getCompletions(BukkitCommandCompletionContext context) throws InvalidCommandArgument {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < getPages(context); i++) {
            list.add(String.valueOf(i + 1));
        }
        return list;
    }

    private int getPages(BukkitCommandCompletionContext context) {
        String type = context.getConfig("type");
        if (type == null) {
            return 0;
        }
        int size;
        switch (type) {
            case "warrior":
                size = getDatabaseManager().getWarriors().size();
                break;
            case "group":
                size = getDatabaseManager().getGroups().size();
                break;
            default:
                size = 0;
        }
        return size / getConfigManager().getPageLimitRanking();
    }
}
