package com.example

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import com.example.GreeterMain.Start

object Greeter {
  final case class Greet(whom: String, replyTo: ActorRef[Greeted])
  final case class Greeted(whom: String, from: ActorRef[Greet])

  def apply(): Behavior[Greet] = Behaviors.receive{(context, messgae) =>
    context.log.info("Hello {}!", messgae.whom)
    messgae.replyTo ! Greeted(messgae.whom, context.self)
    Behaviors.same
  }
}

object GreeterBot {

  def apply(max: Int): Behavior[Greeter.Greeted] = {
    bot(0, max)
  }

  private def bot(greetingCounter: Int, max: Int): Behavior[Greeter.Greeted] = Behaviors.receive{(context, messgae) =>
    val n = greetingCounter + 1
    context.log.info("Greeting {} for {}", n, messgae.whom)

    if(n == max) {
      Behaviors.stopped
    } else {
      messgae.from ! Greeter.Greet(messgae.whom, context.self)
      bot(n, max)
    }
  }
}

object GreeterMain {
  final case class Start(name: String)

  def apply(): Behavior[Start] = Behaviors.setup{ context =>
    val greeter = context.spawn(Greeter(), "greeter")

    Behaviors.receiveMessage{ message =>
      val replyTo = context.spawn(GreeterBot(max = 3), message.name)
      greeter ! Greeter.Greet(message.name, replyTo)
      Behaviors.same
    }
  }
}

object AkkaQuickstart extends App {
  val greeterMain: ActorSystem[GreeterMain.Start] = ActorSystem(GreeterMain(), "AkkaQuickStart")
  greeterMain ! Start("Charles")

}
