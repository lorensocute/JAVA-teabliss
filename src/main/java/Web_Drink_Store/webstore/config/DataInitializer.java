package Web_Drink_Store.webstore.config;

import Web_Drink_Store.webstore.entity.Category;
import Web_Drink_Store.webstore.entity.Product;
import Web_Drink_Store.webstore.enums.CategoryStatus;
import Web_Drink_Store.webstore.enums.ProductStatus;
import Web_Drink_Store.webstore.repository.CategoryRepository;
import Web_Drink_Store.webstore.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(CategoryRepository categoryRepo, ProductRepository productRepo) {
        return args -> {
            // Danh sách Category
            String[] catNames = {"Trà sữa", "Trà trái cây", "Cà phê", "Matcha"};
            String[] catDescs = {
                    "Các loại trà sữa thơm béo kết hợp cùng nhiều loại topping.",
                    "Trà thanh mát kết hợp trái cây tươi và hương vị nhiệt đới.",
                    "Các món cà phê đậm vị dành cho người yêu hương thơm cà phê.",
                    "Các thức uống từ matcha với vị trà xanh đặc trưng."
            };

            for (int i = 0; i < catNames.length; i++) {
                if (!categoryRepo.existsByName(catNames[i])) {
                    Category cat = new Category();
                    cat.setName(catNames[i]);
                    cat.setDescription(catDescs[i]);
                    cat.setStatus(CategoryStatus.ACTIVE);
                    categoryRepo.save(cat);
                }
            }

            // Helper function to create product
            seedProduct(productRepo, categoryRepo, "Trà sữa", "Trà Sữa Trân Châu", 50000, "Trà sữa truyền thống thơm béo kết hợp cùng trân châu dai mềm.");
            seedProduct(productRepo, categoryRepo, "Trà sữa", "Trà Sữa Khoai Môn", 52000, "Vị khoai môn thơm bùi hòa quyện cùng nền trà sữa béo nhẹ.");
            seedProduct(productRepo, categoryRepo, "Trà sữa", "Trà Sữa Ô Long", 55000, "Trà ô long thơm dịu kết hợp sữa tạo nên hương vị cân bằng và dễ uống.");
            seedProduct(productRepo, categoryRepo, "Trà sữa", "Trà Sữa Socola", 55000, "Trà sữa kết hợp socola đậm vị, phù hợp với người yêu vị ngọt béo.");
            seedProduct(productRepo, categoryRepo, "Trà sữa", "Trà Sữa Đường Đen", 59000, "Sữa tươi và trà hòa quyện cùng vị caramel đặc trưng của đường đen.");

            seedProduct(productRepo, categoryRepo, "Trà trái cây", "Trà Đào Cam Sả", 49000, "Trà đào thanh mát kết hợp cam và hương sả thơm nhẹ.");
            seedProduct(productRepo, categoryRepo, "Trà trái cây", "Trà Vải", 49000, "Trà thanh nhẹ kết hợp vị vải ngọt dịu và hương trái cây tươi mát.");
            seedProduct(productRepo, categoryRepo, "Trà trái cây", "Trà Xoài Nhiệt Đới", 52000, "Trà trái cây kết hợp xoài với hương vị nhiệt đới chua ngọt hài hòa.");
            seedProduct(productRepo, categoryRepo, "Trà trái cây", "Trà Dâu", 52000, "Hương trà nhẹ nhàng kết hợp vị dâu chua ngọt và thơm mát.");
            seedProduct(productRepo, categoryRepo, "Trà trái cây", "Trà Chanh Dây", 49000, "Trà thanh mát kết hợp chanh dây chua nhẹ, thích hợp cho ngày nóng.");

            seedProduct(productRepo, categoryRepo, "Cà phê", "Cà Phê Đen", 35000, "Cà phê đậm vị với hương thơm đặc trưng, dành cho người thích vị nguyên bản.");
            seedProduct(productRepo, categoryRepo, "Cà phê", "Cà Phê Sữa", 39000, "Cà phê đậm đà kết hợp sữa tạo vị ngọt béo hài hòa.");
            seedProduct(productRepo, categoryRepo, "Cà phê", "Bạc Xỉu", 42000, "Sữa béo thơm kết hợp một lượng cà phê vừa đủ, nhẹ nhàng và dễ uống.");
            seedProduct(productRepo, categoryRepo, "Cà phê", "Cà Phê Muối", 45000, "Cà phê kết hợp lớp kem muối béo nhẹ tạo vị mặn ngọt cân bằng.");
            seedProduct(productRepo, categoryRepo, "Cà phê", "Cà Phê Dừa", 49000, "Cà phê đậm vị hòa cùng hương dừa béo thơm và mát lạnh.");

            seedProduct(productRepo, categoryRepo, "Matcha", "Matcha Latte", 55000, "Matcha thơm nhẹ kết hợp sữa tạo vị béo và hậu trà xanh đặc trưng.");
            seedProduct(productRepo, categoryRepo, "Matcha", "Matcha Latte Dâu", 59000, "Matcha latte kết hợp dâu chua ngọt tạo hương vị cân bằng và bắt mắt.");
            seedProduct(productRepo, categoryRepo, "Matcha", "Matcha Latte Dừa", 59000, "Matcha kết hợp vị dừa béo thơm tạo nên thức uống thanh nhẹ và độc đáo.");
            seedProduct(productRepo, categoryRepo, "Matcha", "Matcha Kem Sữa", 59000, "Matcha đậm vị kết hợp lớp kem sữa mềm mịn và béo nhẹ.");
            seedProduct(productRepo, categoryRepo, "Matcha", "Matcha Đường Đen", 62000, "Matcha kết hợp đường đen thơm caramel tạo hương vị đậm và ngọt dịu.");
        };
    }

    private void seedProduct(ProductRepository productRepo, CategoryRepository categoryRepo, String catName, String name, double price, String desc) {
        if (!productRepo.existsByName(name)) {
            Optional<Category> catOpt = categoryRepo.findByName(catName);
            if (catOpt.isPresent()) {
                Product p = new Product();
                p.setName(name);
                p.setPrice(BigDecimal.valueOf(price));
                p.setStockQuantity(100);
                p.setDescription(desc);
                p.setImageUrl(null);
                p.setStatus(ProductStatus.ACTIVE);
                p.setCategory(catOpt.get());
                productRepo.save(p);
            }
        }
    }
}
