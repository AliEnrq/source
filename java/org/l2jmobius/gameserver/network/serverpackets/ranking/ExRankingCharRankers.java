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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author NviX
 */
public class ExRankingCharRankers implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _group;
	private final int _scope;
	private final int _race;
	private final int _class;
	private final Map<Integer, StatSet> _playerList;
	private final Map<Integer, StatSet> _snapshotList;
	
	public ExRankingCharRankers(Player player, int group, int scope, int race, int baseclass)
	{
		_player = player;
		_group = group;
		_scope = scope;
		_race = race;
		_class = baseclass;
		_playerList = RankManager.getInstance().getRankList();
		_snapshotList = RankManager.getInstance().getSnapshotList();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_RANKING_CHAR_RANKERS.writeId(packet);
		packet.writeC(_group);
		packet.writeC(_scope);
		packet.writeD(_race);
		packet.writeD(_player.getClassId().getId());
		if (!_playerList.isEmpty())
		{
			switch (_group)
			{
				case 0: // all
				{
					if (_scope == 0) // all
					{
						final int count = _playerList.size() > 150 ? 150 : _playerList.size();
						packet.writeD(count);
						for (Integer id : _playerList.keySet())
						{
							final StatSet player = _playerList.get(id);
							packet.writeString(player.getString("name"));
							packet.writeString(player.getString("clanName"));
							packet.writeD(player.getInt("level"));
							packet.writeD(player.getInt("classId"));
							packet.writeD(player.getInt("race"));
							packet.writeD(id); // server rank
							if (!_snapshotList.isEmpty())
							{
								for (Integer id2 : _snapshotList.keySet())
								{
									final StatSet snapshot = _snapshotList.get(id2);
									if (player.getInt("charId") == snapshot.getInt("charId"))
									{
										packet.writeD(id2); // server rank snapshot
										packet.writeD(snapshot.getInt("raceRank", 0)); // race rank snapshot
										packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
									}
								}
							}
							else
							{
								packet.writeD(id);
								packet.writeD(0);
								packet.writeD(0);
							}
						}
					}
					else
					{
						boolean found = false;
						for (Integer id : _playerList.keySet())
						{
							final StatSet player = _playerList.get(id);
							if (player.getInt("charId") == _player.getObjectId())
							{
								found = true;
								final int first = id > 10 ? (id - 9) : 1;
								final int last = _playerList.size() >= (id + 10) ? id + 10 : id + (_playerList.size() - id);
								if (first == 1)
								{
									packet.writeD(last - (first - 1));
								}
								else
								{
									packet.writeD(last - first);
								}
								for (int id2 = first; id2 <= last; id2++)
								{
									final StatSet plr = _playerList.get(id2);
									packet.writeString(plr.getString("name"));
									packet.writeString(plr.getString("clanName"));
									packet.writeD(plr.getInt("level"));
									packet.writeD(plr.getInt("classId"));
									packet.writeD(plr.getInt("race"));
									packet.writeD(id2); // server rank
									if (!_snapshotList.isEmpty())
									{
										for (Integer id3 : _snapshotList.keySet())
										{
											final StatSet snapshot = _snapshotList.get(id3);
											if (player.getInt("charId") == snapshot.getInt("charId"))
											{
												packet.writeD(id3); // server rank snapshot
												packet.writeD(snapshot.getInt("raceRank", 0));
												packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
											}
										}
									}
								}
							}
						}
						if (!found)
						{
							packet.writeD(0);
						}
					}
					break;
				}
				case 1: // race
				{
					if (_scope == 0) // all
					{
						int count = 0;
						for (int i = 1; i <= _playerList.size(); i++)
						{
							final StatSet player = _playerList.get(i);
							if (_race == player.getInt("race"))
							{
								count++;
							}
						}
						packet.writeD(count > 100 ? 100 : count);
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatSet player = _playerList.get(id);
							if (_race == player.getInt("race"))
							{
								packet.writeString(player.getString("name"));
								packet.writeString(player.getString("clanName"));
								packet.writeD(player.getInt("level"));
								packet.writeD(player.getInt("classId"));
								packet.writeD(player.getInt("race"));
								packet.writeD(i); // server rank
								if (!_snapshotList.isEmpty())
								{
									final Map<Integer, StatSet> snapshotRaceList = new ConcurrentHashMap<>();
									int j = 1;
									for (Integer id2 : _snapshotList.keySet())
									{
										final StatSet snapshot = _snapshotList.get(id2);
										if (_race == snapshot.getInt("race"))
										{
											snapshotRaceList.put(j, _snapshotList.get(id2));
											j++;
										}
									}
									for (Integer id2 : snapshotRaceList.keySet())
									{
										final StatSet snapshot = snapshotRaceList.get(id2);
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											packet.writeD(id2); // server rank snapshot
											packet.writeD(snapshot.getInt("raceRank", 0)); // race rank snapshot
											packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
										}
									}
								}
								else
								{
									packet.writeD(i);
									packet.writeD(i);
									packet.writeD(i); // nClassRank_Snapshot
								}
								i++;
							}
						}
					}
					else
					{
						boolean found = false;
						final Map<Integer, StatSet> raceList = new ConcurrentHashMap<>();
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatSet set = _playerList.get(id);
							if (_player.getRace().ordinal() == set.getInt("race"))
							{
								raceList.put(i, _playerList.get(id));
								i++;
							}
						}
						for (Integer id : raceList.keySet())
						{
							final StatSet player = raceList.get(id);
							if (player.getInt("charId") == _player.getObjectId())
							{
								found = true;
								final int first = id > 10 ? (id - 9) : 1;
								final int last = raceList.size() >= (id + 10) ? id + 10 : id + (raceList.size() - id);
								if (first == 1)
								{
									packet.writeD(last - (first - 1));
								}
								else
								{
									packet.writeD(last - first);
								}
								for (int id2 = first; id2 <= last; id2++)
								{
									final StatSet plr = raceList.get(id2);
									packet.writeString(plr.getString("name"));
									packet.writeString(plr.getString("clanName"));
									packet.writeD(plr.getInt("level"));
									packet.writeD(plr.getInt("classId"));
									packet.writeD(plr.getInt("race"));
									packet.writeD(id2); // server rank
									packet.writeD(id2);
									packet.writeD(id2);
									packet.writeD(id2); // nClassRank_Snapshot
								}
							}
						}
						if (!found)
						{
							packet.writeD(0);
						}
					}
					break;
				}
				case 2: // clan
				{
					if (_player.getClan() != null)
					{
						final Map<Integer, StatSet> clanList = new ConcurrentHashMap<>();
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatSet set = _playerList.get(id);
							if (_player.getClan().getName() == set.getString("clanName"))
							{
								clanList.put(i, _playerList.get(id));
								i++;
							}
						}
						packet.writeD(clanList.size());
						for (Integer id : clanList.keySet())
						{
							final StatSet player = clanList.get(id);
							packet.writeString(player.getString("name"));
							packet.writeString(player.getString("clanName"));
							packet.writeD(player.getInt("level"));
							packet.writeD(player.getInt("classId"));
							packet.writeD(player.getInt("race"));
							packet.writeD(id); // clan rank
							if (!_snapshotList.isEmpty())
							{
								for (Integer id2 : _snapshotList.keySet())
								{
									final StatSet snapshot = _snapshotList.get(id2);
									if (player.getInt("charId") == snapshot.getInt("charId"))
									{
										packet.writeD(id2); // server rank snapshot
										packet.writeD(snapshot.getInt("raceRank", 0)); // race rank snapshot
										packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
									}
								}
							}
							else
							{
								packet.writeD(id);
								packet.writeD(0);
								packet.writeD(0);
							}
						}
					}
					else
					{
						packet.writeD(0);
					}
					break;
				}
				case 3: // friend
				{
					if (!_player.getFriendList().isEmpty())
					{
						final Set<Integer> friendList = ConcurrentHashMap.newKeySet();
						int count = 1;
						for (int id : _player.getFriendList())
						{
							for (Integer id2 : _playerList.keySet())
							{
								final StatSet temp = _playerList.get(id2);
								if (temp.getInt("charId") == id)
								{
									friendList.add(temp.getInt("charId"));
									count++;
								}
							}
						}
						friendList.add(_player.getObjectId());
						packet.writeD(count);
						for (int id : _playerList.keySet())
						{
							final StatSet player = _playerList.get(id);
							if (friendList.contains(player.getInt("charId")))
							{
								packet.writeString(player.getString("name"));
								packet.writeString(player.getString("clanName"));
								packet.writeD(player.getInt("level"));
								packet.writeD(player.getInt("classId"));
								packet.writeD(player.getInt("race"));
								packet.writeD(id); // friend rank
								if (!_snapshotList.isEmpty())
								{
									for (Integer id2 : _snapshotList.keySet())
									{
										final StatSet snapshot = _snapshotList.get(id2);
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											packet.writeD(id2); // server rank snapshot
											packet.writeD(snapshot.getInt("raceRank", 0)); // race rank snapshot
											packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
										}
									}
								}
								else
								{
									packet.writeD(id);
									packet.writeD(0);
									packet.writeD(0);
								}
							}
						}
					}
					else
					{
						packet.writeD(1);
						packet.writeString(_player.getName());
						if (_player.getClan() != null)
						{
							packet.writeString(_player.getClan().getName());
						}
						else
						{
							packet.writeString("");
						}
						packet.writeD(_player.getStat().getBaseLevel());
						packet.writeD(_player.getBaseClass());
						packet.writeD(_player.getRace().ordinal());
						packet.writeD(1); // clan rank
						if (!_snapshotList.isEmpty())
						{
							for (Integer id : _snapshotList.keySet())
							{
								final StatSet snapshot = _snapshotList.get(id);
								if (_player.getObjectId() == snapshot.getInt("charId"))
								{
									packet.writeD(id); // server rank snapshot
									packet.writeD(snapshot.getInt("raceRank", 0)); // race rank snapshot
									packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
								}
							}
						}
						else
						{
							packet.writeD(0);
							packet.writeD(0);
							packet.writeD(0);
						}
					}
					break;
				}
				case 4: // class
				{
					if (_scope == 0) // all
					{
						int count = 0;
						for (int i = 1; i <= _playerList.size(); i++)
						{
							final StatSet player = _playerList.get(i);
							if (_class == player.getInt("classId"))
							{
								count++;
							}
						}
						packet.writeD(count > 100 ? 100 : count);
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatSet player = _playerList.get(id);
							if (_class == player.getInt("classId"))
							{
								packet.writeString(player.getString("name"));
								packet.writeString(player.getString("clanName"));
								packet.writeD(player.getInt("level"));
								packet.writeD(player.getInt("classId"));
								packet.writeD(player.getInt("race"));
								packet.writeD(i); // server rank
								if (_snapshotList.size() > 0)
								{
									final Map<Integer, StatSet> snapshotClassList = new ConcurrentHashMap<>();
									int j = 1;
									for (Integer id2 : _snapshotList.keySet())
									{
										final StatSet snapshot = _snapshotList.get(id2);
										if (_class == snapshot.getInt("classId"))
										{
											snapshotClassList.put(j, _snapshotList.get(id2));
											j++;
										}
									}
									for (Integer id2 : snapshotClassList.keySet())
									{
										final StatSet snapshot = snapshotClassList.get(id2);
										if (player.getInt("charId") == snapshot.getInt("charId"))
										{
											packet.writeD(id2); // server rank snapshot
											packet.writeD(snapshot.getInt("raceRank", 0)); // race rank snapshot
											packet.writeD(snapshot.getInt("classRank", 0)); // nClassRank_Snapshot
										}
									}
								}
								else
								{
									packet.writeD(i);
									packet.writeD(i);
									packet.writeD(i); // nClassRank_Snapshot?
								}
								i++;
							}
						}
					}
					else
					{
						boolean found = false;
						
						final Map<Integer, StatSet> classList = new ConcurrentHashMap<>();
						int i = 1;
						for (Integer id : _playerList.keySet())
						{
							final StatSet set = _playerList.get(id);
							if (_player.getBaseClass() == set.getInt("classId"))
							{
								classList.put(i, _playerList.get(id));
								i++;
							}
						}
						
						for (Integer id : classList.keySet())
						{
							final StatSet player = classList.get(id);
							if (player.getInt("charId") == _player.getObjectId())
							{
								found = true;
								final int first = id > 10 ? (id - 9) : 1;
								final int last = classList.size() >= (id + 10) ? id + 10 : id + (classList.size() - id);
								if (first == 1)
								{
									packet.writeD(last - (first - 1));
								}
								else
								{
									packet.writeD(last - first);
								}
								for (int id2 = first; id2 <= last; id2++)
								{
									final StatSet plr = classList.get(id2);
									packet.writeString(plr.getString("name"));
									packet.writeString(plr.getString("clanName"));
									packet.writeD(plr.getInt("level"));
									packet.writeD(plr.getInt("classId"));
									packet.writeD(plr.getInt("race"));
									packet.writeD(id2); // server rank
									packet.writeD(id2);
									packet.writeD(id2);
									packet.writeD(id2); // nClassRank_Snapshot?
								}
							}
						}
						if (!found)
						{
							packet.writeD(0);
						}
					}
					break;
				}
			}
		}
		else
		{
			packet.writeD(0);
		}
		return true;
	}
}
