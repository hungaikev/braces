# BRACES

> A shark tracking tool using drones (not including laser equipped sharks in the first version)


## Aim

- have drones patrol shark infested waters and take pictures that are sent to backend
- backend performs image analysis and if there is a suspected shark the drone is instructed to track it
- keep track of battery life of drones and plan when drones must be switched to fully charged ones

## Tech

- use JS to simulate (naive) drones
- use Akka Http for F/E
- use Akka Cluster (sharding) to enable multiple drone sites (one backend should be able to support n client sites)
- use Akka Persistence to persist positions of drones/etc to survive server outages (resilience)

-----------------------------------------------

## Plan and to-do-list

0. Start off by explaining the domain model for Braces. Walk through an architecture diagram with all pieces we want to create.

1. Set up HTTP service : ping service to show how easy it is to get started
  -> explain the bits required : understanding of basic Akka Http building blocks
  -> Akka Http has actors and streams available by default
  -> Explain the actor model, its shortcomings and why streams is the remedy
  -> Power of actor model: Distributed programming
  -> Introduce the Reactive Manifesto

2. Create WS connection and stream messages
  -> introduce Akka Streams and some of its basic functionality
    - Source
    - Sink
    - Flow
    - Graphs
  -> handleWebsocketMessages
  -> Spray JSON intro
  -> show how to debug stages in the flow that throws exceptions
  -> show a couple of FlowOps methods to simulate various scenarios (without having a connected backend)
    (point out similarities with Scala collections):
    - filter
    - map
    - mapConcat (flatMap)
    - mapAsync
    - takeWhile
    - dropWhile
    - throttle
    - batch
    - buffer

3. Implement clients (drones)
  -> connect to backend (hello, I am alive and kicking)
  -> send image (store locally if under back-pressure)
  -> send status (battery power, position, velocity, etc.)
  -> receive instruction
    - cover area using specific pattern
    - goto position
    - return to base
    - track item
    - send status
  -> simulate slow backend and show how this has effect on the clients (back-pressure in effect)
  -> would be cool to draw coast line and show drone positions and movement (air traffic control view) per site (Hawaii/Australia/SA)

4. Akka cluster + persistence
  -> use cluster sharding to determine what actors to use when storing the images (each drone site has its own domain)
  -> real-time analysis of incoming images
  -> simulate back-up of the analyzing process and discuss alternative solutions, e.g. temp store (Kafka?), process and delete
  -> simulate shark found and send instructions to drone(s)
  -> extra: implement switching schemes (keep track of distances from drone to charging station etc.)

5. Testing
  -> unit testing
  -> integration testing (multi-jvm)
  -> pull the plugs/chaos monkeys in da house!

6. Monitoring of system
  -> use sandbox and show off dashboards

7. Deployment of system
  -> use Prod Suite
