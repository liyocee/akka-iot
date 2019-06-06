package com.liyosi.iot.actors

import akka.actor.{Actor, ActorLogging, Props}

/**
 * Created by liyosi on Jun, 2019
 */

object IotSupervisor {

  def props: Props = Props(new IotSupervisor)
}

class IotSupervisor extends Actor with ActorLogging {

  override def preStart(): Unit = log.info("IoT Application started")

  override def postStop(): Unit = log.info("IoT Application stopped")

  override def receive: Receive = Actor.emptyBehavior
}
