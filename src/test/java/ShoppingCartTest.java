import com.example.shopping.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ShoppingCartTest {
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("Adding an item to to ShoppingCart should return one item")
    void AddingAnItemToShoppingCartShouldReturnOneItem() {
        cart.addItem("cream of mushrooms", 25.50, 22);
        assertEquals(1, cart.itemCount(), "Cart should contain only one item");
    }

    @Test
    @DisplayName("Deleting an item should return an empty cart")
    void DeletingAnItemShouldReturnAnEmptyCart() {
        cart.removeItem("cream of mushrooms");
        assertEquals(0, cart.itemCount(), "Cart should contain zero items");
    }

}
