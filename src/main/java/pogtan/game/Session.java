package pogtan.game;

import pogtan.server.Client;
import pogtan.server.SendPacket;
import pogtan.stage.SessionStage;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class Session {
    private final Channel channel;
    private final int id;
    private final String name;
    private final String pass;
    private final List<Slot> slots;
    private final ConcurrentHashMap<Client, List<User>> clients;

    public Session(Channel channel, int id, String name, String pass) {
        this.channel = channel;
        this.id = id;
        this.name = name;
        this.pass = pass;
        this.slots = List.of(
                new Slot(0),
                new Slot(1),
                new Slot(2),
                new Slot(3),
                new Slot(4),
                new Slot(5),
                new Slot(6),
                new Slot(7)
        );
        this.slots.get(0).setChiefPlayer(true);
        this.clients = new ConcurrentHashMap<>();
    }

    public Channel getChannel() {
        return channel;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPass() {
        return pass;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public List<Slot> getOpenSlots() {
        return slots.stream()
                .filter(slot -> slot.getUser() == null && !slot.isClosed())
                .toList();
    }

    public void broadcastPacket(SendPacket packet) {
        for (Client client : clients.keySet()) {
            client.write(packet);
        }
    }

    public synchronized boolean addClient(Client client, List<User> users) {
        // Fill slots and update existing clients
        if (clients.containsKey(client)) {
            return false;
        }
        final List<Slot> openSlots = getOpenSlots();
        if (openSlots.size() < users.size()) {
            return false;
        }
        for (int i = 0; i < users.size(); i++) {
            final User user = users.get(i);
            final Slot slot = openSlots.get(i);
            slot.setUser(user);
            broadcastPacket(SessionStage.setPlayer(slot.getIndex(), slot.getUser(), slot.getState(), true));
        }
        // Add client
        clients.put(client, users);
        return true;
    }
}
