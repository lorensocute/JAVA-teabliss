package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.cart.*;
import Web_Drink_Store.webstore.entity.*;
import Web_Drink_Store.webstore.enums.DrinkIce;
import Web_Drink_Store.webstore.enums.DrinkSize;
import Web_Drink_Store.webstore.enums.ProductStatus;
import Web_Drink_Store.webstore.exception.BadRequestException;
import Web_Drink_Store.webstore.exception.ResourceNotFoundException;
import Web_Drink_Store.webstore.exception.UnauthorizedException;
import Web_Drink_Store.webstore.repository.CartItemRepository;
import Web_Drink_Store.webstore.repository.CartRepository;
import Web_Drink_Store.webstore.repository.ProductRepository;
import Web_Drink_Store.webstore.repository.UserRepository;
import Web_Drink_Store.webstore.service.CartService;
import Web_Drink_Store.webstore.service.ToppingPriceTable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductRepository products;
    private final UserRepository users;

    public CartServiceImpl(
            CartRepository carts,
            CartItemRepository items,
            ProductRepository products,
            UserRepository users
    ) {
        this.carts = carts;
        this.items = items;
        this.products = products;
        this.users = users;
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private Cart cart(Long userId) {
        return carts.findByUserId(userId)
                .orElseGet(() -> {
                    User user = users.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
                    Cart cart = new Cart();
                    cart.setUser(user);
                    return carts.save(cart);
                });
    }

    /**
     * Parse DrinkSize từ string. Default M nếu null/invalid.
     */
    private DrinkSize parseSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isBlank()) return DrinkSize.M;
        try {
            return DrinkSize.valueOf(sizeStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Size không hợp lệ: " + sizeStr + ". Hợp lệ: M, L, XL");
        }
    }

    /**
     * Parse DrinkIce từ string. Default NORMAL_ICE nếu null/invalid.
     */
    private DrinkIce parseIce(String iceStr) {
        if (iceStr == null || iceStr.isBlank()) return DrinkIce.NORMAL_ICE;
        try {
            return DrinkIce.valueOf(iceStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Ice không hợp lệ: " + iceStr + ". Hợp lệ: HOT, LESS_ICE, NORMAL_ICE, FULL_ICE");
        }
    }

    /**
     * Validate và build danh sách ToppingItem từ request.
     * Backend validate từng topping id và lấy giá từ ToppingPriceTable.
     */
    private List<ToppingItem> buildToppings(List<ToppingRequest> toppingRequests) {
        if (toppingRequests == null || toppingRequests.isEmpty()) return new ArrayList<>();

        List<ToppingItem> result = new ArrayList<>();
        for (ToppingRequest req : toppingRequests) {
            if (req.getId() == null || req.getId().isBlank()) {
                throw new BadRequestException("Topping id không được để trống");
            }
            if (!ToppingPriceTable.isValid(req.getId())) {
                throw new BadRequestException("Topping không hợp lệ: " + req.getId());
            }
            ToppingPriceTable.ToppingInfo info = ToppingPriceTable.get(req.getId());
            result.add(new ToppingItem(req.getId(), info.getName(), info.getPrice()));
        }
        return result;
    }

    /**
     * Validate sweetness.
     * 0 là hợp lệ (Không ngọt). Không dùng !sweetness.
     */
    private int parseSweetness(Integer sweetness) {
        if (sweetness == null) return 70; // default
        int[] valid = {0, 30, 50, 70, 100};
        for (int v : valid) {
            if (sweetness == v) return sweetness;
        }
        throw new BadRequestException("Sweetness không hợp lệ: " + sweetness + ". Hợp lệ: 0, 30, 50, 70, 100");
    }

    /**
     * Tính unit price:
     * unitPrice = product.price + size surcharge + tổng topping surcharge
     * sweetness và ice KHÔNG cộng tiền.
     */
    private BigDecimal calcUnitPrice(Product product, DrinkSize size, List<ToppingItem> toppings) {
        BigDecimal base = product.getPrice();
        BigDecimal sizeSurcharge = size.getSurcharge();
        BigDecimal toppingTotal = toppings.stream()
                .map(ToppingItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return base.add(sizeSurcharge).add(toppingTotal);
    }

    /**
     * Kiểm tra hai danh sách topping có giống nhau không (sort by id).
     */
    private boolean toppingsMatch(List<ToppingItem> a, List<ToppingItem> b) {
        if (a.size() != b.size()) return false;
        List<String> aIds = a.stream().map(ToppingItem::getToppingId).sorted().collect(Collectors.toList());
        List<String> bIds = b.stream().map(ToppingItem::getToppingId).sorted().collect(Collectors.toList());
        return aIds.equals(bIds);
    }

    /**
     * Tìm CartItem có cùng cấu hình (product + size + sweetness + ice + toppings).
     * Chỉ merge khi TẤT CẢ option giống nhau.
     */
    private Optional<CartItem> findMatchingItem(
            Long cartId,
            Long productId,
            DrinkSize size,
            int sweetness,
            DrinkIce ice,
            List<ToppingItem> toppings
    ) {
        return items.findByCartId(cartId).stream()
                .filter(item ->
                        item.getProduct().getId().equals(productId)
                        && item.getSize() == size
                        && item.getSweetness() == sweetness
                        && item.getIce() == ice
                        && toppingsMatch(item.getToppings(), toppings)
                )
                .findFirst();
    }

    /**
     * Map CartItem → CartItemResponse.
     */
    private CartItemResponse toResponse(CartItem item) {
        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        List<ToppingResponse> toppingResponses = item.getToppings().stream()
                .map(t -> new ToppingResponse(t.getToppingId(), t.getToppingName(), t.getPrice()))
                .collect(Collectors.toList());

        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getUnitPrice(),
                item.getQuantity(),
                lineTotal,
                item.getSize() != null ? item.getSize().name() : null,
                item.getSweetness(),
                item.getIce() != null ? item.getIce().name() : null,
                toppingResponses
        );
    }

    /**
     * Build CartResponse từ Cart entity.
     */
    private CartResponse map(Cart cart) {
        List<CartItemResponse> responses = items.findByCartId(cart.getId())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        BigDecimal total = responses.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(cart.getId(), responses, total);
    }

    // ── Service methods ────────────────────────────────────────────────────

    @Override
    public CartResponse getCart(Long userId) {
        return map(cart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request) {

        // Validate quantity
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new BadRequestException("Số lượng phải > 0");
        }

        Cart cart = cart(userId);

        // Validate product
        Product product = products.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy product"));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không hoạt động");
        }

        // Parse và validate options
        DrinkSize size = parseSize(request.getSize());
        int sweetness = parseSweetness(request.getSweetness());
        DrinkIce ice = parseIce(request.getIce());
        List<ToppingItem> toppings = buildToppings(request.getToppings());

        // Tính unitPrice ở backend
        BigDecimal unitPrice = calcUnitPrice(product, size, toppings);

        // Tìm CartItem có cùng cấu hình để merge
        Optional<CartItem> existing = findMatchingItem(
                cart.getId(), product.getId(), size, sweetness, ice, toppings);

        CartItem item;
        int newQuantity;

        if (existing.isPresent()) {
            // Merge: cộng thêm quantity
            item = existing.get();
            newQuantity = item.getQuantity() + request.getQuantity();
        } else {
            // Tạo mới
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setSize(size);
            item.setSweetness(sweetness);
            item.setIce(ice);
            item.setToppings(toppings);
            newQuantity = request.getQuantity();
        }

        if (newQuantity > product.getStockQuantity()) {
            throw new BadRequestException("Vượt quá tồn kho");
        }

        item.setQuantity(newQuantity);
        item.setUnitPrice(unitPrice);

        items.save(item);

        cart.touch();
        carts.save(cart);

        return map(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, Integer quantity) {

        if (quantity == null || quantity <= 0) {
            throw new BadRequestException("Số lượng phải > 0");
        }

        Cart cart = cart(userId);

        CartItem item = items.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cart item"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Không có quyền");
        }

        if (item.getProduct().getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không hoạt động");
        }

        if (quantity > item.getProduct().getStockQuantity()) {
            throw new BadRequestException("Vượt quá tồn kho");
        }

        item.setQuantity(quantity);
        items.save(item);

        cart.touch();
        carts.save(cart);

        return map(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {

        Cart cart = cart(userId);

        CartItem item = items.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy cart item"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new UnauthorizedException("Không có quyền");
        }

        items.delete(item);

        cart.touch();
        carts.save(cart);

        return map(cart);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        Cart cart = cart(userId);
        items.deleteByCartId(cart.getId());
        cart.touch();
        carts.save(cart);
    }
}