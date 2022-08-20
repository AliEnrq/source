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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.data.xml.LimitShopData;
import org.l2jmobius.gameserver.data.xml.TimedHuntingZoneData;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.stat.PlayerStat;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.model.holders.LimitShopProductHolder;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.model.holders.TimedHuntingZoneHolder;
import org.l2jmobius.gameserver.model.olympiad.Olympiad;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.vip.VipManager;
import org.l2jmobius.gameserver.network.serverpackets.ExVoteSystemInfo;
import org.l2jmobius.gameserver.network.serverpackets.ExWorldChatCnt;

/**
 * @author UnAfraid
 */
public class DailyTaskManager
{
	private static final Logger LOGGER = Logger.getLogger(DailyTaskManager.class.getName());
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM HH:mm");
	private static final int[] RESET_SKILLS =
	{
		2510, // Wondrous Cubic
		22180, // Wondrous Cubic - 1 time use
	};
	
	protected DailyTaskManager()
	{
		// Schedule reset everyday at 6:30.
		final long currentTime = System.currentTimeMillis();
		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 6);
		calendar.set(Calendar.MINUTE, 30);
		calendar.set(Calendar.SECOND, 0);
		if (calendar.getTimeInMillis() < currentTime)
		{
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		
		// Check if 24 hours have passed since the last daily reset.
		final long calendarTime = calendar.getTimeInMillis();
		if (GlobalVariablesManager.getInstance().getLong(GlobalVariablesManager.DAILY_TASK_RESET, 0) < calendarTime)
		{
			LOGGER.info(getClass().getSimpleName() + ": Next schedule at " + SDF.format(new Date(calendarTime)) + ".");
		}
		else
		{
			LOGGER.info(getClass().getSimpleName() + ": Daily task will run now.");
			onReset();
		}
		
		// Daily reset task.
		final long startDelay = Math.max(0, calendarTime - currentTime);
		ThreadPool.scheduleAtFixedRate(this::onReset, startDelay, 86400000); // 86400000 = 1 day
		
		// Global save task.
		ThreadPool.scheduleAtFixedRate(this::onSave, 1800000, 1800000); // 1800000 = 30 minutes
	}
	
	private void onReset()
	{
		// Store last reset time.
		GlobalVariablesManager.getInstance().set(GlobalVariablesManager.DAILY_TASK_RESET, System.currentTimeMillis());
		
		// Wednesday weekly tasks.
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)
		{
			clanLeaderApply();
			resetMonsterArenaWeekly();
			resetTimedHuntingZonesWeekly();
			resetVitalityWeekly();
		}
		else // All days, except Wednesday.
		{
			resetVitalityDaily();
		}
		
		// Daily tasks.
		resetClanBonus();
		resetClanContributionList();
		resetClanDonationPoints();
		resetDailySkills();
		resetDailyLimitShopData();
		resetWorldChatPoints();
		resetRecommends();
		resetTrainingCamp();
		resetTimedHuntingZones();
		resetDailyMissionRewards();
		resetAttendanceRewards();
		resetVip();
	}
	
	private void onSave()
	{
		GlobalVariablesManager.getInstance().storeMe();
		
		if (Olympiad.getInstance().inCompPeriod())
		{
			Olympiad.getInstance().saveOlympiadStatus();
			LOGGER.info("Olympiad System: Data updated.");
		}
	}
	
	private void clanLeaderApply()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getNewLeaderId() != 0)
			{
				final ClanMember member = clan.getClanMember(clan.getNewLeaderId());
				if (member == null)
				{
					continue;
				}
				
				clan.setNewLeader(member);
			}
		}
		LOGGER.info("Clan leaders has been updated.");
	}
	
	private void resetClanContributionList()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			clan.getVariables().deleteWeeklyContribution();
		}
	}
	
	private void resetVitalityDaily()
	{
		if (!Config.ENABLE_VITALITY)
		{
			return;
		}
		
		int vitality = PlayerStat.MAX_VITALITY_POINTS / 4;
		for (Player player : World.getInstance().getPlayers())
		{
			final int VP = player.getVitalityPoints();
			player.setVitalityPoints(VP + vitality, false);
			for (SubClassHolder subclass : player.getSubClasses().values())
			{
				final int VPS = subclass.getVitalityPoints();
				subclass.setVitalityPoints(VPS + vitality);
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("UPDATE character_subclasses SET vitality_points = IF(vitality_points = ?, vitality_points, vitality_points + ?)"))
			{
				st.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
				st.setInt(2, PlayerStat.MAX_VITALITY_POINTS / 4);
				st.execute();
			}
			
			try (PreparedStatement st = con.prepareStatement("UPDATE characters SET vitality_points = IF(vitality_points = ?, vitality_points, vitality_points + ?)"))
			{
				st.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
				st.setInt(2, PlayerStat.MAX_VITALITY_POINTS / 4);
				st.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while updating vitality", e);
		}
		LOGGER.info("Daily Vitality Added");
	}
	
	private void resetVitalityWeekly()
	{
		if (!Config.ENABLE_VITALITY)
		{
			return;
		}
		
		for (Player player : World.getInstance().getPlayers())
		{
			player.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS, false);
			for (SubClassHolder subclass : player.getSubClasses().values())
			{
				subclass.setVitalityPoints(PlayerStat.MAX_VITALITY_POINTS);
			}
		}
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement st = con.prepareStatement("UPDATE character_subclasses SET vitality_points = ?"))
			{
				st.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
				st.execute();
			}
			
			try (PreparedStatement st = con.prepareStatement("UPDATE characters SET vitality_points = ?"))
			{
				st.setInt(1, PlayerStat.MAX_VITALITY_POINTS);
				st.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while updating vitality", e);
		}
		LOGGER.info("Vitality resetted");
	}
	
	private void resetMonsterArenaWeekly()
	{
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			GlobalVariablesManager.getInstance().remove(GlobalVariablesManager.MONSTER_ARENA_VARIABLE + clan.getId());
		}
	}
	
	private void resetClanBonus()
	{
		ClanTable.getInstance().getClans().forEach(Clan::resetClanBonus);
		LOGGER.info("Daily clan bonus has been resetted.");
	}
	
	private void resetDailySkills()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			for (int skill : RESET_SKILLS)
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE skill_id=?;"))
				{
					ps.setInt(1, skill);
					ps.execute();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not reset daily skill reuse: ", e);
		}
		LOGGER.info("Daily skill reuse cleaned.");
	}
	
	private void resetClanDonationPoints()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_pledge_donation WHERE points < ?"))
			{
				ps.setInt(1, 4);
				ps.execute();
			}
			for (Player player : World.getInstance().getPlayers())
			{
				player.setClanDonationPoints(3);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not reset clan donation points: ", e);
		}
		LOGGER.info("Weekly caln contributions cleaned.");
	}
	
	private void resetWorldChatPoints()
	{
		if (!Config.ENABLE_WORLD_CHAT)
		{
			return;
		}
		
		// Update data for offline players.
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_variables SET val = ? WHERE var = ?"))
		{
			ps.setInt(1, 0);
			ps.setString(2, PlayerVariables.WORLD_CHAT_VARIABLE_NAME);
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not reset daily world chat points: ", e);
		}
		
		// Update data for online players.
		for (Player player : World.getInstance().getPlayers())
		{
			player.setWorldChatUsed(0);
			player.sendPacket(new ExWorldChatCnt(player));
			player.getVariables().storeMe();
		}
		
		LOGGER.info("Daily world chat points has been resetted.");
	}
	
	private void resetRecommends()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left = ?, rec_have = 0 WHERE rec_have <= 20"))
			{
				ps.setInt(1, 0); // Rec left = 0
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement("UPDATE character_reco_bonus SET rec_left = ?, rec_have = GREATEST(rec_have - 20,0) WHERE rec_have > 20"))
			{
				ps.setInt(1, 0); // Rec left = 0
				ps.execute();
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not reset Recommendations System: ", e);
		}
		
		for (Player player : World.getInstance().getPlayers())
		{
			player.setRecomLeft(0);
			player.setRecomHave(player.getRecomHave() - 20);
			player.sendPacket(new ExVoteSystemInfo(player));
			player.broadcastUserInfo();
		}
	}
	
	private void resetTrainingCamp()
	{
		if (Config.TRAINING_CAMP_ENABLE)
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ?"))
			{
				ps.setString(1, "TRAINING_CAMP_DURATION");
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Could not reset Training Camp: ", e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.resetTraingCampDuration();
				player.getAccountVariables().storeMe();
			}
			
			LOGGER.info("Training Camp daily time has been resetted.");
		}
	}
	
	private void resetVip()
	{
		// Delete all entries for received gifts
		AccountVariables.deleteVipPurchases(AccountVariables.VIP_ITEM_BOUGHT);
		
		// Checks the tier expiration for online players
		// offline players get handled on next time they log in.
		for (Player player : World.getInstance().getPlayers())
		{
			if (player.getVipTier() > 0)
			{
				VipManager.getInstance().checkVipTierExpiration(player);
			}
			
			player.getAccountVariables().restoreMe();
		}
	}
	
	private void resetDailyMissionRewards()
	{
		DailyMissionData.getInstance().getDailyMissionData().forEach(DailyMissionDataHolder::reset);
	}
	
	public void resetTimedHuntingZones()
	{
		for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
		{
			if (holder.isWeekly())
			{
				continue;
			}
			
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var IN (?, ?, ?)"))
			{
				ps.setString(1, PlayerVariables.HUNTING_ZONE_ENTRY + holder.getZoneId());
				ps.setString(2, PlayerVariables.HUNTING_ZONE_TIME + holder.getZoneId());
				ps.setString(3, PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + holder.getZoneId());
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Could not reset Special Hunting Zones: ", e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.getVariables().remove(PlayerVariables.HUNTING_ZONE_ENTRY + holder.getZoneId());
				player.getVariables().remove(PlayerVariables.HUNTING_ZONE_TIME + holder.getZoneId());
				player.getVariables().remove(PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + holder.getZoneId());
				player.getVariables().storeMe();
			}
		}
		
		LOGGER.info("Special Hunting Zones has been resetted.");
	}
	
	public void resetTimedHuntingZonesWeekly()
	{
		for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
		{
			if (!holder.isWeekly())
			{
				continue;
			}
			
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var IN (?, ?, ?)"))
			{
				ps.setString(1, PlayerVariables.HUNTING_ZONE_ENTRY + holder.getZoneId());
				ps.setString(2, PlayerVariables.HUNTING_ZONE_TIME + holder.getZoneId());
				ps.setString(3, PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + holder.getZoneId());
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Could not reset Weekly Special Hunting Zones: ", e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.getVariables().remove(PlayerVariables.HUNTING_ZONE_ENTRY + holder.getZoneId());
				player.getVariables().remove(PlayerVariables.HUNTING_ZONE_TIME + holder.getZoneId());
				player.getVariables().remove(PlayerVariables.HUNTING_ZONE_REMAIN_REFILL_ + holder.getZoneId());
				player.getVariables().storeMe();
			}
		}
		
		LOGGER.info("Weekly Special Hunting Zones has been resetted.");
	}
	
	public void resetAttendanceRewards()
	{
		if (Config.ATTENDANCE_REWARDS_SHARE_ACCOUNT)
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var=?"))
				{
					ps.setString(1, "ATTENDANCE_DATE");
					ps.execute();
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Could not reset Attendance Rewards: " + e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.getAccountVariables().remove("ATTENDANCE_DATE");
				player.getAccountVariables().storeMe();
			}
			
			LOGGER.info("Account shared Attendance Rewards has been resetted.");
		}
		else
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection())
			{
				try (PreparedStatement ps = con.prepareStatement("DELETE FROM character_variables WHERE var=?"))
				{
					ps.setString(1, PlayerVariables.ATTENDANCE_DATE);
					ps.execute();
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Could not reset Attendance Rewards: " + e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.getVariables().remove(PlayerVariables.ATTENDANCE_DATE);
				player.getVariables().storeMe();
			}
			
			LOGGER.info("Attendance Rewards has been resetted.");
		}
	}
	
	public void resetDailyLimitShopData()
	{
		for (LimitShopProductHolder holder : LimitShopData.getInstance().getProducts())
		{
			// Update data for offline players.
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM account_gsdata WHERE var=?"))
			{
				ps.setString(1, AccountVariables.LCOIN_SHOP_PRODUCT_DAILY_COUNT + holder.getProductionId());
				ps.executeUpdate();
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, getClass().getSimpleName() + ": Could not reset LimitShopData: " + e);
			}
			
			// Update data for online players.
			for (Player player : World.getInstance().getPlayers())
			{
				player.getVariables().remove(AccountVariables.LCOIN_SHOP_PRODUCT_DAILY_COUNT + holder.getProductionId());
				player.getAccountVariables().storeMe();
			}
		}
		LOGGER.info("LimitShopData has been resetted.");
	}
	
	public static DailyTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DailyTaskManager INSTANCE = new DailyTaskManager();
	}
}
