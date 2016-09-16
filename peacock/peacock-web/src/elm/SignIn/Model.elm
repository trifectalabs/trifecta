module SignIn.Model where


type alias Model =
  { firstName : Maybe String
  , lastName : Maybe String
  , email : String
  , password : String
  }
