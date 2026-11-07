package com.example

import com.example.data.model.CurrencyCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyCatalogTest {

  @Test
  fun testCurrencyCatalogCompleteness() {
    assertTrue(CurrencyCatalog.ALL_CURRENCIES.size >= 100)
    val usd = CurrencyCatalog.find("USD")
    assertEquals("USD", usd.code)
    assertEquals("$", usd.symbol)

    val ngn = CurrencyCatalog.find("NGN")
    assertEquals("NGN", ngn.code)
    assertEquals("₦", ngn.symbol)

    val eur = CurrencyCatalog.find("EUR")
    assertEquals("EUR", eur.code)
    assertEquals("€", eur.symbol)
  }

  @Test
  fun testDefaultExchangeRates() {
    assertTrue(CurrencyCatalog.DEFAULT_RATES_USD.containsKey("USD"))
    assertEquals(1.0, CurrencyCatalog.DEFAULT_RATES_USD["USD"] ?: 0.0, 0.0001)
    assertTrue((CurrencyCatalog.DEFAULT_RATES_USD["NGN"] ?: 0.0) > 1000.0)
  }

  @Test
  fun testFlagEmojiGeneration() {
    val flagUs = CurrencyCatalog.flagFromCountryCode("US")
    assertNotNull(flagUs)
    val flagGb = CurrencyCatalog.flagFromCountryCode("GB")
    assertNotNull(flagGb)
  }
}
