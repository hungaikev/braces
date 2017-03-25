package org.h3nk3.braces.backend

import java.util.Date

import org.h3nk3.braces.domain.Domain.DronePosition

case class Image(droneId: String, imageId: Long, date: Date, position: DronePosition, pieceResolution: Int, pieces: Array[Array[Int]])
