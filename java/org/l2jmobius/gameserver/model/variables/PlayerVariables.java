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
package org.l2jmobius.gameserver.model.variables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;

/**
 * @author UnAfraid
 */
public class PlayerVariables extends AbstractVariables
{
	private static final Logger LOGGER = Logger.getLogger(PlayerVariables.class.getName());
	
	// SQL Queries.
	private static final String SELECT_QUERY = "SELECT * FROM character_variables WHERE charId = ?";
	private static final String DELETE_QUERY = "DELETE FROM character_variables WHERE charId = ?";
	private static final String INSERT_QUERY = "INSERT INTO character_variables (charId, var, val) VALUES (?, ?, ?)";
	
	// Public variable names
	public static final String INSTANCE_ORIGIN = "INSTANCE_ORIGIN";
	public static final String HAIR_ACCESSORY_VARIABLE_NAME = "HAIR_ACCESSORY_ENABLED";
	public static final String WORLD_CHAT_VARIABLE_NAME = "WORLD_CHAT_USED";
	public static final String VITALITY_ITEMS_USED_VARIABLE_NAME = "VITALITY_ITEMS_USED";
	public static final String ATTENDANCE_DATE = "ATTENDANCE_DATE";
	public static final String ATTENDANCE_INDEX = "ATTENDANCE_INDEX";
	public static final String ABILITY_POINTS_MAIN_CLASS = "ABILITY_POINTS";
	public static final String ABILITY_POINTS_DUAL_CLASS = "ABILITY_POINTS_DUAL_CLASS";
	public static final String ABILITY_POINTS_USED_MAIN_CLASS = "ABILITY_POINTS_USED";
	public static final String ABILITY_POINTS_USED_DUAL_CLASS = "ABILITY_POINTS_DUAL_CLASS_USED";
	public static final String REVELATION_SKILL_1_MAIN_CLASS = "RevelationSkill1";
	public static final String REVELATION_SKILL_2_MAIN_CLASS = "RevelationSkill2";
	public static final String REVELATION_SKILL_1_DUAL_CLASS = "DualclassRevelationSkill1";
	public static final String REVELATION_SKILL_2_DUAL_CLASS = "DualclassRevelationSkill2";
	public static final String LAST_PLEDGE_REPUTATION_LEVEL = "LAST_PLEDGE_REPUTATION_LEVEL";
	public static final String FORTUNE_TELLING_VARIABLE = "FortuneTelling";
	public static final String FORTUNE_TELLING_BLACK_CAT_VARIABLE = "FortuneTellingBlackCat";
	public static final String DELUSION_RETURN = "DELUSION_RETURN";
	public static final String AUTO_USE_SETTINGS = "AUTO_USE_SETTINGS";
	public static final String AUTO_USE_SHORTCUTS = "AUTO_USE_SHORTCUTS";
	public static final String HUNTING_ZONE_ENTRY = "HUNTING_ZONE_ENTRY_";
	public static final String HUNTING_ZONE_TIME = "HUNTING_ZONE_TIME_";
	public static final String HUNTING_ZONE_REMAIN_REFILL_ = "HUNTING_ZONE_REMAIN_REFILL_";
	public static final String SAYHA_GRACE_SUPPORT_ENDTIME = "SAYHA_GRACE_SUPPORT_ENDTIME";
	public static final String LIMITED_SAYHA_GRACE_ENDTIME = "LIMITED_SAYHA_GRACE_ENDTIME";
	public static final String MAGIC_LAMP_EXP = "MAGIC_LAMP_EXP";
	public static final String MAGIC_LAMP_COUNT = "MAGIC_LAMP_COUNT";
	public static final String DEATH_POINT_COUNT = "DEATH_POINT_COUNT";
	public static final String FAVORITE_TELEPORTS = "FAVORITE_TELEPORTS";
	public static final String ELIXIRS_AVAILABLE = "ELIXIRS_AVAILABLE";
	public static final String ELIXIRS_USED = "ELIXIRS_USED";
	public static final String STAT_POINTS = "STAT_POINTS";
	public static final String STAT_STR = "STAT_STR";
	public static final String STAT_DEX = "STAT_DEX";
	public static final String STAT_CON = "STAT_CON";
	public static final String STAT_INT = "STAT_INT";
	public static final String STAT_WIT = "STAT_WIT";
	public static final String STAT_MEN = "STAT_MEN";
	public static final String CLAN_JOIN_TIME = "CLAN_JOIN_TIME";
	public static final String HENNA1_DURATION = "HENNA1_DURATION";
	public static final String HENNA2_DURATION = "HENNA2_DURATION";
	public static final String HENNA3_DURATION = "HENNA3_DURATION";
	public static final String HENNA4_DURATION = "HENNA4_DURATION";
	public static final String DYE_POTENTIAL_DAILY_STEP = "DYE_POTENTIAL_DAILY_STEP";
	public static final String DYE_POTENTIAL_DAILY_COUNT = "DYE_POTENTIAL_DAILY_COUNT";
	public static final String DYE_POTENTIAL_DAILY_EXP = "DYE_POTENTIAL_DAILY_EXP";
	public static final String DYE_POTENTIAL_DAILY_LVL = "DYE_POTENTIAL_DAILY_LVL";
	
	private final int _objectId;
	
	public PlayerVariables(int objectId)
	{
		_objectId = objectId;
		restoreMe();
	}
	
	@Override
	public boolean restoreMe()
	{
		// Restore previous variables.
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement(SELECT_QUERY))
		{
			st.setInt(1, _objectId);
			try (ResultSet rset = st.executeQuery())
			{
				while (rset.next())
				{
					set(rset.getString("var"), rset.getString("val"));
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't restore variables for: " + _objectId, e);
			return false;
		}
		finally
		{
			compareAndSetChanges(true, false);
		}
		return true;
	}
	
	@Override
	public boolean storeMe()
	{
		// No changes, nothing to store.
		if (!hasChanges())
		{
			return false;
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			// Clear previous entries.
			try (PreparedStatement st = con.prepareStatement(DELETE_QUERY))
			{
				st.setInt(1, _objectId);
				st.execute();
			}
			
			// Insert all variables.
			try (PreparedStatement st = con.prepareStatement(INSERT_QUERY))
			{
				st.setInt(1, _objectId);
				for (Entry<String, Object> entry : getSet().entrySet())
				{
					st.setString(2, entry.getKey());
					st.setString(3, String.valueOf(entry.getValue()));
					st.addBatch();
				}
				st.executeBatch();
			}
		}
		catch (SQLException e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't update variables for: " + _objectId, e);
			return false;
		}
		finally
		{
			compareAndSetChanges(true, false);
		}
		return true;
	}
	
	@Override
	public boolean deleteMe()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			// Clear previous entries.
			try (PreparedStatement st = con.prepareStatement(DELETE_QUERY))
			{
				st.setInt(1, _objectId);
				st.execute();
			}
			
			// Clear all entries
			getSet().clear();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, getClass().getSimpleName() + ": Couldn't delete variables for: " + _objectId, e);
			return false;
		}
		return true;
	}
}
