package org.mage.test.cards.abilities.keywords;

import mage.constants.PhaseStep;
import mage.constants.Zone;
import mage.counters.CounterType;
import org.junit.Test;
import org.mage.test.player.TestPlayer;
import org.mage.test.serverside.base.CardTestPlayerBase;

/**
 * 700.17
 * Some abilities trigger when a player “faces a dilemma.” A player faces a dilemma whenever they choose one or more
 * modes for a spell or ability. Some effects refer to whether a player has “faced a dilemma this turn.” This means that
 * player has chosen one or more modes for a spell or ability this turn. An effect that refers to the number of
 * dilemmas a player has faced in a turn counts the number of spells and/or abilities the player has chosen one or more
 * modes for that turn.
 * <p>
 * 700.2
 * A spell or ability is modal if it has two or more options in a bulleted list preceded by instructions for a player
 * to choose a number of those options, such as “Choose one —.” Each of those options is a mode.
 * <p>
 * 700.2a
 * The controller of a modal spell or activated ability chooses the mode(s) as part of casting that spell or activating
 * that ability. If one of the modes would be illegal (due to an inability to choose legal targets, for example), that
 * mode can’t be chosen. (See rule 601.2b.)
 * <p>
 * 700.2b
 * The controller of a modal triggered ability chooses the mode(s) as part of putting that ability on the stack. If one
 * of the modes would be illegal (due to an inability to choose legal targets, for example), that mode can’t be chosen.
 * If no mode is chosen, the ability is removed from the stack. (See rule 603.3c.)
 * <p>
 * 700.2e
 * Some spells and abilities specify that a player other than their controller chooses a mode for it. In that case, the
 * other player does so when the spell or ability’s controller normally would do so. If there is more than one other
 * player who could make such a choice, the spell or ability’s controller decides which of those players will make the
 * choice.
 * <p>
 * 700.2g
 * A copy of a modal spell or ability copies the mode(s) chosen for it. The controller of the copy can’t choose a
 * different mode. (See rule 707.10.)
 * <p>
 * 601.2b
 * If the spell is modal, the player announces the mode choice (see rule 700.2).
 *
 * @author BrandonPurvis
 */
public class DilemmaTest extends CardTestPlayerBase {

    // Melkotian Adjudicator {2}{U}
    // Creature - Melkotian Advisor 2/1
    // Flying, vigilance
    // Whenever you face a dilemma, put a +1/+1 counter on this creature.
    private static final String adjudicator = "Melkotian Adjudicator";

    // Gabriel Lorca, Mirror Menace {4}{B}
    // Legendary Creature - Human Rogue Officer 5/4
    // Menace
    // Whenever you face a dilemma, you may pay {B}. If you do, return this card from your graveyard to your hand.
    private static final String lorca = "Gabriel Lorca, Mirror Menace";

    // Boros Charm {R}{W}
    // Instant
    // Choose one --
    // * Boros Charm deals 4 damage to target player or planeswalker.
    // * Permanents you control gain indestructible until end of turn.
    // * Target creature gains double strike until end of turn.
    private static final String borosCharm = "Boros Charm";

    // Bow of Nylea {1}{G}{G}
    // Legendary Enchantment Artifact
    // Attacking creatures you control have deathtouch.
    // {1}{G}, {T}: Choose one --
    // * Put a +1/+1 counter on target creature.
    // * Bow of Nylea deals 2 damage to target creature with flying.
    // * You gain 3 life.
    // * Put up to four target cards from your graveyard on the bottom of your library in any order.
    private static final String bow = "Bow of Nylea";

    @Test
    public void testModalSpell() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        addCard(Zone.HAND, playerA, borosCharm);

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2"); // indestructible
        checkStackSize("after cast", 1, PhaseStep.PRECOMBAT_MAIN, playerA, 2); // spell + dilemma trigger
        checkStackObject("after cast", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Whenever you face a dilemma", 1);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, true); // the dilemma trigger resolves first
        checkPermanentCounters("after trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 1);
        checkStackObject("after trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast " + borosCharm, 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, borosCharm, 1);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testModalActivatedAbility() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, bow);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);

        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{G}, {T}:");
        setModeChoice(playerA, "3"); // gain 3 life
        checkStackSize("after activation", 1, PhaseStep.PRECOMBAT_MAIN, playerA, 2); // ability + dilemma trigger
        checkStackObject("after activation", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Whenever you face a dilemma", 1);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, true); // the dilemma trigger resolves first
        checkPermanentCounters("after trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 1);
        checkStackObject("after trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "{1}{G}, {T}:", 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerA, 20 + 3);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testModalTriggeredAbility() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 2);
        // When Charming Prince enters, choose one --
        // * Scry 2.
        // * You gain 3 life.
        // * Exile another target creature you own. Return it to the battlefield under your control at the beginning of
        //   the next end step.
        addCard(Zone.HAND, playerA, "Charming Prince"); // {1}{W}

        // the creature spell itself is not modal
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Charming Prince");
        setModeChoice(playerA, "2"); // gain 3 life
        // the creature spell resolves and its trigger goes on the stack
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, true);
        checkStackSize("after enters", 1, PhaseStep.PRECOMBAT_MAIN, playerA, 2); // trigger + dilemma trigger
        checkStackObject("after enters", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Whenever you face a dilemma", 1);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN, true); // the dilemma trigger resolves first
        checkPermanentCounters("after trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 1);
        checkStackObject("after trigger", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "When {this} enters", 1);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerA, 20 + 3);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testSeveralModesAreOneDilemma() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 3 + 2); // the auto-payer spends Islands on Cryptic Command first
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        // Choose two --
        // * Counter target spell.
        // * Return target permanent to its owner's hand.
        // * Tap all creatures your opponents control.
        // * Draw a card.
        addCard(Zone.HAND, playerA, "Cryptic Command"); // {1}{U}{U}{U}
        // Choose one --
        // * Barbed Lightning deals 3 damage to target creature.
        // * Barbed Lightning deals 3 damage to target player or planeswalker.
        // Entwine {2} (Choose both if you pay the entwine cost.)
        addCard(Zone.HAND, playerA, "Barbed Lightning"); // {2}{R}

        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cryptic Command");
        setModeChoice(playerA, "3");
        setModeChoice(playerA, "4");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("two modes", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 1);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Barbed Lightning");
        setChoice(playerA, true); // entwine, so every mode is chosen
        addTarget(playerA, "Grizzly Bears");
        addTarget(playerA, playerB);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerB, "Grizzly Bears", 1);
        assertLife(playerB, 20 - 3);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 2);
    }

    @Test
    public void testOpponentChoosesModes() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerB, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        // An opponent chooses one --
        // * You draw three cards.
        // * You destroy up to two target creatures that player controls. They can't be regenerated. That player draws
        //   up to three cards.
        addCard(Zone.HAND, playerA, "Fatal Lore"); // {2}{B}{B}
        addCard(Zone.HAND, playerA, borosCharm);

        // the opponent chooses the mode, so the opponent faces the dilemma
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Fatal Lore");
        setChoice(playerA, playerB.getName()); // the opponent who chooses
        setModeChoice(playerB, "1"); // playerA draws three cards
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("chooser", 1, PhaseStep.PRECOMBAT_MAIN, playerB, adjudicator, CounterType.P1P1, 1);
        checkPermanentCounters("caster", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 0);

        // playerA chooses the mode, so playerB's Adjudicator doesn't trigger
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
        assertCounterCount(playerB, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testRandomModes() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        // Whenever Cult of Skaro attacks, choose one at random --
        // * Thay -- Put a +1/+1 counter on each artifact creature you control.
        // * Caan -- Draw two cards.
        // * Sec -- Create a 3/3 black Dalek artifact creature token with menace.
        // * Jast -- Each opponent loses 4 life.
        addCard(Zone.BATTLEFIELD, playerA, "Cult of Skaro");

        attack(1, playerA, "Cult of Skaro");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testLegalModes() {
        addCard(Zone.GRAVEYARD, playerA, lorca);
        addCard(Zone.BATTLEFIELD, playerA, "Badlands", 7);
        // Choose one --
        // * Abrade deals 3 damage to target creature.
        // * Destroy target artifact.
        addCard(Zone.HAND, playerA, "Abrade"); // {1}{R}
        // When Downwind Ambusher enters, choose one --
        // * Target creature an opponent controls gets -1/-1 until end of turn.
        // * Destroy target creature an opponent controls that was dealt damage this turn.
        addCard(Zone.HAND, playerA, "Downwind Ambusher"); // {3}{B} 4/2

        // no creature or artifact to target, so no mode is legal and the spell can't be cast
        checkPlayableAbility("no legal mode", 1, PhaseStep.PRECOMBAT_MAIN, playerA, "Cast Abrade", false);

        // playerB controls no creatures, so the trigger has no legal mode and is removed
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Downwind Ambusher");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkGraveyardCount("no dilemma", 1, PhaseStep.PRECOMBAT_MAIN, playerA, lorca, 1);

        // the Ambusher is the only creature and there are no artifacts, so only the first mode is legal
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Abrade");
        setModeChoice(playerA, "1");
        addTarget(playerA, "Downwind Ambusher");
        setChoice(playerA, true); // pay {B} to return Lorca

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, "Downwind Ambusher", 1);
        assertHandCount(playerA, lorca, 1);
    }

    @Test
    public void testNoModeChosen() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        // Whenever you cast a spell, choose up to one --
        // * Return target spell you don't control to its owner's hand.
        // * Return target nonland permanent to its owner's hand.
        addCard(Zone.BATTLEFIELD, playerA, "Hullbreaker Horror");
        // {T}: Goblin Fireslinger deals 1 damage to target player or planeswalker.
        addCard(Zone.BATTLEFIELD, playerA, "Goblin Fireslinger");
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        addCard(Zone.HAND, playerA, "Lightning Bolt");
        // When Elvish Visionary enters, draw a card.
        addCard(Zone.HAND, playerA, "Elvish Visionary"); // {1}{G}
        addCard(Zone.HAND, playerA, borosCharm);

        // non-modal spell, ability and trigger; Hullbreaker Horror's trigger has legal modes, but none is chosen
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Lightning Bolt", playerB);
        setModeChoice(playerA, TestPlayer.MODE_SKIP); // Hullbreaker Horror
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        activateAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: {this} deals 1 damage", playerB);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Elvish Visionary");
        setModeChoice(playerA, TestPlayer.MODE_SKIP); // Hullbreaker Horror
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("no dilemma", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 0);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2"); // Boros Charm
        setChoice(playerA, "Whenever you face a dilemma"); // order trigger
        setModeChoice(playerA, TestPlayer.MODE_SKIP); // Hullbreaker Horror

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 20 - 3 - 1);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testCopies() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Plains", 4);
        addCard(Zone.HAND, playerA, borosCharm, 2);
        // Copy target instant or sorcery spell. You may choose new targets for the copy.
        addCard(Zone.HAND, playerA, "Twincast"); // {U}{U}
        // Imprint -- When Isochron Scepter enters, you may exile an instant card with mana value 2 or less from
        // your hand.
        // {2}, {T}: You may copy the exiled card. If you do, you may cast the copy without paying its mana cost.
        addCard(Zone.HAND, playerA, "Isochron Scepter"); // {2}

        // a copy of a spell keeps the original's modes
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Twincast", borosCharm, borosCharm);
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("copy of a spell", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                adjudicator, CounterType.P1P1, 1);

        // a copy of a card is cast normally
        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "Isochron Scepter");
        setChoice(playerA, true); // imprint
        setChoice(playerA, borosCharm);
        waitStackResolved(1, PhaseStep.POSTCOMBAT_MAIN);
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{2}, {T}:");
        setChoice(playerA, true); // copy
        setChoice(playerA, true); // cast the copy
        setModeChoice(playerA, "2");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, "Twincast", 1);
        assertExileCount(playerA, borosCharm, 1);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 2);
    }

    @Test
    public void testAsEntersChoice() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Mountain", 4);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        // As Outpost Siege enters, choose Khans or Dragons.
        addCard(Zone.HAND, playerA, "Outpost Siege"); // {3}{R}
        addCard(Zone.HAND, playerA, borosCharm);

        // a choice made as a permanent enters is not a mode
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Outpost Siege");
        setChoice(playerA, "Khans");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("no dilemma", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 0);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertPermanentCount(playerA, "Outpost Siege", 1);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
    }

    @Test
    public void testVillainousChoice() {
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerB, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, "Island", 5);
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 1);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        // Draw three cards. Then target opponent faces a villainous choice -- They discard three cards, or you may
        // cast a spell from your hand without paying its mana cost.
        addCard(Zone.HAND, playerA, "Great Intelligence's Plan"); // {4}{U}{B}
        addCard(Zone.HAND, playerA, borosCharm);

        // a villainous choice is not a mode
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Great Intelligence's Plan");
        addTarget(playerA, playerB);
        setChoice(playerB, true); // discard three cards (playerB has none)
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("no dilemma A", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 0);
        checkPermanentCounters("no dilemma B", 1, PhaseStep.PRECOMBAT_MAIN, playerB, adjudicator, CounterType.P1P1, 0);

        castSpell(1, PhaseStep.POSTCOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2");

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 1);
        assertCounterCount(playerB, adjudicator, CounterType.P1P1, 0);
    }

    @Test
    public void testSacrificedAsCostStillTriggers() {
        // Whenever you face a dilemma, draw a card.
        addCard(Zone.BATTLEFIELD, playerA, "Seven of Nine");
        addCard(Zone.BATTLEFIELD, playerA, "Hill Giant");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 2);
        addCard(Zone.BATTLEFIELD, playerB, "Grizzly Bears");
        // As an additional cost to cast this spell, sacrifice a creature.
        // Choose one or both --
        // * Return target creature card from your graveyard to the battlefield if its power is less than or equal to
        //   the sacrificed creature's power.
        // * Destroy target creature.
        addCard(Zone.HAND, playerA, "Desperate Plea"); // {1}{B}

        // the dilemma is faced before costs are paid, while Seven of Nine is still on the battlefield
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Desperate Plea");
        setModeChoice(playerA, "2");
        setModeChoice(playerA, TestPlayer.MODE_SKIP);
        addTarget(playerA, "Grizzly Bears");
        setChoice(playerA, "Seven of Nine"); // sacrifice for cost

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, "Seven of Nine", 1);
        assertGraveyardCount(playerB, "Grizzly Bears", 1);
        assertHandCount(playerA, 1); // drew a card
    }

    @Test
    public void testDiscardedAsCostDoesNotTrigger() {
        addCard(Zone.HAND, playerA, lorca); // discarded for escalate
        addCard(Zone.GRAVEYARD, playerA, lorca); // returns
        addCard(Zone.BATTLEFIELD, playerA, lorca); // doesn't work on the battlefield
        addCard(Zone.HAND, playerA, "Mountain");
        addCard(Zone.BATTLEFIELD, playerA, "Swamp", 3);
        // Escalate -- Discard a card.
        // Choose one or more --
        // * Target opponent reveals their hand. You choose an instant or sorcery card from it. That player discards
        //   that card.
        // * Target creature gets -2/-2 until end of turn.
        // * Target opponent loses 2 life and you gain 2 life.
        addCard(Zone.HAND, playerA, "Collective Brutality"); // {1}{B}

        // the dilemma is faced before costs are paid, while the discarded Lorca is still in hand
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, "Collective Brutality");
        setModeChoice(playerA, "1");
        setModeChoice(playerA, "3");
        setModeChoice(playerA, TestPlayer.MODE_SKIP);
        addTarget(playerA, playerB);
        addTarget(playerA, playerB);
        setChoice(playerA, lorca); // discard for escalate
        setChoice(playerA, true); // pay {B} to return the Lorca that was already in the graveyard

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertLife(playerB, 20 - 2);
        assertPermanentCount(playerA, lorca, 1);
        assertGraveyardCount(playerA, lorca, 1);
        assertHandCount(playerA, lorca, 1);
        assertHandCount(playerA, "Mountain", 1);
    }

    @Test
    public void testReversedCastAndActivation() {
        // the player cancels the payment, so the cast or activation is reversed after the modes were chosen
        addCard(Zone.BATTLEFIELD, playerA, adjudicator);
        addCard(Zone.BATTLEFIELD, playerA, bow);
        addCard(Zone.BATTLEFIELD, playerA, "Plateau", 2);
        addCard(Zone.BATTLEFIELD, playerA, "Forest", 2);
        addCard(Zone.HAND, playerA, borosCharm);

        disableManaAutoPayment(playerA); // manual mode so the payment can be canceled

        // cast
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2");
        setChoice(playerA, TestPlayer.MANA_CANCEL); // mana payment cancel (cancel button in UI)
        setChoice(playerA, TestPlayer.SKIP_FAILED_COMMAND); // delete cast command from queue
        checkPermanentCounters("reversed cast", 1, PhaseStep.PRECOMBAT_MAIN, playerA, adjudicator, CounterType.P1P1, 0);
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {R}");
        activateManaAbility(1, PhaseStep.PRECOMBAT_MAIN, playerA, "{T}: Add {W}");
        castSpell(1, PhaseStep.PRECOMBAT_MAIN, playerA, borosCharm);
        setModeChoice(playerA, "2");
        setChoice(playerA, "Red");
        setChoice(playerA, "White");
        waitStackResolved(1, PhaseStep.PRECOMBAT_MAIN);
        checkPermanentCounters("completed cast", 1, PhaseStep.PRECOMBAT_MAIN, playerA,
                adjudicator, CounterType.P1P1, 1);

        // activation
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{1}{G}, {T}:");
        setModeChoice(playerA, "3"); // gain 3 life
        setChoice(playerA, TestPlayer.MANA_CANCEL); // mana payment cancel (cancel button in UI)
        setChoice(playerA, TestPlayer.SKIP_FAILED_COMMAND); // delete activate command from queue
        checkPermanentCounters("reversed activation", 1, PhaseStep.POSTCOMBAT_MAIN, playerA,
                adjudicator, CounterType.P1P1, 1);
        activateManaAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{T}: Add {G}", 2);
        activateAbility(1, PhaseStep.POSTCOMBAT_MAIN, playerA, "{1}{G}, {T}:");
        setModeChoice(playerA, "3"); // gain 3 life
        setChoice(playerA, "Green", 2);

        setStrictChooseMode(true);
        setStopAt(1, PhaseStep.END_TURN);
        execute();

        assertGraveyardCount(playerA, borosCharm, 1);
        assertTapped(bow, true);
        assertLife(playerA, 20 + 3);
        assertCounterCount(playerA, adjudicator, CounterType.P1P1, 2);
    }
}
