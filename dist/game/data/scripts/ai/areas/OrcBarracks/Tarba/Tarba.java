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
package ai.areas.OrcBarracks.Tarba;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Tarba extends AbstractNpcAI
{
	// NPC
	private static final int TARBA = 34134;
	// Location
	private static final Location LOCATION = new Location(-93255, 109021, -3696);
	// Misc
	private static final String TARBA_TIME_VAR = "TARBA_TIME";
	
	private Tarba()
	{
		addStartNpc(TARBA);
		addTalkId(TARBA);
		addFirstTalkId(TARBA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("teleport"))
		{
			final long currentTime = System.currentTimeMillis();
			if ((npc.getId() == TARBA) && ((player.getVariables().getLong(TARBA_TIME_VAR, 0) + 86400000) < currentTime))
			{
				player.getVariables().set(TARBA_TIME_VAR, currentTime);
				player.teleToLocation(LOCATION);
				return null;
			}
			return "34134-02.htm";
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34134-01.htm";
	}
	
	public static void main(String[] args)
	{
		new Tarba();
	}
}