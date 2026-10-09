package Web_Drink_Store.webstore.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Bảng giá topping phía backend — mapping theo TOPPINGS trong mockData.js.
 * Backend validate và lấy giá từ đây, không tin tuyệt đối giá frontend gửi lên.
 *
 * Nếu frontend gửi topping id hợp lệ → dùng giá bảng này.
 * Nếu topping id không có trong bảng → throw BadRequestException.
 *
 * TOPPINGS trong mockData.js:
 *   { id: 'tapioca',       name: 'Trân châu đen',    price: 5000  }
 *   { id: 'white-tapioca', name: 'Trân châu trắng',  price: 5000  }
 *   { id: 'pudding',       name: 'Pudding trứng',    price: 8000  }
 *   { id: 'jelly',         name: 'Thạch dừa',        price: 5000  }
 *   { id: 'cheese-foam',   name: 'Foam phô mai',     price: 10000 }
 *   { id: 'aloe',          name: 'Nha đam',          price: 7000  }
 *   { id: 'red-bean',      name: 'Đậu đỏ',           price: 6000  }
 *   { id: 'taro-ball',     name: 'Khoai môn viên',   price: 8000  }
 */
public class ToppingPriceTable {

    private static final Map<String, ToppingInfo> TABLE = new HashMap<>();

    static {
        TABLE.put("tapioca",       new ToppingInfo("Trân châu đen",   BigDecimal.valueOf(5000)));
        TABLE.put("white-tapioca", new ToppingInfo("Trân châu trắng", BigDecimal.valueOf(5000)));
        TABLE.put("pudding",       new ToppingInfo("Pudding trứng",   BigDecimal.valueOf(8000)));
        TABLE.put("jelly",         new ToppingInfo("Thạch dừa",       BigDecimal.valueOf(5000)));
        TABLE.put("cheese-foam",   new ToppingInfo("Foam phô mai",    BigDecimal.valueOf(10000)));
        TABLE.put("aloe",          new ToppingInfo("Nha đam",         BigDecimal.valueOf(7000)));
        TABLE.put("red-bean",      new ToppingInfo("Đậu đỏ",          BigDecimal.valueOf(6000)));
        TABLE.put("taro-ball",     new ToppingInfo("Khoai môn viên",  BigDecimal.valueOf(8000)));
    }

    public static boolean isValid(String id) {
        return TABLE.containsKey(id);
    }

    public static ToppingInfo get(String id) {
        return TABLE.get(id);
    }

    public static class ToppingInfo {
        private final String name;
        private final BigDecimal price;

        ToppingInfo(String name, BigDecimal price) {
            this.name = name;
            this.price = price;
        }

        public String getName() { return name; }
        public BigDecimal getPrice() { return price; }
    }
}
