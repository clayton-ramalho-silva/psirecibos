package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testReceiptTotalCalculation() {
    val quantity = 4
    val unitPrice = 200.0
    val discount = 50.0
    val total = (quantity * unitPrice) - discount
    assertEquals(750.0, total, 0.001)
  }

  @Test
  fun testReceiptNumberFormat() {
    val year = 2026
    val count = 4
    val receiptNumber = "REC-$year-${count.toString().padStart(3, '0')}"
    assertEquals("REC-2026-004", receiptNumber)
  }
}
