package com.play.golf.perf.tracker.data.mapper

import com.play.golf.perf.tracker.data.local.entity.PlayerDetailEntity
import com.play.golf.perf.tracker.data.local.entity.PlayerEntity
import com.play.golf.perf.tracker.data.remote.dto.PlayerDetailDto
import com.play.golf.perf.tracker.data.remote.dto.PlayerDto
import com.play.golf.perf.tracker.domain.model.Player
import com.play.golf.perf.tracker.domain.model.PlayerDetail

// PlayerDto → PlayerEntity (remote to local cache)

fun PlayerDto.toEntity(): PlayerEntity = PlayerEntity(
    id              = id,
    name            = name,
    country         = country,
    club            = club,
    avatarUrl       = avatarUrl,
    averageSpeed    = averageSpeed,
    averageDistance = averageDistance,
    totalShots      = totalShots,
    lastSyncedAt    = System.currentTimeMillis()
)

// PlayerEntity → Player (local cache to domain)

fun PlayerEntity.toDomain(): Player = Player(
    id              = id,
    name            = name,
    country         = country,
    club            = club,
    avatarUrl       = avatarUrl,
    averageSpeed    = averageSpeed,
    averageDistance = averageDistance,
    totalShots      = totalShots
)

// PlayerDetailDto → PlayerDetailEntity (remote to local cache)

fun PlayerDetailDto.toEntity(): PlayerDetailEntity = PlayerDetailEntity(
    id                  = id,
    name                = name,
    country             = country,
    club                = club,
    avatarUrl           = avatarUrl,
    averageSpeed        = averageSpeed,
    averageDistance     = averageDistance,
    totalShots          = totalShots,
    bio                 = bio,
    age                 = age,
    turnsProYear        = turnsProYear,
    majorWins           = majorWins,
    totalWins           = totalWins,
    scoringAverage      = scoringAverage,
    greensInRegulation  = greensInRegulation,
    drivingAccuracy     = drivingAccuracy,
    puttingAverage      = puttingAverage,
    lastSyncedAt        = System.currentTimeMillis()
)

// PlayerDetailEntity + List<ShotEntity> → PlayerDetail (local cache to domain)

fun PlayerDetailEntity.toDomain(shots: List<com.play.golf.perf.tracker.domain.model.Shot>): PlayerDetail =
    PlayerDetail(
        id                  = id,
        name                = name,
        country             = country,
        club                = club,
        avatarUrl           = avatarUrl,
        averageSpeed        = averageSpeed,
        averageDistance     = averageDistance,
        totalShots          = totalShots,
        bio                 = bio,
        age                 = age,
        turnsProYear        = turnsProYear,
        majorWins           = majorWins,
        totalWins           = totalWins,
        scoringAverage      = scoringAverage,
        greensInRegulation  = greensInRegulation,
        drivingAccuracy     = drivingAccuracy,
        puttingAverage      = puttingAverage,
        shots               = shots
    )