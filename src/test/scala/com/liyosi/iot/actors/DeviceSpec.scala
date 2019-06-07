package com.liyosi.iot.actors

import akka.actor.ActorSystem
import akka.testkit.{ TestKit, TestProbe }
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import scala.concurrent.duration._

/**
 * Created by liyosi on Jun, 2019
 */
class DeviceSpec(_system: ActorSystem) extends TestKit(_system)
  with Matchers
  with WordSpecLike
  with BeforeAndAfterAll {

  def this() = this(ActorSystem("iot-test-device"))

  "reply with an empty reading if no temperature is known" in {
    val testProbe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(Device.ReadTemperature(1L), testProbe.ref)
    val response = testProbe.expectMsgType[Device.ReadTemperatureResponse]
    response.requestId shouldBe 1L
    response.value shouldBe None
  }

  "record a temperate and return recorded temperature" in {

    val testProbe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    val requestId = 1L
    val temp: Double = 30.0

    deviceActor.tell(Device.RecordTemperature(requestId, temp), testProbe.ref)
    val response = testProbe.expectMsgType[Device.TemperatureRecorded]
    response.requestId shouldBe requestId

    // try to get the temperature
    deviceActor.tell(Device.ReadTemperature(2L), testProbe.ref)
    val readResponse = testProbe.expectMsgType[Device.ReadTemperatureResponse]
    readResponse.requestId shouldBe 2L
    readResponse.value shouldBe Some(temp)
  }

  "successfully track a device for the correct groupId, deviceId tracking" in {
    val testProbe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("group", "device"), testProbe.ref)

    testProbe.expectMsg(DeviceManager.DeviceRegistered)
    testProbe.lastSender shouldBe deviceActor
  }

  "ignore a wrong device tracking request" in {
    val testProbe = TestProbe()
    val deviceActor = system.actorOf(Device.props("group", "device"))

    deviceActor.tell(DeviceManager.RequestTrackDevice("wrongGroup", "wrongDevice"), testProbe.ref)
    testProbe.expectNoMessage(500.milliseconds)
  }
}
