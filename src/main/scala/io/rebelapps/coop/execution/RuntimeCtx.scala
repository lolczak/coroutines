package io.rebelapps.coop.execution

import java.util.UUID

import scala.collection.immutable.Queue

case class RuntimeCtx(running: Set[Fiber[Any]] = Set.empty,
                      ready: Queue[Fiber[Any]] = Queue.empty,
                      suspended: Map[UUID, Fiber[Any]] = Map.empty,
                      channels: Map[UUID, SimpleChannel[Any]] = Map.empty) {

  def enqueueReady(fiber: Fiber[Any]): RuntimeCtx = {
    this.copy(ready = ready.enqueue(fiber))
  }

  def hasReadyFibers(): Boolean = ready.nonEmpty

  def moveFirstReadyToRunning(): (RuntimeCtx, Fiber[Any]) = {
    val (fiber, queue) = ready.dequeue
    this.copy(ready = queue, running = running + fiber) -> fiber
  }

  def removeRunning(fiber: Fiber[Any]): RuntimeCtx = this.copy(running = running - fiber)

  def removeSuspended(requestId: RequestId): (RuntimeCtx, Fiber[Any]) = {
    val fiber = suspended(requestId)
    this.copy(suspended = suspended - requestId) -> fiber
  }

  def addSuspended(requestId: RequestId, fiber: Fiber[Any]): RuntimeCtx = {
    this.copy(suspended = suspended + (requestId -> fiber))
  }

  def upsertChannel(id: UUID, channel: SimpleChannel[Any]): RuntimeCtx = {
    this.copy(channels = channels + (id -> channel))
  }

  def getChannel(id: UUID): SimpleChannel[Any] = channels(id)

}
