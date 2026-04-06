## Opaque Types

Opaque types are a feature introduced in Scala 3 which let you give values a name and meaning that the compiler enforces, preventing accidental misuse at compile time without paying any runtime cost.

An opaque type is defined using the `opaque` keyword.

```scala mdoc
opaque type Age = Int
```

```scala mdoc
val age: Age = 30
```

### Why “opaque”?

These types are called opaque because outside their defining scope, the compiler cannot see their underlying representation.


### What problem does it solve?

The "Stringly-typed" problem

Without opaque types:

```scala mdoc
case class User(id: String, name: String)
def charge(cardId: String): Unit   = println(s"Charging card $cardId")
def loadUser(userId: String): User = User(userId, "John Doe")
```

By using String types for both cardId and userId you can pass the userId to the charge function and vice versa, which is wrong.
```scala mdoc
val userId = "123"
charge(userId) // compiles but is totally wrong
```

By using Opaque Types you can prevent these mistakes:
```scala mdoc:nest
object OpaqueTypes:
  opaque type CardId = String
  opaque type UserId = String

  def charge(cardId: CardId): Unit   = println(s"Charging card $cardId")
  def loadUser(userId: UserId): User = User(userId.toString, "John Doe")
  val userId: UserId                 = "123"
```

```scala mdoc:fail 
OpaqueTypes.charge(OpaqueTypes.userId)
```

Another example is a large case class model with multiple fields of the same type. 
```scala mdoc
case class RocketLaunchConfig(
  powerOn: Boolean,
  launchPadReady: Boolean,
  selfDestruct: Boolean,
  fuelLevel: Int,
  temperature: Int,
  pressure: Int,
  humidity: Int,
  windSpeed: Int,
  windDirection: Int,
  cloudCover: Int,
  visibility: Int,
  weatherCondition: String,
)
```
With this many fields, it's easy enough to make a mistake. For example it would be fairly easy to accidentally pass in the value for **launchPadReady** to the **selfDestruct** field which would likely spell the end of the rocket as we know it. We want to ensure there is no way in which the value for **launchPadReady** can be passed to the **selfDestruct** field. 
By using Opaque Types you can create new types to represent the different fields:

```scala mdoc:reset
object Rocket:

  opaque type PowerOn          = Boolean
  opaque type LaunchPadReady   = Boolean
  opaque type SelfDestruct     = Boolean
  opaque type FuelLevel        = Int
  opaque type Temperature      = Int
  opaque type Pressure         = Int
  opaque type Humidity         = Int
  opaque type WindSpeed        = Int
  opaque type WindDirection    = Int
  opaque type CloudCover       = Int
  opaque type Visibility       = Int
  opaque type WeatherCondition = String

  case class RocketLaunchConfig(
    powerOn: PowerOn,
    launchPadReady: LaunchPadReady,
    selfDestruct: SelfDestruct,
    fuelLevel: FuelLevel,
    temperature: Temperature,
    pressure: Pressure,
    humidity: Humidity,
    windSpeed: WindSpeed,
    windDirection: WindDirection,
    cloudCover: CloudCover,
    visibility: Visibility,
    weatherCondition: WeatherCondition,
  )

  val powerOn: PowerOn                   = true
  val launchPadReady: LaunchPadReady     = true
  val selfDestruct: SelfDestruct         = true
  val fuelLevel: FuelLevel               = 100
  val temperature: Temperature           = 20
  val pressure: Pressure                 = 1000
  val humidity: Humidity                 = 50
  val windSpeed: WindSpeed               = 10
  val windDirection: WindDirection       = 10
  val cloudCover: CloudCover             = 10
  val visibility: Visibility             = 10
  val weatherCondition: WeatherCondition = "sunny"
```

Now if you try to pass in the value for **launchPadReady** to the **selfDestruct** field, the compiler will error (we're outside `Rocket`, so the types are opaque and distinct):
```scala mdoc:fail
val config = Rocket.RocketLaunchConfig(
  Rocket.powerOn,
  Rocket.launchPadReady,
  Rocket.launchPadReady, // wrong type: compiler error
  Rocket.fuelLevel,
  Rocket.temperature,
  Rocket.pressure,
  Rocket.humidity,
  Rocket.windSpeed,
  Rocket.windDirection,
  Rocket.cloudCover,
  Rocket.visibility,
  Rocket.weatherCondition,
)
```
### When should you use opaque types?

Opaque types are perfect for:

- IDs (UserId, OrderId)
- Value wrappers (Email, Url)
- Domain concepts (ApproximateDate, Action)
- Anything where meaning matters more than structure

Avoid for:

- Data with multiple fields
- Behaviour-heavy objects
- Things that genuinely need identity
