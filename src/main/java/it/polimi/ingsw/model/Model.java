package it.polimi.ingsw.model;

import it.polimi.ingsw.controller.*;
import it.polimi.ingsw.gameAction.build.*;
import it.polimi.ingsw.gameAction.move.*;
import it.polimi.ingsw.gameAction.win.PanWin;
import it.polimi.ingsw.gameAction.win.SimpleWin;
import it.polimi.ingsw.gameAction.win.WinContext;
import it.polimi.ingsw.gameAction.move.ApolloMoves;
import it.polimi.ingsw.gameAction.move.ArtemisMoves;
import it.polimi.ingsw.gameAction.move.MoveContext;
import it.polimi.ingsw.gameAction.move.SimpleMoves;
import it.polimi.ingsw.gameAction.win.*;
import it.polimi.ingsw.utils.Action;
import it.polimi.ingsw.utils.Observable;
import it.polimi.ingsw.utils.PlayerAction;
import it.polimi.ingsw.utils.ServerResponse;

import java.io.IOException;
import java.util.*;


/**
 * The model contains a battlefield and the info to let the game run properly.
 * It has also all the methods to change itself and to check if a move is valid or not.
 */
public class Model extends Observable<ServerResponse> implements Cloneable {

    private Battlefield battlefield;
    private TokenColor turn;
    private List<Player> allPlayers;

    public Model(Battlefield battlefield) {
        this.battlefield = battlefield;
    }

    /*
    GETTER
    */

    public List<Player> getAllPlayers() {
        return allPlayers;
    }

        public Battlefield getBattlefieldCopy() {
        return battlefield.getCopy();
    }

    public Battlefield getBattlefield() {
        return this.battlefield;
    }

    public TokenColor getTurn() {
        return turn;
    }


    /**
     * compare the current turn with a player's color.
     * if they match then it is that player's turn
     *
     * @param player the player to ask for color
     * @return true if it is it's turn, false otherwise.
     */
    public boolean isPlayerTurn(Player player) {////////////dubbia
        return turn == player.getTokenColor();
    }


    /**
     * It returns a partial copy of the model.
     * It contains the battlefield ready to be printed, the turn and the players
     *
     * @return modelCopy
     */
    public ModelUtils getCopy() {
        Battlefield battlefieldCopy = battlefield.getCopy();
        ModelUtils modelCopy = new ModelUtils(battlefieldCopy);  //added players and turn
        modelCopy.setAllPlayers((this.getAllPlayers()));
        modelCopy.setTurn(this.getTurn());
        return modelCopy;
    }


    /**
     * It calculates the valid moves that a player can make.
     * A token CAN move one Cell around himself.
     * A token can NOT move out of the battlefield,
     * where there is another token (himself too),
     * on a build height more than 1 of it's own,
     * where there is a dome.
     *
     * This take all the values needed for call the properly method based on the god card and
     * and call the methods passing it all the parameters.
     *
     * @param playerAction the message from the observer that contain all the information.
     */
    public void validMoves(PlayerAction playerAction) throws CellOutOfBattlefieldException, WrongNumberPlayerException, ImpossibleTurnException, CellHeightException, IOException, ReachHeightLimitException {

        int selectedTokenId = playerAction.getTokenMain();
        int otherTokenId = playerAction.getTokenOther();

        Token selectedToken = parseToken(selectedTokenId);
        if (selectedToken == null || !selectedToken.getTokenColor().equals(turn)) {
            this.notifyWrongInput(playerAction);
            return;
        }
        Token otherToken = parseToken(otherTokenId);

        Player playerActive = playerAction.getPlayer();

        List<Player> opponents = getOpponents(playerActive);
        List<Token> enemyTokens = getTokens(opponents);

        GodCard myGodCard = playerActive.getMyGodCard();
        List<GodCard> enemyGodCards = getGodCards(opponents);

        List<Cell> validMoves = new ArrayList<>();

        validMoves = computeValidMoves(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, getBattlefield());

        ServerResponse serverResponse;

        if (validMoves == null) {
            serverResponse = checkLoseForMove(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, getBattlefield(), opponents, playerActive);
        } else {
            serverResponse = new ServerResponse(Action.ASK_FOR_MOVE, this.getCopy(), validMoves, null, null);
        }
        notify(serverResponse);
    }


    /**
     * @param playerActive a player in the game.
     * @return all the opponent of that player.
     */
    public List<Player> getOpponents(Player playerActive) {

        List<Player> opponents = null;
        for (Player p: allPlayers) {
            if (!playerActive.getUsername().equals(p.getUsername())) {
                opponents.add(p);
            }
        }
        return opponents;
    }


    /**
     * @param players a list of players.
     * @return all the tokens of those players.
     */
    public List<Token> getTokens(List<Player> players) {
        List<Token> tokens = null;
        for (Player p: players) {
            tokens.add(p.getToken1());
            tokens.add(p.getToken2());
        }
        return tokens;
    }


    /**
     * @param players a list of players.
     * @return a list of all those players god cards.
     */
    public List<GodCard> getGodCards(List<Player> players) {
        List<GodCard> godCards = null;
        for (Player p: players) {
            godCards.add(p.getMyGodCard());
        }
        return godCards;
    }


    /**
     * @param tokenId a unique identifier of a token.
     * @return the token who is associated with that token id. Null if there is no token with that id
     */
    public Token parseToken(int tokenId) {
        for (Player player: allPlayers) {
            if (tokenId == player.getToken1().getId())
                return player.getToken1();
            else if(tokenId == player.getToken2().getId())
                return player.getToken2();
        }
        return null;
    }


    /**
     * Here there is the ad-hoc call for the computeValidMoves method.
     * It is based on the player-in-turn god card.
     * If that god card do not modify the move than it calls the default move.
     * More JavaDOC inside the ad-hoc method.
     */
    public List<Cell> computeValidMoves(Token selectedToken, Token otherToken, List<Token> enemyTokens, GodCard myGodCard, List<GodCard> enemyGodCards, Battlefield battlefield) throws CellOutOfBattlefieldException {

        switch (myGodCard) {
            case APOLLO: {
                MoveContext thisMove = new MoveContext(( new ApolloMoves()));
                return thisMove.executeValidMoves(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, battlefield);
            }
            case ARTEMIS: {
                MoveContext thisMove = new MoveContext(( new ArtemisMoves()));
                return thisMove.executeValidMoves(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, battlefield);
            }
            case MINOTAUR: {
                MoveContext thisMove = new MoveContext(( new MinotaurMoves()));
                return thisMove.executeValidMoves(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, battlefield);
            }
            default: {
                MoveContext thisMove = new MoveContext(new SimpleMoves());
                return thisMove.executeValidMoves(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, battlefield);
            }
        }
    }


    /**
     * This is called by the controller just to check that the real player has given an appropriate input in the
     * move phase.
     * It works exactly like the computeValidMoves but there is no need to check that there are no valid moves.
     */
    public List<Cell> askValidMoves (PlayerAction playerAction) throws CellOutOfBattlefieldException {

        Player playerActive = playerAction.getPlayer();

        List<Player> opponents = getOpponents(playerActive);
        List<Token> enemyTokens = getTokens(opponents);

        int selectedTokenId = playerAction.getTokenMain();
        int otherTokenId = playerAction.getTokenOther();

        Token selectedToken = parseToken(selectedTokenId);
        Token otherToken = parseToken(otherTokenId);

        GodCard myGodCard = playerActive.getMyGodCard();
        List<GodCard> enemyGodCards = getGodCards(opponents);

        return computeValidMoves(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, getBattlefield());
    }


    /**
     * This method is called when i check the valid moves of a player.
     * If the return of that method is null I check if that player has lost the game or
     * if he can move the other token.
     * If he lost it check how many player there are.
     * If 2 the other one wins the game,
     * if 3 this player is removed from the game.
     * @return the correct ServerResponse to let the game routine run properly.
     * It could be a TOKEN_NOT_MOVABLE, GAME_OVER or PLAYER_LOST.
     * @throws CellOutOfBattlefieldException if something goes wrong.
     */
    public ServerResponse checkLoseForMove(Token selectedToken, Token otherToken, List<Token> enemyTokens, GodCard myGodCard, List<GodCard> enemyGodCards, Battlefield battlefield, List<Player> opponents, Player playerActive) throws CellOutOfBattlefieldException, WrongNumberPlayerException, ImpossibleTurnException {

        if (otherToken == null) {                              // se il secondo token non esiste
            if (allPlayers.size()==2) {                                             // se ci sono 2 player
                return gameOver(opponents.get(0).getUsername());
            }
            if (allPlayers.size() == 3) {                                             // se ci sono 3 player
                return playerLost(playerActive);
            }
        }
        else {                                                                  // se il secondo token esiste
            List<Cell> validMoves2 = new ArrayList<>();
            validMoves2 = computeValidMoves(otherToken, null, enemyTokens, myGodCard, enemyGodCards, battlefield);

            if (validMoves2 == null) {                                         // se non lo posso muovere
                if (allPlayers.size() == 2) {                                     // se ci sono 2 player
                    return gameOver(opponents.get(0).getUsername());
                }
            if (allPlayers.size() == 3) {                                         // se ci sono 3 player
                    return playerLost(playerActive);
                }
            }
        }
        return new ServerResponse(Action.TOKEN_NOT_MOVABLE, this.getCopy(), null, null, null);
    }


    /**
     * Here is where the move is parsed for ad-hoc method.
     * The check for the legal move is made by the controller.
     * After a player has moved his token, that token id checked for the win condition.
     * If true a message is sent to the client,
     * if not the game continue normally.
     * @param playerAction the message from the observer that contain all the information.
     */
    public void performMove (PlayerAction playerAction) throws CellOutOfBattlefieldException, ReachHeightLimitException, CellHeightException, IOException, ImpossibleTurnException, WrongNumberPlayerException {

        Player playerActive = playerAction.getPlayer();
        GodCard myGodCard = playerActive.getMyGodCard();
        Cell targetCell = playerAction.getCell();

        int selectedTokenId = playerAction.getTokenMain();
        int otherTokenId = playerAction.getTokenOther();

        Token selectedToken = parseToken(selectedTokenId);
        if (selectedToken == null || !selectedToken.getTokenColor().equals(turn)) {
            this.notifyWrongInput(playerAction);
            return;
        }
        Token otherToken = parseToken(otherTokenId);

        List<Player> opponents = getOpponents(playerActive);
        List<Token> enemyTokens = getTokens(opponents);

        List<GodCard> enemyGodCards = getGodCards(opponents);

        switch (myGodCard) {
            case APOLLO:{
                MoveContext thisMove = new MoveContext(new ApolloMoves());
                thisMove.executeMove(selectedToken, otherToken, enemyTokens, targetCell, enemyGodCards, battlefield);
                break;
            }

            default:{
                MoveContext thisMove = new MoveContext(new SimpleMoves());
                thisMove.executeMove(selectedToken, otherToken, enemyTokens, targetCell, enemyGodCards, battlefield);
            }
        }

        // check for a win move

        if (this.checkWin(selectedToken, myGodCard, enemyGodCards)) {
            String winner = playerAction.getPlayer().getUsername();
            gameOver(winner);
            return;
        }

        // If nobody has won then i aks for where to build

        List<Cell> validBuilds = validBuilds(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, battlefield);

        if (validBuilds == null) {
            if (allPlayers.size() == 3) {
                playerLost(playerAction.getPlayer());
            }
            else if(allPlayers.size() == 2) {
                updateTurn();
                String winner = getTurn().toString();
                gameOver(winner);
            }
        }
        else {
            ServerResponse serverResponse = new ServerResponse(Action.ASK_FOR_BUILD, this.getCopy(), null, validBuilds, null);
            notify(serverResponse);
        }
    }


    /**
     * It parses the input for get the correct method based on the god card.
     * A player CAN build around the token he moved.
     * A token can NOT build out of the battlefield,
     * where there is a token (himself too),
     * where there is a dome.
     * @return a List of Cell in which the token just moved can build.
     */
    public List<Cell> validBuilds (Token selectedToken, Token otherToken, List<Token> enemyTokens, GodCard myGodCard, List<GodCard> enemyGodCards, Battlefield battlefield) throws CellOutOfBattlefieldException {

        List<Cell> validBuildsProvv = new ArrayList<>();

        switch (myGodCard) {
            case DEMETER:{
                /*  Demeter può costruire e poi costruire una seconda volta ma non nella stella cella. Questo significa
                    che la prima build, di fatto, è una SimpleBuild.
                 */
                BuildContext thisBuild = new BuildContext(( new SimpleBuild()));
                validBuildsProvv = thisBuild.executeValidBuilds(selectedToken, otherToken, enemyTokens, enemyGodCards, battlefield, null);
            }
            case ZEUS:{
                BuildContext thisBuild = new BuildContext(new ZeusBuild());
                validBuildsProvv = thisBuild.executeValidBuilds(selectedToken, otherToken, enemyTokens, enemyGodCards, battlefield, null);
            }
            default:{
                BuildContext thisBuild = new BuildContext(new SimpleBuild());
                validBuildsProvv = thisBuild.executeValidBuilds(selectedToken, otherToken, enemyTokens, enemyGodCards, battlefield, null);
            }
        }

        if (enemyGodCards.contains(GodCard.LIMUS)){
            BuildContext thisBuild = new BuildContext(new LimusBuild());
            List<Cell> toRemove = thisBuild.executeValidBuilds(selectedToken, otherToken, enemyTokens, enemyGodCards, battlefield, allPlayers);
            for(Cell c: toRemove){                                  //lavora diversamente: toglie quelle vicine a LIMUS
                if(validBuildsProvv.contains(c)){
                    validBuildsProvv.remove(c);
                }
            }
        }

        return validBuildsProvv;
    }




    /**
     * This is called by the controller just to check if the real player has given a correct value.
     * It works exactly like the validBuilds method.
     * @param playerAction the message from the observer that contain all the information.
     * @return a list of Cell in which a player can build.
     */
    public List<Cell> askForValidBuilds (PlayerAction playerAction) throws CellOutOfBattlefieldException {

        Player playerActive = playerAction.getPlayer();

        List<Player> opponents = getOpponents(playerActive);
        List<Token> enemyTokens = getTokens(opponents);

        int selectedTokenId = playerAction.getTokenMain();
        int otherTokenId = playerAction.getTokenOther();

        Token selectedToken = parseToken(selectedTokenId);
        Token otherToken = parseToken(otherTokenId);

        GodCard myGodCard = playerActive.getMyGodCard();
        List<GodCard> enemyGodCards = getGodCards(opponents);

        return validBuilds(selectedToken, otherToken, enemyTokens, myGodCard, enemyGodCards, getBattlefield());
    }


    /**
     * It call the incrementHeight on the cell that a player has chosen to build.
     * After a build has been made, the turn is updated.
     * @param playerAction the message from the observer that contain all the information.
     */
    public void performBuild (PlayerAction playerAction) throws WrongNumberPlayerException, ImpossibleTurnException, CellHeightException, ReachHeightLimitException, CellOutOfBattlefieldException {

        Player playerActive = playerAction.getPlayer();
        GodCard myGodCard = playerActive.getMyGodCard();
        Cell targetCell = playerAction.getCell();
        boolean wantToUsePower = playerAction.getDoWantUsePower();

        switch (myGodCard) {
            case ATLAS: {
                BuildContext thisBuild;
                if (wantToUsePower){
                    thisBuild = new BuildContext(new AtlasBuild());
                }
                else{
                    thisBuild = new BuildContext(new SimpleBuild());
                }
                thisBuild.executePerformBuild(targetCell, getBattlefield());
                break;

            }
            case DEMETER:{
                /*  Per com'è fatto il programma ora, il giocatore inserire solo una cella in cui vuole costruire
                    non due, per cui per sfruttare il potere di demeter basterà chiedere al giocatore di inserire
                    una seconda volta un'altra cella in cui costruire. Nel fare ciò si deve controllare che le due
                    celle non siano uguali.
                    Una alternativa, che possiamo vedere insieme (ora non la implemento per evitare problemi con
                    git se state lavorando anche voi) può essere quella di, sapendo la god card del giocatore,
                    chiedergli le due celle in cui vuole costruire ( e metterle dentro una lista per esempio ). In
                    tal caso bisognerà rivedere i vari metodi.
                    Ovviamente per i giocatori che non hanno in nessun caso la faccoltà di costruire due volte
                    basterà continuare con il metodo finora implementato.
                 */

                BuildContext thisBuild = new BuildContext(new SimpleBuild());
                thisBuild.executePerformBuild(targetCell, getBattlefield());
                break;
            }
            default:{
                BuildContext thisBuild = new BuildContext(new SimpleBuild());
                thisBuild.executePerformBuild(targetCell, getBattlefield());
            }
        }
        // After the build need to check if chronus win.

        List<GodCard> allGodcards = getGodCards(allPlayers);

        if (allGodcards.contains(GodCard.CHRONUS)){
            WinContext thisWin = new WinContext(new ChronusWin());
            if (thisWin.executeCheckWin(null, battlefield)) {
                String winner = null;
                for (Player p: allPlayers){
                    if (p.getMyGodCard().equals(GodCard.CHRONUS)){
                        winner = p.getUsername();
                    }
                }
                assert winner != null;
                gameOver(winner);
            }
        }

        //  After the build is done the turn is end and a new player has to begin his turn.

        this.updateTurn();

        String askForSelect = String.format("Player %s select the token you want to move (x,y)",this.turn.toString());
        ServerResponse serverResponse = new ServerResponse(Action.START_NEW_TURN, this.getCopy(), null, null, askForSelect);
    }


    /**
     * A player win the game if he goes up from level 2 to level 3
     * @param movedToken the token to check if he verify the win condition
     * @return true if the token's owner win, false else.
     */
    public boolean checkWin (Token movedToken, GodCard myGodCard, List<GodCard> enemyGodCards) throws CellOutOfBattlefieldException {

        boolean didIWin;

        switch (myGodCard) {
            case PAN: {
                WinContext thisWin = new WinContext(new PanWin());
                didIWin = thisWin.executeCheckWin(movedToken, null);
                break;
            }
            default: {
                WinContext thisWin = new WinContext(new SimpleWin());
                didIWin = thisWin.executeCheckWin(movedToken, null);
            }
        }
        if (enemyGodCards.contains(GodCard.HERA)){
            if (didIWin){
                WinContext thisWin = new WinContext(new HeraWin());
                didIWin = thisWin.executeCheckWin(movedToken, null);
            }
        }
        return didIWin;
    }


    /**
     * This method create a GAME OVER message because someone has won the game.
     * @param winner the player who win.
     * @return the correct ServerResponse.
     */
    public ServerResponse gameOver (String winner) {
        String victoryMessage = String.format("%s HAS WIN!", winner.toUpperCase());
        return new ServerResponse (Action.GAME_OVER, this.getCopy(), null, null, victoryMessage);
    }


    /**
     * This method remove a player from the game because there are 3 player and one lost.
     * @param looser the player who lost.
     * @return the correct ServerResponse.
     */
    public ServerResponse playerLost (Player looser) throws WrongNumberPlayerException, ImpossibleTurnException {
        String lostMessage = String.format("%s HAS LOST! Better luck next time!", looser.getUsername().toUpperCase());
        removeFromTheGame(looser);
        return new ServerResponse (Action.PLAYER_LOST, this.getCopy(), null, null, lostMessage);
    }


    /**
     * It removes a player from the game and set free the cells where he has his tokens.
     * @param player the player who has to be removed.
     */
    public void removeFromTheGame (Player player) throws WrongNumberPlayerException, ImpossibleTurnException {

        player.getToken1().getTokenPosition().setFree();
        player.getToken2().getTokenPosition().setFree();
        allPlayers.remove(player);

        // probabilmente dovrei anche deallocare cose e liberarne altre, ma confido nel garbage collector <3

        updateTurn();
    }


    /**
     * It updates the current turn:
     * if it is red than it update to blue,
     * if it is blue it check for number of players. If 2 than it is red again,
     * if it is 3 it change to yellow.
     * if it is yellow it update to red.
     * @throws ImpossibleTurnException if the turn is not red, blue or yellow.
     * @throws WrongNumberPlayerException if the numbers of player is not 2 nor 3;
     */
    public void updateTurn() throws ImpossibleTurnException, WrongNumberPlayerException {

        switch (turn) {

            case RED: {
                this.turn = TokenColor.BLUE;
                break;
            }

            case BLUE: {
                if (allPlayers.size() == 2) {
                    this.turn = TokenColor.RED;
                } else if (allPlayers.size() == 3) {
                    this.turn = TokenColor.YELLOW;
                }
                else {
                    throw new WrongNumberPlayerException(
                            String.format("There are %d players and it is not allowed!", allPlayers.size()));
                }
                break;
            }

            case YELLOW: {
                this.turn = TokenColor.RED;
                break;
            }
            default: {
                throw new ImpossibleTurnException(
                        String.format("The color %s is not a valid turn", turn));
            }
        }
    }


    /**
     * It is called by the controller if a player make a request when it is not his turn.
     * It creates a ServerResponse with a message that tells him whose player is turn.
     * @throws CellOutOfBattlefieldException if something goes wrong.
     */
    public void notifyNotYourTurn() throws CellOutOfBattlefieldException, ReachHeightLimitException, CellHeightException, IOException, ImpossibleTurnException, WrongNumberPlayerException {

        String whoIsTurn = String.format("Now is player %s turn!", this.turn.toString());

        ServerResponse serverResponse = new ServerResponse(Action.NOT_YOUR_TURN, null, null, null, whoIsTurn);

        notify(serverResponse);
    }


    /**
     * It is called by the controller to notify the player-in-turn that he has insert a wrong or
     * a non expected input.
     * @param playerAction the message from the observer that contain all the information.
     */
    public void notifyWrongInput(PlayerAction playerAction) throws ImpossibleTurnException, IOException, CellHeightException, WrongNumberPlayerException, ReachHeightLimitException, CellOutOfBattlefieldException {

        String whatAction;

        switch (playerAction.getAction()) {
            case SELECT_TOKEN: {
                whatAction = "Please insert the position of a token you can move.";
            }
            case MOVE: {
                whatAction = "Please insert a valid position where you can move your token.";
            }
            default: {
                whatAction = Action.WRONG_INPUT.getInfo();
            }
        }

        String errorMessage = String.format("%s your choice is not valid.\n%s",
                playerAction.getPlayer().getUsername().toUpperCase(), whatAction);

        ServerResponse serverResponse = new ServerResponse(Action.WRONG_INPUT, null, null, null, errorMessage);
        notify(serverResponse);
    }
}






























