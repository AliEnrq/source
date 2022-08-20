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
package events.DefeatTheQueen;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.model.skill.SkillCaster;

/**
 * @author Mobius
 */
public class DefeatTheQueen extends LongTimeEvent
{
	// NPCs
	private static final int ANTRI = 34184;
	private static final int JIO = 34185;
	// Skill
	private static final SkillHolder ANT_PROPHECY = new SkillHolder(48200, 1);
	// Teleport
	private static final Location ANT_NEST = new Location(-9997, 175533, -4152);
	// Misc
	private static final int MIN_LEVEL = 65;
	
	private DefeatTheQueen()
	{
		addStartNpc(ANTRI, JIO);
		addFirstTalkId(ANTRI, JIO);
		addTalkId(ANTRI, JIO);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (!isEventPeriod())
		{
			return null;
		}
		
		switch (event)
		{
			case "34184.htm":
			case "34184-1.htm":
			{
				return event;
			}
			case "TeleportToAntNest":
			{
				if ((npc != null) && (npc.getId() == ANTRI))
				{
					player.teleToLocation(ANT_NEST);
				}
				break;
			}
			case "ExitInstance":
			{
				if ((npc != null) && (npc.getId() == JIO) && npc.isInInstance())
				{
					npc.getInstanceWorld().ejectPlayer(player);
				}
				break;
			}
			case "GiveAntProphecy":
			{
				if ((npc != null) && (npc.getId() == JIO) && npc.isInInstance())
				{
					SkillCaster.triggerCast(npc, player, ANT_PROPHECY.getSkill());
				}
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (npc.getId() == JIO)
		{
			if (npc.isInInstance())
			{
				return "34185-2.htm";
			}
			if (player.getLevel() < MIN_LEVEL)
			{
				return "34185-1.htm";
			}
		}
		return npc.getId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new DefeatTheQueen();
	}
}
