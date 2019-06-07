package com.liyosi.iot.actors

import akka.actor.{ Actor, ActorLogging, ActorRef, Props, Terminated }
import com.liyosi.iot.actors.DeviceGroup.{ ReplyRequestDeviceList, RequestDeviceList }

/**
 * Created by liyosi on Jun, 2019
 */
object DeviceGroup {
  def props(groupId: String): Props = Props(new DeviceGroup(groupId))

  final case class RequestDeviceList(requestId: Long)
  final case class ReplyRequestDeviceList(requestId: Long, deviceIds: Set[String])

  final case class RequestAllTemperatures(requestId: Long)
  final case class RespondAllTemperatures(requestId: Long, temperatures: Map[String, TemperatureReading])


  sealed trait TemperatureReading
  final case class Temperature(value: Double) extends TemperatureReading
  final case object TemperatureNotAvailable extends TemperatureReading
  final case object DeviceNotAvailable extends TemperatureReading
  final case object DeviceTimedOut extends TemperatureReading
}

class DeviceGroup(groupId: String) extends Actor with ActorLogging {

  var deviceIdToActor: Map[String, ActorRef] = Map.empty
  var actorToDeviceId: Map[ActorRef, String] = Map.empty

  override def preStart(): Unit = log.info("DeviceGroup {} started", groupId)
  override def postStop(): Unit = log.info("DeviceGroup {} stop", groupId)

  override def receive: Receive = {
    case trackMsg @ DeviceManager.RequestTrackDevice(`groupId`, _) =>
      deviceIdToActor.get(trackMsg.deviceId) match {
        case Some(deviceActor) => // device exists
          deviceActor forward trackMsg
        case None => // register device the device
          val deviceActor = context.actorOf(Device.props(trackMsg.groupId, trackMsg.deviceId))
          context.watch(deviceActor)
          deviceIdToActor += trackMsg.deviceId -> deviceActor
          actorToDeviceId += deviceActor -> trackMsg.deviceId
          deviceActor forward trackMsg
      }

    case DeviceManager.RequestTrackDevice(groupId, _) =>
      log.warning("Ignoring TrackDevice request for {} . This actor is responsible for {}", groupId, this.groupId)

    case RequestDeviceList(requestId) =>
      sender() ! ReplyRequestDeviceList(requestId, deviceIdToActor.keySet)

    case Terminated(deviceActor) =>
      val deviceId = actorToDeviceId(deviceActor)
      log.info("Device actor {} has been terminated", deviceActor)
      actorToDeviceId -= deviceActor
      deviceIdToActor -= deviceId
  }
}

