package mage.abilities.common;

import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.effects.Effect;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;

/**
 * @author BrandonPurvis
 */
public class FacedDilemmaTriggeredAbility extends TriggeredAbilityImpl {

    public FacedDilemmaTriggeredAbility(Effect effect) {
        this(Zone.BATTLEFIELD, effect, false);
    }

    public FacedDilemmaTriggeredAbility(Zone zone, Effect effect, boolean optional) {
        super(zone, effect, optional);
        setTriggerPhrase("Whenever you face a dilemma, ");
    }

    protected FacedDilemmaTriggeredAbility(final FacedDilemmaTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public FacedDilemmaTriggeredAbility copy() {
        return new FacedDilemmaTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == GameEvent.EventType.FACED_DILEMMA;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        return isControlledBy(event.getPlayerId());
    }
}
