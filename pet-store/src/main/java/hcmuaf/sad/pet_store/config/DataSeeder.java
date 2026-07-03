package hcmuaf.sad.pet_store.config;

import hcmuaf.sad.pet_store.entity.*;
import hcmuaf.sad.pet_store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds sample product data on startup for testing/demo purposes.
 * Runs only when the database is empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() == 0) {
            log.info("DataSeeder: Seeding sample categories and products...");

        // ── Categories ──────────────────────────────────────────────────
        Category food = createCategory("Thức ăn", "Thức ăn dinh dưỡng cho mọi loại thú cưng");
        Category toy  = createCategory("Đồ chơi", "Đồ chơi thú vị để thú cưng vui chơi");
        Category acc  = createCategory("Phụ kiện", "Phụ kiện chăm sóc và làm đẹp cho thú cưng");

        // ── Products ─────────────────────────────────────────────────────
        Product royalCanin = createProduct(food, "Royal Canin Chó Trưởng Thành",
                "Thức ăn hạt cao cấp cho chó trưởng thành, bổ sung đầy đủ dưỡng chất");
        addVariant(royalCanin, "Gói 1kg",  BigDecimal.valueOf(185_000), 50);
        addVariant(royalCanin, "Gói 3kg",  BigDecimal.valueOf(490_000), 30);
        addVariant(royalCanin, "Gói 10kg", BigDecimal.valueOf(1_350_000), 10);

        Product whiskas = createProduct(food, "Whiskas Mèo Trưởng Thành Cá Ngừ",
                "Thức ăn ướt cho mèo trưởng thành, hương vị cá ngừ tươi ngon");
        addVariant(whiskas, "Hộp 85g",  BigDecimal.valueOf(22_000), 100);
        addVariant(whiskas, "Thùng 24 hộp", BigDecimal.valueOf(480_000), 20);

        Product fishFood = createProduct(food, "Tetra Min Thức Ăn Cá Cảnh",
                "Thức ăn dạng vảy đa năng cho cá cảnh nhiệt đới");
        addVariant(fishFood, "Hũ 52g",  BigDecimal.valueOf(75_000), 80);
        addVariant(fishFood, "Hũ 200g", BigDecimal.valueOf(245_000), 40);

        Product toyBall = createProduct(toy, "Bóng Cao Su Tương Tác Cho Chó",
                "Bóng cao su tự nhiên, bền bỉ, kích thích vận động cho chó");
        addVariant(toyBall, "Size S (đường kính 5cm)", BigDecimal.valueOf(45_000), 60);
        addVariant(toyBall, "Size M (đường kính 7cm)", BigDecimal.valueOf(65_000), 60);
        addVariant(toyBall, "Size L (đường kính 10cm)", BigDecimal.valueOf(85_000), 40);

        Product catToy = createProduct(toy, "Đũa Lông Vũ Câu Mèo",
                "Đồ chơi câu mèo với lông vũ sặc sỡ, kích thích bản năng săn mồi");
        addVariant(catToy, "Bộ 1 cái", BigDecimal.valueOf(35_000), 120);
        addVariant(catToy, "Bộ 3 cái", BigDecimal.valueOf(89_000), 50);

        Product hamsterWheel = createProduct(toy, "Bánh Xe Chạy Cho Hamster",
                "Bánh xe im lặng, giúp hamster vận động mỗi ngày");
        addVariant(hamsterWheel, "Đường kính 15cm - Trắng", BigDecimal.valueOf(89_000), 35);
        addVariant(hamsterWheel, "Đường kính 21cm - Hồng",  BigDecimal.valueOf(125_000), 25);

        Product collar = createProduct(acc, "Vòng Cổ Chó Da Thật",
                "Vòng cổ da bò tự nhiên, chắc chắn và thời trang cho chó");
        addVariant(collar, "Size S (cổ 25-35cm)", BigDecimal.valueOf(165_000), 30);
        addVariant(collar, "Size M (cổ 35-50cm)", BigDecimal.valueOf(195_000), 30);
        addVariant(collar, "Size L (cổ 50-65cm)", BigDecimal.valueOf(225_000), 20);

        Product shampoo = createProduct(acc, "Sữa Tắm Thú Cưng Bio-Pet",
                "Sữa tắm thiên nhiên, an toàn cho da nhạy cảm, hương thơm dịu nhẹ");
        addVariant(shampoo, "Chai 300ml - Hương Lavender", BigDecimal.valueOf(135_000), 45);
        addVariant(shampoo, "Chai 500ml - Hương Chamomile", BigDecimal.valueOf(195_000), 35);

        Product carrier = createProduct(acc, "Túi Vận Chuyển Thú Cưng",
                "Túi thoáng khí, tiện lợi cho việc di chuyển cùng thú cưng");
        addVariant(carrier, "Size S (cho chó/mèo < 5kg)", BigDecimal.valueOf(345_000), 20);
        addVariant(carrier, "Size M (cho chó/mèo < 10kg)", BigDecimal.valueOf(495_000), 15);

            log.info("DataSeeder: Seeded {} categories, {} products.", categoryRepository.count(), productRepository.count());
        }

        // ── Seed Dummy Orders ────────────────────────────────────────────
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@petstore.com");
            admin.setPassword("{noop}admin123");
            admin.setFullName("Admin User");
            admin.setRole("ADMIN");
            userRepository.save(admin);

            User customer = new User();
            customer.setEmail("customer@petstore.com");
            customer.setPassword("{noop}123456");
            customer.setFullName("Lư Trần Bảo Ngọc");
            customer.setRole("CUSTOMER");
            userRepository.save(customer);

            // Order 1 (NEW, UNPAID)
            Order order1 = new Order();
            order1.setUser(customer);
            order1.setRecipientName("Bảo Ngọc");
            order1.setRecipientPhone("0901234567");
            order1.setDeliveryAddress("Đại học Nông Lâm, Thủ Đức, TP.HCM");
            order1.setShippingFee(new BigDecimal("30000"));
            order1.setSubtotal(new BigDecimal("220000"));
            order1.setTotalAmount(new BigDecimal("250000"));
            order1.setOrderStatus(Order.OrderStatus.NEW);
            order1.setPaymentStatus(Order.PaymentStatus.UNPAID);
            order1.setNote("Giao giờ hành chính");
            
            OrderItem item1 = new OrderItem();
            item1.setOrder(order1);
            item1.setProductName("Whiskas Mèo Trưởng Thành Cá Ngừ");
            item1.setVariantName("Hộp 85g");
            item1.setUnitPrice(new BigDecimal("22000"));
            item1.setQuantity(10);
            item1.setSubtotal(new BigDecimal("220000"));
            order1.setItems(List.of(item1));
            orderRepository.save(order1);

            // Order 2 (SHIPPING, PAID)
            Order order2 = new Order();
            order2.setUser(customer);
            order2.setRecipientName("Bảo Ngọc");
            order2.setRecipientPhone("0901234567");
            order2.setDeliveryAddress("Khu Công Nghệ Cao, Thủ Đức, TP.HCM");
            order2.setShippingFee(new BigDecimal("22000"));
            order2.setSubtotal(new BigDecimal("185000"));
            order2.setTotalAmount(new BigDecimal("207000"));
            order2.setOrderStatus(Order.OrderStatus.SHIPPING);
            order2.setPaymentStatus(Order.PaymentStatus.PAID);
            order2.setPaymentMethod("VNPAY");
            
            OrderItem item2 = new OrderItem();
            item2.setOrder(order2);
            item2.setProductName("Royal Canin Chó Trưởng Thành");
            item2.setVariantName("Gói 1kg");
            item2.setUnitPrice(new BigDecimal("185000"));
            item2.setQuantity(1);
            item2.setSubtotal(new BigDecimal("185000"));
            order2.setItems(List.of(item2));
            orderRepository.save(order2);
            
            log.info("DataSeeder: Seeded sample users and orders.");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Category createCategory(String name, String description) {
        Category c = new Category();
        c.setName(name);
        c.setDescription(description);
        c.setActive(true);
        return categoryRepository.save(c);
    }

    private Product createProduct(Category category, String name, String description) {
        Product p = new Product();
        p.setCategory(category);
        p.setName(name);
        p.setDescription(description);
        p.setStatus(Product.Status.ACTIVE);
        return productRepository.save(p);
    }

    private void addVariant(Product product, String name, BigDecimal price, int stock) {
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setName(name);
        v.setPrice(price);
        v.setAvailableStock(stock);
        v.setStatus(ProductVariant.Status.ACTIVE);
        variantRepository.save(v);
    }
}
