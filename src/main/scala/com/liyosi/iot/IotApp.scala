package com.liyosi.iot

import akka.actor.ActorSystem
import com.liyosi.iot.actors.IotSupervisor

/**
 * Created by liyosi on Jun, 2019
 */
object IotApp extends App {

  override def main(args: Array[String]): Unit = {
    var actorSystem: ActorSystem = ActorSystem("iot-system")
    var iotSupervisor = actorSystem.actorOf(IotSupervisor.props, "iot-supervisor")

  }

}
