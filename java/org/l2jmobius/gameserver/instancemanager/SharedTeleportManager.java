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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SharedTeleportHolder;

/**
 * Shared Teleport Manager
 * @author NasSeKa
 */
public class SharedTeleportManager
{
	protected static final Logger LOGGER = Logger.getLogger(PetitionManager.class.getName());
	private int _lastSharedTeleportId = 0;
	private final Map<Integer, SharedTeleportHolder> _sharedTeleports = new ConcurrentHashMap<>();
	
	protected SharedTeleportManager()
	{
		init();
	}
	
	public void init()
	{
		_sharedTeleports.clear();
		LOGGER.info(getClass().getSimpleName() + ": Shared Teleport data cleared.");
	}
	
	public Map<Integer, SharedTeleportHolder> getSharedTeleports()
	{
		return _sharedTeleports;
	}
	
	public void addSharedTeleport(Player player)
	{
		int freeId = getNextFreeID();
		_sharedTeleports.put(freeId, new SharedTeleportHolder(freeId, player.getName(), 5, player.getX(), player.getY(), player.getZ()));
	}
	
	public int getNextFreeID()
	{
		_lastSharedTeleportId += 1;
		return _lastSharedTeleportId;
	}
	
	public int getLastSharedId()
	{
		return _lastSharedTeleportId;
	}
	
	public void teleportToSharedLocation(Player player, int id)
	{
		if ((id == 0) || (getTimesUsed(id) == 0) || (player == null))
		{
			return;
		}
		
		_sharedTeleports.get(id).useTeleport();
		player.abortCast();
		player.stopMove(null);
		player.teleToLocation(getX(id), getY(id), getZ(id));
	}
	
	public String getCharName(int id)
	{
		if (id == 0)
		{
			return "";
		}
		
		return _sharedTeleports.get(id).getCharName();
	}
	
	public int getTimesUsed(int id)
	{
		if (_sharedTeleports.getOrDefault(id, null) == null)
		{
			return 0;
		}
		return _sharedTeleports.get(id).getTimesUsed();
	}
	
	public int getX(int id)
	{
		if (_sharedTeleports.getOrDefault(id, null) == null)
		{
			return 0;
		}
		return _sharedTeleports.get(id).getX();
	}
	
	public int getY(int id)
	{
		if (_sharedTeleports.getOrDefault(id, null) == null)
		{
			return 0;
		}
		return _sharedTeleports.get(id).getY();
	}
	
	public int getZ(int id)
	{
		if (_sharedTeleports.getOrDefault(id, null) == null)
		{
			return 0;
		}
		return _sharedTeleports.get(id).getZ();
	}
	
	/**
	 * Gets the single instance of {@code SharedTeleportManager}.
	 * @return single instance of {@code SharedTeleportManager}
	 */
	public static SharedTeleportManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SharedTeleportManager INSTANCE = new SharedTeleportManager();
	}
}
