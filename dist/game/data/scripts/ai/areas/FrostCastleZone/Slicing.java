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
package ai.areas.FrostCastleZone;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;

import ai.AbstractNpcAI;

/**
 * @author Serenitty
 */
public class Slicing extends AbstractNpcAI
{
	// NPCs
	private static final int SLICING = 25943;
	private static final int CHARGED_CRYSTAL = 34232;
	
	private Slicing()
	{
		addKillId(SLICING);
		
		// Slicing door.
		ThreadPool.scheduleAtFixedRate(() ->
		{
			try
			{
				if (getTimeHour() == 22)
				{
					addSpawn(SLICING, 146316, 141503, -11854, 49151, false, 0, true);
					// LOGGER.info("Slicing door FrostCastle opened, game time 22.00.");
					DoorData.getInstance().getDoor(24220002).openMe();
					ThreadPool.schedule(() ->
					{
						try
						{
							// LOGGER.info("Slicing door FrostCastle id closed.");
							DoorData.getInstance().getDoor(24220002).closeMe();
						}
						catch (Throwable e)
						{
							// LOGGER.warning("Cannot close door ID: 24220002 " + e);
						}
					}, 1800000);
				}
			}
			catch (Throwable e)
			{
				// LOGGER.warning("Cannot open door ID: 24220002 " + e);
			}
		}, 2000, 600000);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		addSpawn(CHARGED_CRYSTAL, 146316, 141503, -11854, 49151, false, 0, true, killer.getInstanceId());
		return super.onKill(npc, killer, isSummon);
	}
	
	public int getTimeHour()
	{
		return (GameTimeTaskManager.getInstance().getGameTime() / 60) % 24;
	}
	
	public static void main(String[] args)
	{
		new Slicing();
	}
}