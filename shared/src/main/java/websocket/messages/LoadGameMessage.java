package websocket.messages;

import java.util.Objects;

import model.GameData;



public class LoadGameMessage extends ServerMessage {
    private GameData gameData;

    public LoadGameMessage(ServerMessageType type, GameData gameData) {
        super(type);
        this.gameData = gameData;
    }

    public GameData getGameData() {
        return this.gameData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoadGameMessage that)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Objects.equals(getGameData(), that.getGameData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getGameData());
    }
}
