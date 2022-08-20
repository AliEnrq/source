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
package ai.others.ProvisionalHalls;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.gameserver.data.xml.ClanHallData;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.residences.ClanHall;

import ai.AbstractNpcAI;

/**
 * Custom implementation for Provisional Clan Halls.
 * @author Mobius
 */
public class ProvisionalHalls extends AbstractNpcAI
{
	// NPCs
	private static final int KERRY = 33359;
	private static final int MAID = 33360;
	// Misc
	private static final int HALL_PRICE = 50000000;
	private static final long TWO_WEEKS = 1209600000;
	private static final Map<Integer, Location> CLAN_HALLS = new LinkedHashMap<>();
	static
	{
		CLAN_HALLS.put(187, new Location(-122200, -116552, -5798, 1779));
		CLAN_HALLS.put(186, new Location(-122264, -122392, -5870, 15229));
		CLAN_HALLS.put(188, new Location(-121864, -111240, -6014, 30268));
		CLAN_HALLS.put(190, new Location(-117080, -116551, -5771, 1779));
		CLAN_HALLS.put(189, new Location(-117000, -122052, -5845, 15229));
		CLAN_HALLS.put(191, new Location(-117074, -111237, -5989, 30268));
		CLAN_HALLS.put(193, new Location(-111717, -116550, -5773, 1779));
		CLAN_HALLS.put(192, new Location(-111726, -122378, -5845, 15229));
		CLAN_HALLS.put(194, new Location(-111158, -111230, -5989, 30268));
	}
	private static final String HALL_OWNER_VAR = "PCH_OWNER_";
	private static final String HALL_TIME_VAR = "PCH_TIME_";
	private static final String HALL_RESET_VAR = "PCH_RESET_";
	private static final String HALL_RETURN_VAR = "PCH_RETURN";
	private static final Object LOCK = new Object();
	
	private ProvisionalHalls()
	{
		addStartNpc(KERRY);
		addFirstTalkId(KERRY);
		addTalkId(KERRY);
		
		for (int id : CLAN_HALLS.keySet())
		{
			final long resetTime = GlobalVariablesManager.getInstance().getLong(HALL_TIME_VAR + id, 0);
			if (resetTime > 0)
			{
				cancelQuestTimers(HALL_RESET_VAR + id);
				startQuestTimer(HALL_RESET_VAR + id, Math.max(1000, (TWO_WEEKS - (System.currentTimeMillis() - resetTime) - 30000)), null, null);
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if (event.equals("33359-01.html") || event.equals("33359-02.html") || event.equals("33359-03.html"))
		{
			htmltext = event;
		}
		else if (event.equals("buy"))
		{
			if ((npc == null) || (npc.getId() != KERRY))
			{
				return null;
			}
			
			synchronized (LOCK)
			{
				final Calendar calendar = Calendar.getInstance();
				final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
				if ((player.getClan() == null) || (player.getClan().getLeaderId() != player.getObjectId()))
				{
					player.sendMessage("You need to be a clan leader in order to proceed.");
				}
				else if ((player.getClan().getHideoutId() > 0))
				{
					player.sendMessage("You already own a hideout.");
				}
				else if ((dayOfWeek != Calendar.SATURDAY) && (dayOfWeek != Calendar.SUNDAY))
				{
					htmltext = "33359-02.html";
				}
				else if (player.getAdena() < HALL_PRICE)
				{
					player.sendMessage("You need " + HALL_PRICE + " adena in order to proceed.");
				}
				else
				{
					if (dayOfWeek != Calendar.SATURDAY)
					{
						calendar.add(Calendar.DAY_OF_WEEK, -1);
					}
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 1);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 0);
					
					for (int id : CLAN_HALLS.keySet())
					{
						if ((GlobalVariablesManager.getInstance().getInt(HALL_OWNER_VAR + id, 0) == 0) && ((GlobalVariablesManager.getInstance().getLong(HALL_TIME_VAR + id, 0) + TWO_WEEKS) < System.currentTimeMillis()))
						{
							player.reduceAdena("ProvisionalHall", HALL_PRICE, player, true);
							GlobalVariablesManager.getInstance().set(HALL_OWNER_VAR + id, player.getClanId());
							GlobalVariablesManager.getInstance().set(HALL_TIME_VAR + id, calendar.getTimeInMillis());
							final ClanHall clanHall = ClanHallData.getInstance().getClanHallById(id);
							if (clanHall != null)
							{
								clanHall.setOwner(player.getClan());
							}
							player.sendMessage("Congratulations! You now own a provisional clan hall!");
							startQuestTimer("RESET_ORCHID_HALL", TWO_WEEKS - (System.currentTimeMillis() - calendar.getTimeInMillis()), null, null);
							return null;
						}
					}
					player.sendMessage("I am sorry, all halls have been taken.");
				}
			}
		}
		else if (event.equals("enter"))
		{
			if ((npc == null) || (npc.getId() != KERRY))
			{
				return null;
			}
			
			final int playerClanId = player.getClanId();
			for (Entry<Integer, Location> hall : CLAN_HALLS.entrySet())
			{
				if (playerClanId == GlobalVariablesManager.getInstance().getInt(HALL_OWNER_VAR + hall.getKey(), -1))
				{
					player.getVariables().set(HALL_RETURN_VAR, player.getX() + "," + player.getY() + "," + player.getZ() + "," + player.getHeading());
					player.teleToLocation(hall.getValue());
					return null;
				}
			}
			htmltext = "33359-02.html";
		}
		else if (event.equals("leave"))
		{
			if ((npc == null) || (npc.getId() != MAID))
			{
				return null;
			}
			
			final String[] location = player.getVariables().getString(HALL_RETURN_VAR, "-83246,242118,-3730,-1").split(",");
			player.teleToLocation(Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]), Integer.parseInt(location[3]));
		}
		else if (event.startsWith(HALL_RESET_VAR))
		{
			final String id = event.replace(HALL_RESET_VAR, "");
			if (((GlobalVariablesManager.getInstance().getLong(HALL_TIME_VAR + id, 0) + TWO_WEEKS) - 60000) <= System.currentTimeMillis())
			{
				final int clanId = GlobalVariablesManager.getInstance().getInt(HALL_OWNER_VAR + id, 0);
				if (clanId > 0)
				{
					final ClanHall clanHall = ClanHallData.getInstance().getClanHallById(Integer.parseInt(id));
					if (clanHall != null)
					{
						clanHall.setOwner(null);
					}
				}
				GlobalVariablesManager.getInstance().remove(HALL_TIME_VAR + id);
				GlobalVariablesManager.getInstance().remove(HALL_OWNER_VAR + id);
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		final Calendar calendar = Calendar.getInstance();
		final int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		if ((dayOfWeek != Calendar.SATURDAY) && (dayOfWeek != Calendar.SUNDAY))
		{
			return "33359-01.html";
		}
		return "33359-01b.html";
	}
	
	public static void main(String[] args)
	{
		new ProvisionalHalls();
	}
}