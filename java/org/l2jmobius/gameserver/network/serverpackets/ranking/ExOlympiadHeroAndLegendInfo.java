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
package org.l2jmobius.gameserver.network.serverpackets.ranking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.olympiad.Hero;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author NviX
 */
public class ExOlympiadHeroAndLegendInfo implements IClientOutgoingPacket
{
	// TODO: Move query and store data at RankManager.
	private static final String GET_HEROES = "SELECT characters.charId, characters.char_name, characters.race, characters.sex, characters.base_class, characters.level, characters.clanid, olympiad_nobles_eom.competitions_won, olympiad_nobles_eom.competitions_lost, olympiad_nobles_eom.olympiad_points, heroes.legend_count, heroes.count FROM heroes, characters, olympiad_nobles_eom WHERE characters.charId = heroes.charId AND characters.charId = olympiad_nobles_eom.charId ORDER BY olympiad_nobles_eom.olympiad_points DESC, characters.base_class ASC LIMIT " + RankManager.PLAYER_LIMIT;
	
	public ExOlympiadHeroAndLegendInfo()
	{
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_OLYMPIAD_HERO_AND_LEGEND_INFO.writeId(packet);
		if (!Hero.getInstance().getHeroes().isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement(GET_HEROES))
			{
				try (ResultSet rset = statement.executeQuery())
				{
					int i = 1;
					final boolean writedCount = false;
					while (rset.next())
					{
						if (i == 1)
						{
							packet.writeC(1); // ?? shows 78 on JP
							packet.writeC(1); // ?? shows 0 on JP
							packet.writeString(rset.getString("char_name"));
							final int clanId = rset.getInt("clanid");
							if (clanId > 0)
							{
								packet.writeString(ClanTable.getInstance().getClan(clanId).getName());
							}
							else
							{
								packet.writeString("");
							}
							packet.writeD(Config.SERVER_ID);
							packet.writeD(rset.getInt("race"));
							packet.writeD(rset.getInt("sex") == 1 ? 0 : 1);
							packet.writeD(rset.getInt("base_class"));
							packet.writeD(rset.getInt("level"));
							packet.writeD(rset.getInt("legend_count"));
							packet.writeD(rset.getInt("competitions_won"));
							packet.writeD(rset.getInt("competitions_lost"));
							packet.writeD(rset.getInt("olympiad_points"));
							if (clanId > 0)
							{
								packet.writeD(ClanTable.getInstance().getClan(clanId).getLevel());
							}
							else
							{
								packet.writeD(0);
							}
							i++;
						}
						else
						{
							if (!writedCount)
							{
								packet.writeD(Hero.getInstance().getHeroes().size() - 1);
							}
							if (Hero.getInstance().getHeroes().size() > 1)
							{
								packet.writeString(rset.getString("char_name"));
								final int clanId = rset.getInt("clanid");
								if (clanId > 0)
								{
									packet.writeString(ClanTable.getInstance().getClan(clanId).getName());
								}
								else
								{
									packet.writeString("");
								}
								packet.writeD(Config.SERVER_ID);
								packet.writeD(rset.getInt("race"));
								packet.writeD(rset.getInt("sex") == 1 ? 0 : 1);
								packet.writeD(rset.getInt("base_class"));
								packet.writeD(rset.getInt("level"));
								packet.writeD(rset.getInt("count"));
								packet.writeD(rset.getInt("competitions_won"));
								packet.writeD(rset.getInt("competitions_lost"));
								packet.writeD(rset.getInt("olympiad_points"));
								if (clanId > 0)
								{
									packet.writeD(ClanTable.getInstance().getClan(clanId).getLevel());
								}
								else
								{
									packet.writeD(0);
								}
							}
						}
					}
				}
			}
			catch (SQLException e)
			{
				PacketLogger.warning("Hero and Legend Info: Couldnt load data: " + e.getMessage());
			}
		}
		return true;
	}
}
