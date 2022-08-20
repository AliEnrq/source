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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.ExperienceData;
import org.l2jmobius.gameserver.enums.BonusExpType;
import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CursedWeaponsManager;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw, UnAfraid
 */
public class UserInfo extends AbstractMaskPacket<UserInfoType>
{
	private Player _player;
	private int _relation;
	private int _runSpd;
	private int _walkSpd;
	private int _swimRunSpd;
	private int _swimWalkSpd;
	private final int _flRunSpd = 0;
	private final int _flWalkSpd = 0;
	private int _flyRunSpd;
	private int _flyWalkSpd;
	private double _moveMultiplier;
	private int _enchantLevel;
	private int _armorEnchant;
	private String _title;
	private final byte[] _masks = new byte[]
	{
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00,
		(byte) 0x00
	};
	private int _initSize = 5;
	
	public UserInfo(Player player)
	{
		this(player, true);
	}
	
	public UserInfo(Player player, boolean addAll)
	{
		if (!player.isSubclassLocked()) // Changing class.
		{
			_player = player;
			_relation = calculateRelation(player);
			_moveMultiplier = player.getMovementSpeedMultiplier();
			_runSpd = (int) Math.round(player.getRunSpeed() / _moveMultiplier);
			_walkSpd = (int) Math.round(player.getWalkSpeed() / _moveMultiplier);
			_swimRunSpd = (int) Math.round(player.getSwimRunSpeed() / _moveMultiplier);
			_swimWalkSpd = (int) Math.round(player.getSwimWalkSpeed() / _moveMultiplier);
			_flyRunSpd = player.isFlying() ? _runSpd : 0;
			_flyWalkSpd = player.isFlying() ? _walkSpd : 0;
			_enchantLevel = player.getInventory().getWeaponEnchant();
			_armorEnchant = player.getInventory().getArmorMinEnchant();
			_title = player.getTitle();
			
			if (player.isGM() && player.isInvisible())
			{
				_title = "[Invisible]";
			}
			
			if (addAll)
			{
				addComponentType(UserInfoType.values());
			}
		}
	}
	
	@Override
	protected byte[] getMasks()
	{
		return _masks;
	}
	
	@Override
	protected void onNewMaskAdded(UserInfoType component)
	{
		calcBlockSize(component);
	}
	
	private void calcBlockSize(UserInfoType type)
	{
		switch (type)
		{
			case BASIC_INFO:
			{
				_initSize += type.getBlockLength() + (_player.getAppearance().getVisibleName().length() * 2);
				break;
			}
			case CLAN:
			{
				_initSize += type.getBlockLength() + (_title.length() * 2);
				break;
			}
			default:
			{
				_initSize += type.getBlockLength();
				break;
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player == null)
		{
			return false;
		}
		
		OutgoingPackets.USER_INFO.writeId(packet);
		packet.writeD(_player.getObjectId());
		packet.writeD(_initSize);
		packet.writeH(28); // 286 - 28
		packet.writeB(_masks);
		if (containsMask(UserInfoType.RELATION))
		{
			packet.writeD(_relation);
		}
		if (containsMask(UserInfoType.BASIC_INFO))
		{
			packet.writeH(23 + (_player.getAppearance().getVisibleName().length() * 2));
			packet.writeString(_player.getName());
			packet.writeC(_player.isGM() ? 1 : 0);
			packet.writeC(_player.getRace().ordinal());
			packet.writeC(_player.getAppearance().isFemale() ? 1 : 0);
			packet.writeD(_player.getBaseTemplate().getClassId().getRootClassId().getId());
			packet.writeD(_player.getClassId().getId());
			packet.writeD(_player.getLevel()); // 270
			packet.writeD(_player.getClassId().getId()); // 286
		}
		if (containsMask(UserInfoType.BASE_STATS))
		{
			packet.writeH(18);
			packet.writeH(_player.getSTR());
			packet.writeH(_player.getDEX());
			packet.writeH(_player.getCON());
			packet.writeH(_player.getINT());
			packet.writeH(_player.getWIT());
			packet.writeH(_player.getMEN());
			packet.writeH(0);
			packet.writeH(0);
		}
		if (containsMask(UserInfoType.MAX_HPCPMP))
		{
			packet.writeH(14);
			packet.writeD(_player.getMaxHp());
			packet.writeD(_player.getMaxMp());
			packet.writeD(_player.getMaxCp());
		}
		if (containsMask(UserInfoType.CURRENT_HPMPCP_EXP_SP))
		{
			packet.writeH(38);
			packet.writeD((int) Math.round(_player.getCurrentHp()));
			packet.writeD((int) Math.round(_player.getCurrentMp()));
			packet.writeD((int) Math.round(_player.getCurrentCp()));
			packet.writeQ(_player.getSp());
			packet.writeQ(_player.getExp());
			packet.writeF((float) (_player.getExp() - ExperienceData.getInstance().getExpForLevel(_player.getLevel())) / (ExperienceData.getInstance().getExpForLevel(_player.getLevel() + 1) - ExperienceData.getInstance().getExpForLevel(_player.getLevel())));
		}
		if (containsMask(UserInfoType.ENCHANTLEVEL))
		{
			packet.writeH(5); // 338
			packet.writeC(_enchantLevel);
			packet.writeC(_armorEnchant);
			packet.writeC(0); // 338 - cBackEnchant?
		}
		if (containsMask(UserInfoType.APPAREANCE))
		{
			packet.writeH(19); // 338
			packet.writeD(_player.getVisualHair());
			packet.writeD(_player.getVisualHairColor());
			packet.writeD(_player.getVisualFace());
			packet.writeC(_player.isHairAccessoryEnabled() ? 1 : 0);
			packet.writeD(_player.getVisualHairColor() + 1); // 338 - DK color.
		}
		if (containsMask(UserInfoType.STATUS))
		{
			packet.writeH(6);
			packet.writeC(_player.getMountType().ordinal());
			packet.writeC(_player.getPrivateStoreType().getId());
			packet.writeC(_player.hasDwarvenCraft() || (_player.getSkillLevel(248) > 0) ? 1 : 0);
			packet.writeC(0);
		}
		if (containsMask(UserInfoType.STATS))
		{
			packet.writeH(64); // 270
			packet.writeH(_player.getActiveWeaponItem() != null ? 40 : 20);
			packet.writeD(_player.getPAtk());
			packet.writeD(_player.getPAtkSpd());
			packet.writeD(_player.getPDef());
			packet.writeD(_player.getEvasionRate());
			packet.writeD(_player.getAccuracy());
			packet.writeD(_player.getCriticalHit());
			packet.writeD(_player.getMAtk());
			packet.writeD(_player.getMAtkSpd());
			packet.writeD(_player.getPAtkSpd()); // Seems like atk speed - 1
			packet.writeD(_player.getMagicEvasionRate());
			packet.writeD(_player.getMDef());
			packet.writeD(_player.getMagicAccuracy());
			packet.writeD(_player.getMCriticalHit());
			packet.writeD(0); // 270
			packet.writeD(0); // 270
		}
		if (containsMask(UserInfoType.ELEMENTALS))
		{
			packet.writeH(14);
			packet.writeH(0);
			packet.writeH(0);
			packet.writeH(0);
			packet.writeH(0);
			packet.writeH(0);
			packet.writeH(0);
		}
		if (containsMask(UserInfoType.POSITION))
		{
			packet.writeH(18);
			packet.writeD(_player.getX());
			packet.writeD(_player.getY());
			packet.writeD(_player.getZ());
			packet.writeD(_player.isInVehicle() ? _player.getVehicle().getObjectId() : 0);
		}
		if (containsMask(UserInfoType.SPEED))
		{
			packet.writeH(18);
			packet.writeH(_runSpd);
			packet.writeH(_walkSpd);
			packet.writeH(_swimRunSpd);
			packet.writeH(_swimWalkSpd);
			packet.writeH(_flRunSpd);
			packet.writeH(_flWalkSpd);
			packet.writeH(_flyRunSpd);
			packet.writeH(_flyWalkSpd);
		}
		if (containsMask(UserInfoType.MULTIPLIER))
		{
			packet.writeH(18);
			packet.writeF(_moveMultiplier);
			packet.writeF(_player.getAttackSpeedMultiplier());
		}
		if (containsMask(UserInfoType.COL_RADIUS_HEIGHT))
		{
			packet.writeH(18);
			packet.writeF(_player.getCollisionRadius());
			packet.writeF(_player.getCollisionHeight());
		}
		if (containsMask(UserInfoType.ATK_ELEMENTAL))
		{
			packet.writeH(5);
			packet.writeC(0);
			packet.writeH(0);
		}
		if (containsMask(UserInfoType.CLAN))
		{
			packet.writeH(32 + (_title.length() * 2));
			packet.writeString(_title);
			packet.writeH(_player.getPledgeType());
			packet.writeD(_player.getClanId());
			packet.writeD(_player.getClanCrestLargeId());
			packet.writeD(_player.getClanCrestId());
			packet.writeD(_player.getClanPrivileges().getBitmask());
			packet.writeC(_player.isClanLeader() ? 1 : 0);
			packet.writeD(_player.getAllyId());
			packet.writeD(_player.getAllyCrestId());
			packet.writeC(_player.isInMatchingRoom() ? 1 : 0);
		}
		if (containsMask(UserInfoType.SOCIAL))
		{
			packet.writeH(30); // 228
			packet.writeC(_player.getPvpFlag());
			packet.writeD(_player.getReputation()); // Reputation
			packet.writeC(_player.isNoble() ? 1 : 0);
			packet.writeC(_player.isHero() || (_player.isGM() && Config.GM_HERO_AURA) ? 2 : 0); // 152 - Value for enabled changed to 2?
			packet.writeC(_player.getPledgeClass());
			packet.writeD(_player.getPkKills());
			packet.writeD(_player.getPvpKills());
			packet.writeH(_player.getRecomLeft());
			packet.writeH(_player.getRecomHave());
			// AFK animation.
			if ((_player.getClan() != null) && (CastleManager.getInstance().getCastleByOwner(_player.getClan()) != null)) // 196
			{
				packet.writeD(_player.isClanLeader() ? 100 : 101);
			}
			else
			{
				packet.writeD(0);
			}
			packet.writeD(0); // 228
		}
		if (containsMask(UserInfoType.VITA_FAME))
		{
			packet.writeH(19); // 196
			packet.writeD(_player.getVitalityPoints());
			packet.writeC(0); // Vita Bonus
			packet.writeD(0); // _player.getFame()
			packet.writeD(0); // _player.getRaidbossPoints()
			packet.writeC(0); // 196
			packet.writeH(0); // Henna Seal Engraving Gauge
			packet.writeC(0); // 196
		}
		if (containsMask(UserInfoType.SLOTS))
		{
			packet.writeH(12); // 152
			packet.writeC(_player.getInventory().getTalismanSlots());
			packet.writeC(_player.getInventory().getBroochJewelSlots());
			packet.writeC(_player.getTeam().getId());
			packet.writeD(0);
			if (_player.getInventory().getAgathionSlots() > 0)
			{
				packet.writeC(1); // Charm slots
				packet.writeC(_player.getInventory().getAgathionSlots() - 1);
				packet.writeC(_player.getInventory().getArtifactSlots()); // Artifact set slots // 152
			}
			else
			{
				packet.writeC(0); // Charm slots
				packet.writeC(0);
				packet.writeC(_player.getInventory().getArtifactSlots()); // Artifact set slots // 152
			}
		}
		if (containsMask(UserInfoType.MOVEMENTS))
		{
			packet.writeH(4);
			packet.writeC(_player.isInsideZone(ZoneId.WATER) ? 1 : _player.isFlyingMounted() ? 2 : 0);
			packet.writeC(_player.isRunning() ? 1 : 0);
		}
		if (containsMask(UserInfoType.COLOR))
		{
			packet.writeH(10);
			packet.writeD(_player.getAppearance().getNameColor());
			packet.writeD(_player.getAppearance().getTitleColor());
		}
		if (containsMask(UserInfoType.INVENTORY_LIMIT))
		{
			packet.writeH(13);
			packet.writeH(0);
			packet.writeH(0);
			packet.writeH(_player.getInventoryLimit());
			packet.writeC(_player.isCursedWeaponEquipped() ? CursedWeaponsManager.getInstance().getLevel(_player.getCursedWeaponEquippedId()) : 0);
			packet.writeC(0); // 196
			packet.writeC(0); // 196
			packet.writeC(0); // 196
			packet.writeC(0); // 196
		}
		if (containsMask(UserInfoType.TRUE_HERO))
		{
			packet.writeH(9);
			packet.writeD(0);
			packet.writeH(0);
			packet.writeC(_player.isTrueHero() ? 100 : 0);
		}
		if (containsMask(UserInfoType.ATT_SPIRITS)) // 152
		{
			packet.writeH(26);
			packet.writeD((int) _player.getActiveElementalSpiritAttack());
			packet.writeD((int) _player.getFireSpiritDefense());
			packet.writeD((int) _player.getWaterSpiritDefense());
			packet.writeD((int) _player.getWindSpiritDefense());
			packet.writeD((int) _player.getEarthSpiritDefense());
			packet.writeD(_player.getActiveElementalSpiritType());
		}
		if (containsMask(UserInfoType.RANKING)) // 196
		{
			packet.writeH(6);
			packet.writeD(RankManager.getInstance().getPlayerGlobalRank(_player) == 1 ? 1 : RankManager.getInstance().getPlayerRaceRank(_player) == 1 ? 2 : 0);
		}
		if (containsMask(UserInfoType.STAT_POINTS)) // 235
		{
			packet.writeH(16);
			packet.writeH(_player.getLevel() < 76 ? 0 : (_player.getLevel() - 75) + _player.getVariables().getInt(PlayerVariables.ELIXIRS_AVAILABLE, 0) + _player.getVariables().getInt(PlayerVariables.ELIXIRS_USED, 0)); // Usable points
			packet.writeH(_player.getVariables().getInt(PlayerVariables.STAT_STR, 0));
			packet.writeH(_player.getVariables().getInt(PlayerVariables.STAT_DEX, 0));
			packet.writeH(_player.getVariables().getInt(PlayerVariables.STAT_CON, 0));
			packet.writeH(_player.getVariables().getInt(PlayerVariables.STAT_INT, 0));
			packet.writeH(_player.getVariables().getInt(PlayerVariables.STAT_WIT, 0));
			packet.writeH(_player.getVariables().getInt(PlayerVariables.STAT_MEN, 0));
		}
		if (containsMask(UserInfoType.STAT_ABILITIES)) // 235
		{
			packet.writeH(18);
			packet.writeH(_player.getSTR() - _player.getTemplate().getBaseSTR() - _player.getVariables().getInt(PlayerVariables.STAT_STR, 0)); // additional STR
			packet.writeH(_player.getDEX() - _player.getTemplate().getBaseDEX() - _player.getVariables().getInt(PlayerVariables.STAT_DEX, 0)); // additional DEX
			packet.writeH(_player.getCON() - _player.getTemplate().getBaseCON() - _player.getVariables().getInt(PlayerVariables.STAT_CON, 0)); // additional CON
			packet.writeH(_player.getINT() - _player.getTemplate().getBaseINT() - _player.getVariables().getInt(PlayerVariables.STAT_INT, 0)); // additional INT
			packet.writeH(_player.getWIT() - _player.getTemplate().getBaseWIT() - _player.getVariables().getInt(PlayerVariables.STAT_WIT, 0)); // additional WIT
			packet.writeH(_player.getMEN() - _player.getTemplate().getBaseMEN() - _player.getVariables().getInt(PlayerVariables.STAT_MEN, 0)); // additional MEN
			packet.writeH(0);
			packet.writeH(0);
		}
		if (containsMask(UserInfoType.ELIXIR_USED)) // 286
		{
			packet.writeD(_player.getVariables().getInt(PlayerVariables.ELIXIRS_USED, 0)); // count
			packet.writeH(0);
		}
		
		// Send exp bonus change.
		if (containsMask(UserInfoType.VITA_FAME))
		{
			_player.sendPacket(new ExUserBoostStat(_player, BonusExpType.VITALITY));
			_player.sendPacket(new ExUserBoostStat(_player, BonusExpType.BUFFS));
			_player.sendPacket(new ExUserBoostStat(_player, BonusExpType.PASSIVE));
			if (Config.ENABLE_VITALITY)
			{
				_player.sendPacket(new ExVitalityEffectInfo(_player));
			}
		}
		
		return true;
	}
	
	private int calculateRelation(Player player)
	{
		int relation = 0;
		final Party party = player.getParty();
		final Clan clan = player.getClan();
		if (party != null)
		{
			relation |= 0x08; // Party member
			if (party.getLeader() == _player)
			{
				relation |= 0x10; // Party leader
			}
		}
		if (clan != null)
		{
			relation |= 0x20; // Clan member
			if (clan.getLeaderId() == player.getObjectId())
			{
				relation |= 0x40; // Clan leader
			}
		}
		if (player.isInSiege())
		{
			relation |= 0x80; // In siege
		}
		return relation;
	}
}
