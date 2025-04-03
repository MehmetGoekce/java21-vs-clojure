package com.example.ecommerce;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstration of the E-commerce Order Processing System
 * This class provides a complete working example of the system by creating
 * in-memory implementations of repositories and running sample workflows.
 */
@SuppressWarnings("preview")
public class Demo {

    // Domain Models
    public record Customer(String id, String name, String email) {}

    public record Product(String id, String name, String category, double price, int stockLevel) {}

    public record OrderItem(String productId, int quantity, double price) {
        public double getTotal() {
            return price * quantity;
        }
    }

    public enum OrderStatus {
        CREATED, PAID, PREPARING, SHIPPED
    }

    public record Order(
            String id,
            String customerId,
            List<OrderItem> items,
            LocalDateTime createdAt,
            OrderStatus status,
            PaymentInfo paymentInfo
    ) {
        public double getTotal() {
            return items.stream().mapToDouble(OrderItem::getTotal).sum();
        }

        public Order updateStatus(OrderStatus newStatus) {
            return new Order(id, customerId, items, createdAt, newStatus, paymentInfo);
        }
    }

    public record PaymentInfo(String transactionId, String method, double amount, LocalDateTime processedAt) {}

    public record ShippingInfo(String orderId, String address, String trackingCode, LocalDateTime shippedAt) {}

    // Repository Interfaces
    public interface CustomerRepository {
        Optional<Customer> findById(String id);
        void save(Customer customer);
    }

    public interface ProductRepository {
        Optional<Product> findById(String id);
        List<Product> findAllById(List<String> ids);
        void updateStock(String productId, int newStockLevel);
        void save(Product product);
    }

    public interface OrderRepository {
        void save(Order order);
        Optional<Order> findById(String id);
        List<Order> findByCustomerId(String customerId);
    }

    // Service Interfaces
    public interface InventoryService {
        boolean checkAvailability(String productId, int quantity);
        void reserveStock(String productId, int quantity);
    }

    public interface PaymentService {
        PaymentInfo processPayment(String customerId, double amount, String paymentMethod);
    }

    public interface NotificationService {
        void notifyCustomer(String customerId, String message);
    }

    public interface ShippingService {
        ShippingInfo ship(Order order, String address);
    }

    // In-memory Repository Implementations
    public static class InMemoryCustomerRepository implements CustomerRepository {
        private final Map<String, Customer> customers = new ConcurrentHashMap<>();

        @Override
        public Optional<Customer> findById(String id) {
            return Optional.ofNullable(customers.get(id));
        }

        @Override
        public void save(Customer customer) {
            customers.put(customer.id(), customer);
        }
    }

    public static class InMemoryProductRepository implements ProductRepository {
        private final Map<String, Product> products = new ConcurrentHashMap<>();

        @Override
        public Optional<Product> findById(String id) {
            return Optional.ofNullable(products.get(id));
        }

        @Override
        public List<Product> findAllById(List<String> ids) {
            return ids.stream()
                    .map(products::get)
                    .filter(Objects::nonNull)
                    .toList();
        }

        @Override
        public void updateStock(String productId, int newStockLevel) {
            products.computeIfPresent(productId, (id, product) ->
                    new Product(id, product.name(), product.category(), product.price(), newStockLevel));
        }

        @Override
        public void save(Product product) {
            products.put(product.id(), product);
        }
    }

    public static class InMemoryOrderRepository implements OrderRepository {
        private final Map<String, Order> orders = new ConcurrentHashMap<>();

        @Override
        public void save(Order order) {
            orders.put(order.id(), order);
        }

        @Override
        public Optional<Order> findById(String id) {
            return Optional.ofNullable(orders.get(id));
        }

        @Override
        public List<Order> findByCustomerId(String customerId) {
            return orders.values().stream()
                    .filter(order -> order.customerId().equals(customerId))
                    .toList();
        }
    }

    // Service Implementations
    public static class InventoryServiceImpl implements InventoryService {
        private final ProductRepository productRepository;

        public InventoryServiceImpl(ProductRepository productRepository) {
            this.productRepository = productRepository;
        }

        @Override
        public boolean checkAvailability(String productId, int quantity) {
            return productRepository.findById(productId)
                    .map(product -> product.stockLevel() >= quantity)
                    .orElse(false);
        }

        @Override
        public void reserveStock(String productId, int quantity) {
            productRepository.findById(productId).ifPresent(product -> {
                int newStockLevel = product.stockLevel() - quantity;
                if (newStockLevel >= 0) {
                    productRepository.updateStock(productId, newStockLevel);
                } else {
                    throw new IllegalStateException(STR."Not enough stock for product: \{productId}");
                }
            });
        }
    }

    public static class PaymentServiceImpl implements PaymentService {
        @Override
        public PaymentInfo processPayment(String customerId, double amount, String paymentMethod) {
            // Simulate payment processing
            String transactionId = UUID.randomUUID().toString();
            return new PaymentInfo(transactionId, paymentMethod, amount, LocalDateTime.now());
        }
    }

    public static class NotificationServiceImpl implements NotificationService {
        @Override
        public void notifyCustomer(String customerId, String message) {
            System.out.println(STR."Notification sent to customer \{customerId}: \{message}");
        }
    }

    public static class ShippingServiceImpl implements ShippingService {
        @Override
        public ShippingInfo ship(Order order, String address) {
            // Simulate shipping process
            String trackingCode = STR."SHIP-\{UUID.randomUUID().toString().substring(0, 8).toUpperCase()}";
            return new ShippingInfo(order.id(), address, trackingCode, LocalDateTime.now());
        }
    }

    // Main Orchestrator Service
    public static class OrderService {
        private final OrderRepository orderRepository;
        private final CustomerRepository customerRepository;
        private final ProductRepository productRepository;
        private final InventoryService inventoryService;
        private final PaymentService paymentService;
        private final NotificationService notificationService;
        private final ShippingService shippingService;

        public OrderService(
                OrderRepository orderRepository,
                CustomerRepository customerRepository,
                ProductRepository productRepository,
                InventoryService inventoryService,
                PaymentService paymentService,
                NotificationService notificationService,
                ShippingService shippingService) {
            this.orderRepository = orderRepository;
            this.customerRepository = customerRepository;
            this.productRepository = productRepository;
            this.inventoryService = inventoryService;
            this.paymentService = paymentService;
            this.notificationService = notificationService;
            this.shippingService = shippingService;
        }

        public Order createOrder(String customerId, Map<String, Integer> productQuantities) {
            // Verify customer exists
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new IllegalArgumentException(STR."Customer not found: \{customerId}"));

            // Get product details and check availability
            List<String> productIds = new ArrayList<>(productQuantities.keySet());
            List<Product> products = productRepository.findAllById(productIds);

            if (products.size() != productIds.size()) {
                throw new IllegalArgumentException("One or more products not found");
            }

            // Check inventory and create order items
            List<OrderItem> items = new ArrayList<>();
            for (Product product : products) {
                int quantity = productQuantities.get(product.id());
                if (!inventoryService.checkAvailability(product.id(), quantity)) {
                    throw new IllegalStateException(STR."Product not available: \{product.id()}");
                }
                items.add(new OrderItem(product.id(), quantity, product.price()));
            }

            // Create order
            String orderId = UUID.randomUUID().toString();
            Order order = new Order(orderId, customerId, items, LocalDateTime.now(),
                    OrderStatus.CREATED, null);

            // Save order
            orderRepository.save(order);

            // Notify customer
            notificationService.notifyCustomer(customerId,
                    STR."Your order \{orderId} has been created successfully.");

            return order;
        }

        public Order processPayment(String orderId, String paymentMethod) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(STR."Order not found: \{orderId}"));

            if (order.status() != OrderStatus.CREATED) {
                throw new IllegalStateException(STR."Order is not in CREATED state: \{orderId}");
            }

            // Process payment
            PaymentInfo paymentInfo = paymentService.processPayment(
                    order.customerId(), order.getTotal(), paymentMethod);

            // Update order status
            Order updatedOrder = new Order(
                    order.id(), order.customerId(), order.items(),
                    order.createdAt(), OrderStatus.PAID, paymentInfo);

            orderRepository.save(updatedOrder);

            // Reserve inventory
            for (OrderItem item : order.items()) {
                inventoryService.reserveStock(item.productId(), item.quantity());
            }

            // Notify customer
            notificationService.notifyCustomer(order.customerId(),
                    STR."Payment for order \{orderId} has been processed successfully.");

            return updatedOrder;
        }

        public Order shipOrder(String orderId, String shippingAddress) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException(STR."Order not found: \{orderId}"));

            if (order.status() != OrderStatus.PAID) {
                throw new IllegalStateException(STR."Order is not in PAID state: \{orderId}");
            }

            // Update order status to prepare
            Order preparingOrder = order.updateStatus(OrderStatus.PREPARING);
            orderRepository.save(preparingOrder);

            // Ship the order
            ShippingInfo shippingInfo = shippingService.ship(order, shippingAddress);

            // Update order status to ship
            Order shippedOrder = preparingOrder.updateStatus(OrderStatus.SHIPPED);
            orderRepository.save(shippedOrder);

            // Notify customer
            notificationService.notifyCustomer(order.customerId(),
                    STR."Your order \{orderId} has been shipped. Tracking code: \{shippingInfo.trackingCode()}");

            return shippedOrder;
        }

        // Using virtual threads for parallel order processing
        public void processOrdersBatch(List<String> orderIds, String paymentMethod) {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<Future<?>> futures = new ArrayList<>();

                for (String orderId : orderIds) {
                    futures.add(executor.submit(() -> {
                        try {
                            processPayment(orderId, paymentMethod);
                            return true; // Successfully processed
                        } catch (Exception e) {
                            System.err.println(STR."Error processing order \{orderId}: \{e.getMessage()}");
                            return false; // Failed to process
                        }
                    }));
                }

                // Wait for all tasks to complete
                for (Future<?> future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        System.err.println(STR."Error waiting for task: \{e.getMessage()}");
                    }
                }
            } catch (Exception e) {
                System.err.println(STR."Error processing orders batch: \{e.getMessage()}");
            }
        }
    }

    // Demo class to run a full example workflow
    public static void main(String[] args) {
        System.out.println("=== E-commerce Order Processing Demo ===");

        // Create repositories
        CustomerRepository customerRepository = new InMemoryCustomerRepository();
        ProductRepository productRepository = new InMemoryProductRepository();
        OrderRepository orderRepository = new InMemoryOrderRepository();

        // Create services
        OrderService orderService = getOrderService(productRepository, orderRepository, customerRepository);

        // Add test data
        customerRepository.save(new Customer("custom1", "John Doe", "john@example.com"));

        productRepository.save(new Product("prod1", "Laptop", "Electronics", 1299.99, 10));
        productRepository.save(new Product("prod2", "Smartphone", "Electronics", 699.99, 20));
        productRepository.save(new Product("prod3", "Headphones", "Accessories", 149.99, 50));

        System.out.println("\n--- Creating Order ---");
        Map<String, Integer> items = new HashMap<>();
        items.put("prod1", 1); // One laptop
        items.put("prod3", 2); // Two headphones

        Order order = orderService.createOrder("custom1", items);
        System.out.println(STR."Created Order: \{order}");
        System.out.println(STR."Order Total: CHF\{order.getTotal()}");

        System.out.println("\n--- Processing Payment ---");
        Order paidOrder = orderService.processPayment(order.id(), "credit_card");
        System.out.println(STR."Payment processed. Order status: \{paidOrder.status()}");
        System.out.println(STR."Payment details: \{paidOrder.paymentInfo()}");

        System.out.println("\n--- Shipping Order ---");
        Order shippedOrder = orderService.shipOrder(order.id(), "123 Main St, New York, NY 10001");
        System.out.println(STR."Order shipped. Status: \{shippedOrder.status()}");

        System.out.println("\n--- Batch Processing Orders ---");
        // Create multiple orders for batch processing
        Map<String, Integer> smallOrder = Map.of("prod2", 1);
        Order order2 = orderService.createOrder("custom1", smallOrder);
        Order order3 = orderService.createOrder("custom1", smallOrder);
        Order order4 = orderService.createOrder("custom1", smallOrder);

        // Process them in parallel using virtual threads
        System.out.println("Processing batch of orders with virtual threads...");
        orderService.processOrdersBatch(
                List.of(order2.id(), order3.id(), order4.id()),
                "paypal"
        );

        System.out.println("\n--- Retrieving Customer Orders ---");
        List<Order> customerOrders = orderRepository.findByCustomerId("custom1");
        System.out.println(STR."Found \{customerOrders.size()} orders for customer:");
        for (Order customerOrder : customerOrders) {
            System.out.println(STR." - Order \{customerOrder.id()}: \{customerOrder.status()}, Total: CHF\{customerOrder.getTotal()}");
        }

        System.out.println("\nDemo completed successfully!");
    }

    private static OrderService getOrderService(ProductRepository productRepository, OrderRepository orderRepository, CustomerRepository customerRepository) {
        InventoryService inventoryService = new InventoryServiceImpl(productRepository);
        PaymentService paymentService = new PaymentServiceImpl();
        NotificationService notificationService = new NotificationServiceImpl();
        ShippingService shippingService = new ShippingServiceImpl();

        return new OrderService(
                orderRepository,
                customerRepository,
                productRepository,
                inventoryService,
                paymentService,
                notificationService,
                shippingService
        );
    }
}