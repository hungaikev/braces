/*
 * Copyright © 2015–2017 Lightbend, Inc. All rights reserved.
 * No information contained herein may be reproduced or transmitted in any form
 * or by any means without the express written permission of Lightbend, Inc.
 */
package org.h3nk3.braces.backend

import akka.actor.{Actor, ActorLogging, Props}

object DroneManager {
  def props: Props = Props[DroneManager]
}

class DroneManager extends Actor with ActorLogging {
  def receive: Receive = {
    case _ =>
  }
}
