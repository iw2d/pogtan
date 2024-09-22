package pogtan.game;

import pogtan.server.Client;
import pogtan.server.SendPacket;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UserStorage {
    private final Map<Client, List<User>> connectedUsers = new ConcurrentHashMap<>();

    public void broadcastPacket(SendPacket packet) {
        for (Client client : connectedUsers.keySet()) {
            client.write(packet);
        }
    }

    public void add(Client client, List<User> users) {
        connectedUsers.put(client, users);
    }

    public void remove(Client client) {
        connectedUsers.remove(client);
    }

    public int size() {
        return connectedUsers.size();
    }
}
