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
package ai.areas.DragonValley.Fellow;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.NpcSay;

import ai.AbstractNpcAI;

/**
 * @author RobikBobik
 */
public class Fellow extends AbstractNpcAI
{
	// NPC
	private static final int FELLOW = 31713;
	// Location - Antharas Heart
	private static final Location TELEPORT_LOC = new Location(154376, 121290, -3807);
	// Misc
	private static final NpcStringId[] TEXT =
	{
		NpcStringId.LET_S_JOIN_OUR_FORCES_AND_FACE_THIS_TOGETHER,
		NpcStringId.BALTHUS_KNIGHTS_ARE_LOOKING_FOR_MERCENARIES
	};
	
	private Fellow()
	{
		addFirstTalkId(FELLOW);
		addTalkId(FELLOW);
		addSpawnId(FELLOW);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "teleToAntharas":
			{
				if ((player.getCommandChannel() == null) || (player.getCommandChannel().getLeader() != player) || (player.getCommandChannel().getMemberCount() < 27) || (player.getCommandChannel().getMemberCount() > 300))
				{
					return "31713-01.html";
				}
				for (Player member : player.getCommandChannel().getMembers())
				{
					if ((member != null) && (member.getLevel() > 70))
					{
						member.teleToLocation(TELEPORT_LOC);
					}
				}
				break;
			}
			case "CHAT_TIMER":
			{
				npc.broadcastPacket(new NpcSay(npc, ChatType.NPC_GENERAL, TEXT[getRandom(TEXT.length)]));
				startQuestTimer("CHAT_TIMER", 30000, npc, null);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "31713.html";
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("CHAT_TIMER", 5000, npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Fellow();
	}
}
