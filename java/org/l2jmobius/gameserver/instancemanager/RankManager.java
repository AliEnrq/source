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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.PetDataTable;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.olympiad.Hero;

/**
 * @author NviX
 */
public class RankManager
{
	private static final Logger LOGGER = Logger.getLogger(RankManager.class.getName());
	
	public static final Long TIME_LIMIT = 2592000000L; // 30 days in milliseconds
	public static final long CURRENT_TIME = System.currentTimeMillis();
	public static final int PLAYER_LIMIT = 500;
	
	private static final String SELECT_CHARACTERS = "SELECT charId,char_name,level,race,base_class, clanid FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 ORDER BY exp DESC, onlinetime DESC LIMIT " + PLAYER_LIMIT;
	private static final String SELECT_CHARACTERS_PVP = "SELECT charId,char_name,level,race,base_class, clanid, deaths, kills, pvpkills FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 ORDER BY kills DESC, onlinetime DESC LIMIT " + PLAYER_LIMIT;
	private static final String SELECT_CHARACTERS_BY_RACE = "SELECT charId FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 AND race = ? ORDER BY exp DESC, onlinetime DESC LIMIT " + PLAYER_LIMIT;
	private static final String SELECT_PETS = "SELECT characters.charId, pets.exp, characters.char_name, pets.level as petLevel, characters.level as charLevel, characters.clanId, pet_evolves.index, pets.item_obj_id FROM characters, pets, pet_evolves WHERE pets.ownerId = characters.charId AND pet_evolves.itemObjId = pets.item_obj_id AND (" + CURRENT_TIME + " - cast(characters.lastAccess as signed) < " + TIME_LIMIT + ") AND characters.accesslevel = 0 AND pets.level > 39 ORDER BY pets.exp DESC, characters.onlinetime DESC LIMIT " + PLAYER_LIMIT;
	private static final String SELECT_CLANS = "SELECT characters.level, characters.char_name, clan_data.clan_id, clan_data.clan_level, clan_data.clan_name, clan_data.reputation_score, clan_data.exp FROM characters, clan_data WHERE characters.charId = clan_data.leader_id AND characters.clanid = clan_data.clan_id AND dissolving_expiry_time = 0 ORDER BY exp DESC LIMIT " + PLAYER_LIMIT;
	
	private static final String GET_CURRENT_CYCLE_DATA = "SELECT characters.char_name, characters.level, characters.base_class, characters.clanid, olympiad_nobles.charId, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost FROM characters, olympiad_nobles WHERE characters.charId = olympiad_nobles.charId ORDER BY olympiad_nobles.olympiad_points DESC LIMIT " + PLAYER_LIMIT;
	private static final String GET_CHARACTERS_BY_CLASS = "SELECT charId FROM characters WHERE (" + CURRENT_TIME + " - cast(lastAccess as signed) < " + TIME_LIMIT + ") AND accesslevel = 0 AND level > 39 AND characters.base_class = ? ORDER BY exp DESC, onlinetime DESC LIMIT " + PLAYER_LIMIT;
	
	private final Map<Integer, StatSet> _mainList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainOlyList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotOlyList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainPvpList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotPvpList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainPetList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotPetList = new ConcurrentHashMap<>();
	private final Map<Integer, StatSet> _mainClanList = new ConcurrentHashMap<>();
	private Map<Integer, StatSet> _snapshotClanList = new ConcurrentHashMap<>();
	
	protected RankManager()
	{
		ThreadPool.scheduleAtFixedRate(this::update, 0, 1800000);
	}
	
	private synchronized void update()
	{
		// Load charIds All
		_snapshotList = _mainList;
		_mainList.clear();
		_snapshotOlyList = _mainOlyList;
		_mainOlyList.clear();
		_snapshotPvpList = _mainPvpList;
		_mainPvpList.clear();
		_snapshotPetList = _mainPetList;
		_mainPetList.clear();
		_snapshotClanList = _mainClanList;
		_mainClanList.clear();
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				int i = 1;
				while (rset.next())
				{
					final StatSet player = new StatSet();
					final int charId = rset.getInt("charId");
					final int classId = rset.getInt("base_class");
					player.set("charId", charId);
					player.set("name", rset.getString("char_name"));
					player.set("level", rset.getInt("level"));
					player.set("classId", rset.getInt("base_class"));
					final int race = rset.getInt("race");
					player.set("race", race);
					
					loadRaceRank(charId, race, player);
					loadClassRank(charId, classId, player);
					final int clanId = rset.getInt("clanid");
					if (clanId > 0)
					{
						player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
					}
					else
					{
						player.set("clanName", "");
					}
					
					_mainList.put(i, player);
					i++;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load chars total rank data: " + this + " - " + e.getMessage(), e);
		}
		
		// load olympiad data.
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(GET_CURRENT_CYCLE_DATA))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				int i = 1;
				while (rset.next())
				{
					final StatSet player = new StatSet();
					final int charId = rset.getInt("charId");
					player.set("charId", charId);
					player.set("name", rset.getString("char_name"));
					final int clanId = rset.getInt("clanid");
					if (clanId > 0)
					{
						player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
					}
					else
					{
						player.set("clanName", "");
					}
					player.set("level", rset.getInt("level"));
					final int classId = rset.getInt("base_class");
					player.set("classId", classId);
					if (clanId > 0)
					{
						player.set("clanLevel", ClanTable.getInstance().getClan(clanId).getLevel());
					}
					else
					{
						player.set("clanLevel", 0);
					}
					player.set("competitions_won", rset.getInt("competitions_won"));
					player.set("competitions_lost", rset.getInt("competitions_lost"));
					player.set("olympiad_points", rset.getInt("olympiad_points"));
					
					if (Hero.getInstance().getCompleteHeroes().containsKey(charId))
					{
						final StatSet hero = Hero.getInstance().getCompleteHeroes().get(charId);
						player.set("count", hero.getInt("count", 0));
						player.set("legend_count", hero.getInt("legend_count", 0));
					}
					else
					{
						player.set("count", 0);
						player.set("legend_count", 0);
					}
					
					loadClassRank(charId, classId, player);
					
					_mainOlyList.put(i, player);
					i++;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load olympiad total rank data: " + this + " - " + e.getMessage(), e);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTERS_PVP))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				int i = 1;
				while (rset.next())
				{
					final StatSet player = new StatSet();
					final int charId = rset.getInt("charId");
					player.set("charId", charId);
					player.set("name", rset.getString("char_name"));
					player.set("level", rset.getInt("level"));
					player.set("classId", rset.getInt("base_class"));
					final int race = rset.getInt("race");
					player.set("race", race);
					player.set("kills", rset.getInt("kills"));
					player.set("deaths", rset.getInt("deaths"));
					player.set("points", rset.getInt("pvpkills"));
					loadRaceRank(charId, race, player);
					final int clanId = rset.getInt("clanid");
					if (clanId > 0)
					{
						player.set("clanName", ClanTable.getInstance().getClan(clanId).getName());
					}
					else
					{
						player.set("clanName", "");
					}
					
					_mainPvpList.put(i, player);
					i++;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load pvp total rank data: " + this + " - " + e.getMessage(), e);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_PETS))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				int i = 1;
				while (rset.next())
				{
					final StatSet player = new StatSet();
					final int charId = rset.getInt("charId");
					player.set("charId", charId);
					player.set("name", rset.getString("char_name"));
					player.set("level", rset.getInt("charLevel"));
					player.set("petLevel", rset.getInt("petLevel"));
					player.set("clanName", rset.getInt("clanid") > 0 ? ClanTable.getInstance().getClan(rset.getInt("clanid")).getName() : "");
					player.set("petType", PetDataTable.getInstance().getTypeByIndex(rset.getInt("index")));
					player.set("exp", rset.getLong("exp"));
					player.set("controlledItemObjId", rset.getInt("item_obj_id"));
					_mainPetList.put(i, player);
					i++;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load pet total rank data: " + this + " - " + e.getMessage(), e);
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CLANS))
		{
			try (ResultSet rset = statement.executeQuery())
			{
				int i = 1;
				while (rset.next())
				{
					final StatSet player = new StatSet();
					player.set("char_name", rset.getString("char_name"));
					player.set("level", rset.getInt("level"));
					player.set("clan_level", rset.getInt("clan_level"));
					player.set("clan_name", rset.getString("clan_name"));
					player.set("reputation_score", rset.getInt("reputation_score"));
					player.set("exp", rset.getLong("exp"));
					player.set("clan_id", rset.getInt("clan_id"));
					_mainClanList.put(i, player);
					i++;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load clan total rank data: " + this + " - " + e.getMessage(), e);
		}
	}
	
	private void loadClassRank(int charId, int classId, StatSet player)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(GET_CHARACTERS_BY_CLASS))
		{
			ps.setInt(1, classId);
			try (ResultSet rset = ps.executeQuery())
			{
				int i = 0;
				while (rset.next())
				{
					if (rset.getInt("charId") == charId)
					{
						player.set("classRank", i + 1);
					}
					i++;
				}
				if (i == 0)
				{
					player.set("classRank", 0);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load chars classId olympiad rank data: " + this + " - " + e.getMessage(), e);
		}
	}
	
	private void loadRaceRank(int charId, int race, StatSet player)
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT_CHARACTERS_BY_RACE))
		{
			ps.setInt(1, race);
			try (ResultSet rset = ps.executeQuery())
			{
				int i = 0;
				while (rset.next())
				{
					if (rset.getInt("charId") == charId)
					{
						player.set("raceRank", i + 1);
					}
					i++;
				}
				if (i == 0)
				{
					player.set("raceRank", 0);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not load chars race rank data: " + this + " - " + e.getMessage(), e);
		}
	}
	
	public Map<Integer, StatSet> getRankList()
	{
		return _mainList;
	}
	
	public Map<Integer, StatSet> getSnapshotList()
	{
		return _snapshotList;
	}
	
	public Map<Integer, StatSet> getOlyRankList()
	{
		return _mainOlyList;
	}
	
	public Map<Integer, StatSet> getSnapshotOlyList()
	{
		return _snapshotOlyList;
	}
	
	public Map<Integer, StatSet> getPvpRankList()
	{
		return _mainPvpList;
	}
	
	public Map<Integer, StatSet> getSnapshotPvpRankList()
	{
		return _snapshotPvpList;
	}
	
	public Map<Integer, StatSet> getPetRankList()
	{
		return _mainPetList;
	}
	
	public Map<Integer, StatSet> getSnapshotPetRankList()
	{
		return _snapshotPetList;
	}
	
	public Map<Integer, StatSet> getClanRankList()
	{
		return _mainClanList;
	}
	
	public Map<Integer, StatSet> getSnapshotClanRankList()
	{
		return _snapshotClanList;
	}
	
	public int getPlayerGlobalRank(Player player)
	{
		final int playerOid = player.getObjectId();
		for (Entry<Integer, StatSet> entry : _mainList.entrySet())
		{
			final StatSet stats = entry.getValue();
			if (stats.getInt("charId") != playerOid)
			{
				continue;
			}
			return entry.getKey();
		}
		return 0;
	}
	
	public int getPlayerRaceRank(Player player)
	{
		final int playerOid = player.getObjectId();
		for (StatSet stats : _mainList.values())
		{
			if (stats.getInt("charId") != playerOid)
			{
				continue;
			}
			return stats.getInt("raceRank");
		}
		return 0;
	}
	
	public int getPlayerClassRank(Player player)
	{
		final int playerOid = player.getObjectId();
		for (StatSet stats : _mainList.values())
		{
			if (stats.getInt("charId") != playerOid)
			{
				continue;
			}
			return stats.getInt("classRank");
		}
		return 0;
	}
	
	public static RankManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RankManager INSTANCE = new RankManager();
	}
}