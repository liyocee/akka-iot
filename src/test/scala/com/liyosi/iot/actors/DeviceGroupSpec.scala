package com.liyosi.iot.actors

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

/**
 * Created by liyosi on Jun, 2019
 */
class DeviceGroupSpec(_system: ActorSystem)
  extends TestKit(_system = _system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("iot-test-device-group"))

  "should track a new device" in {
    val testProbe = TestProbe()

    val deviceGroupActor = system.actorOf(DeviceGroup.props("groupId"))
    deviceGroupActor.tell(DeviceManager.RequestTrackDevice("groupId", "deviceId"), testProbe.ref)

    testProbe.expectMsg(DeviceManager.DeviceRegistered)
    val trackedDeviceActor1 = testProbe.lastSender

    deviceGroupActor.tell(DeviceManager.RequestTrackDevice("groupId", "deviceId2"), testProbe.ref)
    testProbe.expectMsg(DeviceManager.DeviceRegistered)
    val trackedDeviceActor2 = testProbe.lastSender

    trackedDeviceActor1 should !==(trackedDeviceActor2)

    // ensure the device are tracked correctly and they are functional
    trackedDeviceActor1.tell(Device.RecordTemperature(1L, 30.0), testProbe.ref)
    val response1 = testProbe.expectMsgType[Device.TemperatureRecorded]
    response1.requestId shouldBe 1L

    // try read temperature
    trackedDeviceActor1.tell(Device.ReadTemperature(2L), testProbe.ref)
    val response3 = testProbe.expectMsgType[Device.ReadTemperatureResponse]
    response3.requestId shouldBe 2L
    response3.value shouldBe Some(30)

    trackedDeviceActor2.tell(Device.RecordTemperature(2L, 30.0), testProbe.ref)
    val response2 = testProbe.expectMsgType[Device.TemperatureRecorded]
    response2.requestId shouldBe 2L
  }
}
