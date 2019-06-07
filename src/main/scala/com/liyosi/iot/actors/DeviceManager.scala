package com.liyosi.iot.actors

/**
 * Created by liyosi on Jun, 2019
 */
object DeviceManager {

  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}
