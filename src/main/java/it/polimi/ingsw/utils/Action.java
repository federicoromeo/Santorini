package it.polimi.ingsw.utils;


/**
 * This enum is an element that each PlayerAction and ServerResponse must have.
 * It specify what kind of action has ben made or what kind or information the server needs in
 * order to let the game routine run properly.
 */
public enum Action {

    /*
    FROM CLIENT TO SERVER
     */
    SELECT_TOKEN("A player has selected a token"),
    MOVE_TOKEN("A player has moved a token"),
    BUILD("A player has made a build. Switch turn"),

    /*
    FROM SERVER TO CLIENT
     */

    NOT_YOUR_TURN("Wait your turn to perform your action!"),

    START_NEW_TURN("Your turn is ended!"),
    ASK_FOR_MOVE("Where do you want to move your token? (x,y)"),
    ASK_FOR_BUILD("Where do you want to build? (x,y)"),

    GAME_OVER("GAME OVER"),
    TOKEN_NOT_MOVABLE("You can not move this token, please select a movable one!"),
    PLAYER_LOST("You have lost the game, your token will be wiped!");

    private String info;

    Action(String info){ this.info = info;}

    public void print (Action message) {
        System.out.println(message);
    }

    public String getInfo(){
        return info;
    }

    @Override
    public String toString(){
        return info;
    }
}