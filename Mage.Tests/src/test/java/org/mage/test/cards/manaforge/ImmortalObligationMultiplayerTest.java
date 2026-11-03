package org.mage.test.cards.manaforge;
import mage.constants.*;
import mage.game.*;
import mage.game.mulligan.MulliganType;
import mage.game.permanent.Permanent;
import org.junit.Test;
import org.mage.test.serverside.base.CardTestMultiPlayerBase;
import java.io.FileNotFoundException;
import static org.junit.Assert.*;

public class ImmortalObligationMultiplayerTest extends CardTestMultiPlayerBase {
    @Override protected Game createNewGameAndPlayers() throws GameException, FileNotFoundException {
        Game game = new FreeForAll(MultiplayerAttackOption.MULTIPLE, RangeOfInfluence.ALL, MulliganType.GAME_DEFAULT.getMulligan(0), 40, 7);
        playerA = createPlayer(game, "PlayerA"); playerB = createPlayer(game, "PlayerB");
        playerC = createPlayer(game, "PlayerC"); playerD = createPlayer(game, "PlayerD");
        return game;
    }
    @Test public void restrictsOnlyCasterAndTheirPermanents() {
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Jace Beleren");
        addCard(Zone.BATTLEFIELD, playerC, "Chandra Nalaar");
        addCard(Zone.BATTLEFIELD, playerA, "Grizzly Bears");
        addCard(Zone.BATTLEFIELD, playerC, "Pillarfield Ox");
        addCard(Zone.GRAVEYARD, playerB, "Silvercoat Lion");
        addCard(Zone.HAND, playerA, "Immortal Obligation");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Immortal Obligation", "Silvercoat Lion");
        setStrictChooseMode(true); setStopAt(4, PhaseStep.PRECOMBAT_MAIN); execute();
        Permanent lion = getPermanent("Silvercoat Lion");
        assertTrue(lion.getGoadingPlayers().contains(playerA.getId()));
        assertFalse(lion.canAttack(playerA.getId(), currentGame));
        assertFalse(lion.canAttack(getPermanent("Jace Beleren").getId(), currentGame));
        assertTrue(lion.canAttack(playerC.getId(), currentGame));
        assertTrue(lion.canAttack(getPermanent("Chandra Nalaar").getId(), currentGame));
        assertFalse(lion.canBlock(getPermanent("Grizzly Bears").getId(), currentGame));
        assertTrue(lion.canBlock(getPermanent("Pillarfield Ox").getId(), currentGame));
    }
}
