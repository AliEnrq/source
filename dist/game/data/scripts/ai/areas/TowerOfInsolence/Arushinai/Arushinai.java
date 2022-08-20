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
package ai.areas.TowerOfInsolence.Arushinai;

import org.l2jmobius.Config;
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
public class Arushinai extends AbstractNpcAI
{
	// NPC
	private static final int ARUSHINAI = 30401;
	
	private Arushinai()
	{
		addStartNpc(ARUSHINAI);
		addTalkId(ARUSHINAI);
		addFirstTalkId(ARUSHINAI);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("30401-1.htm") || event.equals("30401-2.htm"))
		{
			return event;
		}
		
		if (event.equals("proceed"))
		{
			if (!player.isGM())
			{
				Party party = player.getParty();
				if (party == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_IN_A_PARTY_SO_YOU_CANNOT_ENTER);
					player.teleToLocation(114264, 13352, -5104);
					return null;
				}
				if (!party.isLeader(player))
				{
					player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_MAKE_THE_REQUEST_TO_ENTER);
					return null;
				}
			}
			
			if (GlobalVariablesManager.getInstance().getInt("heavenly_rift_complete", 0) != 0)
			{
				return null; // TODO: Find html?
			}
			
			final int riftLevel = getRandom(1, 3);
			GlobalVariablesManager.getInstance().set("heavenly_rift_level", riftLevel);
			GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 4);
			switch (riftLevel)
			{
				case 1:
				{
					HeavenlyRiftManager.startEvent20Bomb(player);
					return null;
				}
				case 2:
				{
					HeavenlyRiftManager.startEventTower(player);
					return null;
				}
				case 3:
				{
					HeavenlyRiftManager.startEvent40Angels(player);
					break;
				}
			}
			return null;
		}
		
		if (!event.equals("finish"))
		{
			return null;
		}
		
		if (player.isInParty())
		{
			Party party = player.getParty();
			if (!party.isLeader(player))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_PROCEED_AS_YOU_ARE_NOT_A_PARTY_LEADER);
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
			GlobalVariablesManager.getInstance().set("heavenly_rift_reward", 0);
			for (Player partyMember : party.getMembers())
			{
				partyMember.teleToLocation(114264, 13352, -5104);
			}
			return null;
		}
		
		if (player.isGM())
		{
			GlobalVariablesManager.getInstance().set("heavenly_rift_complete", 0);
			player.teleToLocation(114264, 13352, -5104);
			return null;
		}
		
		player.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_A_PARTY_AND_CAN_T_SEND_MESSAGES_TO_THE_PARTY_CHAT);
		return null;
	}
	
	public static void main(String[] args)
	{
		new Arushinai();
	}
}
