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
package org.l2jmobius.gameserver.taskmanager;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.xml.ActionData;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.handler.IPlayerActionHandler;
import org.l2jmobius.gameserver.handler.ItemHandler;
import org.l2jmobius.gameserver.handler.PlayerActionHandler;
import org.l2jmobius.gameserver.model.ActionDataHolder;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.Guard;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.holders.ItemSkillHolder;
import org.l2jmobius.gameserver.model.item.EtcItem;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.AbnormalType;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.skill.targets.AffectScope;
import org.l2jmobius.gameserver.model.skill.targets.TargetType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExBasicActionList;

/**
 * @author Mobius
 */
public class AutoUseTaskManager implements Runnable
{
	private static final Set<Player> PLAYERS = ConcurrentHashMap.newKeySet();
	private static final int REUSE_MARGIN_TIME = 3;
	private static boolean _working = false;
	
	protected AutoUseTaskManager()
	{
		ThreadPool.scheduleAtFixedRate(this, 200, 200);
	}
	
	@Override
	public void run()
	{
		if (_working)
		{
			return;
		}
		_working = true;
		
		for (Player player : PLAYERS)
		{
			if (!player.isOnline() || player.isInOfflineMode())
			{
				stopAutoUseTask(player);
				continue;
			}
			
			if (player.hasBlockActions() || player.isControlBlocked() || player.isAlikeDead())
			{
				continue;
			}
			
			final boolean isInPeaceZone = player.isInsideZone(ZoneId.PEACE) || player.isInsideZone(ZoneId.SAYUNE);
			
			if (Config.ENABLE_AUTO_ITEM && !isInPeaceZone)
			{
				ITEMS: for (Integer itemId : player.getAutoUseSettings().getAutoSupplyItems())
				{
					if (player.isTeleporting())
					{
						break ITEMS;
					}
					
					final Item item = player.getInventory().getItemByItemId(itemId.intValue());
					if (item == null)
					{
						player.getAutoUseSettings().getAutoSupplyItems().remove(itemId);
						continue ITEMS;
					}
					
					final ItemTemplate it = item.getTemplate();
					if (it != null)
					{
						if (!it.checkCondition(player, player, false))
						{
							continue ITEMS;
						}
						
						final List<ItemSkillHolder> skills = it.getAllSkills();
						if (skills != null)
						{
							for (ItemSkillHolder itemSkillHolder : skills)
							{
								final Skill skill = itemSkillHolder.getSkill();
								if (player.isAffectedBySkill(skill.getId()) || player.hasSkillReuse(skill.getReuseHashCode()) || !skill.checkCondition(player, player, false))
								{
									continue ITEMS;
								}
							}
						}
					}
					
					final int reuseDelay = item.getReuseDelay();
					if ((reuseDelay <= 0) || (player.getItemRemainingReuseTime(item.getObjectId()) <= 0))
					{
						final EtcItem etcItem = item.getEtcItem();
						final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
						if ((handler != null) && handler.useItem(player, item, false) && (reuseDelay > 0))
						{
							player.addTimeStampItem(item, reuseDelay);
						}
					}
				}
			}
			
			if (Config.ENABLE_AUTO_POTION && !isInPeaceZone && (player.getCurrentHpPercent() < player.getAutoPlaySettings().getAutoPotionPercent()))
			{
				POTIONS: for (Integer itemId : player.getAutoUseSettings().getAutoPotionItems())
				{
					final Item item = player.getInventory().getItemByItemId(itemId.intValue());
					if (item == null)
					{
						player.getAutoUseSettings().getAutoPotionItems().remove(itemId);
						continue POTIONS;
					}
					
					final int reuseDelay = item.getReuseDelay();
					if ((reuseDelay <= 0) || (player.getItemRemainingReuseTime(item.getObjectId()) <= 0))
					{
						final EtcItem etcItem = item.getEtcItem();
						final IItemHandler handler = ItemHandler.getInstance().getHandler(etcItem);
						if ((handler != null) && handler.useItem(player, item, false) && (reuseDelay > 0))
						{
							player.addTimeStampItem(item, reuseDelay);
						}
					}
				}
			}
			
			if (Config.ENABLE_AUTO_SKILL)
			{
				BUFFS: for (Integer skillId : player.getAutoUseSettings().getAutoBuffs())
				{
					// Fixes start area issue.
					if (isInPeaceZone)
					{
						break BUFFS;
					}
					
					// Already casting.
					if (player.isCastingNow())
					{
						break BUFFS;
					}
					
					// Player is teleporting.
					if (player.isTeleporting())
					{
						break BUFFS;
					}
					
					final Skill skill = player.getKnownSkill(skillId.intValue());
					if (skill == null)
					{
						player.getAutoUseSettings().getAutoBuffs().remove(skillId);
						continue BUFFS;
					}
					
					final WorldObject target = player.getTarget();
					if (canCastBuff(player, target, skill))
					{
						// Playable target cast.
						if ((target != null) && target.isPlayable() && !((Playable) target).isAlikeDead() && (target.getActingPlayer().getPvpFlag() == 0) && (target.getActingPlayer().getReputation() >= 0))
						{
							player.doCast(skill);
						}
						else // Target self, cast and re-target.
						{
							final WorldObject savedTarget = target;
							player.setTarget(player);
							player.doCast(skill);
							player.setTarget(savedTarget);
						}
					}
				}
				
				// Continue when auto play is not enabled.
				if (!AutoPlayTaskManager.getInstance().isAutoPlay(player))
				{
					continue;
				}
				
				SKILLS:
				{
					// Already casting.
					if (player.isCastingNow())
					{
						break SKILLS;
					}
					
					// Player is teleporting.
					if (player.isTeleporting())
					{
						break SKILLS;
					}
					
					// Acquire next skill.
					final Integer skillId = player.getAutoUseSettings().getNextSkillId();
					final Skill skill = player.getKnownSkill(skillId.intValue());
					if (skill == null)
					{
						player.getAutoUseSettings().getAutoSkills().remove(skillId);
						player.getAutoUseSettings().resetSkillOrder();
						break SKILLS;
					}
					
					// Casting on self stops movement.
					final WorldObject target = player.getTarget();
					if (target == player)
					{
						break SKILLS;
					}
					
					// Check bad skill target.
					if ((target == null) || !target.isAttackable() || ((Creature) target).isDead())
					{
						break SKILLS;
					}
					
					// Do not attack guards.
					if (target instanceof Guard)
					{
						break SKILLS;
					}
					
					if (!canUseMagic(player, target, skill) || player.useMagic(skill, null, true, false))
					{
						player.getAutoUseSettings().incrementSkillOrder();
					}
				}
				
				ACTIONS: for (Integer actionId : player.getAutoUseSettings().getAutoActions())
				{
					final BuffInfo info = player.getEffectList().getFirstBuffInfoByAbnormalType(AbnormalType.BOT_PENALTY);
					if (info != null)
					{
						for (AbstractEffect effect : info.getEffects())
						{
							if (!effect.checkCondition(actionId))
							{
								player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_AS_AN_ILLEGAL_PROGRAM_USER_SO_YOUR_ACTIONS_HAVE_BEEN_RESTRICTED);
								break ACTIONS;
							}
						}
					}
					
					// Do not allow to do some action if player is transformed.
					if (player.isTransformed())
					{
						final int[] allowedActions = player.isTransformed() ? ExBasicActionList.ACTIONS_ON_TRANSFORM : ExBasicActionList.DEFAULT_ACTION_LIST;
						if (Arrays.binarySearch(allowedActions, actionId) < 0)
						{
							continue ACTIONS;
						}
					}
					
					final ActionDataHolder actionHolder = ActionData.getInstance().getActionData(actionId);
					if (actionHolder != null)
					{
						final IPlayerActionHandler actionHandler = PlayerActionHandler.getInstance().getHandler(actionHolder.getHandler());
						if (actionHandler != null)
						{
							actionHandler.useAction(player, actionHolder, false, false);
						}
					}
				}
			}
		}
		
		_working = false;
	}
	
	private boolean canCastBuff(Player player, WorldObject target, Skill skill)
	{
		// Summon check.
		if (skill.getAffectScope() == AffectScope.SUMMON_EXCEPT_MASTER)
		{
			if (!player.hasServitors())
			{
				return false;
			}
			int occurrences = 0;
			for (Summon servitor : player.getServitors().values())
			{
				if (servitor.isAffectedBySkill(skill.getId()))
				{
					occurrences++;
				}
			}
			if (occurrences == player.getServitors().size())
			{
				return false;
			}
		}
		
		final Playable playableTarget = (target == null) || !target.isPlayable() || (skill.getTargetType() == TargetType.SELF) ? player : (Playable) target;
		if (!canUseMagic(player, playableTarget, skill))
		{
			return false;
		}
		
		final BuffInfo buffInfo = playableTarget.getEffectList().getBuffInfoBySkillId(skill.getId());
		return (buffInfo == null) || (buffInfo.getTime() <= REUSE_MARGIN_TIME);
	}
	
	private boolean canUseMagic(Player player, WorldObject target, Skill skill)
	{
		if ((skill.getItemConsumeCount() > 0) && (player.getInventory().getInventoryItemCount(skill.getItemConsumeId(), -1) < skill.getItemConsumeCount()))
		{
			return false;
		}
		return !player.isSkillDisabled(skill) && skill.checkCondition(player, target, false);
	}
	
	public void startAutoUseTask(Player player)
	{
		if (!PLAYERS.contains(player))
		{
			PLAYERS.add(player);
		}
	}
	
	public void stopAutoUseTask(Player player)
	{
		player.getAutoUseSettings().resetSkillOrder();
		if (player.getAutoUseSettings().isEmpty() || !player.isOnline() || player.isInOfflineMode())
		{
			PLAYERS.remove(player);
		}
	}
	
	public void addAutoSupplyItem(Player player, int itemId)
	{
		player.getAutoUseSettings().getAutoSupplyItems().add(itemId);
		startAutoUseTask(player);
	}
	
	public void removeAutoSupplyItem(Player player, int itemId)
	{
		player.getAutoUseSettings().getAutoSupplyItems().remove(itemId);
		stopAutoUseTask(player);
	}
	
	public void addAutoPotionItem(Player player, int itemId)
	{
		player.getAutoUseSettings().getAutoPotionItems().add(itemId);
		startAutoUseTask(player);
	}
	
	public void removeAutoPotionItem(Player player, int itemId)
	{
		player.getAutoUseSettings().getAutoPotionItems().remove(itemId);
		stopAutoUseTask(player);
	}
	
	public void addAutoBuff(Player player, int skillId)
	{
		player.getAutoUseSettings().getAutoBuffs().add(skillId);
		startAutoUseTask(player);
	}
	
	public void removeAutoBuff(Player player, int skillId)
	{
		player.getAutoUseSettings().getAutoBuffs().remove(skillId);
		stopAutoUseTask(player);
	}
	
	public void addAutoSkill(Player player, Integer skillId)
	{
		player.getAutoUseSettings().getAutoSkills().add(skillId);
		startAutoUseTask(player);
	}
	
	public void removeAutoSkill(Player player, Integer skillId)
	{
		player.getAutoUseSettings().getAutoSkills().remove(skillId);
		stopAutoUseTask(player);
	}
	
	public void addAutoAction(Player player, int actionId)
	{
		player.getAutoUseSettings().getAutoActions().add(actionId);
		startAutoUseTask(player);
	}
	
	public void removeAutoAction(Player player, int actionId)
	{
		player.getAutoUseSettings().getAutoActions().remove(actionId);
		stopAutoUseTask(player);
	}
	
	public static AutoUseTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AutoUseTaskManager INSTANCE = new AutoUseTaskManager();
	}
}
