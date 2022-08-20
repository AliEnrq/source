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
package org.l2jmobius.gameserver.model.actor.instance;

import java.util.concurrent.Future;
import java.util.logging.Level;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.SkillData;
import org.l2jmobius.gameserver.enums.InstanceType;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.taskmanager.DecayTaskManager;

public class Decoy extends Creature
{
	private final Player _owner;
	private int _totalLifeTime;
	private int _timeRemaining;
	private Future<?> _decoyLifeTask;
	private Future<?> _hateSpam;
	
	public Decoy(NpcTemplate template, Player owner, int totalLifeTime)
	{
		super(template);
		setInstanceType(InstanceType.Decoy);
		_owner = owner;
		setXYZInvisible(owner.getX(), owner.getY(), owner.getZ());
		setInvul(false);
		_totalLifeTime = totalLifeTime;
		_timeRemaining = _totalLifeTime;
		final int hateSpamSkillId = 5272;
		final int skilllevel = Math.min(getTemplate().getDisplayId() - 13070, SkillData.getInstance().getMaxLevel(hateSpamSkillId));
		_decoyLifeTask = ThreadPool.scheduleAtFixedRate(new DecoyLifetime(_owner, this), 1000, 1000);
		_hateSpam = ThreadPool.scheduleAtFixedRate(new HateSpam(this, SkillData.getInstance().getSkill(hateSpamSkillId, skilllevel)), 2000, 5000);
	}
	
	@Override
	public boolean doDie(Creature killer)
	{
		if (!super.doDie(killer))
		{
			return false;
		}
		if (_hateSpam != null)
		{
			_hateSpam.cancel(true);
			_hateSpam = null;
		}
		_totalLifeTime = 0;
		DecayTaskManager.getInstance().add(this);
		return true;
	}
	
	static class DecoyLifetime implements Runnable
	{
		private final Player _player;
		
		private final Decoy _Decoy;
		
		DecoyLifetime(Player player, Decoy decoy)
		{
			_player = player;
			_Decoy = decoy;
		}
		
		@Override
		public void run()
		{
			try
			{
				_Decoy.decTimeRemaining(1000);
				final double newTimeRemaining = _Decoy.getTimeRemaining();
				if (newTimeRemaining < 0)
				{
					_Decoy.unSummon(_player);
				}
			}
			catch (Exception e)
			{
				LOGGER.log(Level.SEVERE, "Decoy Error: ", e);
			}
		}
	}
	
	private static class HateSpam implements Runnable
	{
		private final Decoy _player;
		private final Skill _skill;
		
		HateSpam(Decoy player, Skill hate)
		{
			_player = player;
			_skill = hate;
		}
		
		@Override
		public void run()
		{
			try
			{
				_player.setTarget(_player);
				_player.doCast(_skill);
			}
			catch (Throwable e)
			{
				LOGGER.log(Level.SEVERE, "Decoy Error: ", e);
			}
		}
	}
	
	public void unSummon(Player owner)
	{
		if (_decoyLifeTask != null)
		{
			_decoyLifeTask.cancel(true);
			_decoyLifeTask = null;
		}
		if (_hateSpam != null)
		{
			_hateSpam.cancel(true);
			_hateSpam = null;
		}
		
		if (isSpawned() && !isDead())
		{
			ZoneManager.getInstance().getRegion(this).removeFromZones(this);
			decayMe();
		}
	}
	
	public void decTimeRemaining(int value)
	{
		_timeRemaining -= value;
	}
	
	public int getTimeRemaining()
	{
		return _timeRemaining;
	}
	
	public int getTotalLifeTime()
	{
		return _totalLifeTime;
	}
	
	@Override
	public void onSpawn()
	{
		super.onSpawn();
		sendPacket(new CharInfo(this, false));
	}
	
	@Override
	public void updateAbnormalVisualEffects()
	{
		World.getInstance().forEachVisibleObject(this, Player.class, player ->
		{
			if (isVisibleFor(player))
			{
				player.sendPacket(new CharInfo(this, isInvisible() && player.canOverrideCond(PlayerCondOverride.SEE_ALL_PLAYERS)));
			}
		});
	}
	
	public void stopDecay()
	{
		DecayTaskManager.getInstance().cancel(this);
	}
	
	@Override
	public void onDecay()
	{
		deleteMe(_owner);
	}
	
	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return _owner.isAutoAttackable(attacker);
	}
	
	@Override
	public Item getActiveWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getActiveWeaponItem()
	{
		return null;
	}
	
	@Override
	public Item getSecondaryWeaponInstance()
	{
		return null;
	}
	
	@Override
	public Weapon getSecondaryWeaponItem()
	{
		return null;
	}
	
	@Override
	public int getId()
	{
		return getTemplate().getId();
	}
	
	@Override
	public int getLevel()
	{
		return getTemplate().getLevel();
	}
	
	public void deleteMe(Player owner)
	{
		decayMe();
	}
	
	public Player getOwner()
	{
		return _owner;
	}
	
	@Override
	public Player getActingPlayer()
	{
		return _owner;
	}
	
	@Override
	public NpcTemplate getTemplate()
	{
		return (NpcTemplate) super.getTemplate();
	}
	
	@Override
	public void sendInfo(Player player)
	{
		player.sendPacket(new CharInfo(this, isInvisible() && player.canOverrideCond(PlayerCondOverride.SEE_ALL_PLAYERS)));
	}
	
	@Override
	public void sendPacket(IClientOutgoingPacket packet)
	{
		if (_owner != null)
		{
			_owner.sendPacket(packet);
		}
	}
	
	@Override
	public void sendPacket(SystemMessageId id)
	{
		if (_owner != null)
		{
			_owner.sendPacket(id);
		}
	}
}
