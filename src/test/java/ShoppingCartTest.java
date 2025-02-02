import com.example.shopping.ShoppingCart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShoppingCartTest {
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        cart = new ShoppingCart();
    }

    @Test
    @DisplayName("Adding an item to to ShoppingCart should return one item")
    void AddingAnItemToShoppingCartShouldReturnOneItem() {
        cart.addItem("cream of mushrooms", 25.50, 22, 0);
        assertEquals(1, cart.itemCount(), "Cart should contain only one item");
    }

    @Test
    @DisplayName("Deleting an item should return an empty cart")
    void DeletingAnItemShouldReturnAnEmptyCart() {
        cart.removeItem("cream of mushrooms");
        assertEquals(0, cart.itemCount(), "Cart should contain zero items");
    }

    @Test
    @DisplayName("Adding the same item name in cart should increase quantity of the object")
    void AddingTheSameItemNameInCartShouldIncreaseQuantityOfTheObject() {
        cart.addItem("cream of salad", 20.90, 22, 0);
        cart.addItem("cream of salad", 20.90, 1, 0);
        assertEquals(23, cart.getQuantity("cream of salad"), "Quantity should be the total of the two objects");
    }

    @Test
    @DisplayName("Should return total price")
    void ShouldReturnTotalPrice() {
        cart.addItem("cream of mushrooms", 25.50, 13, 0);
        cart.addItem("cream of boar", 20.90, 3, 0);
        assertEquals(394.20, cart.getTotalPrice(), "Total price should be the total of items quality x the items price");
    }

    @Test
    @DisplayName("Adding a discount should return price with discount")
    void AddingADiscountShouldReturnPriceWithDiscount() {
        cart.addItem("cream of salad", 30.50, 1, 30);
        cart.addItem("cream of beans", 18.50, 2, 50);
        assertEquals(39.85, cart.getTotalPrice(), "Items should have a discounts");
    }

    @Test
    @DisplayName("Adding item with zero quantity should not add an item to cart")
    void addingItemWithZeroQuantityShouldNotAddAnItemToCart() {
        cart.addItem("cream of deer", 25.50, 0,0);
        assertEquals(0, cart.itemCount(), "Cart should contain zero items");
    }

    @Test
    @DisplayName("Updated quantity on item should return new quantity value")
    void UpdatedQuantityOnItemShouldReturnQuantityValue() {
        cart.addItem("cream of intestine", 50.40, 10, 0);
        cart.updateQuantity("cream of intestine", 3);
        assertEquals(3, cart.getQuantity("cream of intestine"), "Item should contain the update of the quantity");
    }

    @Test
    @DisplayName("Updating quantity to zero should remove the item from the cart")
    void UpdatingQuantityToZeroShouldRemoveTheItemFromTheCart() {
        cart.addItem("cream of intestine", 50.40, 10, 0);
        cart.updateQuantity("cream of intestine", 0);
        assertEquals(0, cart.itemCount(), "Cart should contain zero items after setting quantity to zero");
    }

    @Test
    @DisplayName("Adding an item with negative quantity should not add the item to the cart")
    void AddingAnItemWithNegativeQuantityShouldNotAddTheItemToTheCart() {
        cart.addItem("cream of deer", 25.50, -1, 0);
        assertEquals(0, cart.itemCount(), "Cart should contain zero items when adding with negative quantity");
    }

    @Test
    @DisplayName("getQuantity should return the correct positive quantity")
    void GetQuantityShouldReturnCorrectPositiveQuantity() {
        cart.addItem("cream of mushrooms", 25.50, 10, 0);
        assertEquals(10, cart.getQuantity("cream of mushrooms"), "Quantity should be 10 when initialized with 10");
    }

    @Test
    @DisplayName("customer should not be able to put more than 6 cream of boar in the cart")
    void customerShouldNotBeAbleToPutMoreThan6CreamOfBarInCart() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->{
            cart.addItem("cream of boar", 25.50, 7, 0);
        });

        String exceptionMessage = "We can only provide our customers with 6 cream of boars maximum per person";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(exceptionMessage));
    }




}
