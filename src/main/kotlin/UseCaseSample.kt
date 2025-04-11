package org.example

class AcceptOrderUseCaseSample {
    fun run() {
        val logger = ConsoleLogger()
        val orderRepository = MemoryOrderRepository()
        val publisher = MemoryPublisher()

        // Classic style
        val classicUseCase = ClassicAcceptOrderUseCase(logger, orderRepository, publisher)
        classicUseCase.execute("orderId")

        // Context receiver style
        with(logger) {
            with(orderRepository) {
                with(publisher) {
                    ContextAcceptOrderUseCase().execute("orderId")
                }
            }
        }
        // we can do this with `Context Parameters` feature
        // context(logger, orderRepository, publisher) {
        //     ContextAcceptOrderUseCase().execute("orderId")
        // }
    }
}

data class Order(val id: String, val status: String)
data class OrderAcceptedEvent(val order: Order)

interface Repository<T> {
    fun findById(id: String): T?
}

class MemoryOrderRepository : Repository<Order> {
    private val orders = mutableMapOf<String, Order>()

    override fun findById(id: String): Order? {
        return orders[id]
    }
}

interface Publisher<T> {
    fun publish(event: T)
}

class MemoryPublisher : Publisher<OrderAcceptedEvent> {
    private val events = mutableListOf<OrderAcceptedEvent>()

    override fun publish(event: OrderAcceptedEvent) {
        events.add(event)
    }
}

class ClassicAcceptOrderUseCase(
    private val logger: Logger,
    private val repository: Repository<Order>,
    private val publisher: Publisher<OrderAcceptedEvent>,
) {
    fun execute(orderId: String) {
        val order = repository.findById(orderId)
        if (order == null) {
            logger.error("Order not found: $orderId")
            return
        }

        if (order.status != "PENDING") {
            logger.error("Order is not pending: $orderId")
            return
        }

        val updatedOrder = order.copy(status = "ACCEPTED")
        publisher.publish(OrderAcceptedEvent(updatedOrder))
        logger.info("Order accepted: $updatedOrder")
    }
}

class ContextAcceptOrderUseCase {
    // with `Context Parameters` feature, we can use named parameters
    // context(Logger, Repository<Order>, Publisher<OrderAcceptedEvent>)
    context(Logger, Repository<Order>, Publisher<OrderAcceptedEvent>)
    fun execute(orderId: String) {
        val order = findById(orderId)
        if (order == null) {
            error("Order not found: $orderId")
            return
        }

        if (order.status != "PENDING") {
            error("Order is not pending: $orderId")
            return
        }

        val updatedOrder = order.copy(status = "ACCEPTED")
        publish(OrderAcceptedEvent(updatedOrder))
        info("Order accepted: $updatedOrder")
    }
}