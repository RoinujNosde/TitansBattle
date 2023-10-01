package me.roinujnosde.titansbattle.managers;

import co.aikar.commands.BukkitLocales;
import co.aikar.commands.Locales;
import co.aikar.commands.PaperCommandManager;
import me.roinujnosde.titansbattle.TitansBattle;
import me.roinujnosde.titansbattle.commands.ChallengeCommand;
import me.roinujnosde.titansbattle.commands.ConfigCommands;
import me.roinujnosde.titansbattle.commands.TBCommands;
import me.roinujnosde.titansbattle.commands.completions.*;
import me.roinujnosde.titansbattle.commands.conditions.*;
import me.roinujnosde.titansbattle.commands.contexts.*;
import me.roinujnosde.titansbattle.dao.ConfigurationDao;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Locale;
import java.util.Set;

public class CommandManager extends PaperCommandManager {

    public CommandManager(TitansBattle plugin) {
        super(plugin);
        enableUnstableAPI("help");
        setDefaultLocale();
        registerDependencies();
        registerCompletions();
        registerContexts();
        registerReplacements();
        registerConditions();
        registerCommands();
    }

    @Override
    public TitansBattle getPlugin() {
        return (TitansBattle) plugin;
    }

    private void setDefaultLocale() {
        BukkitLocales locales = getLocales();
        Locale defaultLocale = new Locale(getPlugin().getConfigManager().getLanguage().split("_")[0]);
        locales.setDefaultLocale(defaultLocale);

        LanguageManager languageManager = getPlugin().getLanguageManager();
        locales.loadLanguage(languageManager.getEnglishLanguageFile(), Locales.ENGLISH);
        locales.loadLanguage(languageManager.getConfig(), defaultLocale);
    }

    private void registerDependencies() {
        registerDependency(GameManager.class, getPlugin().getGameManager());
        registerDependency(ConfigurationDao.class, getPlugin().getConfigurationDao());
        registerDependency(ConfigManager.class, getPlugin().getConfigManager());
        registerDependency(DatabaseManager.class, getPlugin().getDatabaseManager());
        registerDependency(ChallengeManager.class, getPlugin().getChallengeManager());
        registerDependency(TaskManager.class, getPlugin().getTaskManager());
    }

    private void registerCompletions() {
        registerCompletion(new PagesCompletion(getPlugin()));
        registerCompletion(new GroupsCompletion(getPlugin()));
        registerCompletion(new RequestsCompletion(getPlugin()));
        registerCompletion(new ConfigFieldsCompletion(getPlugin()));
        registerCompletion(new OrderByCompletion(getPlugin()));
        registerCompletion(new GamesCompletion(getPlugin()));
        registerCompletion(new ArenasCompletion(getPlugin()));
        registerCompletion(new WinnersDatesCompletion(getPlugin()));
        registerCompletion(new DestinationCompletion(getPlugin()));
        registerCompletion(new PrizeCompletion(getPlugin()));
    }

    private void registerContexts() {
        registerContext(new GameContext(getPlugin()));
        registerContext(new DateContext(getPlugin()));
        registerContext(new GroupContext(getPlugin()));
        registerContext(new WarriorContext(getPlugin()));
        registerContext(new ChallengeRequestContext(getPlugin()));
        registerContext(new ArenaConfigurationContext(getPlugin()));
        registerContext(new GameConfigurationContext(getPlugin()));
        registerContext(new OnlinePlayerContext(getPlugin()));
    }

    private void registerReplacements() {
        ConfigurationSection commandsSection = getPlugin().getConfig().getConfigurationSection("commands");
        if (commandsSection == null) {
            return;
        }
        Set<String> commands = commandsSection.getKeys(false);
        for (String command : commands) {
            getCommandReplacements().addReplacement(command, commandsSection.getString(command) + "|" + command);
        }
    }

    private void registerConditions() {
        registerCondition(new PlayerParticipantCondition(getPlugin()));
        registerCondition(new ParticipantCondition(getPlugin()));
        registerCondition(new HappeningCondition(getPlugin()));
        registerCondition(new EmptyInventoryCondition(getPlugin()));
        registerCondition(new CanChallengeCondition(getPlugin()));
        registerCondition(new ReadyCondition(getPlugin()));
        registerCondition(new InvitedCondition(getPlugin()));
        registerCondition(new OtherPlayerCondition(getPlugin()));
        registerCondition(new OtherGroupCondition(getPlugin()));
    }

    private void registerCommands() {
        registerCommand(new TBCommands());
        registerCommand(new ChallengeCommand());
        registerCommand(new ConfigCommands());
    }

    private <T> void registerContext(AbstractIssuerOnlyContextResolver<T> resolver) {
        getCommandContexts().registerIssuerOnlyContext(resolver.getType(), resolver);
    }

    private <T> void registerContext(AbstractContextResolver<T> resolver) {
        getCommandContexts().registerContext(resolver.getType(), resolver);
    }

    private void registerCompletion(AbstractCompletion completion) {
        getCommandCompletions().registerCompletion(completion.getId(), completion);
    }

    private void registerCompletion(AbstractAsyncCompletion completion) {
        getCommandCompletions().registerAsyncCompletion(completion.getId(), completion);
    }

    private void registerCondition(AbstractCommandCondition condition) {
        getCommandConditions().addCondition(condition.getId(), condition);
    }

    private <T> void registerCondition(AbstractParameterCondition<T> condition) {
        getCommandConditions().addCondition(condition.getType(), condition.getId(), condition);
    }

}
