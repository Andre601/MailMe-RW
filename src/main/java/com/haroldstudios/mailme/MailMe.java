package com.haroldstudios.mailme;

import com.haroldstudios.mailme.commands.MailCommands;
import com.haroldstudios.mailme.commands.MailboxCommands;
import com.haroldstudios.mailme.commands.PostOfficeCommands;
import com.haroldstudios.mailme.commands.PresetCommands;
import com.haroldstudios.mailme.components.hooks.HologramHook;
import com.haroldstudios.mailme.components.hooks.HolographicDisplaysHook;
import com.haroldstudios.mailme.components.hooks.VaultHook;
import com.haroldstudios.mailme.database.DatabaseConnector;
import com.haroldstudios.mailme.database.DatabaseSettingsImpl;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.database.json.DataCache;
import com.haroldstudios.mailme.database.json.JsonDatabase;
import com.haroldstudios.mailme.database.sql.MySQLDatabase;
import com.haroldstudios.mailme.listeners.EntityEvents;
import com.haroldstudios.mailme.mail.MailboxTaskManager;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.GuiConfig;
import com.haroldstudios.mailme.utils.Locale;
import me.mattstudios.mf.base.CommandManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.logging.Level;

public final class MailMe extends JavaPlugin {

    private VaultHook vaultHook;
    private HologramHook hologramHook;

    private MailboxTaskManager mailboxTaskManager;
    @Nullable private DatabaseConnector connector;
    private PlayerMailDAO playerMailDAO;
    private DataCache dataCache;
    private Locale locale;
    private GuiConfig guiConfig;

    private EntityEvents listener;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        ConfigValue.load(this);
        initDatabaseConnection();
        if (getServer().getPluginManager().getPlugin("Vault") != null && ConfigValue.HOOK_VAULT_ENABLED) {
            this.vaultHook = new VaultHook(this);
        }

        if (getServer().getPluginManager().getPlugin("HolographicDisplays") != null && ConfigValue.HOOK_HOLOGRAMS_ENABLED) {
            this.hologramHook = new HolographicDisplaysHook(this);
        }
        this.locale = new Locale(this);
        this.dataCache = new DataCache();
        this.guiConfig = new GuiConfig(this);

        CommandManager commandManager = new CommandManager(this);
        commandManager.getCompletionHandler().register("#locale", val -> new ArrayList<>(locale.getLanguageTokens()));

        commandManager.register(new MailCommands(this));
        commandManager.register(new MailboxCommands(this));
        commandManager.register(new PostOfficeCommands(this));
        commandManager.register(new PresetCommands(this));

        commandManager.hideTabComplete(true);
        listener = new EntityEvents(this);
        getServer().getPluginManager().registerEvents(listener, this);

        this.mailboxTaskManager = new MailboxTaskManager(this);
        this.mailboxTaskManager.beginTasks();


        if (getConfig().getInt("config-ver") <= 2) {
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "WARNING!");
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "MailMe has detected being upgraded! Before you continue, please read the following note;");
            getServer().getConsoleSender().sendMessage(ChatColor.RED + "This version of MailMe introduces a lot of new things! Including the data being stored differently." +
                    " You should run /mailme convert legacy2latest to update your data. However, please note that your preset mail will not be converted.");
            getServer().getConsoleSender().sendMessage("§a§lTo stop this message, set config-ver to 3 in config.yml");
            getServer().getConsoleSender().sendMessage(ChatColor.DARK_RED + "WARNING!");
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(listener);
        if (connector != null)
            connector.disconnect();
        if (mailboxTaskManager != null)
            mailboxTaskManager.stopTasks();
    }

    // Creates a connection to sql database
    private void initDatabaseConnection() {
        ConfigurationSection databaseConfig = getConfig().getConfigurationSection("database");
        if (databaseConfig == null) {
            getLogger().severe("Database connection was not made. Key: database was missing in config.yml");
            return;
        }

        if (databaseConfig.getString("type").equalsIgnoreCase("MYSQL")) {
            connector = new MySQLDatabase(new DatabaseSettingsImpl(
                    databaseConfig.getString("host"),
                    databaseConfig.getInt("port"),
                    databaseConfig.getString("database_name"),
                    databaseConfig.getString("username"),
                    databaseConfig.getString("password"),
                    databaseConfig.getBoolean("useSSL"),
                    databaseConfig.getString("driver"),
                    databaseConfig.getString("additional_url")));
            playerMailDAO = (PlayerMailDAO) connector;
            connector.connect();
        } else {
            connector = null;
            playerMailDAO = new JsonDatabase(this);
        }
    }

    public static void debug(Class<?> clazz, String msg) {
        if (!ConfigValue.DEBUG) return;
        Bukkit.getLogger().log(Level.INFO, String.format("%s: Class: %s", msg, clazz.getName()));
    }

    public static void debug(Exception exception) {
        if (!ConfigValue.DEBUG) return;
        exception.printStackTrace();
    }

    public static void debug(Throwable throwable) {
        if (!ConfigValue.DEBUG) return;
        throwable.printStackTrace();
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public HologramHook getHologramHook() {
        return hologramHook;
    }

    public Locale getLocale() {
        return locale;
    }

    public DataCache getCache() {
        return dataCache;
    }

    public GuiConfig getGuiConfig() {
        return guiConfig;
    }

    public PlayerMailDAO getPlayerMailDAO() {
        return playerMailDAO;
    }

    public static MailMe getInstance() {
        return getPlugin(MailMe.class);
    }
}