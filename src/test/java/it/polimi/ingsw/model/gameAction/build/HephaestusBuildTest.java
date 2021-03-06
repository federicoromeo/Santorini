package it.polimi.ingsw.model.gameAction.build;

import it.polimi.ingsw.model.gameAction.Utility;
import it.polimi.ingsw.model.Battlefield;
import it.polimi.ingsw.model.Cell;
import it.polimi.ingsw.model.Token;
import it.polimi.ingsw.model.TokenColor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

class HephaestusBuildTest {
    Battlefield battlefield;
    Cell first_cell;
    int high_first_cell;
    List<Token> enemyTokens;
    Token selectedToken;

    @BeforeEach
    void setUp() {

        battlefield = Utility.setUpForTest1();
        enemyTokens = new ArrayList<>();
        enemyTokens.add(new Token(TokenColor.RED));
        selectedToken = new Token(TokenColor.BLUE);
    }

    /**
     * The high of first_cell has to increase by two.
     */
    @Test
    void HephaestusPerformBuild() {
        //setup for this method
        battlefield = new Battlefield();
        first_cell = battlefield.getCell(2,3);
        first_cell.setFree();

        first_cell.setHeight(1);
        high_first_cell = first_cell.getHeight();

        assertEquals(high_first_cell,1);

        BuildContext thisBuild = new BuildContext(new HephaestusBuild());
        thisBuild.executePerformBuild(first_cell, null, battlefield);

        assertEquals(first_cell.getHeight(), high_first_cell + 2);
    }

    /**
     * This test the valid builds with almost each kind of limitation.
     */
    @Test
    void HephaestusValidBuilds() {
        selectedToken.setTokenPosition(battlefield.getCell(2,2));
        battlefield.getCell(2,2).setOccupied();
        enemyTokens.get(0).setTokenPosition(battlefield.getCell(1,1));
        battlefield.getCell(1,1).setOccupied();

        List<Cell> validBuilds;

        BuildContext thisBuild = new BuildContext(new HephaestusBuild());
        validBuilds = thisBuild.executeValidBuilds(selectedToken, null, enemyTokens, null, battlefield, null);

        Assert.assertEquals(5,validBuilds.size());
        Assert.assertTrue(validBuilds.contains(battlefield.getCell(3,1)));
        Assert.assertTrue(validBuilds.contains(battlefield.getCell(1,2)));
        Assert.assertTrue(validBuilds.contains(battlefield.getCell(3,3)));
        Assert.assertTrue(validBuilds.contains(battlefield.getCell(1,3)));
        Assert.assertTrue(validBuilds.contains(battlefield.getCell(2,3)));
    }
}