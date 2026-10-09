package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.cart.ToppingResponse;
import Web_Drink_Store.webstore.dto.order.*;
import Web_Drink_Store.webstore.entity.*;
import Web_Drink_Store.webstore.enums.*;
import Web_Drink_Store.webstore.exception.*;
import Web_Drink_Store.webstore.repository.*;
import Web_Drink_Store.webstore.service.OrderService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final UserRepository users;
    private final CartRepository carts;
    private final CartItemRepository cartItems;
    private final ProductRepository products;
    private final PromotionRepository promotions;

    public OrderServiceImpl(
            OrderRepository orders,
            OrderItemRepository orderItems,
            UserRepository users,
            CartRepository carts,
            CartItemRepository cartItems,
            ProductRepository products,
            PromotionRepository promotions
    ) {
        this.orders = orders;
        this.orderItems = orderItems;
        this.users = users;
        this.carts = carts;
        this.cartItems = cartItems;
        this.products = products;
        this.promotions = promotions;
    }

    // =========================
    // TẠO ĐƠN HÀNG
    // =========================
    @Override
    @Transactional
    public OrderResponse create(
            Long userId,
            OrderRequest request
    ) {

        User user = users.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy user"
                        )
                );

        // =========================
        // KIỂM TRA THÔNG TIN GIAO HÀNG
        // =========================

        if (request.getReceiverName() == null
                || request.getReceiverName().isBlank()) {

            throw new BadRequestException(
                    "Tên người nhận không được để trống"
            );
        }

        if (request.getReceiverPhone() == null
                || request.getReceiverPhone().isBlank()) {

            throw new BadRequestException(
                    "Số điện thoại không được để trống"
            );
        }

        if (request.getShippingAddress() == null
                || request.getShippingAddress().isBlank()) {

            throw new BadRequestException(
                    "Địa chỉ giao hàng không được để trống"
            );
        }

        // =========================
        // LẤY GIỎ HÀNG
        // =========================

        Cart cart = carts.findByUserId(userId)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Giỏ hàng trống"
                        )
                );

        List<CartItem> cartItemList =
                cartItems.findByCartId(
                        cart.getId()
                );

        if (cartItemList.isEmpty()) {

            throw new BadRequestException(
                    "Giỏ hàng trống"
            );
        }

        // =========================
        // TÍNH SUBTOTAL
        // =========================

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItemList) {

            Product product =
                    cartItem.getProduct();

            if (product.getStatus()
                    != ProductStatus.ACTIVE) {

                throw new BadRequestException(
                        "Sản phẩm "
                                + product.getName()
                                + " không hoạt động"
                );
            }

            if (cartItem.getQuantity()
                    > product.getStockQuantity()) {

                throw new BadRequestException(
                        "Sản phẩm "
                                + product.getName()
                                + " không đủ hàng"
                );
            }

            // Dùng unitPrice đã tính sẵn trong CartItem (bao gồm size + toppings)
            BigDecimal lineTotal =
                    cartItem.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            cartItem.getQuantity()
                                    )
                            );

            subtotal =
                    subtotal.add(lineTotal);
        }

        // =========================
        // XỬ LÝ PROMOTION
        // =========================

        Promotion promotion = null;
        BigDecimal discount =
                BigDecimal.ZERO;

        if (request.getPromotionCode() != null
                && !request.getPromotionCode()
                .isBlank()) {

            promotion =
                    promotions
                            .findByCode(
                                    request
                                            .getPromotionCode()
                                            .trim()
                            )
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "Mã khuyến mãi không hợp lệ"
                                    )
                            );

            LocalDateTime now =
                    LocalDateTime.now();

            if (promotion.getStatus()
                    != PromotionStatus.ACTIVE) {

                throw new BadRequestException(
                        "Khuyến mãi không còn hiệu lực"
                );
            }

            if (promotion.getStartAt() != null
                    && now.isBefore(
                    promotion.getStartAt()
            )) {

                throw new BadRequestException(
                        "Khuyến mãi chưa bắt đầu"
                );
            }

            if (promotion.getEndAt() != null
                    && now.isAfter(
                    promotion.getEndAt()
            )) {

                throw new BadRequestException(
                        "Khuyến mãi đã hết hạn"
                );
            }

            if (promotion.getMinOrderValue() != null
                    && subtotal.compareTo(
                    promotion.getMinOrderValue()
            ) < 0) {

                throw new BadRequestException(
                        "Chưa đạt giá trị đơn tối thiểu"
                );
            }

            if (promotion.getDiscountType()
                    == DiscountType.PERCENT) {

                discount =
                        subtotal
                                .multiply(
                                        promotion
                                                .getDiscountValue()
                                )
                                .divide(
                                        BigDecimal.valueOf(100),
                                        2,
                                        RoundingMode.HALF_UP
                                );

            } else {

                discount =
                        promotion.getDiscountValue();
            }

            if (promotion.getMaxDiscountValue()
                    != null
                    && discount.compareTo(
                    promotion
                            .getMaxDiscountValue()
            ) > 0) {

                discount =
                        promotion
                                .getMaxDiscountValue();
            }

            // Không để tổng tiền âm
            if (discount.compareTo(subtotal) > 0) {

                discount = subtotal;
            }
        }

        // =========================
        // TẠO ORDER
        // =========================

        Order order = new Order();

        order.setUser(user);
        order.setPromotion(promotion);

        order.setStatus(
                OrderStatus.PENDING
        );

        order.setPaymentMethod(
                PaymentMethod.COD
        );

        // Snapshot thông tin giao hàng
        order.setReceiverName(
                request.getReceiverName().trim()
        );

        order.setReceiverPhone(
                request.getReceiverPhone().trim()
        );

        order.setShippingAddress(
                request.getShippingAddress().trim()
        );

        order.setSubtotal(subtotal);

        order.setDiscountAmount(
                discount
        );

        order.setTotalAmount(
                subtotal.subtract(discount)
        );

        orders.save(order);

        // =========================
        // TẠO ORDER ITEM
        // =========================

        for (CartItem cartItem :
                cartItemList) {

            Product product =
                    cartItem.getProduct();

            OrderItem orderItem =
                    new OrderItem();

            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());

            // Dùng unitPrice từ CartItem (đã bao gồm size + toppings surcharge)
            BigDecimal itemUnitPrice = cartItem.getUnitPrice();
            orderItem.setUnitPrice(itemUnitPrice);
            orderItem.setLineTotal(
                    itemUnitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );

            // Snapshot cấu hình đồ uống
            orderItem.setSize(cartItem.getSize());
            orderItem.setSweetness(cartItem.getSweetness());
            orderItem.setIce(cartItem.getIce());
            orderItem.setToppings(
                    cartItem.getToppings().stream()
                            .map(t -> new ToppingItem(t.getToppingId(), t.getToppingName(), t.getPrice()))
                            .collect(Collectors.toList())
            );

            orderItems.save(orderItem);

            // Trừ tồn kho
            product.setStockQuantity(
                    product.getStockQuantity() - cartItem.getQuantity()
            );
            products.save(product);
        }

        // =========================
        // XÓA GIỎ HÀNG
        // =========================

        cartItems.deleteByCartId(
                cart.getId()
        );

        return map(order);
    }

    // =========================
    // USER XEM DANH SÁCH ORDER
    // =========================
    @Override
    public List<OrderResponse> getMyOrders(
            Long userId
    ) {

        return orders
                .findByUserIdOrderByCreatedAtDesc(
                        userId
                )
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================
    // USER XEM CHI TIẾT ORDER
    // =========================
    @Override
    public OrderResponse getMyOrderById(
            Long userId,
            Long orderId
    ) {

        Order order =
                orders
                        .findByIdAndUserId(
                                orderId,
                                userId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy đơn hàng"
                                )
                        );

        return map(order);
    }

    // =========================
    // ADMIN XEM TẤT CẢ ORDER
    // =========================
    @Override
    public List<OrderResponse> getAll() {

        return orders
                .findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    // =========================
    // ADMIN CẬP NHẬT TRẠNG THÁI
    // =========================
    @Override
    @Transactional
    public OrderResponse updateStatus(
            Long id,
            OrderStatus newStatus
    ) {

        if (newStatus == null) {

            throw new BadRequestException(
                    "Trạng thái không được để trống"
            );
        }

        Order order =
                orders.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy order"
                                )
                        );

        OrderStatus currentStatus =
                order.getStatus();

        boolean valid = false;

        switch (currentStatus) {

            case PENDING:

                valid =
                        newStatus
                                == OrderStatus.CONFIRMED
                                ||
                                newStatus
                                        == OrderStatus.CANCELLED;

                break;

            case CONFIRMED:

                valid =
                        newStatus
                                == OrderStatus.SHIPPING
                                ||
                                newStatus
                                        == OrderStatus.CANCELLED;

                break;

            case SHIPPING:

                valid =
                        newStatus
                                == OrderStatus.COMPLETED;

                break;

            case COMPLETED:
            case CANCELLED:

                valid = false;
                break;
        }

        if (!valid) {

            throw new BadRequestException(
                    "Không thể chuyển trạng thái từ "
                            + currentStatus
                            + " sang "
                            + newStatus
            );
        }

        // =========================
        // CANCEL -> HOÀN TỒN KHO
        // =========================

        if (newStatus
                == OrderStatus.CANCELLED) {

            List<OrderItem> items =
                    orderItems
                            .findByOrderId(
                                    order.getId()
                            );

            for (OrderItem item : items) {

                Product product =
                        item.getProduct();

                product.setStockQuantity(
                        product.getStockQuantity()
                                + item.getQuantity()
                );

                products.save(product);
            }
        }

        order.setStatus(
                newStatus
        );

        return map(
                orders.save(order)
        );
    }

    // =========================
    // ENTITY -> RESPONSE
    // =========================
    private OrderResponse map(
            Order order
    ) {

        List<OrderItemResponse> itemResponses =
                orderItems
                        .findByOrderId(order.getId())
                        .stream()
                        .map(item -> {
                            List<ToppingResponse> toppingResponses = item.getToppings().stream()
                                    .map(t -> new ToppingResponse(t.getToppingId(), t.getToppingName(), t.getPrice()))
                                    .collect(Collectors.toList());
                            return new OrderItemResponse(
                                    item.getProduct().getId(),
                                    item.getProduct().getName(),
                                    item.getQuantity(),
                                    item.getUnitPrice(),
                                    item.getLineTotal(),
                                    item.getSize() != null ? item.getSize().name() : null,
                                    item.getSweetness(),
                                    item.getIce() != null ? item.getIce().name() : null,
                                    toppingResponses
                            );
                        })
                        .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentMethod(),

                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getShippingAddress(),

                order.getSubtotal(),
                order.getDiscountAmount(),
                order.getTotalAmount(),
                order.getCreatedAt(),

                itemResponses
        );
    }
}