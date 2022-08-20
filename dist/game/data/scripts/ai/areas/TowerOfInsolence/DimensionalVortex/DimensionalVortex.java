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
package ai.areas.TowerOfInsolence.DimensionalVortex;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.HeavenlyRiftManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import ai.AbstractNpcAI;

/**
 * @author Brutallis
 */
public class DimensionalVortex extends AbstractNpcAI
{
	// NPC
	private static final int DIMENTIONAL_VORTEX = 30952;
	// Items
	private static final int CELESTIAL_SHARD = 49759;
	private static final int BROKEN_CELESTIAL_SHARD = 49767;
	
	private DimensionalVortex()
	{
		addStartNpc(DIMENTIONAL_VORTEX);
		addTalkId(DIMENTIONAL_VORTEX);
		addFirstTalkId(DIMENTIONAL_VORTEX);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("30952-1.htm") || event.equals("30952-2.htm") || event.equals("30952-3.htm"))
		{
			return event;
		}
		
		if (event.equals("tryenter"))
		{
			if (getQuestItemsCount(player, CELESTIAL_SHARD) >= 1)
			{
				if (player.isGM())
				{
					player.destroyItemByItemId("Rift", CELESTIAL_SHARD, 1, npc, true);
					GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 0);
					GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
					player.teleToLocation(112685, 13362, 10966);
					ThreadPool.schedule(new HeavenlyRiftManager.ClearZoneTask(npc), 180000);
					return null;
				}
				if (!player.isInParty())
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
					return null;
				}
				final Party party = player.getParty();
				if ((party == null) || !party.isLeader(player))
				{
					player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
					return null;
				}
				for (Player partyMember : party.getMembers())
				{
					if (player.calculateDistance2D(partyMember) > Config.ALT_PARTY_RANGE)
					{
						continue;
					}
					final SystemMessage sm = new SystemMessage(SystemMessageId.C1_IS_IN_A_LOCATION_WHICH_CANNOT_BE_ENTERED_THEREFORE_IT_CANNOT_BE_PROCESSED);
					sm.addPcName(partyMember);
					player.sendPacket(sm);
					party.broadcastToPartyMembers(player, sm);
					return null;
				}
				player.destroyItemByItemId("Rift", CELESTIAL_SHARD, 1, npc, true);
				GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 0);
				GlobalVariablesManager.getInstance().set("heavenly_rift_level", 0);
				for (Player partyMember : party.getMembers())
				{
					partyMember.teleToLocation(112685, 13362, 10966);
				}
				ThreadPool.schedule(new HeavenlyRiftManager.ClearZoneTask(npc), 1200000);
			}
			else
			{
				return "30952-3.htm";
			}
		}
		else if (event.equals("exchange"))
		{
			long count = getQuestItemsCount(player, BROKEN_CELESTIAL_SHARD);
			if (count < 10)
			{
				return "30952-2.htm";
			}
			if ((count % 10) != 0)
			{
				count -= count % 10;
			}
			final long reward = count / 10;
			player.destroyItemByItemId("Rift", BROKEN_CELESTIAL_SHARD, count, npc, true);
			player.addItem("Rift", CELESTIAL_SHARD, reward, npc, true);
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new DimensionalVortex();
	}
}
