package com.haroldstudios.mailme.database;

import com.haroldstudios.mailme.MailMe;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerSettings {

    private final UUID uuid;
    private boolean receivingNotifications;
    private List<Location> mailboxLocations;
    @Nullable private String languageToken;

    public PlayerSettings(final UUID uuid) {
        this.uuid = uuid;
        this.receivingNotifications = true;
        this.mailboxLocations = new ArrayList<>();
    }

    /**
     * Retrieves this player's mailbox locations on the server
     * Does NOT include post office locations
     *
     * @return Mailbox locations
     */
    public List<Location> getMailboxLocations() {
        return mailboxLocations;
    }

    /**
     * @return If player setting is set to receive notifications
     */
    public boolean isReceivingNotifications() {
        return receivingNotifications;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void removeMailboxLocation(Location location) {
        this.mailboxLocations.remove(location);
        save();
    }

    public void addMailboxLocation(Location location) {
        this.mailboxLocations.remove(location);
        save();
    }

    public void setReceivingNotifications(boolean receivingNotifications) {
        this.receivingNotifications = receivingNotifications;
        save();
    }

    public String getLanguageToken() {
        return languageToken;
    }

    public void setLanguageToken(String languageToken) {
        this.languageToken = languageToken;
    }

    /**
     * Saves player settings to file
     */
    public void save() {
        Bukkit.getScheduler().runTaskAsynchronously(MailMe.getInstance(), () ->
                MailMe.getInstance().getCache().getFileUtil().save(this, new File(MailMe.getInstance().getDataFolder() + "/playersettings/" + uuid.toString() + ".json")));

    }


}