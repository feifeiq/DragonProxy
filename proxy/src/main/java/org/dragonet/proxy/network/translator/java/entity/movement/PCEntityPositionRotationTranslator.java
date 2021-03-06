/*
 * DragonProxy
 * Copyright (C) 2016-2019 Dragonet Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can view the LICENSE file for more details.
 *
 * https://github.com/DragonetMC/DragonProxy
 */
package org.dragonet.proxy.network.translator.java.entity.movement;

import com.github.steveice10.mc.protocol.packet.ingame.server.entity.ServerEntityPositionRotationPacket;
import com.nukkitx.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.MoveEntityAbsolutePacket;
import lombok.extern.log4j.Log4j2;
import org.dragonet.proxy.network.session.ProxySession;
import org.dragonet.proxy.network.session.cache.object.CachedEntity;
import org.dragonet.proxy.network.translator.PacketTranslator;
import org.dragonet.proxy.network.translator.annotations.PCPacketTranslator;

@Log4j2
@PCPacketTranslator(packetClass = ServerEntityPositionRotationPacket.class)
public class PCEntityPositionRotationTranslator extends PacketTranslator<ServerEntityPositionRotationPacket> {
    public static final PCEntityPositionRotationTranslator INSTANCE = new PCEntityPositionRotationTranslator();

    @Override
    public void translate(ProxySession session, ServerEntityPositionRotationPacket packet) {
        CachedEntity cachedEntity = session.getEntityCache().getByRemoteId(packet.getEntityId());
        if(cachedEntity == null) {
            //log.info("(debug) EntityPositionRotation: Cached entity is null");
            return;
        }

        cachedEntity.moveRelative(Vector3f.from(packet.getMoveX(), packet.getMoveY(), packet.getMoveZ()), packet.getPitch(), packet.getYaw());

        Vector3f rotation = Vector3f.from(cachedEntity.getRotation().getX() / (360d / 256d),
            cachedEntity.getRotation().getY() / (360d / 256d), cachedEntity.getRotation().getZ() / (360d / 256d));

        if(cachedEntity.isShouldMove()) {
            MoveEntityAbsolutePacket moveEntityPacket = new MoveEntityAbsolutePacket();
            moveEntityPacket.setRuntimeEntityId(cachedEntity.getProxyEid());
            moveEntityPacket.setPosition(cachedEntity.getOffsetPosition());
            moveEntityPacket.setRotation(rotation);
            moveEntityPacket.setOnGround(packet.isOnGround());
            moveEntityPacket.setTeleported(false);

            session.sendPacket(moveEntityPacket);

            cachedEntity.setShouldMove(false);
        }
    }
}
