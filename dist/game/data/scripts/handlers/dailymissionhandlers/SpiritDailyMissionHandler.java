/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.dailymissionhandlers;

import java.util.function.Consumer;

import org.l2jmobius.gameserver.enums.DailyMissionStatus;
import org.l2jmobius.gameserver.enums.ElementalType;
import org.l2jmobius.gameserver.handler.AbstractDailyMissionHandler;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.DailyMissionPlayerEntry;
import org.l2jmobius.gameserver.model.ElementalSpirit;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.impl.creature.OnElementalSpiritUpgrade;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnElementalSpiritLearn;
import org.l2jmobius.gameserver.model.events.listeners.ConsumerEventListener;

/**
 * @author JoeAlisson
 */
public class SpiritDailyMissionHandler extends AbstractDailyMissionHandler
{
	private final int _amount;
	private final ElementalType _type;
	
	public SpiritDailyMissionHandler(DailyMissionDataHolder holder)
	{
		super(holder);
		_type = getHolder().getParams().getEnum("element", ElementalType.class, ElementalType.NONE);
		_amount = holder.getRequiredCompletions();
	}
	
	@Override
	public void init()
	{
		final MissionKind kind = getHolder().getParams().getEnum("kind", MissionKind.class, null);
		if (MissionKind.EVOLVE == kind)
		{
			Containers.Players().addListener(new ConsumerEventListener(this, EventType.ON_ELEMENTAL_SPIRIT_UPGRADE, (Consumer<OnElementalSpiritUpgrade>) this::onElementalSpiritUpgrade, this));
		}
		else if (MissionKind.LEARN == kind)
		{
			Containers.Players().addListener(new ConsumerEventListener(this, EventType.ON_ELEMENTAL_SPIRIT_LEARN, (Consumer<OnElementalSpiritLearn>) this::onElementalSpiritLearn, this));
		}
	}
	
	@Override
	public boolean isAvailable(Player player)
	{
		final DailyMissionPlayerEntry entry = getPlayerEntry(player.getObjectId(), false);
		return (entry != null) && (entry.getStatus() == DailyMissionStatus.AVAILABLE);
	}
	
	private void onElementalSpiritLearn(OnElementalSpiritLearn event)
	{
		final DailyMissionPlayerEntry missionData = getPlayerEntry(event.getPlayer().getObjectId(), true);
		missionData.setProgress(1);
		missionData.setStatus(DailyMissionStatus.AVAILABLE);
		storePlayerEntry(missionData);
	}
	
	private void onElementalSpiritUpgrade(OnElementalSpiritUpgrade event)
	{
		final ElementalSpirit spirit = event.getSpirit();
		if (ElementalType.of(spirit.getType()) != _type)
		{
			return;
		}
		
		final DailyMissionPlayerEntry missionData = getPlayerEntry(event.getPlayer().getObjectId(), true);
		missionData.setProgress(spirit.getStage());
		if (missionData.getProgress() >= _amount)
		{
			missionData.setStatus(DailyMissionStatus.AVAILABLE);
		}
		storePlayerEntry(missionData);
	}
	
	private enum MissionKind
	{
		LEARN,
		EVOLVE
	}
}
