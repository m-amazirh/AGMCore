package me.ashenguard.agmcore;

import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import me.ashenguard.agmcore.extension.CoreExtension;
import me.ashenguard.agmcore.extension.ExtensionLoader;
import me.ashenguard.api.messenger.Messenger;
import me.ashenguard.api.messenger.PHManager;
import me.ashenguard.api.spigot.SpigotPlugin;
import me.ashenguard.lib.statistics.Playtime;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public final class AGMCore extends SpigotPlugin {
    private static AGMCore instance;
    public static AGMCore getInstance() {
        return instance;
    }
    public static Messenger getMessenger() {
        return instance.messenger;
    }
    public static Playtime getPlaytimeManager() {
        return instance.playtimeManager;
    }

    @Override
    public @NotNull List<String> getSoftRequirements() {
        return Arrays.asList("PlaceholderAPI", "ProtocolLib");
    }

    @Override
    public int getBStatsID() {
        return 8250;
    }

    @Override
    public int getSpigotID() {
        return 83245;
    }

    private HashMap<String, CoreExtension> extensions = new HashMap<>();
    public HashMap<String, CoreExtension> getExtensions() {
        return new HashMap<>(extensions);
    }
    private Playtime playtimeManager;

    @Override
    public void onPluginEnable() {
        instance = this;

        if (PHManager.enable) new Placeholders();
        ExtensionLoader extensionLoader = new ExtensionLoader();
        extensions = extensionLoader.registerAllExtensions();

        MinecraftVersion.getVersion();
    }

    @Override
    public void onPluginDisable() {
        for (CoreExtension extension: extensions.values()) extension.onDisable();
        AGMEvents.deactivateDayCycleEvent(true);
        messenger.Info("Plugin has been disabled");
    }
}