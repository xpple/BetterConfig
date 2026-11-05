package dev.xpple.betterconfig.api;

/// Represents a side that a mod config is modifiable from via commands.
public enum ModConfigSide {
    /// Both on the client and the server.
    BOTH(true, true),
    /// Only on the client.
    CLIENT(true, false),
    /// Only on the server.
    SERVER(false, true);

    private final boolean onClient;
    private final boolean onServer;

    ModConfigSide(boolean onClient, boolean onServer) {
        this.onClient = onClient;
        this.onServer = onServer;
    }

    /// {@return whether this mod config should be modifiable via a client-side config command}
    public boolean isOnClient() {
        return onClient;
    }

    /// {@return whether this mod config should be modifiable via a server-side config command}
    public boolean isOnServer() {
        return onServer;
    }
}
