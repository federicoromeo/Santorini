package it.polimi.ingsw.model;

import java.io.Serializable;


/**
 * A TokenColor object has the purpose to identify which player has which tokens
 * Each token and each player has a proper TokenColor and if it is the same
 * it means that that player has the control of that token
 */
public enum TokenColor implements Serializable {

    RED("\033[041m"),
    BLUE("\033[044m"),
    YELLOW("\033[043m");

    private final String escape;


    /**
     * Create a new color with an escape
     * @param escape the sequence code of a color
     */
    TokenColor(String escape) {
        this.escape = escape;
    }

    public String getEscape(){
        return escape;
    }

    @Override
    public String toString(){
        return escape;
    }
}