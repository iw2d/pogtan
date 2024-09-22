package pogtan.game;

public final class Slot {
    private final int index;
    private byte[] state;
    private User user;

    public Slot(int index) {
        this.index = index;
        this.state = new byte[3];
        this.user = null;
    }

    public int getIndex() {
        return index;
    }

    public byte[] getState() {
        return state;
    }

    public void setState(byte[] state) {
        this.state = state;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isClosed() {
        return (state[0] & 0x1) != 0;
    }

    public boolean isChiefPlayer() {
        return (state[0] & 0x2) != 0;
    }

    public void setChiefPlayer(boolean set) {
        if (set) {
            state[0] |= 0x2;
        } else {
            state[0] ^= 0x2;
        }
    }

    public boolean isReady() {
        return (state[0] & 0x8) != 0;
    }

    public void setReady(boolean set) {
        if (set) {
            state[0] |= 0x8;
        } else {
            state[0] ^= 0x8;
        }
    }

    public int getBomber() {
        return state[1];
    }

    public void setBomber(int bomber) {
        state[1] = (byte) bomber;
    }

    public int getTeam() {
        return state[2] & 0x1F;
    }
}
