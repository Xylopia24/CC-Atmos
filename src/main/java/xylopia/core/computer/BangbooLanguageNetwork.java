package xylopia.core.computer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks which Bangboo computers are listening for chat and routes messages to them. */
public final class BangbooLanguageNetwork {
    private static final Set<Integer> listeners = ConcurrentHashMap.newKeySet();

    private BangbooLanguageNetwork() {}

    public static void startListening(int computerID) { listeners.add(computerID); }
    public static void stopListening(int computerID)  { listeners.remove(computerID); }
    public static boolean isListening(int computerID) { return listeners.contains(computerID); }

    /** Called by the NeoForge ServerChatEvent handler to broadcast to all listening Bangboos. */
    public static void onChatMessage(String playerName, String message) {
        for (int id : listeners) {
            var bangboo = BangbooComputerRegistry.get(id);
            if (bangboo == null || bangboo.getServerComputer() == null) continue;
            bangboo.getServerComputer().queueEvent("chat_message", new Object[]{playerName, message});
        }
    }
}
