package com.liyosi.iot.actors

import scala.concurrent.duration.FiniteDuration

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import com.liyosi.iot.actors.Device.ReadTemperature
import com.liyosi.iot.actors.DeviceGroupQuery.CollectionTimeout

/**
 * Created by liyosi on Jun, 2019
 */
object DeviceGroupQuery {
  case object CollectionTimeout

  def props(
      actorToDeviceId: Map[ActorRef, String],
      requestId: Long,
      requester: ActorRef,
      timeout: FiniteDuration): Props = Props(new DeviceGroupQuery(actorToDeviceId, requestId, requester, timeout))


}

class DeviceGroupQuery(
    actorToDeviceId: Map[ActorRef, String],
    requestId: Long,
    requester: ActorRef,
    timeout: FiniteDuration) extends Actor with ActorLogging {

  import context.dispatcher

  val queryTimeoutTimer = context.system.scheduler.scheduleOnce(timeout, self, CollectionTimeout)

  override def preStart(): Unit =  {
    actorToDeviceId.keysIterator.foreach { deviceActor =>
      context.watch(deviceActor)
      deviceActor ! ReadTemperature(requestId)
    }
  }

  override def postStop(): Unit = {
    queryTimeoutTimer.cancel()
  }


  override def receive: Receive = waitForReplies(Map.empty, actorToDeviceId.keySet)

  def waitForReplies(
      repliesSoFar: Map[String, DeviceGroup.TemperatureReading],
      stillWaiting: Set[ActorRef]): Receive = {

    case Device.ReadTemperatureResponse(`requestId`, valueOption) =>
      val deviceActor = sender()

      val reading = valueOption match {
        case Some(value) => DeviceGroup.Temperature(value)
        case None => DeviceGroup.TemperatureNotAvailable
      }

      receivedResponse(deviceActor, reading, stillWaiting, repliesSoFar)

    case Terminated(deviceActor) =>
      receivedResponse(deviceActor, DeviceGroup.DeviceNotAvailable, stillWaiting, repliesSoFar)

    case CollectionTimeout =>
      val timeoutReplies: Set[(String, DeviceGroup.TemperatureReading)] = stillWaiting.map { deviceActor =>
        val deviceId = actorToDeviceId(deviceActor)

        deviceId -> DeviceGroup.DeviceTimedOut
      }

      requester ! DeviceGroup.RespondAllTemperatures(requestId, repliesSoFar ++ timeoutReplies)
  }

  def receivedResponse(
      deviceActor: ActorRef,
      reading: DeviceGroup.TemperatureReading,
      stillWating: Set[ActorRef],
      repliesSoFar: Map[String, DeviceGroup.TemperatureReading]): Unit = {

    context.unwatch(deviceActor)
    val deviceId = actorToDeviceId(deviceActor)
    val newStillWaiting = stillWating - deviceActor

    val newRepliesSoFar = repliesSoFar + (deviceId -> reading)

    if (newStillWaiting.isEmpty) {
      requester ! DeviceGroup.RespondAllTemperatures(requestId, newRepliesSoFar)
    } else {
      context.become(waitForReplies(newRepliesSoFar, newStillWaiting))
    }
  }
}


