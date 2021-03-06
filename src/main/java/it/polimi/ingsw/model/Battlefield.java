package it.polimi.ingsw.model;

import java.io.Serializable;


/**
 * A Battlefield is a matrix of Cell
 * It has a list with all the Players that are currently play on that Battlefield
 */
public class Battlefield implements Serializable {


    /**
     * The battlefield size of Santorini is 5
     */
    private static final int battlefieldSize = 5;


    /**
     * The matrix of cells
     */
    private final Cell[][] battlefield;


    /**
     * Value for single instance
     */
    private static Battlefield singleBattlefield;


    /**
     * The constructor create a matrix N*N of Cell, where N is the battlefieldSize
     */
    public Battlefield() {

        Cell[][] battlefield = new Cell[battlefieldSize][battlefieldSize];

        for (int x = 0; x < battlefieldSize; x++) {
            for (int y = 0; y < battlefieldSize; y++) {
                battlefield[x][y] = new Cell(x, y);
            }
        }
        this.battlefield = battlefield;
    }


    /**
     * Method for singleton constructor
     * @return the battlefield if exist. A new one else
     */
    public static Battlefield getInstance(){

        if (singleBattlefield==null){
            singleBattlefield = new Battlefield();
        }
        return singleBattlefield;
    }


    /**
     * It is used to get a Cell of the battlefield when the x-coords and y-coords are known
     * @param posX column of the battlefield
     * @param posY raw of the battlefield
     * @return the Cell with specified coords. Null else
     */
    public Cell getCell(int posX, int posY) {
        try{
            return battlefield[posX][posY];
        } catch (NullPointerException e){
            return null;
        }
    }


    /**
     * It is used when I have a Cell (usually of the Token) and I want to locate
     * it on the Battlefield
     * @param cell the cell i want to locate
     * @return the Cell with the same coords that is part of the battlefield
     */
    public Cell getCell(Cell cell) {
        int inputX = cell.getPosX();
        int inputY = cell.getPosY();
        return battlefield[inputX][inputY];
    }


    /**
     * @return a copy of this battlefield
     */
    protected final Battlefield getCopy() {

        final Battlefield battlefieldClone = new Battlefield();

        for (int x = 0; x < battlefieldSize; x++) {
            System.arraycopy(battlefield[x], 0, battlefieldClone.battlefield[x], 0, battlefieldSize);
        }
        return battlefieldClone;
    }


    /**
     * Called ony for test use
     * It reset the battlefield to run multiple tests at once
     */
    public void resetSingleton(){
        singleBattlefield = null;
    }
}