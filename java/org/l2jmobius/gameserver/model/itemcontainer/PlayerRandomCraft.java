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
package org.l2jmobius.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.RandomCraftData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.request.RandomCraftRequest;
import org.l2jmobius.gameserver.model.holders.RandomCraftRewardItemHolder;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExItemAnnounce;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.randomcraft.ExCraftInfo;
import org.l2jmobius.gameserver.network.serverpackets.randomcraft.ExCraftRandomInfo;
import org.l2jmobius.gameserver.network.serverpackets.randomcraft.ExCraftRandomMake;
import org.l2jmobius.gameserver.network.serverpackets.randomcraft.ExCraftRandomRefresh;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * @author Mode
 */
public class PlayerRandomCraft
{
	private static final Logger LOGGER = Logger.getLogger(PlayerRandomCraft.class.getName());
	
	public static final int MAX_FULL_CRAFT_POINTS = 99;
	public static final int MAX_CRAFT_POINTS = 1000000;
	
	private final Player _player;
	private final List<RandomCraftRewardItemHolder> _rewardList = new ArrayList<>(5);
	
	private int _fullCraftPoints = 0;
	private int _craftPoints = 0;
	private boolean _isSayhaRoll = false;
	
	public PlayerRandomCraft(Player player)
	{
		_player = player;
	}
	
	public void restore()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT * FROM character_random_craft WHERE charId=?"))
		{
			ps.setInt(1, _player.getObjectId());
			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					try
					{
						_fullCraftPoints = rs.getInt("random_craft_full_points");
						_craftPoints = rs.getInt("random_craft_points");
						_isSayhaRoll = rs.getBoolean("sayha_roll");
						for (int i = 1; i <= 5; i++)
						{
							final int itemId = rs.getInt("item_" + i + "_id");
							final long itemCount = rs.getLong("item_" + i + "_count");
							final boolean itemLocked = rs.getBoolean("item_" + i + "_locked");
							final int itemLockLeft = rs.getInt("item_" + i + "_lock_left");
							final RandomCraftRewardItemHolder holder = new RandomCraftRewardItemHolder(itemId, itemCount, itemLocked, itemLockLeft);
							_rewardList.add(i - 1, holder);
						}
					}
					catch (Exception e)
					{
						LOGGER.warning("Could not restore random craft for " + _player);
					}
				}
				else
				{
					storeNew();
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Could not restore random craft for " + _player, e);
		}
	}
	
	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_random_craft SET random_craft_full_points=?,random_craft_points=?,sayha_roll=?,item_1_id=?,item_1_count=?,item_1_locked=?,item_1_lock_left=?,item_2_id=?,item_2_count=?,item_2_locked=?,item_2_lock_left=?,item_3_id=?,item_3_count=?,item_3_locked=?,item_3_lock_left=?,item_4_id=?,item_4_count=?,item_4_locked=?,item_4_lock_left=?,item_5_id=?,item_5_count=?,item_5_locked=?,item_5_lock_left=? WHERE charId=?"))
		{
			ps.setInt(1, _fullCraftPoints);
			ps.setInt(2, _craftPoints);
			ps.setBoolean(3, _isSayhaRoll);
			for (int i = 0; i < 5; i++)
			{
				if (_rewardList.size() >= (i + 1))
				{
					final RandomCraftRewardItemHolder holder = _rewardList.get(i);
					ps.setInt(4 + (i * 4), holder == null ? 0 : holder.getItemId());
					ps.setLong(5 + (i * 4), holder == null ? 0 : holder.getItemCount());
					ps.setBoolean(6 + (i * 4), holder == null ? false : holder.isLocked());
					ps.setInt(7 + (i * 4), holder == null ? 20 : holder.getLockLeft());
				}
				else
				{
					ps.setInt(4 + (i * 4), 0);
					ps.setLong(5 + (i * 4), 0);
					ps.setBoolean(6 + (i * 4), false);
					ps.setInt(7 + (i * 4), 20);
				}
			}
			ps.setInt(24, _player.getObjectId());
			ps.execute();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not store RandomCraft for: " + _player, e);
		}
	}
	
	public void storeNew()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_random_craft VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _player.getObjectId());
			ps.setInt(2, _fullCraftPoints);
			ps.setInt(3, _craftPoints);
			ps.setBoolean(4, _isSayhaRoll);
			for (int i = 0; i < 5; i++)
			{
				ps.setInt(5 + (i * 4), 0);
				ps.setLong(6 + (i * 4), 0);
				ps.setBoolean(7 + (i * 4), false);
				ps.setInt(8 + (i * 4), 0);
			}
			ps.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.log(Level.SEVERE, "Could not store new RandomCraft for: " + _player, e.getMessage());
		}
	}
	
	public void refresh()
	{
		if (_player.hasItemRequest() || _player.hasRequest(RandomCraftRequest.class))
		{
			return;
		}
		_player.addRequest(new RandomCraftRequest(_player));
		
		if ((_fullCraftPoints > 0) && _player.reduceAdena("RandomCraft Refresh", 10000, _player, true))
		{
			_player.sendPacket(new ExCraftInfo(_player));
			_player.sendPacket(new ExCraftRandomRefresh());
			_fullCraftPoints--;
			if (_isSayhaRoll)
			{
				_player.addItem("RandomCraft Roll", 91641, 2, _player, true);
				_isSayhaRoll = false;
			}
			_player.sendPacket(new ExCraftInfo(_player));
			
			for (int i = 0; i < 5; i++)
			{
				final RandomCraftRewardItemHolder holder;
				if (i > (_rewardList.size() - 1))
				{
					holder = null;
				}
				else
				{
					holder = _rewardList.get(i);
				}
				
				if (holder == null)
				{
					_rewardList.add(i, getNewReward());
				}
				else if (!holder.isLocked())
				{
					_rewardList.set(i, getNewReward());
				}
				else
				{
					holder.decLock();
				}
			}
			_player.sendPacket(new ExCraftRandomInfo(_player));
		}
		
		_player.removeRequest(RandomCraftRequest.class);
	}
	
	private RandomCraftRewardItemHolder getNewReward()
	{
		if (RandomCraftData.getInstance().isEmpty())
		{
			return null;
		}
		
		RandomCraftRewardItemHolder result = null;
		while (result == null)
		{
			result = RandomCraftData.getInstance().getNewReward();
			SEARCH: for (RandomCraftRewardItemHolder reward : _rewardList)
			{
				if (reward.getItemId() == result.getItemId())
				{
					result = null;
					break SEARCH;
				}
			}
		}
		return result;
	}
	
	public void make()
	{
		if (_player.hasItemRequest() || _player.hasRequest(RandomCraftRequest.class))
		{
			return;
		}
		_player.addRequest(new RandomCraftRequest(_player));
		
		if (_player.reduceAdena("RandomCraft Make", Config.RANDOM_CRAFT_CREATE_FEE, _player, true))
		{
			final int madeId = Rnd.get(0, 4);
			final RandomCraftRewardItemHolder holder = _rewardList.get(madeId);
			final int itemId = holder.getItemId();
			final long itemCount = holder.getItemCount();
			_rewardList.clear();
			final Item item = _player.addItem("RandomCraft Make", itemId, itemCount, _player, true);
			if (RandomCraftData.getInstance().isAnnounce(itemId))
			{
				Broadcast.toAllOnlinePlayers(new ExItemAnnounce(_player, item, ExItemAnnounce.RANDOM_CRAFT));
				LOGGER.log(Level.INFO, _player + " randomly crafted " + item.getTemplate() + " [" + item.getObjectId() + "]");
			}
			_player.sendPacket(new ExCraftRandomMake(itemId, itemCount));
			_player.sendPacket(new ExCraftRandomInfo(_player));
			
		}
		_player.removeRequest(RandomCraftRequest.class);
	}
	
	public List<RandomCraftRewardItemHolder> getRewards()
	{
		return _rewardList;
	}
	
	public int getFullCraftPoints()
	{
		return _fullCraftPoints;
	}
	
	public void addFullCraftPoints(int value)
	{
		addFullCraftPoints(value, false);
	}
	
	public void addFullCraftPoints(int value, boolean broadcast)
	{
		_fullCraftPoints = Math.min(_fullCraftPoints + value, MAX_FULL_CRAFT_POINTS);
		if (_craftPoints >= MAX_CRAFT_POINTS)
		{
			_craftPoints = 0;
		}
		if (value > 0)
		{
			_isSayhaRoll = true;
		}
		if (broadcast)
		{
			_player.sendPacket(new ExCraftInfo(_player));
		}
	}
	
	public void removeFullCraftPoints(int value)
	{
		_fullCraftPoints -= value;
		_player.sendPacket(new ExCraftInfo(_player));
	}
	
	public void addCraftPoints(int value)
	{
		if ((_craftPoints - 1) < MAX_CRAFT_POINTS)
		{
			_craftPoints += value;
		}
		
		final int fullPointsToAdd = _craftPoints / MAX_CRAFT_POINTS;
		final int pointsToRemove = MAX_CRAFT_POINTS * fullPointsToAdd;
		
		_craftPoints -= pointsToRemove;
		addFullCraftPoints(fullPointsToAdd);
		if (_fullCraftPoints == MAX_FULL_CRAFT_POINTS)
		{
			_craftPoints = MAX_CRAFT_POINTS;
		}
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ACQUIRED_S1_CRAFT_SCALE_POINTS);
		sm.addLong(value);
		_player.sendPacket(sm);
		_player.sendPacket(new ExCraftInfo(_player));
	}
	
	public int getCraftPoints()
	{
		return _craftPoints;
	}
	
	public void setIsSayhaRoll(boolean value)
	{
		_isSayhaRoll = value;
	}
	
	public boolean isSayhaRoll()
	{
		return _isSayhaRoll;
	}
	
	public int getLockedSlotCount()
	{
		int count = 0;
		for (RandomCraftRewardItemHolder holder : _rewardList)
		{
			if (holder.isLocked())
			{
				count++;
			}
		}
		return count;
	}
}
