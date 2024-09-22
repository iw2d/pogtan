package pogtan.header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum ReceiveHeader implements PacketHeader {
    LOGIN_REQUEST(0),
    CHANNELS_REQUEST(1),
    CONFIRM_ID_REQUEST(2),
    NEW_USER_REQUEST(3),
    CONNECT_TO_SERVER_REQUEST(4),
    CONNECT_TO_CHANNEL_REQUEST(5),
    CRASH_INFO(6),

    DISCONNECT_FROM_CHANNEL_REQUEST(8),
    ESTIMATE_RTTP_REQUEST(9),
    CHAT_MESSAGE(10),
    COMMAND_MESSAGE(11),
    WHISPER_MESSAGE(12),
    USER_SIMPLE_INFO_REQUEST(13),
    MY_INFO_REQUEST(14),
    MODIFY_USER_INFO_REQUEST(15),
    USER_DETAIL_INFO_REQUEST(16),
    JOIN_SESSION_REQUEST(17),
    USER_LOCATION_REQUEST(18),
    GUILD_INFO_REQUEST(19),
    INVITE_USER_REQUEST(20),
    CREATE_SESSION(21),
    JOIN_SESSION(22),
    AUTO_JOIN_SESSION(23),
    LEAVE_SESSION(24),
    CREATE_SESSION_GUILD(25),

    READY_STATE_REQUEST(27),
    SET_SESSION_MAP_REQUEST(28),
    SET_SLOT_STATE_REQUEST(29),
    BAN_PLAYER_REQUEST(30),
    ASK_BAN_PLAYER_ANSWER(31),
    START_GAME_REQUEST(32),
    CHANGE_SESSION_NAME(33),
    BOMB_IGNITE(34),
    BOMB_KICK_THROW(35),

    THROW_DART(36),

    MOVABLE_BOX_MOVE(38),
    ITEM_EAT(39),
    OBSTACLE_EAT(40),
    SET_BOMBER_EVENT(41),
    THROW_DART_EFFECT(42),
    DROP_OBSTACLE(43),
    PROLONG_LIFE(44),
    GAME_STAGE_CHECK_IN(45),
    SET_GAME_TYPE_NORMAL(46),
    SET_GAME_TYPE_MOD(47),

    GUILD_MEMBER_SIMPLE_INFO_REQUEST(50),
    UNK_51(51), // CDataI16Dyn::SetLoad
    ONLINE_POLL(52),
    UNK_53(53), // CImageLib::ReportRevisionToGameServer

    NO(54);

    private static final List<ReceiveHeader> headers;

    static {
        final List<ReceiveHeader> headerList = new ArrayList<>(Collections.nCopies(NO.getValue() + 1, null));
        for (ReceiveHeader header : values()) {
            headerList.set(header.getValue(), header);
        }
        headers = Collections.unmodifiableList(headerList);
    }

    private final byte value;

    ReceiveHeader(int value) {
        this.value = (byte) value;
    }

    @Override
    public byte getValue() {
        return value;
    }

    public static ReceiveHeader getByValue(byte value) {
        if (value >= 0 && value < NO.getValue()) {
            return headers.get(value);
        }
        return null;
    }
}
