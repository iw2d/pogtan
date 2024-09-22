package pogtan.header;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum SendHeader implements PacketHeader {
    CONNECT(0),
    LOGIN_RESULT(1),
    CONFIRM_ID_RESULT(2),
    NEW_USER_RESULT(3),
    CONNECT_TO_SERVER_RESULT(4),
    CHANNELS_INFO(5),
    CHANNELS_STATE(6),
    ENTER_CHANNEL(7),
    SERVER_RESERVED_MESSAGE(8),
    ESTIMATE_RTTP_RESULT(9),
    MY_INFO_RESULT(10),
    MODIFY_USER_INFO_RESULT(11),
    USER_SIMPLE_INFO(12),
    USER_DETAIL_INFO(13),
    USER_LOCATION(14),
    GUILD_INFO(16),
    INVITE_USER(17),
    CREATE_SESSION_RESULT(18),
    JOIN_SESSION_RESULT(19),
    AUTO_JOIN_SESSION_RESULT(20),
    SESSION_SIMPLE_INFO(21),
    SESSION_DETAIL_INFO(22),
    SESSION_MODIFIED(23),
    CREATE_WAITING_SESSION_LIST(24),
    SESSION_CLOSED(25),
    SESSION_GUILD_INFO(26),
    SET_GAME_TYPE_MOD(29),
    SET_GAME_TYPE_NORMAL(30),
    CHAT_MESSAGE(31),
    ADMIN_NOTIFY_MESSAGE(32),
    WHISPER_MESSAGE(33),
    WHISPER_MESSAGE_RET(34),
    SET_PLAYER(35),
    RESET_PLAYER(36),
    SET_SLOT_STATE(37),
    SET_SESSION_MAP(38),
    ASK_BAN_PLAYER(39),
    BAN_PLAYER_RESULT(40),
    START_GAME_RESULT(41),
    LAUNCH_GAME_STAGE(42),
    START_GAME(43),
    GAME_STAGE_CHECK_IN(44),
    SET_BOMB_STATE(45),
    BOMB_IGNITE(46),
    BOMB_KICK_THROW(47),
    MOVABLE_BOX_MOVE(48),
    ITEM_EAT_RESULT(49),
    OBSTACLE_EAT_RESULT(50),
    FINAL_MAP_SHORTEN(51),
    SET_BOMBER_EVENT(52),
    SET_EFFECT(53),
    DROP_OBSTACLE(54),
    PROLONG_LIFE(55),
    GAME_RESULT(56),
    RETURN_PREPARE_STAGE(57),
    BANNED_BY_UDP_RECV_SEND_FAIL(58),
    CHANGE_TITLE_RESULT(59),
    GUILD_MEMBER_SIMPLE_INFO(61),
    GUILD_NOT_FOUND(62),
    GUILD_MARK(63),
    NO(64);

    private static final List<SendHeader> headers;

    static {
        final List<SendHeader> headerList = new ArrayList<>(Collections.nCopies(NO.getValue() + 1, null));
        for (SendHeader header : values()) {
            headerList.set(header.getValue(), header);
        }
        headers = Collections.unmodifiableList(headerList);
    }

    private final byte value;

    SendHeader(int value) {
        this.value = (byte) value;
    }

    @Override
    public byte getValue() {
        return value;
    }

    public static SendHeader getByValue(byte value) {
        if (value >= 0 && value < NO.getValue()) {
            return headers.get(value);
        }
        return null;
    }
}
