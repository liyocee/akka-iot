package com.liyosi.iot.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.liyosi.iot.actors.Device.{ ReadTemperature, ReadTemperatureResponse, RecordTemperature, TemperatureRecorded }

/**
 * Created by liyosi on Jun, 2019
 */

object Device {
  def props(groupId: String, deviceId: String) = Props(new Device(groupId = groupId, deviceId = deviceId))

  final case class RecordTemperature(requestId: Long, value: Double)
  final case class TemperatureRecorded(requestId: Long)

  final case class ReadTemperature(requestId: Long)

  final case class ReadTemperatureResponse(requestId: Long, value: Option[Double])
}

class Device(groupId: String, deviceId: String) extends Actor with ActorLogging {

  var lastTemperatureRead: Option[Double] = None

  override def preStart(): Unit = log.info("Device actor {}-{} started", groupId, deviceId)

  override def postStop(): Unit = log.info("Device actor {}-{} stopped", groupId, deviceId)

  override def receive: Receive = {
    case ReadTemperature(requestId) =>
      sender() ! ReadTemperatureResponse(requestId, lastTemperatureRead)

    case RecordTemperature(requestId, value) =>
      lastTemperatureRead = Some(value)
      sender() ! TemperatureRecorded(requestId)
  }
}
