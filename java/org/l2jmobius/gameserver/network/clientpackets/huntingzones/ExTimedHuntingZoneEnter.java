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
package org.l2jmobius.gameserver.network.clientpackets.huntingzones;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.TimedHuntingZoneData;
import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.olympiad.OlympiadManager;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneClose;
import org.l2jmobius.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneEnter;

/**
 * @author Mobius
 */
public class ExTimedHuntingZoneEnter implements IClientIncomingPacket
{
	private int _zoneId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_zoneId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.isInsideZone(ZoneId.PEACE))
		{
			player.sendMessage("Can only enter to a peace zone.");
			return;
		}
		if (player.isInCombat())
		{
			player.sendMessage("You can only enter in time-limited hunting zones while not in combat.");
			return;
		}
		if (player.getReputation() < 0)
		{
			player.sendMessage("You can only enter in time-limited hunting zones when you have positive reputation.");
			return;
		}
		if (player.isMounted())
		{
			player.sendMessage("Cannot use time-limited hunting zones while mounted.");
			return;
		}
		if (player.isInDuel())
		{
			player.sendMessage("Cannot use time-limited hunting zones during a duel.");
			return;
		}
		if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
		{
			player.sendMessage("Cannot use time-limited hunting zones while waiting for the Olympiad.");
			return;
		}
		if (player.isOnEvent() || (player.getBlockCheckerArena() > -1))
		{
			player.sendMessage("Cannot use time-limited hunting zones while registered on an event.");
			return;
		}
		if (player.isInInstance() || player.isInTimedHuntingZone(player.getX(), player.getY()))
		{
			player.sendMessage("Cannot use time-limited hunting zones while in an instance.");
			return;
		}
		
		final TimedHuntingZoneHolder holder = TimedHuntingZoneData.getInstance().getHuntingZone(_zoneId);
		if (holder == null)
		{
			return;
		}
		
		if ((player.getLevel() < holder.getMinLevel()) || (player.getLevel() > holder.getMaxLevel()))
		{
			player.sendMessage("Your level does not correspond the zone equivalent.");
			return;
		}
		
		final long currentTime = System.currentTimeMillis();
		final int instanceId = holder.getInstanceId();
		if ((instanceId > 0) && holder.isSoloInstance())
		{
			// Shared cooldown for all Transcendent instances.
			// TODO: Move shared instance cooldown to XML.
			for (int instId = 208; instId <= 213; instId++)
			{
				if (InstanceManager.getInstance().getInstanceTime(player, instId) > currentTime)
				{
					player.sendMessage("The transcendent instance has not reset yet.");
					return;
				}
			}
		}
		
		long endTime = currentTime + player.getTimedHuntingZoneRemainingTime(_zoneId);
		final long lastEntryTime = player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_ENTRY + _zoneId, 0);
		if ((lastEntryTime + holder.getResetDelay()) < currentTime)
		{
			if (endTime == currentTime)
			{
				endTime += holder.getInitialTime();
				player.getVariables().set(PlayerVariables.HUNTING_ZONE_ENTRY + _zoneId, currentTime);
			}
		}
		
		if (endTime > currentTime)
		{
			if (holder.getEntryItemId() == Inventory.ADENA_ID)
			{
				if (player.getAdena() > holder.getEntryFee())
				{
					player.reduceAdena("TimedHuntingZone", holder.getEntryFee(), player, true);
				}
				else
				{
					player.sendMessage("Not enough adena.");
					return;
				}
			}
			else if (!player.destroyItemByItemId("TimedHuntingZone", holder.getEntryItemId(), holder.getEntryFee(), player, true))
			{
				player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
				return;
			}
			
			player.getVariables().set(PlayerVariables.HUNTING_ZONE_TIME + _zoneId, endTime - currentTime);
			
			// Close window.
			player.sendPacket(TimedHuntingZoneClose.STATIC_PACKET);
			
			if (instanceId == 0)
			{
				player.teleToLocation(holder.getEnterLocation());
				
				// Send time icon.
				player.sendPacket(new TimedHuntingZoneEnter(player, _zoneId));
			}
			else // Transcendent zones.
			{
				QuestManager.getInstance().getQuest("TranscendentZone").notifyEvent("ENTER " + _zoneId, null, player);
			}
		}
		else
		{
			player.sendMessage("You don't have enough time available to enter the hunting zone.");
		}
	}
}
