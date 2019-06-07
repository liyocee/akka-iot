package com.liyosi.iot.actors

import akka.actor.{ Actor, ActorLogging, ActorRef }
import com.liyosi.iot.actors.DeviceManager.RequestTrackDevice

/**
 * Created by liyosi on Jun, 2019
 */
object DeviceManager {

  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}

class DeviceManager extends Actor with ActorLogging {

  var groupIdToActor: Map[String, ActorRef] = Map.empty
  var actorToGroupId: Map[ActorRef, String] = Map.empty

  override def preStart(): Unit = log.info("Device manager started..")
  override def postStop(): Unit = log.info("Device manager stopped..")

  override def receive: Receive = {
    case trackMsg @ RequestTrackDevice(groupId, _) =>
      groupIdToActor.get(groupId) match {
        case Some(groupActor) =>
          groupActor forward trackMsg

        case None =>
          log.info("Creating device group actor for {}", groupId)
          val groupActor = context.actorOf(DeviceGroup.props(groupId))
          context.watch(groupActor)
          groupIdToActor += groupId -> groupActor
          actorToGroupId += groupActor -> groupId
          groupActor forward trackMsg
      }
  }
}
