package com.haroldstudios.mailme.mail;

import com.haroldstudios.mailme.MailMe;
import com.haroldstudios.mailme.database.PlayerMailDAO;
import com.haroldstudios.mailme.utils.ConfigValue;
import com.haroldstudios.mailme.utils.PlayerUtils;
import com.haroldstudios.mailme.utils.Utils;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Mail {

    // Mail class fields
    private final UUID uuid;
    private final long dateCreated;
    private final long expiryTimeMins;
    private final String sender;
    private final ItemStack icon;
    private final String identifier;
    // Per Player Fields
    private transient boolean read;
    private transient Integer colId;
    private transient long dateReceived;

    /**
     * Main class constructor
     *
     * @param icon ItemStack to show in mailbox
     * @param sender String of the sender e.g "Harry", "Server" etc
     * @param expiryTimeMins Minutes until the mail should expire and delete itself from existence!
     */
    protected Mail(ItemStack icon, String sender, int expiryTimeMins, String identifier) {
        this.uuid = UUID.randomUUID();
        this.identifier = identifier;
        this.icon = icon;
        this.sender = sender;
        this.expiryTimeMins = expiryTimeMins;
        this.dateCreated = System.currentTimeMillis();
    }

    public long getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(long dateReceived) {
        this.dateReceived = dateReceived;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isRead() {
        return read;
    }

    public Integer getColId() {
        return colId;
    }

    public void setColId(int colId) {
        this.colId = colId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getSender() {
        return sender;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void sendPreset(UUID uuid) {
        PlayerMailDAO dao = MailMe.getInstance().getPlayerMailDAO();

        dao.savePlayerMail(uuid, this).thenAccept(success -> {
            PlayerUtils.notifyUnread(uuid);
        });
    }

    public void send(List<UUID> recipients) {
        PlayerMailDAO dao = MailMe.getInstance().getPlayerMailDAO();

        dao.saveMailObj(this);

        Bukkit.getScheduler().runTaskLater(MailMe.getInstance(), () -> {
            for (UUID recipient : recipients) {
                dao.savePlayerMail(recipient, this).thenAccept(success -> {
                    PlayerUtils.notifyUnread(recipient);
                });
            }
        }, (20L * 60) * ConfigValue.SEND_DELAY);
    }

    public long getExpiryTimeMilliSeconds() { return expiryTimeMins * 60 * 1000; }

    /**
     * Gets the mail in its text form
     * Send using player.spigot().sendMessage();
     *
     * @return BaseComponent
     */
    public BaseComponent[] getMailAsText(Player player) {
        String[] msgs = MailMe.getInstance().getLocale().getMessages(player, "text.format");
        ComponentBuilder builder = new ComponentBuilder("");

        builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/mailme reply " + sender));
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(MailMe.getInstance().getLocale().getMessage("text.hover")).create()));

        for (String each : msgs) {
            String[] str = Utils.applyPlaceholders(each, this, player);
            for (String s : str) {
                builder.append(s + "\n");
            }
        }

        return builder.create();
    }

    public abstract void onMailClick(Player whoClicked);
    public abstract BaseComponent[] getContentsAsText();
    public abstract String[] getContentsAsString();

    // Hierarchical Builder Pattern
    public abstract static class Builder<T extends Builder<T>>{

        protected String identifier;
        protected String sender;
        protected ItemStack icon;
        protected int expiryTimeMins = ConfigValue.EXPIRY_TIME_MINS;
        private final List<UUID> recipients = new ArrayList<>();

        public T setSender(String sender) {
            this.sender = sender;
            return self();
        }

        public T setIdentifier(String identifier) {
            this.identifier = identifier;
            return self();
        }

        public T setIcon(ItemStack icon) {
            this.icon = icon;
            return self();
        }

        public T addRecipient(UUID uuid) {
            this.recipients.add(uuid);
            return self();
        }

        public T removeRecipient(UUID uuid) {
            this.recipients.remove(uuid);
            return self();
        }

        public List<UUID> getRecipients() {
            return recipients;
        }

        public boolean isRecipient(UUID uuid) {
            return this.recipients.contains(uuid);
        }

        public T setExpiryTimeMins(int expiry) {
            this.expiryTimeMins = expiry;
            return self();
        }

        public long getDateCreated() {
            return System.currentTimeMillis();
        }

        public ItemStack getIcon() {
            if (icon != null)
                return icon.clone();
            else return null;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getSender() {
            return sender;
        }

        public int getExpiryTimeMins() {
            return expiryTimeMins;
        }

        public Mail.Builder<?> combine(Mail.Builder<?> combineTo) {
            if (combineTo == null) return this;
            this.getRecipients().addAll(combineTo.getRecipients());
            this.setIcon(combineTo.getIcon());
            this.setSender(combineTo.getSender());
            this.setExpiryTimeMins(combineTo.getExpiryTimeMins());
            return this;
        }

        public abstract String[] getContents();

        // Subclasses must override and return 'this' (themselves)
        protected abstract T self();

        public abstract Mail build();
    }

}
