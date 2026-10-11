package com.example

import com.example.data.DominosRepository
import com.example.data.OrderSubmissionResult
import com.example.data.OrderValidationError
import com.example.data.SampleTunisiaData
import com.example.model.AppLanguage
import com.example.model.CartItem
import com.example.model.CurrencyFormatter
import com.example.model.OrderStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `currency formatter formats millimes into Tunisian Dinars accurately`() {
        assertEquals("19,900 DT", CurrencyFormatter.formatMillimes(19900, AppLanguage.FR))
        assertEquals("4,000 DT", CurrencyFormatter.formatMillimes(4000, AppLanguage.EN))
        assertTrue(CurrencyFormatter.formatMillimes(18500, AppLanguage.AR).contains("18,500 DT"))
    }

    @Test
    fun `Tunisian phone validation accepts valid 8 digit and +216 numbers and rejects invalid ones`() {
        assertTrue(CurrencyFormatter.isValidTunisianPhone("+216 22 456 789"))
        assertTrue(CurrencyFormatter.isValidTunisianPhone("71800900"))
        assertTrue(CurrencyFormatter.isValidTunisianPhone("00216 55 123 456"))
        assertFalse(CurrencyFormatter.isValidTunisianPhone("12345"))
        assertFalse(CurrencyFormatter.isValidTunisianPhone("abc"))
    }

    @Test
    fun `cart calculations compute subtotal, promo discount, delivery fee, and minimum order`() {
        val repo = DominosRepository()
        val pizza = SampleTunisiaData.initialProducts.first { it.id == "prod-pepperoni-feast" } // 19900
        val cart = listOf(CartItem(id = "c1", product = pizza, quantity = 1))

        val normalSummary = repo.calculateCartTotals(cart, null)
        assertEquals(19900, normalSummary.subtotalMillimes)
        assertEquals(4000, normalSummary.deliveryFeeMillimes)
        assertEquals(0, normalSummary.discountMillimes)
        assertEquals(23900, normalSummary.totalMillimes)
        assertTrue(normalSummary.meetsMinimumOrder)

        val promo30Summary = repo.calculateCartTotals(cart, "FOODIE30")
        assertEquals(5970, promo30Summary.discountMillimes)
        assertEquals(19900 - 5970 + 4000, promo30Summary.totalMillimes)

        val freeShipSummary = repo.calculateCartTotals(cart, "FREESHIP")
        assertEquals(0, freeShipSummary.deliveryFeeMillimes)
        assertEquals(19900, freeShipSummary.totalMillimes)
    }

    @Test
    fun `checkout validation enforces minimum order, restaurant open status, and network failure`() = runTest {
        val repo = DominosRepository()
        val garlicBread = SampleTunisiaData.initialProducts.first { it.id == "prod-cheesy-garlic-bread" } // 7900 < 12000 min

        // 1. Minimum order not met
        val minOrderResult = repo.submitOrder(
            customerName = "Yassine Ben Ali",
            customerPhone = "+216 22 456 789",
            deliveryAddress = "12 Avenue Habib Bourguiba, Tunis",
            deliveryInstructions = "",
            cartItems = listOf(CartItem(id = "c1", product = garlicBread, quantity = 1)),
            appliedPromoCode = null,
            lang = AppLanguage.FR
        )
        assertEquals(
            OrderSubmissionResult.Error(OrderValidationError.MIN_ORDER_NOT_MET),
            minOrderResult
        )

        // 2. Valid COD order in demo mode succeeds
        val validCart = listOf(CartItem(id = "c2", product = garlicBread, quantity = 2)) // 15800 >= 12000
        val successResult = repo.submitOrder(
            customerName = "Yassine Ben Ali",
            customerPhone = "+216 22 456 789",
            deliveryAddress = "12 Avenue Habib Bourguiba, Tunis",
            deliveryInstructions = "2ème étage",
            cartItems = validCart,
            appliedPromoCode = null,
            lang = AppLanguage.FR
        )
        assertTrue(successResult is OrderSubmissionResult.Success)

        // 3. Network failure does not silently succeed
        repo.simulateNetworkFailure = true
        val netFailResult = repo.submitOrder(
            customerName = "Yassine Ben Ali",
            customerPhone = "+216 22 456 789",
            deliveryAddress = "12 Avenue Habib Bourguiba, Tunis",
            deliveryInstructions = "",
            cartItems = validCart,
            appliedPromoCode = null,
            lang = AppLanguage.FR
        )
        assertEquals(
            OrderSubmissionResult.Error(OrderValidationError.NETWORK_UNAVAILABLE),
            netFailResult
        )
    }

    @Test
    fun `admin operations are blocked for customer role and permitted for admin role`() {
        val repo = DominosRepository()
        repo.loginAsDemoCustomer()
        val orderId = repo.orders.value.first().id

        // Customer cannot change order status
        assertFalse(repo.adminUpdateOrderStatus(orderId, OrderStatus.DELIVERED))

        // Admin can change order status
        repo.loginAsDemoAdmin()
        assertTrue(repo.adminUpdateOrderStatus(orderId, OrderStatus.DELIVERED))
        assertEquals(OrderStatus.DELIVERED, repo.orders.value.first { it.id == orderId }.status)
    }

    @Test
    fun `signUp and login always succeed for saber bradai account`() = runTest {
        val repo = DominosRepository()
        val signedUp = repo.signUp(
            fullName = "saber bradai",
            email = "saber@gmail.com",
            phone = "51121136",
            password = "password123"
        )
        assertTrue(signedUp)
        assertEquals("saber bradai", repo.currentUser.value?.displayName)
        assertEquals("saber@gmail.com", repo.currentUser.value?.email)
        assertEquals("51121136", repo.currentUser.value?.phone)
    }
}
