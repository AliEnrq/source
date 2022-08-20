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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.SubclassInfoType;
import org.l2jmobius.gameserver.enums.SubclassType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SubClassHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw
 */
public class ExSubjobInfo implements IClientOutgoingPacket
{
	private final int _currClassId;
	private final int _currRace;
	private final int _type;
	private final List<SubInfo> _subs;
	
	public ExSubjobInfo(Player player, SubclassInfoType type)
	{
		_currClassId = player.getClassId().getId();
		_currRace = player.getRace().ordinal();
		_type = type.ordinal();
		_subs = new ArrayList<>();
		_subs.add(0, new SubInfo(player));
		for (SubClassHolder sub : player.getSubClasses().values())
		{
			_subs.add(new SubInfo(sub));
		}
	}
	
	private final class SubInfo
	{
		private final int _index;
		private final int _classId;
		private final int _level;
		private final int _type;
		
		public SubInfo(SubClassHolder sub)
		{
			_index = sub.getClassIndex();
			_classId = sub.getClassId();
			_level = sub.getLevel();
			_type = sub.isDualClass() ? SubclassType.DUALCLASS.ordinal() : SubclassType.SUBCLASS.ordinal();
		}
		
		public SubInfo(Player player)
		{
			_index = 0;
			_classId = player.getBaseClass();
			_level = player.getStat().getBaseLevel();
			_type = SubclassType.BASECLASS.ordinal();
		}
		
		public int getIndex()
		{
			return _index;
		}
		
		public int getClassId()
		{
			return _classId;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public int getType()
		{
			return _type;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SUBJOB_INFO.writeId(packet);
		packet.writeC(_type);
		packet.writeD(_currClassId);
		packet.writeD(_currRace);
		packet.writeD(_subs.size());
		for (SubInfo sub : _subs)
		{
			packet.writeD(sub.getIndex());
			packet.writeD(sub.getClassId());
			packet.writeD(sub.getLevel());
			packet.writeC(sub.getType());
		}
		return true;
	}
}