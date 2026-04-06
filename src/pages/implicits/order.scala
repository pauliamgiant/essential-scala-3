final case class Order(units: Int, unitPrice: Double):
  val totalPrice: Double = units * unitPrice

object Order:
  given lessThanOrdering: Ordering[Order] =
    Ordering.fromLessThan[Order]: (x, y) =>
      x.totalPrice < y.totalPrice

object OrderUnitPriceOrdering:
  given unitPriceOrdering: Ordering[Order] =
    Ordering.fromLessThan[Order]: (x, y) =>
      x.unitPrice < y.unitPrice

object OrderUnitsOrdering:
  given unitsOrdering: Ordering[Order] =
    Ordering.fromLessThan[Order]: (x, y) =>
      x.units < y.units
