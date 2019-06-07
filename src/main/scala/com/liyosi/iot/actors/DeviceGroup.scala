package com.liyosi.iot.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }

/**
 * Created by liyosi on Jun, 2019
 */
object DeviceGroup {
  def props(groupId: String): Props = Props(new DeviceGroup(groupId))
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {

  var deviceIdToActor: Map[String, ActorRef] = Map.empty

  override def receive: Receive = {
    case trackMsg @ DeviceManager.RequestTrackDevice(`groupId`, _) =>
      deviceIdToActor.get(trackMsg.deviceId) match {
        case Some(deviceActor) => // device exists
          deviceActor forward trackMsg
        case None => // register device the device
          val deviceActor = context.actorOf(Device.props(trackMsg.groupId, trackMsg.deviceId))
          deviceIdToActor += trackMsg.deviceId -> deviceActor
          deviceActor forward trackMsg
      }
    case DeviceManager.RequestTrackDevice(groupId, _) =>
      log.warning("Ignoring TrackDevice request for {} . This actor is responsible for {}", groupId, this.groupId)
  }
}

