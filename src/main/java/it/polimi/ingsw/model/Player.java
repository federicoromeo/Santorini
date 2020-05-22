package it.polimi.ingsw.model;

import it.polimi.ingsw.utils.Connection;

import java.io.Serializable;


/**
 *  A Player is an object that has an unique view.
 *  It is an observable for the controller and
 *  it is an observer of the controller.
 *  It interact with the network
 *  and notify the observer of the controller to let the game run.
 */
public class Player implements Serializable {

    private final String username;
    private final TokenColor tokenColor;
    private Token token1, token2;
    private GodCard myGodCard;

    /**
     * A new player is created when it connect to the server
     *
     * @param username    the name of the player. It should be unique
     * @param color       the color that identify the player and his tokens
     */
    public Player(String username, TokenColor color) {
        this.username = username;
        this.tokenColor = color;
        this.token1 = new Token(color);
        this.token2 = new Token(color);

        if (this.tokenColor.equals(TokenColor.RED)){
            this.token1.setId(1);
            this.token2.setId(11);
        }
        if (this.tokenColor.equals(TokenColor.BLUE)){
            this.token1.setId(2);
            this.token2.setId(22);
        }
        if (this.tokenColor.equals(TokenColor.YELLOW)){
            this.token1.setId(3);
            this.token2.setId(33);
        }
    }

    /*  GETTER  */
    public Token getToken1() {
        return this.token1;
    }

    public Token getToken2() {
        return this.token2;
    }

    public String getUsername() {
        return this.username;
    }

    public GodCard getMyGodCard() {
        return this.myGodCard;
    }


    /*  SETTER  */

    public void setToken1(Token token1) {
        this.token1 = token1;
    }

    public void setToken2(Token token2) {
        this.token2 = token2;
    }

    public void setMyGodCard(GodCard godCard){
        this.myGodCard = godCard;
    }


    /**
     * Used to return a player color to let
     * game know if it is the player's turn
     * @return the color of the player
     */
    public TokenColor getTokenColor() {
        return this.tokenColor;
    }
}