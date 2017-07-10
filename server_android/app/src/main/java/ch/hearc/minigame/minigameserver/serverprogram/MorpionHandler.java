package ch.hearc.minigame.minigameserver.serverprogram;

/**
 * Created by jonathan.guerne on 21.06.2017.
 */

public class MorpionHandler {

    private static MorpionHandler instance;

    private MorpionHandler() {

    }

    public static MorpionHandler getInstance() {
        if (instance == null) {

            instance = new MorpionHandler();
        }

        return instance;
    }

    /**
     * check if the game is over at the actual stage
     *
     * @param gameBoard char array for the gameBoard
     * @return true if the game is over false otherwise
     */
    public boolean isGameOver(char[] gameBoard, GameChar winner) {

        for (int i = 0; i <= 6; i += 3) {
            if (gameBoard[i] != '\0' && (gameBoard[i] == gameBoard[i + 1] && gameBoard[i + 1] == gameBoard[i + 2])) {
                winner.setInfoChar(gameBoard[i]);
                return true;
            }
        }

        for (int i = 0; i < 3; i++) {
            if (gameBoard[i] != '\0' && (gameBoard[i] == gameBoard[i + 3] && gameBoard[i + 3] == gameBoard[i + 6])) {
                winner.setInfoChar(gameBoard[i]);
                return true;
            }
        }

        if (gameBoard[0] != '\0' && (gameBoard[0] == gameBoard[4] && gameBoard[4] == gameBoard[8])) {
            winner.setInfoChar(gameBoard[0]);
            return true;
        }

        if (gameBoard[2] != '\0' && (gameBoard[2] == gameBoard[4] && gameBoard[4] == gameBoard[6])) {
            winner.setInfoChar(gameBoard[2]);
            return true;
        }

        boolean full =true;
        for(int i=0 ;i <gameBoard.length;i++){
            if(gameBoard[i] == '\0'){
                full = false;
                break;
            }
        }

        if(full){
            winner.setInfoChar('e');//winner equals e like 'egalite'
            return true;
        }

        return false;
    }
}
