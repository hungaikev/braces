package org.h3nk3.braces.backend

import java.util.Date

case class Image(droneId: String, imageId: Long, date: Date, position: Position, pieceResolution: Int, pieces: Array[Array[Int]])