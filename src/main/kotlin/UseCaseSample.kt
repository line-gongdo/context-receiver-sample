package org.example

class AcceptOrderUseCaseSample {
    fun run() {
        val logger = ConsoleLogger()
        val orderRepository = MemoryOrderFindOrderService()
        //val orderRepository = StubOrderRepository()
        val publisher = MemoryPublisher()

        // Classic style
        val classicUseCase = ClassicAcceptOrderUseCase(logger, orderRepository, publisher)
        classicUseCase.execute("orderId")

        // Context receiver style
        with(logger) {
            with(orderRepository) {
                with(publisher) {
                    ContextAcceptOrderUseCase().acceptByOrderId("orderId")
                }
            }
        }
        // we can do this with `Context Parameters` feature
        // context(logger, orderRepository, publisher) {
        //     ContextAcceptOrderUseCase().execute("orderId")
        // }

        // currently, it sucks in many ways
//        with(logger) {
//            with(orderRepository) {
//                with(publisher) {
//                    with(StubPickupCodeService()) {
//                        ContextAcceptOrderUseCase().acceptByPickupCode("pickupCode")
//                    }
//                }
//            }
//        }
    }
}

data class Order(val id: String, val pickupCode: String, val status: String)
data class OrderAcceptedEvent(val order: Order)

interface FindOrderService<T> {
    fun findById(id: String): T?
}

class MemoryOrderFindOrderService : FindOrderService<Order> {
    private val orders = mutableMapOf<String, Order>()

    override fun findById(id: String): Order? {
        return orders[id]
    }
}

interface PickupCodeService {
    fun findOrderId(pickupCode: String): String
}

class StubPickupCodeService : PickupCodeService {
    override fun findOrderId(pickupCode: String): String {
        return "orderId"
    }
}

class StubOrderFindOrderService : FindOrderService<Order> {
    override fun findById(id: String) = Order(
        id = id,
        pickupCode = "pickupCode",
        status = "PENDING",
    )
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
    private val findOrderService: FindOrderService<Order>,
    private val publisher: Publisher<OrderAcceptedEvent>,
) {
    fun execute(orderId: String) {
        val order = findOrderService.findById(orderId)
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
    // context(logger: Logger, repository: Repository<Order>, publisher: Publisher<OrderAcceptedEvent>)
    context(Logger, FindOrderService<Order>, Publisher<OrderAcceptedEvent>)
    fun acceptByOrderId(orderId: String) {
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

    context(Logger, FindOrderService<Order>, Publisher<OrderAcceptedEvent>, PickupCodeService)
    fun acceptByPickupCode(pickupCode: String) {
        val orderId = findOrderId(pickupCode)
        acceptByOrderId(orderId)
    }
}