package it.polimi.ingsw.model.gameAction.win;

import it.polimi.ingsw.model.Battlefield;
import it.polimi.ingsw.model.Token;


/**
 * This is the context for the win behavior.
 * For more info about a context read the MoveContext's JavaDOC.
 */
public class WinContext {

    private final WinBehavior winStrategy;

    public WinContext(WinBehavior winStrategy) {
        this.winStrategy = winStrategy;
    }

    public boolean executeCheckWin (Token movedToken, Battlefield battlefield){
        return winStrategy.checkWin(movedToken, battlefield);
    }
}
