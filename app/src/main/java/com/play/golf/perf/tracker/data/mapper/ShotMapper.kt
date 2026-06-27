package com.play.golf.perf.tracker.data.mapper

import com.play.golf.perf.tracker.data.local.entity.ShotEntity
import com.play.golf.perf.tracker.data.remote.dto.ShotDto
import com.play.golf.perf.tracker.domain.model.Shot

// ── ShotDto → ShotEntity (remote to local cache) ─────────────────────────────

fun ShotDto.toEntity(playerId: Int): ShotEntity = ShotEntity(
    playerId      = playerId,
    shotId        = shotId,
    club          = club,
    ballSpeed     = ballSpeed,
    launchAngle   = launchAngle,
    distance      = distance,
    spinRate      = spinRate,
    carryDistance = carryDistance,
    peakHeight    = peakHeight,
    landingAngle  = landingAngle
)

// ── ShotEntity → Shot (local cache to domain) ─────────────────────────────────

fun ShotEntity.toDomain(): Shot = Shot(
    shotId        = shotId,
    playerId      = playerId,
    club          = club,
    ballSpeed     = ballSpeed,
    launchAngle   = launchAngle,
    distance      = distance,
    spinRate      = spinRate,
    carryDistance = carryDistance,
    peakHeight    = peakHeight,
    landingAngle  = landingAngle
)

// ── ShotDto → Shot (direct remote to domain — used when no caching needed) ───

fun ShotDto.toDomain(playerId: Int): Shot = Shot(
    shotId        = shotId,
    playerId      = playerId,
    club          = club,
    ballSpeed     = ballSpeed,
    launchAngle   = launchAngle,
    distance      = distance,
    spinRate      = spinRate,
    carryDistance = carryDistance,
    peakHeight    = peakHeight,
    landingAngle  = landingAngle
)